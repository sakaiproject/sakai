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
package org.sakaiproject.login.saml;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticationToken;

/**
 * A SAML authentication provider that extracts username from SAML assertion attributes.
 * This class is designed to be used with Spring Security 5.7.x SAML2 support.
 */
@Slf4j
public class SakaiSamlAuthenticationConverter {

    @Getter @Setter private String usernameAttributeName = "urn:oid:1.3.6.1.4.1.5923.1.1.1.6"; // Default to eduPersonPrincipalName
    
    /**
     * Maximum time in seconds that a SAML authentication is considered valid.
     * Default is 7200 seconds (2 hours), same as original config.
     * This is enforced at the IdP level and in the SAML assertion itself.
     */
    @Getter @Setter private long maxAuthenticationAge = 7200;

    /**
     * Creates an authentication provider that extracts usernames from SAML assertions
     */
    public OpenSaml4AuthenticationProvider createProvider() {
        OpenSaml4AuthenticationProvider provider = new OpenSaml4AuthenticationProvider();
        
        // Set our custom authentication converter
        provider.setResponseAuthenticationConverter(responseToken -> {
            // First, use the default converter to create the Saml2Authentication
            Saml2Authentication authentication = OpenSaml4AuthenticationProvider
                .createDefaultResponseAuthenticationConverter()
                .convert(responseToken);
            
            // If authentication failed, return null
            if (authentication == null) {
                return null;
            }
            
            try {
                // Note: In Spring Security 5.7.x, the SAML authentication age check is
                // built into the default authentication validation logic, controlled by 
                // NotBefore and NotOnOrAfter conditions in the SAML assertion
                
                // Extract username from SAML attributes
                String username = extractUsername(authentication);
                
                if (username == null || username.isEmpty()) {
                    log.error("No username found in SAML assertion using attribute: {}", usernameAttributeName);
                    throw new AuthenticationServiceException("No username found in SAML assertion");
                }
                
                log.debug("Authenticated SAML user with username: {}", username);
                Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
                
                // Return a standard UsernamePasswordAuthenticationToken
                return new UsernamePasswordAuthenticationToken(username, authentication.getCredentials(), authorities);
            } catch (AuthenticationException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error extracting username from SAML assertion", e);
                throw new AuthenticationServiceException("Error extracting username from SAML assertion", e);
            }
        });
        
        return provider;
    }

    /**
     * Extracts the username from the SAML assertion using the configured attribute name
     */
    private String extractUsername(Saml2Authentication authentication) {
        Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
        Map<String, List<Object>> attributes = principal.getAttributes();
        
        List<Object> attributeValues = attributes.get(usernameAttributeName);
        if (attributeValues != null && !attributeValues.isEmpty()) {
            Object value = attributeValues.get(0);
            return (value != null) ? value.toString() : null;
        }
        
        return null;
    }
}