/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/DbSingleStorage.java $
 * $Id: DbSingleStorage.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.javax.Filter;

/**
 * This interface was extracted so that we could have multiple implementations of SingleStorage.
 * Originally the entity was serialise to XML in a DB column, but due to performance issues a 
 * binary serialisation was also developed and this interface is a common API into all of them.
 */
@SuppressWarnings("rawtypes")
public interface DbSingleStorage
{

    void setDatabaseBeans(Map databaseBeans);

	/**
	 * sets which bean containing database dependent code should be used depending on the database vendor.
	 */
	void setSingleStorageSql(String vendor);

	/**
	 * Open and be ready to read / write.
	 */
	void open();

	/**
	 * Close.
	 */
	void close();

	/**
	 * Check if a Resource by this id exists.
	 * 
	 * @param id
	 *        The id.
	 * @return true if a Resource by this id exists, false if not.
	 */
	boolean checkResource(String id);

	/**
	 * Get the Resource with this id, or null if not found.
	 * 
	 * @param id
	 *        The id.
	 * @return The Resource with this id, or null if not found.
	 */
	Entity getResource(String id);

	boolean isEmpty();

	List getAllResources();

	List getAllResources(int first, int last);

	int countAllResources();

	int countSelectedResourcesWhere(String sqlWhere);

	/**
	 * Get all Resources where the given field matches the given value.
	 * 
	 * @param field
	 *        The db field name for the selection.
	 * @param value
	 *        The value to select.
	 * @return The list of all Resources that meet the criteria.
	 */
	List getAllResourcesWhere(String field, String value);

	List getAllResourcesWhereLike(String field, String value);

	/**
	 * Get selected Resources, filtered by a test on the id field
	 * 
	 * @param filter
	 *        A filter to select what gets returned.
	 * @return The list of selected Resources.
	 */
	List getSelectedResources(final Filter filter);

	/**
	 * Get selected Resources, using a supplied where clause
	 * 
	 * @param sqlWhere
	 *        The SQL where clause.
	 * @return The list of selected Resources.
	 */
	List getSelectedResourcesWhere(String sqlWhere);

	/**
	 * Add a new Resource with this id.
	 * 
	 * @param id
	 *        The id.
	 * @param others
	 *        Other fields for the newResource call
	 * @return The locked Resource object with this id, or null if the id is in use.
	 */
	Edit putResource(String id, Object[] others);

	/** store the record in content_resource_delete table along with resource_uuid and date */
	Edit putDeleteResource(String id, String uuid, String userId, Object[] others);

	/** update XML attribute on properties and remove locks */
	void commitDeleteResource(Edit edit, String uuid);

	/**
	 * Get a lock on the Resource with this id, or null if a lock cannot be gotten.
	 * 
	 * @param id
	 *        The user id.
	 * @return The locked Resource with this id, or null if this records cannot be locked.
	 */
	Edit editResource(String id);

	/**
	 * Commit the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to commit.
	 */
	void commitResource(Edit edit);

	/**
	 * Cancel the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to cancel.
	 */
	void cancelResource(Edit edit);

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param user
	 *        The Edit to remove.
	 */
	void removeResource(Edit edit);
	
	/**
	 * Get a limited number of Resources a given field matches a given value, returned in ascending order 
	 * by another field.  The limit on the number of rows is specified by values for the first item to be 
	 * retrieved (indexed from 0) and the maxCount.
	 * @param selectBy The name of a field to be used in selecting resources.
	 * @param selectByValue The value to select.
	 * @param orderBy The name of a field to be used in ordering the resources.
	 * @param tableName The table on which the query is to operate
	 * @param first A non-negative integer indicating the first record to return
	 * @param maxCount A positive integer indicating the maximum number of rows to return
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getAllResourcesWhere(String selectBy, String selectByValue, String orderBy, int first, int maxCount);

}