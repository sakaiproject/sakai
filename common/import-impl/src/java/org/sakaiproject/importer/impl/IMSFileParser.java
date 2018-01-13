/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.impl.importables.FileResource;
import org.sakaiproject.importer.impl.importables.Folder;

@Slf4j
public abstract class IMSFileParser extends ZipFileParser {
	protected Map<String, Node> resourceMap = new HashMap<String, Node>();
	protected Map<String, IMSResourceTranslator> translatorMap = new HashMap<String, IMSResourceTranslator>();
	protected Map dependencies = new HashMap();
	protected Document archiveManifest;
	protected ResourceHelper resourceHelper;
	protected ItemHelper itemHelper;
	protected FileHelper fileHelper;
	protected ManifestHelper manifestHelper;
	
	protected void awakeFromUnzip(String pathToData) {
		this.pathToData = pathToData;
		String absolutepathToManifest = pathToData + "/" + "imsmanifest.xml";
	    absolutepathToManifest = absolutepathToManifest.replace('\\', '/');
	    InputStream fis = null;
        try {
            fis = new FileInputStream(absolutepathToManifest);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            this.archiveManifest = (Document) docBuilder.parse(fis);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (SAXException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
	}
	
	public void setTranslatorMap(Map translatorMap) {
		this.translatorMap = translatorMap;	
	}
	
	public boolean isValidArchive(InputStream fileData) {
		if (super.isValidArchive(fileData)) {
			if (!fileExistsInArchive("/imsmanifest.xml", fileData)) 
				return false;
			return true;
		} else return false;
	}

	protected abstract Collection getCategoriesFromArchive(String pathToData);

	protected Collection getImportableItemsFromArchive(String pathToData) {
		Collection rv = new ArrayList();
		Document manifest = this.archiveManifest;
		List itemNodes = manifestHelper.getTopLevelItemNodes(manifest);
		List resourceNodes = manifestHelper.getResourceNodes(manifest);
		Node resourceNode;
		// set up a Map of resource Nodes keyed on their identifier attribute
		for(Iterator i = resourceNodes.iterator(); i.hasNext();) {
			resourceNode = (Node) i.next();
			resourceMap.put(resourceHelper.getId(resourceNode), resourceNode);
		}
		Node itemNode;
		int priority = 1;
		for(Iterator i = itemNodes.iterator(); i.hasNext(); ) {
			itemNode = (Node) i.next();
			String title = itemHelper.getTitle(itemNode);
			rv.addAll(translateFromNodeToImportables(itemNode, "", priority, null));
			priority++;
		}
		// the remainder of resources in the resourcesMap need to be processed
		Object[] remainingRes = resourceMap.values().toArray();
		for (int i = 0;i < remainingRes.length; i++) {
			resourceNode = (Node)remainingRes[i];
			rv.addAll(translateFromNodeToImportables(resourceNode, "",i+1, null));
			resourceMap.remove(XPathHelper.getNodeValue("./attribute::identifier", resourceNode));
		}
		return rv;
	}

	protected Collection translateFromNodeToImportables(Node node, String contextPath, int priority, Importable parent) {
		Collection branchOfImportables = new ArrayList();
		String tag = node.getNodeName();
		String itemResourceId = null;
		if ("item".equals(tag)) {
			itemResourceId = itemHelper.getResourceId(node);
		} else if ("resource".equals(tag)) {
			itemResourceId = resourceHelper.getId(node);
		} else if ("file".equals(tag)) {
			itemResourceId = resourceHelper.getId(node.getParentNode());
		}
		Document resourceDescriptor = resourceHelper.getDescriptor(manifestHelper.getResourceForId(itemResourceId, this.archiveManifest));
		if (resourceHelper.isFolder(resourceDescriptor) || 
	  		    ("item".equals(tag) && (XPathHelper.selectNodes("./item", node).size() > 0)) ||
	  		    ( "item".equals(tag) && 
	  	          isCompoundDocument(manifestHelper.getResourceForId(itemResourceId, archiveManifest),resourceDescriptor)
	  		    )) {
			String folderTitle = getTitleForNode(node);
			Folder folder = new Folder();
			folder.setPath(contextPath);
			folder.setTitle(folderTitle);
			folder.setDescription(getDescriptionForNode(node));
			folder.setSequenceNum(priority);
			if (parent != null) {
  				folder.setParent(parent);
  				folder.setLegacyGroup(parent.getLegacyGroup());
  			} else folder.setLegacyGroup(folderTitle);
			// now we take care of the folder's child Nodes
			// construct a new path and make sure we replace any forward slashes from the resource title
			String folderPath = contextPath + folderTitle.replaceAll("/", "_") + "/";
			if (isCompoundDocument(manifestHelper.getResourceForId(itemResourceId, archiveManifest),resourceDescriptor)) {
				if (wantsCompanionForCompoundDocument()) {
					priority++;
					folder.setSequenceNum(priority);
					branchOfImportables.add(getCompanionForCompoundDocument(resourceDescriptor, folder));
				}
				branchOfImportables.addAll(translateFromNodeToImportables(manifestHelper.getResourceForId(itemResourceId, archiveManifest), folderPath, priority, folder));
			} else {
	  			List children = XPathHelper.selectNodes("./item", node);
	  			int childPriority = 1;
	  			for (Iterator i = children.iterator(); i.hasNext();) {
	  				branchOfImportables.addAll(
	  						translateFromNodeToImportables((Node)i.next(),folderPath, childPriority, folder));
	  				childPriority++;
	  			}
			}
  			resourceMap.remove(itemResourceId);
  			branchOfImportables.add(folder);
		} // node is folder
		
		else if("item".equals(tag)) {
			// this item is a leaf, so we handle the resource associated with it
			Node resourceNode = manifestHelper.getResourceForId(itemResourceId, this.archiveManifest);
  			if (resourceNode != null) {
  				if (parent == null) {
  					parent = new Folder();
  					parent.setLegacyGroup(itemHelper.getTitle(node));
  				}
  				branchOfImportables.addAll(
  						translateFromNodeToImportables(resourceNode,contextPath, priority, parent));
  			}
		} else if("file".equals(tag)) {
			FileResource file = new FileResource();
			try {
				String fileName = fileHelper.getFilenameForNode(node);
 				file.setFileName(fileName);
				// If 
				if (node.getParentNode().getChildNodes().getLength() > 1) {
					file.setDescription("");
				} else file.setDescription(resourceHelper.getDescription(node.getParentNode()));
				//Takes too much memory just pass file
				//file.setFileBytes(fileHelper.getFileBytesForNode(node, contextPath));
				file.setInputStream(fileHelper.getInputStreamForNode(node,contextPath));
				file.setDestinationResourcePath(fileHelper.getFilePathForNode(node, contextPath));
				file.setContentType(this.mimeTypes.getContentType(fileName));
				file.setTitle(fileHelper.getTitle(node));
				if(parent != null) {
					file.setParent(parent);
					file.setLegacyGroup(parent.getLegacyGroup());
				} else file.setLegacyGroup("");
			} catch (IOException e) {
				resourceMap.remove(resourceHelper.getId(node.getParentNode()));
				return branchOfImportables;
			}
			branchOfImportables.add(file);
			resourceMap.remove(resourceHelper.getId(node.getParentNode()));
			return branchOfImportables;
		} else if("resource".equals(tag)) {
			// TODO handle a resource node
			Importable resource = null;
			boolean processResourceChildren = true;
			IMSResourceTranslator translator = (IMSResourceTranslator)translatorMap.get(resourceHelper.getType(node));
			if (translator != null) {
				String title = resourceHelper.getTitle(node);
				((Element)node).setAttribute("title", title);
				((Element)node).setAttribute("priority", Integer.toString(priority));
				resource = translator.translate(node, resourceHelper.getDescriptor(node), contextPath, this.pathToData);
				processResourceChildren = translator.processResourceChildren();
			}
			if (resource != null) {
				// make a note of a dependency if there is one.
				String dependency = resourceHelper.getDependency(node);
				if (!"".equals(dependency)) {
					dependencies.put(resourceHelper.getId(node), dependency);
				}
				// section to twiddle with the Importable's legacyGroup,
				// which we only want to do if it hasn't already been set.
				if ((resource.getLegacyGroup() == null) || ("".equals(resource.getLegacyGroup()))) {
					// find out if something depends on this.
					if (dependencies.containsValue(resourceHelper.getId(node))) {
						resource.setLegacyGroup("mandatory");
					} else if (parent != null) {
						resource.setParent(parent);
						resource.setLegacyGroup(parent.getLegacyGroup());
					} else resource.setLegacyGroup(resourceHelper.getTitle(node));
				}
				branchOfImportables.add(resource);
				parent = resource;
			}
			// processing the child nodes implies that their files can wind up in the Resources tool.
			// this is not always desirable, such as the QTI files from assessments.
			if (processResourceChildren) {
				NodeList children = node.getChildNodes();
		  		for (int i = 0;i < children.getLength();i++) {
		  			branchOfImportables.addAll(translateFromNodeToImportables(children.item(i), contextPath, priority, parent));
		  			}
			}
			resourceMap.remove(itemResourceId);
		}
		return branchOfImportables;
	}

	protected abstract Importable getCompanionForCompoundDocument(Document resourceForId, Folder folder);

	protected abstract boolean wantsCompanionForCompoundDocument();

	protected String getTitleForNode(Node node) {
		if ("item".equals(node.getNodeName())) {
			return itemHelper.getTitle(node);
		} else if ("resource".equals(node.getNodeName())) {
			return resourceHelper.getTitle(node);
		} else return "";
	}

	protected String getDescriptionForNode(Node node){
		if ("item".equals(node.getNodeName())) {
			return itemHelper.getDescription(node);
		} else if ("resource".equals(node.getNodeName())) {
			return resourceHelper.getDescription(node);
		} else return "";
	}


	protected abstract boolean isCompoundDocument(Node node, Document resourceDescriptor);
	
	public void addResourceTranslator(IMSResourceTranslator t) {
		translatorMap.put(t.getTypeName(), t);
	}
	
	protected abstract static class ResourceHelper implements ManifestResource {

		public String getDependency(Node node) {
			return XPathHelper.getNodeValue("./dependency/@identifierref", node);
		}
	}
	
	protected abstract static class ItemHelper implements ManifestItem {
		
		public String getResourceId(Node itemNode) {
			return XPathHelper.getNodeValue("./@identifierref", itemNode);
		}
	}
	
	protected abstract class FileHelper implements ManifestFile {
		
		//This should be avoided
		public byte[] getFileBytesForNode(Node node, String contextPath) throws IOException {
			String filePath = getFilePathForNode(node, contextPath);
			return getBytesFromFile(new File(pathToData + "/" + filePath));
		}
		
		
		public InputStream getInputStreamForNode(Node node, String contextPath) throws IOException{
			String filePath = getFilePathForNode(node,contextPath);
			InputStream is = new FileInputStream(filePath);
			return is;
		}
		
		public String getFilePathForNode(Node node, String contextPath) {
			return contextPath + "/" + getFilenameForNode(node);
		}

		public String getTitle(Node fileNode) {
			// if the resource that this file belongs to has multiple files,
			// we just want to use the filename as the title
			if (fileNode.getParentNode().getChildNodes().getLength() > 1) {
				return getFilenameForNode(fileNode);
			} else return resourceHelper.getTitle(fileNode.getParentNode());
		}
		
		public String getFilenameForNode(Node node) {
			String sourceFilePath = XPathHelper.getNodeValue("./@href", node).replaceAll("\\\\", "/");
			return (sourceFilePath.lastIndexOf("/") < 0) ? sourceFilePath 
					: sourceFilePath.substring(sourceFilePath.lastIndexOf("/") + 1);
		}
	}
	
	protected abstract static class ManifestHelper implements Manifest {}

}
