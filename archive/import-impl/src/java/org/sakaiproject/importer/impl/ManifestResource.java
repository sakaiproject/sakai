package org.sakaiproject.importer.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface ManifestResource {
	
	String getTitle(Node resourceNode);
	
	String getType(Node resourceNode);
	
	String getDescription(Node resourceNode);
	
	String getId(Node resourceNode);
	
	Document getDescriptor(Node resourceNode);
	
	boolean isFolder(Document descriptor);

}
