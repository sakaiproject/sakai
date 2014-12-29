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
