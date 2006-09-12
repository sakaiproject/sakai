package org.sakaiproject.importer.impl;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface Manifest {
	
	Node getResourceForId(String resourceId, Document manifest);
	
	List getItemNodes(Document manifest);
	
	List getResourceNodes(Document manifest);
	
	List getTopLevelItemNodes(Document manifest);

}
