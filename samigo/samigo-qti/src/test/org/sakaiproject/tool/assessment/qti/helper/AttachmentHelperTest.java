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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.sakaiproject.content.api.ContentResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttachmentHelperTestConfiguration.class)
public class AttachmentHelperTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    @Autowired private AttachmentHelper helper;

    @Test
    public void uploadsResolvedFileWithoutDecodingItsPathAgain() throws Exception {
        Path parent = temporary.getRoot().toPath();
        Files.writeString(parent.resolve("attachment.txt"), "outside contents");
        Path literalDirectory = Files.createDirectories(parent.resolve("package/%2e%2e"));
        Path literal = Files.writeString(literalDirectory.resolve("attachment.txt"), "inside contents");

        ContentResource resource = helper.createContentResource(literal.toFile().getCanonicalPath(),
            "attachment.txt", "text/plain");

        assertNotNull(resource);
        assertArrayEquals("inside contents".getBytes(StandardCharsets.UTF_8), resource.getContent());
    }

    @Test
    public void uploadsLiteralPercentAndPlusFilename() throws Exception {
        Path literal = Files.writeString(temporary.getRoot().toPath().resolve("a+b%2e%2e.txt"), "literal contents");

        ContentResource resource = helper.createContentResource(literal.toFile().getCanonicalPath(),
            "a%2Bb%252e%252e.txt", "text/plain");

        assertNotNull(resource);
        assertArrayEquals("literal contents".getBytes(StandardCharsets.UTF_8), resource.getContent());
    }

    @Test
    public void uploadsFilenameContainingAnUnescapedPercent() throws Exception {
        Path literal = Files.writeString(temporary.getRoot().toPath().resolve("100%complete.txt"), "percent contents");

        ContentResource resource = helper.createContentResource(literal.toFile().getCanonicalPath(),
            "100%complete.txt", "text/plain");

        assertNotNull(resource);
        assertArrayEquals("percent contents".getBytes(StandardCharsets.UTF_8), resource.getContent());
    }
}
