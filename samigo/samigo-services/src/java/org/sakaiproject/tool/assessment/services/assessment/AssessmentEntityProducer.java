package org.sakaiproject.tool.assessment.services.assessment;

import org.sakaiproject.entity.cover.EntityManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AssessmentEntityProducer implements EntityTransferrer,
		EntityProducer {

	private static Log log = LogFactory.getLog(AssessmentEntityProducer.class);

	public void init() {
		log.info("init()");
		try {
			EntityManager.registerEntityProducer(this, Entity.SEPARATOR
					+ "samigo");
		} catch (Exception e) {
			log.warn("Error registering Samigo Entity Producer", e);
		}
	}

	public void destroy() {
	}

	public String[] myToolIds() {
		String[] toolIds = { "sakai.samigo" };
		return toolIds;
	}

	public void transferCopyEntities(String fromContext, String toContext,
			List ids) {
		AssessmentService service = new AssessmentService();
		service.copyAllAssessments(fromContext, toContext);
	}

	public String archive(String siteId, Document doc, Stack stack,
			String archivePath, List attachments) {
		return null;
	}

	public Entity getEntity(Reference ref) {
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

	public String getEntityDescription(Reference ref) {
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return null;
	}

	public String getEntityUrl(Reference ref) {
		return null;
	}

	public HttpAccess getHttpAccess() {
		return null;
	}

	public String getLabel() {
		return "samigo";
	}

	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref) {
		return false;
	}

	public boolean willArchiveMerge() {
		return false;
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{	
		//TODO
	}
}
