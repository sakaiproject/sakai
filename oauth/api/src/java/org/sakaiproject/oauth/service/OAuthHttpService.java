/**
 * Copyright (c) 2009-2017 The Apereo Foundation
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

package org.sakaiproject.oauth.service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * OAuthHttpService handles incoming OAuth requests and interactions with filters. *
 *
 * @author Colin Hebert
 */
public interface OAuthHttpService {
    /**
     * Check the validity of a request toward a protected resource
     * <p>
     * Mostly used in filters, this method checks the validity of the token provided by a consumer when accessing
     * protected resources on the behalf of a client.
     * </p>
     * <p>
     * If the request is an OAuth request but some issues due to invalid values or unauthorised access the response
     * will be modified to return an HTTP error.
     * </p>
     *
     * @param request  Incoming request
     * @param response Outgoing response
     * @return True if the request is an OAuth request and is valid, false otherwise
     * @throws IOException
     * @throws ServletException
     */
    boolean isValidOAuthRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;

    /**
     * Extract the access token from an OAuth request.
     *
     * @param request OAuth request
     * @return The access token if it exists, null otherwise
     * @throws IOException
     */
    String getOAuthAccessToken(HttpServletRequest request) throws IOException;

    /**
     * Handle the first step of the OAuth 1.0 authentication.
     * <p>
     * Check consumer's signature and provide temporary credentials
     * </p>
     *
     * @param request  Incoming request
     * @param response Outgoing response
     * @throws IOException
     * @throws ServletException
     * @see <a href="http://tools.ietf.org/html/rfc5849#section-2.1">Temporary Credentials</a>
     */
    void handleRequestToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;

    /**
     * Handle the last step of the OAuth 1.0 authentication.
     * <p>
     * Check consumer's signature, the request accessor's verifier<br />
     * Create an access accessor for the user who accepted the connection
     * </p>
     *
     * @param request  Incoming request
     * @param response Outgoing response
     * @throws IOException
     * @throws ServletException
     * @see <a href="http://tools.ietf.org/html/rfc5849#section-2.3">Token Credentials</a>
     */
    void handleGetAccessToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException;

    /**
     * Handle the authorisation step of the OAuth 1.0 authentication.
     * <p>
     * Check if the user has authorised the consumer.<br />
     * Assign the user to the request token.<br />
     * Check an optional verifier (not from the OAuth protocol).<br />
     * Once the user is logged-in and has accepted (or denied) the authorisation, send him back on the callback URL.
     * </p>
     *
     * @param request    Incoming request
     * @param response   Outgoing response
     * @param authorised Has the user accepted to give access to the consumer
     * @param token      Request token from the consumer
     * @param verifier   Optionnal verifier, unknown to the consumer so the client has to actually accept the token.
     * @param userId     Current user's id
     * @throws IOException
     * @throws ServletException
     * @see <a href="http://tools.ietf.org/html/rfc5849#section-2.2">Resource Owner Authorization</a>
     */
    void handleRequestAuthorisation(HttpServletRequest request, HttpServletResponse response, boolean authorised,
                                    String token, String verifier, String userId)
            throws IOException, ServletException;

    /**
     * Checks if OAuth is enabled.
     * @return <code>true</code> if the OAuth service is enabled.
     */
    boolean isEnabled();
}
