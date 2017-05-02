/**********************************************************************************
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 * Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.rubrics.security;

import org.sakaiproject.rubrics.RubricsConfiguration;
import org.sakaiproject.rubrics.security.exception.JwtTokenMalformedException;
import org.sakaiproject.rubrics.security.model.AuthenticatedRequestContext;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.rubrics.security.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -3301605591108950415L;
    private final Log logger = LogFactory.getLog(this.getClass());

    private static final String JWT_ISSUER = "sakai";
    private static final String JWT_AUDIENCE = "rubrics";
    private static final String JWT_CUSTOM_CLAIM_TOOL_ID = "toolId";
    private static final String JWT_CUSTOM_CLAIM_SESSION_ID = "sessionId";
    private static final String JWT_CUSTOM_CLAIM_ROLES = "roles";
    private static final String JWT_CUSTOM_CLAIM_CONTEXT_ID = "contextId";
    private static final String JWT_CUSTOM_CLAIM_CONTEXT_TYPE = "contextType";

    @Autowired
    RubricsConfiguration rubricsConfiguration;

    private JWT decodeToken(String token) {

        JWT jwt = null;

        try {

            jwt = JWT.decode(token);

            // First verify it
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(
                    rubricsConfiguration.getIntegration().getTokenSecret()))
                    .build(); //Reusable verifier instance
            verifier.verify(token);

        } catch (UnsupportedEncodingException | JWTVerificationException e) {
            // If expired, check if the session is still live in the sakai system
            // we can do this because the first exception to be launched is the signature verification failure
            // So if the exception is only about token expiring we can be sure the token was a correct one.
            if (!(e.getMessage().startsWith("The Token has expired on")
                    && isSakaiSessionStillValid(jwt.getClaim("sessionId").asString()))) {
                throw new JwtTokenMalformedException(String.format("Error occurred while decoding access token '%s'", token), e);
            }
        }

        // Manually verify audience and issuer since we are using the validation error flow to allow for time
        // extensions - in lieu of just specifying withAudience(JWT_AUDIENCE).withIssuer(JWT_ISSUER) to the Verifier
        if (!jwt.getAudience().contains(JWT_AUDIENCE)) {
            throw new JwtTokenMalformedException(String.format("Access token denied for audience. Expected: ['%s'], " +
                    "Provided: %s, Token: %s", JWT_AUDIENCE, jwt.getAudience().toString(), token));
        }
        if (!jwt.getIssuer().contentEquals(JWT_ISSUER)) {
            throw new JwtTokenMalformedException(String.format("Access token denied for issuer. Expected: ['%s'], " +
                    "Provided: %s, Token: %s", JWT_ISSUER, jwt.getIssuer().toString(), token));
        }

        return jwt;
    }

    public AuthenticatedRequestContext getAuthenticatedUser(String token) {
        try {

            JWT jwt = decodeToken(token);
            AuthenticatedRequestContext context = new AuthenticatedRequestContext(jwt.getSubject(),
                    jwt.getClaim(JWT_CUSTOM_CLAIM_TOOL_ID).asString(),
                    jwt.getClaim(JWT_CUSTOM_CLAIM_CONTEXT_ID).asString(),
                    jwt.getClaim(JWT_CUSTOM_CLAIM_CONTEXT_TYPE).asString());

            List<String> roles = jwt.getClaim(JWT_CUSTOM_CLAIM_ROLES).asList(String.class);
            for (String role : roles) {
                context.addAuthority(new SimpleGrantedAuthority(Role.fromPermissionKey(role).name()));
            }
            if (context.getAuthorities().size() == 0) {
                throw new JwtTokenMalformedException(String.format("Access token '%s' does not contain any roles", token));
            }
            return context;

        } catch (Exception e) {
            throw new JwtTokenMalformedException(String.format(String.format("Error occurred while authenticating " +
                    "the user for token %s", token, e)));
        }
    }

    private boolean isSakaiSessionStillValid(String session) {
        try {

            URL url = new URL(rubricsConfiguration.getIntegration().getSakaiRestUrl() + "sakai/checkSession?sessionid=" + session);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output = br.readLine();
            conn.disconnect();

            if (output != null) {
                return (output.trim().equals(session.trim()));
            } else {
                return false;
            }


        } catch (MalformedURLException e) {

            logger.debug("Error getting a rubric association " + e.getMessage());
            return false;

        } catch (IOException e) {

            logger.debug("Error getting a rubric association" + e.getMessage());
            return false;
        }
    }

}
