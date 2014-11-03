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
