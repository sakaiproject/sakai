/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/FlatStorageSql.java $
 * $Id: FlatStorageSql.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
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

/**
 * database methods.
 */
public interface FlatStorageSql
{
	public String getDeleteLockSql();

	public String getDeleteSql(String table, String idField);

	public String getIdField(String table);

	public String getInsertSql(String table, String idField, String extraIdField);

	public String getInsertLockSql();

	public String getOrder(String table, String sortField1, String sortField2);

	public String getRecordId(String recordId);

	public String getSelectCountSql(String table);

	public String getSelectCount2Sql(String table, String join, String where);

	public String getSelectFieldSql(String table, String field);

	public String getSelectFieldsSql(String table, String fieldList);

	public String getSelectFieldsSql(String table, String fieldList, String idField);

	public String getSelectFieldsSql1(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end);

	public String getSelectFieldsSql2(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end);

	public String getSelectFieldsSql3(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
			String join, String where, String order);

	public String getSelectFieldsSql4(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
			String join, String where, String order);

	public Object[] getSelectFieldsFields(int first, int last);

	public String getSelectNameValueSql(String table, String idField);

	public String getUpdateSql(String table, String fieldList, String idField);
}
