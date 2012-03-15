/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.dash.model;

import java.io.Serializable;
import java.util.Arrays;

import org.sakaiproject.dash.entity.EntityLinkStrategy;


/**
 * SourceType encapsulates information about the types of entities to be
 * represented as dashboard items, along with information about how and where 
 * notifications about them will be rendered .
 *
 */
public class SourceType implements Serializable {

	protected Long id;
	protected String identifier;
	protected String accessPermission;
	protected String[] alwaysAccessPermission;

	/**
	 * 
	 */
	public SourceType() {
		super();
	}

	public SourceType(String identifier) {
		super();
		this.identifier = identifier;
	}
	
	/**
	 * @param identifier
	 * @param accessPermission
	 */
	public SourceType(String identifier, String accessPermission) {
		super();
		this.identifier = identifier;
		this.accessPermission = accessPermission;
	}

	/**
	 * @param id
	 * @param identifier
	 * @param accessPermission
	 */
	public SourceType(Long id, String identifier, String accessPermission) {
		super();
		this.id = id;
		this.identifier = identifier;
		this.accessPermission = accessPermission;
	}

	/**
	 * 
	 * @param id
	 * @param identifier
	 * @param accessPermission
	 * @param alwaysAccessPermission
	 */
	public SourceType(String identifier, String accessPermission,
			String[] alwaysAccessPermission) {
		super();
		this.identifier = identifier;
		this.accessPermission = accessPermission;
		if(alwaysAccessPermission != null) {
			this.alwaysAccessPermission = alwaysAccessPermission.clone();
		}
	}

	/**
	 * 
	 * @param id
	 * @param identifier
	 * @param accessPermission
	 * @param alwaysAccessPermission
	 */
	public SourceType(Long id, String identifier, String accessPermission,
			String[] alwaysAccessPermission) {
		super();
		this.id = id;
		this.identifier = identifier;
		this.accessPermission = accessPermission;
		if(alwaysAccessPermission != null) {
			this.alwaysAccessPermission = alwaysAccessPermission.clone();
		}
	}

	/**
	 * 
	 * @param other
	 */
	public SourceType(SourceType other) {
		super();
		if(other.id != null) {
			this.id = new Long(other.id.longValue());
		}
		this.identifier = other.identifier;
		this.accessPermission = other.accessPermission;
		if(other.alwaysAccessPermission != null && other.alwaysAccessPermission.length > 0) {
			this.alwaysAccessPermission = other.alwaysAccessPermission.clone();
		}
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * The permission needed to view items of this type when they are available.
	 * @return the accessPermission
	 */
	public String getAccessPermission() {
		return accessPermission;
	}

	/**
	 * The permissions with which users can view items of this type when they are not available.
	 * @return the alwaysAccessPermission
	 */
	public String[] getAlwaysAccessPermission() {
		String[] array = null;
		if(alwaysAccessPermission != null) {
			array = alwaysAccessPermission.clone();
		}
		return array;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * The permission needed to view items of this type when they are available.
	 * @param accessPermission the accessPermission to set
	 */
	public void setAccessPermission(String accessPermission) {
		this.accessPermission = accessPermission;
	}

	/**
	 * The permissions with which users can view items of this type when they are not available.
	 * These will be checked in the order given and access will be granted if a user can unlock 
	 * access to the entity with any one of these permissions.  
	 * @param alwaysAccessPermission the alwaysAccessPermission to set
	 */
	public void setAlwaysAccessPermission(String[] alwaysAccessPermission) {
		this.alwaysAccessPermission = alwaysAccessPermission;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SourceType [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (identifier != null) {
			builder.append("identifier=");
			builder.append(identifier);
			builder.append(", ");
		}
		if (accessPermission != null) {
			builder.append("accessPermission=");
			builder.append(accessPermission);
			builder.append(", ");
		}
		if (alwaysAccessPermission != null) {
			builder.append("alwaysAccessPermission=");
			builder.append(Arrays.toString(alwaysAccessPermission));
		}
		builder.append("]");
		return builder.toString();
	}

	
}
