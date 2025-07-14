/**
 * Copyright (c) 2003-2021 The Apereo Foundation
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
package org.sakaiproject.datemanager.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.datemanager.api.DateManagerConstants;
import org.sakaiproject.datemanager.api.DateManagerService;
import org.sakaiproject.datemanager.api.model.DateManagerError;
import org.sakaiproject.datemanager.api.model.DateManagerUpdate;
import org.sakaiproject.datemanager.api.model.DateManagerValidation;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.SortType;
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentFeedbackIfc;

@Slf4j
public class DateManagerServiceImpl implements DateManagerService {

	@Setter private ToolManager toolManager;
	@Setter private SessionManager sessionManager;
	@Setter private PreferencesService prefService;
	@Setter private AssignmentService assignmentService;
	@Setter private PersistenceService assessmentPersistenceService;
	@Setter private AssessmentFacadeQueriesAPI assessmentServiceQueries;
	@Setter private PublishedAssessmentFacadeQueriesAPI pubAssessmentServiceQueries;
	@Setter private GradingService gradingService;
	@Setter private SignupMeetingService signupService;
	@Setter private ContentHostingService contentHostingService;
	@Setter private CalendarService calendarService;
	@Setter private MessageForumsForumManager forumManager;
	@Setter private AnnouncementService announcementService;
	@Setter private ResourceLoader resourceLoader;
	@Setter private SiteService siteService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SimplePageToolDao simplePageToolDao;
	@Setter private TimeService timeService;
	@Setter private UserTimeService userTimeService;
	@Setter private SamigoAvailableNotificationService samigoAvailableNotificationService;
	@Setter private FormattedText formattedText;

	private final Map<String, Calendar> calendarMap = new HashMap<>();
	private final DateTimeFormatter inputDateFormatter;
	private final DateTimeFormatter inputDateTimeFormatter;
	private final DateTimeFormatter outputDateFormatter;
	private final DateTimeFormatter outputDatePickerFormat;

	public DateManagerServiceImpl() {
		inputDateFormatter = new DateTimeFormatterBuilder()
				.appendOptional(DateTimeFormatter.ofPattern("M/d/yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("d-M-yyyy"))
				.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
				.appendOptional(DateTimeFormatter.ofPattern("yyyy-M-d"))
				.toFormatter();
		inputDateTimeFormatter = new DateTimeFormatterBuilder()
				.appendPattern("yyyy-MM-dd'T'HH:mm:ss")
				.optionalStart()
				.appendOffset("+HH:MM", "Z")  // Add the zone offset
				.optionalEnd()
				.parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
				.toFormatter();
		outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		outputDatePickerFormat = DateTimeFormatter.ofPattern(DateManagerConstants.DATEPICKER_DATETIME_FORMAT);
	}

	public void init() {
		setAssessmentServiceQueries(assessmentPersistenceService.getAssessmentFacadeQueries());
		setPubAssessmentServiceQueries(assessmentPersistenceService.getPublishedAssessmentFacadeQueries());
	}

	private String getCurrentToolSessionAttribute(String name) {
		ToolSession session = sessionManager.getCurrentToolSession();
		return session != null ? session.getAttribute(name).toString() : "";
	}

	@Override
	public String getCurrentSiteId() {
		String siteID = getCurrentToolSessionAttribute(STATE_SITE_ID);
		if (StringUtils.isEmpty(siteID)) {
			siteID = toolManager.getCurrentPlacement().getContext();
		}

		return siteID;
	}

	@Override
	public Optional<Site> getCurrentSite() {
		String siteId = getCurrentSiteId();

		try {
			return Optional.of(siteService.getSite(siteId));
		} catch (Exception ex) {
			log.error("Unable to find the site with Id {}.", siteId);
		}
		return Optional.empty();
	}

	@Override
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	@Override
	public Locale getUserLocale() {
		Locale locale = prefService.getLocale(getCurrentUserId());
		if (locale == null) locale = Locale.US;
		return locale;
	}

	public Locale getLocaleForCurrentSiteAndUser() {
		Locale locale = null;

		// First try to get site locale
		Optional<Site> currentSite = getCurrentSite();
		if (currentSite.isPresent()) {
			ResourceProperties siteProperties = currentSite.get().getProperties();
			String siteLocale = (String) siteProperties.get("locale_string");
			if (StringUtils.isNotBlank(siteLocale)) {
				locale = serverConfigurationService.getLocaleFromString(siteLocale);
			}
		}

		// If there is not site locale defined, get user default locale
		if (locale == null) {
			locale = getUserLocale();
		}

		return locale;
	}

	@Override
	public String getMessage(String messageId) {
		return resourceLoader.getString(messageId);
	}

	@Override
	public boolean currentSiteContainsTool(String commonId) {
		try {
			Site site = siteService.getSite(getCurrentSiteId());
			return (site.getToolForCommonId(commonId) != null);
		} catch(Exception e){
			log.error("siteContainsTool : Cannot find site {}", getCurrentSiteId());
		}
		return false;
	}

	@Override
	public String getToolTitle(String commonId) {
		String toolTitle = "";
		Tool tool = toolManager.getTool(commonId);
		if (tool != null) {
			toolTitle = tool.getTitle();
		}
		return toolTitle;
	}

	private String getUrlForTool(String tool) {
		try {
			Site site = siteService.getSite(getCurrentSiteId());
			return serverConfigurationService.getServerUrl()+"/portal/directtool/"+site.getToolForCommonId(tool).getId();
		} catch(Exception e){
			log.error("getUrlForTool : Error generating {} url {} ", tool, e);
		}
		return null;
	}

	private String formatToUserDateFormat(Date date) {
		if (date == null) return "";
		Instant instant = date.toInstant();
		return formatToUserInstantFormat(instant);
	}
	
	private String formatToUserInstantFormat(Instant instant) {
		if (instant == null) return "";
		ZonedDateTime userDate = ZonedDateTime.ofInstant(instant, userTimeService.getLocalTimeZone().toZoneId());
        return userDate.format(outputDatePickerFormat);
	}

	@Override
	public JSONArray getAssignmentsForContext(String siteId) {
		JSONArray jsonAssignments = new JSONArray();
		Collection<Assignment> assignments = assignmentService.getAssignmentsForContext(siteId);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ASSIGNMENTS).getTitle();
		for(Assignment assignment : assignments) {
			try {
				JSONObject assobj = new JSONObject();
				assobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, assignment.getId());
				assobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, assignment.getTitle());
				assobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(Date.from(assignment.getDueDate())));
				assobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(Date.from(assignment.getOpenDate())));
				assobj.put(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME, formatToUserDateFormat(Date.from(assignment.getCloseDate())));
				assobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
				assobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, assignmentService.getDeepLink(siteId, assignment.getId(), getCurrentUserId()));
				String extraInfo = "false";
				if (assignment.getDraft()) extraInfo = resourceLoader.getString("itemtype.draft");
				assobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, extraInfo);
				jsonAssignments.add(assobj);
			} catch (Exception e) {
				log.error("Error while trying to add assignment {}", assignment.getId(), e);
			}
		}
		return orderJSONArrayByTitle(jsonAssignments);
	}

	@Override
	public DateManagerValidation validateAssignments(String siteId, JSONArray assignments) throws Exception {
		DateManagerValidation assignmentValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ASSIGNMENTS).getTitle();
		for (int i = 0; i < assignments.size(); i++) {
			JSONObject jsonAssignment = (JSONObject)assignments.get(i);
			String assignmentId = (String)jsonAssignment.get(DateManagerConstants.JSON_ID_PARAM_NAME);
			int idx = Integer.parseInt(jsonAssignment.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				if (assignmentId == null) {
                                        errors.add(new DateManagerError("assignment", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.assignments.item.name")), "assignments", toolTitle, idx));
					continue;
				}

				String assignmentReference = assignmentService.assignmentReference(siteId, assignmentId);

				if (!assignmentService.allowUpdateAssignment(assignmentReference)) {
					errors.add(new DateManagerError("assignment", resourceLoader.getString("error.update.permission.denied"), "assignments", toolTitle, idx));
					continue;
				}

                                boolean errored = false;
                                String openDateRaw = (String) jsonAssignment.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
                                String dueDateRaw = (String) jsonAssignment.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);
                                String acceptUntilRaw = (String) jsonAssignment.get(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME);
                                Instant openDate = null;
                                Instant dueDate = null;
                                Instant acceptUntil = null;

				if (StringUtils.isNotBlank(openDateRaw)) {
                                    openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
				}
				if (StringUtils.isNotBlank(dueDateRaw)) {
                                    dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
				}
				if (StringUtils.isNotBlank(acceptUntilRaw)) {
                                    acceptUntil = userTimeService.parseISODateInUserTimezone(acceptUntilRaw).toInstant();
				} 

				if (openDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.not.found"), "assignments", toolTitle, idx));
				}
				if (dueDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, resourceLoader.getString("error.due.date.not.found"), "assignments", toolTitle, idx));
				}
				if (acceptUntil == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME, resourceLoader.getString("error.accept.until.not.found"), "assignments", toolTitle, idx));
				}

				if (errored) {
					continue;
				}

				Assignment assignment = assignmentService.getAssignment(assignmentId);

				if (assignment == null) {
					errors.add(new DateManagerError("assignment", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.assignments.item.name")), "assignments", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(assignment, openDate, dueDate, acceptUntil, null, null);

				if (!update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.before.due.date"), "assignments", toolTitle, idx));
					continue;
				}

				if (update.dueDate.isAfter(update.acceptUntilDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, resourceLoader.getString("error.due.date.before.accept.until"), "assignments", toolTitle, idx));
					continue;
				}

				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "assignments", toolTitle, idx));
				log.error("Error trying to validate Assignments {}", ex.toString());
			}
		}

		assignmentValidate.setErrors(errors);
		assignmentValidate.setUpdates(updates);
		return assignmentValidate;
	}

	@Override
	public void updateAssignments(DateManagerValidation assignmentValidation) throws Exception {
		for (DateManagerUpdate update : assignmentValidation.getUpdates()) {
			Assignment assignment = (Assignment) update.object;
			assignment.setOpenDate(update.openDate);
			assignment.setDueDate(update.dueDate);
			assignment.setCloseDate(update.acceptUntilDate);
			assignmentService.updateAssignment(assignment);

			// if assignment sending grades to gradebook, update the due date in the gradebook
			String associatedGradebookAssignment = assignment.getProperties().get(AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
			if (StringUtils.isNotBlank(associatedGradebookAssignment)) {
				// only update externally linked assignments since internal links are already handled
				String gradebookUid = assignment.getContext();

				List<String> contextIds = new ArrayList<>();
				contextIds.add(gradebookUid);
				if (gradingService.isGradebookGroupEnabled(getCurrentSiteId())) {
					contextIds = new ArrayList<>();
					List<Gradebook> gradebooks = gradingService.getGradebookGroupInstances(gradebookUid);
					for (Gradebook gradebook : gradebooks) {
						List<org.sakaiproject.grading.api.Assignment> groupAssignments = gradingService.getAssignments(gradebook.getUid().toString(), getCurrentSiteId(), SortType.SORT_BY_NONE);
						for (org.sakaiproject.grading.api.Assignment assignmentAux : groupAssignments) {
							if (assignmentAux.getExternalId() != null && assignmentAux.getExternalId().equals(associatedGradebookAssignment)) {
								contextIds.add(gradebook.getUid());
								gradebookUid = gradebook.getUid();
							}
						}
					}
				}

				if (gradingService.isExternalAssignmentDefined(gradebookUid, associatedGradebookAssignment)) {
					for (String contextId : contextIds) {
						org.sakaiproject.grading.api.Assignment gAssignment = gradingService.getExternalAssignment(gradebookUid, associatedGradebookAssignment);
						if (gAssignment != null) {
							gradingService.updateExternalAssessment(
									contextId,
									associatedGradebookAssignment,
									null,
									gAssignment.getExternalData(),
									gAssignment.getName(),
									gAssignment.getCategoryId(),
									gAssignment.getPoints(),
									Date.from(update.dueDate),
									gAssignment.getUngraded()
							);
						}	
					}
				}
			}
		}
	}

	@Override
	public JSONArray getAssessmentsForContext(String siteId) {
		JSONArray jsonAssessments = new JSONArray();
		List<AssessmentData> assessments = assessmentServiceQueries.getAllActiveAssessmentsByAgent(getCurrentSiteId());
		List<PublishedAssessmentFacade> pubAssessments = pubAssessmentServiceQueries.getBasicInfoOfAllPublishedAssessments2(PublishedAssessmentFacadeQueries.TITLE, true, getCurrentSiteId());
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_ASSESSMENTS);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ASSESSMENTS).getTitle();
		for (AssessmentData assessment : assessments) {
			AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
			boolean lateHandling = (control.getLateHandling() != null && Objects.equals(control.getLateHandling(), AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION));
			JSONObject assobj = new JSONObject();
			assobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, assessment.getAssessmentBaseId());
			assobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, assessment.getTitle());
			assobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(control.getDueDate()));
			assobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(control.getStartDate()));
			assobj.put(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME, formatToUserDateFormat(control.getRetractDate()));
			assobj.put(DateManagerConstants.JSON_ISDRAFT_PARAM_NAME, true);
			assobj.put(DateManagerConstants.JSON_LATEHANDLING_PARAM_NAME, lateHandling);
			assobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
			assobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
			assobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, resourceLoader.getString("itemtype.draft"));
			if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
				assobj.put(DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME, formatToUserDateFormat(control.getFeedbackDate()));
				assobj.put(DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME, formatToUserDateFormat(control.getFeedbackEndDate()));
				assobj.put(DateManagerConstants.JSON_FEEDBACKBYDATE_PARAM_NAME, true);
			} else {
				assobj.put(DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME, null);
				assobj.put(DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME, null);
				assobj.put(DateManagerConstants.JSON_FEEDBACKBYDATE_PARAM_NAME, false);
			}
			jsonAssessments.add(assobj);
		}
		for (PublishedAssessmentFacade paf : pubAssessments) {
			PublishedAssessmentFacade assessment = pubAssessmentServiceQueries.getSettingsOfPublishedAssessment(paf.getPublishedAssessmentId());
			AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
			boolean lateHandling = (control.getLateHandling() != null && Objects.equals(control.getLateHandling(), AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION));
			JSONObject assobj = new JSONObject();
			assobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, assessment.getPublishedAssessmentId());
			assobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, assessment.getTitle());
			assobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(control.getDueDate()));
			assobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(control.getStartDate()));
			assobj.put(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME, formatToUserDateFormat(control.getRetractDate()));
			assobj.put(DateManagerConstants.JSON_ISDRAFT_PARAM_NAME, false);
			assobj.put(DateManagerConstants.JSON_LATEHANDLING_PARAM_NAME, lateHandling);
			assobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
			assobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
			assobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, "false");
			if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
				assobj.put(DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME, formatToUserDateFormat(control.getFeedbackDate()));
				assobj.put(DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME, formatToUserDateFormat(control.getFeedbackEndDate()));
				assobj.put(DateManagerConstants.JSON_FEEDBACKBYDATE_PARAM_NAME, true);
			} else {
				assobj.put(DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME, null);
				assobj.put(DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME, null);
				assobj.put(DateManagerConstants.JSON_FEEDBACKBYDATE_PARAM_NAME, false);
			}
			jsonAssessments.add(assobj);
		}
		return orderJSONArrayByTitle(jsonAssessments);
	}

	@Override
	public DateManagerValidation validateAssessments(String siteId, JSONArray assessments) throws Exception {
		DateManagerValidation assessmentValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ASSESSMENTS).getTitle();
		for (int i = 0; i < assessments.size(); i++) {
			JSONObject jsonAssessment = (JSONObject)assessments.get(i);
			Long assessmentId = Long.parseLong(jsonAssessment.get(DateManagerConstants.JSON_ID_PARAM_NAME).toString());
			int idx = Integer.parseInt(jsonAssessment.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				/* VALIDATE IF USER CAN UPDATE THE ASSESSMENT */

				String openDateRaw = (String) jsonAssessment.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				String dueDateRaw = (String) jsonAssessment.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);
				String acceptUntilRaw = (String) jsonAssessment.get(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME);
				String feedbackStartRaw = (String) jsonAssessment.get(DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME);
				String feedbackEndRaw = (String) jsonAssessment.get(DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME);

				Instant openDate = null;
				Instant dueDate = null;
				Instant acceptUntil = null;
				Instant feedbackStart = null;
				Instant feedbackEnd = null;

				if (StringUtils.isNotBlank(openDateRaw)) {
					openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
				}
				if (StringUtils.isNotBlank(dueDateRaw)) {
					dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
				}
				if (StringUtils.isNotBlank(acceptUntilRaw)) {
					acceptUntil = userTimeService.parseISODateInUserTimezone(acceptUntilRaw).toInstant();
				}
				if (StringUtils.isNotBlank(feedbackStartRaw)) {
					feedbackStart = userTimeService.parseISODateInUserTimezone(feedbackStartRaw).toInstant();
				}
				if (StringUtils.isNotBlank(feedbackEndRaw)) {
					feedbackEnd = userTimeService.parseISODateInUserTimezone(feedbackEndRaw).toInstant();
				}
				boolean isDraft = Boolean.parseBoolean(jsonAssessment.get(DateManagerConstants.JSON_ISDRAFT_PARAM_NAME).toString());

				AssessmentIfc assessment;
				AssessmentAccessControlIfc control;
				if (isDraft) {
					assessment = assessmentServiceQueries.getAssessment(assessmentId);
                } else {
					assessment = pubAssessmentServiceQueries.getPublishedAssessment(assessmentId);
                }
                control = assessment.getAssessmentAccessControl();
                boolean lateHandling = control.getLateHandling() != null && Objects.equals(control.getLateHandling(), AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);

				if (assessment == null) {
					errors.add(new DateManagerError("assessment", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.assessments.item.name")), "assessments", toolTitle, idx));
					continue;
				}
				boolean errored = false;

				if (openDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.assessments.open.date.not.found"), "assessments", toolTitle, idx));
				}
				if (acceptUntil != null) {
					if (dueDate == null) {
						errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, resourceLoader.getString("error.due.date.not.found.accept.until"),"assessments", toolTitle, idx));
						errored = true;
					} else if (acceptUntil.isBefore(dueDate) && lateHandling) {
						errors.add(new DateManagerError(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME, resourceLoader.getString("error.accept.until.before.due.date.open.date"),"assessments", toolTitle, idx));
						errored = true;
					}
				}

				Integer feedbackMode = isDraft ? ((AssessmentFacade) assessment).getAssessmentFeedback().getFeedbackDelivery()
												: ((PublishedAssessmentFacade) assessment).getAssessmentFeedback().getFeedbackDelivery();
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackMode) && feedbackStart == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME, resourceLoader.getString("error.feedback.start.not.found"), "assessments", toolTitle, idx));
				}

				if (errored) {
					continue;
				}

				log.debug("Open {} ; Due {} ; Until {} ; Feedback Start {} ; Feedback End {}", jsonAssessment.get(DateManagerConstants.JSON_OPENDATELABEL_PARAM_NAME), jsonAssessment.get(DateManagerConstants.JSON_DUEDATELABEL_PARAM_NAME),
								jsonAssessment.get("accept_until_label"), jsonAssessment.get(DateManagerConstants.JSON_FEEDBACKSTARTLABEL_PARAM_NAME), jsonAssessment.get(DateManagerConstants.JSON_FEEDBACKENDLABEL_PARAM_NAME));

				DateManagerUpdate update = new DateManagerUpdate(assessment, openDate, dueDate, acceptUntil);
				update.setFeedbackStartDate(feedbackStart);
				update.setFeedbackEndDate(feedbackEnd);

				if (dueDate != null && !update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.assessments.open.date.before.due.date"), "assessments", toolTitle, idx));
					continue;
				}

				if (lateHandling && dueDate != null && acceptUntil != null && update.dueDate.isAfter(update.acceptUntilDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, resourceLoader.getString("error.due.date.before.accept.until"), "assessments", toolTitle, idx));
					continue;
				}

				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackMode) && feedbackStart != null && feedbackEnd != null && feedbackEnd.isBefore(feedbackStart)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME, resourceLoader.getString("error.feedback.start.before.feedback.end"), "assessments", toolTitle, idx));
					continue;
				}

				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "assessments", toolTitle, idx));
				log.error("Error trying to validate Tests & Quizzes {}", ex);
			}
		}

		assessmentValidate.setErrors(errors);
		assessmentValidate.setUpdates(updates);
		return assessmentValidate;
	}

	@Override
	public void updateAssessments(DateManagerValidation assessmentsValidation) throws Exception {
		for (DateManagerUpdate update : assessmentsValidation.getUpdates()) {
			if (update.object.getClass().equals(AssessmentFacade.class)) {
				AssessmentFacade assessment = (AssessmentFacade) update.object;
				AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
				boolean lateHandling = control.getLateHandling() != null && Objects.equals(control.getLateHandling(), AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);
				control.setStartDate(Date.from(update.openDate));
				Date dueDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
				control.setDueDate(dueDateTemp);
				if (lateHandling) {
					Date lateDateTemp =
							update.acceptUntilDate != null ? Date.from(update.acceptUntilDate) : null;
					control.setRetractDate(lateDateTemp);
				} else {
					if (control.getRetractDate() != null) {
						control.setRetractDate(dueDateTemp);
					}
				}
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
					control.setFeedbackDate(Date.from(update.feedbackStartDate));
					Date feedbackEndDateTemp = 
                                        update.feedbackEndDate != null ? Date.from(update.feedbackEndDate) : null;
                                        control.setFeedbackEndDate(feedbackEndDateTemp);
				}
				assessment.setAssessmentAccessControl(control);
				assessmentServiceQueries.saveOrUpdate(assessment);

			} else {
				PublishedAssessmentFacade assessment = (PublishedAssessmentFacade) update.object;
				String id = assessment.getPublishedAssessmentId().toString();
				AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
				boolean lateHandling = control.getLateHandling() != null && Objects.equals(control.getLateHandling(), AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);
				control.setStartDate(Date.from(update.openDate));
				Date dueDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
				control.setDueDate(dueDateTemp);
				if (lateHandling) {
					Date lateDateTemp =
							update.acceptUntilDate != null ? Date.from(update.acceptUntilDate) : null;
					control.setRetractDate(lateDateTemp);
				} else {
					if (control.getRetractDate() != null) {
						control.setRetractDate(dueDateTemp);
					}
				}
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
					control.setFeedbackDate(Date.from(update.feedbackStartDate));
					Date feedbackEndDateTemp = 
						update.feedbackEndDate != null ? Date.from(update.feedbackEndDate) : null;
					control.setFeedbackEndDate(feedbackEndDateTemp);
				}
				assessment.setAssessmentAccessControl(control);
				pubAssessmentServiceQueries.saveOrUpdate(assessment);
				samigoAvailableNotificationService.scheduleAssessmentAvailableNotification(id);

				// only updating if the gradebook item exists and is external
				String gradebookUid = assessment.getOwnerSiteId();

				List<String> contextIds = new ArrayList<>();
				contextIds.add(gradebookUid);
				if (gradingService.isGradebookGroupEnabled(getCurrentSiteId())) {
					contextIds = new ArrayList<>();
					List<Gradebook> gradebooks = gradingService.getGradebookGroupInstances(gradebookUid);
					for (Gradebook gradebook : gradebooks) {
						List<org.sakaiproject.grading.api.Assignment> groupAssignments = gradingService.getAssignments(gradebook.getUid().toString(), getCurrentSiteId(), SortType.SORT_BY_NONE);
						for (org.sakaiproject.grading.api.Assignment assignment : groupAssignments) {
							if (assignment.getExternalId() != null && assignment.getExternalId().equals(id)) {
								contextIds.add(gradebook.getUid());
								gradebookUid = gradebook.getUid();
							}
						}
					}
				}

				if (StringUtils.isNotBlank(gradebookUid) && gradingService.isExternalAssignmentDefined(gradebookUid, id)) {
					for (String contextId : contextIds) {
						org.sakaiproject.grading.api.Assignment gAssignment = gradingService.getExternalAssignment(contextId, id);
						if (gAssignment != null) {
							gradingService.updateExternalAssessment(
									contextId,
									id,
									null,
									gAssignment.getExternalData(),
									gAssignment.getName(),
									gAssignment.getCategoryId(),
									gAssignment.getPoints(),
									dueDateTemp,
									gAssignment.getUngraded()
							);
						}
					}
				}
			}
		}
	}

	@Override
	public JSONArray getGradebookItemsForContext(String siteId) {
		JSONArray jsonGradebook = new JSONArray();
		List<String> gradebookUids = Arrays.asList(getCurrentSiteId());
		if (gradingService.isGradebookGroupEnabled(getCurrentSiteId())) {
			gradebookUids = gradingService.getGradebookGroupInstancesIds(siteId);
		}
		boolean hasGradebookPermissions = gradingService.currentUserHasEditPerm(getCurrentSiteId());
		if (!hasGradebookPermissions) {
			return jsonGradebook;
		}
		for (String gradebookUid : gradebookUids) {
			Collection<org.sakaiproject.grading.api.Assignment> gbitems = gradingService.getAssignments(gradebookUid, siteId, SortType.SORT_BY_NONE);
			String url = getUrlForTool(DateManagerConstants.COMMON_ID_GRADEBOOK);
			String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_GRADEBOOK).getTitle();
			for(org.sakaiproject.grading.api.Assignment gbitem : gbitems) {
				if(!gbitem.getExternallyMaintained()) {
					JSONObject gobj = new JSONObject();
					gobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, gbitem.getId());
					gobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, gbitem.getName());
					gobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(gbitem.getDueDate()));
					gobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
					gobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
					gobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, "false");
					jsonGradebook.add(gobj);
				}
			}
		}
		return orderJSONArrayByTitle(jsonGradebook);
	}

	@Override
	public DateManagerValidation validateGradebookItems(String siteId, JSONArray gradebookItems) throws Exception {
		DateManagerValidation gradebookItemsValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();

		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_GRADEBOOK).getTitle();
		if (!gradebookItems.isEmpty() && !gradingService.currentUserHasEditPerm(getCurrentSiteId())) {
			errors.add(new DateManagerError("gbitem", resourceLoader.getString("error.update.permission.denied"), "gradebookItems", toolTitle, 0));
		}

		for (int i = 0; i < gradebookItems.size(); i++) {
			JSONObject jsonItem = (JSONObject)gradebookItems.get(i);
			int idx = Integer.parseInt(jsonItem.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {				
				Long itemId;
				if (jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME).getClass().getName().contains("Long")) {
					itemId = (Long)jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				} else {
					itemId = Long.parseLong((String)jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME));
				}
				if (itemId == null) {
					errors.add(new DateManagerError("gbitem", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.gradebook.item.name")), "gradebookItems", toolTitle, idx));
					continue;
				}

				String dueDateRaw = (String) jsonItem.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);
				Instant dueDate = null;
				if (StringUtils.isNotBlank(dueDateRaw)) {
					dueDateRaw = dueDateRaw.replaceAll("\"", "").replace("/", "-");
					try {

						LocalDate date;
						if (dueDateRaw.contains("T")) {
							date = LocalDateTime.parse(dueDateRaw, inputDateTimeFormatter).toLocalDate();
						} else {
							date = LocalDate.parse(dueDateRaw, inputDateFormatter);
						}
						ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
						dueDate = date.atStartOfDay(zone).toInstant();
					} catch (DateTimeParseException e) {
						log.warn("Could not parse due date [{}], {}", dueDateRaw, e);
					}
				}

				org.sakaiproject.grading.api.Assignment gbitem;
				if (gradingService.isGradebookGroupEnabled(getCurrentSiteId())) {
					List<Gradebook> gradebooks = gradingService.getGradebookGroupInstances(siteId);
					String groupId = "";
					for (Gradebook gra : gradebooks) {
						List<org.sakaiproject.grading.api.Assignment> groupAssignments = gradingService.getAssignments(gra.getUid().toString(), getCurrentSiteId(), SortType.SORT_BY_NONE);
						for (org.sakaiproject.grading.api.Assignment assignment : groupAssignments) {
							if (assignment.getId().equals(itemId)) {
								groupId = gra.getUid();
							}
						}
					}
					gbitem = gradingService.getAssignment(groupId, getCurrentSiteId(), itemId);
				} else {
					gbitem = gradingService.getAssignment(getCurrentSiteId(), getCurrentSiteId(), itemId);
				}
				if (gbitem == null) {
					errors.add(new DateManagerError("gbitem", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.gradebook.item.name")), "gradebookItems", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(gbitem, null, dueDate, null, null, null);
				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "gradebookItems", toolTitle, idx));
				log.error("Error trying to validate Gradebook {}", ex.toString());
			}
		}

		gradebookItemsValidate.setErrors(errors);
		gradebookItemsValidate.setUpdates(updates);
		return gradebookItemsValidate;
	}

	@Override
	public void updateGradebookItems(DateManagerValidation gradebookItemsValidate) throws Exception {
		for (DateManagerUpdate update : gradebookItemsValidate.getUpdates()) {
			org.sakaiproject.grading.api.Assignment assignmentDefinition = (org.sakaiproject.grading.api.Assignment) update.object;
			Date dueDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
			assignmentDefinition.setDueDate(dueDateTemp);
			String gradebookUid = gradingService.getGradebookUidByAssignmentById(getCurrentSiteId(), assignmentDefinition.getId());
			gradingService.updateAssignment(gradebookUid, getCurrentSiteId(), assignmentDefinition.getId(), assignmentDefinition);
		}
	}

	@Override
	public JSONArray getSignupMeetingsForContext(String siteId) {
		JSONArray jsonMeetings = new JSONArray();
		Collection<SignupMeeting> meetings = signupService.getAllSignupMeetings(siteId, getCurrentUserId());
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_SIGNUP);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_SIGNUP).getTitle();
		for(SignupMeeting meeting : meetings) {
			JSONObject mobj = new JSONObject();
			mobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, meeting.getId());
			mobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, meeting.getTitle());
			mobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(meeting.getEndTime()));
			mobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(meeting.getStartTime()));
			mobj.put(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME, formatToUserDateFormat(meeting.getSignupBegins()));
			mobj.put(DateManagerConstants.JSON_SIGNUPDEADLINE_PARAM_NAME, formatToUserDateFormat(meeting.getSignupDeadline()));
			mobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
			mobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
			mobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, "false");
			jsonMeetings.add(mobj);
		}
		return orderJSONArrayByTitle(jsonMeetings);
	}

	@Override
	public DateManagerValidation validateSignupMeetings(String siteId, JSONArray signupMeetings) throws Exception {
		DateManagerValidation meetingsValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();

		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_SIGNUP).getTitle();
		if (!signupMeetings.isEmpty() && !signupService.isAllowedToCreateinSite(getCurrentUserId(), getCurrentSiteId())) {
			errors.add(new DateManagerError("signup", resourceLoader.getString("error.update.permission.denied"), "signupMeetings", toolTitle, 0));
		}
		for (int i = 0; i < signupMeetings.size(); i++) {
			JSONObject jsonMeeting = (JSONObject)signupMeetings.get(i);
			int idx = Integer.parseInt(jsonMeeting.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				Long meetingId;
				if (jsonMeeting.get(DateManagerConstants.JSON_ID_PARAM_NAME).getClass().getName().contains("Long")) {
					meetingId = (Long)jsonMeeting.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				} else {
					meetingId = Long.parseLong((String)jsonMeeting.get(DateManagerConstants.JSON_ID_PARAM_NAME));
				}
				if (meetingId == null) {
					errors.add(new DateManagerError("signup", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.signup.item.name")), "signupMeetings", toolTitle, idx));
					continue;
				}

				String openDateRaw = (String) jsonMeeting.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				String dueDateRaw = (String) jsonMeeting.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);
				String signupBeginsRaw = (String) jsonMeeting.get(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME);
				String signupDeadlineRaw = (String) jsonMeeting.get(DateManagerConstants.JSON_SIGNUPDEADLINE_PARAM_NAME);
				Instant openDate = null;
				Instant dueDate = null;
				Instant signupBegins = null;
				Instant signupDeadline = null;

				if (StringUtils.isNotBlank(openDateRaw)) {
                                        openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
				}
				if (StringUtils.isNotBlank(dueDateRaw)) {
                                        dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
				}
				if (StringUtils.isNotBlank(signupBeginsRaw)) {
                                        signupBegins = userTimeService.parseISODateInUserTimezone(signupBeginsRaw).toInstant();
				}
				if (StringUtils.isNotBlank(signupDeadlineRaw)) {
                                        signupDeadline = userTimeService.parseISODateInUserTimezone(signupDeadlineRaw).toInstant();
				}
				boolean errored = false;
				if (openDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (dueDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, resourceLoader.getString("error.due.date.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (signupBegins == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME, resourceLoader.getString("error.signup.begins.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (signupDeadline == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPDEADLINE_PARAM_NAME, resourceLoader.getString("error.signup.deadline.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (errored) {
					continue;
				}

				SignupMeeting meeting = signupService.loadSignupMeeting(meetingId, getCurrentUserId(), getCurrentSiteId());
				if (meeting == null) {
					errors.add(new DateManagerError("signup", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.signup.item.name")), "signupMeetings", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(meeting, openDate, dueDate, null, null, null);
				update.setSignupBegins(signupBegins);
				update.setSignupDeadline(signupDeadline);
                                if (update.openDate != null && update.dueDate != null && !update.openDate.isBefore(update.dueDate)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.before.due.date"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                if (update.signupBegins != null && update.openDate != null && update.signupBegins.isAfter(update.openDate)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME, resourceLoader.getString("error.signup.begins.after.open.date"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                if (update.signupDeadline != null && update.dueDate != null && update.signupDeadline.isAfter(update.dueDate)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPDEADLINE_PARAM_NAME, resourceLoader.getString("error.signup.deadline.after.due.date"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                if (update.signupBegins != null && update.signupDeadline != null && update.signupBegins.isAfter(update.signupDeadline)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME, resourceLoader.getString("error.signup.begins.after.signup.deadline"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "signupMeetings", toolTitle, idx));
				log.error("Error trying to validate Sign Up {}", ex);
			}
		}
		meetingsValidate.setErrors(errors);
		meetingsValidate.setUpdates(updates);
		return meetingsValidate;
	}

	@Override
	public void updateSignupMeetings(DateManagerValidation signupValidate) throws Exception {
		for (DateManagerUpdate update : signupValidate.getUpdates()) {
			SignupMeeting meeting = (SignupMeeting) update.object;
			meeting.setStartTime(Date.from(update.openDate));
			meeting.setEndTime(Date.from(update.dueDate));
			meeting.setSignupBegins(Date.from(update.signupBegins));
			meeting.setSignupDeadline(Date.from(update.signupDeadline));
			signupService.updateSignupMeeting(meeting, true);
		}
	}

	@Override
	public JSONArray getResourcesForContext(String siteId) {
		JSONArray jsonResources = new JSONArray();
		String siteCollection = contentHostingService.getSiteCollection(siteId);
		List<ContentEntity> unformattedList = contentHostingService.getAllEntities(siteCollection);
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_RESOURCES);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_RESOURCES).getTitle();
		for(ContentEntity res : unformattedList) {
			JSONObject robj = new JSONObject();
			ResourceProperties contentResourceProps = res.getProperties();
			String resourceId = res.getId();
			String displayName = contentResourceProps.getProperty(ResourceProperties.PROP_DISPLAY_NAME);

			// Extract the path information from the resource ID relative to site collection
			String relativePath = "";
			if (resourceId.startsWith(siteCollection) && !resourceId.equals(siteCollection)) {
				// Remove the site collection prefix and trailing slash if it's a collection
				String pathPart = resourceId.substring(siteCollection.length());
				if (res instanceof ContentCollection && pathPart.endsWith("/")) {
					pathPart = pathPart.substring(0, pathPart.length() - 1);
				}

				// Build the relative path for display
				if (pathPart.contains("/")) {
					int lastSlash = pathPart.lastIndexOf("/");
					if (lastSlash > 0) {
						relativePath = pathPart.substring(0, lastSlash);
						relativePath = relativePath.replace("/", " > ");
						robj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, relativePath + " > " + displayName);
					} else {
						robj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, displayName);
					}
				} else {
					robj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, displayName);
				}
			} else {
				robj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, displayName);
			}
			
			// Store original path for sorting
			robj.put("resourcePath", resourceId);
			robj.put(DateManagerConstants.JSON_ID_PARAM_NAME, resourceId);
			
			if(res.getRetractInstant() != null) robj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserInstantFormat(res.getRetractInstant()));
			else robj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, null);
			if(res.getReleaseInstant() != null) robj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserInstantFormat(res.getReleaseInstant()));
			else robj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, null);
			robj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, StringUtils.defaultIfBlank(res.getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE), resourceLoader.getString("itemtype.folder")));
			robj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
			robj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
			jsonResources.add(robj);
		}
		return orderResourcesByHierarchy(jsonResources);
	}

	@Override
	public DateManagerValidation validateResources(String siteId, JSONArray resources) throws Exception {
		DateManagerValidation resourcesValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_RESOURCES).getTitle();
		String entityType = null;
		ContentResourceEdit resource = null;
		ContentCollectionEdit folder = null;
		for (int i = 0; i < resources.size(); i++) {
			JSONObject jsonResource = (JSONObject)resources.get(i);
			int idx = Integer.parseInt(jsonResource.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				String resourceId = (String)jsonResource.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				if (resourceId == null) {
					errors.add(new DateManagerError("resource", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.resources.item.name")), "resources", toolTitle, idx));
					continue;
				}

				String openDateRaw = (String) jsonResource.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				String dueDateRaw = (String) jsonResource.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);

				Instant openDate = null;
				Instant dueDate = null;

				if (StringUtils.isNotBlank(openDateRaw)) {
					openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
				}
				if (StringUtils.isNotBlank(dueDateRaw)) {
					dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
				}

				if (openDate != null && dueDate != null && !openDate.isBefore(dueDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.before.due.date"), "resources", toolTitle, idx));
					continue;
				}

				entityType = (String)jsonResource.get(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME);
				if(!resourceLoader.getString("itemtype.folder").equals(entityType)) {
					resource = contentHostingService.editResource(resourceId);
					if (resource == null) {
						errors.add(new DateManagerError("resource", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.resources.item.name")), "resources", toolTitle, idx));
						continue;
					}

					if (!contentHostingService.allowUpdateResource(resourceId)) {
						errors.add(new DateManagerError("resource", resourceLoader.getString("error.update.permission.denied"), "resources", toolTitle, idx));
					}
					updates.add(new DateManagerUpdate(resource, openDate, dueDate, null, null, null));
				} else {
					folder = contentHostingService.editCollection(resourceId);
					if (folder == null) {
						errors.add(new DateManagerError("resource", resourceLoader.getString("error.folder.not.found"), "resources", toolTitle, idx));
						continue;
					}

					if (!contentHostingService.allowUpdateCollection(resourceId)) {
						errors.add(new DateManagerError("resource", resourceLoader.getString("error.update.permission.denied"), "resources", toolTitle, idx));
					}
					updates.add(new DateManagerUpdate(folder, openDate, dueDate, null, null, null));
				}

			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "resources", toolTitle, idx));
				log.error("Error trying to validate Resources {}", e);

				if(entityType != null) {
					if(!resourceLoader.getString("itemtype.folder").equals(entityType)) {
						contentHostingService.cancelResource(resource);
					} else {
						contentHostingService.cancelCollection(folder);
					}
				}
			}
		}

		resourcesValidate.setErrors(errors);
		resourcesValidate.setUpdates(updates);
		return resourcesValidate;
	}

	@Override
	public void clearUpdateResourceLocks(DateManagerValidation resourceValidation) throws Exception {
                try {
                        for (DateManagerUpdate update : resourceValidation.getUpdates()) {
                                if (update.object instanceof ContentResourceEdit) { 
                                        contentHostingService.cancelResource((ContentResourceEdit) update.getObject());
                                }
                        }
                } catch (Exception e) {
                        log.warn("Could not clear update for resource, {}", e.toString());
                }
        }

	@Override
	public void updateResources(DateManagerValidation resourceValidation) throws Exception {
		for (DateManagerUpdate update : resourceValidation.getUpdates()) {
			if (update.object instanceof ContentCollectionEdit cce) {
                if (update.dueDate != null) {
					cce.setRetractInstant(Instant.from(update.dueDate));
				} else {
					cce.setRetractDate(null);
				}
				if (update.openDate != null) {
					cce.setReleaseDate(timeService.newTime(Date.from(update.openDate).getTime()));
				} else {
					cce.setReleaseDate(null);
				}

				contentHostingService.commitCollection(cce);
			} else {
				ContentResourceEdit cre = (ContentResourceEdit) update.object;
				if(update.dueDate != null) {
					cre.setRetractInstant(Instant.from(update.dueDate));
				} else {
					cre.setRetractDate(null);
				}
				if (update.openDate != null) {
					cre.setReleaseDate(timeService.newTime(Date.from(update.openDate).getTime()));
				} else {
					cre.setReleaseDate(null);
				}

				contentHostingService.commitResource(cre, NotificationService.NOTI_NONE);
			}
		}
	}

	@Override
	public JSONArray getCalendarEventsForContext(String siteId) {
		JSONArray jsonCalendar = new JSONArray();
		int startYear = timeService.newTime().breakdownLocal().getYear() - DateManagerConstants.LIST_VIEW_YEAR_RANGE / 2;
	 	int endYear = timeService.newTime().breakdownLocal().getYear() + DateManagerConstants.LIST_VIEW_YEAR_RANGE / 2;
		Time startingListViewDate = timeService.newTimeLocal(startYear, 0, 0, 0, 0, 0, 0);
		Time endingListViewDate = timeService.newTimeLocal(endYear, 12, 31, 23, 59, 59, 99);
		try {
			Calendar c = getCalendar();
			if (c == null) {
				return jsonCalendar;
			}
			String url = getUrlForTool(DateManagerConstants.COMMON_ID_CALENDAR);
			String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_CALENDAR).getTitle();
			List<CalendarEvent> calendarEvents = c.getEvents(timeService.newTimeRange(startingListViewDate, endingListViewDate), null);
			for (CalendarEvent calendarEvent : calendarEvents) {
				JSONObject cobj = new JSONObject();
				cobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, calendarEvent.getId());
				cobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, calendarEvent.getDisplayName());
				cobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(new Date(calendarEvent.getRange().firstTime().getTime())));
				cobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(new Date(calendarEvent.getRange().lastTime().getTime())));
				cobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
				cobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url + "?eventReference=" + formattedText.escapeUrl(calendarEvent.getReference()) + "&panel=Main&sakai_action=doDescription&sakai.state.reset=true");
				cobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, "false");
				jsonCalendar.add(cobj);
			}
		} catch(Exception e) {
			log.error("Error getting Calendar events for site {} : {}", siteId, e);
		}
		return orderJSONArrayByTitle(jsonCalendar);
	}

	@Override
	public DateManagerValidation validateCalendarEvents(String siteId, JSONArray calendarEvents) throws Exception {
		DateManagerValidation calendarValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_CALENDAR).getTitle();
		Calendar c = getCalendar();
		CalendarEventEdit calendarEvent = null;

		for (int i = 0; i < calendarEvents.size(); i++) {
			JSONObject jsonEvent = (JSONObject)calendarEvents.get(i);
			String eventId = (String)jsonEvent.get(DateManagerConstants.JSON_ID_PARAM_NAME);
			int idx = Integer.parseInt(jsonEvent.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {
				if (eventId == null) {
					errors.add(new DateManagerError("calendar", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.calendar.item.name")), "calendarEvents", toolTitle, idx));
					continue;
				}
				String openDateRaw = (String) jsonEvent.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				String dueDateRaw = (String) jsonEvent.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);
				Instant openDate = null;
				Instant dueDate = null;
				boolean errored = false;

                                if (StringUtils.isNotBlank(openDateRaw)) {
                                        openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
                                }
                                if (StringUtils.isNotBlank(dueDateRaw)) {
                                        dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
                                }	
                                if (openDate == null) {
                                        errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.not.found"), "calendarEvents", toolTitle, idx));
                                }
                                if (dueDate == null) {
                                        errored = errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, resourceLoader.getString("error.due.date.not.found"), "calendarEvents", toolTitle, idx));
                                }
                                if (openDate != null && dueDate != null && dueDate.isBefore(openDate)) {
                                        errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.before.end.date"), "calendarEvents", toolTitle, idx));
                                }
                                if (errored) {
                                        continue;
                                }

				if (c == null || !c.allowEditEvent(eventId)) {
					errors.add(new DateManagerError("calendar", resourceLoader.getString("error.event.permission"), "calendarEvents", toolTitle, idx));
				} else {
					calendarEvent = c.getEditEvent(eventId, CalendarService.EVENT_MODIFY_CALENDAR);
					if (calendarEvent == null) {
						errors.add(new DateManagerError("calendar", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.calendar.item.name")), "calendarEvents", toolTitle, idx));
						continue;
					}

					updates.add(new DateManagerUpdate(calendarEvent, openDate, dueDate, null, null, null));
				}

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "calendarEvents", toolTitle, idx));
				log.error("Cannot edit event {}", eventId, ex);

				// Clear out the lock
				if (c != null && calendarEvent != null) {
					c.cancelEvent(calendarEvent);
				}
			}
		}

		calendarValidate.setErrors(errors);
		calendarValidate.setUpdates(updates);
		return calendarValidate;
	}

	@Override
	public void clearUpdateCalendarLocks(DateManagerValidation calendarValidate) throws Exception {
		Calendar c = getCalendar();
		if (c != null) { 
                        try {
                                for (DateManagerUpdate update : calendarValidate.getUpdates()) {
                                        CalendarEventEdit edit = (CalendarEventEdit) update.object;
                                        c.cancelEvent(edit);
                                }
                        } catch (Exception e) {
                                log.warn("Could not clear update for calendar, {}", e.toString());
                        }
		} 
	}

	@Override
	public void updateCalendarEvents(DateManagerValidation calendarValidation) throws Exception {
		Calendar c = getCalendar();
		if (c != null) {
			for (DateManagerUpdate update : calendarValidation .getUpdates()) {
				CalendarEventEdit edit = (CalendarEventEdit) update.object;
				long date1 = Date.from(update.openDate).getTime();
				long date2 = Date.from(update.dueDate).getTime() - date1;
				edit.setRange(timeService.newTimeRange(date1, date2));
				c.commitEvent(edit);
			}
		}
	}

	private Calendar getCalendar() {
		if(calendarMap.get(getCurrentSiteId()) != null) { return calendarMap.get(getCurrentSiteId()); }
		try {
			String calendarId = calendarService.calendarReference(getCurrentSiteId(), SiteService.MAIN_CONTAINER);
			Calendar c = calendarService.getCalendar(calendarId);
			calendarMap.put(getCurrentSiteId(), c);
			return c;
		} catch (Exception ex) {
			log.warn("getCalendar : exception {}", ex.getMessage());
		}
		return null;
	}

	@Override
	public JSONArray getForumsForContext(String siteId) {
		JSONArray jsonForums = new JSONArray();
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_FORUMS);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_FORUMS).getTitle();
		for (DiscussionForum forum : forumManager.getForumsForMainPage()) {
			JSONObject fobj = new JSONObject();
			fobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, forum.getId());
			fobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, forum.getTitle());
			if(forum.getAvailabilityRestricted()) {
				fobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(forum.getCloseDate()));
				fobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(forum.getOpenDate()));
				fobj.put(DateManagerConstants.JSON_RESTRICTED_PARAM_NAME, true);
			} else {
				fobj.put(DateManagerConstants.JSON_RESTRICTED_PARAM_NAME, false);
				fobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, null);
				fobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, null);
			}
			
			String forumExtraInfo = resourceLoader.getString("itemtype.forum");
			if (forum.getDraft()) {
				forumExtraInfo = forumExtraInfo + " " + resourceLoader.getString("itemtype.draft"); 
			}
			
			fobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, forumExtraInfo);
			fobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
			fobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
			for (Object o : forum.getTopicsSet()) {
				DiscussionTopic topic = (DiscussionTopic)o;
				JSONObject tobj = new JSONObject();
				tobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, topic.getId());
				tobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, topic.getTitle());
				if(topic.getAvailabilityRestricted()) {
					tobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(topic.getCloseDate()));
					tobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(topic.getOpenDate()));
					tobj.put(DateManagerConstants.JSON_RESTRICTED_PARAM_NAME, true);
				} else {
					tobj.put(DateManagerConstants.JSON_RESTRICTED_PARAM_NAME, false);
					tobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, null);
					tobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, null);
				}
				
				String extraInfo = resourceLoader.getString("itemtype.topic");
				if (topic.getDraft()) {
					extraInfo = extraInfo + " " + resourceLoader.getString("itemtype.draft"); 
				}
				
				tobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, extraInfo);
				tobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
				tobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
				jsonForums.add(tobj);
			}
			jsonForums.add(fobj);
		}
		return orderJSONArrayByTitle(jsonForums);
	}

	@Override
	public DateManagerValidation validateForums(String siteId, JSONArray forums) throws Exception {
		DateManagerValidation forumValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_FORUMS).getTitle();
		for (int i = 0; i < forums.size(); i++) {
			JSONObject jsonForum = (JSONObject)forums.get(i);
			int idx = Integer.parseInt(jsonForum.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				Long forumId;
				if (jsonForum.get(DateManagerConstants.JSON_ID_PARAM_NAME).getClass().getName().contains("Long")) {
					forumId = (Long)jsonForum.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				} else {
					forumId = Long.parseLong((String)jsonForum.get(DateManagerConstants.JSON_ID_PARAM_NAME));
				}
				if (forumId == null) {
					errors.add(new DateManagerError("forum", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.forum.topic.item.name")), "forums", toolTitle, idx));
					continue;
				}

				String openDateRaw = (String) jsonForum.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				String dueDateRaw = (String) jsonForum.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);

				Instant openDate = null;
				Instant dueDate = null;

                                if (StringUtils.isNotBlank(openDateRaw)) {					
                                        openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
                                }
                                if (StringUtils.isNotBlank(dueDateRaw)) {	
                                        dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
                                }

                                String entityType = (String)jsonForum.get(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME);
                                DateManagerUpdate update;
                                if(resourceLoader.getString("itemtype.forum").equals(entityType)) {
                                        BaseForum forum = forumManager.getForumById(true, forumId);
                                        if (forum == null) {
                                                errors.add(new DateManagerError("forum", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.forums.item.name")), "forums", toolTitle, idx));
                                                continue;
                                        }

                                        update = new DateManagerUpdate(forum, openDate, dueDate, null, null, null);
                                } else {
                                        Topic topic = forumManager.getTopicById(true, forumId);
                                        if (topic == null) {
                                                errors.add(new DateManagerError("forum", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.topics.item.name")), "forums", toolTitle, idx));
                                                continue;
                                        }

                                        update = new DateManagerUpdate(topic, openDate, dueDate, null, null, null);
                                }

				if (update.openDate != null
						&& update.dueDate != null
						&& !update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.before.close.date"), "forums", toolTitle, idx));
					continue;
				}
				updates.add(update);

			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "forums", toolTitle, idx));
				log.error("Error trying to validate Forums {}", e);
			}
		}

		forumValidate.setErrors(errors);
		forumValidate.setUpdates(updates);
		return forumValidate;
	}

	@Override
	public void updateForums(DateManagerValidation forumValidation) throws Exception {
                for (DateManagerUpdate update : forumValidation.getUpdates()) {
                    if (update.object instanceof BaseForum) {
                                DiscussionForum forum = (DiscussionForum) update.object;
                                if(forum.getAvailabilityRestricted()) {
                                        Date openDateTemp = update.openDate != null ? Date.from(update.openDate) : null;
                                        Date closeDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
                                        if (update.openDate == null && update.dueDate == null) {
                                                forum.setAvailabilityRestricted(false);
                                        }
                                        forum.setOpenDate(openDateTemp);
                                        forum.setCloseDate(closeDateTemp);
                                } else {
                                        Date openDateTemp = update.openDate != null ? Date.from(update.openDate) : null;
                                        Date closeDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
                                        if (update.openDate != null || update.dueDate != null) {
                                                forum.setAvailabilityRestricted(true);
                                                forum.setOpenDate(openDateTemp);
                                                forum.setCloseDate(closeDateTemp);
                                        }
                                }
                                forumManager.saveDiscussionForum(forum);
                        } else {
                                DiscussionTopic topic = (DiscussionTopic) update.object;
                                if(topic.getAvailabilityRestricted()) {
                                        Date openDateTemp = update.openDate != null ? Date.from(update.openDate) : null;
                                        Date closeDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
                                        topic.setOpenDate(openDateTemp);
                                        topic.setCloseDate(closeDateTemp);
                                        if (update.openDate == null && update.dueDate == null) {
                                                topic.setAvailabilityRestricted(false);
                                        }
                                } else {
                                        Date openDateTemp = update.openDate != null ? Date.from(update.openDate) : null;
                                        Date closeDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
                                        if (update.openDate != null || update.dueDate != null) {
                                                topic.setAvailabilityRestricted(true);
                                                topic.setOpenDate(openDateTemp);
                                                topic.setCloseDate(closeDateTemp);
                                        }
                                }
                                forumManager.saveDiscussionForumTopic(topic);
                        }
                }
	}

	@Override
	public JSONArray getAnnouncementsForContext(String siteId) {
		JSONArray jsonAnnouncements = new JSONArray();
		String anncRef = announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		try {
			String url = getUrlForTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS);
			String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS).getTitle();
			List<AnnouncementMessage> announcements = announcementService.getMessages(anncRef, null, false, true);
			for(AnnouncementMessage announcement : announcements) {
				JSONObject aobj = new JSONObject();
				aobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, announcement.getId());
				AnnouncementMessageHeader header = announcement.getAnnouncementHeader();
				aobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, header.getSubject());
				if(announcement.getProperties().getProperty(AnnouncementService.RETRACT_DATE) != null) {
					aobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserDateFormat(Date.from(announcement.getProperties().getInstantProperty(AnnouncementService.RETRACT_DATE))));
				} else {
					aobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, null);
				}
				if(announcement.getProperties().getProperty(AnnouncementService.RELEASE_DATE) != null) {
					aobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(Date.from(announcement.getProperties().getInstantProperty(AnnouncementService.RELEASE_DATE))));
				} else {
					aobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, null);
				}
				aobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
				aobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url + "?itemReference=" + formattedText.escapeUrl(announcement.getReference()) + "&panel=Main&sakai_action=doShowmetadata&sakai.state.reset=true");
				String extraInfo = "false";
				if (header.getDraft()) extraInfo = resourceLoader.getString("itemtype.draft");
				aobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, extraInfo);
				jsonAnnouncements.add(aobj);
			}
		} catch (Exception e) {
			log.error("getAnnouncementsForContext error for context {} : {}", siteId, e);
		}
		return orderJSONArrayByTitle(jsonAnnouncements);
	}

	@Override
	public DateManagerValidation validateAnnouncements(String siteId, JSONArray announcements) throws Exception {
		DateManagerValidation announcementValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();

		String anncRef = announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS).getTitle();
		AnnouncementMessageEdit announcement = null;
		for (int i = 0; i < announcements.size(); i++) {
			JSONObject jsonAnnouncement = (JSONObject)announcements.get(i);
			int idx = Integer.parseInt(jsonAnnouncement.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				String announcementId = (String)jsonAnnouncement.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				if (announcementId == null) {
					errors.add(new DateManagerError("announcement", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.announcements.item.name")), "announcements", toolTitle, idx));
					continue;
				}

				String openDateRaw = (String) jsonAnnouncement.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				String dueDateRaw = (String) jsonAnnouncement.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);
				Instant openDate = null;
				Instant dueDate = null;
				if (StringUtils.isNotBlank(openDateRaw)) {	
					openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
				} 

				if (StringUtils.isNotBlank(dueDateRaw)) {		
					dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
				} 
				boolean errored = false;

				if (errored) {
					continue;
				}

				if (openDate != null && dueDate != null && !openDate.isBefore(dueDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.open.date.before.due.date"), "announcements", toolTitle, idx));
					continue;
				}

				AnnouncementChannel aChannel = announcementService.getAnnouncementChannel(anncRef);
				announcement = aChannel.editAnnouncementMessage(announcementId);
				if (announcement == null) {
					errors.add(new DateManagerError("announcement", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.announcements.item.name")), "announcements", toolTitle, idx));
					continue;
				}

				updates.add(new DateManagerUpdate(announcement, openDate, dueDate, null, null, null));
			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "announcements", toolTitle, idx));
				log.error("Error trying to validate Announcements {}", e.toString());

				// Clear out the lock
				if (announcement != null) {
					announcementService.cancelMessage(announcement);
				}
			}
		}
		announcementValidate.setErrors(errors);
		announcementValidate.setUpdates(updates);
		return announcementValidate;
	}

	@Override
	public void clearUpdateAnnouncementLocks(DateManagerValidation announcementValidate) throws Exception {
                try {
                        for (DateManagerUpdate update : announcementValidate.getUpdates()) {
                                announcementService.cancelMessage((AnnouncementMessageEdit) update.getObject());
                        }
                } catch (Exception e) {
                        log.warn("Could not clear update for announcement, {}", e.toString());
                }
	}

	@Override
	public void updateAnnouncements(DateManagerValidation announcementValidate) {
		String anncRef = announcementService.channelReference(getCurrentSiteId(), SiteService.MAIN_CONTAINER);
                try {
                        AnnouncementChannel aChannel = announcementService.getAnnouncementChannel(anncRef);
                        for (DateManagerUpdate update : announcementValidate.getUpdates()) {
                                AnnouncementMessageEdit msg = (AnnouncementMessageEdit) update.object;
                                if (update.openDate != null) {				
                                        msg.getPropertiesEdit().addProperty(AnnouncementService.RELEASE_DATE, timeService.newTime(Date.from(update.openDate).getTime()).toString());
                                } else {
                                        msg.getPropertiesEdit().removeProperty(AnnouncementService.RELEASE_DATE);
                                }	
                                if (update.dueDate != null) {
                                        msg.getPropertiesEdit().addProperty(AnnouncementService.RETRACT_DATE, timeService.newTime(Date.from(update.dueDate).getTime()).toString());
                                } else {
                                        msg.getPropertiesEdit().removeProperty(AnnouncementService.RETRACT_DATE);		
                                }				
                                aChannel.commitMessage(msg, NotificationService.NOTI_IGNORE);
                        }
                } catch (Exception e) {
                        log.warn("Announcement channel {} doesn't exist. {}", anncRef, e.toString());
                }
	}

	@Override
	public JSONArray getLessonsForContext(String siteId) {
		JSONArray jsonLessons = new JSONArray();
		List<Long> processedItemIDs = new ArrayList<>();
		jsonLessons = addAllSubpages(simplePageToolDao.findItemsInSite(siteId), null, jsonLessons, "false", processedItemIDs);
		return orderJSONArrayByTitle(jsonLessons);
	}

	private JSONArray addAllSubpages(List<SimplePageItem> items, Long pageId, JSONArray jsonLessons, String extraInfo, List<Long> processedItemIDs) {
		if (items != null) {
			String url = getUrlForTool(DateManagerConstants.COMMON_ID_LESSONS);
			String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_LESSONS).getTitle();
			for (SimplePageItem item : items) {
				if (item.getType() == SimplePageItem.PAGE) {
					Long itemId = Long.parseLong(item.getSakaiId());
					if (!itemId.equals(pageId) && !processedItemIDs.contains(itemId)) { // Avoid creating a infinite loop
						processedItemIDs.add(itemId);
						JSONObject lobj = new JSONObject();
						lobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, itemId);
						lobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, item.getName());
						SimplePage page = simplePageToolDao.getPage(itemId);
						if(page.getReleaseDate() != null) {
							lobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserDateFormat(page.getReleaseDate()));
						} else {
							lobj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, null);
						}
						lobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
						lobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
						lobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, extraInfo);
						jsonLessons.add(lobj);
						jsonLessons = addAllSubpages(simplePageToolDao.findItemsOnPage(itemId), itemId, jsonLessons, resourceLoader.getString("tool.lessons.extra.subpage"), processedItemIDs);
					}
				}
			}
		}
		return jsonLessons;
	}


	@Override
	public DateManagerValidation validateLessons(String siteId, JSONArray lessons) throws Exception {
		DateManagerValidation lessonsValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<DateManagerUpdate> updates = new ArrayList<>();

		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_LESSONS).getTitle();
		for (int i = 0; i < lessons.size(); i++) {
			JSONObject jsonItem = (JSONObject)lessons.get(i);
			int idx = Integer.parseInt(jsonItem.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				String openDateRaw = (String) jsonItem.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				Instant openDate = null;
				Long itemId;
				if (jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME).getClass().getName().contains("Long")) {
					itemId = (Long)jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				} else {
					itemId = Long.parseLong((String)jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME));
				}
				if (itemId == null) {
					errors.add(new DateManagerError("page", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.lessons.item.name")), "lessons", toolTitle, idx));
					continue;
				}

				if (StringUtils.isNotBlank(openDateRaw)) {
					openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
				}	

				SimplePage page = simplePageToolDao.getPage(itemId);
				if (page == null) {
					errors.add(new DateManagerError("page", resourceLoader.getFormattedMessage("error.item.not.found", resourceLoader.getString("tool.lessons.item.name")), "lessons", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(page, openDate, null, null, null, null);
				updates.add(update);
			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, resourceLoader.getString("error.uncaught"), "lessons", toolTitle, idx));
				log.error("Error trying to validate Lessons {}", e);
			}
		}

		lessonsValidate.setErrors(errors);
		lessonsValidate.setUpdates(updates);
		return lessonsValidate;
	}

	@Override
	public void updateLessons(DateManagerValidation lessonsValidation) throws Exception {
                for (DateManagerUpdate update : lessonsValidation.getUpdates()) {
                        SimplePage page = (SimplePage) update.object;
                        Date openDateTemp = update.openDate != null ? Date.from(update.openDate) : null;
                        page.setReleaseDate(openDateTemp);
                        log.debug("Saving changes on lessons : {}", simplePageToolDao.quickUpdate(page));
                }
	}

	private JSONArray orderJSONArrayByTitle(JSONArray jsonArray) {
		try {
			List list = (List) jsonArray.stream()
				.sorted(Comparator.comparing(json -> ((JSONObject) json).get(DateManagerConstants.JSON_TITLE_PARAM_NAME).toString().toLowerCase()))
				.collect(Collectors.toList());
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(JSONArray.toJSONString(list));
			jsonArray = (JSONArray) obj;
		} catch (Exception ex) {
			log.error("Cannot order the JSONArray elements alphabetically: {}", ex.getMessage());
		}
		return jsonArray;
	}
	
	/**
	 * Orders resources by their folder hierarchy path
	 * Resources in the same folder will be grouped together
	 * @param jsonArray JSONArray of resources
	 * @return ordered JSONArray
	 */
	private JSONArray orderResourcesByHierarchy(JSONArray jsonArray) {
		try {
			List list = (List) jsonArray.stream()
				.sorted(Comparator.comparing(json -> ((JSONObject) json).get("resourcePath").toString().toLowerCase()))
				.collect(Collectors.toList());
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(JSONArray.toJSONString(list));
			jsonArray = (JSONArray) obj;
		} catch (Exception ex) {
			log.error("Cannot order the resources by hierarchy: {}", ex.toString());
		}
		return jsonArray;
	}

	/**
	 * Generic validator that use the specific validators of each tool
	 * 
	 * @param toolId - String - the tool Id
	 * @param idx - int - the position in the menu
	 * @param columnsNames - String[][] - the names of the columns
	 * @param columns - String[] - the information of the columns
	 * 
	 * @return DateManagerValidation
	 */
	@Override
	public DateManagerValidation validateTool(String toolId, int idx, String[][] columnsNames, String[] columns) {
		String siteId = getCurrentSiteId();
		DateManagerValidation toolValidation = null;
		if (DateManagerConstants.COMMON_ID_ASSIGNMENTS.equals(toolId.replaceAll("\"", ""))) {
			JSONArray assignmentJsonArray = new JSONArray();
			assignmentJsonArray.add(this.createJsonObject(columnsNames[0], columns, idx));
			try {
				toolValidation = this.validateAssignments(siteId, assignmentJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the Assignments tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_ASSESSMENTS.equals(toolId.replaceAll("\"", ""))) {
			String id = columns[0].replaceAll("\"", "");
			JSONObject assessmentJsonObject = this.createJsonObject(columnsNames[1], columns, idx);
			assessmentJsonObject.put("due_date_label", columns[3] != null? columns[3].replaceAll("\"", "") : "");
			assessmentJsonObject.put("accept_until_label", columns[4] != null? columns[4].replaceAll("\"", "") : "");
			if (StringUtils.isBlank((String)assessmentJsonObject.get("due_date"))) {
				assessmentJsonObject.remove("due_date");
				assessmentJsonObject.put("due_date", ZonedDateTime.now().toString());
			}
			if (StringUtils.isBlank((String)assessmentJsonObject.get("accept_until"))) {
				assessmentJsonObject.remove("accept_until");
				assessmentJsonObject.put("accept_until", ZonedDateTime.now().toString());
			}
			if (StringUtils.isBlank((String)assessmentJsonObject.get("feedback_start"))) {
				assessmentJsonObject.remove("feedback_start");
				assessmentJsonObject.put("feedback_start", ZonedDateTime.now().toString());
			}
			if (StringUtils.isBlank((String)assessmentJsonObject.get("feedback_end"))) {
				assessmentJsonObject.remove("feedback_end");
				assessmentJsonObject.put("feedback_end", ZonedDateTime.now().toString());
			}
			if (pubAssessmentServiceQueries.isPublishedAssessmentIdValid(Long.parseLong(id))) {
				PublishedAssessmentFacade pubAssessment = pubAssessmentServiceQueries.getPublishedAssessment(Long.parseLong(id));
				assessmentJsonObject.put("is_draft", false);
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(pubAssessment.getAssessmentFeedback().getFeedbackDelivery())) {
					assessmentJsonObject.put("feedback_start_label", columns[5].replaceAll("\"", ""));
					assessmentJsonObject.put("feedback_end_label", columns[6].replaceAll("\"", ""));
					assessmentJsonObject.put("feedback_by_date", true);
				} else {
					assessmentJsonObject.put("feedback_start_label", "");
					assessmentJsonObject.put("feedback_end_label", "");
					assessmentJsonObject.put("feedback_by_date", false);
				}
			} else {
				AssessmentData assesmentData = assessmentServiceQueries.loadAssessment(Long.parseLong(id));
				assessmentJsonObject.put("is_draft", true);
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assesmentData.getAssessmentFeedback().getFeedbackDelivery())) {
					assessmentJsonObject.put("feedback_start_label", columns[5].replaceAll("\"", ""));
					assessmentJsonObject.put("feedback_end_label", columns[6].replaceAll("\"", ""));
					assessmentJsonObject.put("feedback_by_date", true);
				} else {
					assessmentJsonObject.put("feedback_start_label", "");
					assessmentJsonObject.put("feedback_end_label", "");
					assessmentJsonObject.put("feedback_by_date", false);
				}
			}
			JSONArray assessmentJsonArray = new JSONArray();
			assessmentJsonArray.add(assessmentJsonObject);
			try {
				toolValidation = this.validateAssessments(siteId, assessmentJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the Assessments tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_GRADEBOOK.equals(toolId.replaceAll("\"", ""))) {
			JSONArray gradebookJsonArray = new JSONArray();
			gradebookJsonArray.add(this.createJsonObject(columnsNames[2], columns, idx));
			try {
				toolValidation = this.validateGradebookItems(siteId, gradebookJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the GradebookItems tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_SIGNUP.equals(toolId.replaceAll("\"", ""))) {
			JSONArray signupJsonArray = new JSONArray();
			signupJsonArray.add(this.createJsonObject(columnsNames[3], columns, idx));
			try {
				toolValidation = this.validateSignupMeetings(siteId, signupJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the SignupMeetings tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_RESOURCES.equals(toolId.replaceAll("\"", ""))) {
			JSONObject resourcesJsonObject = this.createJsonObject(columnsNames[5], columns, idx);
			resourcesJsonObject.put("open_date_label", columns[2] != null? columns[2].replaceAll("\"", "") : "");
			resourcesJsonObject.put("due_date_label", columns[3] != null? columns[3].replaceAll("\"", "") : "");

			if (StringUtils.isBlank((String)resourcesJsonObject.get("open_date"))) {
				resourcesJsonObject.remove("open_date");
			}
			if (StringUtils.isBlank((String)resourcesJsonObject.get("due_date"))) {
				resourcesJsonObject.remove("due_date");
			}

			JSONArray resourcesJsonArray = new JSONArray();
			resourcesJsonArray.add(resourcesJsonObject);
			try {
				toolValidation = this.validateResources(siteId, resourcesJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the Resources tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_CALENDAR.equals(toolId.replaceAll("\"", ""))) {
			JSONArray calendarJsonArray = new JSONArray();
			calendarJsonArray.add(this.createJsonObject(columnsNames[4], columns, idx));
			try {
				toolValidation = this.validateCalendarEvents(siteId, calendarJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the Calendar tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_FORUMS.equals(toolId.replaceAll("\"", ""))) {
			JSONObject forumsJsonObject = this.createJsonObject(columnsNames[5], columns, idx);
			
			if (StringUtils.isBlank((String) forumsJsonObject.get("due_date")) && columns[2].replaceAll("\"", "").isEmpty()) {
				forumsJsonObject.remove("due_date");
				forumsJsonObject.put("due_date", columns[2].replaceAll("\"", ""));
			}
			JSONArray forumJsonArray = new JSONArray();
			forumJsonArray.add(forumsJsonObject);
			try {
				toolValidation = this.validateForums(siteId, forumJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the Forums tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_ANNOUNCEMENTS.equals(toolId.replaceAll("\"", ""))) {
			JSONObject announcementJsonObject = this.createJsonObject(columnsNames[4], columns, idx);
			announcementJsonObject.put("open_date_label", columns[2] != null? columns[2].replaceAll("\"", "") : "");
			announcementJsonObject.put("due_date_label", columns[3] != null? columns[3].replaceAll("\"", "") : "");

			if (StringUtils.isBlank((String) announcementJsonObject.get("open_date"))) {
				announcementJsonObject.remove("open_date");
			}
			if (StringUtils.isBlank((String) announcementJsonObject.get("due_date"))) {
				announcementJsonObject.remove("due_date");
			}
			JSONArray announcementJsonArray = new JSONArray();
			announcementJsonArray.add(announcementJsonObject);
			try {
				toolValidation = this.validateAnnouncements(siteId, announcementJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the Announcements tool", ex);
				return null;
			}
		}
		if (DateManagerConstants.COMMON_ID_LESSONS.equals(toolId.replaceAll("\"", ""))) {
			JSONArray lessonJsonArray = new JSONArray();
			lessonJsonArray.add(this.createJsonObject(columnsNames[6], columns, idx));
			try {
				toolValidation = this.validateLessons(siteId, lessonJsonArray);
			} catch (Exception ex) {
				log.error("Cannot validate the Lessons tool", ex);
				return null;
			}
		}
		return toolValidation;
	}

	/**
	 * Void function of a generic update that use the specific update of each tool
	 * 
	 * @param toolId - String - the tool Id
	 * @param dateManagerValidation - DateManagerValidation - the validator used to update the object
	 */
	public void updateTool(String toolId, DateManagerValidation dateManagerValidation) {
		try {
			if (DateManagerConstants.COMMON_ID_ASSIGNMENTS.equals(toolId)) {
				this.updateAssignments(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_ASSESSMENTS.equals(toolId)) {
				this.updateAssessments(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_GRADEBOOK.equals(toolId)) {
				this.updateGradebookItems(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_SIGNUP.equals(toolId)) {
				this.updateSignupMeetings(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_RESOURCES.equals(toolId)) {
				this.updateResources(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_CALENDAR.equals(toolId)) {
				this.updateCalendarEvents(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_FORUMS.equals(toolId)) {
				this.updateForums(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_ANNOUNCEMENTS.equals(toolId)) {
				this.updateAnnouncements(dateManagerValidation);
			} else if (DateManagerConstants.COMMON_ID_LESSONS.equals(toolId)) {
				this.updateLessons(dateManagerValidation);
			}
		} catch (Exception ex) {
			log.error("Cannot update the tool {} receibed", toolId, ex); 
		}
	}

	/**
	 * Function that detect if there is any change or not in the sent tool
	 * 
	 * @param toolId - String - the tool Id
	 * @param dateManagerValidation - DateManagerValidation - the validator used to update the object
	 * 
	 * @return boolean
	 */
	public boolean isChanged(String toolId, String[] columns) {
		String id = columns[0].replaceAll("\"", "");
		String siteId = getCurrentSiteId();
		boolean changed = false;
		if (DateManagerConstants.COMMON_ID_ASSIGNMENTS.equals(toolId.replaceAll("\"", ""))) {
			try {
				Assignment assignment = assignmentService.getAssignment(id);
				changed = this.compareDates(Date.from(assignment.getOpenDate()), columns[2]) 
						|| this.compareDates(Date.from(assignment.getDueDate()), columns[3])
						|| this.compareDates(Date.from(assignment.getCloseDate()), columns[4]);
			} catch (Exception ex) {
				log.error("Cannot identify the tool Content received", ex);
			}
		} else if (DateManagerConstants.COMMON_ID_ASSESSMENTS.equals(toolId.replaceAll("\"", ""))) {
			if (pubAssessmentServiceQueries.isPublishedAssessmentIdValid(Long.parseLong(id))) {
				PublishedAssessmentFacade pubAssessment = pubAssessmentServiceQueries.getPublishedAssessment(Long.parseLong(id));
				changed = this.compareDates(pubAssessment.getStartDate(), columns[2])
						|| this.compareDates(pubAssessment.getDueDate(), (columns.length > 3? columns[3] : ""))
						|| this.compareDates(pubAssessment.getRetractDate(), (columns.length > 4? columns[4] : ""));
			} else {
				AssessmentData assesmentData = assessmentServiceQueries.loadAssessment(Long.parseLong(id));
				AssessmentAccessControlIfc control = assesmentData.getAssessmentAccessControl();
				changed = this.compareDates(control.getStartDate(), columns[2])
						|| this.compareDates(control.getDueDate(), (columns.length > 3? columns[3] : ""))
						|| this.compareDates(control.getRetractDate(), (columns.length > 4? columns[4] : ""))
						|| this.compareDates(control.getFeedbackDate(), (columns.length > 5? columns[5] : ""))
						|| this.compareDates(control.getFeedbackEndDate(), (columns.length > 6? columns[6] : ""));
			}
		} else if (DateManagerConstants.COMMON_ID_GRADEBOOK.equals(toolId.replaceAll("\"", ""))) {
			org.sakaiproject.grading.api.Assignment gbitem = gradingService.getAssignment(getCurrentSiteId(), getCurrentSiteId(), Long.parseLong(id));
			if (columns[2] != null && columns[2].matches(".*\\d.*")) {
				columns[2] = columns[2].replaceAll("\"", "").replace("/", "-");
				try {
					LocalDate date;
					if (columns[2].contains("T")) {
						date = LocalDateTime.parse(columns[2], inputDateTimeFormatter).toLocalDate();
					} else {
						date = LocalDate.parse(columns[2], inputDateFormatter);
					}
					columns[2] = date.format(outputDateFormatter);
					ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
					if (gbitem.getDueDate() != null) {
						changed = !gbitem.getDueDate().toInstant().atZone(zone).toLocalDate().equals(date);
					} else {
						changed = true;
					}
				} catch (DateTimeParseException e) {
					log.warn("Could not parse due date [{}], {}", columns[2], e);
				}
			} else if (gbitem.getDueDate() != null) {
				changed = true; // remove due_date
			}
		} else if (DateManagerConstants.COMMON_ID_SIGNUP.equals(toolId.replaceAll("\"", ""))) {
			SignupMeeting meeting = signupService.loadSignupMeeting(Long.parseLong(id), getCurrentUserId(), siteId);
			changed = this.compareDates(meeting.getStartTime(), columns[2])
					|| this.compareDates(meeting.getEndTime(), columns[3])
					|| this.compareDates(meeting.getSignupBegins(), columns[4])
					|| this.compareDates(meeting.getSignupDeadline(), columns[5]);
		} else if (DateManagerConstants.COMMON_ID_RESOURCES.equals(toolId.replaceAll("\"", ""))) {
			List<ContentEntity> unformattedList = contentHostingService.getAllEntities("/group/"+siteId+"/");
			int i = 0;
			while (i < unformattedList.size() && !changed) {
				ContentEntity contentEnt = unformattedList.get(i);
				if (StringUtils.equals(id, contentEnt.getId())) {
					changed = this.compareDates((contentEnt.getRetractInstant() != null)? Date.from(contentEnt.getRetractInstant()) : null, columns[3])
							|| this.compareDates((contentEnt.getReleaseInstant() != null)? Date.from(contentEnt.getReleaseInstant()) : null, columns[2]);
				}
				i++;
			}
		} else if (DateManagerConstants.COMMON_ID_CALENDAR.equals(toolId.replaceAll("\"", ""))) {
			Calendar c = getCalendar();
			try {
				CalendarEvent calendarEvents = c.getEvent(id);
				changed = this.compareDates(new Date(calendarEvents.getRange().firstTime().getTime()), columns[2])
						|| this.compareDates(new Date(calendarEvents.getRange().lastTime().getTime()), columns[3]);
			} catch (Exception ex) {
				log.error("Cannot identify the tool Content received", ex);
			}
		} else if (DateManagerConstants.COMMON_ID_FORUMS.equals(toolId.replaceAll("\"", ""))) {
			if (columns[4].replaceAll("\"", "").equals(resourceLoader.getString("itemtype.forum"))) {
				DiscussionForum forum = (DiscussionForum) forumManager.getForumByIdWithTopics(Long.parseLong(id));
				
				changed = this.compareDates(forum.getOpenDate(), columns[2])
						|| this.compareDates(forum.getCloseDate(), columns[3]);
			} else {
				Topic topic = forumManager.getTopicById(false, Long.parseLong(id));
				topic = (topic!=null)? forumManager.getTopicById(true, Long.parseLong(id)) : topic;

				changed = this.compareDates(topic.getOpenDate(), columns[2])
						|| this.compareDates(topic.getCloseDate(), columns[3]);
			}
		} else if (DateManagerConstants.COMMON_ID_ANNOUNCEMENTS.equals(toolId.replaceAll("\"", ""))) {
			try {
				String anncRef = announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
				List<AnnouncementMessage> announcements = announcementService.getMessages(anncRef, null, false, true);
				int i = 0;
				while (i < announcements.size() && !changed) {
					AnnouncementMessage announcement = announcements.get(i);
					boolean releaseDateExist = announcement.getProperties().getProperty(AnnouncementService.RELEASE_DATE) != null;
					boolean retractDateExist = announcement.getProperties().getProperty(AnnouncementService.RETRACT_DATE) != null;
					if (announcement.getId().equals(id)) {
						if (releaseDateExist) {
							changed = changed || this.compareDates(Date.from(announcement.getProperties().getInstantProperty(AnnouncementService.RELEASE_DATE)), columns[2]);
						} else if (columns[2] != null && columns[2].matches(".*\\d.*")) {
							changed = true; // new release_date
						}
						if (retractDateExist) {
							changed = changed || this.compareDates(Date.from(announcement.getProperties().getInstantProperty(AnnouncementService.RETRACT_DATE)), columns[3]);
						} else if (columns[3] != null && columns[3].matches(".*\\d.*")) {
							changed = true; // new retract_date
						}
					}
					i++;
				}
			} catch (Exception ex) {
				log.error("Cannot identify the tool Content received", ex); 
			}
		} else if (DateManagerConstants.COMMON_ID_LESSONS.equals(toolId.replaceAll("\"", ""))) {
			JSONArray jsonLessons = this.getLessonsForContext(siteId);
			int i = 0;
			while (i < jsonLessons.size() && !changed) {
				JSONObject lesson = (JSONObject) jsonLessons.get(i);
				if (Long.toString((Long)lesson.get("id")).equals(id) && columns[2] != null && columns[2].matches(".*\\d.*")) {
					if (lesson.get("open_date") != null) {
						changed = this.compareDates(this.stringToDate((String) lesson.get("open_date")), columns[2]);
					} else {
						changed = true; // new open_date
					}
				} else if (Long.toString((Long)lesson.get("id")).equals(id) && !columns[2].matches(".*\\d.*") && lesson.get("open_date") != null) {
					changed = true; // remove open_date
				}
				i++;
			}
		}

		return changed;
	}

	/**
	 * Function that compare two dates
	 * 
	 * @param date - Date
	 * @param dateString - String - Date as string
	 * 
	 * @return boolean
	 */
	public boolean compareDates(Date date, String dateString) {
		boolean isDifferent = false;
		if (dateString != null && StringUtils.isNotBlank(dateString.replaceAll("\"", ""))) {
			if (date != null) {
				isDifferent = this.stringToDate(dateString.replaceAll("\"", "")).compareTo(this.stringToDate(this.formatToUserDateFormat(date))) != 0;
			} else {
				isDifferent = true;
			}
		} else if (date != null) {
			isDifferent = true;
		}
		return isDifferent;
	}

	/**
	 * Function that convert a String into a Date using the specific pattern: 'yyyy-MM-dd'T'HH:mm:ss'
	 * 
	 * @param dateString - String - Date as string
	 * 
	 * @return Date
	 */
	public Date stringToDate(String dateString) {
		ZoneId zone = userTimeService.getLocalTimeZone().toZoneId();
		LocalDateTime localDateTime = LocalDateTime.parse(dateString, inputDateTimeFormatter);
		return Date.from(localDateTime.atZone(zone).toInstant());
	}

	/**
	 * Function that create a jsonObject using the columnsNames and the columns
	 * 
	 * @param columnsNames - String[] - the columns names
	 * @param columns - String[] - the information of the columns
	 * @param idx - int
	 * 
	 * @return JSONObject
	 */
	public JSONObject createJsonObject(String[] columnsNames, String[] columns, int idx) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("idx", idx);
		for (int i = 0; i < columnsNames.length; i++) {
			jsonObject.put(columnsNames[i], columns[i]);
		}
		return  jsonObject;
	}
}
