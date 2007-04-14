package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CCDiscussionTopicTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "imsdt_xmlv1p0";
	}

	public boolean processResourceChildren() {
		// we don't want discussion thread XML descriptors to be dropped into Resources
		return false;
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		// TODO Auto-generated method stub
		return null;
	}

}
