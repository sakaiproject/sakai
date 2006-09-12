package org.sakaiproject.importer.impl.importables;

import org.w3c.dom.Document;

/**
 *
 * @author Joshua Ryan joshua.ryan@asu.edu
 *
 */
public class Assessment extends AbstractImportable {
	
	private Document qti;
	private String version;
	
	public String getTypeName() {
		return "sakai-assessment";
	}

	public Document getQti() {
		return qti;
	}

	public void setQti(Document qti) {
		this.qti = qti;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}	
	
}
