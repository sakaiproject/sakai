/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.importer.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.Folder;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.sakaiproject.importer.impl.translators.CCAssessmentTranslator;
import org.sakaiproject.importer.impl.translators.CCDiscussionTopicTranslator;
import org.sakaiproject.importer.impl.translators.CCLearningApplicationResourceTranslator;
import org.sakaiproject.importer.impl.translators.CCWebContentTranslator;
import org.sakaiproject.importer.impl.translators.CCWebLinkTranslator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import sun.util.logging.resources.logging;

public class CommonCartridgeFileParser extends IMSFileParser {
	private static final String CC_SCHEMA_NAME = "IMS Common Cartridge";
	
	public CommonCartridgeFileParser() {
		// add resource translators here
		addResourceTranslator(new CCAssessmentTranslator());
		addResourceTranslator(new CCWebLinkTranslator());
		addResourceTranslator(new CCWebContentTranslator());
		addResourceTranslator(new CCDiscussionTopicTranslator());
		addResourceTranslator(new CCLearningApplicationResourceTranslator());
		resourceHelper = new CCResourceHelper();
		itemHelper = new CCItemHelper();
		fileHelper = new CCFileHelper();
		manifestHelper = new CCManifestHelper();
	}

	public boolean isValidArchive(byte[] fileData) {
		if (super.isValidArchive(fileData)) {
			Document manifest = extractFileAsDOM("/imsmanifest.xml", fileData);
			return CC_SCHEMA_NAME.equals(XPathHelper.getNodeValue("/manifest/metadata/schema", manifest));
		} else return false;
	}
	
	public ImportFileParser newParser() {
		return new CommonCartridgeFileParser();
	}

	protected Collection getCategoriesFromArchive(String pathToData) {
		Collection categories = new ArrayList();
		ImportMetadata im;
//		if(XPathHelper.getNodeValue("//resource[@type='webcontent']", this.archiveManifest) != null) {
//			im = new BasicImportMetadata();
//			im.setId("webcontent");
//			im.setLegacyTool("Web Content");
//			im.setMandatory(false);
//			im.setFileName(".xml");
//			im.setSakaiServiceName("ContentHostingService");
//			im.setSakaiTool("Resources");
//			categories.add(im);
//		}
//		if(XPathHelper.getNodeValue("//resource[@type='imsqti_xmlv1p2/imscc_xmlv1p0/assessment']", this.archiveManifest) != null) {
//			im = new BasicImportMetadata();
//			im.setId("assessments");
//			im.setLegacyTool("Assessments");
//			im.setMandatory(false);
//			im.setFileName(".xml");
//			im.setSakaiServiceName("Samigo");
//			im.setSakaiTool("Tests and Quizzes");
//			categories.add(im);
//		}
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
		return categories;
	}

	protected boolean isCompoundDocument(Node node, Document resourceDescriptor) {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected class CCResourceHelper extends ResourceHelper {
		public String getTitle(Node resourceNode) {
			String title = null;
			Node itemNode = XPathHelper.selectNode("//item[@identifierref='" + this.getId(resourceNode) + "']", resourceNode.getOwnerDocument());
			if (itemNode != null) {
			title = XPathHelper.getNodeValue("./title", itemNode);
			if (title == null || "".equals(title)) {
				Document descriptor = getDescriptor(resourceNode);
				title = XPathHelper.getNodeValue("/CONTENT/TITLE",descriptor);
				}
			}
			return title;
		}
		
		public String getType(Node resourceNode) {
			return XPathHelper.getNodeValue("./@type", resourceNode);
		}
		
		public String getId(Node resourceNode) {
			return XPathHelper.getNodeValue("./@identifier", resourceNode);
		}
		
		public Document getDescriptor(Node resourceNode) {
			String descriptorFilename = XPathHelper.getNodeValue("./file/@href",resourceNode);
			Document doc = null;
			DocumentBuilder docBuilder;
			InputStream fis = null;
		    try {
                fis = new FileInputStream(pathToData + "/" + descriptorFilename);
				docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			    doc = (Document) docBuilder.parse(fis);
			} catch (FileNotFoundException e) {
                // TODO Auto-generated catch block (this is here since it is not clear what this should do in this case)
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block (this is here since it is not clear what this should do in this case)
                e.printStackTrace();
            } catch (SAXException e) {
                // TODO Auto-generated catch block (this is here since it is not clear what this should do in this case)
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block (this is here since it is not clear what this should do in this case)
                e.printStackTrace();
            } finally {
                if (fis != null) {
    			    try {
    			        fis.close();
                    } catch (IOException e) {
                        // oh well, we tried
                    }
                }
			}
            return doc;
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
	
	protected class CCItemHelper extends ItemHelper {

		public String getId(Node itemNode) {
			return XPathHelper.getNodeValue("./@identifier", itemNode);
		}

		public String getTitle(Node itemNode) {
			return XPathHelper.getNodeValue("./title",itemNode);
		}

		public String getDescription(Node itemNode) {
			String resourceId = XPathHelper.getNodeValue("./@identifierref", itemNode);
			Node resourceNode = manifestHelper.getResourceForId(resourceId, archiveManifest);
			return resourceHelper.getDescription(resourceNode);
		}
		
	}
	
	protected class CCManifestHelper extends ManifestHelper {

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
			List items = XPathHelper.selectNodes("//organization/item", manifest);
			if (items != null && items.size() > 1) return items;
			
			items = XPathHelper.selectNodes("//organization/item/item", manifest);
			if (items != null && items.size() > 1) return items;
			
			return XPathHelper.selectNodes("//organization/item/item/item", manifest);
		}
		
	}
	
	protected class CCFileHelper extends FileHelper {
		
		public String getFilePathForNode(Node node, String basePath) {
			return XPathHelper.getNodeValue("./@href", node);
		}
	}

	@Override
	protected Importable getCompanionForCompoundDocument(Document resourceDescriptor, Folder folder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean wantsCompanionForCompoundDocument() {
		// TODO Auto-generated method stub
		return false;
	}

}
