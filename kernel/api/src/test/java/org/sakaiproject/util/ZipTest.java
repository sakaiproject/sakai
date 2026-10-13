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
package org.sakaiproject.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ZipTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void normalizesPortableRelativeNames() throws IOException {
        assertEquals("folder/file.xml", Zip.validateEntryName("./folder//file.xml"));
        assertEquals("folder/file.xml", Zip.validateEntryName("folder\\file.xml"));
        assertEquals("folder/", Zip.validateEntryName("folder\\"));
        assertEquals("日本語/file.xml", Zip.validateEntryName("日本語/file.xml"));
    }

    @Test
    public void rejectsUnsafeEntryNames() {
        for (String name : new String[] {null, "", " ", ".", "./", "../file", "folder/../file",
                "folder/../../file", "..\\file", "/file", "\\file", "C:/file", "C:file",
                "./C:/file", "\\\\server/share/file", "file:stream", "file\0.xml"}) {
            assertThrows(String.valueOf(name), IOException.class, () -> Zip.validateEntryName(name));
        }
    }

    @Test
    public void resolvesNestedEntriesInsideCanonicalRoot() throws IOException {
        Path root = temporary.newFolder("root").toPath();
        assertEquals(root.toRealPath().resolve("nested/file.xml"), Zip.resolveEntryPath(root, "nested/file.xml"));
    }

    @Test
    public void rejectsSiblingDirectoryWithSamePrefix() throws IOException {
        Path root = temporary.newFolder("root").toPath();
        temporary.newFolder("root-sibling");
        assertThrows(IOException.class, () -> Zip.resolveContainedPath(root, "../root-sibling/file.xml"));
        assertThrows(IOException.class, () -> Zip.resolveContainedPath(root, "."));
    }

    @Test
    public void rejectsSymlinksLeadingOutsideRoot() throws IOException {
        Path root = temporary.newFolder("root").toPath();
        Path outside = temporary.newFolder("outside").toPath();
        Files.createSymbolicLink(root.resolve("link"), outside);
        assertThrows(IOException.class, () -> Zip.resolveEntryPath(root, "link/file.xml"));
        assertThrows(IOException.class, () -> Zip.resolveContainedPath(root, "link/file.xml"));
        assertThrows(IOException.class, () -> Zip.resolveEntryPath(root, "link/missing/nested/file.xml"));
        assertThrows(IOException.class, () -> Zip.resolveContainedPath(root, "link/../file.xml"));
        Path outsideFile = Files.createFile(outside.resolve("existing.xml"));
        Files.createSymbolicLink(root.resolve("file-link.xml"), outsideFile);
        assertThrows(IOException.class, () -> Zip.resolveEntryPath(root, "file-link.xml"));
    }

    @Test
    public void rejectsDanglingSymlinks() throws IOException {
        Path root = temporary.newFolder("root").toPath();
        Files.createSymbolicLink(root.resolve("link"), root.resolve("missing"));
        assertThrows(IOException.class, () -> Zip.resolveEntryPath(root, "link"));
        assertThrows(IOException.class, () -> Zip.resolveEntryPath(root, "link/file.xml"));
        assertThrows(IOException.class, () -> Zip.resolveContainedPath(root, "link/../file.xml"));
    }

    @Test
    public void resolvesSymlinksWithinRootBeforeParentSegments() throws IOException {
        Path root = temporary.newFolder("root").toPath().toRealPath();
        Path nested = Files.createDirectories(root.resolve("folder/nested"));
        Files.createSymbolicLink(root.resolve("link"), nested);
        assertEquals(nested.resolve("missing/file.xml"), Zip.resolveEntryPath(root, "link/missing/file.xml"));
        assertEquals(root.resolve("folder/file.xml"), Zip.resolveContainedPath(root, "link/../file.xml"));
        Path file = Files.createFile(nested.resolve("existing.xml"));
        Files.createSymbolicLink(root.resolve("file-link.xml"), file);
        assertEquals(file, Zip.resolveEntryPath(root, "file-link.xml"));
    }

    @Test
    public void permitsContainedParentReferencesButNotParentEntryNames() throws IOException {
        Path root = temporary.newFolder("root").toPath();
        Files.createDirectory(root.resolve("pages"));
        assertEquals(root.toRealPath().resolve("images/image.png"),
            Zip.resolveContainedPath(root, "pages/../images/image.png"));
        assertThrows(IOException.class, () -> Zip.resolveEntryPath(root, "pages/../images/image.png"));
    }
}
