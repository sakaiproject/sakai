/**
 * Copyright (c) 2003-2007 The Apereo Foundation
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
package org.sakaiproject.citation.api;

public interface SearchDatabase
{
	/**
	 * Returns the display name for this database
	 * 
	 * @return display name for this database
	 */
	public String getDisplayName();
	
	/**
	 * Returns the description for this database
	 * 
	 * @return description for this database
	 */
	public String getDescription();
	
	/**
	 * Returns the id for this database
	 * 
	 * @return id for this database
	 */
	public String getId();
	
	/**
	 * Determines whether or not this database belongs to the given group
	 * 
	 * @param groupId group identifier (name, id, etc...) to check
	 * @return true if this database is a member of the group, false otherwise
	 */
	public boolean isGroupMember( String groupId );
}
