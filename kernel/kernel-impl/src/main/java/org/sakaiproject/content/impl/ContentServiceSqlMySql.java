/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/ChatServiceSqlMySql.java $
 * $Id: ChatServiceSqlMySql.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
 * methods for accessing content data in a mysql database.
 */
public class ContentServiceSqlMySql extends ContentServiceSqlDefault
{
	/**
	 * returns the sql statement to add the FILE_SIZE column to the CONTENT_RESOURCE table.
	 */
	public String getAddFilesizeColumnSql(String table)
	{
		return "alter table " + table + " add FILE_SIZE BIGINT;";
	}

	/**
	 * returns the sql statement to add the CONTEXT column to the CONTENT_RESOURCE table.
	 */
	public String getAddContextColumnSql(String table)
	{
		return "alter table " + table + " add CONTEXT VARCHAR(99);";
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
		return "create index " + table.trim() + "_CONTEXT_INDEX on " + table + " (CONTEXT);";
	}
	
	/**
	 * returns the sql statement to check if any FILE_SIZE columns exist with NULL values
	 */
	public String getFilesizeExistsSql() 
	{
		return "select RESOURCE_ID from CONTENT_RESOURCE where FILE_SIZE is NULL limit 1";
	}
}
