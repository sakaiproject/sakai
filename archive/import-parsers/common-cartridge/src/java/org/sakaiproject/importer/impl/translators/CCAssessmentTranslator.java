package org.sakaiproject.importer.impl.translators;

import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.Assessment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CCAssessmentTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "imsqti_xmlv1p2/imscc_xmlv1p0/assessment";
	}

	public Importable translate(Node resourceNode, Document descriptor,
			String contextPath, String archiveBasePath) {
		Assessment rv = new Assessment();
		rv.setQti(descriptor);
		rv.setVersion("1.2");
		return rv;
	}

}
