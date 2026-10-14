/*
 * Copyright (c) 2026 The Apereo Foundation
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
package org.apache.pluto.descriptors.services;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import org.apache.pluto.core.PortletContextManager;
import org.apache.pluto.descriptors.common.DescriptionDD;
import org.apache.pluto.descriptors.common.DisplayNameDD;
import org.apache.pluto.descriptors.common.InitParamDD;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
import org.apache.pluto.descriptors.portlet.UserAttributeDD;
import org.apache.pluto.descriptors.services.PortletAppDescriptorService;
import org.apache.pluto.descriptors.services.castor.PortletAppDescriptorServiceImpl;
import org.apache.pluto.internal.InternalPortletContext;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

public class PortletAppDescriptorServiceTest {

    private final PortletAppDescriptorService service = new PortletAppDescriptorServiceImpl();

    @Test
    public void plutoRegistersAllBundledDescriptorsWithoutCastor() throws Exception {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("org.exolab.castor.xml.Unmarshaller"));
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("org.apache.pluto.descriptors.services.castor.WebAppDescriptorServiceImpl"));
        String[][] applications = {
            {"web/web-portlet", "PortletIFrame", "Sakai IFrame Portlet (JSR-168)", "IFrame Portlet"},
            {"lti/web-ifp", "SakaiIFrame", "Sakai IFrame Portlet (JSR-168)", "IFrame Portlet"},
            {"lti/lti-portlet", "IMSLTIPortlet", "IMS Learning Tool Interoperability", "IMS LTI"}
        };
        PortletContextManager manager = PortletContextManager.getManager();
        for (String[] application : applications) {
            Path descriptor = Path.of("../../../", application[0], "src/webapp/WEB-INF/portlet.xml");
            MockServletContext servletContext = new MockServletContext(
                    "file:" + descriptor.getParent().getParent().toAbsolutePath(), new DefaultResourceLoader());
            servletContext.setContextPath("/" + application[1]);
            servletContext.setServletContextName(application[1]);
            String applicationId = manager.register(new MockServletConfig(servletContext, application[1]));
            InternalPortletContext registered = (InternalPortletContext) manager.getPortletContext(applicationId);
            try {
                PortletAppDD app = registered.getPortletApplicationDefinition();
                assertEquals("1.0", app.getVersion());
                assertEquals(1, app.getPortlets().size());
                PortletDD portlet = manager.getPortletDescriptor(registered.getApplicationId(), application[1]);
                assertNotNull(portlet);
                assertEquals(application[1], portlet.getPortletName());
                assertEquals("org.sakaiproject.portlets." + application[1], portlet.getPortletClass());
                assertEquals(60, portlet.getExpirationCache());
                assertEquals(List.of("en"), portlet.getSupportedLocales());
                DisplayNameDD display = (DisplayNameDD) portlet.getDisplayNames().get(0);
                assertEquals("en", display.getLang());
                assertEquals(application[1].equals("IMSLTIPortlet")
                        ? "IMS Learning Tool Interoperability" : "Sakai IFrame Portlet", display.getDisplayName());
                InitParamDD parameter = (InitParamDD) portlet.getInitParams().get(0);
                assertEquals("sakai.test.init.parm", parameter.getParamName());
                assertEquals("Hello from portlet.xml", parameter.getParamValue());
                assertEquals("Hello from portlet.xml", manager.getPortletConfig(applicationId, application[1])
                        .getInitParameter("sakai.test.init.parm"));
                SupportsDD supports = (SupportsDD) portlet.getSupports().get(0);
                assertEquals("text/html", supports.getMimeType());
                assertEquals(List.of("view", "edit"), supports.getPortletModes());
                assertEquals(application[2], portlet.getPortletInfo().getTitle());
                assertEquals(application[3], portlet.getPortletInfo().getShortTitle());
                assertEquals("Sakai", portlet.getPortletInfo().getKeywords());
                assertEquals(5, app.getUserAttributes().size());
                UserAttributeDD attribute = (UserAttributeDD) app.getUserAttributes().get(0);
                assertEquals("user.name.given", attribute.getName());
                DescriptionDD description = (DescriptionDD) attribute.getDescriptions().get(0);
                assertEquals("User Given Name", description.getDescription());
                assertEquals("en", description.getLang());
            } finally {
                manager.remove(registered);
            }
        }
    }

    @Test
    public void readsPrefixedNamespacesAndLocalizedUtf8Text() throws Exception {
        String xml = """
                <p:portlet-app xmlns:p="http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd" version="1.0">
                  <p:portlet>
                    <p:portlet-name>test</p:portlet-name><p:portlet-class>example.Portlet</p:portlet-class>
                    <p:display-name xml:lang="fr">Élève</p:display-name>
                    <p:description xml:lang="fr">Français</p:description>
                  </p:portlet>
                </p:portlet-app>
                """;
        PortletDD portlet = (PortletDD) service.read(input(xml)).getPortlets().get(0);
        DisplayNameDD display = (DisplayNameDD) portlet.getDisplayNames().get(0);
        assertEquals("Élève", display.getDisplayName());
        assertEquals("fr", display.getLang());
        DescriptionDD description = (DescriptionDD) portlet.getDescriptions().get(0);
        assertEquals("Français", description.getDescription());
        assertEquals("fr", description.getLang());
        assertEquals(PortletDD.EXPIRATION_CACHE_UNSET, portlet.getExpirationCache());
    }

    @Test
    public void rejectsUnsupportedElementsInsteadOfDroppingSettings() {
        IOException exception = assertThrows(IOException.class,
                () -> service.read(input(descriptor("<portlet-preferences/>"))));
        assertTrue(exception.getMessage().contains("portlet-preferences"));
        assertThrows(IOException.class, () -> service.read(input(descriptor("<supports><unknown/></supports>"))));
        assertThrows(IOException.class, () -> service.read(input(descriptor("<portlet-info><unknown/></portlet-info>"))));
        assertThrows(IOException.class, () -> service.read(input(descriptor("<portlet-name><unknown/></portlet-name>"))));
    }

    @Test
    public void rejectsMissingMalformedAndExternalEntityDescriptors() {
        assertThrows(IOException.class, () -> service.read(null));
        assertThrows(IOException.class, () -> service.read(input("<portlet-app>")));
        assertThrows(IOException.class, () -> service.read(input(descriptor("<expiration-cache>invalid</expiration-cache>"))));
        assertThrows(IOException.class, () -> service.read(input("""
                <!DOCTYPE portlet-app [<!ENTITY external SYSTEM "file:///should-not-be-read">]>
                """ + descriptor("<display-name>&external;</display-name>"))));
    }

    private String descriptor(String extra) {
        return "<portlet-app xmlns=\"http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd\" version=\"1.0\">"
                + "<portlet><portlet-name>test</portlet-name><portlet-class>example.Portlet</portlet-class>"
                + extra + "</portlet></portlet-app>";
    }

    private InputStream input(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }
}
