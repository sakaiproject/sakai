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

package org.sakaiproject.component.common.manager;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.common.manager.Persistable;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 */
@Slf4j
public abstract class PersistableImpl implements Persistable
{
	protected Long id;

	protected Integer version;

	protected String uuid;

	protected String lastModifiedBy;

	protected Date lastModifiedDate;

	protected String createdBy;

	protected Date createdDate;

	/**
	 * @return Returns the id surrogateKey.
	 */
	public Long getId()
	{
		log.trace("getId()");

		return id;
	}

	/**
	 * @param id
	 *        The id surrogateKey to set.
	 */
	public void setId(Long id)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setId(Long " + id + ")");
		}
		if (id == null) throw new IllegalArgumentException("Illegal id argument passed!");

		this.id = id;
	}

	/**
	 * @return Returns the version (optimistic lock).
	 */
	public Integer getVersion()
	{
		log.trace("getVersion()");

		return version;
	}

	/**
	 * @param version
	 *        The version (optimistic lock) to set.
	 */
	public void setVersion(Integer version)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setVersion(Integer " + version + ")");
		}; // validation removed to enable hibernate's reflection optimizer

		this.version = version;
	}

	/**
	 * @see org.sakaiproject.api.common.manager.Persistable#getUuid()
	 */
	public String getUuid()
	{
		log.trace("getUuid()");

		return uuid;
	}

	public void setUuid(String uuid)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setUuid(String " + uuid + ")");
		}
		if (uuid == null || uuid.length() < 1) throw new IllegalArgumentException("Illegal uuid argument passed!");

		this.uuid = uuid;
	}

	/**
	 * @see org.sakaiproject.api.common.manager.Persistable#getLastModifiedBy()
	 */
	public String getLastModifiedBy()
	{
		log.trace("getLastModifiedBy()");

		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setLastModifiedBy(String " + lastModifiedBy + ")");
		}
		// FIXME
		// if (lastModifiedBy == null || lastModifiedBy.length() < 1)
		// throw new IllegalArgumentException(
		// "Illegal lastModifiedBy argument passed!");

		this.lastModifiedBy = lastModifiedBy;
	}

	/**
	 * @see org.sakaiproject.api.common.manager.Persistable#getLastModifiedDate()
	 */
	public Date getLastModifiedDate()
	{
		log.trace("getLastModifiedDate()");

		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setLastModifiedDate(Date " + lastModifiedDate + ")");
		}
		// FIXME
		// if (lastModifiedDate == null)
		// throw new IllegalArgumentException(
		// "Illegal lastModifiedDate argument passed!");

		this.lastModifiedDate = lastModifiedDate;
	}

	/**
	 * @see org.sakaiproject.api.common.manager.Persistable#getCreatedBy()
	 */
	public String getCreatedBy()
	{
		log.trace("getCreatedBy()");

		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setCreatedBy(String " + createdBy + ")");
		}
		// FIXME
		// if (createdBy == null || createdBy.length() < 1)
		// throw new IllegalArgumentException("Illegal createdBy argument passed!");

		this.createdBy = createdBy;
	}

	/**
	 * @see org.sakaiproject.api.common.manager.Persistable#getCreatedDate()
	 */
	public Date getCreatedDate()
	{
		log.trace("getCreatedDate()");

		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setCreatedDate(Date " + createdDate + ")");
		}
		// FIXME
		// if (createdDate == null)
		// throw new IllegalArgumentException("Illegal createdDate argument passed!");

		this.createdDate = createdDate;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		log.trace("toString()");

		StringBuilder sb = new StringBuilder();
		sb.append("{id=");
		sb.append(id);
		sb.append(", lastModifiedBy=");
		sb.append(lastModifiedBy);
		sb.append(", lastModifiedDate=");
		sb.append(lastModifiedDate);
		sb.append(", createdBy=");
		sb.append(createdBy);
		sb.append(", createdDate=");
		sb.append(createdDate);
		sb.append(", uuid=");
		sb.append(uuid);
		sb.append(", version=");
		sb.append(version);
		return sb.toString();
	}
}
