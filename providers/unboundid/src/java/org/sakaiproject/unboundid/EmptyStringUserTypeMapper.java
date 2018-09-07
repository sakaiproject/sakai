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

import lombok.extern.slf4j.Slf4j;

/**
 * Essentially a null impl, which always returns an empty
 * String from {@link #mapLdapEntryToSakaiUserType(LDAPEntry, LdapAttributeMapper)}.
 * 
 * @author Dan McCallum, Unicon

 */
@Slf4j
public class EmptyStringUserTypeMapper extends StringUserTypeMapper {
	
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
		if ( log.isWarnEnabled() ) {
			log.warn("Ignoring setUserType() call. EmptyStringUserTypeMapper cannot be reconfigured. Proposed user type value [" + userType + "]");
		}
	}

}
