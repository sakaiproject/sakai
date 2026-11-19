/*
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
package org.sakaiproject.tool.assessment.qti.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;

public class QtiXmlCompatibilityTest {
    @Test
    public void readsBundledTemplatesThroughBothImportReaders() throws Exception {
        for (String version : new String[] {"v1p2", "v2p0"}) {
            byte[] template;
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(
                    "xml/author/" + version + "/assessmentTemplate.xml")) {
                assertNotNull(input);
                template = input.readAllBytes();
            }
            AuthoringHelper helper = new AuthoringHelper(QTIVersion.VERSION_1_2);
            assertEquals("questestinterop", helper.readXMLDocument(new ByteArrayInputStream(template))
                    .getDocument().getDocumentElement().getTagName());
            Path file = Files.createTempFile("qti-template", ".xml");
            try {
                Files.write(file, template);
                assertEquals("questestinterop", XmlUtil.readDocument(file.toString(), false)
                        .getDocumentElement().getTagName());
            } finally {
                Files.deleteIfExists(file);
            }
        }
    }
}
