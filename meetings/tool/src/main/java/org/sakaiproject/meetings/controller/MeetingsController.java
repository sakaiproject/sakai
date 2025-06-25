/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.meetings.controller;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.meetings.api.MeetingService;
import org.sakaiproject.meetings.api.model.*;
import org.sakaiproject.meetings.controller.data.GroupData;
import org.sakaiproject.meetings.controller.data.MeetingData;
import org.sakaiproject.meetings.controller.data.NotificationType;
import org.sakaiproject.meetings.controller.data.ParticipantData;
import org.sakaiproject.meetings.exceptions.MeetingsException;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.SakaiProxy;
import org.sakaiproject.microsoft.api.data.*;
import org.sakaiproject.microsoft.api.exceptions.MicrosoftCredentialsException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;


/**
 * MainController
 * 
 * This is the controller used by Spring MVC to handle requests
 * 
 */
@SuppressWarnings("deprecation")
@Slf4j
@RestController
public class MeetingsController {

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("Messages");

	@Autowired
	private ServerConfigurationService serverConfigurationService;

	@Autowired
	private MeetingService meetingService;
	
	@Autowired
	private MicrosoftCommonService microsoftCommonService;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private MicrosoftSynchronizationService microsoftSynchronizationService;
	
	@Autowired
	private SakaiProxy sakaiProxy;
	
	private static final String MS_TEAMS = "microsoft_teams";
	private static final String ONLINE_MEETING_ID = "onlineMeetingId";
	private static final String ORGANIZER_USER = "organizerUser";
	private static final String CALENDAR_EVENT_ID = "calendarEventId";
	private static final String MEETING_EVENT_TYPE = "Meeting";
	private static final String NOTIF_SUBJECT = "notification.subject";
	private static final String NOTIF_CONTENT = "notification.content";
	private static final String SMTP_FROM = "smtpFrom@org.sakaiproject.email.api.EmailService";
	private static final String NO_REPLY = "no-reply@";
	private static final String REPORT_FORMAT_CSV = "csv";
	private static final String ATTENDANCE_REPORT_FILENAME = "attendance_report.csv";

	/**
	 * Default meetings properties
	 * @return
	 */
	@GetMapping(value="/config", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Boolean> getConfig() {
		boolean showMeetingBanner = serverConfigurationService.getBoolean( "show.meeting.banner", false);

		return Map.of(
				"showMeetingBanner", showMeetingBanner
		);
	}
	/**
	 * Check if there's an user logged
	 * @return
	 * @throws MeetingsException
	 */
	private void checkSakaiSession() throws MeetingsException {
		if (StringUtils.isBlank(sakaiProxy.getCurrentUserId())) {
			throw new MeetingsException("Unable to get current user");
		}
	}
	
	/**
	 * Retrieves current user permission to edit meetings
	 * @return
	 */
	@GetMapping(value = "/meetings/user/editperms/site/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean canUpdateSite(@PathVariable String siteId) {
		boolean result = false;
		try {
			Site site = sakaiProxy.getSite(siteId);
			
			result = sakaiProxy.canUpdateSite(site.getReference(), sakaiProxy.getCurrentUserId());
		} catch (Exception e) {
			log.error("Error retrieving user permissions", e);
			result = false;
		}
		return result;
	}
	
	/**
	 * 'Update site' permissions check
	 * @param siteId
	 * @return
	 * @throws MeetingsException 
	 */
	private void checkUpdatePermissions(String siteId) throws MeetingsException {
		boolean result = canUpdateSite(siteId);
		if (!result) {
			throw new MeetingsException("User doesn't have permissions to update this site.");
		}
	}
	
	/**
	 * 
	 * @param siteId
	 * @throws MeetingsException
	 */
	private void checkCurrentUserInSite(String siteId) throws MeetingsException {
		if (!sakaiProxy.canCurrentUserAccessSite(siteId)) {
			throw new MeetingsException("Current user has not permissions to see information from site " + siteId);
		}
	}
	
	/**
	 * Check current user permission to see a meeting
	 * @param meetingId
	 * @throws MeetingsException
	 */
	private void checkCurrentUserInMeeting(String meetingId) throws MeetingsException {
		if (sakaiProxy.isAdmin()) {
			return;
		}
		List<Meeting> result = new ArrayList<>();
		try {
			Meeting meeting = meetingService.getMeeting(meetingId);
			if(canUpdateSite(meeting.getSiteId())) {
				return;
			}
			
			Site site = sakaiProxy.getSite(meeting.getSiteId());
			String userId = sakaiProxy.getCurrentUserId();
			String siteId = site.getId();
			List<Group> groups = new ArrayList<>();
			groups.addAll(site.getGroupsWithMember(userId));
			site.getGroupsWithMember(userId);
			List<String> groupIds = groups.stream().map(e->e.getId()).collect(Collectors.toList());
			List<Meeting> meetingList = meetingService.getUserMeetings(userId, siteId, groupIds);
			result = meetingList.stream().filter(item -> meetingId.equals(item.getId())).collect(Collectors.toList());
		} catch(Exception e) {}
		
		if (result.size() == 0) {
			throw new MeetingsException("Current user does not have permission to see this meeting.");
		}
	}
	
	/**
	 * Method to evaluate if there is a calendar tool added to a site
	 * @param siteId
	 * @return
	 * @throws MeetingsException 
	 */
	@GetMapping(value = "/meetings/site/{siteId}/existcalendar", produces = MediaType.APPLICATION_JSON_VALUE)
	private boolean isThereAnyCalendarForSite(@PathVariable String siteId) throws MeetingsException {
		checkCurrentUserInSite(siteId);
		
		String calReference = "/calendar/calendar/" + siteId + "/main";
		return sakaiProxy.existsCalendar(calReference);
	}
	
	/**
	 * Retrieves the groups list from a site
	 * @param siteId
	 * @return
	 * @throws MeetingsException
	 */
	@GetMapping(value = "/meetings/site/{siteId}/groups", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<GroupData> getSiteGroups(@PathVariable String siteId) throws MeetingsException {
		checkCurrentUserInSite(siteId);
		List<GroupData> siteGroups = new ArrayList<>();
		Site site = sakaiProxy.getSite(siteId);
		if(site != null) {
			Collection<Group> groups = site.getGroups();
			groups.stream().forEach(group -> {
				GroupData data = new GroupData();
				data.setGroupId(group.getId());
				data.setGroupName(group.getTitle());
				siteGroups.add(data);
			});
		} else {
			log.error("Error retrieving groups");
			throw new MeetingsException("Error retrieving groups");
		}
		return siteGroups;
	}
	
	/**
	 * Method to retrieve the list of participants in a meeting
	 * @param meetingId
	 * @return
	 * @throws MeetingsException
	 */
	@GetMapping(value = "/meeting/{meetingId}/participants", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ParticipantData> getParticipants(@PathVariable String meetingId) throws MeetingsException {
		checkCurrentUserInMeeting(meetingId);
		final List<ParticipantData> participants = new ArrayList<>();
		Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
		if (optMeeting.isPresent()) {
			Meeting meeting = optMeeting.get();
			checkCurrentUserInSite(meeting.getSiteId());
			Site site = sakaiProxy.getSite(meeting.getSiteId());
			if(site != null) {
				for (MeetingAttendee attendee : meeting.getAttendees()) {
					switch (attendee.getType()) {
					case USER:
						User user = sakaiProxy.getUser(attendee.getObjectId());
						if(user != null) {
							ParticipantData participant = new ParticipantData();
							participant.setUserid(user.getId());
							participant.setName(user.getDisplayName());
							participants.add(participant);
						} else {
							log.error("Error retrieving participants");
						}
						break;

					case SITE:
						site.getMembers().stream()
							.sorted((o1, o2) -> o1.getUserId().compareTo(o2.getUserId()))
							.forEach(member -> {
								ParticipantData siteParticipant = new ParticipantData();
								User siteUser = sakaiProxy.getUser(member.getUserId());
								if(siteUser != null) {
									siteParticipant.setUserid(siteUser.getId());
									siteParticipant.setName(siteUser.getDisplayName());
									participants.add(siteParticipant);
								} else {
									log.error("Error retrieving participants (SITE): userId={}", member.getUserId());
								}
							});
						break;

					case GROUP:
						site.getMembersInGroups(Collections.singleton(attendee.getObjectId()))
							.stream().forEach(userId -> {
								ParticipantData groupParticipant = new ParticipantData();
								User groupUser = sakaiProxy.getUser(userId);
								if(groupUser != null) {
									groupParticipant.setUserid(groupUser.getId());
									groupParticipant.setName(groupUser.getDisplayName());
									participants.add(groupParticipant);
								} else {
									log.error("Error retrieving participants (GROUP): userId={}", userId);
								}
							});
						break;
					default: break;
					}
				}
				return participants.stream().distinct().collect(Collectors.toList());
			} else {
				log.error("Error retrieving participants");
				throw new MeetingsException("Error retrieving participants");
			}
		}
		return participants;
	}

	/**
	 * Retrieves all current user meetings
	 * @return
	 * @throws MeetingsException 
	 */
	@GetMapping(value = "/meetings/site/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Iterable<MeetingData> getSiteMeetings(@PathVariable String siteId) throws MeetingsException {
		checkCurrentUserInSite(siteId);
		// Retrieve meetings for which the user has permission 
		String userId = sakaiProxy.getCurrentUserId();
		List<Meeting> meetingList = null;
		if (sakaiProxy.isAdmin() || canUpdateSite(siteId)) {
			meetingList = meetingService.getAllMeetingsFromSite(siteId);
		} else {
			try {
				Site site = sakaiProxy.getSite(siteId);
				List<Group> groups = new ArrayList<>();
				groups.addAll(site.getGroupsWithMember(userId));
				site.getGroupsWithMember(userId);
				List<String> groupIds = groups.stream().map(e->e.getId()).collect(Collectors.toList());
				meetingList = meetingService.getUserMeetings(userId, siteId, groupIds);   
			} catch (Exception e) {
				log.error("Error while retrieving group list on Meetings", e);
			}
		}
		// Compose the data to send to the frontend
		List<MeetingData> data = new ArrayList<>();
		meetingList.stream().forEach(meeting -> {
		   MeetingData item = new MeetingData();
		   BeanUtils.copyProperties(meeting, item);
		   item.setStartDate(meeting.getStartDate().toString());
		   item.setEndDate(meeting.getEndDate().toString());
		   data.add(item);
		});
		
		return data;
	}
	
	@GetMapping(value = "/meeting/{meetingId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public MeetingData getMeeting(@PathVariable String meetingId) throws MeetingsException {
		checkCurrentUserInMeeting(meetingId);
		Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
		if (optMeeting.isPresent()) {
			final MeetingData data = new MeetingData();
			Meeting meeting = optMeeting.get();
			BeanUtils.copyProperties(meeting, data);
			data.setStartDate(meeting.getStartDate().toString());
			data.setEndDate(meeting.getEndDate().toString());
			List<String> meetingGroupIds = new ArrayList<String>();
			meeting.getAttendees().stream().forEach(attendee -> {
				switch (attendee.getType()) {
					case SITE:
						data.setParticipantOption(attendee.getType());
						break;
					case GROUP:
						data.setParticipantOption(attendee.getType());
						meetingGroupIds.add(attendee.getObjectId());
						break;
					default: break;
				}
			});
			data.setGroupSelection(meetingGroupIds);
			String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
			data.setSaveToCalendar(StringUtils.isNotBlank(calendarEventId));
			data.setParticipants(getParticipants(meetingId));
			return data;
		}
		return null;
	}
	
	
	/**
	 * Method to save a new meeting
	 * @param data
	 * @return
	 * @throws MeetingsException
	 */
	@PostMapping(value = "/meeting", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Meeting createMeeting(@RequestBody MeetingData data) throws MeetingsException {
		checkUpdatePermissions(data.getSiteId());
		Meeting meeting = null;
		User user = sakaiProxy.getCurrentUser();
		try {
			// Meeting info
			meeting = new Meeting();
			BeanUtils.copyProperties(data, meeting);
			meeting.setStartDate(Instant.parse(data.getStartDate()).truncatedTo(ChronoUnit.SECONDS));
			meeting.setEndDate(Instant.parse(data.getEndDate()).truncatedTo(ChronoUnit.SECONDS));
			meeting.setOwnerId(user.getId());
			// Online meeting creation with the selected provider
			String onlineMeetingId = null;
			String onlineMeetingUrl = null;
			List<String> coorganizerEmails = new ArrayList<>();
			if (MS_TEAMS.equals(data.getProvider())) {
				if (data.isCoorganizersEnabled()) {
					List<Member> coorganizers = sakaiProxy.getSite(data.getSiteId()).getMembers()
							.stream()
							.filter(u -> {
								boolean canUpdate = sakaiProxy.canUpdateSite("/site/" + data.getSiteId(), u.getUserId());
								log.debug("User: " + u.getUserId() + " canUpdate: " + canUpdate);
								return canUpdate;
							})
							.collect(Collectors.toList());

					coorganizers.forEach(c -> log.debug("Filtered Coorganizer: " + c.getUserId()));

					coorganizerEmails = coorganizers.stream()
							.map(member -> sakaiProxy.getUser(member.getUserId()).getEmail())
							.filter(StringUtils::isNotEmpty)
							.collect(Collectors.toList());
				}
				TeamsMeetingData meetingTeams = microsoftCommonService.createOnlineMeeting(user.getEmail(), meeting.getTitle(), meeting.getStartDate(), meeting.getEndDate(), coorganizerEmails);
				onlineMeetingUrl = meetingTeams.getJoinUrl();
				onlineMeetingId = meetingTeams.getId();
			}
			meeting.setUrl(onlineMeetingUrl);
			// Participants
			MeetingAttendee attendee = new MeetingAttendee();
			List<MeetingAttendee> meetingAttendees = new ArrayList<MeetingAttendee>();
			attendee.setType(AttendeeType.USER);
			attendee.setObjectId(user.getId());
			meetingAttendees.add(attendee);
			attendee.setMeeting(meeting);
			switch (data.getParticipantOption()) {
				case SITE:
					attendee = new MeetingAttendee();
					attendee.setType(AttendeeType.SITE);
					attendee.setObjectId(data.getSiteId());
					meetingAttendees.add(attendee);
					attendee.setMeeting(meeting);
					break;
				case GROUP:
					for (String groupId : data.getGroupSelection()) {
						attendee = new MeetingAttendee();
						attendee.setType(AttendeeType.GROUP);
						attendee.setObjectId(groupId);
						meetingAttendees.add(attendee);
						attendee.setMeeting(meeting);   
					}
					break;
				default:
					break;
			}
			meeting.setAttendees(meetingAttendees);
			// Meeting creation
			meeting = meetingService.createMeeting(meeting);
			// Properties
			meetingService.setMeetingProperty(meeting, ORGANIZER_USER, user.getEmail());
			meetingService.setMeetingProperty(meeting, ONLINE_MEETING_ID, onlineMeetingId);
			// Calendar events
			if (data.isSaveToCalendar() && isThereAnyCalendarForSite(data.getSiteId())
					&& StringUtils.isNotBlank(data.getStartDate()) && StringUtils.isNotBlank(data.getEndDate())) {
				this.saveToCalendar(meeting);
			}
			// Notifications
			this.sendNotification(meeting, data.getNotificationType());
		} catch (DateTimeParseException e) {
			log.error("Could not parse Meetings start date string '{}' or end time string '{}'", meeting.getStartDate(), meeting.getEndDate());
			throw new MeetingsException(e.getLocalizedMessage());
		} catch (IdUnusedException e) {
			log.error("Error retrieving site when sending notifications.", e);
			throw new MeetingsException(e.getLocalizedMessage());
		} catch (MicrosoftCredentialsException e) {
			log.error("Error creating meeting", e);
			throw new MeetingsException(e.getLocalizedMessage());
		}
		return meeting;
	}
	
	/**
	 * Method to update an existing meeting
	 * @param data
	 * @param meetingId
	 * @return
	 * @throws MeetingsException 
	 */
	@PutMapping(value = "/meeting/{meetingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Meeting updateMeeting(@RequestBody MeetingData data, @PathVariable String meetingId) throws MeetingsException {
		checkUpdatePermissions(data.getSiteId());
		checkCurrentUserInMeeting(meetingId);
		Meeting meeting = null;
		try {
			// Remove site and group attendees
			meetingService.removeSiteAndGroupAttendeesByMeetingId(meetingId);
			meeting = meetingService.getMeeting(meetingId);
			
			// Meeting info
			meeting.setTitle(data.getTitle());
			meeting.setDescription(data.getDescription());
			meeting.setStartDate(Instant.parse(data.getStartDate()).truncatedTo(ChronoUnit.SECONDS));
			meeting.setEndDate(Instant.parse(data.getEndDate()).truncatedTo(ChronoUnit.SECONDS));

			List<String> coorganizerEmails = new ArrayList<>();
			if (MS_TEAMS.equals(data.getProvider())) {
				String organizerEmail = meetingService.getMeetingProperty(meeting, ORGANIZER_USER);
				String onlineMeetingId = meetingService.getMeetingProperty(meeting, ONLINE_MEETING_ID);

				// Updating coorganizers
				if (data.isCoorganizersEnabled()) {
					List<Member> coorganizers = sakaiProxy.getSite(data.getSiteId()).getMembers()
							.stream()
							.filter(u -> {
								boolean canUpdate = sakaiProxy.canUpdateSite("/site/" + data.getSiteId(), u.getUserId());
								log.debug("User: " + u.getUserId() + " canUpdate: " + canUpdate);
								return canUpdate;
							})
							.collect(Collectors.toList());

					coorganizers.forEach(c -> log.debug("Filtered Coorganizer: " + c.getUserId()));

					coorganizerEmails = coorganizers.stream()
							.map(member -> sakaiProxy.getUser(member.getUserId()).getEmail())
							.filter(StringUtils::isNotEmpty)
							.collect(Collectors.toList());
				}
				if(StringUtils.isNotBlank(onlineMeetingId)) {
					microsoftCommonService.updateOnlineMeeting(organizerEmail, onlineMeetingId, meeting.getTitle(), meeting.getStartDate(), meeting.getEndDate(), coorganizerEmails);
				}
			}
			
			// Participants
			MeetingAttendee attendee = null;
			switch (data.getParticipantOption()) {
				case SITE:
					attendee = new MeetingAttendee();
					attendee.setType(AttendeeType.SITE);
					attendee.setObjectId(data.getSiteId());
					meeting.getAttendees().add(attendee);
					attendee.setMeeting(meeting);
					break;
				case GROUP:
					for (String groupId : data.getGroupSelection()) {
						attendee = new MeetingAttendee();
						attendee.setType(AttendeeType.GROUP);
						attendee.setObjectId(groupId);
						meeting.getAttendees().add(attendee);
						attendee.setMeeting(meeting);
					}
					break;
				default:
					break;
			}
			// Update meeting
			meetingService.updateMeeting(meeting);
			
			// Calendar events
			if (data.isSaveToCalendar() && isThereAnyCalendarForSite(data.getSiteId())
					&& StringUtils.isNotBlank(data.getStartDate()) && StringUtils.isNotBlank(data.getEndDate())) {
				this.saveToCalendar(meeting);
			}
			if(!data.isSaveToCalendar() && StringUtils.isNotBlank(meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID))) {
				removeFromCalendar(meetingId);
				meetingService.removeMeetingProperty(meeting, CALENDAR_EVENT_ID);
			}
			
			// Notifications
			this.sendNotification(meeting, data.getNotificationType());
		} catch (DateTimeParseException e) {
			log.error("Could not parse Meetings start date string '{}' or end time string '{}'", meeting.getStartDate(), meeting.getEndDate());
			throw new MeetingsException(e.getLocalizedMessage());
		} catch (IdUnusedException e) {
			log.error("Error retrieving site when sending notifications.", e);
			throw new MeetingsException(e.getLocalizedMessage());
		} catch (MicrosoftCredentialsException e) {
			log.error("Error updating meeting", e);
			throw new MeetingsException(e.getLocalizedMessage());
		} catch (Exception e) {
			log.error("Error updating meeting", e);
			throw new MeetingsException(e.getLocalizedMessage());
		}
		return meeting;
	}
	
	/**
	 * Method to remove an existing meeting
	 * @param meetingId
	 * @throws MeetingsException
	 */
	@DeleteMapping(value = "/meeting/{meetingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public void deleteMeeting(@PathVariable String meetingId) throws MeetingsException {
		checkCurrentUserInMeeting(meetingId);
		Meeting meeting = meetingService.getMeeting(meetingId);
		checkUpdatePermissions(meeting.getSiteId());
		try {
			this.removeFromCalendar(meetingId);
			meetingService.deleteMeetingById(meetingId);
		} catch (Exception e) {
			log.error("Error deleting meeting", e);
			throw new MeetingsException(e.getLocalizedMessage());
		}
	}

	@GetMapping(value = "/meeting/{meetingId}/attendanceReport", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getMeetingAttendanceReport(@PathVariable String meetingId, @RequestParam(required = false) String format) throws MeetingsException {
		checkCurrentUserInMeeting(meetingId);
		Meeting meeting = meetingService.getMeeting(meetingId);
		String onlineMeetingId = meetingService.getMeetingProperty(meeting, ONLINE_MEETING_ID);
		String organizerEmail = meetingService.getMeetingProperty(meeting, ORGANIZER_USER);
		checkUpdatePermissions(meeting.getSiteId());
		List<String> columnNames = Arrays.asList(
				rb.getString("meeting.column_name"),
				rb.getString("meeting.column_email"),
				rb.getString("meeting.column_role"),
				rb.getString("meeting.column_duration"),
				rb.getString("meeting.entry_date"),
				rb.getString("meeting.exit_date"),
				rb.getString("meeting.interval_duration")
		);

		try {
			List<AttendanceRecord> attendanceRecords = microsoftCommonService.getMeetingAttendanceReport(onlineMeetingId, organizerEmail);
			if (REPORT_FORMAT_CSV.equalsIgnoreCase(format)) {
				byte[] csvContent = microsoftCommonService.createAttendanceReportCsv(attendanceRecords, columnNames);
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ATTENDANCE_REPORT_FILENAME + "\"")
						.contentType(MediaType.TEXT_PLAIN)
						.body(csvContent);
			} else {
				return ResponseEntity.ok(attendanceRecords);
			}
		} catch (Exception e) {
			log.error("Error when obtaining the attendance report", e);
			throw new MeetingsException(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Get i18n bundle
	 * @param bundle
	 * @param locale
	 * @return
	 * @throws MeetingsException 
	 */
	@GetMapping(value = "/i18n/{locale}/{bundle}", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getI18nProperties(@PathVariable String bundle, @PathVariable String locale) throws MeetingsException {
		checkSakaiSession();
		StringBuilder i18n = new StringBuilder();
		if (StringUtils.isNotBlank(bundle) && StringUtils.isNotBlank(locale)) {
			ResourceLoader rbundle = new ResourceLoader(bundle);
			if (rbundle != null) {
				rbundle.setContextLocale(Locale.forLanguageTag(locale));
				rbundle.forEach((k, v) -> i18n.append(k).append("=").append(v).append("\n"));
			}
		}
		return i18n.toString();
	}
	
	/**
	 * Returns true if MS Teams is set up in Sakai properties
	 * @return
	 * @throws MeetingsException
	 */
	@GetMapping(value = "/meetings/teams/status", produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean isMicrosofTeamsConfigured() throws MeetingsException {
		checkSakaiSession();
		try {
			microsoftCommonService.checkConnection();
			return true;
		} catch (MicrosoftCredentialsException e) {}
		return false;
	}
	
	/**
	 * Method to send notifications to users about meetings, by level of priority
	 * @param meeting
	 * @param type
	 * @throws IdUnusedException 
	 * @throws AddressException 
	 */
	private void sendNotification(Meeting meeting, NotificationType type) throws IdUnusedException {
		if (type == NotificationType.ALL && meeting != null) {
			Site site = sakaiProxy.getSite(meeting.getSiteId());
			if(site == null) {
				throw new IdUnusedException(meeting.getSiteId());
			}
			String from = sakaiProxy.getString(SMTP_FROM, NO_REPLY + sakaiProxy.getServerName());
			String subject = MessageFormat.format(rb.getString(NOTIF_SUBJECT), meeting.getTitle(), site.getTitle());
			String content = MessageFormat.format(rb.getString(NOTIF_CONTENT), meeting.getTitle(), site.getTitle());
			Set<Member> members = site.getMembers();
			List<String> participantEmails = members.stream().map(member -> {
				String email = null;
				User user = sakaiProxy.getUser(member.getUserId());
				if(user != null) {
					email = user.getEmail();
				} else {
					log.warn("Member {} does not exist as a user.", member.getUserId());
				}
				return email;
			}).collect(Collectors.toList());
			sakaiProxy.sendMail(from, participantEmails, subject, content);
		}
	}
	
	/**
	 * Method to save a meeting as an event of the Sakai calendar
	 * @param meeting
	 * @throws MeetingsException
	 */
	private void saveToCalendar(Meeting meeting) {
		try {
			String calReference = "/calendar/calendar/" + meeting.getSiteId() + "/main";
			String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
			long init = meeting.getStartDate().toEpochMilli();
			long duration = meeting.getEndDate().toEpochMilli() - init;
			
			List<Group> groups = new ArrayList<Group>();
			Site site = sakaiProxy.getSite(meeting.getSiteId());
			meeting.getAttendees().stream()
				.filter(attendee -> AttendeeType.GROUP.equals(attendee.getType()))
				.forEach(attendee -> {
					groups.add(site.getGroup(attendee.getObjectId()));
				});
			
			String retId = sakaiProxy.saveCalendar(SakaiCalendarEvent.builder()
				.calendarReference(calReference)
				.eventId(calendarEventId)
				.init(init)
				.duration(duration)
				.title(meeting.getTitle())
				.description(meeting.getDescription())
				.type(MEETING_EVENT_TYPE)
				.groups(groups)
				.build()
			);
			if(retId != null) {             
				meetingService.setMeetingProperty(meeting, CALENDAR_EVENT_ID, retId);
			}
		} catch (Exception e) {
			log.error("Error saving calendar");
		}
	}
	
	
	/**
	 * Method to remove a calendar event based on a meeting
	 * @param meetingId
	 * @throws MeetingsException
	 */
	private void removeFromCalendar(String meetingId) throws MeetingsException {
		try {
			Optional<Meeting> optMeeting = meetingService.getMeetingById(meetingId);
			if (optMeeting.isPresent()) {
				Meeting meeting = optMeeting.get();
				String calendarEventId = meetingService.getMeetingProperty(meeting, CALENDAR_EVENT_ID);
				if (StringUtils.isNotBlank(calendarEventId)) {
					sakaiProxy.removeFromCalendar(meeting.getSiteId(), calendarEventId);
				}
			}
		} catch (Exception e) {
			throw new MeetingsException(e.getLocalizedMessage());
		}
	}
	
	// ------------------------------- RECORDINGS -----------------------------------------------
	@GetMapping(value = "/meeting/{meetingId}/recordings", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<MeetingRecordingData> getMeetingRecordings(@PathVariable String meetingId, @RequestParam(defaultValue = "false") Boolean force) throws MeetingsException {
		checkCurrentUserInMeeting(meetingId);
		try {
			Meeting meeting = meetingService.getMeeting(meetingId);
			List<String> teamIdsList = microsoftSynchronizationService.getSiteSynchronizationsBySite(meeting.getSiteId()).stream()
					.map(ss -> ss.getTeamId())
					.collect(Collectors.toList());
			return microsoftCommonService.getOnlineMeetingRecordings(meeting.getMeetingId(), teamIdsList, force);
		} catch (MicrosoftCredentialsException  e) {
			log.error("Error getting meeting recordings", e);
			throw new MeetingsException(e.getLocalizedMessage());
		} catch (Exception e) {
			log.error("Error getting meeting recordings", e);
			throw new MeetingsException(e.getLocalizedMessage());
		}
	}
}
