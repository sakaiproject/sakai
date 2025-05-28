/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.login.filter;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.pac4j.core.config.Config;
import org.pac4j.core.profile.CommonProfile;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * PAC4J authentication provider for Spring Security integration.
 * Processes PAC4J authentication tokens and creates Spring Security authentication objects.
 */
@Slf4j
public class SakaiPac4jAuthenticationProvider implements AuthenticationProvider {

    @Setter private Config config;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;
            
            // Extract PAC4J profile from the token
            Object credentials = token.getCredentials();
            if (credentials instanceof CommonProfile) {
                CommonProfile profile = (CommonProfile) credentials;
                
                log.debug("Authenticating user with PAC4J profile: {}", profile.getId());
                
                // Create authorities based on the profile
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                
                // Add any additional roles from the profile
                if (profile.getRoles() != null) {
                    for (String role : profile.getRoles()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                    }
                }
                
                // Create authenticated token
                PreAuthenticatedAuthenticationToken result = new PreAuthenticatedAuthenticationToken(
                    profile.getId(), profile, authorities);
                result.setDetails(token.getDetails());
                
                log.debug("Successfully authenticated user: {} with authorities: {}", 
                         profile.getId(), authorities);
                
                return result;
            }
        }
        
        log.debug("Authentication token not supported: {}", 
                 authentication != null ? authentication.getClass().getName() : "null");
        throw new BadCredentialsException("PAC4J authentication failed");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}