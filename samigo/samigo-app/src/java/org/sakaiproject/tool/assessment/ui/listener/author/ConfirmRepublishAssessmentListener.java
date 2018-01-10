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

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishRepublishNotificationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

@Slf4j
public class ConfirmRepublishAssessmentListener implements ActionListener {
	// To Do: I think this can be combined with SavePublishedSettingsListener.

	public void processAction(ActionEvent ae) throws AbortProcessingException {
		AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
		// in case we're in published assessment pool edit view, disable that mode
	 	if (author.getIsEditPoolFlow()) {
	 	    author.setIsEditPoolFlow(false);
	 	}
		PublishedAssessmentService assessmentService = new PublishedAssessmentService();
		SavePublishedSettingsListener savePublishedSettingsListener = new SavePublishedSettingsListener();
		PublishedAssessmentSettingsBean assessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
		assessmentSettings.setTitle(FormattedText.convertFormattedTextToPlaintext(assessmentSettings.getTitle()));
		Long assessmentId = assessmentSettings.getAssessmentId();
		PublishedAssessmentFacade assessment = assessmentService.getPublishedAssessment(assessmentId.toString());
		/*
		PublishedAccessControl control = (PublishedAccessControl)assessment.getAssessmentAccessControl();
		if (control == null){
			control = new PublishedAccessControl();
		    // need to fix accessControl so it can take AssessmentFacade
			// later
		    control.setAssessmentBase(assessment.getData());
		}
	    */
		EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_PUBLISHED_ASSESSMENT_SETTING_EDIT, "siteId=" + AgentFacade.getCurrentSiteId() + ", publishedAssessmentId=" + assessmentId, true));
		FacesContext context = FacesContext.getCurrentInstance();
		//boolean error = savePublishedSettingsListener.setPublishedSettings(assessmentService, assessmentSettings, context, control, assessment, false);
		/*
		if (error){
		   	assessmentSettings.setOutcome("editPublishedAssessmentSettings");
		   	return;
		}
		*/
		boolean gbError = savePublishedSettingsListener.checkScore(assessmentSettings, assessment, context);
		if (gbError){
		   	author.setOutcome("editAssessment");
		   	return;
		}

		//savePublishedSettingsListener.updateGB(assessmentSettings, assessment);

		//assessmentService.saveAssessment(assessment);
		
		//These outcome are set for Cancel button in publishAssessment.jsp
		String actionCommand = ae.getComponent().getId();
		if (actionCommand.startsWith("republishRegrade")) {
			log.debug("republishRegrade");
			//author.setOutcome("editAssessment");
			author.setIsRepublishAndRegrade(true);
		}
		else if (actionCommand.startsWith("republish")) {
			log.debug("republish");
			//author.setOutcome("editAssessment");
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

		List<Boolean> al = gradingService.getHasGradingDataAndHasSubmission(publishedAssessmentId);
		AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");

		if (al.size() == 2) {
			assessmentBean.setHasGradingData(((Boolean)al.get(0)).booleanValue());
			assessmentBean.setHasSubmission(((Boolean)al.get(1)).booleanValue());				
		}
		else {
			assessmentBean.setHasGradingData(false);
			assessmentBean.setHasSubmission(false);
		}
		
		publishedAssessmentSettings.setReleaseToGroupsAsString(assessment.getReleaseToGroups());
		publishedAssessmentSettings.setUpdateMostCurrentSubmission(false);
		PublishRepublishNotificationBean publishRepublishNotification = (PublishRepublishNotificationBean) ContextUtil.lookupBean("publishRepublishNotification");
		publishRepublishNotification.setSendNotification(false);
		if (author.getIsRepublishAndRegrade()) {
			publishRepublishNotification.setPrePopulateText(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","pre_populate_text_regrade_republish"));
		}
		else {
			publishRepublishNotification.setPrePopulateText(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AssessmentSettingsMessages","pre_populate_text_republish"));
		}
		author.setOutcome("saveSettingsAndConfirmPublish");
	}
}
