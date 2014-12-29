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
 * Person encapsulates information about users needed to link sakai users with 
 * dashboard items of interest to them.
 *
 */
public class Person implements Serializable {

	protected Long id = null;
	protected String sakaiId;
	protected String userId;

	/**
	 * 
	 */
	public Person() {
		super();
}

	/**
	 * @param sakaiId
	 * @param userId
	 */
	public Person(String sakaiId, String userId) {
		super();
		this.sakaiId = sakaiId;
		this.userId = userId;
	}

	public Person(Person other) {
		super();
		this.id = other.getId();
		this.sakaiId = other.getSakaiId();
		this.userId = other.getUserId();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the sakaiId
	 */
	public String getSakaiId() {
		return sakaiId;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param sakaiId the sakaiId to set
	 */
	public void setSakaiId(String sakaiId) {
		this.sakaiId = sakaiId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Person [id=");
		builder.append(id);
		builder.append(", sakaiId=");
		builder.append(sakaiId);
		builder.append(", userId=");
		builder.append(userId);
		builder.append("]");
		return builder.toString();
	}
	
}
