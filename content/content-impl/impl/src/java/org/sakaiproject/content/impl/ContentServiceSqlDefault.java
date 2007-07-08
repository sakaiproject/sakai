/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/ChatServiceSqlDefault.java $
 * $Id: ChatServiceSqlDefault.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		return "select RESOURCE_ID, XML from CONTENT_RESOURCE where FILE_PATH IS NULL";
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
		return "update CONTENT_RESOURCE set FILE_PATH = ?, XML = ? where RESOURCE_ID = ?";
	}
}
