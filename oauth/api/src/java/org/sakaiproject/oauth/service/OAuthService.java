/*
 * #%L
 * OAuth API
 * %%
 * Copyright (C) 2009 - 2013 Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.service;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.oauth.domain.Accessor;
import org.sakaiproject.oauth.domain.Consumer;

import java.util.Collection;

/**
 * OAuthService handles OAuth operations without interacting with HTTP data.
 *
 * @author Colin Hebert
 */
public interface OAuthService {
    /**
     * Callback used when the client is unable to receive callbacks
     * or a callback URI has been established via other means.
     *
     * @see <a href="http://tools.ietf.org/html/rfc5849#section-2.1">Temporary credentials</a>
     */
    String OUT_OF_BAND_CALLBACK = "oob";

    /**
     * Get the valid accessor using the given token.
     * <p>
     * If the accessor doesn't exist, or the token isn't valid an exception is thrown.
     * </p>
     * <p>
     * getAccessor is expected to return an accessor with a specific type.<br />
     * If an accessor is found but the type doesn't match the expected type, an exception is thrown.
     * </p>
     *
     * @param oAuthToken   Token associated to the accessor
     * @param expectedType Expected accessor's type
     * @return the matching accessor
     */
    Accessor getAccessor(String oAuthToken, Accessor.Type expectedType);

    /**
     * Get the security advisor associated with an access accessor.
     * <p>
     * Each accessor can define its own set of rules to allow access to certain protected resources.
     * </p>
     *
     * @param accessorId token of the said accessor
     * @return A security advisor allowing access to some protected resources
     */
    SecurityAdvisor getSecurityAdvisor(String accessorId);

    /**
     * Get a consumer given its key (id).
     *
     * @param consumerKey identifier of the consumer
     * @return the requested consumer
     * @throws org.sakaiproject.oauth.exception.InvalidConsumerException
     *          if the consumer doesn't exists or isn't valid
     */
    Consumer getConsumer(String consumerKey);

    /**
     * Create a request accessor.
     * <p>
     * The created accessor's type is {@link Accessor.Type#REQUEST}
     * </p>
     *
     * @param consumerId     consumer associated to the accessor
     * @param callback       callback used when the authorisation is done
     * @param accessorSecret optional variable accessor secret
     *                       <a href="http://wiki.oauth.net/w/page/12238502/AccessorSecret">AccessorSecret</a>
     * @return the new request accessor
     * @see <a href="http://tools.ietf.org/html/rfc5849#section-2.1">Temporary credentials</a>
     */
    Accessor createRequestAccessor(String consumerId, String callback, String accessorSecret);

    /**
     * Start the authorisation process.
     * <p>
     * Changes the accessor's type to {@link Accessor.Type#REQUEST_AUTHORISING}
     * </p>
     *
     * @param accessorId access accessor used for the autorisation process
     * @return the request accessor used for the authorisation
     */
    Accessor startAuthorisation(String accessorId);

    /**
     * Finish the authorisation process when the client has authorised the consumer to connect.
     * <p>
     * Changes the accessor's type to {@link Accessor.Type#REQUEST_AUTHORISED}<br/>
     * Sets the verifier.
     * </p>
     *
     * @param accessorId Request token
     * @param verifier   Optional verifier (not a part of OAuth)
     * @param userId     User accepting the connection
     * @return the request accessor used for the authorisation
     * @see <a href="http://tools.ietf.org/html/rfc5849#section-2.2">Resource owner authorization</a>
     */
    Accessor authoriseAccessor(String accessorId, String verifier, String userId);

    /**
     * Create an accessor allowing to access protected resources.
     *
     * @param requestAccessorId the allowed request accessor
     * @return a new Accessor allowed to access protected resources
     * @see <a href="http://tools.ietf.org/html/rfc5849#section-2.3">Token credentials</a>
     */
    Accessor createAccessAccessor(String requestAccessorId);

    /**
     * Get a collection of every valid access accessors for one user.
     *
     * @param userId unique identifier of a Sakai user
     * @return a collection of {@link Accessor.Status#VALID} accessors
     */
    Collection<Accessor> getAccessAccessorForUser(String userId);

    /**
     * Manually revoke an accessor.
     *
     * @param accessorId accessor's token
     */
    void revokeAccessor(String accessorId);

    /**
     * Finish the authorisation process when the client has NOT authorised the consumer to connect.
     *
     * @param accessorId accessor's token
     */
    void denyRequestAccessor(String accessorId);
}
