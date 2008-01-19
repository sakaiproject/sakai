package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAccessControl;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class ConfirmRepublishAssessmentListener implements ActionListener {
	private static Log log = LogFactory.getLog(ConfirmRepublishAssessmentListener.class);
	
	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		SavePublishedSettingsListener savePublishedSettingsListener = new SavePublishedSettingsListener();
		PublishedAssessmentSettingsBean assessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
		//Long assessmentId = assessmentSettings.getAssessmentId();
		//PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(assessmentId.toString());
		PublishedAssessmentFacade assessment = assessmentSettings.getAssessment();
		PublishedAccessControl control = (PublishedAccessControl)assessment.getAssessmentAccessControl();
		if (control == null){
			control = new PublishedAccessControl();
		    // need to fix accessControl so it can take AssessmentFacade
			// later
		    control.setAssessmentBase(assessment.getData());
		}
	    String id = ae.getComponent().getId();
	    
		FacesContext context = FacesContext.getCurrentInstance();
		boolean error = savePublishedSettingsListener.setPublishedSettings(assessmentSettings, context, control, assessment);
		if (error){
		   	assessmentSettings.setOutcome("editPublishedAssessmentSettings");
		   	return;
		}

		boolean gbError = savePublishedSettingsListener.checkScore(assessmentSettings, assessment, context);
		if (gbError){
		   	assessmentSettings.setOutcome("editPublishedAssessmentSettings");
		   	return;
		}

		savePublishedSettingsListener.updateGB(assessmentSettings, assessment);

		assessmentService.saveAssessment(assessment);
		
		//These outcome are set for Cancel button in publishAssessment.jsp
		String actionCommand = ae.getComponent().getId();
		if ("republishRegrade".equals(actionCommand)) {
			log.debug("republishRegrade");
			author.setOutcome("editAssessment");
		}
		else if ("republish".equals(actionCommand)) {
			log.debug("republish");
			author.setOutcome("editAssessment");
		}
		else if ("publish".equals(actionCommand)) {
			log.debug("publish");
			author.setOutcome("editPublishedAssessmentSettings");
		}
		
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
