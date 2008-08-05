/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/rsmart/dbrefactor/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/ChatServiceSqlOracle.java $
 * $Id: ChatServiceSqlOracle.java 3560 2007-02-19 22:08:01Z jbush@rsmart.com $
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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
 * methods for accessing cluster event tracking data in an oracle database.
 */
public class ClusterEventTrackingServiceSqlOracle extends ClusterEventTrackingServiceSqlDefault {

   /**
    * returns the sql statement which inserts an event into the sakai_event table.
    */
   public String getInsertEventSql() {
      return "insert into SAKAI_EVENT (EVENT_ID,EVENT_DATE,EVENT,REF,SESSION_ID,EVENT_CODE,CONTEXT) " +
             "values      (SAKAI_EVENT_SEQ.NEXTVAL," + // form the id based on the sequence
                          "?, "                      + // date
                          "?, "                      + // event
                          "?, "                      + // reference
                          "?, "                      + // session id
                          "?, "                      + // code
                          "?) ";                       // context
   }

   /**
    * returns the sql statement which retrieves an event from the sakai_event and sakai_session tables.
    */
	public String getEventSql()
	{
		// this now has Oracle specific hint to improve performance with large tables -ggolden
		return "select /*+ FIRST_ROWS */ EVENT_ID,EVENT_DATE,EVENT,REF,SAKAI_EVENT.SESSION_ID,EVENT_CODE,CONTEXT,SESSION_SERVER "
				+ "from   SAKAI_EVENT,SAKAI_SESSION " + "where  (SAKAI_EVENT.SESSION_ID = SAKAI_SESSION.SESSION_ID(+)) and (EVENT_ID > ?)";
	}
}
