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

import org.sakaiproject.dash.entity.EntityLinkStrategy;


/**
 * SourceType encapsulates information about the types of entities to be
 * represented as dashboard items, along with information about how and where 
 * notifications about them will be rendered .
 *
 */
public class SourceType {

	protected Long id;
	protected String identifier;
	protected String accessPermission;
	protected EntityLinkStrategy entityLinkStrategy;

	/**
	 * 
	 */
	public SourceType() {
		super();
	}

	public SourceType(String identifier) {
		super();
		this.identifier = identifier;
		this.entityLinkStrategy = EntityLinkStrategy.ACCESS_URL;
	}
	
	/**
	 * @param identifier
	 * @param accessPermission
	 * @param entityLinkStrategy TODO
	 */
	public SourceType(String identifier, String accessPermission, EntityLinkStrategy entityLinkStrategy) {
		super();
		this.identifier = identifier;
		this.accessPermission = accessPermission;
		this.entityLinkStrategy = entityLinkStrategy;
	}

	/**
	 * @param id
	 * @param identifier
	 * @param accessPermission
	 * @param entityLinkStrategy TODO
	 */
	public SourceType(Long id, String identifier, String accessPermission, EntityLinkStrategy entityLinkStrategy) {
		super();
		this.id = id;
		this.identifier = identifier;
		this.accessPermission = accessPermission;
		this.entityLinkStrategy = entityLinkStrategy;
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
	 * @return the accessPermission
	 */
	public String getAccessPermission() {
		return accessPermission;
	}

	/**
	 * @return the entityLinkStrategy
	 */
	public EntityLinkStrategy getEntityLinkStrategy() {
		return entityLinkStrategy;
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
	 * @param accessPermission the accessPermission to set
	 */
	public void setAccessPermission(String accessPermission) {
		this.accessPermission = accessPermission;
	}

	/**
	 * @param entityLinkStrategy the entityLinkStrategy to set
	 */
	public void setEntityLinkStrategy(EntityLinkStrategy entityLinkStrategy) {
		this.entityLinkStrategy = entityLinkStrategy;
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
		if (entityLinkStrategy != null) {
			builder.append("entityLinkStrategy=");
			builder.append(entityLinkStrategy);
		}
		builder.append("]");
		return builder.toString();
	}

	
}
