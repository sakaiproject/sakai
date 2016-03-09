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

import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;

/**
 * Represents a pluggable strategy for calculating a Sakai user
 * type given a user's directory entry
 * 
 * @author Dan McCallum, Unicon Inc
 */
public interface UserTypeMapper {
	
	/**
	 * Calculate a Sakai user type given a <code>LDAPEntry</code>
	 * and a {@link LdapAttributeMapper}.
	 * 
	 * @param ldapEntry a user's <code>LDAPEntry</code>, should not 
	 *   be <code>null</code>
	 * @param mapper encapsulates mapping configuration
	 * @return a Sakai user type symbolic String, <code>null</code>s 
	 *   and empty Strings typically represent an unsuccessful mapping
	 */
	public String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry, LdapAttributeMapper mapper);

}
