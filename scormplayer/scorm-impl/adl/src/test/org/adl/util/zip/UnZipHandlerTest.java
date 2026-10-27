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
package org.adl.util.zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.adl.validator.contentpackage.CPValidator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class UnZipHandlerTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void extractsNestedFilesWithPortableSeparators() throws Exception {
        Path root = temporary.newFolder().toPath().resolve("output");
        new UnZipHandler(archive("nested/file.txt", "other\\file.txt").toString(), root.toString(), "UTF-8").extract();
        assertEquals("marker", Files.readString(root.resolve("nested/file.txt")));
        assertEquals("marker", Files.readString(root.resolve("other/file.txt")));
    }

    @Test
    public void rejectsWholePackageBeforeWritingFiles() throws Exception {
        for (String name : new String[] {"../outside.txt", "../outside/", "nested/../../outside.txt",
                "..\\outside.txt", "/outside.txt", "C:/outside.txt", "C:outside.txt", "file:stream",
                "\\\\server/share/file.txt"}) {
            Path parent = temporary.newFolder().toPath();
            Path root = Files.createDirectory(parent.resolve("output"));
            Path canary = Files.writeString(parent.resolve("outside.txt"), "original");
            Path zip = archive("imsmanifest.xml", name, "later.txt");
            assertThrows(name, IOException.class, () -> new UnZipHandler(zip.toString(), root + File.separator, "UTF-8").extract());
            assertEquals("original", Files.readString(canary));
            try (java.util.stream.Stream<Path> files = Files.list(root)) {
                assertEquals(0, files.count());
            }
        }
    }

    @Test
    public void rejectsExistingDirectoryAndFileLinks() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path outside = temporary.newFolder().toPath();
        Files.createSymbolicLink(root.resolve("link"), outside);
        Path canary = Files.writeString(outside.resolve("existing.txt"), "original");
        Files.createSymbolicLink(root.resolve("file-link.txt"), canary);
        for (String name : new String[] {"link/missing/file.txt", "file-link.txt"}) {
            Path zip = archive(name);
            assertThrows(IOException.class, () -> new UnZipHandler(zip.toString(), root + File.separator, "UTF-8").extract());
        }
        assertEquals("original", Files.readString(canary));
        assertFalse(Files.exists(outside.resolve("missing")));
    }

    @Test
    public void validatorStopsWhenExtractionFails() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path zip = archive("imsmanifest.xml", "../outside.txt");
        CPValidator validator = new CPValidator(root.toString());
        assertFalse(validator.validate(zip.toString(), "pif", "contentpackage", false, "UTF-8"));
        assertFalse(validator.getADLValidatorOutcome().getDoesIMSManifestExist());
        assertFalse(Files.exists(root.resolve("outside.txt")));
        assertFalse(Files.exists(root.resolve("PackageImport/imsmanifest.xml")));
    }

    private Path archive(String... names) throws IOException {
        Path zip = temporary.newFile().toPath();
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (String name : names) {
                out.putNextEntry(new ZipEntry(name));
                if (!name.endsWith("/")) out.write("marker".getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return zip;
    }
}
