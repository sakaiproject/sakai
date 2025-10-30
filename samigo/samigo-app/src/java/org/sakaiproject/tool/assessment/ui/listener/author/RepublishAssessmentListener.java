/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.Instant;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentEntityProducer;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;

@Slf4j
public class RepublishAssessmentListener implements ActionListener {

		private final CalendarServiceHelper calendarService;
		private final TaskService taskService;
	    private static final ResourceLoader rl = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
	    private final SamigoAvailableNotificationService samigoAvailableNotificationService;
	    private final EventTrackingService eventTrackingService;

    public RepublishAssessmentListener() {
        // Prefer fetching services in the constructor to avoid initialization in a static-like context
        this.calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
        this.taskService = ComponentManager.get(TaskService.class);
        this.samigoAvailableNotificationService = ComponentManager.get(SamigoAvailableNotificationService.class);
        this.eventTrackingService = ComponentManager.get(EventTrackingService.class);
    }

	@Override
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		boolean hasGradingData = assessmentBean.getHasGradingData();

		String publishedAssessmentId = assessmentBean.getAssessmentId();
        log.debug("publishedAssessmentId = {}", publishedAssessmentId);
		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		
		// Go to database to get the newly updated data. The data inside beans might not be up to date.
		PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId);
		eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REPUBLISH, "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));

		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		publishedAssessmentService.saveAssessment(assessment);

		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
		// If there are submissions, need to regrade them
		if (author.getIsRepublishAndRegrade() && hasGradingData) {
			publishedAssessmentService.regradePublishedAssessment(assessment, publishedAssessmentSettings.getupdateMostCurrentSubmission());
		}
        // Determine notification preference and emit availability events accordingly
        PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
        boolean sendNotification = publishRepublishNotification.isSendNotification();
        emitAvailabilityEvents(assessment, publishedAssessmentSettings, sendNotification);
        // Keep gradebook update position unchanged
        publishedAssessmentService.updateGradebook((PublishedAssessmentData) assessment.getData());

		PublishAssessmentListener publishAssessmentListener = new PublishAssessmentListener();
		String subject = publishRepublishNotification.getNotificationSubject();
		String notificationMessage = publishAssessmentListener.getNotificationMessage(publishRepublishNotification, publishedAssessmentSettings.getTitle(), publishedAssessmentSettings.getReleaseTo(), 
				publishedAssessmentSettings.getStartDateInClientTimezoneString(), publishedAssessmentSettings.getPublishedUrl(), publishedAssessmentSettings.getDueDateInClientTimezoneString(),
				publishedAssessmentSettings.getTimedHours(), publishedAssessmentSettings.getTimedMinutes(), publishedAssessmentSettings.getUnlimitedSubmissions(),
				publishedAssessmentSettings.getSubmissionsAllowed(), publishedAssessmentSettings.getScoringType(), publishedAssessmentSettings.getFeedbackDelivery(),
				publishedAssessmentSettings.getFeedbackDateInClientTimezoneString(), publishedAssessmentSettings.getFeedbackEndDateString(), publishedAssessmentSettings.getFeedbackScoreThreshold(),
				publishedAssessmentSettings.getAutoSubmit(), publishedAssessmentSettings.getLateHandling(), publishedAssessmentSettings.getRetractDateString());
		
		GradingService gradingService = new GradingService();
		AssessmentService assessmentService = new AssessmentService();
		AuthorActionListener authorActionListener = new AuthorActionListener();
		authorActionListener.prepareAssessmentsList(author, authorization, assessmentService, gradingService, publishedAssessmentService);
		
		// Tell AuthorBean that we just published an assessment
		// This will allow us to jump directly to published assessments tab
		author.setJustPublishedAnAssessment(true);
		
		// Update Delivery Bean
		DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
		delivery.setPublishedAssessment(assessment);
		
		//update Calendar Events
		boolean addDueDateToCalendar = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("publishAssessmentForm:calendarDueDate2") != null;
		calendarService.updateAllCalendarEvents(assessment, publishedAssessmentSettings.getReleaseTo(), publishedAssessmentSettings.getGroupsAuthorized(), rl.getString("calendarDueDatePrefix") + " ", addDueDateToCalendar, notificationMessage);

		// Update task for the widget or create it
		String reference = AssessmentEntityProducer.REFERENCE_ROOT + "/" + AgentFacade.getCurrentSiteId() + "/" + assessment.getPublishedAssessmentId();
		Optional<Task> optTask = taskService.getTask(reference);
		if (optTask.isPresent()) {
			Task task = optTask.get();
			task.setDescription(assessment.getTitle());
			task.setDue((assessment.getDueDate() == null) ? null : assessment.getDueDate().toInstant());
			taskService.saveTask(task);
		} else {
			// Create a task in the students' dashboard
			Task task = new Task();
			task.setSiteId(AgentFacade.getCurrentSiteId());
			task.setReference(reference);
			task.setSystem(true);
			task.setDescription(assessment.getTitle());
			task.setDue((assessment.getDueDate() == null ? null : assessment.getDueDate().toInstant()));
			SelectItem[] usersMap = publishedAssessmentSettings.getUsersInSite();
			Set<String> users = new HashSet<>();
			if (usersMap != null) {
				for (SelectItem item : usersMap) {
					String userId = (String) item.getValue();
					if (StringUtils.isNotBlank(userId)) {
						users.add(userId);
					}
				}
			}
			taskService.createTask(task, users, Priorities.HIGH);
		}
		// Update scheduled assessment available notification according to instructor choice
		// Always clear any existing scheduled notifications to avoid duplicates or stale schedules,
		// then schedule new ones only if opted in.
		samigoAvailableNotificationService.removeScheduledAssessmentNotification(publishedAssessmentId);
		if (publishRepublishNotification.isSendNotification()) {
			samigoAvailableNotificationService.scheduleAssessmentAvailableNotification(publishedAssessmentId);
		}
		author.setOutcome("author");
	}

    // Posts immediate events and schedules future availability events as needed.
    // When sendNotification is false, events are created with NOTI_NONE to avoid user notifications.
    private void emitAvailabilityEvents(PublishedAssessmentFacade assessment, PublishedAssessmentSettingsBean publishedAssessmentSettings, boolean sendNotification) {

        List<ExtendedTime> extendedTimes = publishedAssessmentSettings.getExtendedTimes();
        Instant now = Instant.now();
        Instant baseStart = (assessment.getStartDate() != null) ? assessment.getStartDate().toInstant() : now;
        int notiMask = sendNotification ? NotificationService.NOTI_OPTIONAL : NotificationService.NOTI_NONE;
        if (baseStart.isBefore(now)) {
            eventTrackingService.post(eventTrackingService.newEvent(
                SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE,
                "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(),
                true,
                notiMask));
            if (publishedAssessmentSettings.getExtendedTimesSize() != 0) {
                for (ExtendedTime exTime : extendedTimes) {
                    Instant startInstant = (exTime.getStartDate() != null) ? exTime.getStartDate().toInstant() : null;
                    if (startInstant != null && startInstant.isAfter(now)) {
                        eventTrackingService.delay(eventTrackingService.newEvent(
                            SamigoConstants.EVENT_ASSESSMENT_AVAILABLE,
                            "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(),
                            true,
                            notiMask), startInstant);
                    }
                }
            }
        } else {
            eventTrackingService.delay(eventTrackingService.newEvent(
                SamigoConstants.EVENT_ASSESSMENT_AVAILABLE,
                "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(),
                true,
                notiMask), baseStart);
            if (publishedAssessmentSettings.getExtendedTimesSize() != 0) {
                for (ExtendedTime exTime : extendedTimes) {
                    Instant startInstant = (exTime.getStartDate() != null) ? exTime.getStartDate().toInstant() : null;
                    if (startInstant == null) {
                        continue;
                    }
                    if (startInstant.isBefore(now)) {
                        eventTrackingService.post(eventTrackingService.newEvent(
                            SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE,
                            "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(),
                            true,
                            notiMask));
                    } else if (startInstant.isAfter(now) && !baseStart.equals(startInstant)) {
                        eventTrackingService.delay(eventTrackingService.newEvent(
                            SamigoConstants.EVENT_ASSESSMENT_AVAILABLE,
                            "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(),
                            true,
                            notiMask), startInstant);
                    }
                }
            }
        }
    }

}
