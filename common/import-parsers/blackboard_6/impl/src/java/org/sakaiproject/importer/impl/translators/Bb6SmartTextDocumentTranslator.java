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
import org.sakaiproject.importer.impl.importables.TextDocument;
import org.sakaiproject.importer.impl.importables.Folder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Bb6SmartTextDocumentTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		// Just in case it gets registered accidentally, we don't want it to list itself as a handler
		// for resource/x-bb-document - it would conflict with the the delegator that calls it - Bb6DocumentTranslator
		return "resource/x-bb-document-smart-text";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		String content = XPathHelper.getNodeValue("/CONTENT/BODY/TEXT", descriptor);
		String title = XPathHelper.getNodeValue("/CONTENT/TITLE/@value", descriptor).trim().replaceAll("\\<.*?\\>", "").replaceAll("/", "_");
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		// SmartText replaces the double-quote with the HTML entity equivalent
		content.replaceAll("&quot;", "\"");
		contextPath = contextPath + title; //Validator.escapeResourceName(title);

		//Some smart text has files, frequently zipped up 'learning modules'
		if (!XPathHelper.selectNode("/CONTENT/FILES", descriptor).hasChildNodes()) {
			TextDocument text = new TextDocument();
			text.setContent(content);
			text.setTitle(title);
			text.setContextPath(contextPath);
			text.setSequenceNum(priority);
			return text;
		}
		else {
			Folder folder = new Folder();
			folder.setDescription(content);
			folder.setTitle(title);
			folder.setPath(contextPath);
			folder.setSequenceNum(priority);
			return folder;
		}
	}

	public boolean processResourceChildren() {
		return true;
	}
}
