package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Bb55ExternalLinkTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "resource/x-bb-externallink";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		String url = XPathHelper.getNodeValue("/EXTERNALLINK/URL/@value", descriptor);
		String title = XPathHelper.getNodeValue("/EXTERNALLINK/TITLE/@value", descriptor);
		int priority = Integer.parseInt(((Element)resourceNode).getAttribute("priority"));
		contextPath = contextPath + title; //Validator.escapeResourceName(title);
		String description = XPathHelper.getNodeValue("/EXTERNALLINK/TEXT", descriptor);
		WebLink link = new WebLink();
		link.setUrl(url);
		link.setTitle(title);
		link.setContextPath(contextPath);
		link.setDescription(description);
		link.setAbsolute(url.startsWith("http://"));
		link.setSequenceNum(priority);
		return link;
	}

	public boolean processResourceChildren() {
		return true;
	}

}
