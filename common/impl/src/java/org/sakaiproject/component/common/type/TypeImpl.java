/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.common.type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.component.common.manager.PersistableImpl;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public class TypeImpl extends PersistableImpl implements Type
{
	private static final Log LOG = LogFactory.getLog(TypeImpl.class);

	private String authority;

	private String domain;

	private String keyword;

	private String description;

	private String displayName;

	/**
	 * Simple pattern for implementing a businessKey.
	 * 
	 * @return
	 */
	private String getBusinessKey()
	{
		LOG.trace("getBusinessKey()");

		StringBuilder sb = new StringBuilder();
		sb.append(authority);
		sb.append(domain);
		sb.append(keyword);
		return sb.toString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (LOG.isTraceEnabled())
		{
			LOG.trace("equals(Object " + obj + ")");
		}

		if (this == obj) return true;
		if (!(obj instanceof Type)) return false;
		if (obj instanceof TypeImpl)
		{ // found well known Type
			if (LOG.isDebugEnabled())
			{
				LOG.debug("equals(obj): // found well known Type");
			}
			TypeImpl other = (TypeImpl) obj;
			if (this.getBusinessKey().equals(other.getBusinessKey())) return true;
		}
		else
		{ // found external Type
			if (LOG.isDebugEnabled())
			{
				LOG.debug("equals(obj): // found external Type");
			}
			Type other = (Type) obj;
			if (this.getAuthority().equals(other.getAuthority()) && this.getDomain().equals(other.getDomain())
					&& this.getKeyword().equals(other.getKeyword())) return true;
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		LOG.trace("hashCode()");

		return getBusinessKey().hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		LOG.trace("toString()");

		StringBuilder sb = new StringBuilder();
		sb.append("{id=");
		sb.append(super.id);
		sb.append(", displayName=");
		sb.append(displayName);
		sb.append(", authority=");
		sb.append(authority);
		sb.append(", domain=");
		sb.append(domain);
		sb.append(", keyword=");
		sb.append(keyword);
		sb.append("}");
		return sb.toString();
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getAuthority()
	 */
	public String getAuthority()
	{
		LOG.trace("getAuthority()");

		return authority;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getDomain()
	 */
	public String getDomain()
	{
		LOG.trace("getDomain()");

		return domain;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getKeyword()
	 */
	public String getKeyword()
	{
		LOG.trace("getKeyword()");

		return keyword;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getDisplayName()
	 */
	public String getDisplayName()
	{
		LOG.trace("getDisplayName()");

		return displayName;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getDescription()
	 */
	public String getDescription()
	{
		LOG.trace("getDescription()");

		return description;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setAuthority(java.lang.String)
	 */
	public void setAuthority(String authority)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setAuthority(String " + authority + ")");
		}
		if (authority == null || authority.length() < 1) throw new IllegalArgumentException("authority");

		this.authority = authority;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setDomain(java.lang.String)
	 */
	public void setDomain(String domain)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setDomain(String " + domain + ")");
		}
		if (domain == null || domain.length() < 1) throw new IllegalArgumentException("domain");

		this.domain = domain;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setKeyword(java.lang.String)
	 */
	public void setKeyword(String keyword)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setKeyword(String " + keyword + ")");
		}
		if (keyword == null || keyword.length() < 1) throw new IllegalArgumentException("keyword");

		this.keyword = keyword;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setDisplayName(java.lang.String)
	 */
	public void setDisplayName(String displayName)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setDisplayName(String " + displayName + ")");
		}
		if (displayName == null || displayName.length() < 1) throw new IllegalArgumentException("displayName");

		this.displayName = displayName;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setDescription(java.lang.String)
	 */
	public void setDescription(String description)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setDescription(String " + description + ")");
		}

		this.description = description;
	}

}
