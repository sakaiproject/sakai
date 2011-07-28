/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/authz/trunk/authz-api/api/src/java/org/sakaiproject/authz/api/AuthzGroup.java $
 * $Id: AuthzGroup.java 7063 2006-03-27 17:46:13Z ggolden@umich.edu $
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

package org.sakaiproject.authz.impl;

/**
 * methods for accessing authz data in a db2 database.
 */
public class DbAuthzGroupSqlDb2 extends DbAuthzGroupSqlDefault
{
   @Override
   public String getInsertRealmFunctionSql()
   {
      return "insert into SAKAI_REALM_FUNCTION (FUNCTION_NAME) values (?)";
   }

   @Override
   public String getInsertRealmRoleSql()
   {
      return "insert into SAKAI_REALM_ROLE (ROLE_NAME) values(?)";
   }

   @Override
   public String getCountRealmRoleFunctionSql(String anonymousRole, String authorizationRole, boolean authorized)
      {
         return "select count(1) " + "from   SAKAI_REALM_RL_FN MAINTABLE "
               + "       LEFT JOIN SAKAI_REALM_RL_GR GRANTED_ROLES ON (MAINTABLE.REALM_KEY = GRANTED_ROLES.REALM_KEY AND "
               + "       MAINTABLE.ROLE_KEY = GRANTED_ROLES.ROLE_KEY), SAKAI_REALM REALMS, SAKAI_REALM_ROLE ROLES, SAKAI_REALM_FUNCTION FUNCTIONS "
               + "where "
               +
               // our criteria
               "  (ROLES.ROLE_NAME in('" + anonymousRole + "'" + (authorized ? ",'" + authorizationRole + "'" : "") + ") or "
               + "  (GRANTED_ROLES.USER_ID = ? AND GRANTED_ROLES.ACTIVE = '1')) AND FUNCTIONS.FUNCTION_NAME = ? AND REALMS.REALM_ID in (?) " +
               // for the join
               "  AND MAINTABLE.REALM_KEY = REALMS.REALM_KEY AND MAINTABLE.FUNCTION_KEY = FUNCTIONS.FUNCTION_KEY AND MAINTABLE.ROLE_KEY = ROLES.ROLE_KEY ";
      }
}
