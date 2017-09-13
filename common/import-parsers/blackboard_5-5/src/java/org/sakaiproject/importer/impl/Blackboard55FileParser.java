/**
 * Copyright (c) 2005-2014 The Apereo Foundation
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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.Folder;
import org.sakaiproject.importer.impl.importables.HtmlDocument;
import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.impl.translators.Bb55AnnouncementTranslator;
import org.sakaiproject.importer.impl.translators.Bb55AssessmentTranslator;
import org.sakaiproject.importer.impl.translators.Bb55ExternalLinkTranslator;
import org.sakaiproject.importer.impl.translators.Bb55StaffInfoTranslator;
import org.sakaiproject.importer.impl.translators.Bb55DocumentTranslator;
import org.sakaiproject.importer.impl.translators.Bb55QuestionPoolTranslator;
import org.sakaiproject.importer.impl.translators.Bb55SurveyTranslator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Blackboard55FileParser extends IMSFileParser {
	
	public static final String ASSESSMENT_GROUP = "Assessments";
	public static final String ANNOUNCEMENT_GROUP = "Announcements";
	
	public Blackboard55FileParser() {
		// eventually, this will be spring-injected, 
		// but it's ok to hard-code this for now
		addResourceTranslator(new Bb55AnnouncementTranslator());
		addResourceTranslator(new Bb55StaffInfoTranslator());
		addResourceTranslator(new Bb55ExternalLinkTranslator());
		addResourceTranslator(new Bb55DocumentTranslator());
		addResourceTranslator(new Bb55QuestionPoolTranslator());
		addResourceTranslator(new Bb55AssessmentTranslator());
		addResourceTranslator(new Bb55SurveyTranslator());
		resourceHelper = new Bb55ResourceHelper();
		itemHelper = new Bb55ItemHelper();
		fileHelper = new Bb55FileHelper();
		manifestHelper = new Bb55ManifestHelper();
	}
	
    // assumption is that 5.5 is tested after 6. This could match 6 archives, but we don't worry
    // about excluding 6.

	public boolean isValidArchive(InputStream fileData) {
		if (super.isValidArchive(fileData)) {
			//TODO check for compliance with IMS 1.0 DTD
			Document manifest = extractFileAsDOM("/imsmanifest.xml", fileData);
			return (XPathHelper.selectNodes("/manifest/organizations/tableofcontents/item",manifest).size() > 0 
				|| XPathHelper.selectNodes("/manifest/resources/resource",manifest).size() > 0

				|| XPathHelper.selectNodes("/manifest/organization/tableofcontents/item", manifest).size() > 0);
		} else return false;
	}

	protected Collection getCategoriesFromArchive(String pathToData) {
		Collection categories = new ArrayList();
		ImportMetadata im;
		Node topLevelItem;
		List topLevelItems = manifestHelper.getTopLevelItemNodes(this.archiveManifest);
		for(Iterator i = topLevelItems.iterator(); i.hasNext(); ) {
			topLevelItem = (Node)i.next();
			im = new BasicImportMetadata();
			im.setId(itemHelper.getId(topLevelItem));
			im.setLegacyTool(itemHelper.getTitle(topLevelItem));
			im.setMandatory(false);
			im.setFileName(".xml");
			im.setSakaiServiceName("ContentHostingService");
			im.setSakaiTool("Resources");
			categories.add(im);
		}
		
		// Figure out if there are assessments 
		if (XPathHelper.selectNodes("//resource[@type='assessment/x-bb-quiz']", this.archiveManifest).size() 
				+ XPathHelper.selectNodes("//resource[@type='assessment/x-bb-pool']", this.archiveManifest).size()
				+ XPathHelper.selectNodes("//resource[@type='assessment/x-bb-survey']", this.archiveManifest).size() > 0) {
			im = new BasicImportMetadata();
			im.setId("assessments");
	                im.setLegacyTool(ASSESSMENT_GROUP);
	                im.setMandatory(false);
	                im.setFileName(".xml");
	                im.setSakaiTool("Tests & Quizzes");
	                categories.add(im);
		}
		
		// Figure out if we need an Announcements category
		if (XPathHelper.selectNodes("//resource[@type='resource/x-bb-announcement']", this.archiveManifest).size() > 0) {
			im = new BasicImportMetadata();
			im.setId("announcements");
	                im.setLegacyTool(ANNOUNCEMENT_GROUP);
	                im.setMandatory(false);
	                im.setFileName(".xml");
	                im.setSakaiTool("Announcements");
	                categories.add(im);
		}
		return categories;
	}

	protected boolean isFolder(Document resourceDescriptor) {
		return "true".equals(XPathHelper.getNodeValue("//ISFOLDER/@value", resourceDescriptor));
	}

	protected boolean isCompoundDocument(Node node, Document resourceDescriptor) {
		return "resource/x-bb-document".equals(XPathHelper.getNodeValue("./@type",node)) &&
	       node.hasChildNodes() && (node.getChildNodes().getLength() > 1);
	}
	
	protected boolean wantsCompanionForCompoundDocument() {
		return true;
	}
	
	protected Importable getCompanionForCompoundDocument(Document resourceDescriptor, Folder folder) {
		HtmlDocument html = new HtmlDocument();
		StringBuffer content = new StringBuffer();
		List<Node> linkFileNodes = XPathHelper.selectNodes("//FILEREF/FILEACTION[@value='link']/parent::node()", resourceDescriptor);
		List<Node> embedFileNodes = XPathHelper.selectNodes("//FILEREF/FILEACTION[@value='embed']/parent::node()", resourceDescriptor);
		String documentText = XPathHelper.getNodeValue("//TEXT", resourceDescriptor);
		content.append("<html>\n");
		content.append("  <head><title>" + folder.getTitle() + "</title></head>\n");
		content.append("  <body>\n");
		content.append("    <p>" + documentText + "</p>\n");
		for (Node fileNode : embedFileNodes) {
			content.append(imgTagFromFileNode(fileNode, folder.getTitle()) + "<br/>\n");
		}
		if(linkFileNodes.size() > 0) {
			content.append("    <table border=\"1\">\n");
			for (Node fileNode : linkFileNodes) {
				String fileName = XPathHelper.getNodeValue("./RELFILE/@value", fileNode);
				content.append("      <tr><td><a href=\""+ folder.getTitle() + "/" + fileName + "\">" + fileName + "</a></td></tr>\n");
			}
			content.append("    </table>\n");
		}
		content.append("  </body>\n");
		content.append("</html>");
		html.setContent(content.toString());
		html.setTitle(folder.getTitle());
		html.setContextPath(folder.getPath() + folder.getTitle() + "_manifest");
		html.setLegacyGroup(folder.getLegacyGroup());
		// we want the html document to come before the folder in sequence
		html.setSequenceNum(folder.getSequenceNum() - 1);
		return html;
	}

	private String imgTagFromFileNode(Node fileNode, String folder) {
		String src = folder + "/" + XPathHelper.getNodeValue("./RELFILE/@value", fileNode);
		String height = XPathHelper.getNodeValue("./REGISTRY/REGISTRYENTRY[@key='height']/@value", fileNode);
		String width = XPathHelper.getNodeValue("./REGISTRY/REGISTRYENTRY[@key='width']/@value", fileNode);
		String alt = XPathHelper.getNodeValue("./REGISTRY/REGISTRYENTRY[@key='alttext']/@value", fileNode);
		String align = XPathHelper.getNodeValue("./REGISTRY/REGISTRYENTRY[@key='alignment']/@value", fileNode);
		String link = XPathHelper.getNodeValue("./REGISTRY/REGISTRYENTRY[@key='hyperlink']/@value", fileNode);
		String imgTag = "<img src=\""+src+"\" height=\""+height+"\" width=\""+width+"\" align=\""+align+"\" alt=\""+alt+"\" />";
		if ("http://".equals(link)) {
			return imgTag;
		} else {
			String target = "N".equals(XPathHelper.getNodeValue("./REGISTRY/REGISTRYENTRY[@key='launchinnewwindow']/@value", fileNode)) ? "_top" : "_blank";
			return "<a href=\""+link+"\" target=\""+target+"\">" + imgTag + "</a>";
		}
	}

	protected String getFilePathForNode(Node node) {
		String basePath = XPathHelper.getNodeValue("./attribute::baseurl",node.getParentNode());
		String fileName = XPathHelper.getNodeValue("./attribute::href", node);
		return basePath + "/" + fileName;
	}

	protected String getFilenameForNode(Node node) {
		String sourceFilePath = XPathHelper.getNodeValue("./attribute::href", node);
		return (sourceFilePath.lastIndexOf("/") < 0) ? sourceFilePath 
				: sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1);
	}
	
	protected class Bb55ResourceHelper extends ResourceHelper {
		public String getTitle(Node resourceNode) {
			Document descriptor = getDescriptor(resourceNode);
			return XPathHelper.getNodeValue("/CONTENT/TITLE",descriptor);
		}
		
		public String getType(Node resourceNode) {
			return XPathHelper.getNodeValue("./@type", resourceNode);
		}
		
		public String getId(Node resourceNode) {
			return XPathHelper.getNodeValue("./@identifier", resourceNode);
		}
		
		public Document getDescriptor(Node resourceNode) {
			String descriptorFilename = XPathHelper.getNodeValue("./@file",resourceNode);
			DocumentBuilder docBuilder;
		    try {
				docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			    InputStream fis = new FileInputStream(pathToData + "/" + descriptorFilename);
			    return (Document) docBuilder.parse(fis);
			} catch (Exception e) {
				return null;
			}
		}

		public String getDescription(Node resourceNode) {
			Document descriptor = resourceHelper.getDescriptor(resourceNode);
			String desc = XPathHelper.getNodeValue("//TEXT", descriptor);
			return desc;
		}
		
		public boolean isFolder(Document resourceDescriptor) {
			return "true".equals(XPathHelper.getNodeValue("/CONTENT/FLAGS/ISFOLDER/@value", resourceDescriptor));
		}
	}
	
	protected class Bb55ItemHelper extends ItemHelper {

		public String getId(Node itemNode) {
			return XPathHelper.getNodeValue("./@identifier", itemNode);
		}

		public String getTitle(Node itemNode) {
			return XPathHelper.getNodeValue("./@title",itemNode);
		}

		public String getDescription(Node itemNode) {
			String resourceId = XPathHelper.getNodeValue("./@identifierref", itemNode);
			Node resourceNode = manifestHelper.getResourceForId(resourceId, archiveManifest);
			return resourceHelper.getDescription(resourceNode);
		}
		
	}
	
	protected class Bb55ManifestHelper extends ManifestHelper {

		public List getItemNodes(Document manifest) {
			return XPathHelper.selectNodes("//item", manifest);
		}

		public Node getResourceForId(String resourceId, Document manifest) {
			return XPathHelper.selectNode("//resource[@identifier='" + resourceId + "']",archiveManifest);
		}

		public List getResourceNodes(Document manifest) {
			return XPathHelper.selectNodes("//resource", manifest);
		}

		public List getTopLevelItemNodes(Document manifest) {
			return XPathHelper.selectNodes("//tableofcontents/item", manifest);
		}
		
	}
	
	protected class Bb55FileHelper extends FileHelper {
		
		public byte[] getFileBytesForNode(Node node, String contextPath) throws IOException {
			// for Bb, we ignore the context path
			String basePath = XPathHelper.getNodeValue("./@baseurl",node.getParentNode());
			String fileName = XPathHelper.getNodeValue("./@href", node);
			String filePath = basePath + "/" + fileName;
			return getBytesFromFile(new File(pathToData + "/" + filePath));
		}
		
	}

	public ImportFileParser newParser() {
		return new Blackboard55FileParser();
	}

}