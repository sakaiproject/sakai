/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/ChatServiceSqlDb2.java $
 * $Id: ChatServiceSqlDb2.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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
 * methods for accessing content data in a db2 database.
 */
public class ContentServiceSqlDb2 extends ContentServiceSqlDefault
{
   public String getFilesizeColumnExistsSql()
   {
      return "select colname from syscat.columns where tabname = 'CONTENT_RESOURCE' and colname = 'FILE_SIZE'";
   }

   public String getAddFilesizeColumnSql(String table)
   {
      return "alter table " + table + " add FILE_SIZE BIGINT;";
   }

   public String getFilesizeExistsSql()
   {
	  return "select RESOURCE_ID from CONTENT_RESOURCE where FILE_SIZE is NULL FETCH FIRST 1 ROWS ONLY";
   }
}
