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
public class EmptyStringUserTypeMapper extends StringUserTypeMapper {
	
	/** Class-specific logger */
	private static Log M_log = LogFactory.getLog(EmptyStringUserTypeMapper.class);
	
	/**
	 * Initializes the cached user type <code>String</code> to an
	 * empty <code>String</code>
	 */
	public EmptyStringUserTypeMapper() {
		super("");
	}
	
	/**
	 * Overridden to log a warn message but otherwise do nothing.
	 * This class is intended to always return an empty
	 * <code>String</code> from any invocation of
	 * {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}.
	 * As such, reconfiguration is inappropriate.
	 * 
	 * @param userType ignored
	 */
	@Override
	public void setUserType(String userType) {
		if ( M_log.isWarnEnabled() ) {
			M_log.warn("Ignoring setUserType() call. EmptyStringUserTypeMapper cannot be reconfigured. Proposed user type value [" + userType + "]");
		}
	}

}
