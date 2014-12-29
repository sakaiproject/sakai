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
 * methods for accessing cluster event tracking data in a database.
 */
public class ClusterEventTrackingServiceSqlDefault implements ClusterEventTrackingServiceSql
{
   /**
    * returns the sql statement which inserts an event into the sakai_event table.
    */
   public String getInsertEventSql()
   {
      return "insert into SAKAI_EVENT (EVENT_ID,EVENT_DATE,EVENT,REF,SESSION_ID,EVENT_CODE,CONTEXT) " +
             "values      (NEXT VALUE FOR SAKAI_EVENT_SEQ, "  + // form the id based on the sequence
                          "?, "                               + // date
                          "?, "                               + // event
                          "?, "                               + // reference
                          "?, "                               + // session id
                          "?, "                               + // context
                          "? )";                                // code
   }

   /**
	 * returns the sql statement which retrieves an event from the sakai_event and sakai_session tables.
	 */
	public String getEventSql()
	{
	    return "select SAKAI_EVENT.EVENT_ID,SAKAI_EVENT.EVENT_DATE,SAKAI_EVENT.EVENT,SAKAI_EVENT.REF,SAKAI_EVENT.SESSION_ID,SAKAI_EVENT.EVENT_CODE,SAKAI_EVENT.CONTEXT,SAKAI_SESSION.SESSION_SERVER "
	            + "from SAKAI_EVENT "
	            + "left join SAKAI_SESSION ON SAKAI_EVENT.SESSION_ID = SAKAI_SESSION.SESSION_ID "
	            + "where (SAKAI_EVENT.EVENT_ID > ?)";
	}

	/**
	 * returns the sql statement which retrieves the largest event id from the sakai_event table.
	 */
	public String getMaxEventIdSql()
	{
		return "select MAX(EVENT_ID) from SAKAI_EVENT";
	}

    /* (non-Javadoc)
     * @see org.sakaiproject.event.impl.ClusterEventTrackingServiceSql#getEventsCountSql()
     */
    public String getEventsCountSql() {
        return "select COUNT(*) from SAKAI_EVENT";
    }

}
