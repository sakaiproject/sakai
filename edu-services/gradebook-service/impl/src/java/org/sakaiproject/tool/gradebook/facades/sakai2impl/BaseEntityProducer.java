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

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;

/**
 * Utility class that provides safe defaults for all EntityProducer methods.
 * By subclassing, developers can focus only on the methods they
 * actually need to customize. External configuration files can be used to
 * set their label, reference root, and service name. The public "init()" method can be
 * used to register as an EntityProducer.
 *
 * @deprecated This is part of the import/export for gradebook1 which will be removed at some point
 */
@Deprecated
public class BaseEntityProducer implements EntityProducer {
	protected String label;	// This should always be set.
	protected String referenceRoot = "/gradebook"; // stupid default but OK since it is not used
	protected String serviceName = null;

	protected EntityManager entityManager;
	public void setEntityManager(EntityManager entityManager) {
	    this.entityManager = entityManager;
	}

	/**
	 * Register this class as an EntityProducer.
	 */
	public void init() {
	    entityManager.registerEntityProducer(this, referenceRoot);
	}

	public void setLabel(String label) {
		this.label = label;
	}
	public String getReferenceRoot() {
		return referenceRoot;
	}
	public void setReferenceRoot(String referenceRoot) {
		this.referenceRoot = referenceRoot;
	}

	/**
	 * Although not required, the service name is frequently used. As a
	 * convenience, it's also settable by this bean.
	 */
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	// EntityProducer methods begin here.

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean willArchiveMerge() {
		return false;
	}

	@Override
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
		return null;
	}

	@Override
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport) {
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
	public Collection getEntityAuthzGroups(Reference ref, String userId) {
		return null;
	}

	@Override
	public HttpAccess getHttpAccess() {
		return null;
	}
}
