package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.lang.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.Assignment;
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
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class RepublishAssessmentListener implements ActionListener {

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
			regradeRepublishedAssessment(publishedAssessmentService, assessment);
		}
		
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubassessment.republish", "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + publishedAssessmentId, true));
		assessment.setStatus(AssessmentBaseIfc.ACTIVE_STATUS);
		updateGB(assessment);
		publishedAssessmentService.saveAssessment(assessment);
		
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
	
	private void regradeRepublishedAssessment (PublishedAssessmentService pubService, PublishedAssessmentFacade publishedAssessment) {
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
			if (evaluation == null) {
				evaluation = new PublishedEvaluationModel();
				evaluation.setAssessmentBase(assessment.getData());
			}
			GradebookService gService = (GradebookService) ComponentManager.get("org.sakaiproject.service.gradebook.GradebookService");
			Integer scoringType = evaluation.getScoringType();
			try {
				String toDefaultGradebook = String.valueOf(EvaluationModelIfc.TO_DEFAULT_GRADEBOOK);
				if (evaluation.getToGradeBook() != null){
					if (StringUtils.equals(evaluation.getToGradeBook(), toDefaultGradebook)
							|| StringUtils.equals(evaluation.getToGradeBook(), String.valueOf(EvaluationModelIfc.TO_EXISTING_GRADEBOOK_ITEM))) {
						GradingService gradingService = new GradingService();
						gradingService.linkAssessmentWithGradebook(scoringType, assessment);
						evaluation.setToGradeBook(toDefaultGradebook);
					}
					// remove
					else if (StringUtils.equals(evaluation.getToGradeBook(),String.valueOf(EvaluationModelIfc.NOT_TO_GRADEBOOK))) {
						gbsHelper.removeExternalAssessment(
								GradebookFacade.getGradebookUId(),
								assessment.getPublishedAssessmentId().toString(), g);
					}
				}
			} catch (Exception e) {
				log.warn("Exception thrown while removing item in updateGB():" + e.getMessage());

			}
		}
	}
}
