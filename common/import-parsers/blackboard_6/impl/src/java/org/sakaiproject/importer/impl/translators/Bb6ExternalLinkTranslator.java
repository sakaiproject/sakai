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
package org.sakaiproject.importer.impl.translators;

import java.io.UnsupportedEncodingException;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Bb6ExternalLinkTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "resource/x-bb-externallink";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		String url = XPathHelper.getNodeValue("/CONTENT/URL/@value", descriptor);
		String title = XPathHelper.getNodeValue("/CONTENT/TITLE/@value", descriptor).replaceAll("/", "_");
		try {
			contextPath = contextPath + java.net.URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
		String description = XPathHelper.getNodeValue("/CONTENT/BODY/TEXT", descriptor);
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		WebLink link = new WebLink();
		link.setUrl(url);
		link.setTitle(title);
		link.setSequenceNum(priority);
		link.setContextPath(contextPath);
		link.setDescription(description);
		link.setAbsolute(url.indexOf("://") > -1);
		return link;
	}

	public boolean processResourceChildren() {
		return true;
	}
}
