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
}