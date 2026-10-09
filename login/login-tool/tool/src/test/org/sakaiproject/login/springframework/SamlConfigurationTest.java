/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.login.springframework;

import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.beans.BeansException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/** Verify property activation through the login tool's production context. */
public class SamlConfigurationTest {
    @Test
    public void disabledSamlNeedsNoMetadataAndCreatesNoSecurityChain() {
        try (AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext(SamlTestConfiguration.class);
                XmlWebApplicationContext login = new XmlWebApplicationContext()) {
            ServerConfigurationService configuration = parent.getBean(ServerConfigurationService.class);
            when(configuration.getBoolean("saml.enabled", false)).thenReturn(false);
            when(configuration.getString("saml.idp.metadata", "")).thenReturn("");
            login.setParent(parent);
            login.setServletContext(new MockServletContext());
            login.setConfigLocation("file:src/webapp/WEB-INF/applicationContext.xml");
            login.refresh();
            assertFalse(login.containsBean("springSecurityFilterChain"));
        }
    }

    @Test
    public void enabledSamlRejectsMissingMetadataAtStartup() {
        try (AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext(SamlTestConfiguration.class);
                XmlWebApplicationContext login = new XmlWebApplicationContext()) {
            ServerConfigurationService configuration = parent.getBean(ServerConfigurationService.class);
            when(configuration.getString("saml.idp.metadata", "")).thenReturn("");
            login.setParent(parent);
            login.setServletContext(new MockServletContext());
            login.setConfigLocation("file:src/webapp/WEB-INF/applicationContext.xml");
            assertThrows(BeansException.class, login::refresh);
        }
    }
}
