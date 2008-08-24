/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.ldap.LDAPEntry;

/**
 * Essentially a null impl, which always returns an empty
 * String from {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}.
 * 
 * @author Dan McCallum, Unicon

 */
public class EmptyStringUserTypeMapper implements UserTypeMapper {
	
	/** Class-specific logger */
	private static Log M_log = LogFactory.getLog(EmptyStringUserTypeMapper.class);

	/**
	 * Always returns an empty String.
	 * 
	 * @param ldapEntry the user's <code>LDAPEntry</code>
	 * @param mapper a source of mapping configuration
	 * @return an empty String
	 */
	public String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry,
			LdapAttributeMapper mapper) {
		
		if ( M_log.isDebugEnabled() ) {
			M_log.debug("mapLdapEntryToSakaiUserType(): returning empty String [entry DN = " + 
					ldapEntry.getDN() + "]");
		}
		
		return "";
	}

}
