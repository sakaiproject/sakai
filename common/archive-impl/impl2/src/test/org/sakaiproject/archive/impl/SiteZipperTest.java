/**
 * Copyright (c) 2006-2017 The Apereo Foundation
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
package org.sakaiproject.archive.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SiteZipperTest {

    private SiteZipper siteZipper;

    @Before
    public void setUp() {
        siteZipper = new SiteZipper();
    }

    @Test
    public void testUnzip() throws IOException, URISyntaxException {
        String unzipFolder = Files.createTempDirectory("testUnzip").toString();
        URL resource = getClass().getResource("/!admin-20170303050657.zip");

        String zip = Paths.get(resource.toURI()).toFile().getAbsolutePath();
        String folder = siteZipper.unzipArchive(zip, unzipFolder);

        Assert.assertEquals("!admin-archive/", folder);
    }

    @Test
    public void testUnzipNoFolder() throws IOException, URISyntaxException {
        String unzipFolder = Files.createTempDirectory("testUnzip").toString();
        URL resource = getClass().getResource("/!admin-no-folder.zip");

        String zip = Paths.get(resource.toURI()).toFile().getAbsolutePath();
        String folder = siteZipper.unzipArchive(zip, unzipFolder);

        Assert.assertEquals(null, folder);
    }
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void rejectsEntryNamesBeforeCreatingAnyArchiveContents() throws Exception {
        for (String name : new String[] {"c/../../outside.txt", "../outside/", "c/../../../outside.txt",
                "c/..\\..\\outside.txt", "/outside.txt", "C:/outside.txt", "file:stream",
                "\\\\server/share/file.txt"}) {
            Path parent = temporary.newFolder().toPath();
            Path root = Files.createDirectory(parent.resolve("output"));
            Path canary = Files.writeString(parent.resolve("outside.txt"), "original");
            Path zip = archive("c/", "c/manifest.xml", name);
            Assert.assertThrows(name, IOException.class, () -> siteZipper.unzipArchive(zip.toString(), root.toString()));
            Assert.assertEquals("original", Files.readString(canary));
            try (Stream<Path> files = Files.list(root)) {
                Assert.assertEquals(0, files.count());
            }
        }
    }

    @Test
    public void extractsOnlyTheFirstDirectoryAndNormalizesNames() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path zip = archive("./c/", "c/nested/file.txt", "c\\other.txt", "other/file.txt");
        Assert.assertEquals("c/", siteZipper.unzipArchive(zip.toString(), root.toString()));
        Assert.assertEquals("marker", Files.readString(root.resolve("c/nested/file.txt")));
        Assert.assertEquals("marker", Files.readString(root.resolve("c/other.txt")));
        Assert.assertFalse(Files.exists(root.resolve("other/file.txt")));
    }

    @Test
    public void rejectsSymlinkedDestinationsBeforeWriting() throws Exception {
        Path root = temporary.newFolder().toPath();
        Path outside = temporary.newFolder().toPath();
        Files.createSymbolicLink(root.resolve("c"), outside);
        Path zip = archive("c/", "c/missing/file.txt");
        Assert.assertThrows(IOException.class, () -> siteZipper.unzipArchive(zip.toString(), root.toString()));
        Assert.assertFalse(Files.exists(outside.resolve("missing")));
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
