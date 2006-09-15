package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.WebLink;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CCWebLinkTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "imswl_xmlv1p0";
	}

	public boolean processResourceChildren() {
		return false;
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		WebLink rv = new WebLink();
		String url = XPathHelper.getNodeValue("//url/@href", descriptor);
		String title = XPathHelper.getNodeValue("//title", descriptor);
		rv.setTitle(title);
		rv.setUrl(url);
		rv.setAbsolute(url.lastIndexOf("://") > 0);
		rv.setContextPath(contextPath + title);
		return rv;
	}

}
