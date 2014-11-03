package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class Bb6CollabSessionTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "course/x-bb-collabsession";
	}

	public boolean processResourceChildren() {
		return false;
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		// intentionally do nothing.
		return null;
	}

}
