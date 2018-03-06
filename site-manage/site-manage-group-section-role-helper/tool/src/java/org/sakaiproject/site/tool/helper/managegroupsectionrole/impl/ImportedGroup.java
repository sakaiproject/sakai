/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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
package org.sakaiproject.site.tool.helper.managegroupsectionrole.impl;

import java.util.SortedSet;
import java.util.TreeSet;

import lombok.Data;

/**
  + * Model for a group that is imported. Has a title and list of members.
  + * 
  + */
@Data
public class ImportedGroup {
    	
	private String groupTitle;
	private SortedSet<String> userIds;
		
	// special constructor to take the title and a single user, then add that user to the internal set
	public ImportedGroup(String groupTitle, String userId){
		this.groupTitle = groupTitle;
				
		userIds = new TreeSet<String>();
				
		addUser(userId);
	}
		
		
	//helper method to update the internal list
	public void addUser(String userId) {
		userIds.add(userId);
	}
	
	public String getGroupTitle() { return groupTitle;}

	public SortedSet<String> getUserIds() { return userIds;}
}