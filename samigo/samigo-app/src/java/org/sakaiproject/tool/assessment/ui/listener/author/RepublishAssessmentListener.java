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

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.samigo.api.SamigoAvailableNotificationService;
import org.sakaiproject.samigo.api.SamigoReferenceReckoner;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
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
import org.sakaiproject.tool.assessment.util.TextFormat;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.time.api.Time;
import java.util.ListIterator;
import java.time.Instant;
import org.sakaiproject.component.cover.ComponentManager;

@Slf4j
public class RepublishAssessmentListener implements ActionListener {

	private static final GradebookServiceHelper gbsHelper =
	    IntegrationContextFactory.getInstance().getGradebookServiceHelper();
	private static final boolean integrated =
	    IntegrationContextFactory.getInstance().isIntegrated();
	
	private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
	private TaskService taskService = ComponentManager.get(TaskService.class);;
	private static final ResourceLoader rl = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
	private final SamigoAvailableNotificationService samigoAvailableNotificationService = ComponentManager.get(SamigoAvailableNotificationService.class);
	private EventTrackingService eventTrackingService;
	public RepublishAssessmentListener() {
		eventTrackingService = ComponentManager.get(EventTrackingService.class);
	}
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		boolean hasGradingData = assessmentBean.getHasGradingData();

		String publishedAssessmentId = assessmentBean.getAssessmentId();
		log.debug("publishedAssessmentId = " + publishedAssessmentId);
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
		postUserNotification(assessment, publishedAssessmentSettings);
		eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REPUBLISH, "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));
		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		publishedAssessmentService.saveAssessment(assessment);
		publishedAssessmentService.updateGradebook((PublishedAssessmentData) assessment.getData());
		PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");

		PublishAssessmentListener publishAssessmentListener = new PublishAssessmentListener();
		String subject = publishRepublishNotification.getNotificationSubject();
		String notificationMessage = publishAssessmentListener.getNotificationMessage(publishRepublishNotification, publishedAssessmentSettings.getTitle(), publishedAssessmentSettings.getReleaseTo(), 
				publishedAssessmentSettings.getStartDateInClientTimezoneString(), publishedAssessmentSettings.getPublishedUrl(), publishedAssessmentSettings.getDueDateInClientTimezoneString(),
				publishedAssessmentSettings.getTimedHours(), publishedAssessmentSettings.getTimedMinutes(), publishedAssessmentSettings.getUnlimitedSubmissions(),
				publishedAssessmentSettings.getSubmissionsAllowed(), publishedAssessmentSettings.getScoringType(), publishedAssessmentSettings.getFeedbackDelivery(),
				publishedAssessmentSettings.getFeedbackDateInClientTimezoneString(), publishedAssessmentSettings.getFeedbackEndDateString(), publishedAssessmentSettings.getFeedbackScoreThreshold(),
				publishedAssessmentSettings.getAutoSubmit(), publishedAssessmentSettings.getLateHandling(), publishedAssessmentSettings.getRetractDateString());
		if (publishRepublishNotification.getSendNotification()) {
		    publishAssessmentListener.sendNotification(assessment, publishedAssessmentService, subject, notificationMessage, publishedAssessmentSettings.getReleaseTo());
		}
		
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
			for(SelectItem item : usersMap) {
                            String userId = (String)item.getValue(); 
                            if (StringUtils.isNotBlank(userId)) {
				users.add(userId);
                            }
			}
			taskService.createTask(task, users, Priorities.HIGH);
		}
		// Update scheduled assessment available notification
		samigoAvailableNotificationService.scheduleAssessmentAvailableNotification(publishedAssessmentId);
		author.setOutcome("author");
	}

	private void postUserNotification(PublishedAssessmentFacade assessment, PublishedAssessmentSettingsBean publishedAssessmentSettings) {

		List<ExtendedTime> extendedTimes = publishedAssessmentSettings.getExtendedTimes();
		Instant instant = assessment.getStartDate().toInstant();
		if (instant.isBefore(Instant.now())) {
			eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true));
			if (publishedAssessmentSettings.getExtendedTimesSize() != 0) {
				ListIterator<ExtendedTime> it = extendedTimes.listIterator();
				while (it.hasNext()) {
					ExtendedTime exTime = (ExtendedTime) it.next();
					Instant startInstant = exTime.getStartDate().toInstant();
					if (startInstant.isAfter(Instant.now())) {
						eventTrackingService.delay(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true), startInstant);
					}
				}
			}
		} else {
			eventTrackingService.delay(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true), instant);
			if (publishedAssessmentSettings.getExtendedTimesSize() != 0) {
				ListIterator<ExtendedTime> it = extendedTimes.listIterator();
				while (it.hasNext()) {
					ExtendedTime exTime = (ExtendedTime) it.next();
					Instant startInstant = exTime.getStartDate().toInstant();
					if (startInstant.isBefore(Instant.now())) {
						eventTrackingService.post(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true));
					} else if (startInstant.isAfter(Instant.now()) && !instant.equals(startInstant)) {
						eventTrackingService.delay(eventTrackingService.newEvent(SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, "siteId=" + AgentFacade.getCurrentSiteId() + ", assessmentId=" + assessment.getAssessmentId() + ", publishedAssessmentId=" + assessment.getPublishedAssessmentId(), true), startInstant);
					}
				}
			}
		}
	}

}
