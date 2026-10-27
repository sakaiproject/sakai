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
package org.sakaiproject.scorm.service.standalone.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import static org.junit.Assert.*;

public class StandaloneResourceServiceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();
    private final StandaloneResourceService service = new StandaloneResourceService();
    private final List<Path> created = new ArrayList<>();

    @After
    public void removeOnlyTestPackages() throws Exception {
        for (Path root : created) {
            if (Files.exists(root)) {
                try (Stream<Path> paths = Files.walk(root)) {
                    for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
                }
            }
        }
    }

    @Test
    public void normalArchiveRoundTripsThroughPublicService() throws Exception {
        String source = service.putArchive(new ByteArrayInputStream(archive("nested\\file.txt")), "package.zip", "application/zip", false, 0);
        created.add(Path.of("contentPackages", source));
        String extracted = service.convertArchive(source, "package");
        Path root = Path.of("contentPackages", extracted);
        created.add(root);
        assertEquals("content", Files.readString(root.resolve("nested/file.txt")));
    }

    @Test
    public void rejectsUploadNamesOutsidePackage() {
        assertThrows(ResourceStorageException.class, () -> service.putArchive(new ByteArrayInputStream(new byte[] {1}),
            "../outside.zip", "application/zip", false, 0));
    }

    @Test
    public void fileAndDirectorySinksRejectLinksOutsidePackage() throws Exception {
        String uuid = "test-" + UUID.randomUUID();
        Path root = Files.createDirectories(Path.of("contentPackages", uuid));
        created.add(root);
        Path outside = temporary.newFolder().toPath();
        Files.createSymbolicLink(root.resolve("link"), outside);
        assertThrows(ResourceStorageException.class, () -> service.newFolder(uuid, new ZipEntry("link/missing/")));
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive("link/file.txt")))) {
            ZipEntry entry = zip.getNextEntry();
            assertThrows(ResourceStorageException.class, () -> service.newItem(uuid, zip, entry));
        }
        assertFalse(Files.exists(outside.resolve("missing")));
        assertFalse(Files.exists(outside.resolve("file.txt")));
    }

    private byte[] archive(String name) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(name));
            zip.write("content".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return bytes.toByteArray();
    }
}
