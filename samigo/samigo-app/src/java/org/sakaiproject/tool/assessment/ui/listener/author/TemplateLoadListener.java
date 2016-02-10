/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.data.dao.shared.TypeD;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.ui.bean.shared.BackingBean;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * <p>Description: Action Listener for loading a template</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class TemplateLoadListener
    extends TemplateBaseListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(TemplateLoadListener.class);
  static private ContextUtil cu;
  /**
   * Normal listener method.
   * @param ae
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();

    TemplateBean templateBean = lookupTemplateBean(context);
    //log.info("id=" + cu.lookupParam("templateId"));
    String templateId = cu.lookupParam("templateId");

    AssessmentService assessmentService = new AssessmentService();
    AssessmentTemplateFacade template = assessmentService.getAssessmentTemplate(templateId);
    String author = template.getCreatedBy();
    if (!(author != null && author.equals(UserDirectoryService.getCurrentUser().getId())) && !template.getTypeId().equals(TypeIfc.TEMPLATE_SYSTEM_DEFINED)) {
        log.error("User attempted to load template owned by another author " + author + " " + UserDirectoryService.getCurrentUser().getId());
        throw new AbortProcessingException("Attempted to load template owned by another author " + author + " " + UserDirectoryService.getCurrentUser().getId());
     }

    loadAssessment(templateBean, templateId);
  }

  /**
   * Update an existing assessment.
   * @param templateBean
   * @param template
   * @param props
   * @return
   * @throws java.lang.Exception
   */
  /**
   * @param templateBean
   * @param templateId template id or "0" if create new
   * @return true on success
   * @throws java.lang.Exception
   */
  public boolean loadAssessment(TemplateBean templateBean, String templateId)
  {
    try
    {
      AssessmentService delegate = new AssessmentService();
      AssessmentTemplateFacade template = delegate.getAssessmentTemplate
        (templateId);

      templateBean.setIdString(templateId);
      templateBean.setTemplateName(FormattedText.convertFormattedTextToPlaintext(template.getTitle()));
      templateBean.setTemplateAuthor(FormattedText.convertFormattedTextToPlaintext((String) template.getAssessmentMetaDataMap(template.getAssessmentMetaDataSet()).get("author")));
      templateBean.setTemplateDescription(template.getDescription());

      // Assessment Access Control
      AssessmentAccessControl aac = (AssessmentAccessControl)
        template.getAssessmentAccessControl();
      if (aac != null)
      {
        if (aac.getItemNavigation() != null)
          templateBean.setItemAccessType(aac.getItemNavigation().toString());
        if (aac.getAssessmentFormat() != null)
          templateBean.setDisplayChunking(aac.getAssessmentFormat().toString());
        if (aac.getItemNumbering() != null)
          templateBean.setQuestionNumbering(aac.getItemNumbering().toString());
        if (aac.getDisplayScoreDuringAssessments() != null)
            templateBean.setDisplayScoreDuringAssessments(aac.getDisplayScoreDuringAssessments().toString());
        
        if (aac.getMarkForReview() != null) {
        	if (aac.getMarkForReview().equals(Integer.valueOf(1))) {
        		templateBean.setMarkForReview(Boolean.TRUE);
        	}
        	else {
        		templateBean.setMarkForReview(Boolean.FALSE);
        	}
        }
        else
        	templateBean.setMarkForReview(Boolean.FALSE);
        if (aac.getSubmissionsSaved() != null)
          templateBean.setSubmissionModel(aac.getSubmissionsSaved().toString());
        if (aac.getUnlimitedSubmissions().equals(Boolean.TRUE)){
          templateBean.setSubmissionModel(AssessmentAccessControlIfc.UNLIMITED_SUBMISSIONS.toString());
          templateBean.setSubmissionNumber(null);
        }
        else{
          templateBean.setSubmissionModel(AssessmentAccessControlIfc.LIMITED_SUBMISSIONS.toString());
          if (aac.getSubmissionsAllowed() != null)
            templateBean.setSubmissionNumber(aac.getSubmissionsAllowed().toString());
          else
            templateBean.setSubmissionNumber("0");
        }
        if (aac.getLateHandling() != null)
          templateBean.setLateHandling(aac.getLateHandling().toString());
        if (aac.getAutoSubmit() != null && aac.getAutoSubmit().intValue() == 1) {
        		templateBean.setAutomaticSubmission(Boolean.TRUE);
        	}
        }
        else {
        	templateBean.setAutomaticSubmission(Boolean.FALSE);
        }

      // Evaluation Model
      EvaluationModel model = (EvaluationModel) template.getEvaluationModel();
      if (model != null)
      {
        if (model.getAnonymousGrading() != null)
          templateBean.setAnonymousGrading(model.getAnonymousGrading().toString());
        templateBean.setToGradebook(model.getToGradeBook());
        if (model.getScoringType() != null)
          templateBean.setRecordedScore(model.getScoringType().toString());
        //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().
         // put("template.em", model);
      }

      // Assessment Feedback
      AssessmentFeedback feedback = (AssessmentFeedback)
        template.getAssessmentFeedback();
      if (feedback != null)
      {
        if (feedback.getFeedbackDelivery() != null)
          templateBean.setFeedbackType
            (feedback.getFeedbackDelivery().toString());
        if (feedback.getFeedbackAuthoring() != null)
          templateBean.setFeedbackAuthoring
            (feedback.getFeedbackAuthoring().toString());
        templateBean.setFeedbackComponent_QuestionText
          (feedback.getShowQuestionText());
        templateBean.setFeedbackComponent_StudentResp
          (feedback.getShowStudentResponse());
        templateBean.setFeedbackComponent_CorrectResp
          (feedback.getShowCorrectResponse());
        templateBean.setFeedbackComponent_StudentScore
          (feedback.getShowStudentScore());
        templateBean.setFeedbackComponent_StudentQuestionScore
          (feedback.getShowStudentQuestionScore());
        templateBean.setFeedbackComponent_QuestionLevel
          (feedback.getShowQuestionLevelFeedback());
        templateBean.setFeedbackComponent_SelectionLevel
          (feedback.getShowSelectionLevelFeedback());
        templateBean.setFeedbackComponent_GraderComments
          (feedback.getShowGraderComments());
        templateBean.setFeedbackComponent_Statistics
          (feedback.getShowStatistics());
       }

       SimpleDateFormat format = new SimpleDateFormat();
       templateBean.setCreatedDate(format.format(template.getCreatedDate()));
       templateBean.setCreatedBy(template.getCreatedBy());
       templateBean.setLastModified(template.getLastModifiedDate().toString());
       templateBean.setLastModifiedBy(template.getLastModifiedBy());
       templateBean.setTypeId(template.getTypeId().toString());

       templateBean.setValueMap(template.getAssessmentMetaDataMap
         (template.getAssessmentMetaDataSet()));
    }
    catch (RuntimeException ex)
    {
      log.error(ex.getMessage(), ex);
      return false;
    }

    return true;
  }
}
