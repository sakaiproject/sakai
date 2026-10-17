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
package org.sakaiproject.tool.assessment.ui.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import org.junit.Test;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** XML and include-graph checks run by Samigo's normal Maven test phase. */
public class SamigoJspXmlTest {
    private static final Path WEBAPP = Path.of("src/webapp").toAbsolutePath().normalize();
    private static final String JSP_NAMESPACE = "http://java.sun.com/JSP/Page";

    @Test
    public void allViewsAreNamespaceWellFormedXmlWithExistingIncludes() throws Exception {
        List<Path> files;
        try (Stream<Path> paths = Files.walk(WEBAPP)) {
            files = paths.filter(Files::isRegularFile).toList();
        }
        assertFalse("Views must retain their JSP filenames", files.stream().anyMatch(path -> path.toString().endsWith(".jspx")));
        List<Path> views = files.stream().filter(path -> path.toString().endsWith(".jsp")).toList();
        assertFalse("Must validate actual JSP views", views.isEmpty());
        DocumentBuilder builder = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder();
        for (Path view : views) {
            Document document = builder.parse(view.toFile());
            assertEquals(view.toString(), JSP_NAMESPACE, document.getDocumentElement().getNamespaceURI());
            assertEquals(view.toString(), "root", document.getDocumentElement().getLocalName());
            assertNoBlankTemplateBlocks(document, view);
            NodeList includes = document.getElementsByTagNameNS(JSP_NAMESPACE, "directive.include");
            for (int i = 0; i < includes.getLength(); i++) {
                String target = ((Element) includes.item(i)).getAttribute("file");
                Path included = target.startsWith("/") ? WEBAPP.resolve(target.substring(1)) : view.getParent().resolve(target);
                included = included.normalize();
                assertTrue(view + " includes a missing or external file: " + target,
                    included.startsWith(WEBAPP) && Files.isRegularFile(included));
            }
        }
    }

    private static void assertNoBlankTemplateBlocks(Node node, Path view) {
        if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
            assertFalse(view + " contains a redundant blank CDATA section", node.getNodeValue().isBlank());
        }
        if (node.getNodeType() == Node.ELEMENT_NODE && JSP_NAMESPACE.equals(node.getNamespaceURI())
                && "text".equals(node.getLocalName())) {
            String text = node.getTextContent();
            assertFalse(view + " contains a newline-only template block",
                text.isBlank() && (text.contains("\n") || text.contains("\r")));
        }
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            assertNoBlankTemplateBlocks(child, view);
        }
    }

    @Test
    public void navigationReferencesExistingJspViews() throws Exception {
        Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
            .parse(WEBAPP.resolve("WEB-INF/faces-config.xml").toFile());
        for (String name : List.of("from-view-id", "to-view-id")) {
            NodeList views = document.getElementsByTagName(name);
            for (int i = 0; i < views.getLength(); i++) {
                String view = views.item(i).getTextContent().trim();
                // Sakai helper-tool destinations are dispatched outside JSF.
                if (view.contains("*") || !view.startsWith("/jsf/")) {
                    continue;
                }
                assertTrue("Navigation must target JSP: " + view, view.startsWith("/") && view.endsWith(".jsp"));
                assertTrue("Navigation target must exist: " + view, Files.isRegularFile(WEBAPP.resolve(view.substring(1))));
            }
        }
    }

    @Test
    public void jspFilenamesUseXmlSyntaxWithoutAddingThePreludeTwice() throws Exception {
        Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder()
            .parse(WEBAPP.resolve("WEB-INF/web.xml").toFile());
        NodeList groups = document.getElementsByTagName("jsp-property-group");
        boolean configured = false;
        for (int i = 0; i < groups.getLength(); i++) {
            Element group = (Element) groups.item(i);
            NodeList patterns = group.getElementsByTagName("url-pattern");
            for (int j = 0; j < patterns.getLength(); j++) {
                if (patterns.item(j).getTextContent().trim().equals("*.jsp")) {
                    NodeList xml = group.getElementsByTagName("is-xml");
                    assertEquals("JSP views must use XML syntax", 1, xml.getLength());
                    assertEquals("true", xml.item(0).getTextContent().trim());
                    assertEquals("Converted pages already include their header explicitly", 0,
                        group.getElementsByTagName("include-prelude").getLength());
                    configured = true;
                }
            }
        }
        assertTrue("XML syntax must be configured for the retained filenames", configured);
    }
}
