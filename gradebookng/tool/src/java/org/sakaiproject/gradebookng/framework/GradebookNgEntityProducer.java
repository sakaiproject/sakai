package org.sakaiproject.gradebookng.framework;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.Setter;

/**
 * Entity Producer for GradebookNG. This is required to participate in other entity actions. All operations are no-ops.
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
		this.entityManager.registerEntityProducer(this, referenceRoot);
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
	public String archive(final String siteId, final Document doc, final Stack<Element> stack,
			final String archivePath, final List<Reference> attachments) {
		return null;
	}

	@Override
	public String merge(final String siteId, final Element root, final String archivePath,
			final String fromSiteId, final Map<String, String> attachmentNames,
			final Map<String, String> userIdTrans, final Set<String> userListAllowImport) {
		return null;
	}

	@Override
	public boolean parseEntityReference(final String reference, final Reference ref) {
		return false;
	}

	@Override
	public String getEntityDescription(final Reference ref) {
		return null;
	}

	@Override
	public ResourceProperties getEntityResourceProperties(final Reference ref) {
		return null;
	}

	@Override
	public Entity getEntity(final Reference ref) {
		return null;
	}

	@Override
	public String getEntityUrl(final Reference ref) {
		return null;
	}

	@Override
	public Collection<String> getEntityAuthzGroups(final Reference ref, final String userId) {
		return null;
	}

	@Override
	public HttpAccess getHttpAccess() {
		return null;
	}

}
