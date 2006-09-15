package org.sakaiproject.importer.api;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface IMSResourceTranslator {
	
	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath);
	
	public String getTypeName();
	
	public boolean processResourceChildren();

}
