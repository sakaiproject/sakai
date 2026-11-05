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
package org.adl.validator.contentpackage;

import java.nio.file.Files;
import java.nio.file.Path;
import org.adl.logging.DetailedLogMessageCollection;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.util.Messages;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class CPValidatorTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    /** Package validation rejects metadata references and symlinks outside the extracted archive. */
    @Test
    public void rejectsMetadataOutsidePackageBeforeParsing() throws Exception {
        Path parent = temporary.newFolder().toPath();
        Path outside = Files.writeString(parent.resolve("metadata.xml"), "<lom xmlns='http://ltsc.ieee.org/xsd/LOM'/>");
        for (String reference : new String[] {"../metadata.xml", "..\\metadata.xml", outside.toString(), "link.xml"}) {
            Path archive = Files.createTempDirectory(parent, "archive-");
            Files.createSymbolicLink(archive.resolve("link.xml"), outside);
            validateMetadata(archive, reference);
            assertTrue(reference, reportedMissingMetadata(reference));
        }
    }

    /** Metadata inside the package remains available to validation. */
    @Test
    public void acceptsMetadataWithinPackage() throws Exception {
        Path archive = temporary.newFolder().toPath();
        Files.createDirectory(archive.resolve("metadata"));
        Files.writeString(archive.resolve("metadata/course.xml"), "<lom xmlns='http://ltsc.ieee.org/xsd/LOM'/>");

        validateMetadata(archive, "metadata/course.xml");

        assertFalse(reportedMissingMetadata("metadata/course.xml"));
    }

    /** Validates a real manifest with the supplied package-relative metadata reference. */
    private void validateMetadata(Path archive, String reference) throws Exception {
        DetailedLogMessageCollection messages = DetailedLogMessageCollection.getInstance();
        while (messages.getSize() > 0) messages.getMessage();
        String escaped = reference.replace("&", "&amp;").replace("<", "&lt;");
        Path manifest = Files.writeString(archive.resolve("imsmanifest.xml"),
                "<manifest xmlns='http://www.imsglobal.org/xsd/imscp_v1p1' "
                + "xmlns:adlcp='http://www.adlnet.org/xsd/adlcp_v1p3' identifier='manifest'>"
                + "<metadata><adlcp:location>" + escaped + "</adlcp:location></metadata>"
                + "<organizations/><resources/></manifest>");
        CPValidator validator = new CPValidator(archive.toString());
        validator.setSchemaLocation("http://www.imsglobal.org/xsd/imscp_v1p1 imscp_v1p1.xsd");
        validator.setPerformValidationToSchema(true);
        // Required schema files are deliberately absent; inspect the metadata result below.
        validator.validate(manifest.toString(), "media", "contentpackage", false, "UTF-8");
        assertTrue("The manifest must reach metadata validation", validator.getIsWellformed());
    }

    /** Checks the validator messages for a rejected metadata reference. */
    private boolean reportedMissingMetadata(String reference) {
        DetailedLogMessageCollection messages = DetailedLogMessageCollection.getInstance();
        String expected = Messages.getString("CPValidator.537", reference);
        boolean rejected = false;
        while (messages.getSize() > 0) {
            LogMessage message = messages.getMessage();
            if (message.getMessageType() == MessageType.FAILED && expected.equals(message.getMessageText())) {
                rejected = true;
            }
        }
        return rejected;
    }
}
