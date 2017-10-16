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

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.HtmlDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Bb6HTMLDocumentTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		// Not a real BB type - created to provide sub-handler for HTML
		return "resource/x-bb-document-html";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		String content = XPathHelper.getNodeValue("/CONTENT/BODY/TEXT", descriptor);
		String title = XPathHelper.getNodeValue("/CONTENT/TITLE/@value", descriptor).replaceAll("/", "_");
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		// HTML docs in the archive have the < character replaced with the equivalent HTML entity
		// Without this, the TEXT node contents would be treated as sub-nodes.
		content.replaceAll("&lt;", "<");
		contextPath = contextPath + title; //Validator.escapeResourceName(title);
		HtmlDocument html = new HtmlDocument();
		html.setContent(content);
		html.setTitle(title);
		html.setContextPath(contextPath);
		html.setSequenceNum(priority);
		return html;
	}

	public boolean processResourceChildren() {
		return true;
	}
}
