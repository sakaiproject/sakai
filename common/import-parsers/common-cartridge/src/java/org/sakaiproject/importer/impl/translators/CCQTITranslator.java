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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;

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

	@Override
	public String getTypeName() {
		return "associatedcontent/imscc_xmlv1p1/learning-application-resource";
	}

	@Override
	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		
		// Check if this is actually a QTI file by examining the href attribute
		String href = XPathHelper.getNodeValue("./@href", resourceNode);
		if (href == null) {
			return null;
		}
		String lower = href.toLowerCase();
		if (!(lower.endsWith(".xml.qti") || lower.endsWith(".qti") || lower.endsWith(".xml"))) {
			// Not a QTI file, let the default handler process it
			return null;
		}
		
		Assessment assessment = new Assessment();
		assessment.setVersion("1.2"); // Default to QTI 1.2 for Canvas
		
		try {
			// Securely resolve and validate the QTI file path
			Path archiveBase = Paths.get(archiveBasePath).normalize();
			Path resolvedPath = archiveBase.resolve(href).normalize();
			
			// Prevent path traversal by ensuring resolved path is within archive base
			if (!resolvedPath.startsWith(archiveBase)) {
				log.warn("Path traversal attempt detected: {} resolves to {} which is outside {}", 
					href, resolvedPath, archiveBase);
				return null;
			}
			
			// Configure secure XML parsing
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setXIncludeAware(false);
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document qtiDocument;
			
			// Load and parse the QTI file with proper resource management
			try (InputStream qtiStream = Files.newInputStream(resolvedPath)) {
				qtiDocument = builder.parse(qtiStream);
			}
			
			// Set the QTI document for Samigo to process
			assessment.setQti(qtiDocument);
			
			// Detect QTI version from document and update assessment version if needed
			String detectedVersion = detectQTIVersion(qtiDocument);
			if (detectedVersion.startsWith("2")) {
				// Map any 2.x version to QTI 2.0 for handler compatibility
				assessment.setVersion("2.0");
			}
			
			// Extract title from QTI metadata or objectbank
			String title = extractTitleFromQTI(qtiDocument, resourceNode);
			assessment.setTitle(title);
			
			// Extract description if available
			String description = extractDescriptionFromQTI(qtiDocument);
			assessment.setDescription(description);
			
			// Set context path for proper import hierarchy
			String sanitizedTitle = sanitizePathComponent(title);
			String normalizedContextPath = normalizePathComponent(contextPath);
			String normalizedTitle = normalizePathComponent(sanitizedTitle);
			String fullPath = joinPaths(normalizedContextPath, normalizedTitle);
			assessment.setContextPath(fullPath);
			
			log.debug("Created QTI assessment import object: {}", title);
			
		} catch (Exception e) {
			log.error("Error parsing QTI file {}: {}", href, e.getMessage(), e);
			return null;
		}
		
		return assessment;
	}

	@Override
    public boolean processResourceChildren() {
		return false;
	}
	
	/**
	 * Extract title from QTI document metadata
	 */
	private String extractTitleFromQTI(Document qtiDocument, Node resourceNode) {
		// First try to get bank_title from QTI metadata
		String title = XPathHelper.getNodeValue("//*[local-name()='qtimetadatafield'][*[local-name()='fieldlabel' and text()='bank_title']]/*[local-name()='fieldentry']", qtiDocument);
		
		if (title == null || title.isEmpty()) {
			// Try to get title from the resource node in manifest
			title = XPathHelper.getNodeValue("//*[local-name()='item'][@identifierref='" + 
				XPathHelper.getNodeValue("./@identifier", resourceNode) + "']/*[local-name()='title']", 
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
		String description = XPathHelper.getNodeValue("//*[local-name()='qtimetadatafield'][*[local-name()='fieldlabel' and text()='bank_description']]/*[local-name()='fieldentry']", qtiDocument);
		
		if (description == null || description.isEmpty()) {
			description = XPathHelper.getNodeValue("//*[local-name()='qtimetadatafield'][*[local-name()='fieldlabel' and text()='description']]/*[local-name()='fieldentry']", qtiDocument);
		}
		
		return description;
	}
	
	/**
	 * Sanitize a path component by removing filesystem-unfriendly characters
	 */
	private String sanitizePathComponent(String component) {
		if (component == null || component.isEmpty()) {
			return "";
		}
		
		// Replace path separators and control characters with underscores
		String sanitized = component.replaceAll("[/\\\\\\x00-\\x1F\\x7F]", "_");
		
		// Trim whitespace
		sanitized = sanitized.trim();
		
		return sanitized;
	}
	
	/**
	 * Normalize path component by removing leading and trailing slashes
	 */
	private String normalizePathComponent(String component) {
		if (component == null || component.isEmpty()) {
			return "";
		}
		
		// Remove leading and trailing slashes
		String normalized = component.replaceAll("^/+|/+$", "");
		
		return normalized;
	}
	
	/**
	 * Join two path components with a single separator
	 */
	private String joinPaths(String contextPath, String title) {
		if (contextPath == null || contextPath.isEmpty()) {
			return title != null ? title : "";
		}
		
		if (title == null || title.isEmpty()) {
			return contextPath;
		}
		
		return contextPath + "/" + title;
	}
	
	/**
	 * Detect QTI version from document root element or metadata
	 */
	private String detectQTIVersion(Document qtiDocument) {
		// Check root element for version attribute (QTI 2.x pattern)
		org.w3c.dom.Element root = qtiDocument.getDocumentElement();
		if (root != null) {
			String version = root.getAttribute("version");
			if (!version.isEmpty()) {
				return version;
			}
			
			// Check for QTI 2.x namespace indicators
			String namespaceURI = root.getNamespaceURI();
			if (namespaceURI != null && namespaceURI.contains("2.")) {
				return "2.0";
			}
		}
		
		// Default to 1.2 if no version detected
		return "1.2";
	}
}