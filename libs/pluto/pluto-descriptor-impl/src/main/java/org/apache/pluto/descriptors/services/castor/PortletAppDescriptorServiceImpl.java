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
package org.apache.pluto.descriptors.services.castor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.pluto.descriptors.common.DescriptionDD;
import org.apache.pluto.descriptors.common.DisplayNameDD;
import org.apache.pluto.descriptors.common.InitParamDD;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.PortletInfoDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
import org.apache.pluto.descriptors.portlet.UserAttributeDD;
import org.apache.pluto.descriptors.services.PortletAppDescriptorService;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Reads the Portlet 1.0 descriptor elements used by Sakai's bundled portlets.
 * The class name is retained because Pluto loads it from its own configuration.
 * XML parsing uses Sakai's secure XML utility rather than Castor.
 */
@SuppressWarnings("unchecked") // Pluto 1.1.7 exposes raw descriptor collections.
public class PortletAppDescriptorServiceImpl implements PortletAppDescriptorService {

    private static final String NAMESPACE = "http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd";

    @Override
    public PortletAppDD read(InputStream input) throws IOException {
        if (input == null) {
            throw new IOException("Missing /WEB-INF/portlet.xml");
        }
        try (InputStream stream = input) {
            Element root = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
                    .parse(stream).getDocumentElement();
            if (!"portlet-app".equals(root.getLocalName()) || !NAMESPACE.equals(root.getNamespaceURI())
                    || !"1.0".equals(root.getAttribute("version"))) {
                throw new IOException("Expected a Portlet 1.0 portlet-app descriptor");
            }
            PortletAppDD app = new PortletAppDD();
            app.setVersion("1.0");
            for (Element child : children(root, "portlet", "user-attribute")) {
                if ("portlet".equals(child.getLocalName())) {
                    app.getPortlets().add(readPortlet(child));
                } else {
                    UserAttributeDD attribute = new UserAttributeDD();
                    for (Element field : children(child, "name", "description")) {
                        if ("name".equals(field.getLocalName())) {
                            attribute.setName(text(field));
                        } else {
                            attribute.getDescriptions().add(readDescription(field));
                        }
                    }
                    app.getUserAttributes().add(attribute);
                }
            }
            return app;
        } catch (ParserConfigurationException | SAXException | NumberFormatException e) {
            throw new IOException("Unable to read /WEB-INF/portlet.xml", e);
        }
    }

    private PortletDD readPortlet(Element element) throws IOException {
        PortletDD portlet = new PortletDD();
        for (Element child : children(element, "portlet-name", "display-name", "description", "portlet-class",
                "init-param", "expiration-cache", "supports", "supported-locale", "resource-bundle", "portlet-info")) {
            switch (child.getLocalName()) {
                case "portlet-name": portlet.setPortletName(text(child)); break;
                case "portlet-class": portlet.setPortletClass(text(child)); break;
                case "expiration-cache": portlet.setExpirationCache(Integer.parseInt(text(child))); break;
                case "supported-locale": portlet.getSupportedLocales().add(text(child)); break;
                case "resource-bundle": portlet.setResourceBundle(text(child)); break;
                case "description": portlet.getDescriptions().add(readDescription(child)); break;
                case "display-name":
                    DisplayNameDD displayName = new DisplayNameDD();
                    displayName.setDisplayName(text(child));
                    if (child.hasAttributeNS(XMLConstants.XML_NS_URI, "lang")) {
                        displayName.setLang(child.getAttributeNS(XMLConstants.XML_NS_URI, "lang"));
                    }
                    portlet.getDisplayNames().add(displayName);
                    break;
                case "init-param":
                    InitParamDD parameter = new InitParamDD();
                    for (Element field : children(child, "name", "value", "description")) {
                        switch (field.getLocalName()) {
                            case "name": parameter.setParamName(text(field)); break;
                            case "value": parameter.setParamValue(text(field)); break;
                            case "description": parameter.getDescriptions().add(readDescription(field)); break;
                        }
                    }
                    portlet.getInitParams().add(parameter);
                    break;
                case "supports":
                    SupportsDD supports = new SupportsDD();
                    for (Element field : children(child, "mime-type", "portlet-mode")) {
                        if ("mime-type".equals(field.getLocalName())) {
                            supports.setMimeType(text(field));
                        } else {
                            supports.getPortletModes().add(text(field));
                        }
                    }
                    portlet.getSupports().add(supports);
                    break;
                case "portlet-info":
                    PortletInfoDD info = new PortletInfoDD();
                    for (Element field : children(child, "title", "short-title", "keywords")) {
                        switch (field.getLocalName()) {
                            case "title": info.setTitle(text(field)); break;
                            case "short-title": info.setShortTitle(text(field)); break;
                            case "keywords": info.setKeywords(text(field)); break;
                        }
                    }
                    portlet.setPortletInfo(info);
                    break;
            }
        }
        if (portlet.getPortletName() == null || portlet.getPortletClass() == null) {
            throw new IOException("portlet requires portlet-name and portlet-class");
        }
        return portlet;
    }

    private DescriptionDD readDescription(Element element) throws IOException {
        DescriptionDD description = new DescriptionDD();
        description.setDescription(text(element));
        if (element.hasAttributeNS(XMLConstants.XML_NS_URI, "lang")) {
            description.setLang(element.getAttributeNS(XMLConstants.XML_NS_URI, "lang"));
        }
        return description;
    }

    private List<Element> children(Element parent, String... allowedNames) throws IOException {
        Set<String> allowed = Set.of(allowedNames);
        List<Element> elements = new ArrayList<>();
        for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element) {
                Element element = (Element) node;
                if (!NAMESPACE.equals(element.getNamespaceURI()) || !allowed.contains(element.getLocalName())) {
                    throw new IOException("Unsupported portlet.xml element " + element.getTagName()
                            + " in " + parent.getTagName());
                }
                elements.add(element);
            }
        }
        return elements;
    }

    private String text(Element element) throws IOException {
        children(element);
        return element.getTextContent().trim();
    }

}
