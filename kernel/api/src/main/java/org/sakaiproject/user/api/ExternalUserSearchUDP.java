package org.sakaiproject.user.api;

import java.util.List;

/**
 * <p>
 * ExternalUserSearchUDP is an optional interface for a UserDirectoryProvider to indicate that they support searching their directory
 * </p>
 */
public interface ExternalUserSearchUDP {

	/** 
     * Search for externally provided users that match this criteria in eid, email, first or last name. 
     * 
     * <p>Returns a List of User objects. This list will be <b>empty</b> if no results are returned or <b>null</b>
     * if your external provider does not implement this interface.<br />
     * The list will also be null if the LDAP server returns an error, for example an '(11) Administrative Limit Exceeded' 
     * or '(4) Sizelimit Exceeded', due to a search term being too broad and returning too many results.
     * 
     * @param criteria 
     * 		The search criteria. 
     * @param first 
     * 		The first record position to return. If the provider does not support paging, this value is unused.
     * @param last 
     * 		The last record position to return. If the provider does not support paging, this value is unused.
     * @param factory 
     * 		Use this factory's newUser() method to create the UserEdit objects you populate and return in the List.
     * @return 
     * 		A List (UserEdit) of all the users matching the criteria or null if an error occurred.
     */ 
	public List<UserEdit> searchExternalUsers(String criteria, int first, int last, UserFactory factory);
	
}
