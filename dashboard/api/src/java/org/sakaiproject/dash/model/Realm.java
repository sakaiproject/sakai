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


/**
 * Realm encapsulates information about sakai authz realms, which define who 
 * has access to particular entities in sakai and what permissions they have 
 * with respect to entities in a realm. We need a way to link a dashboard item 
 * to a realm to improve the efficiency of updates.
 *
 */
public class Realm {

	protected Long id;
	protected String realmId;

	/**
	 * 
	 */
	public Realm() {
		super();
	}

	public Realm(String realmId) {
		super();
		this.realmId = realmId;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
}

	/**
	 * @return the realmId
	 */
	public String getRealmId() {
		return realmId;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @param realmId the realmId to set
	 */
	public void setRealmId(String realmId) {
		this.realmId = realmId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Realm [id=");
		builder.append(id);
		builder.append(", realmId=");
		builder.append(realmId);
		builder.append("]");
		return builder.toString();
	}

}
