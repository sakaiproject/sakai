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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.Assessment;
import org.sakaiproject.importer.impl.XPathHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Translator for Canvas QTI files within IMSCC packages.
 * Handles resources with type "associatedcontent/imscc_xmlv1p1/learning-application-resource"
 * that reference .xml.qti files.
 */
@Slf4j
public class CCQTITranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "associatedcontent/imscc_xmlv1p1/learning-application-resource";
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		
		// Check if this is actually a QTI file by examining the href attribute
		String href = XPathHelper.getNodeValue("./@href", resourceNode);
		if (href == null || !href.endsWith(".xml.qti")) {
			// Not a QTI file, let the default handler process it
			return null;
		}
		
		Assessment assessment = new Assessment();
		assessment.setVersion("1.2"); // Canvas uses QTI 1.2
		
		try {
			// Load the QTI file
			String qtiFilePath = archiveBasePath + File.separator + href;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document qtiDocument = builder.parse(new FileInputStream(qtiFilePath));
			
			// Set the QTI document for Samigo to process
			assessment.setQti(qtiDocument);
			
			// Extract title from QTI metadata or objectbank
			String title = extractTitleFromQTI(qtiDocument, resourceNode);
			assessment.setTitle(title);
			
			// Extract description if available
			String description = extractDescriptionFromQTI(qtiDocument);
			assessment.setDescription(description);
			
			// Set context path for proper import hierarchy
			assessment.setContextPath(contextPath + title);
			
			log.debug("Created QTI assessment import object: {}", title);
			
		} catch (Exception e) {
			log.error("Error parsing QTI file {}: {}", href, e.getMessage());
			return null;
		}
		
		return assessment;
	}

	public boolean processResourceChildren() {
		return false;
	}
	
	/**
	 * Extract title from QTI document metadata
	 */
	private String extractTitleFromQTI(Document qtiDocument, Node resourceNode) {
		// First try to get bank_title from QTI metadata
		String title = XPathHelper.getNodeValue("//qtimetadatafield[fieldlabel='bank_title']/fieldentry", qtiDocument);
		
		if (title == null || title.isEmpty()) {
			// Try to get title from the resource node in manifest
			title = XPathHelper.getNodeValue("//item[@identifierref='" + 
				XPathHelper.getNodeValue("./@identifier", resourceNode) + "']/title", 
				resourceNode.getOwnerDocument());
		}
		
		if (title == null || title.isEmpty()) {
			// Fallback to objectbank ident or filename
			title = XPathHelper.getNodeValue("//objectbank/@ident", qtiDocument);
			if (title == null || title.isEmpty()) {
				String href = XPathHelper.getNodeValue("./@href", resourceNode);
				if (href != null) {
					title = href.substring(href.lastIndexOf("/") + 1);
					if (title.endsWith(".xml.qti")) {
						title = title.substring(0, title.length() - 8);
					}
				} else {
					title = "Imported Assessment";
				}
			}
		}
		
		return title;
	}
	
	/**
	 * Extract description from QTI document if available
	 */
	private String extractDescriptionFromQTI(Document qtiDocument) {
		// Look for common description fields in QTI metadata
		String description = XPathHelper.getNodeValue("//qtimetadatafield[fieldlabel='bank_description']/fieldentry", qtiDocument);
		
		if (description == null || description.isEmpty()) {
			description = XPathHelper.getNodeValue("//qtimetadatafield[fieldlabel='description']/fieldentry", qtiDocument);
		}
		
		return description;
	}
}