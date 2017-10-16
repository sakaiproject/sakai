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

public interface SearchDatabaseHierarchy
{
	public static final String ROOT_CATEGORY_NAME = "root";
	public static final String ROOT_CATEGORY_ID = "!root";
	
	/**
	 * Returns an Asset representing the database with the given databaseId.
	 * This method is in place to provide individual database browse funcationality.
	 * 
	 * @param databaseId String representing the id of the database requested
	 * @return an Asset if the database exists in this hierarchy, null otherwise.
	 *
	public org.osid.repository.Asset getDatabase( String databaseId );
	 */
	
	/**
	 * Returns the category within this hierarchy with the given category id
	 * 
	 * @param categoryId id of category to check
	 * @return SearchCategory within this hierarchy with given category id; null
	 * if the category id is not found in this hierarchy
	 */
	public SearchCategory getCategory( String categoryId );
	
	/**
	 * Returns the number of hierarchical levels in this hierarchy.
	 * 
	 * @return number of hierarchical levels in this hierarchy.  One less than the number returned
	 * provides the number of categorization levels - examples:
	 * <ul>
	 * <li>return value of 1 indicates there are just databases (no categorization)</li>
	 * <li>return value of 5 indicates there are 4 categorization levels</li>
	 * </ul>
	 */
	public int getNumLevels();
	
	/**
	 * Returns the maximum number of searchable databases within this hierarchy.
	 * This number defaults to 8 for now.
	 * 
	 * @return max number of searchable databases within this hierarchy
	 */
	public int getNumMaxSearchableDb();
	
	/**
	 * Returns a list containing all categories in this hierarchy.
	 * This list can be used to iterate through the entire hierarchy.
	 * 
	 * @return list containing the SearchCategory objects in this hierarchy.
	 */
	public java.util.List getCategoryListing();
	
	/**
	 * Returns the default category in this hierarchy.  A default category will contain
	 * at least one database and no sub-categories.
	 * 
	 * @return Default category in this hierarchy, or null if it does not exist.
	 */
	public SearchCategory getDefaultCategory();
	
	/**
	 * Determines whether or not the given database is within this hierarchy.
	 * 
	 * @param databaseId database id to check
	 * @return true if the database exists in this hierarchy,
	 * false if it does not
	 */
	public boolean isSearchableDatabase( String databaseId );
	
	/**
	 * Get the Repository associated with this hierarchy
	 * 
	 * @return Repository associated with this hierarchy
	 */
	public org.osid.repository.Repository getRepository();
	
	/**
	 * Determines whether or not this hierarchy is properly configured.
	 * This hierarchy could not be properly configured if the config xml files
	 * are not found or there is an error in parsing them.
	 * 
	 * @return true if hierarchy is properly configured, false otherwise.
	 */
	public boolean isConfigured();
}
