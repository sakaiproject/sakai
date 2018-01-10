/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/common/branches/sakai-10.x/import-parsers/common-cartridge/src/java/org/sakaiproject/importer/impl/CommonCartridgeFileParser.java $
 * $Id: CommonCartridgeFileParser.java 118267 2013-01-10 22:29:52Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.archive.api.ImportMetadata;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.LBCCResource;
import org.sakaiproject.importer.impl.importables.Folder;

public class LBCommonCartridgeFileParser extends IMSFileParser {
    private static final String CC_NAMESPACE_URIS[] = {"http://www.imsglobal.org/xsd/imscc/imscp_v1p1",
						       "http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1",
						       "http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1",
						       "http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1"};
	
	public LBCommonCartridgeFileParser() {
	}

    InputStream inputStream = null;

	public boolean isValidArchive(InputStream fileData) {
		inputStream = fileData;
		if (super.isValidArchive(fileData)) {
			Document manifest = extractFileAsDOM("/imsmanifest.xml", fileData);
			for (int i = 0; i < CC_NAMESPACE_URIS.length; i++) {
			    if (enclosingDocumentContainsNamespaceDeclaration(manifest, CC_NAMESPACE_URIS[i]))
				return true;
			}
			return false;
		} else return false;
	}
	
	private boolean enclosingDocumentContainsNamespaceDeclaration(Node node, String nameSpaceURI) {
	    return node.isDefaultNamespace(nameSpaceURI);
	}

	public ImportFileParser newParser() {
		return new LBCommonCartridgeFileParser();
	}

	// we don't know the namespace, because it's different for each version.
	// I haven't managed to get this copy of xpath to ignore namespace
        // Indeed getlocalname doesn't work either

	public Node findNode(Node node, String tag) {
	    Node ret = null;
	    String ntag = ":" + tag;
	    NodeList nodes = node.getChildNodes();
	    for (int i = 0; i < nodes.getLength(); i++) {
		Node n = nodes.item(i);
		String name = n.getNodeName();
		if (name != null && (name.equals(tag) || name.endsWith(ntag))) {
		    ret = n;
		    break;
		}
	    }
	    return ret;
	}

	protected Collection getCategoriesFromArchive(String pathToData) {
		Collection categories = new ArrayList();

		ImportMetadata im;
		Node node = XPathHelper.selectNode("/manifest/metadata",this.archiveManifest);
		if (node != null)
		    node = findNode(node, "lom");
		if (node != null)
		    node = findNode(node, "general");
		if (node != null)
		    node = findNode(node, "title");

		String title = "Lesson";
		if (node != null)
		    title = node.getTextContent();
		if (title == null || title.equals(""))
		    title = "Lesson";

		im = new BasicImportMetadata();
		im.setId("cc-item");
		im.setLegacyTool(title);
		im.setMandatory(false);
		im.setFileName(".xml");
		im.setSakaiServiceName("Lessons");
		im.setSakaiTool("Lessons");
		categories.add(im);

		return categories;
	}
	
	protected Collection getImportableItemsFromArchive(String pathToData) {
		Collection items = new ArrayList();

		LBCCResource file = new LBCCResource();

		String fileName = "";
		file.setFileName(pathToData);
		file.setDescription("");
		file.setContentType("lessonbuilder-cc-file");
		String title = XPathHelper.getNodeValue("/manifest/metadata/lom/general/title", this.archiveManifest);
		file.setTitle(title);
		file.setLegacyGroup("");

		items.add(file);
		return items;

	}  

	protected boolean isCompoundDocument(Node node, Document resourceDescriptor) {
		// TODO Auto-generated method stub
		return false;
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
