/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.common.type;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.component.common.manager.PersistableImpl;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
@Slf4j
public class TypeImpl extends PersistableImpl implements Type
{
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
		log.trace("getBusinessKey()");

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
		if (log.isTraceEnabled())
		{
			log.trace("equals(Object " + obj + ")");
		}

		if (this == obj) return true;
		if (!(obj instanceof Type)) return false;
		if (obj instanceof TypeImpl)
		{ // found well known Type
			if (log.isDebugEnabled())
			{
				log.debug("equals(obj): // found well known Type");
			}
			TypeImpl other = (TypeImpl) obj;
			if (this.getBusinessKey().equals(other.getBusinessKey())) return true;
		}
		else
		{ // found external Type
			if (log.isDebugEnabled())
			{
				log.debug("equals(obj): // found external Type");
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
		log.trace("hashCode()");

		return getBusinessKey().hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		log.trace("toString()");

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
		log.trace("getAuthority()");

		return authority;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getDomain()
	 */
	public String getDomain()
	{
		log.trace("getDomain()");

		return domain;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getKeyword()
	 */
	public String getKeyword()
	{
		log.trace("getKeyword()");

		return keyword;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getDisplayName()
	 */
	public String getDisplayName()
	{
		log.trace("getDisplayName()");

		return displayName;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#getDescription()
	 */
	public String getDescription()
	{
		log.trace("getDescription()");

		return description;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setAuthority(java.lang.String)
	 */
	public void setAuthority(String authority)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setAuthority(String " + authority + ")");
		}
		if (authority == null || authority.length() < 1) throw new IllegalArgumentException("authority");

		this.authority = authority;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setDomain(java.lang.String)
	 */
	public void setDomain(String domain)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setDomain(String " + domain + ")");
		}
		if (domain == null || domain.length() < 1) throw new IllegalArgumentException("domain");

		this.domain = domain;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setKeyword(java.lang.String)
	 */
	public void setKeyword(String keyword)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setKeyword(String " + keyword + ")");
		}
		if (keyword == null || keyword.length() < 1) throw new IllegalArgumentException("keyword");

		this.keyword = keyword;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setDisplayName(java.lang.String)
	 */
	public void setDisplayName(String displayName)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setDisplayName(String " + displayName + ")");
		}
		if (displayName == null || displayName.length() < 1) throw new IllegalArgumentException("displayName");

		this.displayName = displayName;
	}

	/**
	 * @see org.sakaiproject.service.type.Type#setDescription(java.lang.String)
	 */
	public void setDescription(String description)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setDescription(String " + description + ")");
		}

		this.description = description;
	}

}
