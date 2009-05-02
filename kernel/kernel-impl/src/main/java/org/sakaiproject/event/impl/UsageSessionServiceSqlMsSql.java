/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/UsageSessionServiceSqlMsSql.java $
 * $Id: UsageSessionServiceSqlMsSql.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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

package org.sakaiproject.event.impl;

/**
 * methods for accessing session usage data in an ms sql server database.
 */
public class UsageSessionServiceSqlMsSql extends UsageSessionServiceSqlDefault
{
   public String getSakaiSessionSql1()
   {
      // SESSION_USER is a reserved word in mssql and db2
      return "select SESSION_ID,SESSION_SERVER,[SESSION_USER],SESSION_IP,SESSION_USER_AGENT,SESSION_START,SESSION_END from SAKAI_SESSION where SESSION_ID = ?";
   }

   public String getInsertSakaiSessionSql()
   {
      // SESSION_USER is a reserved word in mssql and db2
      return "insert into SAKAI_SESSION (SESSION_ID,SESSION_SERVER," +
         "[SESSION_USER],SESSION_IP,SESSION_USER_AGENT,SESSION_START,SESSION_END) values (?, ?, ?, ?, ?, ?, ?)";
   }


   public String getSakaiSessionSql3(String alias, String joinAlias, String joinTable, String joinColumn, String joinCriteria)
   {
      return "select " + alias + ".SESSION_ID," + alias + ".SESSION_SERVER," + alias + ".[SESSION_USER]," + alias + ".SESSION_IP," + alias + ".SESSION_USER_AGENT," + alias + ".SESSION_START," + alias + ".SESSION_END," + alias + ".SESSION_ACTIVE " +
             "from   SAKAI_SESSION " + alias                                    + " " +
             "inner join " + joinTable + " " + joinAlias                        + " " +
             "ON "    + alias + ".SESSION_ID = " + joinAlias + "." + joinColumn + " " +
             "where " + alias + ".SESSION_ACTIVE=1 and " + joinCriteria;
   }


   public String getSakaiSessionSql2()
   {
      return "select [SESSION_USER],SESSION_IP,SESSION_USER_AGENT,SESSION_START,SESSION_END from SAKAI_SESSION where SESSION_ACTIVE=1 ORDER BY SESSION_SERVER ASC, SESSION_START ASC";
   }

}
