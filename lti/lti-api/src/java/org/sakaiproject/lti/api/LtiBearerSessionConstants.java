/**
 * Copyright (c) 2026 The Apereo Foundation
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
 */
package org.sakaiproject.lti.api;

/**
 * Session and request attribute keys for LTI Bearer (SAT) access to Sakai APIs
 * (webapi, /direct, and similar entry points).
 */
public final class LtiBearerSessionConstants {

    private LtiBearerSessionConstants() {
    }

    /** Validated {@link org.sakaiproject.lti13.util.SakaiAccessToken} on the Sakai session. */
    public static final String SESSION_ATTR_SAT = "sakai.lti.bearer.sat";

    /** Synthetic user id prefix: {@code lti-tool-{toolId}}. */
    public static final String LTI_TOOL_USER_ID_PREFIX = "lti-tool-";

    /** Request attribute: LTI mini-session created for this request (for cleanup). */
    public static final String REQUEST_ATTR_LTI_SESSION = "sakai.lti.bearer.session";

    /** Request attribute: session in effect before LTI Bearer handling ran. */
    public static final String REQUEST_ATTR_PREVIOUS_SESSION = "sakai.lti.bearer.previousSession";
}
