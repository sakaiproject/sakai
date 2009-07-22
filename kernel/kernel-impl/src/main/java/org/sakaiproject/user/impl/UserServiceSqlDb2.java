/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/user/user-impl/impl/src/java/org/sakaiproject/user/impl/UserServiceSqlDb2.java $
 * $Id: UserServiceSqlDb2.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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

package org.sakaiproject.user.impl;

/**
 * methods for accessing user data in a db2 database.
 */
public class UserServiceSqlDb2 extends UserServiceSqlDefault
{
     public String getUserWhereSql()
     {
         return "SAKAI_USER.USER_ID = SAKAI_USER_ID_MAP.USER_ID AND (SAKAI_USER.USER_ID = ? OR UPPER(EID) LIKE UPPER(?||'') OR EMAIL_LC LIKE ? OR UPPER(FIRST_NAME) LIKE UPPER(?||'') OR UPPER(LAST_NAME) LIKE UPPER(?||''))";
     }
 
}
