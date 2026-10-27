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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import static org.junit.Assert.*;

public class Blackboard55PackageTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void readsFilesInsidePackageThroughVendorHelper() throws Exception {
        Blackboard55FileParser parser = parsePackage();
        Element file = fileNode("file.txt");
        try (InputStream in = parser.new Bb55FileHelper().getInputStreamForNode(file, "destination")) {
            assertEquals("package content", new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void rejectsFileReferencesOutsidePackage() throws Exception {
        Blackboard55FileParser parser = parsePackage();
        Path outside = Files.writeString(temporary.newFile().toPath(), "outside canary");
        for (String href : new String[] {outside.toString(), "../../../outside.txt", "..\\..\\..\\outside.txt"}) {
            Element file = fileNode(href);
            assertThrows(IOException.class, () -> parser.new Bb55FileHelper().getInputStreamForNode(file, "destination"));
        }
    }

    @Test
    public void descriptorCannotReadOutsideXml() throws Exception {
        Blackboard55FileParser parser = parsePackage();
        Path outside = Files.writeString(temporary.newFile("outside.xml").toPath(), "<CONTENT><TITLE>outside canary</TITLE></CONTENT>");
        String traversal = Path.of(parser.pathToData).relativize(outside).toString();
        for (String reference : new String[] {outside.toString(), traversal}) {
            Element file = fileNode(reference);
            Element resource = (Element) file.getParentNode();
            resource.setAttribute("file", reference);
            resource.setAttribute("bb:file", reference);
            assertNull(parser.resourceHelper.getDescriptor(resource));
        }
    }

    @Test
    public void descriptorCanReadXmlInsidePackage() throws Exception {
        Blackboard55FileParser parser = parsePackage();
        Element file = fileNode("descriptor.xml");
        Element resource = (Element) file.getParentNode();
        resource.setAttribute("file", "descriptor.xml");
        resource.setAttribute("bb:file", "descriptor.xml");
        assertNotNull(parser.resourceHelper.getDescriptor(resource));
    }

    private Blackboard55FileParser parsePackage() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("imsmanifest.xml"));
            zip.write("<manifest><organizations/><resources/></manifest>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("resource/file.txt"));
            zip.write("package content".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("descriptor.xml"));
            zip.write("<CONTENT><TITLE>package descriptor</TITLE></CONTENT>".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        Blackboard55FileParser parser = new Blackboard55FileParser();
        parser.parse(new ByteArrayInputStream(bytes.toByteArray()), temporary.newFolder().toString());
        return parser;
    }

    private Element fileNode(String href) throws Exception {
        Document document = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder().newDocument();
        Element resource = document.createElement("resource");
        resource.setAttribute("identifier", "resource");
        resource.setAttribute("baseurl", "resource");
        document.appendChild(resource);
        Element file = document.createElement("file");
        file.setAttribute("href", href);
        resource.appendChild(file);
        return file;
    }
}
