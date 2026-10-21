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
package org.sakaiproject.content.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.Zip;

/** Uses the Kernel's production Spring wiring and real content storage. */
public class ContentHostingServiceZipTest extends SakaiKernelTestBase {
    private ContentHostingService content;

    @BeforeClass
    public static void configureKernel() throws IOException {
        oneTimeSetup("content");
    }

    @Before
    public void authenticateAdmin() {
        content = getService(ContentHostingService.class);
        Session session = getService(SessionManager.class).getCurrentSession();
        session.setUserId("admin");
        session.setUserEid("admin");
    }

    @Test
    public void rejectedArchiveReleasesOriginalResourceEditAndCreatesNoContent() throws Exception {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("safe.txt", "must not be imported");
        entries.put("../canary.txt", "unsafe");
        String archiveId = storeArchive(entries);
        long originalLength = content.getResource(archiveId).getContentLength();

        assertThrows(Zip.InvalidEntryException.class, () -> content.expandZippedResource(archiveId));

        assertEquals(originalLength, content.getResource(archiveId).getContentLength());
        assertThrows(IdUnusedException.class,
            () -> content.getCollection(archiveId.substring(0, archiveId.length() - 4) + "/"));
        assertEditable(archiveId);
    }

    @Test
    public void successfulExpansionAlsoReleasesOriginalResourceEdit() throws Exception {
        String archiveId = storeArchive(Map.of("safe.txt", "valid content"));
        long originalLength = content.getResource(archiveId).getContentLength();

        content.expandZippedResource(archiveId);

        String extractedId = archiveId.substring(0, archiveId.length() - 4) + "/safe.txt";
        assertEquals("valid content", new String(content.getResource(extractedId).getContent(), StandardCharsets.UTF_8));
        assertEquals(originalLength, content.getResource(archiveId).getContentLength());
        assertEditable(archiveId);
    }

    private void assertEditable(String resourceId) throws Exception {
        ContentResourceEdit edit = content.editResource(resourceId);
        try {
            assertNotNull(edit);
        } finally {
            content.cancelResource(edit);
        }
    }

    private String storeArchive(Map<String, String> entries) throws Exception {
        String collectionId = "/zip-regression-" + UUID.randomUUID() + "/";
        ContentCollectionEdit collection = content.addCollection(collectionId);
        content.commitCollection(collection);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        String resourceId = collectionId + "package.zip";
        ContentResourceEdit archive = content.addResource(resourceId);
        archive.setContent(bytes.toByteArray());
        archive.setContentType("application/zip");
        content.commitResource(archive);
        return resourceId;
    }
}
