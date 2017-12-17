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
package edu.amc.sakai.user;

import com.novell.ldap.LDAPEntry;
import lombok.extern.slf4j.Slf4j;

/**
 * Very simple {@link UserTypeMapper} which generates the same
 * <code>String</code> value for any users. Technically, this
 * object could be reconfigured at any time.
 * 
 * @author dmccallum
 */
@Slf4j
public class StringUserTypeMapper implements UserTypeMapper {

	private String userType;
	
	/**
	 * Leaves the cached user type <code>String</code> initialized to <code>null</code>
	 */
	public StringUserTypeMapper() {}
	
	/**
	 * Sets the cached user type to the given <code>String</code>.
	 * Effectively the same as using the no-arg constructor,
	 * then invoking {@link #setUserType(String)}. No guarantees
	 * exist that this constructor will actually invoke that
	 * method, though.
	 * 
	 * @param userType a user type String. <code>null</code> and
	 *   empty Strings OK
	 */
	public StringUserTypeMapper(String userType) {
		this.userType = userType;
	}

	/**
	 * Always returns the value set by {@link #setUserType(String)}.
	 * Default is <code>null</code>
	 * 
	 * @return the value cached by {@link #setUserType(String)}.
	 */
	public String mapLdapEntryToSakaiUserType(LDAPEntry ldapEntry,
			LdapAttributeMapper mapper) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("mapLdapEntryToSakaiUserType(): returning user type [" + userType + 
					"] for [entry DN = " + ldapEntry.getDN() + "]");
		}
		
		return userType;
	}

	/**
	 * Access the user type <code>String</code> to be returned from
	 * any invocation of {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}.
	 *
	 * @return the cached user type <code>String</code>. Might be
	 *   <code>null</code> or empty.
	 */
	public String getUserType() {
		return userType;
	}

	/**
	 * Set the user type <code>String</code> to be returned from
	 * any invocation of {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}.
	 * 
	 * @param userType a user type String. <code>null</code> and
	 *   empty Strings OK
	 */
	public void setUserType(String userType) {
		this.userType = userType;
	}

}
