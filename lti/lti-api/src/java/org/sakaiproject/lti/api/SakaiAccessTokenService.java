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

import java.security.Key;

import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.tsugi.oauth2.objects.AccessToken;

/**
 * Issues and validates Sakai Access Tokens (SATs) used by LTI Advantage and,
 * eventually, LTI-authorized webapi and Entity Broker (/direct) calls.
 */
public interface SakaiAccessTokenService {

    String SAT_CACHE_NAME = "org.sakaiproject.lti.impl.SakaiAccessTokenServiceImpl_cache";

    String PROPERTY_PUBLIC = "lti.advantage.lti13servlet.public";
    String PROPERTY_PRIVATE = "lti.advantage.lti13servlet.private";

    void init();

    void destroy();

    boolean isSigningKeyAvailable();

    /**
     * Public key used to verify SAT signatures.
     */
    Key getVerificationKey();

    /**
     * Parse {@code Authorization: Bearer <jwt>} and return the JWT string.
     */
    String extractBearerToken(String authorizationHeader) throws SakaiAccessTokenException;

    /**
     * Verify signature, expiry, and required SAT claims.
     */
    SakaiAccessToken validateToken(String jws) throws SakaiAccessTokenException;

    /**
     * Sign a SAT payload into a compact JWS.
     */
    String sign(SakaiAccessToken sat) throws SakaiAccessTokenException;

    /**
     * Validate the tool's client_assertion JWT and return an OAuth access token response
     * (IMS scopes in {@link AccessToken#scope}, Sakai scopes inside the signed SAT).
     */
    AccessToken issueAccessToken(long toolId, String clientAssertion, String requestedScope)
            throws SakaiAccessTokenException;
}
