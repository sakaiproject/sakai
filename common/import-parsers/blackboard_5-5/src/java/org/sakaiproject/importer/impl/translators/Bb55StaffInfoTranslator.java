package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Bb55StaffInfoTranslator implements IMSResourceTranslator{

	public String getTypeName() {
		return "resource/x-bb-staffinfo";
	}

	public Importable translate(Node archiveResource, Document descriptor, String contextPath, String archiveBasePath) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean processResourceChildren() {
		return true;
	}

}
