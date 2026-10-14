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
package org.sakaiproject.lessonbuildertool.cc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.util.Zip;

public class CartridgeLoaderTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void readsManifestAndLegacyQtiResource() throws Exception {
        Path root = temporary.newFolder("cartridge").toPath();
        Files.writeString(root.resolve("imsmanifest.xml"),
                "<manifest xmlns='http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1' identifier='course'>"
                + "<organizations><organization identifier='org'><item identifier='item' identifierref='quiz'>"
                + "<title>Quiz</title></item></organization></organizations><resources>"
                + "<resource identifier='quiz' type='imsqti_xmlv1p2/imscc_xmlv1p1/assessment' href='quiz.xml'>"
                + "<file href='quiz.xml'/></resource></resources></manifest>");
        Files.writeString(root.resolve("quiz.xml"),
                "<!DOCTYPE questestinterop SYSTEM 'ims_qtiasiv1p2p1.dtd'><questestinterop>"
                + "<assessment ident='quiz' title='Quiz'><section ident='section'><item ident='question'>"
                + "<presentation><material><mattext>Question</mattext></material></presentation>"
                + "</item></section></assessment></questestinterop>");
        CartridgeLoader loader = ZipLoader.getUtilities(root.toString());
        Parser parser = Parser.createCartridgeParser(loader);
        assertEquals("course", parser.getXML(loader, "imsmanifest.xml").getAttributeValue("identifier"));
        assertEquals("Quiz", parser.getXML(loader, "quiz.xml").getChild("assessment").getAttributeValue("title"));
    }

    @Test
    public void rejectsTraversalIntoSiblingWithSameDirectoryPrefix() throws IOException {
        Path archive = temporary.newFile("package.zip").toPath();
        Path parent = temporary.newFolder("extraction").toPath();
        Path sibling = Files.createDirectory(parent.resolve("package.zip-sibling"));
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry("../package.zip-sibling/canary.txt"));
            zip.write("unsafe".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        CartridgeLoader loader = ZipLoader.getUtilities(archive.toFile(), parent.toString());
        assertThrows(Zip.InvalidEntryException.class, loader::unzip);
        assertFalse(Files.exists(sibling.resolve("canary.txt")));
    }

    @Test
    public void readsContainedReferencesAndRejectsSiblingReads() throws IOException {
        Path root = temporary.newFolder("root").toPath();
        Files.createDirectory(root.resolve("pages"));
        Path image = Files.createDirectories(root.resolve("images")).resolve("image.txt");
        Files.writeString(image, "image");
        Path sibling = temporary.newFolder("root-sibling").toPath();
        Files.writeString(sibling.resolve("canary.txt"), "outside");
        CartridgeLoader loader = ZipLoader.getUtilities(root.toString());
        try (InputStream stream = loader.getFile("pages/../images/image.txt")) {
            assertEquals("image", new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        }
        assertThrows(Zip.InvalidEntryException.class, () -> loader.getFile("../root-sibling/canary.txt"));
    }
}
