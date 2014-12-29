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


/**
 * SourceType encapsulates information about the types of entities to be
 * represented as dashboard items, along with information about how and where 
 * notifications about them will be rendered .
 *
 */
public class SourceType implements Serializable {

	protected Long id;
	protected String identifier;

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
	 * 
	 * @param other
	 */
	public SourceType(SourceType other) {
		super();
		if(other.id != null) {
			this.id = new Long(other.id.longValue());
		}
		this.identifier = other.identifier;
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
		builder.append("]");
		return builder.toString();
	}
	
}
