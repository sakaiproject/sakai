package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.spring.SpringBeanLocator;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.exception.SamigoDataAccessException;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.GradebookFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;
import org.sakaiproject.tool.assessment.integration.helper.ifc.CalendarServiceHelper;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.tool.assessment.services.FinFormatException;
import org.sakaiproject.tool.assessment.services.GradebookServiceException;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;

public class RepublishAssessmentListener implements ActionListener {

	private static Log log = LogFactory
			.getLog(RepublishAssessmentListener.class);
	private static final GradebookServiceHelper gbsHelper =
	    IntegrationContextFactory.getInstance().getGradebookServiceHelper();
	private static final boolean integrated =
	    IntegrationContextFactory.getInstance().isIntegrated();
	
	private CalendarServiceHelper calendarService = IntegrationContextFactory.getInstance().getCalendarServiceHelper();
	private ResourceLoader rl= new ResourceLoader("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages");
	  
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil
				.lookupBean("assessmentBean");
		boolean hasGradingData = assessmentBean.getHasGradingData();

		String publishedAssessmentId = assessmentBean.getAssessmentId();
		log.debug("publishedAssessmentId = " + publishedAssessmentId);
		PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
		
		// Go to database to get the newly updated data. The data inside beans might not be up to date.
		PublishedAssessmentFacade assessment = publishedAssessmentService.getPublishedAssessment(publishedAssessmentId);
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.republish", "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));

		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		publishedAssessmentService.saveAssessment(assessment);

		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		// If there are submissions, need to regrade them
		if (author.getIsRepublishAndRegrade() && hasGradingData) {
			try {
			regradeRepublishedAssessment(publishedAssessmentService, assessment);
			} catch (SamigoDataAccessException e) {
				e.printStackTrace();
			}
		}
		
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.republish", "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));
		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		publishedAssessmentService.saveAssessment(assessment);
		updateGB(assessment);
		
		PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
		
		PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
		PublishAssessmentListener publishAssessmentListener = new PublishAssessmentListener();
		String subject = publishRepublishNotification.getNotificationSubject();
		String notificationMessage = publishAssessmentListener.getNotificationMessage(publishRepublishNotification, publishedAssessmentSettings.getTitle(), publishedAssessmentSettings.getReleaseTo(), publishedAssessmentSettings.getStartDateString(), publishedAssessmentSettings.getPublishedUrl(),
				publishedAssessmentSettings.getReleaseToGroupsAsString(), publishedAssessmentSettings.getDueDateString(), publishedAssessmentSettings.getTimedHours(), publishedAssessmentSettings.getTimedMinutes(), 
				publishedAssessmentSettings.getUnlimitedSubmissions(), publishedAssessmentSettings.getSubmissionsAllowed(), publishedAssessmentSettings.getScoringType(), publishedAssessmentSettings.getFeedbackDelivery(), publishedAssessmentSettings.getFeedbackDateString());
		if (publishRepublishNotification.getSendNotification()) {
		    publishAssessmentListener.sendNotification(assessment, publishedAssessmentService, subject, notificationMessage, publishedAssessmentSettings.getReleaseTo());
		}
		
		GradingService gradingService = new GradingService();
		AssessmentService assessmentService = new AssessmentService();
		AuthorActionListener authorActionListener = new AuthorActionListener();
		authorActionListener.prepareAssessmentsList(author, assessmentService, gradingService, publishedAssessmentService);
		
		// Tell AuthorBean that we just published an assessment
		// This will allow us to jump directly to published assessments tab
		author.setJustPublishedAnAssessment(true);
		
		//update Calendar Events
       boolean addDueDateToCalendar = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("publishAssessmentForm:calendarDueDate2") != null;
       calendarService.updateAllCalendarEvents(assessment, publishedAssessmentSettings.getReleaseTo(), publishedAssessmentSettings.getGroupsAuthorized(), rl.getString("calendarDueDatePrefix") + " ", addDueDateToCalendar, notificationMessage);

		author.setOutcome("author");
	}
	
	private void regradeRepublishedAssessment (PublishedAssessmentService pubService, PublishedAssessmentFacade publishedAssessment) throws GradebookServiceException, FinFormatException, SamigoDataAccessException {
		HashMap publishedItemHash = pubService.preparePublishedItemHash(publishedAssessment);
		HashMap publishedItemTextHash = pubService.preparePublishedItemTextHash(publishedAssessment);
		HashMap publishedAnswerHash = pubService.preparePublishedAnswerHash(publishedAssessment);
		PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil
			.lookupBean("publishedSettings");
		// Actually we don't really need to consider linear or random here.
		// boolean randomAccessAssessment = publishedAssessmentSettings.getItemNavigation().equals("2");
		boolean updateMostCurrentSubmission = publishedAssessmentSettings.getupdateMostCurrentSubmission();
		GradingService service = new GradingService();
		List list = service.getAllAssessmentGradingData(publishedAssessment.getPublishedAssessmentId());
		Iterator iter = list.iterator();
		if (updateMostCurrentSubmission) {
			publishedAssessment.setLastNeedResubmitDate(new Date());
		    String currentAgent = "";
			while (iter.hasNext()) {
				AssessmentGradingData adata = (AssessmentGradingData) iter.next();
				if (!currentAgent.equals(adata.getAgentId())){
					if (adata.getForGrade().booleanValue()) {
						adata.setForGrade(Boolean.FALSE);
						adata.setStatus(AssessmentGradingData.ASSESSMENT_UPDATED_NEED_RESUBMIT);
					}
					else {
						adata.setStatus(AssessmentGradingData.ASSESSMENT_UPDATED);
					}
					currentAgent = adata.getAgentId();
				}
				service.storeGrades(adata, true, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash, true);
			}
		}
		else {
			while (iter.hasNext()) {
				AssessmentGradingData adata = (AssessmentGradingData) iter.next();
				service.storeGrades(adata, true, publishedAssessment, publishedItemHash, publishedItemTextHash, publishedAnswerHash, true);
			}
		}
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

				try {
					log.debug("before gbsHelper.removeGradebook()");
					gbsHelper.removeExternalAssessment(GradebookFacade.getGradebookUId(), assessment.getPublishedAssessmentId().toString(), g);
				} catch (Exception e1) {
					// Should be the external assessment doesn't exist in GB. So we quiet swallow the exception. Please check the log for the actual error.
					log.info("Exception thrown in updateGB():" + e1.getMessage());
				}
				
				try {
					log.debug("before gbsHelper.addToGradebook()");
					gbsHelper.addToGradebook((PublishedAssessmentData) assessment.getData(), g);
					
					// any score to copy over? get all the assessmentGradingData and copy over
					GradingService gradingService = new GradingService();
					// need to decide what to tell gradebook
					List list = null;

					if ((scoringType).equals(EvaluationModelIfc.HIGHEST_SCORE)) {
						list = gradingService.getHighestSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
					} else {
						list = gradingService.getLastSubmittedOrGradedAssessmentGradingList(assessment.getPublishedAssessmentId());
					}
					
					log.debug("list size =" + list.size());
					for (int i = 0; i < list.size(); i++) {
						try {
							AssessmentGradingData ag = (AssessmentGradingData) list.get(i);
							log.debug("ag.scores " + ag.getTotalAutoScore());
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
