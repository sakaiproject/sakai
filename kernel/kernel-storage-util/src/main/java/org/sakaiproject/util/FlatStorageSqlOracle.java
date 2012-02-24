/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/FlatStorageSqlOracle.java $
 * $Id: FlatStorageSqlOracle.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
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
 * methods for accessing flat storage data in an oracle database.
 */
public class FlatStorageSqlOracle extends FlatStorageSqlDefault
{
	public String getIdField(String table)
	{
		return "," + table + "_SEQ.NEXTVAL";
	}

	public String getSelectFieldsSql1(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end)
	{
		return "select * from (select a.*, rownum rnum from (select " + fieldList + " from " + table + " order by "
				+ table + "." + sortField1 + (sortField2 == null ? "" : "," + table + "." + sortField2) + "," + table + "." + idField
				+ ") a where rownum <= ? ) where rnum >= ?";
	}

	public String getSelectFieldsSql3(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
			String join, String where, String order)
	{
		return  "select * from (select a.*, rownum rnum from (select " + fieldList + " from "
				+ table + ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "")
				+ " order by " + order + "," + table + "." + idField + " ) a where rownum <= ? ) where rnum >= ?";
	}

	public Object[] getSelectFieldsFields(int first, int last)
	{
		Object[] fields = new Object[2];

		fields[0] = Long.valueOf(last);
		fields[1] = Long.valueOf(first);
		

		return fields;
	}
}
