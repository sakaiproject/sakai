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
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.samigo.api.SamigoETSProvider;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * sends students the "your instructor has added comments or updated
 * grading" email. Dispatched from the per-row send link on Total Scores, and
 * from the notify / Save &amp; Notify buttons on the individual student page;
 * the source component id selects the mode. The actual sending is
 * {@link SamigoETSProvider#notifyGradingUpdated}; this listener only gathers
 * eligible targets, applies the per-submission cooldown, and reports the
 * outcome.
 */
@Slf4j
public class NotifyGradingUpdatedListener implements ActionListener {

    /** component ids that target the single student of the student score page */
    private static final List<String> STUDENT_PAGE_SOURCES = List.of("notifyStudent", "saveAndNotify");
    /** the unified Update button; notify rides with it via the "Also email" checkbox */
    private static final String APPLY_SOURCE = "updateScores";

    public void processAction(ActionEvent ae) throws AbortProcessingException {
        TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
        if (!totalScores.getGradingNotifyAvailable()) {
            return;
        }

        String sourceId = ae.getComponent().getId();
        List<String> userIds = new ArrayList<>();
        List<String> gradingIds = new ArrayList<>();
        int skippedCooldown = 0;

        if (STUDENT_PAGE_SOURCES.contains(sourceId)) {
            StudentScoresBean studentScores = (StudentScoresBean) ContextUtil.lookupBean("studentScores");
            if (StringUtils.isNotBlank(studentScores.getStudentId())
                    && isRealSubmission(studentScores.getAssessmentGradingId())) {
                if (totalScores.isNotifyCoolingDown(studentScores.getAssessmentGradingId())) {
                    skippedCooldown++;
                } else {
                    userIds.add(studentScores.getStudentId());
                    gradingIds.add(studentScores.getAssessmentGradingId());
                }
            }
        } else if (APPLY_SOURCE.equals(sourceId)) {
            // Rides with the unified Apply: only fire when the instructor ticked
            // "Also email affected students". Emails the target's submitters
            // (the same matchesTarget set Apply wrote to, intersected with real
            // submissions) — non-submitters have nothing to view, so they are
            // never emailed whatever the target.
            if (!Boolean.TRUE.equals(totalScores.getBulkNotify())) {
                return;
            }
            String target = totalScores.getBulkApplyTarget();
            for (Object o : totalScores.getAgents()) {
                AgentResults agent = (AgentResults) o;
                if (!ApplyToSelectedListener.matchesTarget(agent, target) || !isEligible(agent)) {
                    continue;
                }
                // "affected students" means it: the save loop just recorded per-row
                // whether anything actually changed — an Update with no effective
                // edit must not email the whole target set
                if (!Boolean.TRUE.equals(agent.getGradeUpdated())) {
                    continue;
                }
                if (totalScores.isNotifyCoolingDown(agent.getAssessmentGradingId().toString())) {
                    skippedCooldown++;
                    continue;
                }
                userIds.add(agent.getIdString());
                gradingIds.add(agent.getAssessmentGradingId().toString());
            }
            totalScores.setBulkNotify(Boolean.FALSE);
        } else { // per-row send link: trust only the gradingData id and
                 // resolve the student from our own rows, so a tampered
                 // studentid param cannot direct mail at someone else
            String gradingId = ContextUtil.lookupParam("gradingData");
            if (isRealSubmission(gradingId)) {
                for (Object o : totalScores.getAgents()) {
                    AgentResults agent = (AgentResults) o;
                    if (agent.getAssessmentGradingId() == null
                            || !gradingId.equals(agent.getAssessmentGradingId().toString())) {
                        continue;
                    }
                    if (isEligible(agent)) {
                        if (totalScores.isNotifyCoolingDown(gradingId)) {
                            skippedCooldown++;
                        } else {
                            userIds.add(agent.getIdString());
                            gradingIds.add(gradingId);
                        }
                    }
                    break;
                }
            }
        }

        int notified = 0;
        if (!userIds.isEmpty()) {
            SamigoETSProvider provider = ComponentManager.get(SamigoETSProvider.class);
            String siteId = AgentFacade.getCurrentSiteId();
            // Only rows whose student was actually delivered to count as
            // notified: skipped users (no email, unknown) must not update the
            // cooldown bookkeeping or emit notify events.
            List<String> delivered = provider.notifyGradingUpdated(userIds, siteId, totalScores.getAssessmentName());
            notified = delivered.size();
            for (int i = 0; i < userIds.size(); i++) {
                if (!delivered.contains(userIds.get(i))) {
                    continue;
                }
                String gradingId = gradingIds.get(i);
                totalScores.markGradingNotified(gradingId);
                EventTrackingService.post(EventTrackingService.newEvent(SamigoConstants.EVENT_GRADING_UPDATED_NOTIFY,
                        "siteId=" + siteId + ", publishedAssessmentId=" + totalScores.getPublishedId()
                                + ", assessmentGradingId=" + gradingId, true));
            }
        }

        FacesContext context = FacesContext.getCurrentInstance();
        String bundle = "org.sakaiproject.tool.assessment.bundle.EvaluationMessages";
        // one localized template per case (no concatenation of localized fragments, so word order stays translatable)
        String message = skippedCooldown > 0
                ? MessageFormat.format(ContextUtil.getLocalizedString(bundle, "notify_grading_updated_result_with_skipped"), notified, skippedCooldown)
                : MessageFormat.format(ContextUtil.getLocalizedString(bundle, "notify_grading_updated_result"), notified);
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private boolean isEligible(AgentResults agent) {
        return agent.getAssessmentGradingId() != null
                && isRealSubmission(agent.getAssessmentGradingId().toString())
                && Boolean.TRUE.equals(agent.getForGrade())
                && agent.getAttemptDate() != null;
    }

    private boolean isRealSubmission(String gradingId) {
        return StringUtils.isNotBlank(gradingId) && !"-1".equals(gradingId);
    }
}
