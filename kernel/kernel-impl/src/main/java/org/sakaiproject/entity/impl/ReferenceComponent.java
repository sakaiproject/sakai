/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.entity.impl;

import java.util.Collection;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * Implementation of the Reference API
 * </p>
 * <p>
 * Note: a Reference is immutable.
 * </p>
 */
@Slf4j
public class ReferenceComponent implements Reference
{
	/** The reference string. */
	protected String m_reference = null;

	/** The reference type (a service name string). */
	protected String m_type = "";

	/** The reference sub-type. */
	protected String m_subType = "";

	/** The reference primary id. */
	protected String m_id = null;

	/** The reference containment ids. */
	protected String m_container = null;

	/** Another container, the context id. */
	protected String m_context = null;

	/** Set to true once the values are set. */
	protected boolean m_setAlready = false;

	/** The service owning the entity. */
	protected EntityProducer m_service = null;

	/** These are some more services known. */
	public static final String GRADEBOOK_ROOT = "gradebook";

	public static final String GRADTOOLS_ROOT = "dissertation";

	private UserDirectoryService userDirectoryService;

	/**
	 * Construct with a reference string.
	 * 
	 * @param ref
	 *        The resource reference.
	 * @param entityComponentManager 
	 */
	public ReferenceComponent(EntityManagerComponent entityManagerComponent, String ref)
	{
		m_reference = ref;
		this.userDirectoryService = entityManagerComponent.getUserDirectoryService();
		parse(entityManagerComponent);
	}

	/**
	 * Construct with a Reference.
	 * 
	 * @param ref
	 *        The resource reference.
	 */
	public ReferenceComponent(Reference copyMe)
	{
		ReferenceComponent ref = (ReferenceComponent) copyMe;

		m_reference = ref.m_reference;
		m_type = ref.m_type;
		m_subType = ref.m_subType;
		m_id = ref.m_id;
		m_container = ref.m_container;
		m_context = ref.m_context;
		m_service = ref.m_service;
		this.userDirectoryService = ref.userDirectoryService;
	}

	/**
	 * Access the reference.
	 * 
	 * @return The reference.
	 */
	public String getReference()
	{
		return m_reference;
	}

	/**
	 * Access the type, a service id string.
	 * 
	 * @return The type, a service id string.
	 */
	public String getType()
	{
		return m_type;
	}

	/**
	 * Check if the reference's type is known
	 * 
	 * @return true if known, false if not.
	 */
	public boolean isKnownType()
	{
		return m_type.length() > 0;
	}

	/**
	 * Access the subType.
	 * 
	 * @return The subType.
	 */
	public String getSubType()
	{
		return m_subType;
	}

	/**
	 * Access the primary id.
	 * 
	 * @return The primary id.
	 */
	public String getId()
	{
		return m_id;
	}

	/**
	 * Access a single container id, the from most general (or only)
	 * 
	 * @return The single or most general container, if any.
	 */
	public String getContainer()
	{
		return m_container;
	}

	/**
	 * Access the context id, if any.
	 * 
	 * @return the context id, if any.
	 */
	public String getContext()
	{
		return m_context;
	}

	/**
	 * Find the ResourceProperties object for this reference.
	 * 
	 * @return A ResourcesProperties object found (or constructed) for this reference.
	 */
	public ResourceProperties getProperties()
	{
		ResourceProperties props = null;

		if (m_service != null)
		{
			props = m_service.getEntityResourceProperties(this);
		}

		return props;
	}

	/**
	 * Find the Entity that is referenced.
	 * 
	 * @return The Entity object that this references.
	 */
	public Entity getEntity()
	{
		Entity e = null;

		if (m_service != null)
		{
			e = m_service.getEntity(this);
		}

		return e;
	}

	/**
	 * Access the URL which can be used to access the referenced resource.
	 * 
	 * @return The URL which can be used to access the referenced resource.
	 */
	public String getUrl()
	{
		String url = null;

		if (m_service != null)
		{
			url = m_service.getEntityUrl(this);
		}

		return url;
	}

	/**
	 * @return a description of the resource referenced.
	 */
	public String getDescription()
	{
		String rv = "unknown";

		if (m_service != null)
		{
			rv = m_service.getEntityDescription(this);

			if (rv == null)
			{
				rv = m_service.getLabel() + " " + m_reference;
			}
		}

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getAuthzGroups()
	{
		return getAuthzGroups(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getAuthzGroups(String userId)
	{
		Collection realms = null;

		if (m_service != null)
		{
			realms = m_service.getEntityAuthzGroups(this, userId);
		}

		if (realms == null) realms = new Vector();

		return realms;
	}

	/**
	 * Add the AuthzGroup(s) for context as a site.
	 * 
	 * @param rv
	 *        The list.
	 */
	public void addSiteContextAuthzGroup(Collection rv)
	{
		String context = getContext();
		if (context == null) return;

		// site using context as id
		// TODO: taken from site -ggolden was: rv.add(SiteService.siteReference(getContext()));
		rv.add("/site/" + context);

		// site helper
		rv.add("!site.helper");
	}

	/**
	 * Add the AuthzGroup for this user id, or for the user's type template, or for the general template.
	 * 
	 * @param rv
	 *        The list.
	 * @param id
	 *        The user id.
	 */
	public void addUserAuthzGroup(Collection rv, String id)
	{
		if (id == null) {
			id = "";
		}

		// the user's realm (unless it's anon)
		if (id.length() > 0) {
			rv.add(userDirectoryService.userReference(id));
		}

		addUserTemplateAuthzGroup(rv, id);
	}

	/**
	 * Add the AuthzGroup for this user id, or for the user's type template, or for the general template.
	 * 
	 * @param rv
	 *        The list.
	 * @param id
	 *        The user id.
	 */
	public void addUserTemplateAuthzGroup(Collection rv, String id)
	{
		if (id == null) {
			id = "";
		}

		// user type template
		String template = "!user.template";
		try {
			User user = userDirectoryService.getUser(id);
			String type = user.getType();
			if (type != null) {
				rv.add(template + "." + type);
			}
		}
		catch (Exception ignore) {
		}

		// general user template
		rv.add("!user.template");
	}

	/**
	 * Accept the settings for a reference - may be rejected if already set
	 * 
	 * @param type
	 * @param subType
	 * @param id
	 * @param container
	 * @param container2
	 * @param context
	 * @return true if settings are accepted, false if not.
	 */
	public boolean set(String type, String subType, String id, String container, String context)
	{
		if (m_setAlready) return false;

		// these must not be null
		m_type = type;
		m_subType = subType;
		if (m_type == null) m_type = "";
		if (m_subType == null) m_subType = "";

		// these should be null if empty
		m_id = id;
		m_container = container;
		m_context = context;
		if ((m_id != null) && (m_id.length() == 0)) m_id = null;
		if ((m_container != null) && (m_container.length() == 0)) m_container = null;
		if ((m_context != null) && (m_context.length() == 0)) m_context = null;

		m_setAlready = true;

		return true;
	}

	/**
	 * @inheritDoc
	 */
	public void updateReference(String ref)
	{
		m_reference = ref;
	}

	/**
	 * @inheritDoc
	 */
	public EntityProducer getEntityProducer()
	{
		return m_service;
	}
	
	/*
	 * Parse the reference
	 */
	protected void parse(EntityManagerComponent entityManagerComponent)
	{
		if (m_reference == null) return;
		
		EntityProducer service = entityManagerComponent.getEntityProducer(m_reference,this);
		if ( service != null ) {
			m_service = service;
			return;
		}

		if (log.isDebugEnabled()) log.debug("parse(): unhandled reference: " + m_reference);
	}
}
