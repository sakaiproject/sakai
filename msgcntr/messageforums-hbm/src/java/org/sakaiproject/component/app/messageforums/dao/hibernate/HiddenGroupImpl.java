/**
 * Copyright (c) 2005-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.HiddenGroup;
import org.sakaiproject.api.app.messageforums.PrivateForum;

public class HiddenGroupImpl implements HiddenGroup {

	private Long id;
	private String groupId;
	protected Integer version;
	private Area area;
	
	public HiddenGroupImpl(){
		
	}
	
	public HiddenGroupImpl(String groupId){
		this.groupId = groupId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public Integer getVersion(){
		return version;
	}
	
	public void setVersion(Integer version)
	{
		this.version = version;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

}
