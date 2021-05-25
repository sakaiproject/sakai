/**********************************************************************************
 Copyright (c) 2019 Apereo Foundation
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
           http://opensource.org/licenses/ecl2
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************/

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

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.service.gradebook.shared.GradebookService;
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
	@Setter private GradebookService gradebookService;
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
	@Setter private FormattedText formattedText;

	private static final ResourceLoader rb = new ResourceLoader("datemanager");
	private final Map<String, Calendar> calendarMap = new HashMap<>();

	public void init() {
		setAssessmentServiceQueries(assessmentPersistenceService.getAssessmentFacadeQueries());
		setPubAssessmentServiceQueries(assessmentPersistenceService.getPublishedAssessmentFacadeQueries());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCurrentSiteId() {
		return toolManager.getCurrentPlacement().getContext();
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
				assobj.put("id", assignment.getId());
				assobj.put("title", assignment.getTitle());
				assobj.put("due_date", formatToUserDateFormat(Date.from(assignment.getDueDate())));
				assobj.put("open_date", formatToUserDateFormat(Date.from(assignment.getOpenDate())));
				assobj.put("accept_until", formatToUserDateFormat(Date.from(assignment.getCloseDate())));
				assobj.put("tool_title", toolTitle);
				assobj.put("url", assignmentService.getDeepLink(siteId, assignment.getId(), getCurrentUserId()));
				String extraInfo = "false";
				if (assignment.getDraft()) extraInfo = rb.getString("itemtype.draft");
				assobj.put("extraInfo", extraInfo);
				jsonAssignments.add(assobj);
			} catch (Exception e) {
				log.error("Error while trying to add assignment {}", assignment.getId(), e);
			}
		}
		return jsonAssignments;
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
			String assignmentId = (String)jsonAssignment.get("id");
			int idx = Integer.parseInt(jsonAssignment.get("idx").toString());

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

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonAssignment.get("open_date")).toInstant();
				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonAssignment.get("due_date")).toInstant();
				Instant acceptUntil = userTimeService.parseISODateInUserTimezone((String)jsonAssignment.get("accept_until")).toInstant();

				boolean errored = false;

				if (openDate == null) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.not.found"), "assignments", toolTitle, idx));
					errored = true;
				}
				if (dueDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "assignments", toolTitle, idx));
					errored = true;
				}
				if (acceptUntil == null) {
					errors.add(new DateManagerError("accept_until", rb.getString("error.accept.until.not.found"), "assignments", toolTitle, idx));
					errored = true;
				}

				if (errored) {
					continue;
				}

				Assignment assignment = assignmentService.getAssignment(assignmentId);

				if (assignment == null) {
					errors.add(new DateManagerError("assignment", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.assignments.item.name")}), "assignments", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(assignment, openDate, dueDate, acceptUntil);

				if (!update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.before.due.date"), "assignments", toolTitle, idx));
					continue;
				}

				if (update.dueDate.isAfter(update.acceptUntilDate)) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.before.accept.until"), "assignments", toolTitle, idx));
					continue;
				}

				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "assignments", toolTitle, idx));
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
			assobj.put("id", assessment.getAssessmentBaseId());
			assobj.put("title", assessment.getTitle());
			assobj.put("due_date", formatToUserDateFormat(control.getDueDate()));
			assobj.put("open_date", formatToUserDateFormat(control.getStartDate()));
			assobj.put("accept_until", formatToUserDateFormat(control.getRetractDate()));
			assobj.put("is_draft", true);
			assobj.put("late_handling", lateHandling);
			assobj.put("tool_title", toolTitle);
			assobj.put("url", url);
			assobj.put("extraInfo", rb.getString("itemtype.draft"));
			if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
				assobj.put("feedback_start", formatToUserDateFormat(control.getFeedbackDate()));
				assobj.put("feedback_end", formatToUserDateFormat(control.getFeedbackEndDate()));
				assobj.put("feedback_by_date", true);
			} else {
				assobj.put("feedback_start", null);
				assobj.put("feedback_end", null);
				assobj.put("feedback_by_date", false);
			}
			jsonAssessments.add(assobj);
		}
		for (PublishedAssessmentFacade paf : pubAssessments) {
			PublishedAssessmentFacade assessment = pubAssessmentServiceQueries.getSettingsOfPublishedAssessment(paf.getPublishedAssessmentId());
			AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
			boolean lateHandling = (control.getLateHandling() != null && control.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION);
			JSONObject assobj = new JSONObject();
			assobj.put("id", assessment.getPublishedAssessmentId());
			assobj.put("title", assessment.getTitle());
			assobj.put("due_date", formatToUserDateFormat(control.getDueDate()));
			assobj.put("open_date", formatToUserDateFormat(control.getStartDate()));
			assobj.put("accept_until", formatToUserDateFormat(control.getRetractDate()));
			assobj.put("is_draft", false);
			assobj.put("late_handling", lateHandling);
			assobj.put("tool_title", toolTitle);
			assobj.put("url", url);
			assobj.put("extraInfo", "false");
			if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
				assobj.put("feedback_start", formatToUserDateFormat(control.getFeedbackDate()));
				assobj.put("feedback_end", formatToUserDateFormat(control.getFeedbackEndDate()));
				assobj.put("feedback_by_date", true);
			} else {
				assobj.put("feedback_start", null);
				assobj.put("feedback_end", null);
				assobj.put("feedback_by_date", false);
			}
			jsonAssessments.add(assobj);
		}
		return jsonAssessments;
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
			Long assessmentId = Long.parseLong(jsonAssessment.get("id").toString());
			int idx = Integer.parseInt(jsonAssessment.get("idx").toString());

			try {

				/* VALIDATE IF USER CAN UPDATE THE ASSESSMENT */

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonAssessment.get("open_date")).toInstant();
				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonAssessment.get("due_date")).toInstant();
				Instant acceptUntil = userTimeService.parseISODateInUserTimezone((String)jsonAssessment.get("accept_until")).toInstant();
				Instant feedbackStart = userTimeService.parseISODateInUserTimezone((String)jsonAssessment.get("feedback_start")).toInstant();
				Instant feedbackEnd = userTimeService.parseISODateInUserTimezone((String)jsonAssessment.get("feedback_end")).toInstant();
				boolean isDraft = Boolean.parseBoolean(jsonAssessment.get("is_draft").toString());

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
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.not.found"), "assessments", toolTitle, idx));
					errored = true;
				}
				if (dueDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "assessments", toolTitle, idx));
					errored = true;
				}
				if (acceptUntil == null && lateHandling) {
					errors.add(new DateManagerError("accept_until", rb.getString("error.accept.until.not.found"), "assessments", toolTitle, idx));
					errored = true;
				}

				Integer feedbackMode = isDraft ? ((AssessmentFacade) assessment).getAssessmentFeedback().getFeedbackDelivery()
												: ((PublishedAssessmentFacade) assessment).getAssessmentFeedback().getFeedbackDelivery();
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackMode) && feedbackStart == null) {
					errors.add(new DateManagerError("feedback_start", rb.getString("error.feedback.start.not.found"), "assessments", toolTitle, idx));
					errored = true;
				}

				if (errored) {
					continue;
				}

				log.debug("Open {} ; Due {} ; Until {} ; Feedback Start {} ; Feedback End {}", jsonAssessment.get("open_date_label"), jsonAssessment.get("due_date_label"),
								jsonAssessment.get("accept_until_label"), jsonAssessment.get("feedback_start_label"), jsonAssessment.get("feedback_end_label"));
				if(StringUtils.isBlank((String)jsonAssessment.get("due_date_label"))) {
					dueDate = null;
				}
				if(StringUtils.isBlank((String)jsonAssessment.get("accept_until_label"))) {
					acceptUntil = null;
				}
				if(StringUtils.isBlank((String)jsonAssessment.get("feedback_start_label"))) {
					feedbackStart = null;
				}
				if(StringUtils.isBlank((String)jsonAssessment.get("feedback_end_label"))) {
					feedbackEnd = null;
				}

				DateManagerUpdate update = new DateManagerUpdate(assessment, openDate, dueDate, acceptUntil);
				update.setFeedbackStartDate(feedbackStart);
				update.setFeedbackEndDate(feedbackEnd);

				if (dueDate != null && !update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.before.due.date"), "assessments", toolTitle, idx));
					continue;
				}

				if (lateHandling && dueDate != null && acceptUntil != null && update.dueDate.isAfter(update.acceptUntilDate)) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.before.accept.until"), "assessments", toolTitle, idx));
					continue;
				}

				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(feedbackMode) && feedbackStart != null && feedbackEnd != null && feedbackEnd.isBefore(feedbackStart)) {
					errors.add(new DateManagerError("feedback_end", rb.getString("error.feedback.start.before.feedback.end"), "assessments", toolTitle, idx));
					continue;
				}

				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "assessments", toolTitle, idx));
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
				if (update.dueDate != null) {
					control.setDueDate(Date.from(update.dueDate));
				}
				if (lateHandling && update.acceptUntilDate != null) {
					control.setRetractDate(Date.from(update.acceptUntilDate));
				}
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
					control.setFeedbackDate(Date.from(update.feedbackStartDate));
					if (update.feedbackEndDate != null) {
						control.setFeedbackEndDate(Date.from(update.feedbackEndDate));
					}
				}
				assessment.setAssessmentAccessControl(control);
				assessmentServiceQueries.saveOrUpdate(assessment);

			} else {
				PublishedAssessmentFacade assessment = (PublishedAssessmentFacade) update.object;
				AssessmentAccessControlIfc control = assessment.getAssessmentAccessControl();
				boolean lateHandling = control.getLateHandling() != null && control.getLateHandling() == AssessmentAccessControlIfc.ACCEPT_LATE_SUBMISSION;
				control.setStartDate(Date.from(update.openDate));
				if (update.dueDate != null) {
					control.setDueDate(Date.from(update.dueDate));
				}
				if (lateHandling && update.acceptUntilDate != null) {
					control.setRetractDate(Date.from(update.acceptUntilDate));
				}
				if (AssessmentFeedbackIfc.FEEDBACK_BY_DATE.equals(assessment.getAssessmentFeedback().getFeedbackDelivery())) {
					control.setFeedbackDate(Date.from(update.feedbackStartDate));
					if (update.feedbackEndDate != null) {
						control.setFeedbackEndDate(Date.from(update.feedbackEndDate));
					}
				}
				assessment.setAssessmentAccessControl(control);
				pubAssessmentServiceQueries.saveOrUpdate(assessment);
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
		if(!gradebookService.currentUserHasEditPerm(getCurrentSiteId())) {
			return jsonAssignments;
		}
		Collection<org.sakaiproject.service.gradebook.shared.Assignment> gbitems = gradebookService.getAssignments(siteId);
		String url = getUrlForTool(DateManagerConstants.COMMON_ID_GRADEBOOK);
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_GRADEBOOK).getTitle();
		for(org.sakaiproject.service.gradebook.shared.Assignment gbitem : gbitems) {
			if(!gbitem.isExternallyMaintained()) {
				JSONObject gobj = new JSONObject();
				gobj.put("id", gbitem.getId());
				gobj.put("title", gbitem.getName());
				gobj.put("due_date", gbitem.getDueDate());
				gobj.put("tool_title", toolTitle);
				gobj.put("url", url);
				gobj.put("extraInfo", "false");
				jsonAssignments.add(gobj);
			}
		}
		return jsonAssignments;
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
		if (!gradebookItems.isEmpty() && !gradebookService.currentUserHasEditPerm(getCurrentSiteId())) {
			errors.add(new DateManagerError("gbitem", rb.getString("error.update.permission.denied"), "gradebookItems", toolTitle, 0));
		}

		for (int i = 0; i < gradebookItems.size(); i++) {
			JSONObject jsonItem = (JSONObject)gradebookItems.get(i);
			int idx = Integer.parseInt(jsonItem.get("idx").toString());

			try {
				Long itemId = (Long)jsonItem.get("id");
				if (itemId == null) {
					errors.add(new DateManagerError("gbitem", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.gradebook.item.name")}), "gradebookItems", toolTitle, idx));
					continue;
				}

				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonItem.get("due_date")).toInstant();

				if (dueDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "gradebookItems", toolTitle, idx));
					continue;
				}

				org.sakaiproject.service.gradebook.shared.Assignment gbitem = gradebookService.getAssignment(getCurrentSiteId(), itemId);
				if (gbitem == null) {
					errors.add(new DateManagerError("gbitem", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.gradebook.item.name")}), "gradebookItems", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(gbitem, null, dueDate, null);
				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "gradebookItems", toolTitle, idx));
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
			org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition = (org.sakaiproject.service.gradebook.shared.Assignment) update.object;
			assignmentDefinition.setDueDate(Date.from(update.dueDate));
			gradebookService.updateAssignment(getCurrentSiteId(), assignmentDefinition.getId(), assignmentDefinition);
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
			mobj.put("id", meeting.getId());
			mobj.put("title", meeting.getTitle());
			mobj.put("due_date", formatToUserDateFormat(meeting.getEndTime()));
			mobj.put("open_date", formatToUserDateFormat(meeting.getStartTime()));
			mobj.put("signup_begins", formatToUserDateFormat(meeting.getSignupBegins()));
			mobj.put("signup_deadline", formatToUserDateFormat(meeting.getSignupDeadline()));
			mobj.put("tool_title", toolTitle);
			mobj.put("url", url);
			mobj.put("extraInfo", "false");
			jsonMeetings.add(mobj);
		}
		return jsonMeetings;
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
			int idx = Integer.parseInt(jsonMeeting.get("idx").toString());

			try {

				Long meetingId = (Long)jsonMeeting.get("id");
				if (meetingId == null) {
					errors.add(new DateManagerError("signup", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.signup.item.name")}), "signupMeetings", toolTitle, idx));
					continue;
				}

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonMeeting.get("open_date")).toInstant();
				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonMeeting.get("due_date")).toInstant();
				Instant signupBegins = userTimeService.parseISODateInUserTimezone((String)jsonMeeting.get("signup_begins")).toInstant();
				Instant signupDeadline = userTimeService.parseISODateInUserTimezone((String)jsonMeeting.get("signup_deadline")).toInstant();
				boolean errored = false;
				if (openDate == null) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.not.found"), "signupMeetings", toolTitle, idx));
					errored = true;
				}
				if (dueDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "signupMeetings", toolTitle, idx));
					errored = true;
				}
				if (signupBegins == null) {
					errors.add(new DateManagerError("signup_begins", rb.getString("error.signup.begins.not.found"), "signupMeetings", toolTitle, idx));
					errored = true;
				}
				if (signupDeadline == null) {
					errors.add(new DateManagerError("signup_deadline", rb.getString("error.signup.deadline.not.found"), "signupMeetings", toolTitle, idx));
					errored = true;
				}
				if (errored) {
					continue;
				}

				SignupMeeting meeting = signupService.loadSignupMeeting(meetingId, getCurrentUserId(), getCurrentSiteId());
				if (meeting == null) {
					errors.add(new DateManagerError("signup", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.signup.item.name")}), "signupMeetings", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(meeting, openDate, dueDate, null);
				update.setSignupBegins(signupBegins);
				update.setSignupDeadline(signupDeadline);
				if (!update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.before.due.date"), "signupMeetings", toolTitle, idx));
					continue;
				}
				if (update.signupBegins.isAfter(update.openDate)) {
					errors.add(new DateManagerError("signup_begins", rb.getString("error.signup.begins.after.open.date"), "signupMeetings", toolTitle, idx));
					continue;
				}
				if (update.signupDeadline.isAfter(update.dueDate)) {
					errors.add(new DateManagerError("signup_deadline", rb.getString("error.signup.deadline.after.due.date"), "signupMeetings", toolTitle, idx));
					continue;
				}
				if (update.signupBegins.isAfter(update.signupDeadline)) {
					errors.add(new DateManagerError("signup_begins", rb.getString("error.signup.begins.after.signup.deadline"), "signupMeetings", toolTitle, idx));
					continue;
				}
				updates.add(update);

			} catch (Exception ex) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "signupMeetings", toolTitle, idx));
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
			robj.put("id", res.getId());
			robj.put("title", contentResourceProps.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
			if(res.getRetractInstant() != null) robj.put("due_date", formatToUserInstantFormat(res.getRetractInstant()));
			else robj.put("due_date", null);
			if(res.getReleaseInstant() != null) robj.put("open_date", formatToUserInstantFormat(res.getReleaseInstant()));
			else robj.put("open_date", null);
			robj.put("extraInfo", StringUtils.defaultIfBlank(res.getProperties().getProperty(ResourceProperties.PROP_CONTENT_TYPE), rb.getString("itemtype.folder")));
			robj.put("tool_title", toolTitle);
			robj.put("url", url);
			jsonResources.add(robj);
		}
		return jsonResources;
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
		for (int i = 0; i < resources.size(); i++) {
			JSONObject jsonResource = (JSONObject)resources.get(i);
			int idx = Integer.parseInt(jsonResource.get("idx").toString());

			try {

				String resourceId = (String)jsonResource.get("id");
				if (resourceId == null) {
					errors.add(new DateManagerError("resource", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.resources.item.name")}), "resources", toolTitle, idx));
					continue;
				}

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonResource.get("open_date")).toInstant();
				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonResource.get("due_date")).toInstant();
				boolean errored = false;
				if (openDate == null) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.not.found"), "resources", toolTitle, idx));
					errored = true;
				}
				if (dueDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "resources", toolTitle, idx));
					errored = true;
				}
				if (errored) {
					continue;
				}

				log.debug("Open {} ; Due {}", jsonResource.get("open_date_label"), jsonResource.get("due_date_label"));
				if(StringUtils.isBlank((String)jsonResource.get("due_date_label"))) {
					dueDate = null;
				}

				String entityType = (String)jsonResource.get("extraInfo");
				DateManagerUpdate update;
				if(!rb.getString("itemtype.folder").equals(entityType)) {
					ContentResourceEdit resource = contentHostingService.editResource(resourceId);
					if (resource == null) {
						errors.add(new DateManagerError("resource", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.resources.item.name")}), "resources", toolTitle, idx));
						continue;
					}

					if (!contentHostingService.allowUpdateResource(resourceId)) {
						errors.add(new DateManagerError("resource", rb.getString("error.update.permission.denied"), "resources", toolTitle, idx));
					}
					update = new DateManagerUpdate(resource, openDate, dueDate, null);
				} else {
					ContentCollectionEdit folder = contentHostingService.editCollection(resourceId);
					if (folder == null) {
						errors.add(new DateManagerError("resource", rb.getString("error.folder.not.found"), "resources", toolTitle, idx));
						continue;
					}

					if (!contentHostingService.allowUpdateCollection(resourceId)) {
						errors.add(new DateManagerError("resource", rb.getString("error.update.permission.denied"), "resources", toolTitle, idx));
					}
					update = new DateManagerUpdate(folder, openDate, dueDate, null);
				}

				if (dueDate != null && !update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.before.due.date"), "resources", toolTitle, idx));
					continue;
				}
				updates.add(update);

			} catch(Exception e) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "resources", toolTitle, idx));
				log.error("Error trying to validate Resources {}", e);
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
	public void updateResources(DateManagerValidation resourceValidation) throws Exception {
		for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) resourceValidation.getUpdates()) {
			if (update.object instanceof ContentCollectionEdit) {
				ContentCollectionEdit cce = (ContentCollectionEdit) update.object;
				if(update.dueDate != null) {
					cce.setRetractInstant(Instant.from(update.dueDate));
				}
				cce.setReleaseInstant(Instant.from(update.openDate));
				contentHostingService.commitCollection(cce);
			} else {
				ContentResourceEdit cre = (ContentResourceEdit) update.object;
				if(update.dueDate != null) {
					cre.setRetractInstant(Instant.from(update.dueDate));
				}
				cre.setReleaseInstant(Instant.from(update.openDate));
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
				cobj.put("id", calendarEvent.getId());
				cobj.put("title", calendarEvent.getDisplayName());
				cobj.put("open_date", formatToUserDateFormat(new Date(calendarEvent.getRange().firstTime().getTime())));
				cobj.put("due_date", formatToUserDateFormat(new Date(calendarEvent.getRange().lastTime().getTime())));
				cobj.put("tool_title", toolTitle);
				cobj.put("url", url + "?eventReference=" + formattedText.escapeUrl(calendarEvent.getReference()) + "&panel=Main&sakai_action=doDescription&sakai.state.reset=true");
				cobj.put("extraInfo", "false");
				jsonCalendar.add(cobj);
			}
		} catch(Exception e) {
			log.error("Error getting Calendar events for site {} : {}", siteId, e);
		}
		return jsonCalendar;
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

		if (c != null && !calendarEvents.isEmpty() && !calendarService.allowEditCalendar(c.getReference())) {
			errors.add(new DateManagerError("calendar", rb.getString("error.update.permission.denied"), "calendarEvents", toolTitle, 0));
		}
		for (int i = 0; i < calendarEvents.size(); i++) {
			JSONObject jsonEvent = (JSONObject)calendarEvents.get(i);
			String eventId = (String)jsonEvent.get("id");
			int idx = Integer.parseInt(jsonEvent.get("idx").toString());

			try {
				if (eventId == null) {
					errors.add(new DateManagerError("calendar", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.calendar.item.name")}), "calendarEvents", toolTitle, idx));
					continue;
				}

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonEvent.get("open_date")).toInstant();
				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonEvent.get("due_date")).toInstant();
				boolean errored = false;
				if (openDate == null) {
					errored = errors.add(new DateManagerError("open_date", rb.getString("error.open.date.not.found"), "calendarEvents", toolTitle, idx));
				}
				else if (dueDate == null) {
					errored = errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "calendarEvents", toolTitle, idx));
				}
				else if (dueDate.isBefore(openDate)) {
					errored = errors.add(new DateManagerError("open_date", rb.getString("error.open.date.before.end.date"), "calendarEvents", toolTitle, idx));
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

					updates.add(new DateManagerUpdate(calendarEvent, openDate, dueDate, null));
				}

			} catch (Exception ex) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "calendarEvents", toolTitle, idx));
				log.error("Cannot edit event {}", eventId);

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
			fobj.put("id", forum.getId());
			fobj.put("title", forum.getTitle());
			if(forum.getAvailabilityRestricted()) {
				fobj.put("due_date", formatToUserDateFormat(forum.getCloseDate()));
				fobj.put("open_date", formatToUserDateFormat(forum.getOpenDate()));
				fobj.put("restricted", true);
			} else {
				fobj.put("restricted", false);
				fobj.put("due_date", null);
				fobj.put("open_date", null);
			}
			fobj.put("extraInfo", rb.getString("itemtype.forum"));
			fobj.put("tool_title", toolTitle);
			fobj.put("url", url);
			for (Object o : forum.getTopicsSet()) {
				DiscussionTopic topic = (DiscussionTopic)o;
				JSONObject tobj = new JSONObject();
				tobj.put("id", topic.getId());
				tobj.put("title", topic.getTitle());
				if(topic.getAvailabilityRestricted()) {
					tobj.put("due_date", formatToUserDateFormat(topic.getCloseDate()));
					tobj.put("open_date", formatToUserDateFormat(topic.getOpenDate()));
					tobj.put("restricted", true);
				} else {
					tobj.put("restricted", false);
					tobj.put("due_date", null);
					tobj.put("open_date", null);
				}
				tobj.put("extraInfo", rb.getString("itemtype.topic"));
				tobj.put("tool_title", toolTitle);
				tobj.put("url", url);
				jsonForums.add(tobj);
			}
			jsonForums.add(fobj);
		}
		return jsonForums;
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
			int idx = Integer.parseInt(jsonForum.get("idx").toString());

			try {

				Long forumId = (Long)jsonForum.get("id");
				if (forumId == null) {
					errors.add(new DateManagerError("forum", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.forum.topic.item.name")}), "forums", toolTitle, idx));
					continue;
				}

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonForum.get("open_date")).toInstant();
				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonForum.get("due_date")).toInstant();
				boolean errored = false;
				if (openDate == null) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.not.found"), "forums", toolTitle, idx));
					errored = true;
				}
				if (dueDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "forums", toolTitle, idx));
					errored = true;
				}
				if (errored) {
					continue;
				}

				String entityType = (String)jsonForum.get("extraInfo");
				DateManagerUpdate update;
				if("forum".equals(entityType)) {
					BaseForum forum = forumManager.getForumById(true, forumId);
					if (forum == null) {
						errors.add(new DateManagerError("forum",rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.forums.item.name")}), "forums", toolTitle, idx));
						continue;
					}

					/*boolean canUpdate = contentHostingService.allowUpdateResource(resourceId);
					if (!canUpdate) {
						errors.add(new DateManagerError("forum", rb.getString("error.update.permission.denied"), "forums", toolTitle, idx));
					}*/
					update = new DateManagerUpdate(forum, openDate, dueDate, null);
				} else {
					Topic topic = forumManager.getTopicById(true, forumId);
					if (topic == null) {
						errors.add(new DateManagerError("forum", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.topics.item.name")}), "forums", toolTitle, idx));
						continue;
					}

					/*boolean canUpdate = contentHostingService.allowUpdateCollection(resourceId);
					if (!canUpdate) {
						errors.add(new DateManagerError("forum", rb.getString("error.update.permission.denied"), "forums", toolTitle, idx));
					}*/
					update = new DateManagerUpdate(topic, openDate, dueDate, null);
				}

				if (!update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.before.close.date"), "forums", toolTitle, idx));
					continue;
				}
				updates.add(update);

			} catch(Exception e) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "forums", toolTitle, idx));
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
					forum.setOpenDate(Date.from(update.openDate));
					forum.setCloseDate(Date.from(update.dueDate));
				}
				forumManager.saveDiscussionForum(forum);
			} else {
				DiscussionTopic topic = (DiscussionTopic) update.object;
				if(topic.getAvailabilityRestricted()) {
					topic.setOpenDate(Date.from(update.openDate));
					topic.setCloseDate(Date.from(update.dueDate));
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
				aobj.put("id", announcement.getId());
				AnnouncementMessageHeader header = announcement.getAnnouncementHeader();
				aobj.put("title", header.getSubject());
				if(announcement.getProperties().getProperty(AnnouncementService.RETRACT_DATE) != null) {
					aobj.put("due_date", formatToUserDateFormat(Date.from(announcement.getProperties().getInstantProperty(AnnouncementService.RETRACT_DATE))));
				} else {
					aobj.put("due_date", null);
				}
				if(announcement.getProperties().getProperty(AnnouncementService.RELEASE_DATE) != null) {
					aobj.put("open_date", formatToUserDateFormat(Date.from(announcement.getProperties().getInstantProperty(AnnouncementService.RELEASE_DATE))));
				} else {
					aobj.put("open_date", null);
				}
				aobj.put("tool_title", toolTitle);
				aobj.put("url", url + "?itemReference=" + formattedText.escapeUrl(announcement.getReference()) + "&panel=Main&sakai_action=doShowmetadata&sakai.state.reset=true");
				aobj.put("extraInfo", "false");
				jsonAnnouncements.add(aobj);
			}
		} catch (Exception e) {
			log.error("getAnnouncementsForContext error for context {} : {}", siteId, e);
		}
		return jsonAnnouncements;
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
		/*boolean canUpdate = messageService.allowEditChanel(anncRef);
		if (!canUpdate) {
			errors.add(new DateManagerError("announcement", rb.getString("error.update.permission.denied"), "announcements", toolTitle, 0));
		}*/
		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_ANNOUNCEMENTS).getTitle();
		for (int i = 0; i < announcements.size(); i++) {
			JSONObject jsonAnnouncement = (JSONObject)announcements.get(i);
			int idx = Integer.parseInt(jsonAnnouncement.get("idx").toString());

			try {

				String announcementId = (String)jsonAnnouncement.get("id");
				if (announcementId == null) {
					errors.add(new DateManagerError("announcement", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.announcements.item.name")}), "announcements", toolTitle, idx));
					continue;
				}

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonAnnouncement.get("open_date")).toInstant();
				Instant dueDate = userTimeService.parseISODateInUserTimezone((String)jsonAnnouncement.get("due_date")).toInstant();
				boolean errored = false;
				if (openDate == null) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.not.found"), "announcements", toolTitle, idx));
					errored = true;
				}
				if (dueDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.due.date.not.found"), "announcements", toolTitle, idx));
					errored = true;
				}
				if (errored) {
					continue;
				}

				AnnouncementChannel aChannel = announcementService.getAnnouncementChannel(anncRef);
				AnnouncementMessageEdit announcement = aChannel.editAnnouncementMessage(announcementId);
				if (announcement == null) {
					errors.add(new DateManagerError("announcement", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.announcements.item.name")}), "announcements", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(announcement, openDate, dueDate, null);
				if (!update.openDate.isBefore(update.dueDate)) {
					errors.add(new DateManagerError("open_date", rb.getString("error.open.date.before.due.date"), "announcements", toolTitle, idx));
					continue;
				}
				updates.add(update);
			} catch(Exception e) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "announcements", toolTitle, idx));
				log.error("Error trying to validate Announcements {}", e);
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
	public void updateAnnouncements(DateManagerValidation announcementValidate) {
		String anncRef = announcementService.channelReference(getCurrentSiteId(), SiteService.MAIN_CONTAINER);
		try {
			AnnouncementChannel aChannel = announcementService.getAnnouncementChannel(anncRef);
			for (DateManagerUpdate update : (List<DateManagerUpdate>)(Object) announcementValidate.getUpdates()) {
				AnnouncementMessageEdit msg = (AnnouncementMessageEdit) update.object;
				msg.getPropertiesEdit().addProperty(AnnouncementService.RELEASE_DATE, timeService.newTime(Date.from(update.openDate).getTime()).toString());
				msg.getPropertiesEdit().addProperty(AnnouncementService.RETRACT_DATE, timeService.newTime(Date.from(update.dueDate).getTime()).toString());
				aChannel.commitMessage(msg, NotificationService.NOTI_IGNORE);
			}
		} catch (Exception e) {
			log.error("Announcement channel {} doesn't exist. {}", anncRef, e.getMessage());
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
		return jsonLessons;
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
						lobj.put("id", itemId);
						lobj.put("title", item.getName());
						SimplePage page = simplePageToolDao.getPage(itemId);
						if(page.getReleaseDate() != null) {
							lobj.put("open_date", formatToUserDateFormat(page.getReleaseDate()));
						} else {
							lobj.put("open_date", null);
						}
						lobj.put("tool_title", toolTitle);
						lobj.put("url", url);
						lobj.put("extraInfo", extraInfo);
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

		/*if(!securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, siteService.siteReference(siteId));
			errors.add(new DateManagerError("page", rb.getString("error.update.permission.denied"), "lessons", toolTitle, 0));
		}*/

		String toolTitle = toolManager.getTool(DateManagerConstants.COMMON_ID_LESSONS).getTitle();
		for (int i = 0; i < lessons.size(); i++) {
			JSONObject jsonItem = (JSONObject)lessons.get(i);
			int idx = Integer.parseInt(jsonItem.get("idx").toString());

			try {

				Long itemId = (Long)jsonItem.get("id");
				if (itemId == null) {
					errors.add(new DateManagerError("page", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.lessons.item.name")}), "lessons", toolTitle, idx));
					continue;
				}

				Instant openDate = userTimeService.parseISODateInUserTimezone((String)jsonItem.get("open_date")).toInstant();
				if (openDate == null) {
					errors.add(new DateManagerError("due_date", rb.getString("error.release.date.not.found"), "lessons", toolTitle, idx));
					continue;
				}

				SimplePage page = simplePageToolDao.getPage(itemId);
				if (page == null) {
					errors.add(new DateManagerError("page", rb.getFormattedMessage("error.item.not.found", new Object[]{rb.getString("tool.lessons.item.name")}), "lessons", toolTitle, idx));
					continue;
				}

				DateManagerUpdate update = new DateManagerUpdate(page, openDate, null, null);
				updates.add(update);
			} catch(Exception e) {
				errors.add(new DateManagerError("open_date", rb.getString("error.uncaught"), "lessons", toolTitle, idx));
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
			page.setReleaseDate(Date.from(update.openDate));
			log.debug("Saving changes on lessons : {}", simplePageToolDao.quickUpdate(page));
		}
	}
}
