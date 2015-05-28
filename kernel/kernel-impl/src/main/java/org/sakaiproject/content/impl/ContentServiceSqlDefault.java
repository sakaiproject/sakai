/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/ChatServiceSqlDefault.java $
 * $Id: ChatServiceSqlDefault.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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
 * methods for accessing content data in a database.
 */
public class ContentServiceSqlDefault implements ContentServiceSql
{
	/**
	 * returns the sql statement which retrieves the body from the specified table (content_resource_body_binary).
	 */
	public String getBodySql(String table)
	{
		return "select BODY from " + table + " where ( RESOURCE_ID = ? )";
	}

	/**
	 * returns the sql statement which retrieves the collection id from the specified table.
	 */
	public String getCollectionIdSql(String table)
	{
		return "select COLLECTION_ID from " + table + " where IN_COLLECTION = ?";
	}

	/**
	 * returns the sql statement which deletes content from the specified table (content_resource_body_binary).
	 */
	public String getDeleteContentSql(String table)
	{
		return "delete from " + table + " where resource_id = ? ";
	}

	/**
	 * returns the sql statement which inserts content into the specified table (content_resource_body_binary).
	 */
	public String getInsertContentSql(String table)
	{
		return "insert into " + table + " (RESOURCE_ID, BODY)" + " values (? , ? )";
	}

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_resource table.
	 */
	public String getNumContentResources1Sql()
	{
		return "select count(IN_COLLECTION) from CONTENT_RESOURCE where IN_COLLECTION like ?";
	}

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_collection table.
	 */
	public String getNumContentResources2Sql()
	{
		return "select count(IN_COLLECTION) from CONTENT_COLLECTION where IN_COLLECTION like ?";
	}

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_resource table.
	 */
	public String getNumContentResources3Sql()
	{
		return "select count(IN_COLLECTION) from CONTENT_RESOURCE where IN_COLLECTION = ?";
	}

	/**
	 * returns the sql statement which retrieves the number of content resources from the content_collection table.
	 */
	public String getNumContentResources4Sql()
	{
		return "select count(IN_COLLECTION) from CONTENT_COLLECTION where IN_COLLECTION = ?";
	}

	/**
	 * returns the sql statement which retrieves the resource id from the content_resource table.
	 */
	public String getResourceId1Sql()
	{
		return "select RESOURCE_ID from CONTENT_RESOURCE where RESOURCE_UUID=?";
	}

	/**
	 * returns the sql statement which retrieves the resource id from the content_resource_body_binary table.
	 */
	public String getResourceId2Sql()
	{
		return "select RESOURCE_ID from CONTENT_RESOURCE_BODY_BINARY where (RESOURCE_ID = ?)";
	}

	/**
	 * returns the sql statement which retrieves the resource id from the specified table.
	 */
	public String getResourceId3Sql(String table)
	{
		return "select RESOURCE_ID from " + table + " where IN_COLLECTION = ?";
	}

	/**
	 * returns the sql statement which retrieves the resource id and xml fields from the content_resource table.
	 */
	public String getResourceIdXmlSql()
	{
		return "select RESOURCE_ID, XML, BINARY_ENTITY from CONTENT_RESOURCE where FILE_PATH IS NULL";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getResourceIdAndFilePath()
	{
		return "select RESOURCE_ID, FILE_PATH from CONTENT_RESOURCE where FILE_PATH IS NOT NULL";
	}

	/**
	 * returns the sql statement which retrieves the resource uuid from the content_resource table.
	 */
	public String getResourceUuidSql()
	{
		return "select RESOURCE_UUID from CONTENT_RESOURCE where RESOURCE_ID=?";
	}

	/**
	 * returns the sql statement which updates the resource uuid in the content_resource table for a given resource uuid.
	 */
	public String getUpdateContentResource1Sql()
	{
		return "update CONTENT_RESOURCE set RESOURCE_UUID = ? where RESOURCE_UUID = ?";
	}

	/**
	 * returns the sql statement which updates the resource uuid in the content_resource table for a given resource id.
	 */
	public String getUpdateContentResource2Sql()
	{
		return "update CONTENT_RESOURCE set RESOURCE_UUID = ? where RESOURCE_ID = ?";
	}

	/**
	 * returns the sql statement which updates the file path and xml fields in the content_resource table for a given resource id.
	 */
	public String getUpdateContentResource3Sql()
	{
		return "update CONTENT_RESOURCE set FILE_PATH = ?, XML = NULL, BINARY_ENTITY = ?, CONTEXT = ?, FILE_SIZE = ?, RESOURCE_TYPE_ID = ? where RESOURCE_ID = ?";
	}

	/**
	 * returns the sql statement which retrieves pairs of individual-dropbox-id and last-update fields from the content_dropbox_changes table for a given site-level dropbox-id.
	 */
	public String getIndividualDropboxChangeSql() 
	{
		return "select LAST_UPDATE from CONTENT_DROPBOX_CHANGES where (DROPBOX_ID = ?)";
	}

	/**
	 * returns the sql statement which retrieves the last-update field from the content_dropbox_changes table for a given individual-dropbox-id.
	 */
	public String getSiteDropboxChangeSql() 
	{
		return "select DROPBOX_ID, LAST_UPDATE from CONTENT_DROPBOX_CHANGES where (IN_COLLECTION = ?)";
	}

	/**
	 * returns the sql statement which updates the last-update field in the content_dropbox_changes table for a given site-level dropbox-id and individual-dropbox-id.
	 */
	public String getUpdateIndividualDropboxChangeSql() 
	{
		return "update CONTENT_DROPBOX_CHANGES set IN_COLLECTION = ?, LAST_UPDATE = ? where DROPBOX_ID = ?";
	}

	/**
	 * returns the sql statement which inserts the individual-dropbox-id, site-level dropbox-id and last-update fields into the content_dropbox_changes table.
	 */
	public String getInsertIndividualDropboxChangeSql() 
	{
		return "insert into CONTENT_DROPBOX_CHANGES (DROPBOX_ID, IN_COLLECTION, LAST_UPDATE) values (? , ? , ?) on duplicate key update IN_COLLECTION = ?, LAST_UPDATE = ?";
	}

	/**
	 * returns the sql statement to add the FILE_SIZE column to the CONTENT_RESOURCE table.
	 */
	public String getAddFilesizeColumnSql(String table)
	{
		return "alter table " + table + " add FILE_SIZE BIGINT default null";
	}

	/**
	 * returns the sql statement to add the CONTEXT column to the CONTENT_RESOURCE table.
	 */
	public String getAddContextColumnSql(String table)
	{
		return "alter table " + table + " add CONTEXT VARCHAR(99) default null";
	}

	/**
	 * returns the sql statement to add an index of the CONTENT column to the CONTENT_RESOURCE table.
	 */
	public String getAddContextIndexSql(String table)
	{
		return "create index " + table.trim() + "_CI on " + table + " (CONTEXT)";
	}
	
	/**
	 * returns the sql statement to add the RESOURCE_TYPE_ID column to the specified table.
	 */
	public String getAddResourceTypeColumnSql(String table)
	{
		return "alter table " + table + " add RESOURCE_TYPE_ID VARCHAR(255) default null"; 
	}
	
	/**
	 * returns the sql statement to add an index of the RESOURCE_TYPE_ID column to the specified table.
	 */
	public String getAddResourceTypeIndexSql(String table)
	{
		return "create index " + table.trim() + "_RTI on " + table + " (RESOURCE_TYPE_ID)";
	}

	/**
	 * returns the sql statement which retrieves the total number of bytes within a site-level collection (context) in the CONTENT_RESOURCE table.
	 */
	public String getQuotaQuerySql()
	{
		return "select SUM(FILE_SIZE) from CONTENT_RESOURCE where CONTEXT = ?";
	}
	/**
	 * returns the sql statement which retrieves the total number of bytes within a site-level collection (context) in the CONTENT_RESOURCE table.
	 */
	public String getDropBoxQuotaQuerySql()
	{
		return "select SUM(FILE_SIZE) from CONTENT_RESOURCE where IN_COLLECTION LIKE ?";
	}
	
	/**
	 * returns the sql statement which retrieves the RESOURCE_ID and XML values for all entries in the CONTENT_RESOURCE table where file-size is null.
	 */
	public String getAccessResourceIdAndXmlSql(String table)
	{
		return "select RESOURCE_ID, RESOURCE_UUID, XML from " + table + " where FILE_SIZE is NULL";
	}

	/**
	 * returns the sql statement which updates a row in the CONTENT_RESOURCE table with values for CONTEXT and FILE_SIZE.
	 */
	public String getContextFilesizeValuesSql(String table, boolean addingUuid)
	{
		String sql = "update " + table + " set CONTEXT = ?, FILE_SIZE = ?, RESOURCE_TYPE_ID = ? where RESOURCE_UUID = ?";
		if(addingUuid)
		{
			sql = "update " + table + " set CONTEXT = ?, FILE_SIZE = ?, RESOURCE_TYPE_ID = ?, RESOURCE_UUID = ? where RESOURCE_ID = ?";
		}
		return sql;
	}

	public String getFilesizeColumnExistsSql() 
	{
		return "show columns from CONTENT_RESOURCE like 'FILE_SIZE'";
	}

	/**
	 * returns the sql statement to check if any rows exist with NULL FILE_SIZE values
	 */
	public String getFilesizeExistsSql()
	{
	 	return "select TOP 1 RESOURCE_ID from CONTENT_RESOURCE where FILE_SIZE is NULL";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.ContentServiceSql#getCreateTemporaryUTF8TestTable(java.lang.String)
	 */
	public String getCreateTemporaryUTF8TestTable(String tempTableName)
	{
		return "create table " + tempTableName + " ( id int, bval varchar(2048) )";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.ContentServiceSql#getDropTemporaryUTF8TestTable(java.lang.String)
	 */
	public String getDropTemporaryUTF8TestTable(String tempTableName)
	{
		return "drop table " + tempTableName;
	}

	/**
	 * returns the sql statement which retrieves the BINARY_ENTITY and XML values for all entries in the CONTENT_RESOURCE table, 
	 * selecting by the RESOURCE_TYPE_ID with first and last record indexes, and returned in ascending order by RESOURCE_ID.
	 */
	public String getSelectByResourceTypeQuerySql()
	{
		return "select BINARY_ENTITY, XML from CONTENT_RESOURCE where RESOURCE_TYPE_ID = ? ORDER BY RESOURCE_ID LIMIT ?, ? ";
	}

	/**
	 * returns the sql statement which retrieves the total number of bytes within a site-level collection skiping user folders.
	 * KNL-1084, SAK-22169
	 */
	public String getDropBoxRootQuotaQuerySql() {
	    return "select SUM(FILE_SIZE) from CONTENT_RESOURCE where IN_COLLECTION LIKE ? and not exists (select 1 from SAKAI_USER_ID_MAP where USER_ID = substr(in_collection,length(?)+1,instr(substr(in_collection,length(?)+1),'/')-1))";
	}

}
