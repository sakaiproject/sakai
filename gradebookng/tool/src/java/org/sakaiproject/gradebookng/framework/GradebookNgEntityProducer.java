package org.sakaiproject.gradebookng.framework;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import lombok.Setter;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Entity Producer for GradebookNG. This is required to participate in other entity actions.
 * All operations are no-ops.
 */
public class GradebookNgEntityProducer implements EntityProducer {

	protected final static String LABEL = "GradebookNG";
	protected final static String referenceRoot = "/gradebookng";
	
	@Setter
	protected EntityManager entityManager;
	
	/**
	 * Register this class as an EntityProducer.
	 */
	public void init() {
	    entityManager.registerEntityProducer(this, referenceRoot);
	}

	@Override
	public String getLabel() {
		return LABEL;
	}

	@Override
	public boolean willArchiveMerge() {
		return false;
	}

	@Override
	public String archive(String siteId, Document doc, Stack<Element> stack,
			String archivePath, List<Reference> attachments) {
		return null;
	}

	@Override
	public String merge(String siteId, Element root, String archivePath,
			String fromSiteId, Map<String, String> attachmentNames,
			Map<String, String> userIdTrans, Set<String> userListAllowImport) {
		return null;
	}

	@Override
	public boolean parseEntityReference(String reference, Reference ref) {
		return false;
	}

	@Override
	public String getEntityDescription(Reference ref) {
		return null;
	}

	@Override
	public ResourceProperties getEntityResourceProperties(Reference ref) {
		return null;
	}

	@Override
	public Entity getEntity(Reference ref) {
		return null;
	}

	@Override
	public String getEntityUrl(Reference ref) {
		return null;
	}

	@Override
	public Collection<String> getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

	@Override
	public HttpAccess getHttpAccess() {
		return null;
	}

}
