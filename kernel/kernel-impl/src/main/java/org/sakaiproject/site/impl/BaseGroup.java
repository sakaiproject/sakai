/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.site.impl;

import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseGroup is an implementation of the Site API Group.
 * </p>
 */
public class BaseGroup implements Group, Identifiable
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BaseGroup.class);

	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The title. */
	protected String m_title = null;

	/** The description. */
	protected String m_description = null;

	/** The site id. */
	protected String m_id = null;

	/** The properties. */
	protected ResourcePropertiesEdit m_properties = null;

	/** The site I belong to. */
	protected Site m_site = null;

	/** The azg from the AuthzGroupService that is my AuthzGroup impl. */
	protected AuthzGroup m_azg = null;

	/** Set to true if we have changed our azg, so it need to be written back on save. */
	protected boolean m_azgChanged = false;

	private BaseSiteService siteService;

	/**
	 * Construct. Auto-generate the id.
	 * 
	 * @param site
	 *        The site in which this page lives.
	 */
	protected BaseGroup(BaseSiteService siteService, Site site)
	{
		this.siteService = siteService;

		if (site == null) M_log.warn("BaseGroup(site) created with null site");

		m_site = site;
		m_id = IdManager.createUuid();
		m_properties = new BaseResourcePropertiesEdit();
	}

	protected BaseGroup(BaseSiteService siteService, String id, String title, String description, Site site)
	{
		this.siteService = siteService;
		if (site == null) M_log.warn("BaseGroup(..., site) created with null site");

		m_id = id;
		m_title = title;
		m_description = description;
		m_site = site;
		m_properties = new BaseResourcePropertiesEdit();
	}

	/**
	 * Construct as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 * @param site
	 *        The site in which this group lives.
	 * @param exact
	 *        If true, we copy id - else we generate a new one.
	 */
	protected BaseGroup(BaseSiteService siteService, Group other, Site site, boolean exact)
	{
		this.siteService = siteService;
		if (site == null) M_log.warn("BaseGroup(other, site...) created with null site");

		BaseGroup bOther = (BaseGroup) other;

		m_site = (Site) site;

		if (exact)
		{
			m_id = bOther.m_id;
		}
		else
		{
			m_id = IdManager.createUuid();
		}

		m_title = bOther.m_title;
		m_description = bOther.m_description;

		m_properties = new BaseResourcePropertiesEdit();
		m_properties.addAll(other.getProperties());
		((BaseResourcePropertiesEdit) m_properties).setLazy(((BaseResourceProperties) other.getProperties()).isLazy());
	}

	/**
	 * @inheritDoc
	 */
	public String getTitle()
	{
		return m_title;
	}

	/**
	 * @inheritDoc
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * @inheritDoc
	 */
	public Site getContainingSite()
	{
		return m_site;
	}

	/**
	 * @inheritDoc
	 */
	public void setTitle(String title)
	{
		m_title = title;
	}

	/**
	 * @inheritDoc
	 */
	public void setDescription(String description)
	{
		m_description = description;
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public String getReference()
	{
		return siteService.siteGroupReference(m_site.getId(), getId());
	}

	/**
	 * @inheritDoc
	 */
	public String getReference(String rootProperty)
	{
		return getReference();
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl(String rootProperty)
	{
		return getUrl();
	}

	/**
	 * @inheritDoc
	 */
	public String getId()
	{
		return m_id;
	}

	/**
	 * @inheritDoc
	 */
	public ResourceProperties getProperties()
	{
		return m_properties;
	}

	/**
	 * @inheritDoc
	 */
	public Element toXml(Document doc, Stack stack)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isActiveEdit()
	{
		return true;
	}

	/**
	 * @inheritDoc
	 */
	public ResourcePropertiesEdit getPropertiesEdit()
	{
		return m_properties;
	}

	/**
	 * @inheritDoc
	 */
	public String toString()
	{
		return m_title + " (" + m_id + ")";
	}

	/**
	 * @inheritDoc
	 */
	public boolean equals(Object obj)
	{
        if (this == obj)
            return true;
        if (obj == null)
            return false;
		if (obj instanceof Group)
		{
			return ((Group) obj).getId().equals(getId());
		} 
        // NOTE: findbugs considers this bad practice
		else if (obj instanceof String)
		{
			return ((String) obj).equals(getId());
		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}

	/**
	 * @inheritDoc
	 */
	public int compareTo(Object obj)
	{
		if (!(obj instanceof Group)) throw new ClassCastException();

		// if the object are the same, say so
		if (obj == this) return 0;

		// start the compare by comparing their title
		int compare = getTitle().compareTo(((Site) obj).getTitle());

		// if these are the same
		if (compare == 0)
		{
			// sort based on (unique) id
			compare = getId().compareTo(((Group) obj).getId());
		}

		return compare;
	}

	/**
	 * Access (find if needed) the azg from the AuthzGroupService that implements my grouping.
	 * 
	 * @return My azg.
	 */
	protected AuthzGroup getAzg()
	{
		if (m_azg == null)
		{
			try
			{
				m_azg = AuthzGroupService.getAuthzGroup(getReference());
			}
			catch (GroupNotDefinedException e)
			{
				try
				{
					// create the group's azg, but don't store it yet (that happens if save is called)
					// use a template, but assign no user any maintain role

					// find the template for the new azg
					String groupAzgTemplate = siteService.groupAzgTemplate(m_site);
					AuthzGroup template = null;
					try
					{
						template = AuthzGroupService.getAuthzGroup(groupAzgTemplate);
					}
					catch (Exception e1)
					{
						try
						{
							// if the template is not defined, try the fall back template
							template = AuthzGroupService.getAuthzGroup("!group.template");
						}
						catch (Exception e2)
						{
						}
					}

					m_azg = AuthzGroupService.newAuthzGroup(getReference(), template, null);
					m_azgChanged = true;
				}
				catch (Throwable t)
				{
					M_log.warn("getAzg: " + t);
				}
			}
		}

		return m_azg;
	}

	public void addMember(String userId, String roleId, boolean active, boolean provided)
	{
		m_azgChanged = true;
		getAzg().addMember(userId, roleId, active, provided);
	}

	public Role addRole(String id) throws RoleAlreadyDefinedException
	{
		m_azgChanged = true;
		return getAzg().addRole(id);
	}

	public Role addRole(String id, Role other) throws RoleAlreadyDefinedException
	{
		m_azgChanged = true;
		return getAzg().addRole(id, other);
	}

	public User getCreatedBy()
	{
		return getAzg().getCreatedBy();
	}

	public Time getCreatedTime()
	{
		return getAzg().getCreatedTime();
	}

	public String getMaintainRole()
	{
		return getAzg().getMaintainRole();
	}

	public Member getMember(String userId)
	{
		return getAzg().getMember(userId);
	}

	public Set getMembers()
	{
		return getAzg().getMembers();
	}

	public User getModifiedBy()
	{
		return getAzg().getModifiedBy();
	}

	public Time getModifiedTime()
	{
		return getAzg().getModifiedTime();
	}

	public String getProviderGroupId()
	{
		return getAzg().getProviderGroupId();
	}

	public Role getRole(String id)
	{
		return getAzg().getRole(id);
	}

	public Set getRoles()
	{
		return getAzg().getRoles();
	}

	public Set getRolesIsAllowed(String function)
	{
		return getAzg().getRolesIsAllowed(function);
	}

	public Role getUserRole(String userId)
	{
		return getAzg().getUserRole(userId);
	}

	public Set getUsers()
	{
		return getAzg().getUsers();
	}

	public Set getUsersHasRole(String role)
	{
		return getAzg().getUsersHasRole(role);
	}

	public Set getUsersIsAllowed(String function)
	{
		return getAzg().getUsersIsAllowed(function);
	}

	public boolean hasRole(String userId, String role)
	{
		return getAzg().hasRole(userId, role);
	}

	public boolean isAllowed(String userId, String function)
	{
		return getAzg().isAllowed(userId, function);
	}

	public boolean isEmpty()
	{
		return getAzg().isEmpty();
	}

	public void removeMember(String userId)
	{
		m_azgChanged = true;
		getAzg().removeMember(userId);
	}

	public void removeMembers()
	{
		m_azgChanged = true;
		getAzg().removeMembers();
	}

	public void removeRole(String role)
	{
		m_azgChanged = true;
		getAzg().removeRole(role);
	}

	public void removeRoles()
	{
		m_azgChanged = true;
		getAzg().removeRoles();
	}

	public void setMaintainRole(String role)
	{
		m_azgChanged = true;
		getAzg().setMaintainRole(role);
	}

	public void setProviderGroupId(String id)
	{
		m_azgChanged = true;
		getAzg().setProviderGroupId(id);
	}

	public boolean keepIntersection(AuthzGroup other)
	{
		boolean changed = getAzg().keepIntersection(other);
		if (changed) m_azgChanged = true;
		return changed;
	}
}
