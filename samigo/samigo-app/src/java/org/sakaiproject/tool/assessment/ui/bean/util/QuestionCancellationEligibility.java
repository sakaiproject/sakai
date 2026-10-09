/*
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
package org.sakaiproject.tool.assessment.ui.bean.util;

import java.util.Map;
import java.util.function.BooleanSupplier;
import jakarta.faces.context.FacesContext;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/** Shares cancellation display eligibility across JSF getters within one request. */
@Slf4j
public final class QuestionCancellationEligibility {
    private static final String RESTRICTED = "cancel_question_reduce_total_category_restricted";
    private static final String UNAVAILABLE = "cancel_question_reduce_total_unavailable";

    private QuestionCancellationEligibility() {
    }

    public static String restrictionKey(PublishedAssessmentIfc assessment) {
        if (assessment == null) {
            return UNAVAILABLE;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        Placement placement = ToolManager.getCurrentPlacement();
        String siteId = placement == null ? null : placement.getContext();
        return restrictionKey(context.getExternalContext().getRequestMap(),
            siteId, assessment.getPublishedAssessmentId(),
            () -> new PublishedAssessmentService().isTotalScoreCancellationAllowed(assessment));
    }

    public static String restrictionMessage(PublishedAssessmentIfc assessment) {
        String messageKey = restrictionKey(assessment);
        return messageKey.isEmpty() ? "" : ContextUtil.getLocalizedString(
            "org.sakaiproject.tool.assessment.bundle.CommonMessages", messageKey);
    }

    // The request-map seam lets tests exercise lifecycle behavior without replacing Samigo services.
    public static String restrictionKey(Map<String, Object> requestMap, String siteId, Long assessmentId,
            BooleanSupplier lookup) {
        String key = QuestionCancellationEligibility.class.getName() + ":" + siteId + ":" + assessmentId;
        String messageKey = (String) requestMap.get(key);
        if (messageKey == null) {
            try {
                messageKey = lookup.getAsBoolean() ? "" : RESTRICTED;
            } catch (PublishedAssessmentService.TotalScoreCancellationException e) {
                messageKey = UNAVAILABLE;
                log.warn("Unable to check question cancellation eligibility for assessment {} in site {}",
                    assessmentId, siteId, e);
            }
            requestMap.put(key, messageKey);
        }
        return messageKey;
    }
}
