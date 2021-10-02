/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.sakaiproject.grading.api.model.Gradebook;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.CategoryDefinition;
import org.sakaiproject.grading.api.GradebookInformation;
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

	@Setter
	protected EntityManager entityManager;

	@Setter
	protected GradingService gradingService;

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

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options) {

		final Gradebook gradebook = (Gradebook) this.gradingService.getGradebook(fromContext);

		final GradebookInformation gradebookInformation = this.gradingService.getGradebookInformation(gradebook.getUid());

		final List<Assignment> assignments = this.gradingService.getAssignments(fromContext);

		return this.gradingService.transferGradebook(gradebookInformation, assignments, toContext, fromContext);
	}

	@Override
	public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> options, boolean cleanup) {

		if (cleanup == true) {

			final Gradebook gradebook = (Gradebook) this.gradingService.getGradebook(toContext);

			// remove assignments in 'to' site
			final List<Assignment> assignments = this.gradingService.getAssignments(gradebook.getUid());
			assignments.forEach(a -> this.gradingService.removeAssignment(a.getId()));

			// remove categories in 'to' site
			final List<CategoryDefinition> categories = this.gradingService.getCategoryDefinitions(gradebook.getUid());
			categories.forEach(c -> this.gradingService.removeCategory(c.getId()));
		}

		// now migrate
		return this.transferCopyEntities(fromContext, toContext, ids, null);
	}

	@Override
	public void updateEntityReferences(String toContext, Map<String, String> transversalMap) {
		//not necessary
	}
}
