package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.DiscussionTopic;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.sakaiproject.importer.impl.XPathHelper;

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
		DiscussionTopic rv = new DiscussionTopic();
		rv.setTitle(XPathHelper.getNodeValue("//title[1]", descriptor));
		rv.setDescription(XPathHelper.getNodeValue("//text[1]", descriptor));
		return rv;
	}

}
