package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.TextDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Bb6TextDocumentTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		// Just in case it gets registered accidentally, we don't want it to list itself as a handler
		// for resource/x-bb-document - it would conflict with the the delegator that calls it - Bb6DocumentTranslator
		return "resource/x-bb-document-plain-text";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		String content = XPathHelper.getNodeValue("/CONTENT/BODY/TEXT", descriptor);
		String title = XPathHelper.getNodeValue("/CONTENT/TITLE/@value", descriptor).replaceAll("/", "_");
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		contextPath = contextPath + title; //Validator.escapeResourceName(title);
		TextDocument text = new TextDocument();
		text.setContent(content);
		text.setTitle(title);
		text.setContextPath(contextPath);
		text.setSequenceNum(priority);
		return text;
	}

	public boolean processResourceChildren() {
		return true;
	}
}
