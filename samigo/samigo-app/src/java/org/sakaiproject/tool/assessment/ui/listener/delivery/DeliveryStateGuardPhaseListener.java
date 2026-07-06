/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.ui.listener.delivery;

import java.util.Map;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * SAK-44349: the two halves of the delivery stale-tab guard.
 *
 * GATE (after RESTORE_VIEW): a POST to a guarded delivery view must carry the
 * current state token; a stale post is short-circuited to the non-fatal
 * resync view before JSF's Update Model Values can bind its inputs into the
 * shared session-scoped DeliveryBean. Acceptance rotates the token atomically,
 * so duplicate/concurrent posts of the same form lose cleanly.
 *
 * ROTATION (before RENDER_RESPONSE): every render of a guarded view - POST
 * response, plain GET, duplicated tab, back/forward revalidation - issues a
 * fresh token, so every previously rendered form is invalid by construction.
 * Autosave responses are the one exception: autosaves must not advance the
 * sequence, or a lost autosave response would invalidate its own tab.
 *
 * Scoped to student take modes; review and preview posts fall through to the
 * legacy checks untouched.
 */
@Slf4j
public class DeliveryStateGuardPhaseListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    private static final String DELIVERY_VIEW = "/jsf/delivery/deliverAssessment.jsp";
    private static final String TOC_VIEW = "/jsf/delivery/tableOfContents.jsp";
    private static final String CONFIRM_SUBMIT_VIEW = "/jsf/delivery/confirmSubmit.jsp";
    private static final String DELIVERY_FORM = "takeAssessmentForm";
    private static final String TOC_FORM = "tableOfContentsForm";
    private static final String RESYNC_OUTCOME = "staleTabResync";

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId() != PhaseId.RENDER_RESPONSE) {
            return;
        }
        FacesContext context = event.getFacesContext();
        DeliveryBean delivery = guardedDelivery(context);
        if (delivery == null || !DeliveryStateGuard.isGuardEnabled()) {
            return;
        }
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        if (DeliveryStateGuard.isAutoSavePost(params)) {
            return;
        }
        delivery.getStateGuard().regenerate();
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        if (event.getPhaseId() != PhaseId.RESTORE_VIEW) {
            return;
        }
        FacesContext context = event.getFacesContext();
        if (!context.isPostback() || context.getResponseComplete()) {
            return;
        }
        DeliveryBean delivery = guardedDelivery(context);
        if (delivery == null) {
            return;
        }
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String viewId = context.getViewRoot().getViewId();
        boolean deliveryForm = params.containsKey(DELIVERY_FORM);
        if (!(TOC_VIEW.equals(viewId) ? params.containsKey(TOC_FORM) : deliveryForm)) {
            return;
        }

        DeliveryStateGuard.Decision decision = DeliveryStateGuard.evaluate(
            delivery.getStateGuard(),
            params.get(DeliveryStateGuard.TOKEN_PARAM),
            DeliveryStateGuard.isGuardEnabled(),
            DeliveryStateGuard.isTimeoutSubmit(params),
            DeliveryStateGuard.isAutoSavePost(params));

        switch (decision) {
            case ACCEPT:
                // Only a genuine token match earns this attribute: it lifts the
                // persistence backstop and the cross-window submittedDate check
                // switches to its session-aware form (DeliveryBean check 4).
                context.getExternalContext().getRequestMap()
                    .put(DeliveryStateGuard.REQUEST_ATTR_VERIFIED, Boolean.TRUE);
                break;
            case REJECT_SILENT:
            case REJECT_RESYNC:
                log.info("SAK-44349 stale {} post rejected; view={}, gradingId={}",
                    decision == DeliveryStateGuard.Decision.REJECT_SILENT ? "autosave" : "interactive",
                    viewId, delivery.getAssessmentGradingId());
                context.renderResponse();
                context.getApplication().getNavigationHandler()
                    .handleNavigation(context, null, RESYNC_OUTCOME);
                break;
            default:
                break;
        }
    }

    /**
     * The session DeliveryBean when this request targets a guarded view in a
     * student take mode; null otherwise.
     */
    private DeliveryBean guardedDelivery(FacesContext context) {
        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot == null) {
            return null;
        }
        String viewId = viewRoot.getViewId();
        if (!DELIVERY_VIEW.equals(viewId) && !TOC_VIEW.equals(viewId) && !CONFIRM_SUBMIT_VIEW.equals(viewId)) {
            return null;
        }
        DeliveryBean delivery = (DeliveryBean) ContextUtil.lookupBean("delivery");
        if (delivery == null) {
            return null;
        }
        String actionString = delivery.getActionString();
        if (!"takeAssessment".equals(actionString) && !"takeAssessmentViaUrl".equals(actionString)) {
            return null;
        }
        return delivery;
    }
}
