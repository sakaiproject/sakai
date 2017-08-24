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

import java.util.ArrayList;
import java.util.List;

/**
 * Adds support for finding users by email address where users may
 * have email addresses stored across multiple LDAP attributes.
 * 
 * <p>Thanks to Erik Froese of NYU for the original patch</p>
 * 
 * @author dmccallum@unicon.net
 */
public class MultipleEmailLdapAttributeMapper extends SimpleLdapAttributeMapper {

	/**
	 * A list of logical attribute names to search against when
	 * locating a user entry by email address.
	 */
	private List<String> searchableEmailAttributes = new ArrayList<String>();

	/**
	 * Access the list of logical attribute names searched when
	 * locating a user entry by email address.
	 * 
	 * @return a list of logical LDAP attribute names. Will not
	 *   return <code>null</code>
	 */
	public List<String> getSearchableEmailAttributes() {
		return searchableEmailAttributes;
	}

	@Override
	public String getFindUserByEmailFilter(String emailAddr) {
		
		if ( searchableEmailAttributes.isEmpty() ) {
			return super.getFindUserByEmailFilter(emailAddr);
		}
		
		StringBuilder filter = new StringBuilder();
		for ( String logicalAttrName : searchableEmailAttributes ) {
			
			String physicalAttrName = getAttributeMapping(logicalAttrName);
			if ( physicalAttrName == null ) {
				continue;
			}
			
			if ( filter.length() == 0 ) {
				appendSingleSearchPredicate(filter,physicalAttrName, emailAddr);
			} else {
				if ( filter.charAt(0) != '(') {
					filter.insert(0, "(");
					filter.append(")");
				}
				filter.insert(0, "(|").append("(");
				appendSingleSearchPredicate(filter, physicalAttrName, emailAddr).append("))");
			}
		}
		
		if ( filter.length() == 0 ) {
			return super.getFindUserByEmailFilter(emailAddr);
		}
		
		return filter.toString();
		
	}
	
	protected StringBuilder appendSingleSearchPredicate(StringBuilder into, 
			String physicalAttrName, String rawSearchTerm) {
		return into.append(physicalAttrName).
			append("=").
			append(escapeSearchFilterTerm(rawSearchTerm));
	}
	
	/**
	 * Assign the list of logical attribute names to search when
	 * locating a user entry by email address. If <code>null</code>,
	 * will have the effect of clearing the current attribute list.
	 * Note that this configuration option has no bearing on the
	 * email address which will be mapped to <code>UserEdit.email</code>
	 * 
	 * @param searchableEmailAttributes a list of logical attribute
	 *   names. <code>null</code> treated like an empty list.
	 */
	public void setSearchableEmailAttributes(List<String> searchableEmailAttributes) {
		if ( searchableEmailAttributes == null ) {
			this.searchableEmailAttributes.clear();
		} else {
			this.searchableEmailAttributes = searchableEmailAttributes;
		}
	}
	
}
