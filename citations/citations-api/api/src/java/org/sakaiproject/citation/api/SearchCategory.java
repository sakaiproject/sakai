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

public interface SearchCategory
{
	
	/**
	 * Returns the display name of this category
	 * 
	 * @return display name of this category
	 */
	public String getDisplayName();
	
	/**
	 * Returns the description of this category
	 * 
	 * @return description of this category
	 */
	public String getDescription();

	/**
	 * Returns the id of this category
	 * 
	 * @return id of this category
	 */
	public String getId();

	/**
	 * Indicates whether this category has any databases within it
	 * 
	 * @return true if this category contains at least one database,
	 * false otherwise
	 */
	public boolean hasDatabases();
	
	/**
	 * Returns the databases contained in this category
	 * 
	 * @return a java.util.List containing SearchDatabase objects within this SearchCategory,
	 * null if this category does not contain any databases (if hasDatabases() returns false)
	 */
	public java.util.List<SearchDatabase> getDatabases();
	
	/**
	 * Determines whether the given database is recommended within this category
	 * 
	 * @param databaseId id of the database to check
	 * @return true if the database is recommended, false otherwise (also returns
	 * false if this database does not exist in this category)
	 */
	public boolean isDatabaseRecommended( String databaseId );
	
	/**
	 * Indicates whether this category has any sub-categories within it
	 * 
	 * @return true if this category contains at least one category,
	 * false otherwise
	 */
	public boolean hasSubCategories();

	/**
	 * Returns the categories contained within this category
	 * 
	 * @return a list containing SearchCategory objects within this SearchCategory,
	 * null if this category does not contain any categories (if hasSubCategories() returns false)
	 */
	public java.util.List<SearchCategory> getSubCategories();
}
