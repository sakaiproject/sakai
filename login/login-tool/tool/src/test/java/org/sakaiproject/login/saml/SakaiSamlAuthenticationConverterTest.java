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

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;

import static org.junit.Assert.*;

/**
 * Unit tests for SakaiSamlAuthenticationConverter.
 * These tests focus on configuration aspects that can be easily verified
 * without complex mocking of SAML-specific classes.
 */
public class SakaiSamlAuthenticationConverterTest {

    private SakaiSamlAuthenticationConverter converter;
    
    @Before
    public void setUp() {
        converter = new SakaiSamlAuthenticationConverter();
    }
    
    @Test
    public void testDefaultConfiguration() {
        // Test default values
        assertEquals("Default attribute should be ePPN", 
                     "urn:oid:1.3.6.1.4.1.5923.1.1.1.6", 
                     converter.getUsernameAttributeName());
        assertEquals("Default max auth age should be 2 hours (in seconds)", 
                     7200, 
                     converter.getMaxAuthenticationAge());
    }

    @Test
    public void testCustomConfiguration() {
        // Test setting custom values
        converter.setUsernameAttributeName("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn");
        converter.setMaxAuthenticationAge(3600);
        
        assertEquals("Username attribute should be updated", 
                     "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn", 
                     converter.getUsernameAttributeName());
        assertEquals("Max auth age should be updated", 
                     3600, 
                     converter.getMaxAuthenticationAge());
    }
    
    @Test
    public void testCreateProvider() {
        // Test that provider is created correctly
        OpenSaml4AuthenticationProvider provider = converter.createProvider();
        assertNotNull("Provider should not be null", provider);
        
        // In OpenSaml4AuthenticationProvider, the converter is private
        // We just verify the provider is created
    }
    
    @Test
    public void testUpnSamlAuthenticationConverter() {
        // Test the UPN subclass
        UpnSamlAuthenticationConverter upnConverter = new UpnSamlAuthenticationConverter();
        assertEquals("UPN converter should use the UPN attribute", 
                     "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/upn", 
                     upnConverter.getUsernameAttributeName());
        
        // The UPN converter should inherit all other behavior from the parent
        OpenSaml4AuthenticationProvider provider = upnConverter.createProvider();
        assertNotNull("UPN provider should not be null", provider);
    }
}