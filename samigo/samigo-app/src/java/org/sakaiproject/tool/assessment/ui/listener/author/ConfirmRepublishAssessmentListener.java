package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ConfirmRepublishAssessmentListener implements ActionListener {
	// To Do: I think this can be combined with SavePublishedSettingsListener.
	
	private static Log log = LogFactory.getLog(ConfirmRepublishAssessmentListener.class);
	
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		SavePublishedSettingsListener savePublishedSettingsListener = new SavePublishedSettingsListener();
		PublishedAssessmentSettingsBean assessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
		Long assessmentId = assessmentSettings.getAssessmentId();
		PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(assessmentId.toString());
		PublishedAccessControl control = (PublishedAccessControl)assessment.getAssessmentAccessControl();
		if (control == null){
			control = new PublishedAccessControl();
		    // need to fix accessControl so it can take AssessmentFacade
			// later
		    control.setAssessmentBase(assessment.getData());
		}
	    
		EventTrackingService.post(EventTrackingService.newEvent("sam.pubsetting.edit", "publishedAssessmentId=" + assessmentId, true));
		FacesContext context = FacesContext.getCurrentInstance();
		boolean error = savePublishedSettingsListener.setPublishedSettings(assessmentSettings, context, control, assessment, false);
		if (error){
		   	assessmentSettings.setOutcome("editPublishedAssessmentSettings");
		   	return;
		}

		boolean gbError = savePublishedSettingsListener.checkScore(assessmentSettings, assessment, context);
		if (gbError){
		   	assessmentSettings.setOutcome("editPublishedAssessmentSettings");
		   	return;
		}

		//savePublishedSettingsListener.updateGB(assessmentSettings, assessment);

		assessmentService.saveAssessment(assessment);
		
		//These outcome are set for Cancel button in publishAssessment.jsp
		String actionCommand = ae.getComponent().getId();
		if (actionCommand.startsWith("republishRegrade")) {
			log.debug("republishRegrade");
			author.setOutcome("editAssessment");
			author.setIsRepublishAndRegrade(true);
		}
		else if (actionCommand.startsWith("republish")) {
			log.debug("republish");
			author.setOutcome("editAssessment");
			author.setIsRepublishAndRegrade(false);
		}
		/* This is commented out for 1534
		else if (actionCommand.startsWith("publish")) {
			log.debug("publish");
			author.setOutcome("editPublishedAssessmentSettings");
		}
		*/
		
		PublishedAssessmentSettingsBean publishedAssessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil
			.lookupBean("publishedSettings");
		Long publishedAssessmentId = publishedAssessmentSettings.getAssessmentId();
		GradingService gradingService = new GradingService();

		ArrayList al = gradingService.getHasGradingDataAndHasSubmission(publishedAssessmentId);
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

		if (al.size() == 2) {
			assessmentBean.setHasGradingData(((Boolean)al.get(0)).booleanValue());
			assessmentBean.setHasSubmission(((Boolean)al.get(1)).booleanValue());				
		}
		else {
			assessmentBean.setHasGradingData(false);
			assessmentBean.setHasSubmission(false);
		}
		publishedAssessmentSettings.setUpdateMostCurrentSubmission(false);
	}
}
