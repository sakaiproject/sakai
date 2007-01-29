/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/qti/QTIService.java $
 * $Id: QTIService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.contentpackaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>Copyright: Copyright (c) 2007 Sakai</p>
 * @version $Id: ManifestGenerator 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 */

public class ManifestGenerator {
	private static Log log = LogFactory.getLog(ManifestGenerator.class);
	private String assessmentId;
	private HashMap contentMap = new HashMap();
	
	public ManifestGenerator(String assessmentId) {
		this.assessmentId = assessmentId;
	}
	
	public String getManifest() {
		Document doc = null;
		try {
			doc = readXMLDocument();
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		String xmlString = XmlUtil.getDOMString(doc);
		String newXmlString = xmlString;
		if (xmlString.startsWith("<?xml version")) {
			newXmlString = xmlString.replaceFirst("version=\"1.0\"", "version=\"1.0\"  encoding=\"UTF-8\"");
		}
		log.debug(newXmlString);
		return newXmlString;
	}
	
	public HashMap getContentMap() {
		return contentMap;
	}

	private Document readXMLDocument()
			throws ParserConfigurationException, SAXException, IOException {
		
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
		.newInstance();
		DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		
		Element manifestElement = document.createElement("manifest");
		manifestElement.setAttribute("identifier", "Manifest1");
		document.appendChild(manifestElement);
		
		Element organizationsElement = document.createElement("organizations");
		manifestElement.appendChild(organizationsElement);
		
		Element resourcesElement = document.createElement("resources");
		manifestElement.appendChild(resourcesElement);
		
		Element resourceElement = document.createElement("resource");
		resourceElement.setAttribute("identifier", "Resource1");
		resourcesElement.appendChild(resourceElement);
		
		getAttachments();
		Iterator iter = contentMap.keySet().iterator();
		Element fileElement = null;
		Node fileText = null;
		String filename = null;
		while(iter.hasNext()) {
			filename = ((String) iter.next()).replaceAll(" ", "");
			fileElement = document.createElement("file");
			fileText = document.createTextNode(filename);
			fileElement.setAttribute("href", filename);
			fileElement.appendChild(fileText);
			resourceElement.appendChild(fileElement);
		}
        return document;
	}
	
	private void getAttachments() {
		try {
			AssessmentService assessmentService = new AssessmentService();
			AssessmentFacade assessment = assessmentService.getAssessment(assessmentId);
		
			// Assessment attachment
			AssessmentData assessmentData = (AssessmentData) assessment.getData();
			Set assessmentAttachmentSet = assessmentData.getAssessmentAttachmentSet();
			Iterator assessmentAttachmentIter = assessmentAttachmentSet.iterator();
			byte [] content = null;
			String resourceId = null; // resoureId is also the filename (whole path) in the zip file
			while (assessmentAttachmentIter.hasNext()) {
				AssessmentAttachment assessmentAttachment = (AssessmentAttachment) assessmentAttachmentIter.next();
				resourceId = assessmentAttachment.getResourceId();
				content = ContentHostingService.getResource(resourceId).getContent();
				contentMap.put(resourceId.replace(" ", ""), content);
			}
			
			// Section attachment
			Set sectionSet = assessment.getSectionSet();
			Iterator sectionIter = sectionSet.iterator();
			SectionData sectionData = null;
			Set sectionAttachmentSet = null;
			Iterator sectionAttachmentIter = null;
			SectionAttachment sectionAttachment = null;
			Set itemSet = null;
			ItemData itemData = null;
			Set itemAttachmentSet = null;
			Iterator itemAttachmentIter = null;
			ItemAttachment itemAttachment = null;
			while (sectionIter.hasNext()) {
				sectionData = (SectionData) ((SectionFacade) sectionIter.next()).getData();
				sectionAttachmentSet = sectionData.getSectionAttachmentSet();
				sectionAttachmentIter = sectionAttachmentSet.iterator();
				while (sectionAttachmentIter.hasNext()) {
					sectionAttachment = (SectionAttachment) sectionAttachmentIter.next();
					resourceId = sectionAttachment.getResourceId();
					content = ContentHostingService.getResource(resourceId).getContent();
					contentMap.put(resourceId.replace(" ", ""), content);
				}
				
				itemSet = sectionData.getItemSet();
				Iterator itemIter = itemSet.iterator();
				while (itemIter.hasNext()) {
					itemData = (ItemData) itemIter.next();
					itemAttachmentSet = itemData.getItemAttachmentSet();
					itemAttachmentIter = itemAttachmentSet.iterator();
					while (itemAttachmentIter.hasNext()) {
						itemAttachment = (ItemAttachment) itemAttachmentIter.next();
						resourceId = itemAttachment.getResourceId();
						content = ContentHostingService.getResource(resourceId).getContent();
						contentMap.put(resourceId.replace(" ", ""), content);
					}
				}
			}
		
		} catch (PermissionException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (IdUnusedException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (TypeException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (ServerOverloadException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
