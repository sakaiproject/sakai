/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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


package org.sakaiproject.lessonbuildertool;

public class SimplePageGroupImpl implements SimplePageGroup {
	private long id;
	private String itemId;  // this is actually the sakaiID
	private String groupId;
	private String groups;
        private String siteId;

	public SimplePageGroupImpl() {}

	public SimplePageGroupImpl(String itemId, String groupId, String groups, String siteId) {
		this.itemId = itemId;
		this.groupId = groupId;
		this.groups = groups;
		this.siteId = siteId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroups(String groups) {
		this.groups = groups;
	}

	public String getGroups() {
		return groups;
	}

	public void setSiteId(String s) {
		this.siteId = s;
	}

	public String getSiteId() {
		return siteId;
	}

}
