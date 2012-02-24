/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/FlatStorageSqlDefault.java $
 * $Id: FlatStorageSqlDefault.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
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
 * methods for accessing flat storage data in a database.
 */
public class FlatStorageSqlDefault implements FlatStorageSql
{
	public String getDeleteLockSql()
	{
		return "delete from SAKAI_LOCKS where TABLE_NAME = ? and RECORD_ID = ?";
	}

	public String getDeleteSql(String table, String idField)
	{
		return "delete from " + table + " where ( " + idField + " = ? )";
	}

	public String getIdField(String table)
	{
		return ", NEXT VALUE FOR " + table + "_SEQ";
	}

	public String getInsertLockSql()
	{
		return "insert into SAKAI_LOCKS" + " (TABLE_NAME,RECORD_ID,LOCK_TIME,USAGE_SESSION_ID) values (?, ?, ?, ?)";
	}

	public String getInsertSql(String table, String idField, String extraIdField)
	{
		return "insert into " + table + "( " + idField + ", NAME, VALUE" + ((extraIdField != null) ? (", " + extraIdField) : "") + " ) values (?,?,?"
				+ ((extraIdField != null) ? ",?" : "") + ")";
	}

	public String getRecordId(String recordId)
	{
		return recordId;
	}

	public String getOrder(String table, String sortField1, String sortField2)
	{
		return table + "." + sortField1 + (sortField2 == null ? "" : "," + table + "." + sortField2);
	}

	public String getSelectCountSql(String table)
	{
		return "select count(1) from " + table;
	}

	public String getSelectCount2Sql(String table, String join, String where)
	{
		return "select count(1) from " + table + ((join == null) ? "" : ("," + join))
				+ (((where != null) && (where.length() > 0)) ? (" where " + where) : "");
	}

	public String getSelectFieldSql(String table, String field)
	{
		return "select " + field + " from " + table + " where ( " + field + " = ? )";
	}

	public String getSelectFieldsSql(String table, String fieldList)
	{
		return "select " + fieldList + " from " + table;
	}

	public String getSelectFieldsSql(String table, String fieldList, String idField)
	{
		return "select " + fieldList + " from " + table + " where ( " + idField + " = ? )";
	}

	public String getSelectFieldsSql1(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end)
	{
		return "select limit " + begin + " " + end + " " + fieldList + " from " + table + " order by " + table + "." + sortField1
				+ (sortField2 == null ? "" : "," + table + "." + sortField2);
	}

	// only used by ms sql server
	public String getSelectFieldsSql2(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end)
	{
		return null;
	}

	public String getSelectFieldsSql3(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
			String join, String where, String order)
	{
		return "select limit " + begin + " " + end + " " + fieldList + " from " + table + ((join == null) ? "" : ("," + join))
				+ (((where != null) && (where.length() > 0)) ? (" where " + where) : "") + " order by " + order + "," + table + "." + sortField1
				+ (sortField2 == null ? "" : "," + table + "." + sortField2);
	}

	// only used by ms sql server
	public String getSelectFieldsSql4(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
			String join, String where, String order)
	{
		return null;
	}

	public Object[] getSelectFieldsFields(int first, int last)
	{
		return null;
	}

	public String getSelectNameValueSql(String table, String idField)
	{
		return "select NAME, VALUE from " + table + " where ( " + idField + " = ? )";
	}

	public String getUpdateSql(String table, String fieldList, String idField)
	{
		return "update " + table + " set " + fieldList + " where ( " + idField + " = ? )";
	}
}
