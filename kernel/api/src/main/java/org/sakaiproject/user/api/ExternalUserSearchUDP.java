package org.sakaiproject.user.api;

import java.util.List;

/**
 * <p>
 * ExternalUserSearchUDP is an optional interface for a UserDirectoryProvider to indicate that they support searching their directory
 * </p>
 */
public interface ExternalUserSearchUDP {

	/** 
     * Search all the externally provided users that match this criteria in eid, 
     * email, first or last name. 
     * 
     * @param criteria 
     * The search criteria. 
     * @param first 
     * The first record position to return. 
     * @param last 
     * The last record position to return. 
     * @return A list (User) of all the aliases matching the criteria, within the 
     * record range given (sorted by sort name). 
     */ 
	public List<User> searchUsers(String criteria, int first, int last);
	
}
