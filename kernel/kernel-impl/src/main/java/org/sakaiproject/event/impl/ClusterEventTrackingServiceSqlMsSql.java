/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/ChatServiceSqlMsSql.java $
 * $Id: ChatServiceSqlMsSql.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
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
 * methods for accessing cluster event tracking data in an ms sql server database.
 */
public class ClusterEventTrackingServiceSqlMsSql extends ClusterEventTrackingServiceSqlDefault
{
   /**
    * returns the sql statement which inserts an event into the sakai_event table.
    */
   public String getInsertEventSql()
   {
      // leave out the EVENT_ID as it will be automatically generated on the server
      return "insert into SAKAI_EVENT (EVENT_DATE,EVENT,REF,SESSION_ID,EVENT_CODE,CONTEXT) values (?, ?, ?, ?, ?, ? )";
   }
}
