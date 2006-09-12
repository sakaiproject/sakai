package org.sakaiproject.importer.impl;

import org.w3c.dom.Node;

public interface ManifestItem {
	String getId(Node itemNode);
	
	String getTitle(Node itemNode);
	
	String getDescription(Node itemNode);
	
	String getResourceId(Node itemNode);
}
