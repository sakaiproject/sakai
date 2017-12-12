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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.authz.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SimpleRole;
import org.sakaiproject.util.Xml;

/**
 * <p>
 * BaseRole is an implementation of the AuthzGroup API Role.
 * </p>
 */
@Slf4j
public class BaseRole implements Role
{
	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The role id. */
	protected String m_id = null;

	/** The locks that make up this. */
	protected Set m_locks = null;

	/** The role description. */
	protected String m_description = null;
	
	/** Whether this is a provider-only role */
	protected boolean m_providerOnly;

	/** Active flag. */
	protected boolean m_active = false;

	/**
	 * creates a new BaseRole, based on a SimpleRole which was exported in a previous
	 * step, in order to support distributed cluster caching.  BaseRole cannot be serialized by
	 * distributed caches because of ClassLoader issues.
	 * @param map
	 */
	public BaseRole(SimpleRole role) {
		if (role != null) {
			m_id = role.getId();
			m_locks = role.getLocks();
			m_description = role.getDescription();
			m_providerOnly = role.isProviderOnly();
			m_active = role.isActive();
		}
	}
	
	/**
	 * Construct.
	 * 
	 * @param id
	 *        The role id.
	 */
	public BaseRole(String id)
	{
		m_id = id;
		m_locks = new HashSet();
	}

	/**
	 * Construct as a copy
	 * 
	 * @param id
	 *        The role id.
	 * @param other
	 *        The role to copy.
	 */
	public BaseRole(String id, Role other)
	{
		m_id = id;
		m_description = ((BaseRole) other).m_description;
		m_providerOnly = ((BaseRole) other).m_providerOnly;
		m_locks = new HashSet();
		m_locks.addAll(((BaseRole) other).m_locks);
	}

	/**
	 * Construct from information in XML.
	 * 
	 * @param el
	 *        The XML DOM Element definining the role.
	 */
	public BaseRole(Element el, AuthzGroup azGroup)
	{
		m_locks = new HashSet();
		m_id = StringUtils.trimToNull(el.getAttribute("id"));

		m_description = StringUtils.trimToNull(el.getAttribute("description"));
		if (m_description == null)
		{
			m_description = StringUtils.trimToNull(Xml.decodeAttribute(el, "description-enc"));
		}
		
		if("true".equalsIgnoreCase(el.getAttribute("provider-only"))) m_providerOnly = true;

		// the children (abilities)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for role | lock ability
			if (element.getTagName().equals("ability"))
			{
				String roleId = StringUtils.trimToNull(element.getAttribute("role"));
				String lock = StringUtils.trimToNull(element.getAttribute("lock"));

				if (roleId != null)
				{
					log.warn("(el): nested role: " + m_id + " " + roleId);
				}

				m_locks.add(lock);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element role = doc.createElement("role");

		if (stack.isEmpty())
		{
			doc.appendChild(role);
		}
		else
		{
			((Element) stack.peek()).appendChild(role);
		}

		stack.push(role);

		role.setAttribute("id", getId());

		// encode the description
		if (m_description != null) Xml.encodeAttribute(role, "description-enc", m_description);

		// encode the provider only flag
		if (m_providerOnly) Xml.encodeAttribute(role, "provider-only", "true");

		// locks
		for (Iterator a = m_locks.iterator(); a.hasNext();)
		{
			String lock = (String) a.next();

			Element element = doc.createElement("ability");
			role.appendChild(element);
			element.setAttribute("lock", lock);
		}

		stack.pop();

		return role;
	}

	/**
	 * Enable editing.
	 */
	protected void activate()
	{
		m_active = true;
	}

	/**
	 * Check to see if the azGroup is still active, or has already been closed.
	 * 
	 * @return true if the azGroup is active, false if it's been closed.
	 */
	public boolean isActiveEdit()
	{
		return m_active;
	}

	/**
	 * Close the azGroup object - it cannot be used after this.
	 */
	protected void closeEdit()
	{
		m_active = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return m_id;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription()
	{
		return m_description;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isProviderOnly()
	{
		return m_providerOnly;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isAllowed(String lock)
	{
		return m_locks.contains(lock);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set getAllowedFunctions()
	{
		Set rv = new HashSet();
		rv.addAll(m_locks);
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(String description)
	{
		m_description = StringUtils.trimToNull(description);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProviderOnly(boolean providerOnly)
	{
		m_providerOnly = providerOnly;
	}

	/**
	 * {@inheritDoc}
	 */
	public void allowFunction(String lock)
	{
		m_locks.add(lock);
	}

	/**
	 * {@inheritDoc}
	 */
	public void allowFunctions(Collection locks)
	{
		m_locks.addAll(locks);
	}

	/**
	 * {@inheritDoc}
	 */
	public void disallowFunction(String lock)
	{
		m_locks.remove(lock);
	}

	/**
	 * {@inheritDoc}
	 */
	public void disallowFunctions(Collection locks)
	{
		m_locks.removeAll(locks);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean allowsNoFunctions()
	{
		return m_locks.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	public void disallowAll()
	{
		m_locks.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object obj)
	{
		if (!(obj instanceof Role)) throw new ClassCastException();

		// if the object are the same, say so
		if (obj == this) return 0;

		// sort based on (unique) id
		int compare = getId().compareTo(((Role) obj).getId());

		return compare;
	}
	
	/**
	 * exports the contents of BaseRole to a primitive Map, which can be Serialized
	 * if necessary to support Terracotta cluster caching.  BaseRole cannot be serialized by
	 * Terracotta because of classloader issues.
	 * @return
	 */
	public SimpleRole exportToSimpleRole() {
		SimpleRole role = new SimpleRole();
		role.setId(m_id);
		role.setLocks(m_locks);
		role.setDescription(m_description);
		role.setProviderOnly(m_providerOnly);
		role.setActive(m_active);
		return role;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Role)) return false;

		return ((Role) obj).getId().equals(getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}
}
