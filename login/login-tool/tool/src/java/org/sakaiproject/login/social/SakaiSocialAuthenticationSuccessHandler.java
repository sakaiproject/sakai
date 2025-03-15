/**
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.login.social;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * Authentication success handler for social login integration.
 * Responsible for mapping the social user to a Sakai user account and establishing the session.
 */
@Slf4j
public class SakaiSocialAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private UsageSessionService usageSessionService;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private SocialAuthenticationService socialAuthenticationService;
    
    private static final String CONTAINER_SUCCESS_URL = "/portal";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
            throws IOException, ServletException {
        
        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.warn("Authentication is not an OAuth2AuthenticationToken");
            redirectToPortal(response);
            return;
        }
        
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String providerId = token.getAuthorizedClientRegistrationId();
        
        if (!socialAuthenticationService.isProviderEnabled(providerId)) {
            log.warn("Social authentication provider {} is not enabled", providerId);
            handleLoginFailure(request, response, "Provider not enabled");
            return;
        }
        
        OAuth2User oauth2User = token.getPrincipal();
        User sakaiUser = mapSocialUserToSakaiUser(oauth2User, providerId);
        
        if (sakaiUser == null) {
            log.warn("Could not map social user to Sakai user from provider: {}", providerId);
            handleLoginFailure(request, response, "Could not map user from " + providerId);
            return;
        }
        
        String sakaiUserId = sakaiUser.getId();
        establishSakaiSession(sakaiUserId, request);
        
        // Redirect to portal
        redirectToPortal(response);
    }
    
    /**
     * Map the social user to a Sakai user
     * 
     * @param oauth2User The OAuth2 user from the provider
     * @param providerId The provider ID (google, microsoft, etc.)
     * @return The mapped Sakai user, or null if mapping failed
     */
    private User mapSocialUserToSakaiUser(OAuth2User oauth2User, String providerId) {
        String userIdAttribute = socialAuthenticationService.getUserIdAttributeName(providerId);
        if (userIdAttribute == null) {
            log.warn("No user ID attribute configured for provider: {}", providerId);
            return null;
        }
        
        Map<String, Object> attributes = oauth2User.getAttributes();
        if (attributes == null) {
            log.warn("OAuth2User attributes are null for provider: {}", providerId);
            return null;
        }
        
        Object userIdValue = attributes.get(userIdAttribute);
        if (userIdValue == null) {
            log.warn("No value found for attribute {} in provider: {}", userIdAttribute, providerId);
            return null;
        }
        
        String userEmail = userIdValue.toString();
        
        // Try to find user by email
        try {
            User user = userDirectoryService.getUserByEid(userEmail);
            if (user != null) {
                log.debug("Found user by email: {}", userEmail);
                return user;
            }
        } catch (UserNotDefinedException e) {
            log.debug("User not found by email: {}", userEmail);
        }
        
        // User not found, check if auto-provisioning is enabled
        boolean autoProvision = serverConfigurationService.getBoolean(
            "social.authentication.provider." + providerId + ".autoprovision", false);
        
        if (!autoProvision) {
            log.debug("Auto-provisioning is disabled for provider: {}", providerId);
            return null;
        }
        
        // Auto-provisioning logic would go here
        // For now, just return null as this is a complex topic that depends on institutional policies
        return null;
    }
    
    /**
     * Establish a Sakai session for the authenticated user
     * 
     * @param userId The Sakai user ID
     * @param request The servlet request
     */
    private void establishSakaiSession(String userId, HttpServletRequest request) {
        // Get the Sakai session
        Session session = sessionManager.getCurrentSession();
        
        // Clear any existing user from the session
        if (session.getUserId() != null) {
            session.clear();
        }
        
        // Establish the user
        boolean loginSuccess = usageSessionService.login(
            userId, userId, request.getRemoteAddr(), request.getHeader("user-agent"), "user.login.oauth2");
        
        if (loginSuccess) {
            log.debug("Social login successful for user: {}", userId);
        } else {
            log.warn("Failed to establish Sakai session for user: {}", userId);
        }
    }
    
    /**
     * Handle login failure by redirecting to the login page with an error message
     * 
     * @param request The servlet request
     * @param response The servlet response
     * @param errorMessage The error message
     * @throws IOException If an I/O error occurs
     */
    private void handleLoginFailure(HttpServletRequest request, HttpServletResponse response, String errorMessage) 
            throws IOException {
        HttpSession session = request.getSession(true);
        session.setAttribute("socialLoginError", errorMessage);
        response.sendRedirect("/portal/xlogin?socialAuthError=true");
    }
    
    /**
     * Redirect to the portal
     * 
     * @param response The servlet response
     * @throws IOException If an I/O error occurs
     */
    private void redirectToPortal(HttpServletResponse response) throws IOException {
        response.sendRedirect(CONTAINER_SUCCESS_URL);
    }
}