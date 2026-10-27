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
package org.sakaiproject.search.util.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.search.util.FileUtils;
import static org.junit.Assert.*;

public class ZipUnpackTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void unpacksNestedFilesAndEmptyDirectories() throws Exception {
        Path root = temporary.newFolder().toPath().resolve("output");
        FileUtils.unpack(new ByteArrayInputStream(archive("empty/", "nested/file.txt")), root.toFile());
        assertTrue(Files.isDirectory(root.resolve("empty")));
        assertEquals("marker", Files.readString(root.resolve("nested/file.txt")));
    }

    @Test
    public void rejectsEntriesOutsideDestination() throws Exception {
        for (String name : new String[] {"../outside.txt", "../outside/", "..\\outside.txt", "/outside.txt", "C:/outside.txt"}) {
            Path parent = temporary.newFolder().toPath();
            Path root = parent.resolve("output");
            Path canary = Files.writeString(parent.resolve("outside.txt"), "original");
            byte[] zip = archive(name);
            assertThrows(IOException.class, () -> FileUtils.unpack(new ByteArrayInputStream(zip), root.toFile()));
            assertEquals("original", Files.readString(canary));
        }
    }

    @Test
    public void rejectsExistingLinksOutsideDestination() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path outside = temporary.newFolder().toPath();
        Files.createSymbolicLink(root.resolve("link"), outside);
        byte[] zip = archive("link/missing/file.txt");
        assertThrows(IOException.class, () -> FileUtils.unpack(new ByteArrayInputStream(zip), root.toFile()));
        assertFalse(Files.exists(outside.resolve("missing")));
    }

    private byte[] archive(String... names) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            for (String name : names) {
                out.putNextEntry(new ZipEntry(name));
                if (!name.endsWith("/")) out.write("marker".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }
}
