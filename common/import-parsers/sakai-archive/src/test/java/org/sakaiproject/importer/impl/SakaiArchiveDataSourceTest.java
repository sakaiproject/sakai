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
package org.sakaiproject.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.importer.api.ResetOnCloseInputStream;
import static org.junit.Assert.*;

public class SakaiArchiveDataSourceTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void extractsSelectedXmlAndAttachments() throws Exception {
        Path root = temporary.newFolder().toPath();
        BasicImportMetadata selected = new BasicImportMetadata();
        selected.setFileName("selected.xml");
        dataSource(root, "selected.xml", "other.xml", "attachment.txt").buildSourceFolder(List.of(selected));
        assertEquals("marker", Files.readString(root.resolve("source/selected.xml")));
        assertEquals("marker", Files.readString(root.resolve("source/attachment.txt")));
        assertFalse(Files.exists(root.resolve("source/other.xml")));
    }

    @Test
    public void rejectsSelectedXmlOutsideSourceFolder() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path canary = Files.writeString(root.resolve("outside.xml"), "original");
        BasicImportMetadata selected = new BasicImportMetadata();
        selected.setFileName("../outside.xml");
        SakaiArchiveDataSource source = dataSource(root, "../outside.xml");
        assertThrows(UncheckedIOException.class, () -> source.buildSourceFolder(List.of(selected)));
        assertEquals("original", Files.readString(canary));
    }

    @Test
    public void rejectsAttachmentsOutsideSourceFolder() throws Exception {
        for (String name : new String[] {"../outside.txt", "..\\outside.txt", "/outside.txt", "C:/outside.txt"}) {
            Path root = temporary.newFolder().toPath();
            Path canary = Files.writeString(root.resolve("outside.txt"), "original");
            SakaiArchiveDataSource source = dataSource(root, name);
            assertThrows(UncheckedIOException.class, () -> source.buildSourceFolder(List.of()));
            assertEquals("original", Files.readString(canary));
        }
    }

    @Test
    public void rejectsAttachmentLinksOutsideSourceFolder() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path source = Files.createDirectory(root.resolve("source"));
        Path outside = temporary.newFolder().toPath();
        Path canary = Files.writeString(outside.resolve("outside.txt"), "original");
        Files.createSymbolicLink(source.resolve("attachment.txt"), canary);
        SakaiArchiveDataSource data = dataSource(root, "attachment.txt");
        assertThrows(UncheckedIOException.class, () -> data.buildSourceFolder(List.of()));
        assertEquals("original", Files.readString(canary));
    }

    @Test
    public void extractsExplicitDirectoriesAndNestedSelectedXml() throws Exception {
        Path root = temporary.newFolder().toPath();
        BasicImportMetadata selected = new BasicImportMetadata();
        selected.setFileName("folder/selected.xml");
        dataSource(root, "folder/", "folder/selected.xml", "folder/file.txt").buildSourceFolder(List.of(selected));
        assertTrue(Files.isDirectory(root.resolve("source/folder")));
        assertEquals("marker", Files.readString(root.resolve("source/folder/selected.xml")));
        assertEquals("marker", Files.readString(root.resolve("source/folder/file.txt")));
    }

    @Test
    public void createsImplicitParentDirectoriesForFiles() throws Exception {
        Path root = temporary.newFolder().toPath();
        BasicImportMetadata selected = new BasicImportMetadata();
        selected.setFileName("nested/selected.xml");
        dataSource(root, "nested/selected.xml", "attachments/deep/file.txt").buildSourceFolder(List.of(selected));
        assertEquals("marker", Files.readString(root.resolve("source/nested/selected.xml")));
        assertEquals("marker", Files.readString(root.resolve("source/attachments/deep/file.txt")));
    }

    private SakaiArchiveDataSource dataSource(Path root, String... names) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            for (String name : names) {
                out.putNextEntry(new ZipEntry(name));
                if (!name.endsWith("/")) out.write("marker".getBytes(java.nio.charset.StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return new SakaiArchiveDataSource(new ResetOnCloseInputStream(new ByteArrayInputStream(bytes.toByteArray())),
            root.toString(), root.toString());
    }
}
