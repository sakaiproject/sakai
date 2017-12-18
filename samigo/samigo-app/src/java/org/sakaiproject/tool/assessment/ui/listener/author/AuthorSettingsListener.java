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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentMetaDataIfc;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentSettingsBean;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.authz.AuthorizationBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.util.FormattedText;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * @author Ed Smiley
 * @version $Id$
 */
@Slf4j
public class AuthorSettingsListener implements ActionListener
{

  public AuthorSettingsListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    AssessmentSettingsBean assessmentSettings = (AssessmentSettingsBean) ContextUtil.lookupBean("assessmentSettings");
    // #1a - load the assessment
    String assessmentId = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("assessmentId");
    if (assessmentId == null){
      assessmentId = assessmentSettings.getAssessmentId().toString();
    }

    AssessmentService assessmentService = new AssessmentService();
    AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);

    //#1b - permission checking before proceeding - daisyf
    AuthorBean author = (AuthorBean) ContextUtil.lookupBean("author");
    author.setOutcome("editAssessmentSettings");
    
    AuthorizationBean authzBean = (AuthorizationBean) ContextUtil.lookupBean("authorization");
    if (!authzBean.isUserAllowedToEditAssessment(assessmentId, assessment.getCreatedBy(), false)) {
      String err=(String)ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "denied_edit_assessment_error");
      context.addMessage(null,new FacesMessage(err));
      author.setOutcome("author");
      return;
    }

    // passed authz checks, move on
    author.setIsEditPendingAssessmentFlow(true);

    assessmentSettings.setAssessment(assessment);
    assessmentSettings.setAssessmentId(assessment.getAssessmentId());
    assessmentSettings.setAttachmentList(((AssessmentIfc)assessment.getData()).getAssessmentAttachmentList());
    assessmentSettings.setDisplayFormat(ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.GeneralMessages","output_data_picker_w_sec"));
    assessmentSettings.resetIsValidDate();
    assessmentSettings.resetOriginalDateString();
    assessmentSettings.setNoGroupSelectedError(false);
    assessmentSettings.setBlockDivs("");
    
    // To convertFormattedTextToPlaintext for the fields that have been through convertPlaintextToFormattedTextNoHighUnicode
    assessmentSettings.setTitle(FormattedText.convertFormattedTextToPlaintext(assessment.getTitle()));
    assessmentSettings.setAuthors(FormattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.AUTHORS)));
    assessmentSettings.setFinalPageUrl(FormattedText.convertFormattedTextToPlaintext(assessment.getAssessmentAccessControl().getFinalPageUrl()));
    assessmentSettings.setBgColor(FormattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.BGCOLOR)));
    assessmentSettings.setBgImage(FormattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.BGIMAGE)));
    assessmentSettings.setKeywords(FormattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.KEYWORDS)));
    assessmentSettings.setObjectives(FormattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.OBJECTIVES)));
    assessmentSettings.setRubrics(FormattedText.convertFormattedTextToPlaintext(assessment.getAssessmentMetaDataByLabel(AssessmentMetaDataIfc.RUBRICS)));
    assessmentSettings.setPassword(FormattedText.convertFormattedTextToPlaintext(StringUtils.trim(assessment.getAssessmentAccessControl().getPassword())));
    
    // else throw error

    // #1c - get question size
    int questionSize = assessmentService.getQuestionSize(assessmentId);
    if (questionSize > 0)
      assessmentSettings.setHasQuestions(true);
    else
      assessmentSettings.setHasQuestions(false);

    if (ae == null) {
    	author.setFromPage("author");
    }
    else {
    	author.setFromPage("editAssessment");
    }
  }


}
