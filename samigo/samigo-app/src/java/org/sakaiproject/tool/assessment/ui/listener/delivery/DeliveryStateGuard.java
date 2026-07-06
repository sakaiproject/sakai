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

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.samigo.util.SamigoConstants;

/**
 * SAK-44349: state token for the delivery stale-tab guard.
 *
 * Every render of a guarded delivery view carries the token currently held by
 * the session-scoped DeliveryBean; rendering rotates it, so every previously
 * rendered form (other tabs, duplicated tabs, back/forward cache pages) is
 * invalidated. A POST must return the current token to be processed; on
 * acceptance the token rotates atomically, so a duplicate or concurrent
 * submit of the same form loses cleanly. Autosave posts verify without
 * rotating and autosave re-renders do not rotate, so a current tab's autosave
 * can never invalidate that tab - even when its response is lost in transit.
 */
public class DeliveryStateGuard implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Request parameter carrying the token back from the browser. */
    public static final String TOKEN_PARAM = "dlvrStateToken";
    /** Request parameter identifying an autosave post of the delivery form. */
    public static final String AUTOSAVE_PARAM = "takeAssessmentForm:autoSave";
    /** Request parameter of the forced time-expiry submit button. */
    public static final String TIMEOUT_SUBMIT_PARAM = "takeAssessmentForm:submitNoCheck";
    /** Request attribute marking a request that passed the token check. */
    public static final String REQUEST_ATTR_VERIFIED = "samigo.delivery.tokenVerified";

    public enum Decision {
        /** Token matches: process the request. */
        ACCEPT,
        /** Stale interactive POST: reject and show the resync page. */
        REJECT_RESYNC,
        /** Stale autosave POST: reject; the client handles it quietly. */
        REJECT_SILENT,
        /** Guard does not apply to this request. */
        BYPASS
    }

    private String token = newToken();

    public synchronized String getToken() {
        return token;
    }

    /** Rotate the token, invalidating every previously rendered form. */
    public synchronized String regenerate() {
        token = newToken();
        return token;
    }

    /** Non-rotating comparison, for posts that must not advance the sequence. */
    public synchronized boolean matches(String postedToken) {
        return StringUtils.isNotEmpty(postedToken) && postedToken.equals(token);
    }

    /**
     * Atomically accept-and-rotate: succeeds for exactly one caller per issued
     * token, so concurrent duplicate posts cannot both pass.
     */
    public synchronized boolean compareAndRotate(String postedToken) {
        if (matches(postedToken)) {
            token = newToken();
            return true;
        }
        return false;
    }

    /**
     * Decide what to do with a delivery-form POST.
     *
     * Autosave posts verify WITHOUT rotating (like Moodle's negative-sequence
     * autosave steps). The forced time-expiry submit is tried against the
     * token first - in the normal single-tab case it matches and gets full
     * authority - but a stale one is never blocked (BYPASS): the persistence
     * backstop in SubmitToGradingActionListener protects saved answers there.
     */
    public static Decision evaluate(DeliveryStateGuard guard, String postedToken,
            boolean guardEnabled, boolean timeoutSubmit, boolean autoSavePost) {
        if (!guardEnabled || guard == null) {
            return Decision.BYPASS;
        }
        if (autoSavePost) {
            return guard.matches(postedToken) ? Decision.ACCEPT : Decision.REJECT_SILENT;
        }
        if (timeoutSubmit) {
            return guard.compareAndRotate(postedToken) ? Decision.ACCEPT : Decision.BYPASS;
        }
        return guard.compareAndRotate(postedToken) ? Decision.ACCEPT : Decision.REJECT_RESYNC;
    }

    /** Is the stale-tab guard enabled by configuration? Default true. */
    public static boolean isGuardEnabled() {
        return ServerConfigurationService.getBoolean(SamigoConstants.SAK_PROP_DELIVERY_STALE_TAB_GUARD, true);
    }

    /** Was this request's token verified by the guard? */
    public static boolean isVerified(FacesContext context) {
        return context != null && Boolean.TRUE.equals(context.getExternalContext()
                .getRequestMap().get(REQUEST_ATTR_VERIFIED));
    }

    /** Is this request an autosave post from the delivery form? */
    public static boolean isAutoSavePost(Map<String, String> requestParams) {
        return requestParams != null && requestParams.containsKey(AUTOSAVE_PARAM);
    }

    /** Is this request the forced time-expiry submit? */
    public static boolean isTimeoutSubmit(Map<String, String> requestParams) {
        return requestParams != null && requestParams.containsKey(TIMEOUT_SUBMIT_PARAM);
    }

    private static String newToken() {
        return UUID.randomUUID().toString();
    }
}
