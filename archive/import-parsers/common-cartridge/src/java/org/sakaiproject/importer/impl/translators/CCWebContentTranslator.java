package org.sakaiproject.importer.impl.translators;

import java.io.FileInputStream;
import java.io.IOException;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.HtmlDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.sakaiproject.importer.impl.XPathHelper;

public class CCWebContentTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "webcontent";
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		HtmlDocument rv = new HtmlDocument();
		String href = XPathHelper.getNodeValue("./@href", resourceNode);
		String filename = href.substring(href.lastIndexOf("/"),href.length() - 1);
		try {
	        FileInputStream file = new FileInputStream (archiveBasePath + "/" + href);
	        byte[] b = new byte[file.available()];
	        file.read(b);
	        file.close ();
	        rv.setContent(new String (b));
	        rv.setContextPath(contextPath);
	        rv.setTitle(filename);
		} catch (IOException e) {
			return null;
		}
		return rv;
	}

	public boolean processResourceChildren() {
		return true;
	}
	
	

}
