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
package org.sakaiproject.oauth.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Plain OAuth accessor.
 *
 * @author Colin Hebert
 */
public class Accessor implements Serializable {
    /**
     * Unique token associated with the accessor.
     */
    private String token;
    /**
     * Type of the accessor (request or access) during every stage of the authentication.
     */
    private Type type;
    /**
     * Status of the accessor (valid or not).
     */
    private Status status;
    /**
     * Verifier associated with the accessor, used during the authorisation phase.
     */
    private String verifier;
    /**
     * Token's secret.
     * <p>
     * Be careful, this is not the accessor's secret defined in http://wiki.oauth.net/w/page/12238502/AccessorSecret
     * </p>
     */
    private String secret;
    /**
     * Consumer (client) requiring an OAuth token.
     */
    private String consumerId;
    /**
     * Date when the accessor has been created (should not change).
     */
    private Date creationDate;
    /**
     * Date when the accessor will expire, if set to null, the accessor doesn't expire.
     */
    private Date expirationDate;
    /**
     * URL used by a request token to send the user back on the web site he's coming from and validate his token.
     */
    private String callbackUrl;
    /**
     * User's id, only available in access accessors and authorised request accessors.
     */
    private String userId;
    /**
     * Variable accessor secret as defined in http://wiki.oauth.net/w/page/12238502/AccessorSecret.
     * <p>
     * Should be only set on request accessors, if null OAuth will use either the accessor secret or the consumer secret
     * </p>
     */
    private String accessorSecret;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getVerifier() {
        return verifier;
    }

    public void setVerifier(String verifier) {
        this.verifier = verifier;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String consumerId) {
        this.consumerId = consumerId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAccessorSecret() {
        return accessorSecret;
    }

    public void setAccessorSecret(String accessorSecret) {
        this.accessorSecret = accessorSecret;
    }

    @Override
    public String toString() {
        return "Accessor{"
                + "token='" + token + '\''
                + ", type=" + type
                + ", status=" + status
                + ", consumerId='" + consumerId + '\''
                + ", expirationDate=" + expirationDate
                + ", userId='" + userId + '\''
                + '}';
    }

    /**
     * Types of accessors available.
     */
    public static enum Type {
        /**
         * Request accessor, used to initiate the authentication.
         */
        REQUEST,
        /**
         * Request accessor, during the authorisation phase.
         */
        REQUEST_AUTHORISING,
        /**
         * Authorised request accessor, associated with a user who accepted the connection.
         */
        REQUEST_AUTHORISED,
        /**
         * Access accessor, used to access to protected resources.
         */
        ACCESS
    }

    /**
     * Possible status for a token.
     */
    public static enum Status {
        /**
         * Valid token, can be used either to access resources or authenticate an user.
         */
        VALID,
        /**
         * Invalid token which has either already been used (request token) or revoked by the user.
         */
        REVOKED,
        /**
         * Invalid token which hasn't been used during the validity period.
         */
        EXPIRED
    }
}
