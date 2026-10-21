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

import javax.xml.parsers.DocumentBuilderFactory;

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

    @Test(expected = SAXException.class)
    public void testProcessStringRejectsExternalEntity() throws Exception {
        Xml.processString("<!DOCTYPE root [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><root>&xxe;</root>", new DefaultHandler());
    }

    @Test(expected = SAXException.class)
    public void testSecureSaxReaderRejectsInternalDoctype() throws Exception {
        Xml.createSecureXMLReader(true).parse(new org.xml.sax.InputSource(new java.io.StringReader(
                "<!DOCTYPE root [<!ENTITY repeated 'value'>]><root>&repeated;</root>")));
    }

    @Test
    public void testSaxParserPreservesLargeSubmissionXml() throws Exception {
        StringBuilder xml = new StringBuilder("<submission");
        for (int i = 0; i < 40; i++) {
            xml.append(" submitter").append(i).append("='user'");
        }
        xml.append('>').append("<node>".repeat(26)).append("</node>".repeat(26)).append("</submission>");
        Xml.processString(xml.toString(), new DefaultHandler());
    }

    @Test
    public void testNativeStreamingFactoryDoesNotResolveExternalEntities() throws Exception {
        javax.xml.stream.XMLInputFactory factory = Xml.createSecureXMLInputFactory();
        javax.xml.stream.XMLStreamReader safe = factory.createXMLStreamReader(new java.io.StringReader("<root>safe</root>"));
        try {
            safe.nextTag();
            assertEquals("safe", safe.getElementText());
        } finally {
            safe.close();
        }
        java.nio.file.Path canary = java.nio.file.Files.createTempFile("xml-stream-canary", ".txt");
        String marker = "external-entity-canary";
        java.nio.file.Files.writeString(canary, marker);
        try {
            String xml = "<!DOCTYPE root [<!ENTITY external SYSTEM '" + canary.toUri() + "'>]><root>&external;</root>";
            javax.xml.stream.XMLStreamReader reader = factory.createXMLStreamReader(new java.io.StringReader(xml));
            StringBuilder content = new StringBuilder();
            try {
                while (reader.hasNext()) {
                    if (reader.next() == javax.xml.stream.XMLStreamConstants.CHARACTERS) {
                        content.append(reader.getText());
                    }
                }
            } catch (javax.xml.stream.XMLStreamException expected) {
                // Rejecting an unresolved entity is also safe.
            } finally {
                reader.close();
            }
            assertFalse(content.toString().contains(marker));
        } finally {
            java.nio.file.Files.deleteIfExists(canary);
        }
    }

    @Test
    public void testReadDocumentPreservesLegacyCdataNodes() {
        Document document = Xml.readDocumentFromString("<root><![CDATA[<p>archived content</p>]]></root>");
        assertNotNull(document);
        assertEquals(org.w3c.dom.Node.CDATA_SECTION_NODE, document.getDocumentElement().getFirstChild().getNodeType());
        assertEquals("<p>archived content</p>", document.getDocumentElement().getTextContent());
    }

    @Test
    public void testReadDocumentPreservesLegacyDoctypeWithoutExternalEntities() throws Exception {
        String xml = "<!DOCTYPE root SYSTEM 'file:///xml-test-missing.dtd' ["
            + "<!ENTITY external SYSTEM 'file:///xml-test-missing-entity'>]><root>&external;<child>value</child></root>";
        Document document = Xml.readDocumentFromString(xml);
        assertNotNull(document);
        assertEquals("value", document.getDocumentElement().getTextContent());
    }

    @Test
    public void testLegacyDoctypeParsersDoNotLoadExternalResources() throws Exception {
        String xml = "<!DOCTYPE root SYSTEM 'file:///xml-test-missing.dtd' ["
                + "<!ENTITY % remote SYSTEM 'file:///xml-test-missing-parameter.dtd'>%remote;"
                + "<!ENTITY external SYSTEM 'file:///xml-test-missing-entity'>]><root>&external;<child>value</child></root>";
        Document document = Xml.createSecureDocumentBuilderFactoryWithDoctype(true)
                .newDocumentBuilder().parse(toInputStream(xml));
        assertEquals("value", document.getDocumentElement().getTextContent());

        StringBuilder content = new StringBuilder();
        Xml.createSecureSAXParserWithDoctype(false).parse(toInputStream(xml), new DefaultHandler() {
            @Override
            public void characters(char[] ch, int start, int length) {
                content.append(ch, start, length);
            }
        });
        assertEquals("value", content.toString());
    }

    @Test(expected = SAXException.class)
    public void testSecureDomParserRejectsExcessiveAttributes() throws Exception {
        StringBuilder xml = new StringBuilder("<root");
        for (int i = 0; i < 31; i++) {
            xml.append(" a").append(i).append("='value'");
        }
        xml.append("/>");
        Xml.createSecureDocumentBuilderFactory().newDocumentBuilder().parse(toInputStream(xml.toString()));
    }

    @Test(expected = javax.xml.transform.TransformerException.class)
    public void testSecureTransformationRejectsDoctype() throws Exception {
        String xml = "<!DOCTYPE root [<!ENTITY entity 'value'>]><root>&entity;</root>";
        Xml.createSecureTransformerFactory().newTransformer().transform(
                Xml.createSecureSource(toInputStream(xml)),
                new javax.xml.transform.stream.StreamResult(new java.io.StringWriter()));
    }

    @Test
    public void testPackageSchemasAllowLocalIncludesAndRejectEscapingFiles() throws Exception {
        java.nio.file.Path directory = java.nio.file.Files.createTempDirectory("xml-schema-test");
        java.nio.file.Path schema = directory.resolve("root.xsd");
        java.nio.file.Path included = directory.resolve("included.xsd");
        try {
            java.nio.file.Files.writeString(included,
                    "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:element name='root' type='xs:string'/></xs:schema>");
            java.nio.file.Files.writeString(schema,
                    "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'><xs:include schemaLocation='included.xsd'/></xs:schema>");
            javax.xml.validation.Schema compiled = Xml.createSecureSchema(directory.toFile(), java.util.List.of(schema.toFile()));
            compiled.newValidator().validate(new javax.xml.transform.dom.DOMSource(Xml.readDocumentFromString("<root>value</root>")));
            java.nio.file.Files.createDirectory(directory.resolve("subdirectory"));
            try {
                Xml.createSecureSchema(directory.resolve("subdirectory").toFile(), java.util.List.of(schema.toFile()));
                fail("Schema outside the package must be rejected");
            } catch (IOException expected) {
                // The package boundary is enforced before schema parsing.
            }
        } finally {
            java.nio.file.Files.deleteIfExists(schema);
            java.nio.file.Files.deleteIfExists(included);
            java.nio.file.Files.deleteIfExists(directory.resolve("subdirectory"));
            java.nio.file.Files.deleteIfExists(directory);
        }
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
    public void testCreateSecureDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = Xml.createSecureDocumentBuilderFactory();

        assertNotNull("Secure document builder factory should not be null", factory);
        assertTrue("Secure document builder factory should be namespace aware", factory.isNamespaceAware());
        assertFalse("Secure document builder factory should not expand entity references", factory.isExpandEntityReferences());
        assertFalse("Secure document builder factory should not be validating", factory.isValidating());
        assertFalse("Secure document builder factory should not support XInclude", factory.isXIncludeAware());

        String xml = "<root xmlns=\"urn:test\"><child>value</child></root>";
        Document document = factory.newDocumentBuilder().parse(toInputStream(xml));

        assertEquals("Namespace should be preserved", "urn:test", document.getDocumentElement().getNamespaceURI());
    }

    @Test(expected = SAXException.class)
    public void testSecureDocumentBuilderFactoryRejectsDoctype() throws Exception {
        DocumentBuilderFactory factory = Xml.createSecureDocumentBuilderFactory();
        String xml = "<!DOCTYPE root [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><root>&xxe;</root>";

        factory.newDocumentBuilder().parse(toInputStream(xml));
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

    private ByteArrayInputStream toInputStream(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }
}
