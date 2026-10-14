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
package org.adl.parsers.dom;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.adl.sequencer.impl.ADLSeqParser;
import static org.junit.Assert.*;

public class ADLDOMParserTest {
    @Test
    public void reportsRemoteSchemaAsInvalid() throws Exception {
        Path file = Files.createTempFile("scorm-schema", ".xml");
        try {
            Files.writeString(file, "<manifest/>");
            ADLDOMParser parser = new ADLDOMParser();
            parser.setSchemaLocation("urn:manifest https://example.invalid/manifest.xsd");
            parser.parseForValidation(file.toString());
            assertTrue(parser.getIsWellformed());
            assertFalse(parser.getIsValidToSchema());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void testSequencingParserUsesSharedXmlHandling() throws Exception {
        Path file = Files.createTempFile("scorm-sequence", ".xml");
        try {
            Files.writeString(file, "<manifest identifier='course'><organizations default='organization'>"
                    + "<organization identifier='organization'/></organizations></manifest>");
            ADLSeqParser parser = new ADLSeqParser(file.toString());
            assertEquals("organization", parser.findDefaultOrganization().getAttributes().getNamedItem("identifier").getNodeValue());
            assertEquals("course", parser.getCourseID());

            Files.writeString(file, "<!DOCTYPE manifest [<!ENTITY value 'course'>]><manifest identifier='course'/>");
            assertNull(new ADLSeqParser(file.toString()).findDefaultOrganization());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void testSharedParserReadsXmlAndRejectsDoctype() throws Exception {
        Path file = Files.createTempFile("scorm-xml", ".xml");
        try {
            Files.writeString(file, "<root><child>value</child></root>");
            ADLDOMParser parser = new ADLDOMParser();
            parser.parseForWellformedness(file.toString(), false, false);
            assertTrue(parser.getIsWellformed());
            assertEquals("root", parser.getDocument().getDocumentElement().getTagName());

            Files.write(file, "<root><child>café</child></root>".getBytes(StandardCharsets.UTF_16));
            ADLDOMParser encoded = new ADLDOMParser();
            encoded.parseForWellformedness(file.toString(), false, false);
            assertTrue(encoded.getIsWellformed());
            assertEquals("café", encoded.getDocument().getDocumentElement().getTextContent());

            Files.writeString(file, "<!DOCTYPE root [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]><root>&xxe;</root>");
            ADLDOMParser rejected = new ADLDOMParser();
            rejected.parseForWellformedness(file.toString(), false, false);
            assertFalse(rejected.getIsWellformed());
            assertNull(rejected.getDocument());
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
