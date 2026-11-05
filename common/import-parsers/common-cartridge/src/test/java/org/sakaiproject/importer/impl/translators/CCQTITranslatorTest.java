/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2025 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.importer.impl.translators;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.sakaiproject.importer.impl.importables.Assessment;
import org.sakaiproject.util.Xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sakaiproject.importer.api.Importable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CCQTITranslatorTest {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Test
	public void testGetTypeName() {
		CCQTITranslator translator = new CCQTITranslator();
		assertEquals("associatedcontent/imscc_xmlv1p1/learning-application-resource", translator.getTypeName());
	}

	@Test
	public void testTranslateWithQTIFile() throws Exception {
		Path archive = temporaryFolder.newFolder().toPath();
		Files.createDirectory(archive.resolve("assessments"));
		Files.writeString(archive.resolve("assessments/bank.xml.qti"), "<questestinterop><objectbank ident='package bank'/></questestinterop>");

		Assessment result = (Assessment) translate(archive, "assessments/bank.xml.qti");

		assertNotNull(result);
		assertEquals("package bank", result.getTitle());
		assertEquals("questestinterop", result.getQti().getDocumentElement().getTagName());
	}

	@Test
	public void testRejectsQTIReferencesOutsidePackage() throws Exception {
		Path parent = temporaryFolder.newFolder().toPath();
		Path archive = Files.createDirectory(parent.resolve("archive"));
		Path outside = Files.writeString(parent.resolve("outside.xml.qti"), "<questestinterop><objectbank ident='outside canary'/></questestinterop>");
		Files.createSymbolicLink(archive.resolve("link.xml.qti"), outside);
		for (String reference : new String[] {"../outside.xml.qti", "..\\outside.xml.qti", outside.toString(), "link.xml.qti"}) {
			assertNull(reference, translate(archive, reference));
		}
	}

	private Importable translate(Path archive, String reference) throws Exception {
		Document manifest = Xml.createSecureDocumentBuilderFactory().newDocumentBuilder().newDocument();
		Element resource = manifest.createElement("resource");
		resource.setAttribute("href", reference);
		resource.setAttribute("identifier", "qti-bank");
		return new CCQTITranslator().translate(resource, null, "context/", archive.toString());
	}

	@Test
	public void testTranslateWithNonQTIFile() throws Exception {
		CCQTITranslator translator = new CCQTITranslator();
		
		// Create a mock resource node with non-QTI file reference
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document manifestDoc = builder.newDocument();
		Element resourceElement = manifestDoc.createElement("resource");
		resourceElement.setAttribute("identifier", "test-resource");
		resourceElement.setAttribute("href", "web_content/test.html");
		
		// Should return null for non-QTI files
		Importable result = translator.translate(resourceElement, null, "context/", "/tmp");
		assertNull("Should return null for non-QTI files", result);
	}

	@Test
	public void testTranslateRejectsQTIFileWithDoctype() throws Exception {
		CCQTITranslator translator = new CCQTITranslator();

		File archiveBase = temporaryFolder.newFolder("archive");
		File assessmentDirectory = new File(archiveBase, "non_cc_assessments");
		assertTrue("Assessment directory should be created", assessmentDirectory.mkdirs());

		File qtiFile = new File(assessmentDirectory, "test.xml.qti");
		String qtiXml = "<!DOCTYPE questestinterop [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
			+ "<questestinterop>&xxe;</questestinterop>";
		Files.write(qtiFile.toPath(), qtiXml.getBytes(StandardCharsets.UTF_8));

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document manifestDoc = builder.newDocument();
		Element resourceElement = manifestDoc.createElement("resource");
		resourceElement.setAttribute("identifier", "test-qti");
		resourceElement.setAttribute("href", "non_cc_assessments/test.xml.qti");

		Importable result = translator.translate(resourceElement, null, "context/", archiveBase.getPath());

		assertNull("Should reject QTI files containing a DOCTYPE", result);
	}
}
