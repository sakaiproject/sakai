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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
	@Setter private SiteService siteService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SimplePageToolDao simplePageToolDao;
	@Setter private TimeService timeService;
	@Setter private UserTimeService userTimeService;
	@Setter private SamigoAvailableNotificationService samigoAvailableNotificationService;
	@Setter private FormattedText formattedText;

	private static final ResourceLoader rb = new ResourceLoader("datemanager");
	private final Map<String, Calendar> calendarMap = new HashMap<>();

	public void init() {
		setAssessmentServiceQueries(assessmentPersistenceService.getAssessmentFacadeQueries());
		setPubAssessmentServiceQueries(assessmentPersistenceService.getPublishedAssessmentFacadeQueries());
	}

	private String getCurrentToolSessionAttribute(String name) {
		ToolSession session = sessionManager.getCurrentToolSession();
		return session != null ? session.getAttribute(name).toString() : "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentSiteId() {
		String siteID = getCurrentToolSessionAttribute(STATE_SITE_ID);
		if (StringUtils.isEmpty(siteID)) {
			siteID = toolManager.getCurrentPlacement().getContext();
		}

		return siteID;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Locale getUserLocale() {
		Locale locale = prefService.getLocale(getCurrentUserId());
		if (locale == null) locale = Locale.US;
		return locale;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage(String messageId) {
		return rb.getString(messageId);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateManagerConstants.DATEPICKER_DATETIME_FORMAT);
		String text = userDate.format(formatter);
		return text;
	}

	/***** ASSIGNMENTS *****/
	/**
	 * {@inheritDoc}
	 */
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
				if (assignment.getDraft()) extraInfo = rb.getString("itemtype.draft");
				assobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, extraInfo);
				jsonAssignments.add(assobj);
			} catch (Exception e) {
				log.error("Error while trying to add assignment {}", assignment.getId(), e);
			}
		}
		return orderJSONArrayByTitle(jsonAssignments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateAssignments(String siteId, JSONArray assignments) throws Exception {
		DateManagerValidation assignmentValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ASSIGNMENTS).getTitle();
		for (int i = 0; i < assignments.size(); i++) {
			JSONObject jsonAssignment = (JSONObject)assignments.get(i);
			String assignmentId = (String)jsonAssignment.get(DateManagerConstants.JSON_ID_PARAM_NAME);
			int idx = Integer.parseInt(jsonAssignment.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				if (assignmentId == null) {
                                        errors.add(new DateManagerError("assignment", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.assignments.item.name")}), "assignments", toolTitle, idx));
					continue;
				}

				String assignmentReference = assignmentService.assignmentReference(siteId, assignmentId);

				if (!assignmentService.allowUpdateAssignment(assignmentReference)) {
					errors.add(new DateManagerError("assignment", rb.getString("error.update.permission.denied"), "assignments", toolTitle, idx));
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
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.not.found"), "assignments", toolTitle, idx));
				}
				if (dueDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, rb.getString("error.due.date.not.found"), "assignments", toolTitle, idx));
				}
				if (acceptUntil == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME, rb.getString("error.accept.until.not.found"), "assignments", toolTitle, idx));
				}

				if (errored) {
					continue;
				}

				Assignment assignment = assignmentService.getAssignment(assignmentId);

				if (assignment == null) {
					errors.add(new DateManagerError("assignment", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.assignments.item.name")}), "assignments", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(assignment, openDate, dueDate, acceptUntil, null, null);

				if (!update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.before.due.date"), "assignments", toolTitle, idx));
					continue;
				}

				if (update.dueDate.isAfter(update.acceptUntilDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, rb.getString("error.due.date.before.accept.until"), "assignments", toolTitle, idx));
					continue;
				}

				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "assignments", toolTitle, idx));
				log.error("Error trying to validate Assignments {}", ex);
			}
		}

		assignmentValidate.setErrors(errors);
		assignmentValidate.setUpdates(updates);
		return assignmentValidate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateAssignments(DateManagerValidation assignmentValidation) throws Exception {
		for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) assignmentValidation.getUpdates()) {
			Assignment assignment = (Assignment) update.object;
			assignment.setOpenDate(update.openDate);
			assignment.setDueDate(update.dueDate);
			assignment.setCloseDate(update.acceptUntilDate);
			assignmentService.updateAssignment(assignment);

			// if assignment sending grades to gradebook, update the due date in the gradebook
			String associatedGradebookAssignment = assignment.getProperties().get(AssignmentConstants.PROP_ASSIGNMENT_ASSOCIATE_GRADEBOOK_ASSIGNMENT);
			if (StringUtils.isNotBlank(associatedGradebookAssignment)) {
				// only update externally linked assignments since internal links are already handled
				if (gradingService.isExternalAssignmentDefined(assignment.getContext(), associatedGradebookAssignment)) {
					org.sakaiproject.grading.api.Assignment gAssignment = gradingService.getExternalAssignment(assignment.getContext(), associatedGradebookAssignment);
					if (gAssignment != null) {
						gradingService.updateExternalAssessment(
								assignment.getContext(),
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

	/***** ASSESSMENTS *****/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONArray getAssessmentsForContext(String siteId) {
		JSONArray jsonAssessments = new JSONArray();
		List<AssessmentData> assessments = assessmentServiceQueries.getAllActiveAssessmentsByAgent(getCurrentSiteId());
		List<PublishedAssessmentFacade> pubAssessments = pubAssessmentServiceQueries.getBasicInfoOfAllPublishedAssessments2(PublishedAssessmentFacadeQueries.TITLE, true, getCurrentSiteId());
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_ASSESSMENTS);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ASSESSMENTS).getTitle();
		for (AssessmentData assessment : assessments) {
			AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
			boolean lateHandling = (control.getLateHandling() != null && control.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);
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
			assobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, rb.getString("itemtype.draft"));
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
			boolean lateHandling = (control.getLateHandling() != null && control.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateAssessments(String siteId, JSONArray assessments) throws Exception {
		DateManagerValidation assessmentValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();
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

				Object assessment;
				AssessmentAccessControlIfc control;
				if (isDraft) {
					assessment = assessmentServiceQueries.getAssessment(assessmentId);
					control = ((AssessmentFacade) assessment).getAssessmentAccessControl();
				} else {
					assessment = pubAssessmentServiceQueries.getPublishedAssessment(assessmentId);
					control = ((PublishedAssessmentFacade) assessment).getAssessmentAccessControl();
				}
				boolean lateHandling = control.getLateHandling() != null && control.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION;

				if (assessment == null) {
					errors.add(new DateManagerError("assessment", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.assessments.item.name")}), "assessments", toolTitle, idx));
					continue;
				}
				boolean errored = false;

				if (openDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.assessments.open.date.not.found"), "assessments", toolTitle, idx));
				}
				if (acceptUntil != null) {
					if (dueDate == null) {
						errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME,rb.getString("error.due.date.not.found.accept.until"),"assessments", toolTitle, idx));
						errored = true;
					} else if (acceptUntil.isBefore(dueDate)) {
						errors.add(new DateManagerError(DateManagerConstants.JSON_ACCEPTUNTIL_PARAM_NAME,rb.getString("error.accept.until.before.due.date.open.date"),"assessments", toolTitle, idx));
						errored = true;
					}
				}

				Integer feedbackMode = isDraft ? ((AssessmentFacade) assessment).getAssessmentFeedback().getFeedbackDelivery()
												: ((PublishedAssessmentFacade) assessment).getAssessmentFeedback().getFeedbackDelivery();
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackMode) && feedbackStart == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_FEEDBACKSTART_PARAM_NAME, rb.getString("error.feedback.start.not.found"), "assessments", toolTitle, idx));
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
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.assessments.open.date.before.due.date"), "assessments", toolTitle, idx));
					continue;
				}

				if (lateHandling && dueDate != null && acceptUntil != null && update.dueDate.isAfter(update.acceptUntilDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, rb.getString("error.due.date.before.accept.until"), "assessments", toolTitle, idx));
					continue;
				}

				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackMode) && feedbackStart != null && feedbackEnd != null && feedbackEnd.isBefore(feedbackStart)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_FEEDBACKEND_PARAM_NAME, rb.getString("error.feedback.start.before.feedback.end"), "assessments", toolTitle, idx));
					continue;
				}

				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "assessments", toolTitle, idx));
				log.error("Error trying to validate Tests & Quizzes {}", ex);
			}
		}

		assessmentValidate.setErrors(errors);
		assessmentValidate.setUpdates(updates);
		return assessmentValidate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateAssessments(DateManagerValidation assessmentsValidation) throws Exception {
		for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) assessmentsValidation.getUpdates()) {
			if (update.object.getClass().equals(AssessmentFacade.class)) {
				AssessmentFacade assessment = (AssessmentFacade) update.object;
				AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
				boolean lateHandling = control.getLateHandling() != null && control.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION;
				control.setStartDate(Date.from(update.openDate));
				Date dueDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
				control.setDueDate(dueDateTemp);
				if (lateHandling) {
					Date lateDateTemp =
							update.acceptUntilDate != null ? Date.from(update.acceptUntilDate) : null;
					control.setRetractDate(lateDateTemp);
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
				boolean lateHandling = control.getLateHandling() != null && control.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION;
				control.setStartDate(Date.from(update.openDate));
				Date dueDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
				control.setDueDate(dueDateTemp);
				if (lateHandling) {
					Date lateDateTemp =
							update.acceptUntilDate != null ? Date.from(update.acceptUntilDate) : null;
					control.setRetractDate(lateDateTemp);
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
				String siteId = assessment.getOwnerSiteId();
				if (StringUtils.isNotBlank(siteId) && gradingService.isExternalAssignmentDefined(siteId, id)) {
					org.sakaiproject.grading.api.Assignment gAssignment = gradingService.getExternalAssignment(siteId, id);
					if (gAssignment != null) {
						gradingService.updateExternalAssessment(
								siteId,
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

	/***** GRADEBOOK *****/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONArray getGradebookItemsForContext(String siteId) {
		JSONArray jsonAssignments = new JSONArray();
		if(!gradingService.currentUserHasEditPerm(getCurrentSiteId())) {
			return jsonAssignments;
		}
		Collection<org.sakaiproject.grading.api.Assignment> gbitems = gradingService.getAssignments(siteId);
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_GRADEBOOK);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_GRADEBOOK).getTitle();
		for(org.sakaiproject.grading.api.Assignment gbitem : gbitems) {
			if(!gbitem.getExternallyMaintained()) {
				JSONObject gobj = new JSONObject();
				gobj.put(DateManagerConstants.JSON_ID_PARAM_NAME, gbitem.getId());
				gobj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, gbitem.getName());
				gobj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, gbitem.getDueDate());
				gobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
				gobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
				gobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, "false");
				jsonAssignments.add(gobj);
			}
		}
		return orderJSONArrayByTitle(jsonAssignments);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateGradebookItems(String siteId, JSONArray gradebookItems) throws Exception {
		DateManagerValidation gradebookItemsValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();

		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_GRADEBOOK).getTitle();
		if (!gradebookItems.isEmpty() && !gradingService.currentUserHasEditPerm(getCurrentSiteId())) {
			errors.add(new DateManagerError("gbitem", rb.getString("error.update.permission.denied"), "gradebookItems", toolTitle, 0));
		}

		for (int i = 0; i < gradebookItems.size(); i++) {
			JSONObject jsonItem = (JSONObject)gradebookItems.get(i);
			int idx = Integer.parseInt(jsonItem.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {
				Long itemId = (Long)jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				if (itemId == null) {
					errors.add(new DateManagerError("gbitem", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.gradebook.item.name")}), "gradebookItems", toolTitle, idx));
					continue;
				}

				String dueDateRaw = (String) jsonItem.get(DateManagerConstants.JSON_DUEDATE_PARAM_NAME);
				Instant dueDate = null;
				if (StringUtils.isNotBlank(dueDateRaw)) {
					dueDate = userTimeService.parseISODateInUserTimezone(dueDateRaw).toInstant();
				}

				org.sakaiproject.grading.api.Assignment gbitem = gradingService.getAssignment(getCurrentSiteId(), itemId);
				if (gbitem == null) {
					errors.add(new DateManagerError("gbitem", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.gradebook.item.name")}), "gradebookItems", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(gbitem, null, dueDate, null, null, null);
				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "gradebookItems", toolTitle, idx));
				log.error("Error trying to validate Gradebook {}", ex);
			}
		}

		gradebookItemsValidate.setErrors(errors);
		gradebookItemsValidate.setUpdates(updates);
		return gradebookItemsValidate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateGradebookItems(DateManagerValidation gradebookItemsValidate) throws Exception {
		for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) gradebookItemsValidate.getUpdates()) {
			org.sakaiproject.grading.api.Assignment assignmentDefinition = (org.sakaiproject.grading.api.Assignment) update.object;
			Date dueDateTemp = update.dueDate != null ? Date.from(update.dueDate) : null;
			assignmentDefinition.setDueDate(dueDateTemp);
			gradingService.updateAssignment(getCurrentSiteId(), assignmentDefinition.getId(), assignmentDefinition);
		}
	}

	/***** SIGNUP *****/
	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateSignupMeetings(String siteId, JSONArray signupMeetings) throws Exception {
		DateManagerValidation meetingsValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();

		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_SIGNUP).getTitle();
		if (!signupMeetings.isEmpty() && !signupService.isAllowedToCreateinSite(getCurrentUserId(), getCurrentSiteId())) {
			errors.add(new DateManagerError("signup", rb.getString("error.update.permission.denied"), "signupMeetings", toolTitle, 0));
		}
		for (int i = 0; i < signupMeetings.size(); i++) {
			JSONObject jsonMeeting = (JSONObject)signupMeetings.get(i);
			int idx = Integer.parseInt(jsonMeeting.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				Long meetingId = (Long)jsonMeeting.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				if (meetingId == null) {
					errors.add(new DateManagerError("signup", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.signup.item.name")}), "signupMeetings", toolTitle, idx));
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
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (dueDate == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, rb.getString("error.due.date.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (signupBegins == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME, rb.getString("error.signup.begins.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (signupDeadline == null) {
					errored = errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPDEADLINE_PARAM_NAME, rb.getString("error.signup.deadline.not.found"), "signupMeetings", toolTitle, idx));
				}
				if (errored) {
					continue;
				}

				SignupMeeting meeting = signupService.loadSignupMeeting(meetingId, getCurrentUserId(), getCurrentSiteId());
				if (meeting == null) {
					errors.add(new DateManagerError("signup", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.signup.item.name")}), "signupMeetings", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(meeting, openDate, dueDate, null, null, null);
				update.setSignupBegins(signupBegins);
				update.setSignupDeadline(signupDeadline);
                                if (update.openDate != null && update.dueDate != null && !update.openDate.isBefore(update.dueDate)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.before.due.date"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                if (update.signupBegins != null && update.openDate != null && update.signupBegins.isAfter(update.openDate)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME, rb.getString("error.signup.begins.after.open.date"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                if (update.signupDeadline != null && update.dueDate != null && update.signupDeadline.isAfter(update.dueDate)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPDEADLINE_PARAM_NAME, rb.getString("error.signup.deadline.after.due.date"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                if (update.signupBegins != null && update.signupDeadline != null && update.signupBegins.isAfter(update.signupDeadline)) {
                                        errors.add(new DateManagerError(DateManagerConstants.JSON_SIGNUPBEGINS_PARAM_NAME, rb.getString("error.signup.begins.after.signup.deadline"), "signupMeetings", toolTitle, idx));
                                        continue;
                                }
                                updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "signupMeetings", toolTitle, idx));
				log.error("Error trying to validate Sign Up {}", ex);
			}
		}
		meetingsValidate.setErrors(errors);
		meetingsValidate.setUpdates(updates);
		return meetingsValidate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateSignupMeetings(DateManagerValidation signupValidate) throws Exception {
		for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) signupValidate.getUpdates()) {
			SignupMeeting meeting = (SignupMeeting) update.object;
			meeting.setStartTime(Date.from(update.openDate));
			meeting.setEndTime(Date.from(update.dueDate));
			meeting.setSignupBegins(Date.from(update.signupBegins));
			meeting.setSignupDeadline(Date.from(update.signupDeadline));
			signupService.updateSignupMeeting(meeting, true);
		}
	}

	/***** RESOURCES *****/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONArray getResourcesForContext(String siteId) {
		JSONArray jsonResources = new JSONArray();
		List<ContentEntity> unformattedList = contentHostingService.getAllEntities("/group/"+siteId+"/");
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_RESOURCES);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_RESOURCES).getTitle();
		for(ContentEntity res : unformattedList) {
			JSONObject robj = new JSONObject();
			ResourceProperties contentResourceProps = res.getProperties();
			robj.put(DateManagerConstants.JSON_ID_PARAM_NAME, res.getId());
			robj.put(DateManagerConstants.JSON_TITLE_PARAM_NAME, contentResourceProps.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			if(res.getRetractInstant() != null) robj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, formatToUserInstantFormat(res.getRetractInstant()));
			else robj.put(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, null);
			if(res.getReleaseInstant() != null) robj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, formatToUserInstantFormat(res.getReleaseInstant()));
			else robj.put(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, null);
			robj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, StringUtils.defaultIfBlank(res.getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE), rb.getString("itemtype.folder")));
			robj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
			robj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
			jsonResources.add(robj);
		}
		return orderJSONArrayByTitle(jsonResources);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateResources(String siteId, JSONArray resources) throws Exception {
		DateManagerValidation resourcesValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();
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
					errors.add(new DateManagerError("resource", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.resources.item.name")}), "resources", toolTitle, idx));
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
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.before.due.date"), "resources", toolTitle, idx));
					continue;
				}
				
				entityType = (String)jsonResource.get(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME);
				if(!rb.getString("itemtype.folder").equals(entityType)) {
					resource = contentHostingService.editResource(resourceId);
					if (resource == null) {
						errors.add(new DateManagerError("resource", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.resources.item.name")}), "resources", toolTitle, idx));
						continue;
					}

					if (!contentHostingService.allowUpdateResource(resourceId)) {
						errors.add(new DateManagerError("resource", rb.getString("error.update.permission.denied"), "resources", toolTitle, idx));
					}
					updates.add(new DateManagerUpdate(resource, openDate, dueDate, null, null, null));
				} else {
					folder = contentHostingService.editCollection(resourceId);
					if (folder == null) {
						errors.add(new DateManagerError("resource", rb.getString("error.folder.not.found"), "resources", toolTitle, idx));
						continue;
					}

					if (!contentHostingService.allowUpdateCollection(resourceId)) {
						errors.add(new DateManagerError("resource", rb.getString("error.update.permission.denied"), "resources", toolTitle, idx));
					}
					updates.add(new DateManagerUpdate(folder, openDate, dueDate, null, null, null));
				}

			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "resources", toolTitle, idx));
				log.error("Error trying to validate Resources {}", e);

				if(entityType != null) {
					if(!rb.getString("itemtype.folder").equals(entityType)) {
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearUpdateResourceLocks(DateManagerValidation resourceValidation) throws Exception {
                try {
                        for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) resourceValidation.getUpdates()) {
                                if (update.object instanceof ContentResourceEdit) { 
                                        contentHostingService.cancelResource((ContentResourceEdit) update.getObject());
                                }
                        }
                } catch (Exception e) {
                        log.warn("Could not clear update for resource, {}", e.toString());
                }
        }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateResources(DateManagerValidation resourceValidation) throws Exception {
		for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) resourceValidation.getUpdates()) {
			if (update.object instanceof ContentCollectionEdit) {
				ContentCollectionEdit cce = (ContentCollectionEdit) update.object;
				if(update.dueDate != null) {
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

	/***** CALENDAR EVENTS *****/
	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateCalendarEvents(String siteId, JSONArray calendarEvents) throws Exception {
		DateManagerValidation calendarValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_CALENDAR).getTitle();
		Calendar c = getCalendar();
		CalendarEventEdit calendarEvent = null;

		for (int i = 0; i < calendarEvents.size(); i++) {
			JSONObject jsonEvent = (JSONObject)calendarEvents.get(i);
			String eventId = (String)jsonEvent.get(DateManagerConstants.JSON_ID_PARAM_NAME);
			int idx = Integer.parseInt(jsonEvent.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {
				if (eventId == null) {
					errors.add(new DateManagerError("calendar", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.calendar.item.name")}), "calendarEvents", toolTitle, idx));
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
                                        errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.not.found"), "calendarEvents", toolTitle, idx));
                                }
                                if (dueDate == null) {
                                        errored = errors.add(new DateManagerError(DateManagerConstants.JSON_DUEDATE_PARAM_NAME, rb.getString("error.due.date.not.found"), "calendarEvents", toolTitle, idx));
                                }
                                if (openDate != null && dueDate != null && dueDate.isBefore(openDate)) {
                                        errored = errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.before.end.date"), "calendarEvents", toolTitle, idx));
                                }
                                if (errored) {
                                        continue;
                                }

				if (!c.allowEditEvent(eventId)) {
					errors.add(new DateManagerError("calendar", rb.getString("error.event.permission"), "calendarEvents", toolTitle, idx));
				}
				else {
					calendarEvent = c.getEditEvent(eventId, CalendarService.EVENT_MODIFY_CALENDAR);
					if (calendarEvent == null) {
						errors.add(new DateManagerError("calendar", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.calendar.item.name")}), "calendarEvents", toolTitle, idx));
						continue;
					}

					updates.add(new DateManagerUpdate(calendarEvent, openDate, dueDate, null, null, null));
				}

			} catch (Exception ex) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "calendarEvents", toolTitle, idx));
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

		/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearUpdateCalendarLocks(DateManagerValidation calendarValidate) throws Exception {
		Calendar c = getCalendar();
		if (c != null) { 
                        try {
                                for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) calendarValidate.getUpdates()) {
                                        CalendarEventEdit edit = (CalendarEventEdit) update.object;
                                        c.cancelEvent(edit);
                                }
                        } catch (Exception e) {
                                log.warn("Could not clear update for calendar, {}", e.toString());
                        }
		} 
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateCalendarEvents(DateManagerValidation calendarValidation) throws Exception {
		Calendar c = getCalendar();
		if (c != null) {
			for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) calendarValidation .getUpdates()) {
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

	/***** FORUMS *****/
	/**
	 * {@inheritDoc}
	 */
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
			fobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, rb.getString("itemtype.forum"));
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
				tobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, rb.getString("itemtype.topic"));
				tobj.put(DateManagerConstants.JSON_TOOLTITLE_PARAM_NAME, toolTitle);
				tobj.put(DateManagerConstants.JSON_URL_PARAM_NAME, url);
				jsonForums.add(tobj);
			}
			jsonForums.add(fobj);
		}
		return orderJSONArrayByTitle(jsonForums);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateForums(String siteId, JSONArray forums) throws Exception {
		DateManagerValidation forumValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_FORUMS).getTitle();
		for (int i = 0; i < forums.size(); i++) {
			JSONObject jsonForum = (JSONObject)forums.get(i);
			int idx = Integer.parseInt(jsonForum.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				Long forumId = (Long)jsonForum.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				if (forumId == null) {
					errors.add(new DateManagerError("forum", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.forum.topic.item.name")}), "forums", toolTitle, idx));
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
                                if(rb.getString("itemtype.forum").equals(entityType)) {
                                        BaseForum forum = forumManager.getForumById(true, forumId);
                                        if (forum == null) {
                                                errors.add(new DateManagerError("forum",rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.forums.item.name")}), "forums", toolTitle, idx));
                                                continue;
                                        }

                                        update = new DateManagerUpdate(forum, openDate, dueDate, null, null, null);
                                } else {
                                        Topic topic = forumManager.getTopicById(true, forumId);
                                        if (topic == null) {
                                                errors.add(new DateManagerError("forum", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.topics.item.name")}), "forums", toolTitle, idx));
                                                continue;
                                        }

                                        update = new DateManagerUpdate(topic, openDate, dueDate, null, null, null);
                                }

				if (update.openDate != null
						&& update.dueDate != null
						&& !update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.before.close.date"), "forums", toolTitle, idx));
					continue;
				}
				updates.add(update);

			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "forums", toolTitle, idx));
				log.error("Error trying to validate Forums {}", e);
			}
		}

		forumValidate.setErrors(errors);
		forumValidate.setUpdates(updates);
		return forumValidate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateForums(DateManagerValidation forumValidation) throws Exception {
                for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) forumValidation.getUpdates()) {
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

	/***** ANNOUNCEMENTS *****/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONArray getAnnouncementsForContext(String siteId) {
		JSONArray jsonAnnouncements = new JSONArray();
		String anncRef = announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		try {
			String url = getUrlForTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS);
			String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS).getTitle();
			List announcements = announcementService.getMessages(anncRef, null, false, true);
			for(Object o : announcements) {
				AnnouncementMessage announcement = (AnnouncementMessage) o;
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
				aobj.put(DateManagerConstants.JSON_EXTRAINFO_PARAM_NAME, "false");
				jsonAnnouncements.add(aobj);
			}
		} catch (Exception e) {
			log.error("getAnnouncementsForContext error for context {} : {}", siteId, e);
		}
		return orderJSONArrayByTitle(jsonAnnouncements);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateAnnouncements(String siteId, JSONArray announcements) throws Exception {
		DateManagerValidation announcementValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();

		String anncRef = announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS).getTitle();
		AnnouncementMessageEdit announcement = null;
		for (int i = 0; i < announcements.size(); i++) {
			JSONObject jsonAnnouncement = (JSONObject)announcements.get(i);
			int idx = Integer.parseInt(jsonAnnouncement.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				String announcementId = (String)jsonAnnouncement.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				if (announcementId == null) {
					errors.add(new DateManagerError("announcement", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.announcements.item.name")}), "announcements", toolTitle, idx));
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
					errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.open.date.before.due.date"), "announcements", toolTitle, idx));
					continue;
				}

				AnnouncementChannel aChannel = announcementService.getAnnouncementChannel(anncRef);
				announcement = aChannel.editAnnouncementMessage(announcementId);
				if (announcement == null) {
					errors.add(new DateManagerError("announcement", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.announcements.item.name")}), "announcements", toolTitle, idx));
					continue;
				}

				updates.add(new DateManagerUpdate(announcement, openDate, dueDate, null, null, null));
			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "announcements", toolTitle, idx));
				log.error("Error trying to validate Announcements {}", e);

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

		/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearUpdateAnnouncementLocks(DateManagerValidation announcementValidate) throws Exception {
                try {
                        for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) announcementValidate.getUpdates()) {
                                announcementService.cancelMessage((AnnouncementMessageEdit) update.getObject());
                        }
                } catch (Exception e) {
                        log.warn("Could not clear update for announcement, {}", e.toString());
                }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateAnnouncements(DateManagerValidation announcementValidate) {
		String anncRef = announcementService.channelReference(getCurrentSiteId(), SiteService.MAIN_CONTAINER);
                try {
                        AnnouncementChannel aChannel = announcementService.getAnnouncementChannel(anncRef);
                        for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) announcementValidate.getUpdates()) {
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

	/***** LESSONS *****/
	/**
	 * {@inheritDoc}
	 */
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
						jsonLessons = addAllSubpages(simplePageToolDao.findItemsOnPage(itemId), itemId, jsonLessons, rb.getString("tool.lessons.extra.subpage"), processedItemIDs);
					}
				}
			}
		}
		return jsonLessons;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public DateManagerValidation validateLessons(String siteId, JSONArray lessons) throws Exception {
		DateManagerValidation lessonsValidate = new DateManagerValidation();
		List<DateManagerError> errors = new ArrayList<>();
		List<Object> updates = new ArrayList<>();

		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_LESSONS).getTitle();
		for (int i = 0; i < lessons.size(); i++) {
			JSONObject jsonItem = (JSONObject)lessons.get(i);
			int idx = Integer.parseInt(jsonItem.get(DateManagerConstants.JSON_IDX_PARAM_NAME).toString());

			try {

				String openDateRaw = (String) jsonItem.get(DateManagerConstants.JSON_OPENDATE_PARAM_NAME);
				Instant openDate = null;
				Long itemId = (Long)jsonItem.get(DateManagerConstants.JSON_ID_PARAM_NAME);
				if (itemId == null) {
					errors.add(new DateManagerError("page", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.lessons.item.name")}), "lessons", toolTitle, idx));
					continue;
				}

				if (StringUtils.isNotBlank(openDateRaw)) {
					openDate = userTimeService.parseISODateInUserTimezone(openDateRaw).toInstant();
				}	

				SimplePage page = simplePageToolDao.getPage(itemId);
				if (page == null) {
					errors.add(new DateManagerError("page", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.lessons.item.name")}), "lessons", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(page, openDate, null, null, null, null);
				updates.add(update);
			} catch(Exception e) {
				errors.add(new DateManagerError(DateManagerConstants.JSON_OPENDATE_PARAM_NAME, rb.getString("error.uncaught"), "lessons", toolTitle, idx));
				log.error("Error trying to validate Lessons {}", e);
			}
		}

		lessonsValidate.setErrors(errors);
		lessonsValidate.setUpdates(updates);
		return lessonsValidate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateLessons(DateManagerValidation lessonsValidation) throws Exception {
                for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) lessonsValidation.getUpdates()) {
                        SimplePage page = (SimplePage) update.object;
                        Date openDateTemp = update.openDate != null ? Date.from(update.openDate) : null;
                        page.setReleaseDate(openDateTemp);
                        log.debug("Saving changes on lessons : {}", simplePageToolDao.quickUpdate(page));
                }
	}

	private JSONArray orderJSONArrayByTitle(JSONArray jsonArray) {
		try {
			List list = (List) jsonArray.stream()
				.sorted((json1, json2) -> ((JSONObject) json1).get(DateManagerConstants.JSON_TITLE_PARAM_NAME).toString().toLowerCase()
					.compareTo(((JSONObject) json2).get(DateManagerConstants.JSON_TITLE_PARAM_NAME).toString().toLowerCase()))
				.collect(Collectors.toList());
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(JSONArray.toJSONString(list));
			jsonArray = (JSONArray) obj;
		} catch (Exception ex) {
			log.error("Cannot order the JSONArray elements alphabetically: {}", ex.getMessage());
		}
		return jsonArray;
	}
}
