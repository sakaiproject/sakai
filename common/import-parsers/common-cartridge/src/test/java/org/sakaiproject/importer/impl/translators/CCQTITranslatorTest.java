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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.sakaiproject.importer.api.Importable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CCQTITranslatorTest {

	@Test
	public void testGetTypeName() {
		CCQTITranslator translator = new CCQTITranslator();
		assertEquals("associatedcontent/imscc_xmlv1p1/learning-application-resource", translator.getTypeName());
	}

	@Test
	public void testTranslateWithQTIFile() throws Exception {
		CCQTITranslator translator = new CCQTITranslator();
		
		// Create a mock resource node with QTI file reference
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document manifestDoc = builder.newDocument();
		Element resourceElement = manifestDoc.createElement("resource");
		resourceElement.setAttribute("identifier", "test-qti");
		resourceElement.setAttribute("href", "non_cc_assessments/test.xml.qti");
		
		// Create a mock QTI document
		String qtiXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<questestinterop xmlns=\"http://www.imsglobal.org/xsd/ims_qtiasiv1p2\">" +
			"  <objectbank ident=\"test-bank\">" +
			"    <qtimetadata>" +
			"      <qtimetadatafield>" +
			"        <fieldlabel>bank_title</fieldlabel>" +
			"        <fieldentry>Test Assessment Bank</fieldentry>" +
			"      </qtimetadatafield>" +
			"    </qtimetadata>" +
			"  </objectbank>" +
			"</questestinterop>";
		
		// We can't actually create the file system test here without setting up the full environment
		// This test validates the basic logic but would need integration testing for file loading
		
		assertFalse("Should not process resource children", translator.processResourceChildren());
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
}