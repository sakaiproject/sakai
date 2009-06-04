/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * methods for accessing flat storage data in a db2 database.
 */
public class FlatStorageSqlDb2 extends FlatStorageSqlDefault
{
	public String getIdField(String table)
	{
      // for DB2, the DEFAULT keyword must be specified for the identity column
      // when it is included in the list of columns on the INSERT
		return ", DEFAULT";
	}

   public String getSelectFieldsSql1(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end)
   {
      return "with TEMP_QUERY as (select " + table + ".*,ROW_NUMBER() over (order by " + table + "." + sortField1
            + (sortField2 == null ? "" : "," + table + "." + sortField2) + "," + table + "." + idField + ") as rank from " + table + ")";
   }

   public String getSelectFieldsSql2(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end)
   {
      fieldList = fieldList.replaceAll(table + "\\.", "TEMP_QUERY.");
      return "select " + fieldList + " from TEMP_QUERY where rank between ? and ? order by TEMP_QUERY." + sortField1
            + (sortField2 == null ? "" : ", TEMP_QUERY." + sortField2) + (!idField.equals(sortField1) ? (", TEMP_QUERY." + idField) : "");
   }

   public String getSelectFieldsSql3(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
         String join, String where, String order)
   {
      return "with TEMP_QUERY as (select " + table + ".*,ROW_NUMBER() over (order by " + order + "," + table + "." + idField + ") as rank from "
            + table + ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "") + ")";
   }

   public String getSelectFieldsSql4(String table, String fieldList, String idField, String sortField1, String sortField2, int begin, int end,
         String join, String where, String order)
   {
      String sql = " select " + fieldList.replaceAll(table + "\\.", "TEMP_QUERY.") + " from TEMP_QUERY where rank between ? and ? order by ";
      // here, need to replace table name with TEMP_QUERY in 'order'
      String newOrder = String.valueOf(order);
      newOrder = newOrder.replaceAll(table + "\\.", "TEMP_QUERY.");
      sql += newOrder;
      // only add next part if it's not already there
      if (newOrder.indexOf("TEMP_QUERY." + idField) < 0) sql += ",TEMP_QUERY." + idField;

      return sql;
   }

    public Object[] getSelectFieldsFields(int first, int last)
   {
      Object[] fields = new Object[2];

      fields[0] = Long.valueOf(first);
      fields[1] = Long.valueOf(last);

      return fields;
   }

}
