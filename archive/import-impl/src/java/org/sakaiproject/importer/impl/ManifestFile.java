package org.sakaiproject.importer.impl;

import java.io.IOException;

import org.w3c.dom.Node;

public interface ManifestFile {
	
	String getFilePathForNode(Node fileNode);
	
	String getFilenameForNode(Node fileNode);
	
	byte[] getFileBytesForNode(Node fileNode) throws IOException;
	
	String getTitle(Node fileNode);

}
