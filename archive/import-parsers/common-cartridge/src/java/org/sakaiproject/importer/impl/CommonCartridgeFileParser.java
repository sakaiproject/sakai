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
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.Folder;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.sakaiproject.importer.impl.translators.CCAssessmentTranslator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class CommonCartridgeFileParser extends IMSFileParser {
	private static final String CC_SCHEMA_NAME = "IMS Common Cartridge";
	
	public CommonCartridgeFileParser() {
		// add resource translators here
		addResourceTranslator(new CCAssessmentTranslator());
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
			String descriptorFilename = XPathHelper.getNodeValue("./file/@href",resourceNode);
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
	
	protected class CCItemHelper extends ItemHelper {

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
			return XPathHelper.selectNodes("//organization/item/item/item", manifest);
		}
		
	}
	
	protected class CCFileHelper extends FileHelper {
		
		public String getFilePathForNode(Node node) {
			return XPathHelper.getNodeValue("./@href", node);
		}
	}

}
