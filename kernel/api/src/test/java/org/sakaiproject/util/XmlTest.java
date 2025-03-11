/*
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
package org.sakaiproject.util;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlTest {

    private static final String TEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<root>\n" +
            "    <child attribute=\"value\">content</child>\n" +
            "</root>";

    private File tempFile;

    @Before
    public void setUp() throws IOException {
        // Create a temporary file for testing
        tempFile = File.createTempFile("xml-test", ".xml");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(TEST_XML);
        }
    }

    @Test
    public void testCreateDocument() {
        Document doc = Xml.createDocument();
        assertNotNull("Document should be created", doc);
    }

    @Test
    public void testReadDocument() {
        Document doc = Xml.readDocument(tempFile.getPath());
        assertNotNull("Document should be read from file", doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    public void testReadDocumentFromString() {
        Document doc = Xml.readDocumentFromString(TEST_XML);
        assertNotNull("Document should be read from string", doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    public void testProcessString() throws SAXException, IOException {
        final StringBuilder result = new StringBuilder();
        DefaultHandler handler = new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) {
                result.append(ch, start, length);
            }
        };
        
        Xml.processString(TEST_XML, handler);
        assertTrue("Content should be processed", result.toString().contains("content"));
    }

    @Test
    public void testReadDocumentFromStream() {
        ByteArrayInputStream stream = new ByteArrayInputStream(
            TEST_XML.getBytes(StandardCharsets.UTF_8));
        Document doc = Xml.readDocumentFromStream(stream);
        assertNotNull("Document should be read from stream", doc);
        assertEquals("root", doc.getDocumentElement().getTagName());
    }

    @Test
    public void testWriteDocumentToString() {
        Document doc = Xml.readDocumentFromString(TEST_XML);
        String result = Xml.writeDocumentToString(doc);
        assertNotNull("Document should be written to string", result);
        assertTrue("Output should contain XML content", result.contains("<root>"));
    }

    @Test
    public void testEncodeDecodeAttribute() {
        Document doc = Xml.createDocument();
        Element element = doc.createElement("test");
        String originalValue = "Test Value with special chars: &<>";
        
        Xml.encodeAttribute(element, "testAttr", originalValue);
        String decodedValue = Xml.decodeAttribute(element, "testAttr");
        
        assertEquals("Value should be preserved after encode/decode", 
            originalValue, decodedValue);
    }

    @Test
    public void testPropertiesXmlConversion() {
        Properties original = new Properties();
        original.setProperty("key1", "value1");
        original.setProperty("key2", "value2");

        Document doc = Xml.createDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);

        Properties result = new Properties();
        Element propsElement = Xml.propertiesToXml(original, doc, new Stack<Element>() {{ push(root); }});
        Xml.xmlToProperties(result, propsElement);

        assertEquals("Properties should match after conversion", 
            original.getProperty("key1"), result.getProperty("key1"));
        assertEquals("Properties should match after conversion", 
            original.getProperty("key2"), result.getProperty("key2"));
    }

    @Test
    public void testNodeToString() {
        Document doc = Xml.readDocumentFromString(TEST_XML);
        String result = Xml.nodeToString(doc.getDocumentElement());
        assertNotNull("Node string representation should not be null", result);
        assertTrue("Should contain node type", result.contains("Node Type: Element"));
        assertTrue("Should contain node name", result.contains("Name: root"));
    }
}
