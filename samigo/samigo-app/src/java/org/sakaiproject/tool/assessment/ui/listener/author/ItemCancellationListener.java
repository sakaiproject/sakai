/**
 * Copyright (c) 2023 The Apereo Foundation
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

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.grading.api.InvalidCategoryException;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService.TotalScoreCancellationException;
import org.sakaiproject.tool.assessment.ui.bean.author.AssessmentBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.QuestionScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;


@Slf4j
public class ItemCancellationListener implements ActionListener {


    public void processAction(ActionEvent ae) throws AbortProcessingException {
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

        String cancellation = ContextUtil.lookupParam("cancellation");
        String itemId = ContextUtil.lookupParam("itemId");
        String outcome = ContextUtil.lookupParam("outcome");
        boolean regrade = Boolean.parseBoolean(ContextUtil.lookupParam("regrade"));

        AssessmentBean assessmentBean = (AssessmentBean) ContextUtil.lookupBean("assessmentBean");
        QuestionScoresBean questionScoresBean = (QuestionScoresBean) ContextUtil.lookupBean("questionScores");

        log.debug("cancellation {}", cancellation);
        log.debug("itemId: {}", itemId);
        log.debug("outcome {}", outcome);
        log.debug("regrade: {}", regrade);

        String publishedAssessmentId = SamigoConstants.OUTCOME_AUTHOR_EDIT_ASSESSMENT.equals(outcome)
                ? assessmentBean.getAssessmentId()
                : (SamigoConstants.OUTCOME_EVALUATION_QUESTION_SCORES.equals(outcome) ? questionScoresBean.getPublishedId() : null);

        // Abort if publishedAssessmentId or passed params are invalid
        if (!NumberUtils.isParsable(publishedAssessmentId)
                || !NumberUtils.isParsable(itemId)
                || !NumberUtils.isParsable(cancellation)) {
            //Add error message to context
            String errorMessage = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.CommonMessages",
                    "cancel_question_error_cancelling");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(errorMessage));

            throw new AbortProcessingException(String.format("AssessmentId [%s], ItemId [%s] or cancellation [%s] is not parsable",
                    publishedAssessmentId, itemId, cancellation));
        }

        PublishedAssessmentFacade updatedPublishedAssessment;
        try {
            updatedPublishedAssessment = publishedAssessmentService.cancelPublishedItem(
                    publishedAssessmentId, itemId, Integer.parseInt(cancellation));
        } catch (TotalScoreCancellationException e) {
            String messageKey = e.isChangesMayHaveBeenMade() ? "cancel_question_error_incomplete"
                    : (e.isCategoryRestricted() ? "cancel_question_reduce_total_category_restricted"
                            : "cancel_question_reduce_total_unavailable");
            cancellationError(messageKey);
            log.warn("Question cancellation rejected for assessment {} and item {}", publishedAssessmentId, itemId, e);
            throw new AbortProcessingException(e);
        } catch (IllegalArgumentException e) {
            cancellationError("cancel_question_error_cancelling");
            throw new AbortProcessingException(e);
        }

        // Update bean with updated Assessment
        if (SamigoConstants.OUTCOME_AUTHOR_EDIT_ASSESSMENT.equals(outcome)) {
            assessmentBean.setAssessment(updatedPublishedAssessment);
        }

        if (SamigoConstants.OUTCOME_EVALUATION_QUESTION_SCORES.equals(outcome)) {
            if (regrade) {
                publishedAssessmentService.regradePublishedAssessment(
                        publishedAssessmentService.getPublishedAssessment(publishedAssessmentId), false);
                try {
                    publishedAssessmentService.updateGradebook((PublishedAssessmentData) updatedPublishedAssessment.getData());
                } catch (InvalidCategoryException e) {
                    cancellationError("cancel_question_error_gradebook_category");
                    log.warn("Gradebook rejected cancellation synchronization for assessment {}", publishedAssessmentId, e);
                }
            }

            // Update questionsScores bean
            questionScoresBean.setPublishedAssessment(updatedPublishedAssessment);
            questionScoresBean.setItemScoresMap(null);
            QuestionScoreListener questionScoreListener = new QuestionScoreListener();
            questionScoreListener.questionScores(publishedAssessmentId, questionScoresBean, false);
        }
    }
    private void cancellationError(String messageKey) {
        String message = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.CommonMessages", messageKey);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

}
