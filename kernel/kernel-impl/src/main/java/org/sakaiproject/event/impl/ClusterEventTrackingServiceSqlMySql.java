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
 *       http://www.opensource.org/licenses/ECL-2.0
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
 * methods for accessing cluster event tracking data in a mysql database.
 */
public class ClusterEventTrackingServiceSqlMySql extends ClusterEventTrackingServiceSqlDefault
{
   /**
    * returns the sql statement which inserts an event into the sakai_event table.
    */
   public String getInsertEventSql()
   {
      // leave out the EVENT_ID as it will be automatically generated on the server
      return "insert into SAKAI_EVENT (EVENT_DATE, EVENT, REF, SESSION_ID, EVENT_CODE, CONTEXT) " +
              "values     (?, " + // date
                          "?, " + // event
                          "?, " + // reference
                          "?, " + // session id
                          "?, " + // code
                          "?)";   // context
   }


    @Override
    public String getEventsCountSql() {
        return "select TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_NAME='SAKAI_EVENT' ORDER BY CREATE_TIME LIMIT 1;";
    }

}
