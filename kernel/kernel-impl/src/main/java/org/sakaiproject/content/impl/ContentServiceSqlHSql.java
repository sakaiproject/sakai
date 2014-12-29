/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/ChatServiceSqlHSql.java $
 * $Id: ChatServiceSqlHSql.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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
 * methods for accessing content data in a hypersonic sql database.
 */
public class ContentServiceSqlHSql extends ContentServiceSqlDefault
{
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
	 * returns the sql statement to add the RESOURCE_TYPE_ID column to the specified table.
	 */
	public String getAddResourceTypeColumnSql(String table)
	{
		return "alter table " + table + " add RESOURCE_TYPE_ID VARCHAR(255) default null"; 
	}
	
	/**
	 * returns the sql statement to add an index of the CONTENT column to the CONTENT_RESOURCE table.
	 */
	public String getAddContextIndexSql(String table)
	{
		return "create index " + table.trim() + "_CONTEXT_INDEX on " + table + " (CONTEXT)";
	}
	
	/**
	 * The default sql uses "show columns" to determine whetherthe file_size column exists.  HSQL does not support "show columns".
	 * This will throw an SQL exception.  The method that uses this SQL should catch the exception and handle it appropriately.
	 */
	public String getFilesizeColumnExistsSql() 
	{
		return "show columns from CONTENT_RESOURCE like 'FILE_SIZE'";
	}
	
	/**
	 * returns the sql statement which inserts the individual-dropbox-id, site-level dropbox-id and last-update fields into the content_dropbox_changes table.
	 */
	public String getInsertIndividualDropboxChangeSql() 
	{
		return "insert into CONTENT_DROPBOX_CHANGES (DROPBOX_ID, IN_COLLECTION, LAST_UPDATE) values (? , ? , ?)";
		// To use the line below instead (preferable), HSQLDB must be >= 1.9
		//return "merge into CONTENT_DROPBOX_CHANGES using dual on (dual.dummy is not null and CONTENT_DROPBOX_CHANGES.DROPBOX_ID = ?) when not matched then insert (DROPBOX_ID, IN_COLLECTION, LAST_UPDATE) values (?, ?, ?) when matched then update set CONTENT_DROPBOX_CHANGES.IN_COLLECTION = ?, LAST_UPDATE = ?";
	}

	/**
	 * returns the sql statement which retrieves the total number of bytes within a site-level collection skiping user folders.
	 * KNL-1084, SAK-22169
	 */
	public String getDropBoxRootQuotaQuerySql() {
		return "select SUM(FILE_SIZE) from CONTENT_RESOURCE where IN_COLLECTION LIKE ? and not exists (select 1 from SAKAI_USER_ID_MAP where USER_ID = substr(in_collection,length(?)+1,locate('/',substr(in_collection,length(?)+1))-1))";
	}

}
