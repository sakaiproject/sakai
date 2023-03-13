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

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
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
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class RepublishAssessmentListener implements ActionListener {

	private static final GradebookServiceHelper gbsHelper =
	    IntegrationContextFactory.getInstance().getGradebookServiceHelper();
	private static final boolean integrated =
	    IntegrationContextFactory.getInstance().isIntegrated();
	
	private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
	private static final ResourceLoader rl = new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
	  
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		boolean hasGradingData = assessmentBean.getHasGradingData();

		String publishedAssessmentId = assessmentBean.getAssessmentId();
		log.debug("publishedAssessmentId = " + publishedAssessmentId);
		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		
		// Go to database to get the newly updated data. The data inside beans might not be up to date.
		PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId);
		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REPUBLISH, "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));

		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		publishedAssessmentService.saveAssessment(assessment);

		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		AuthorizationBean authorization = (AuthorizationBean) ContextUtil.lookupBean("authorization");
		PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean)
				ContextUtil.lookupBean("publishedSettings");

		// If there are submissions, need to regrade them
		if (author.getIsRepublishAndRegrade() && hasGradingData) {
			publishedAssessmentService.regradePublishedAssessment(assessment, publishedAssessmentSettings.getupdateMostCurrentSubmission());
		}
		
		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_REPUBLISH, "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));
		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		publishedAssessmentService.saveAssessment(assessment);
		updateGB(assessment);
		
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

		author.setOutcome("author");
	}

	private void updateGB(PublishedAssessmentFacade assessment) {
		// a. if Gradebook does not exists, do nothing
		// b. if Gradebook exists, just call removeExternal first to clean up all data. And call addExternal to create
		// a new record. At the end, populate the scores by calling updateExternalAssessmentScores
		GradebookExternalAssessmentService g = null;
		if (integrated) {
			g = (GradebookExternalAssessmentService) SpringBeanLocator.getInstance().getBean(
					"org.sakaiproject.service.gradebook.GradebookExternalAssessmentService");
		}

		if (gbsHelper.gradebookExists(GradebookFacade.getGradebookUId(), g)) { 
			PublishedEvaluationModel evaluation = (PublishedEvaluationModel) assessment.getEvaluationModel();
			//Integer scoringType = EvaluationModelIfc.HIGHEST_SCORE;
			if (evaluation == null) {
				evaluation = new PublishedEvaluationModel();
				evaluation.setAssessmentBase(assessment.getData());
			}
			
			Integer scoringType = evaluation.getScoringType();
			if (evaluation.getToGradeBook() != null	&& evaluation.getToGradeBook().equals(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK.toString())) {

				String assessmentName = TextFormat.convertPlaintextToFormattedTextNoHighUnicode(assessment.getTitle().trim());
				try {
				    try {
						log.debug("before gbsHelper.updateGradebook()");
						gbsHelper.updateGradebook((PublishedAssessmentData) assessment.getData(), g);
                    } catch (Exception ex) {
                        log.warn("Gradebook item does not exist for assessment {}, creating a new gradebook item", assessment.getAssessmentId());
                        gbsHelper.addToGradebook((PublishedAssessmentData) assessment.getData(), null, g);
                    }
					
					// any score to copy over? get all the assessmentGradingData and copy over
					GradingService gradingService = new GradingService();
					// need to decide what to tell gradebook
					List list = null;

					if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)) {
						list = gradingService.getHighestSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
					} else {
						list = gradingService.getLastSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
					}
					
					log.debug("list size = {}", list.size());
					for (int i = 0; i < list.size(); i++) {
						try {
							AssessmentGradingData ag = (AssessmentGradingData) list.get(i);
							log.debug("ag.scores={}", ag.getTotalAutoScore());
							// Send the average score if average was selected for multiple submissions
							if (scoringType.equals(EvaluationModelIfc.AVERAGE_SCORE)) {
								// status = 5: there is no submission but grader update something in the score page
								if(ag.getStatus() ==5) {
									ag.setFinalScore(ag.getFinalScore());
								} else {
									Double averageScore = PersistenceService.getInstance().getAssessmentGradingFacadeQueries().
									getAverageSubmittedAssessmentGrading(Long.valueOf(assessment.getPublishedAssessmentId()), ag.getAgentId());
									ag.setFinalScore(averageScore);
								}	
							}
							gbsHelper.updateExternalAssessmentScore(ag, g);
						} catch (Exception e) {
							log.warn("Exception occues in " + i	+ "th record. Message:" + e.getMessage());
						}
					}
				} catch (Exception e2) {
					log.warn("Exception thrown in updateGB():" + e2.getMessage());
				}
			}
			else{ //remove
				try{
					gbsHelper.removeExternalAssessment(
							GradebookFacade.getGradebookUId(),
							assessment.getPublishedAssessmentId().toString(), g);
				}
				catch(Exception e){
					log.info("*** oh well, looks like there is nothing to remove:"+e.getMessage());
				}
			}
		}
	}
}
