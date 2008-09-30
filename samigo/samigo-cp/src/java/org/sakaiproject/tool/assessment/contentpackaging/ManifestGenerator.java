/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/services/qti/QTIService.java $
 * $Id: QTIService.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.DOMWriter;
import org.sakaiproject.component.cover.ServerConfigurationService; 
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.xml.sax.SAXException;
import org.sakaiproject.content.api.ContentHostingService;

/**
 * <p>
 * Copyright: Copyright (c) 2007 Sakai
 * </p>
 * 
 * @version $Id: ManifestGenerator 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 */

public class ManifestGenerator {
	private static Log log = LogFactory.getLog(ManifestGenerator.class);

	private String assessmentId;

	private HashMap contentMap = new HashMap();
	
	private ContentHostingService contentHostingService;

	public ManifestGenerator(String assessmentId) {
		this.assessmentId = assessmentId;
	}

	public String getManifest() {
		org.w3c.dom.Document document = null;
		try {
			Document doc = readXMLDocument();
			document = new DOMWriter().write(doc);
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		} catch (DocumentException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		if (document == null) {
			log.info("document == null");
			return "";
		}
		String xmlString = XmlUtil.getDOMString(document);
		String newXmlString = xmlString;
		if (xmlString.startsWith("<?xml version")) {
			newXmlString = xmlString.replaceFirst("version=\"1.0\"",
					"version=\"1.0\"  encoding=\"UTF-8\"");
		}
		log.debug(newXmlString);
		return newXmlString;
	}

	public HashMap getContentMap() {
		return contentMap;
	}

	private Document readXMLDocument() throws ParserConfigurationException,
			SAXException, IOException {

		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("root");
		root.addAttribute("identifier", "Manifest1");
		// Set up the necessary namespaces
		root.setQName(new QName("manifest", new Namespace(null,
				"http://www.imsglobal.org/xsd/imscp_v1p1")));
		root.add(new Namespace("imsmd",
				"http://www.imsglobal.org/xsd/imsmd_v1p2"));
		root.add(new Namespace("xsi",
				"http://www.w3.org/2001/XMLSchema-instance"));

		root.addAttribute("xsi:schemaLocation",
				"http://www.imsglobal.org/xsd/imscp_v1p1 "
						+ "http://www.imsglobal.org/xsd/imscp_v1p1.xsd "
						+ "http://www.imsglobal.org/xsd/imsmd_v1p2 "
						+ "http://www.imsglobal.org/xsd/imsmd_v1p2.xsd ");

		root.addElement("organizations");

		Element resourcesElement = DocumentHelper.createElement("resources");
		root.add(resourcesElement);

		Element resourceElement = DocumentHelper.createElement("resource");
		resourceElement.addAttribute("identifier", "Resource1");
		resourcesElement.add(resourceElement);
		
		setContentHostingService();
		getAttachments();
		getFCKAttachments();
		Iterator iter = contentMap.keySet().iterator();
		Element fileElement = null;
		String filename = null;
		while (iter.hasNext()) {
			filename = ((String) iter.next()).replaceAll(" ", "");
			fileElement = resourceElement.addElement("file");
			fileElement.addAttribute("href", filename);
		}
		return document;
	}

	private void getAttachments() {
		try {
			AssessmentService assessmentService = new AssessmentService();
			AssessmentFacade assessment = assessmentService
					.getAssessment(assessmentId);

			// Assessment attachment
			AssessmentData assessmentData = (AssessmentData) assessment
					.getData();
			Set assessmentAttachmentSet = assessmentData
					.getAssessmentAttachmentSet();
			Iterator assessmentAttachmentIter = assessmentAttachmentSet
					.iterator();
			byte[] content = null;
			String resourceId = null; // resoureId is also the filename (whole
			// path) in the zip file
			while (assessmentAttachmentIter.hasNext()) {
				AssessmentAttachment assessmentAttachment = (AssessmentAttachment) assessmentAttachmentIter
						.next();
				resourceId = assessmentAttachment.getResourceId();
				content = contentHostingService.getResource(resourceId)
						.getContent();
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
				sectionData = (SectionData) ((SectionFacade) sectionIter.next())
						.getData();
				sectionAttachmentSet = sectionData.getSectionAttachmentSet();
				sectionAttachmentIter = sectionAttachmentSet.iterator();
				while (sectionAttachmentIter.hasNext()) {
					sectionAttachment = (SectionAttachment) sectionAttachmentIter
							.next();
					resourceId = sectionAttachment.getResourceId();
					content = contentHostingService.getResource(resourceId)
							.getContent();
					contentMap.put(resourceId.replace(" ", ""), content);
				}

				itemSet = sectionData.getItemSet();
				Iterator itemIter = itemSet.iterator();
				while (itemIter.hasNext()) {
					itemData = (ItemData) itemIter.next();
					itemAttachmentSet = itemData.getItemAttachmentSet();
					itemAttachmentIter = itemAttachmentSet.iterator();
					while (itemAttachmentIter.hasNext()) {
						itemAttachment = (ItemAttachment) itemAttachmentIter
								.next();
						resourceId = itemAttachment.getResourceId();
						content = contentHostingService.getResource(resourceId)
								.getContent();
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
	
	private void getFCKAttachments() {
		AssessmentService assessmentService = new AssessmentService();
		AssessmentFacade assessment = assessmentService
				.getAssessment(assessmentId);

		// Assessment FCK attachment
		AssessmentData assessmentData = (AssessmentData) assessment.getData();
		processDescription(assessmentData.getDescription());
		processDescription(assessmentData.getAssessmentAccessControl().getSubmissionMessage());
		
		// Section FCK attachment
		Set sectionSet = assessment.getSectionSet();
		Iterator sectionIter = sectionSet.iterator();
		SectionData sectionData = null;
		Set itemSet = null;
		ItemData itemData = null;

		while (sectionIter.hasNext()) {
			sectionData = (SectionData) ((SectionFacade) sectionIter.next())
					.getData();
			processDescription(sectionData.getDescription());

			itemSet = sectionData.getItemSet();
			Iterator itemIter = itemSet.iterator();
			while (itemIter.hasNext()) {
				itemData = (ItemData) itemIter.next();
				// Question Text
				Set itemTextSet = itemData.getItemTextSet();
				ItemText itemText = null;
				Iterator itemTextIter = itemTextSet.iterator();
				while (itemTextIter.hasNext()) {
					itemText = (ItemText) itemTextIter.next();
					processDescription(itemText.getText());
					
					// Answer
					Set answerSet = itemText.getAnswerSet();
					Answer answer = null;
					Iterator answerIter = answerSet.iterator();
					while (answerIter.hasNext()) {
						answer = (Answer) answerIter.next();
						processDescription(answer.getText());
						
						// Answer Feedback
						Set answerFeedbackSet = answer.getAnswerFeedbackSet();
						AnswerFeedback answerFeedback = null;
						Iterator answerFeedbackIter = answerFeedbackSet.iterator();
						while (answerFeedbackIter.hasNext()) {
							answerFeedback = (AnswerFeedback) answerFeedbackIter.next();
							processDescription(answerFeedback.getText());
						}
					}
				}
				
				// Feedback
				Set itemFeedbackSet = itemData.getItemFeedbackSet();
				ItemFeedback itemFeedback = null;
				Iterator itemFeedbackIter = itemFeedbackSet.iterator();
				while (itemFeedbackIter.hasNext()) {
					itemFeedback = (ItemFeedback) itemFeedbackIter.next();
					processDescription(itemFeedback.getText());
				}
			}
		}
	}

	private void processDescription(String description) {
		String prependString = ServerConfigurationService.getAccessUrl()
				+ contentHostingService.REFERENCE_ROOT;
		
		// Hardcode here for now because I cannot find the API to get them
		// Also, it is hardcoded in BaseContentService.java
		String siteCollection = "/group/";
		String userCollection = "/user/";
		String attachment = "/attachment/";
		User user = UserDirectoryService.getCurrentUser();
		String userId = user.getId();
		String eid = user.getEid();

		try {
			if (description != null && description.indexOf("<img ") > -1) {
				byte[] content = null;
				int srcStartIndex = 0;
				int srcEndIndex = 0;
				String src = null;
				String resourceId = null;
				String eidResourceId = null;
				
				String[] splittedString = description.split("<img ");
				for (int i = 0; i < splittedString.length; i++) {
					log.debug("splittedString[" + i + "] = "
							+ splittedString[i]);
					if (splittedString[i].indexOf(prependString) > -1) {
						srcStartIndex = splittedString[i].indexOf("src=\"");
						srcEndIndex = splittedString[i].indexOf("\"",
								srcStartIndex + 5);
						src = splittedString[i].substring(srcStartIndex + 5,
								srcEndIndex);
						
						if (src.indexOf(siteCollection) > -1) {
							resourceId = src.replace(prependString, "");
							content = contentHostingService.getResource(resourceId).getContent();
							if (content != null) {
								contentMap.put(resourceId.replace(" ", ""), content);
							}
						}
						else if (src.indexOf(userCollection) > -1) {
							eidResourceId = src.replace(prependString, "");
							resourceId = eidResourceId.replace(eid, userId);
							content = contentHostingService.getResource(resourceId).getContent();
							if (content != null) {
								contentMap.put(eidResourceId.replace(" ", ""), content);
							}
						}
						else if (src.indexOf(attachment) > -1) {
							resourceId = src.replace(prependString, "");
							content = contentHostingService.getResource(resourceId).getContent();
							if (content != null) {
								contentMap.put(resourceId.replace(" ", ""), content);
							}
						}
						else {
							log.error("Neither group nor user");
						}
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
	
	public void setContentHostingService(){
		if (contentHostingService == null) {
			this.contentHostingService = AssessmentService.getContentHostingService();
		}
	}
}
