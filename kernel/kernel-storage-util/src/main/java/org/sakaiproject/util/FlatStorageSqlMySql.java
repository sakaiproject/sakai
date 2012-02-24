/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/FlatStorageSqlMySql.java $
 * $Id: FlatStorageSqlMySql.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
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
 * methods for accessing flat storage data in a mysql database.
 */
public class FlatStorageSqlMySql extends FlatStorageSqlDefault
{

	public String getIdField(String table)
	{
		return "";
	}

	public String getRecordId(String recordId)
	{
		return (recordId == null ? "null" : recordId.hashCode() + " - " + recordId);
	}

	public String getSelectFieldsSql1(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end)
	{
		return "select " + fieldList + " from " + table + " order by " + table + "." + sortField1
				+ (sortField2 == null ? "" : ", " + table + "." + sortField2) + " limit " + end + " offset " + begin;
	}

	public String getSelectFieldsSql3(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
			String join, String where, String order)
	{
		return "select " + fieldList + " from " + table + ((join == null) ? "" : ("," + join))
				+ (((where != null) && (where.length() > 0)) ? (" where " + where) : "") + " order by " + order + "," + table + "." + sortField1
				+ (sortField2 == null ? "" : "," + table + "." + sortField2) + " limit " + end + " offset " + begin;
	}
}
