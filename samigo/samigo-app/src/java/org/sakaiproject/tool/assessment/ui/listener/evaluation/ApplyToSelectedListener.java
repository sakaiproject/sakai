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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.AgentResults;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.TotalScoresBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * the unified "Apply" on Total Scores. Writes the bar's Adjustment
 * value and/or Comment onto every participant matching the chosen target
 * ({@code Apply to}: No submission / With a submission / Selected / All), then
 * lets the standard {@link TotalScoreUpdateListener} in the same listener chain
 * persist the rows through its proven change-detection and sanitization.
 *
 * <p>This folds in the old "Apply This Score to No Submission" control: the
 * default target is {@code NO_SUBMISSION} and the grade write for those students
 * goes through exactly the same path as before — the adjustment lands on the
 * override score, and {@code TotalScoreUpdateListener.saveTotalScores} creates
 * the ungraded {@code AssessmentGradingData} record for participants who never
 * submitted (its {@code gradingId == -1} branch). We only widen <em>which</em>
 * participants get the value set; we do not re-implement the grade writer.</p>
 *
 * <p>For roster-wide targets we scan (and hand downstream) the full
 * {@code getAllAgentsDirect()} list so students off the current page are
 * covered, mirroring the legacy applyToUngraded behaviour. Comments are never
 * written to a no-submission student on their own, so we never materialise a
 * 0-grade record just to attach a note — a comment only rides along when an
 * adjustment is also being applied to that student.</p>
 */
@Slf4j
public class ApplyToSelectedListener implements ActionListener {

    private static final String BUNDLE = "org.sakaiproject.tool.assessment.bundle.EvaluationMessages";

    static final String SELECTED = "SELECTED";
    static final String NO_SUBMISSION = "NO_SUBMISSION";
    static final String WITH_SUBMISSIONS = "WITH_SUBMISSIONS";
    static final String ALL = "ALL";

    public void processAction(ActionEvent ae) throws AbortProcessingException {
        TotalScoresBean totalScores = (TotalScoresBean) ContextUtil.lookupBean("totalScores");
        FacesContext context = FacesContext.getCurrentInstance();

        String score = StringUtils.trimToNull(totalScores.getApplyToSelectedScore());
        String comment = StringUtils.trimToNull(totalScores.getBulkComment());
        String mode = totalScores.getBulkCommentMode();
        boolean onlyEmpty = "ONLY_EMPTY".equals(mode);   // skip students who already have a comment
        boolean append = !"REPLACE".equals(mode);        // APPEND (and ONLY_EMPTY) keep existing text
        String target = StringUtils.defaultIfBlank(totalScores.getBulkApplyTarget(), NO_SUBMISSION);

        // This listener handles only the bulk composer. With nothing in it, the
        // single Update just saves inline edits (TotalScoreUpdateListener) and
        // emails whoever changed (NotifyGradingUpdatedListener) — step aside.
        if (score == null && comment == null) {
            return;
        }

        if (score != null) {
            double value;
            try {
                value = Double.parseDouble(score.replace(",", "."));
            } catch (NumberFormatException e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ContextUtil.getLocalizedString(BUNDLE, "batch_apply_bad_score"), null));
                throw new AbortProcessingException();
            }
            score = score.replace(",", ".");

            // Out-of-range guard with a confirm-again gate: the first time we see
            // a negative or above-max adjustment we warn and remember it;
            // re-submitting the same value is the instructor's "apply anyway".
            Double max = parseMax(totalScores.getMaxScore());
            boolean overMax = max != null && value > max;
            boolean negative = value < 0;
            if (overMax || negative) {
                if (!score.equals(totalScores.getBulkOverrideConfirmValue())) {
                    totalScores.setBulkOverrideConfirmValue(score);
                    String msg = MessageFormat.format(ContextUtil.getLocalizedString(BUNDLE,
                                    overMax ? "batch_apply_over_max" : "batch_apply_negative"),
                            trimNumber(value), max != null ? trimNumber(max) : "");
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null));
                    throw new AbortProcessingException();
                }
                // same value re-submitted -> confirmed, fall through
            }
            totalScores.setBulkOverrideConfirmValue(null);
        }

        // "Selected" reads the rendered-row checkboxes; every other target spans
        // the whole roster (including off-page students). Widen the bean's agent
        // view to whatever we scan so the downstream save sees the same set;
        // ResetTotalScoreListener reloads the page view afterwards.
        Collection agentsToScan;
        if (SELECTED.equals(target)) {
            agentsToScan = totalScores.getAgents();
        } else {
            List all = totalScores.getAllAgentsDirect();
            agentsToScan = (all != null) ? all : totalScores.getAgents();
            if (agentsToScan != totalScores.getAgents()) {
                totalScores.setAgents(agentsToScan);
            }
        }

        int applied = 0;
        int skipped = 0;
        int matched = 0;
        for (Iterator iter = agentsToScan.iterator(); iter.hasNext();) {
            AgentResults agent = (AgentResults) iter.next();
            if (!matchesTarget(agent, target)) {
                continue;
            }
            matched++;
            boolean noSubmission = isNoSubmission(agent);
            boolean touched = false;
            if (score != null) {
                agent.setTotalOverrideScore(score);
                touched = true;
            }
            // Only attach a comment to a no-submission student when a score is
            // also being applied, so a lone comment never creates a 0 record.
            if (comment != null && (!noSubmission || score != null)) {
                String existing = agent.getComments();
                if (onlyEmpty && StringUtils.isNotBlank(existing)) {
                    // "Only students without a comment": leave this one untouched
                } else {
                    agent.setComments(onlyEmpty ? comment : mergeComment(existing, comment, append));
                    touched = true;
                }
            }
            if (touched) {
                applied++;
            } else {
                skipped++;
            }
        }

        if (SELECTED.equals(target) && matched == 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    ContextUtil.getLocalizedString(BUNDLE, "batch_apply_need_selection"), null));
            throw new AbortProcessingException();
        }

        totalScores.setApplyToSelectedScore(null);
        totalScores.setBulkComment(null);

        // one localized template per case (no concatenation of localized fragments, so word order stays translatable)
        String message = skipped > 0
                ? MessageFormat.format(ContextUtil.getLocalizedString(BUNDLE, "batch_apply_result_with_skipped"), applied, skipped)
                : MessageFormat.format(ContextUtil.getLocalizedString(BUNDLE, "batch_apply_result"), applied);
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    /** the assessment max score as a number, or null when it isn't one ("N/A") */
    static Double parseMax(String maxScore) {
        if (StringUtils.isBlank(maxScore)) {
            return null;
        }
        try {
            // getMaxScore() is locale-formatted (e.g. "10,5" in comma-decimal locales),
            // so normalize the separator before parsing, like the score fields do
            return Double.valueOf(maxScore.replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** render a score without a trailing ".0" for whole numbers, for messages */
    static String trimNumber(double d) {
        if (d == Math.floor(d) && !Double.isInfinite(d)) {
            return Long.toString((long) d);
        }
        return Double.toString(d);
    }

    /**
     * Merges a bulk comment into a row's existing comment: append keeps what
     * the instructor already wrote (newline separated); a blank addition never
     * changes anything.
     */
    static String mergeComment(String existing, String addition, boolean append) {
        if (StringUtils.isBlank(addition)) {
            return existing;
        }
        if (!append || StringUtils.isBlank(existing)) {
            return addition;
        }
        return existing + "\n" + addition;
    }

    /**
     * No-submission test mirroring the legacy applyToUngraded condition
     * (gradingId -1 or a null submitted date).
     */
    static boolean isNoSubmission(AgentResults agent) {
        return agent.getAssessmentGradingId() == null
                || Long.valueOf(-1L).equals(agent.getAssessmentGradingId())
                || agent.getSubmittedDate() == null;
    }

    /** whether a participant falls in the chosen "Apply to" target set */
    static boolean matchesTarget(AgentResults agent, String target) {
        if (SELECTED.equals(target)) {
            return Boolean.TRUE.equals(agent.getSelected());
        }
        if (WITH_SUBMISSIONS.equals(target)) {
            return !isNoSubmission(agent);
        }
        if (ALL.equals(target)) {
            return true;
        }
        // NO_SUBMISSION and any unknown value fall back to the legacy default.
        return isNoSubmission(agent);
    }
}
