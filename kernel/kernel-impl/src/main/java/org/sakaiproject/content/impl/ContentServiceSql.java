/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

/**
 * database methods.
 */
public interface ContentServiceSql
{
	/**
	 * returns the sql statement which retrieves the body from the specified table (content_resource_body_binary).
	 */
	String getBodySql(String table);

	/**
	 * returns the sql statement which retrieves the collection id from the specified table.
	 */
	String getCollectionIdSql(String table);

	/**
	 * returns the sql statement which deletes content from the specified table (content_resource_body_binary).
	 */
	String getDeleteContentSql(String table);

	/**
	 * returns the sql statement which inserts content into the specified table (content_resource_body_binary).
	 */
	String getInsertContentSql(String table);

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_resource table.
	 */
	String getNumContentResources1Sql();

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_collection table.
	 */
	String getNumContentResources2Sql();

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_resource table.
	 */
	String getNumContentResources3Sql();

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_collection table.
	 */
	String getNumContentResources4Sql();

	/**
	 * returns the sql statement which retrieves resource id from the content_resource table.
	 */
	String getResourceId1Sql();

	/**
	 * returns the sql statement which retrieves the resource id from the content_resource_body_binary table.
	 */
	String getResourceId2Sql();

	/**
	 * returns the sql statement which retrieves the resource id from the specified table.
	 */
	String getResourceId3Sql(String table);

	/**
	 * returns the sql statement which retrieves the resource id and xml fields from the content_resource table.
	 */
	String getResourceIdXmlSql();

	/**
	 * returns the sql statement which retrieves all id's and file paths where the file path is not null.
	 * This is used for converting storage from one FileSystemHandler to another.
	 */
	public String getResourceIdAndFilePath();

	/**
	 * returns the sql statement which retrieves resource uuid from the content_resource table.
	 */
	String getResourceUuidSql();

	/**
	 * returns the sql statement which updates the resource uuid in the content_resource table for a given resource uuid.
	 */
	String getUpdateContentResource1Sql();

	/**
	 * returns the sql statement which updates the resource uuid in the content_resource table for a given resource id.
	 */
	String getUpdateContentResource2Sql();

	/**
	 * returns the sql statement which updates the file path and xml fields in the content_resource table for a given resource id.
	 */
	String getUpdateContentResource3Sql();

	/**
	 * returns the sql statement which retrieves pairs of individual-dropbox-id and last-update fields from the content_dropbox_changes table for a given site-level dropbox-id.
	 */
	String getSiteDropboxChangeSql();

	/**
	 * returns the sql statement which retrieves the last-update field from the content_dropbox_changes table for a given individual-dropbox-id.
	 */
	String getIndividualDropboxChangeSql();

	/**
	 * returns the sql statement which updates the last-update field in the content_dropbox_changes table for a given site-level dropbox-id and individual-dropbox-id.
	 */
	String getUpdateIndividualDropboxChangeSql();

	/**
	 * returns the sql statement which inserts the individual-dropbox-id, site-level dropbox-id and last-update fields into the content_dropbox_changes table.
	 */
	String getInsertIndividualDropboxChangeSql();

	/**
	 * returns the sql statement to add the FILE_SIZE column to the specified table.
	 */
	String getAddFilesizeColumnSql(String table);

	/**
	 * returns the sql statement to add the CONTEXT column to the specified table.
	 */
	String getAddContextColumnSql(String table);

	/**
	 * returns the sql statement to add an index of the CONTEXT column to the specified table.
	 */
	String getAddContextIndexSql(String table);
	
	/**
	 * returns the sql statement to add the RESOURCE_TYPE_ID column to the specified table.
	 */
	String getAddResourceTypeColumnSql(String table);

	/**
	 * returns the sql statement to add an index of the RESOURCE_TYPE_ID column to the specified table.
	 */
	String getAddResourceTypeIndexSql(String table);

	/**
	 * returns the sql statement which retrieves the total number of bytes within a site-level collection (context) in the CONTENT_RESOURCE table.
	 */
	String getQuotaQuerySql();
	String getDropBoxQuotaQuerySql();
	/**
	 * returns the sql statement which retrieves the total number of bytes within a site-level collection skiping user folders.
	 * KNL-1084, SAK-22169
	 */
	String getDropBoxRootQuotaQuerySql();
	/**
	 * returns the sql statement which retrieves the RESOURCE_ID and XML values for all entries in the specified table where file-size is null.
	 */
	String getAccessResourceIdAndXmlSql(String table);

	/**
	 * returns the sql statement which updates a row in the specified table with values for CONTEXT, FILE_SIZE and (possibly) RESOURCE_UUID.
	 */
	String getContextFilesizeValuesSql(String table, boolean addingUuid);

	String getFilesizeColumnExistsSql();

	/**
	 * returns the sql statement to check if any rows exist with NULL FILE_SIZE values
	 */
	String getFilesizeExistsSql();

	/**
	 * A statement to create a UTF test table based on the name
	 * 
	 * @param tempTableName
	 * @return
	 */
	String getCreateTemporaryUTF8TestTable(String tempTableName);

	/**
	 * SQL to drop the table
	 * 
	 * @param tempTableName
	 * @return
	 */
	String getDropTemporaryUTF8TestTable(String tempTableName);

	/**
	 * returns the sql statement which retrieves the BINARY_ENTITY and XML values for all entries in the CONTENT_RESOURCE table, 
	 * selecting by the RESOURCE_TYPE_ID with first and last record indexes, and returned in ascending order by RESOURCE_ID.
	 */
	String getSelectByResourceTypeQuerySql();

}
