/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.unboundid;

import java.util.Map;
import java.util.Set;

import org.sakaiproject.user.api.UserEdit;

import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;

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
	 * Output a filter string for searching the directory with
	 * the specified user aid as a key.
	 * @param aid a user authentication id.
	 * @return an LDAP search filter
	 */
	public String getFindUserByAidFilter(String aid);
	
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

	/**
	 * Scrubs the given search filter term (i.e. a value to be matched, or not,
	 * in a search predicate) for reserved characters. I.e. protects against 
	 * query injection.
	 * 
	 * @param term The string value to be scrubbed
	 * @return <code>null</code> if the received String is null, otherwise
	 *   a copy of the received String with reserved characters escaped.
	 */
	public String escapeSearchFilterTerm(String term);

	/**
	 * Determine the DN to which to bind when executing an authentication
	 * attempt for the given user. An invocation implies that the DN can
	 * be derived from attributes already mapped onto the given {@link LdapUserData}
	 * by this <code>LdapAttributeMapper</code>. For example, the mapper
	 * could have cached the DN in the user's property map, or the bind DN could
	 * be reliably calculated from a combination of the user's <code>eid</code>
	 * and some other configured RDN string.
	 * 
	 * @param userData a mapped collection of user attributes from which
	 *   to derive a bindable DN. Should not be <code>null</code>
	 * @return a bindable DN derived from <code>userData</code> or 
	 *   <code>null</null> if the DN is not known.
	 */
	public String getUserBindDn(LdapUserData userData);
	
	/**
	 * Builds a filter to perform a wildcard search for criteria in uid, email, first name or last name
	 * <p>In order to minimise hitting the limits of searches, this only performs a wildcard match on anything <i>after</i>
	 * the supplied criteria.<br />
	 * For example, a search for 'john' will match 'john' and 'johnson' but not 'gudjohnsen'.
	 * <p>
	 * For reference, the LDAP search filter is of the form:
	 * "(|(uid=criteria*)(mail=criteria*)(givenName=criteria*)(sn=criteria*))"
	 * 
	 * @param	the search string
	 * @return	the formatted search filter
	 */
	public String getFindUserByCrossAttributeSearchFilter(String criteria);
	
	/**
	 * Builds a filter to a uid search against many users at once 
	 * For reference, the LDAP search filter is of the form:
	 * "(|(uid=sample.user)(uid=john.doe)(uid=jane.smith))"
	 * 
	 * @param	the search string
	 * @return	the formatted search filter
	 */
	public String getManyUsersInOneSearch(Set<String> criteria);

}
