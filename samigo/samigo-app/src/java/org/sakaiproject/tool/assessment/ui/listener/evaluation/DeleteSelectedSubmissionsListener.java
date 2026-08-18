/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.evaluation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * "Delete Selected" on Total Scores: applies the existing single-row
 * soft delete ({@link GrantSubmissionListener#deleteSubmission}) to every
 * selected row with a real submission — status flip, page-bean pruning,
 * gradebook renotification and one delete event per row. Gated in the UI by
 * the same permission as the per-row trash icon and re-checked here.
 */
@Slf4j
public class DeleteSelectedSubmissionsListener implements ActionListener {

    private final EventTrackingService eventTrackingService = ComponentManager.get(EventTrackingService.class);

    public void processAction(ActionEvent ae) throws AbortProcessingException {
        TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
        PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
        if (!person.getIsAdmin() && totalScores.getRestrictedDelete()) {
            log.warn("Blocked Delete Selected without delete permission, published assessment {}", totalScores.getPublishedId());
            return;
        }

        // snapshot first: deleteSubmission mutates the agents collection
        List<String> gradingIds = new ArrayList<>();
        for (Iterator iter = totalScores.getAgents().iterator(); iter.hasNext();) {
            AgentResults agent = (AgentResults) iter.next();
            if (Boolean.TRUE.equals(agent.getSelected())
                    && agent.getAssessmentGradingId() != null
                    && !Long.valueOf(-1L).equals(agent.getAssessmentGradingId())
                    && agent.getAttemptDate() != null) {
                gradingIds.add(agent.getAssessmentGradingId().toString());
            }
        }

        GradingService gradingService = new GradingService();
        int failed = 0;
        for (String gradingId : gradingIds) {
            // isolate per-row failures so one bad/stale record doesn't abort the whole batch
            try {
                GrantSubmissionListener.deleteSubmission(totalScores, gradingId, totalScores.getPublishedId(),
                        gradingService, eventTrackingService);
            } catch (RuntimeException e) {
                failed++;
                log.warn("Failed to delete submission {} during batch delete", gradingId, e);
            }
        }

        String message = MessageFormat.format(ContextUtil.getLocalizedString(
                "org.sakaiproject.tool.assessment.bundle.EvaluationMessages", "batch_delete_result"),
                gradingIds.size() - failed);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }
}
