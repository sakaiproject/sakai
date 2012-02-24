/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/DoubleStorageSql.java $
 * $Id: DoubleStorageSql.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
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

package org.sakaiproject.util;

import org.sakaiproject.javax.Order;

/**
 * database methods.
 */
public interface DoubleStorageSql
{
	public String getDeleteSql(String table, String idField);

	public String getDelete2Sql(String table, String idField1, String idField2);

	public String getDeleteLocksSql();

	public String getInsertSql(String table, String fieldList);

	public String getInsertSql2();

	public String getInsertSql3(String table, String fieldList, String params);

	public String getRecordId(String recordId);
    
	public String getCountSql(String table, String idField);

	public String getSelect1Sql(String table, String idField);

	public String getSelect9Sql(String table, String idField);

	public String getSelectIdSql(String table, String idField1, String idField2);

	public String getSelectXml1Sql(String table);

	public String getSelectXml2Sql(String table, String idField);

	public String getSelectXml3Sql(String table, String idField, String ref);

	public String getSelectXml4Sql(String table, String idField1, String idField2);

	public String getSelectXml5Sql(String table, String idField, String orderField, boolean asc);
   
	public String getSelectXml5filterSql(String table, String idField, String orderString, String filter);

	public String getSelectXml6Sql(String table, String idField1, String idField2, String id, String ref);

	public String getUpdateSql(String table, String idField);

	public String getUpdate2Sql(String table, String idField1, String idField2, String fieldList);

	public String addLimitToQuery(String inSql, int startRec, int endRec);

	public String addTopToQuery(String inSql, int endRec);

	public String getSearchWhereClause(String[] searchFields);

        public String getOrderClause(Order [] orders,  String orderField, boolean asc);

	public String getCountSqlWhere(String table, String idField, String whereClause);
        
}
