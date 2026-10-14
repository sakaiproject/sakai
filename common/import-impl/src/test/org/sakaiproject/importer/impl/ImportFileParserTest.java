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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.importer.api.ImportFileParser;

/** Exercises the shared extractor through the public parser API, without mocked internals. */
public class ImportFileParserTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void extractsRootAndNestedManifestPackages() throws IOException {
        for (String prefix : new String[] {"", "package/", "package\\"}) {
            Path parent = temporary.newFolder().toPath();
            ExtractionParser parser = new ExtractionParser();
            Map<String, String> entries = new LinkedHashMap<>();
            entries.put(prefix + "imsmanifest.xml", "<manifest/>");
            entries.put(prefix + "images/", "");
            entries.put(prefix + "images/resource.txt", "attachment");
            parser.parse(archive(entries), parent.toString());
            assertTrue(parser.extractedPath.toRealPath().startsWith(parent.toRealPath()));
            assertEquals("<manifest/>", Files.readString(parser.extractedPath.resolve("imsmanifest.xml")));
            assertEquals("attachment", Files.readString(parser.extractedPath.resolve("images/resource.txt")));
        }
    }

    @Test
    public void rejectsUnsafeFilesAndDirectoriesBeforeManifestProcessing() throws IOException {
        for (String name : new String[] {"../canary.txt", "../escape/", "../../outside.txt",
                "..\\canary.txt", "..\\escape\\", "/tmp/canary.txt", "C:/canary.txt",
                "C:canary.txt", "\\\\server/share/canary.txt", "../import-sibling/imsmanifest.xml"}) {
            Path parent = temporary.newFolder().toPath();
            ExtractionParser parser = new ExtractionParser();
            Map<String, String> entries = new LinkedHashMap<>();
            entries.put("resource.txt", "partial extraction");
            entries.put(name, "canary");
            assertThrows(name, UncheckedIOException.class, () -> parser.parse(archive(entries), parent.toString()));
            assertFalse(parser.awakened);
            try (Stream<Path> children = Files.list(parent)) {
                assertEquals(0, children.count());
            }
        }
        assertFalse(Files.exists(temporary.getRoot().toPath().resolve("canary.txt")));
        assertFalse(Files.exists(temporary.getRoot().toPath().resolve("escape")));
    }

    @Test
    public void rejectsTraversalWithoutOverwritingExistingFile() throws IOException {
        Path parent = temporary.newFolder().toPath();
        Path canary = temporary.getRoot().toPath().resolve("canary.txt");
        Files.writeString(canary, "original");
        ExtractionParser parser = new ExtractionParser();
        assertThrows(UncheckedIOException.class, () -> parser.parse(
            archive(Map.of("../../canary.txt", "replacement")), parent.toString()));
        assertEquals("original", Files.readString(canary));
    }

    @Test
    public void createsIndependentDirectoriesForRepeatedImports() throws IOException {
        Path parent = temporary.newFolder().toPath();
        ExtractionParser parser = new ExtractionParser();
        parser.parse(archive(Map.of("imsmanifest.xml", "first")), parent.toString());
        Path first = parser.extractedPath;
        parser.parse(archive(Map.of("imsmanifest.xml", "second")), parent.toString());
        assertFalse(first.equals(parser.extractedPath));
        assertEquals("first", Files.readString(first.resolve("imsmanifest.xml")));
    }

    private ByteArrayInputStream archive(Map<String, String> entries) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return new ByteArrayInputStream(bytes.toByteArray());
    }

    /** Supplies only vendor-specific hooks; parse and extraction remain production code. */
    private static class ExtractionParser extends ZipFileParser {
        private Path extractedPath;
        private boolean awakened;

        @Override
        public ImportFileParser newParser() {
            return new ExtractionParser();
        }

        @Override
        protected void awakeFromUnzip(String location) {
            extractedPath = Path.of(location);
            awakened = true;
        }

        @Override
        protected Collection getImportableItemsFromArchive(String location) {
            return List.of();
        }

        @Override
        protected Collection getCategoriesFromArchive(String location) {
            return List.of();
        }
    }
}
