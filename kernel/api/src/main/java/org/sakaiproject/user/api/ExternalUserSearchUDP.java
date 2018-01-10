/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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
     * 		If no paging is requested <code>-1</code> will be passed.
     * @param last 
     * 		The last record position to return. If the provider does not support paging, this value is unused.
     * 		If no paging is requested <code>-1</code> will be passed.
     * @param factory
     * 		Use this factory's newUser() method to create the UserEdit objects you populate and return in the List.
     * @return 
     * 		A List (UserEdit) of all the users matching the criteria or null if an error occurred.
     */ 
	public List<UserEdit> searchExternalUsers(String criteria, int first, int last, UserFactory factory);
	
}
