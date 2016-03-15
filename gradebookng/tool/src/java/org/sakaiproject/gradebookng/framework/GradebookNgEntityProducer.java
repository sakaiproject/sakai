package org.sakaiproject.gradebookng.framework;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.Setter;

/**
 * Entity Producer for GradebookNG. This is required to participate in other entity actions but also handles the transfer of data between
 * sites
 */
public class GradebookNgEntityProducer implements EntityProducer, EntityTransferrer {

	protected static final String[] TOOL_IDS = { "sakai.gradebookng" };

	protected final static String LABEL = "GradebookNG";
	protected final static String referenceRoot = "/gradebookng";

	/**
	 * These are shared with the GradebookNgContextObserver
	 */
	@Setter
	protected EntityManager entityManager;

	@Setter
	protected GradebookService gradebookService;

	@Setter
	protected GradebookFrameworkService gradebookFrameworkService;

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

	@Override
	public String[] myToolIds() {
		return TOOL_IDS;
	}

	/**
	 * Handle import via merge
	 */
	@Override
	public void transferCopyEntities(final String fromContext, final String toContext, final List<String> ids) {

		final Gradebook gradebook = (Gradebook) this.gradebookService.getGradebook(fromContext);

		final GradebookInformation gradebookInformation = this.gradebookService.getGradebookInformation(gradebook.getUid());

		final List<Assignment> assignments = this.gradebookService.getAssignments(fromContext);

		this.gradebookService.transferGradebook(gradebookInformation, assignments, toContext);
	}

	/**
	 * Handle import via replace
	 */
	@Override
	public void transferCopyEntities(final String fromContext, final String toContext, final List<String> ids, final boolean cleanup) {

		if (cleanup == true) {

			final Gradebook gradebook = (Gradebook) this.gradebookService.getGradebook(toContext);

			// remove assignments in 'to' site
			final List<Assignment> assignments = this.gradebookService.getAssignments(gradebook.getUid());
			assignments.forEach(a -> this.gradebookService.removeAssignment(a.getId()));

			// remove categories in 'to' site
			final List<CategoryDefinition> categories = this.gradebookService.getCategoryDefinitions(gradebook.getUid());
			categories.forEach(c -> this.gradebookService.removeCategory(c.getId()));
		}

		// now migrate
		this.transferCopyEntities(fromContext, toContext, ids);

	}

}
