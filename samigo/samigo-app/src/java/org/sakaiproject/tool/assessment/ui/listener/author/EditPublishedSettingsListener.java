/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.PublishedAssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class EditPublishedSettingsListener
    implements ActionListener
{

  public EditPublishedSettingsListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    PublishedAssessmentSettingsBean assessmentSettings = (PublishedAssessmentSettingsBean) ContextUtil.lookupBean("publishedSettings");
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    
    // #1a - load the assessment
    String assessmentId;
    boolean fromRetakeConfirm = "retakeConfirm".equals(author.getOutcome());
    if (fromRetakeConfirm) {
        assessmentId = (String) context.getExternalContext().getRequestParameterMap().get("retractAssessmentForm:publishedId");
    } else {
        assessmentId = (String) context.getExternalContext().getRequestParameterMap().get("publishedId");
    }
    PublishedAssessmentService assessmentService = new PublishedAssessmentService();
    PublishedAssessmentFacade assessment = assessmentService.getSettingsOfPublishedAssessment(assessmentId);

    //## - permission checking before proceeding - daisyf
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (!authzBean.isUserAllowedToPublishAssessment(assessmentId, assessment.getCreatedBy(), true)) {
        String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_publish_assessment_settings_error");
        context.addMessage(null,new FacesMessage(err));
        author.setOutcome("author");
        return;
    }
    else if (!authzBean.isUserAllowedToEditAssessment(assessmentId, assessment.getCreatedBy(), true)) {
          String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
          context.addMessage(null,new FacesMessage(err));
          author.setOutcome("author");
          return;
    }

    author.setOutcome("editPublishedAssessmentSettings");
    author.setIsEditPendingAssessmentFlow(false);
    assessmentSettings.setAssessment(assessment);
    assessmentSettings.setAttachmentList(((AssessmentIfc)assessment.getData()).getAssessmentAttachmentList());

    // To convertFormattedTextToPlaintext for the fields that have been through convertPlaintextToFormattedTextNoHighUnicode
    FormattedText formattedText = ComponentManager.get(FormattedText.class);
    assessmentSettings.setTitle(formattedText.convertFormattedTextToPlaintext(assessment.getTitle()));
    assessmentSettings.setAuthors(formattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.AUTHORS)));
    assessmentSettings.setFinalPageUrl(formattedText.convertFormattedTextToPlaintext(assessment.getAssessmentAccessControl().getFinalPageUrl()));
    assessmentSettings.setBgColor(formattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.BGCOLOR)));
    assessmentSettings.setBgImage(formattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.BGIMAGE)));
    assessmentSettings.setKeywords(formattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.KEYWORDS)));
    assessmentSettings.setObjectives(formattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.OBJECTIVES)));
    assessmentSettings.setRubrics(formattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.RUBRICS)));
    assessmentSettings.setPassword(formattedText.convertFormattedTextToPlaintext(StringUtils.trim(assessment.getAssessmentAccessControl().getPassword())));
    assessmentSettings.setBlockDivs("");

    AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
    assessmentBean.setAssessmentId(assessmentId);
    
    if (ae == null || fromRetakeConfirm) {
    	// From authorIndex
    	author.setFromPage("author");
    }
    else {
    	String actionCommand = ae.getComponent().getId();
    	if ("editPublishedAssessmentSettings_editAssessment".equals(actionCommand)) {
    		log.debug("editPublishedAssessmentSettings_editAssessment");
    		author.setFromPage("editAssessment");
    	}
    	else if ("editPublishedAssessmentSettings_saveSettingsAndConfirmPublish".equals(actionCommand)) {
    		log.debug("editPublishedAssessmentSettings_saveSettingsAndConfirmPublish");
    		author.setFromPage("saveSettingsAndConfirmPublish");
    	}
    }
	
	boolean isRetractedForEdit = isRetractedForEdit(assessment);
	log.debug("isRetractedForEdit = " + isRetractedForEdit);
	author.setIsRetractedForEdit(isRetractedForEdit);
	
	String editPubAnonyGradingRestricted = ServerConfigurationService.getString("samigo.editPubAnonyGrading.restricted");
    if (editPubAnonyGradingRestricted != null && editPubAnonyGradingRestricted.equals("true")) {
    	assessmentSettings.setEditPubAnonyGradingRestricted(true);
    }
    else {
    	assessmentSettings.setEditPubAnonyGradingRestricted(false);
    }
  }

  
  private boolean isRetractedForEdit(PublishedAssessmentFacade publishedAssessmentFacade) {
	if (AssessmentBaseIfc.RETRACT_FOR_EDIT_STATUS.equals(publishedAssessmentFacade.getStatus())) {
		return true;
	}
	return false;
  }
}
