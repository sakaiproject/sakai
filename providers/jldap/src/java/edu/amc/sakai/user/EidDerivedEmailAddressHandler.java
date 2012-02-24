/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

/**
 * Mixin interface for objects capable of calculating a user EID
 * from an email address. Commonly added to {@link LdapAttributeMapper}s
 * deployed against LDAP hosts which do not actually define email attributes
 * on user entries. 
 * 
 * @see EmailAddressDerivingLdapAttributeMapper
 * @author dmccallum
 *
 */
public interface EidDerivedEmailAddressHandler {

	/**
	 * Extract a user EID from the given email address.
	 * 
	 * @param email and email address. Not necessarily guaranteed to be non-null or
	 *   even contain valid email syntax
	 * @return an EID derived from the <code>email</code> argument
	 * @throws InvalidEmailAddressException if <code>email</code> cannot be processed
	 *   for any reason. Implementation should raise this exception in any situation
	 *   where it might otherwise return <code>null</code>
	 */
	String unpackEidFromAddress(String email) throws InvalidEmailAddressException;

}
