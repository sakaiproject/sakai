/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import java.util.Map;

import org.sakaiproject.user.api.UserEdit;

import com.novell.ldap.LDAPEntry;

/**
 * Implementations handle mappings between logical and physical
 * directory attribute names and between directory entries and
 * Sakai domain and framework objects. The also handle
 * search filter writing. 
 * 
 * @author Dan McCallum, Unicon Inc
 *
 */
public interface LdapAttributeMapper {
	
	/**
	 * Complete internal configuration. Typical called by Spring.
	 */
	public void init();

	/**
	 * Output a filter string for searching the directory with
	 * the specified email address as a key.
	 * 
	 * @param emailAddr an email address to search on
	 * @return an LDAP search filter
	 */
	public String getFindUserByEmailFilter(String emailAddr);
	
	/**
	 * Output a filter string for searching the directory with
	 * the specified user eid as a key.
	 * 
	 * @param eid a user eid to search on
	 * @return an LDAP search filter
	 */
	public String getFindUserByEidFilter(String eid);
	
	/**
	 * Maps attribites from the specified <code>LDAPEntry</code> onto
	 * a {@link LdapUserData}.
	 * 
	 * @param ldapEntry a non-null directory entry to map
	 * @param userData a non-null user cache entry
	 */
	public void mapLdapEntryOntoUserData(LDAPEntry ldapEntry, LdapUserData userData);
	
	/**
	 * Maps attribites from the specified {@link LdapUserData} onto
	 * a {@link org.sakaiproject.user.api.UserEdit}.
	 * 
	 * @param userData a non-null user cache entry
	 * @param userEdit a non-null user domain object
	 */
	public void mapUserDataOntoUserEdit(LdapUserData userData, UserEdit userEdit);
	
	/**
	 * Access the standard attributes returned from any given
	 * directory search.
	 * 
	 * @return an array of directory attribute names
	 */
	public String[] getSearchResultAttributes();
	
	/**
	 * Access the current directory attribute map. Keys
	 * are logical names, values are physical names.
	 * 
	 * @return the current attribute map.
	 */
	public Map<String,String> getAttributeMappings();

	/**
	 * Assign the directory attribute map. Keys are logical
	 * names, values are physical names.
	 * 
	 * @param attributeMappings the attribute map.
	 */
	public void setAttributeMappings(Map<String,String> attributeMappings);

    /**
     * Map the given logical attribute name to a physical attribute name.
     * 
     * @param key the logical attribute name
     * @return the corresponding physical attribute name, or null
     *   if no mapping exists.
     */
    public String getAttributeMapping(String key);
    
    public String escapeSearchFilterTerm(String term);
    
    public String getUserBindDn(LdapUserData userData);
	
}
