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

package org.sakaiproject.importer.impl.translators;

import java.io.FileInputStream;
import java.io.InputStream;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.HtmlDocument;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.sakaiproject.importer.impl.XPathHelper;

public class CCWebContentTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "webcontent";
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		
		WebLink rv = new WebLink();
		String href = XPathHelper.getNodeValue("./@href", resourceNode);
		Document factoryDocument = resourceNode.getOwnerDocument();
		Element fileElement = factoryDocument.createElement("file");
		fileElement.setAttribute("href", href);
		resourceNode.appendChild(fileElement);
		// String filename = href.substring(href.lastIndexOf("/"),href.length() - 1);
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		String title = ((Element)resourceNode).getAttribute("title");
		rv.setAbsolute(false);
		rv.setUrl(href);
		rv.setContextPath(contextPath + title);
		rv.setTitle(title);
		rv.setSequenceNum(priority);
		return rv;
	}

	public boolean processResourceChildren() {
		return true;
	}
	
	

}
