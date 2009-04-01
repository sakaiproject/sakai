/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 Sakai Foundation
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

package org.sakaiproject.content.providers;

/**
 * methods for accessing delayed events in a database.
 */
public class BaseEventDelayHandlerSqlDefault implements BaseEventDelayHandlerSql {
   
	public String getDelayDeleteSql() {
		return "delete from SAKAI_EVENT_DELAY where EVENT_DELAY_ID = ?";
	}
	
	public String getDelayFindByRefEventSql() {
		return "select EVENT_DELAY_ID from SAKAI_EVENT_DELAY where REF = ? and EVENT = ?";
	}
	
	public String getDelayFindByRefSql() {
		return "select EVENT_DELAY_ID from SAKAI_EVENT_DELAY where REF = ?";
	}
	
	public String getDelayFindEventSql() {
		return "select EVENT_DELAY_ID from SAKAI_EVENT_DELAY where EVENT = ? and EVENT_CODE = ? and PRIORITY = ? and REF = ?";
	}
	
	public String getDelayFindFineSql() {
		return "select EVENT_DELAY_ID from SAKAI_EVENT_DELAY where EVENT = ? and EVENT_CODE = ? and PRIORITY = ? and REF = ? and USER_ID = ?";
	}
	
	public String getDelayReadSql() {
		return "select EVENT_DELAY_ID, EVENT, EVENT_CODE, PRIORITY, REF, USER_ID from SAKAI_EVENT_DELAY where EVENT_DELAY_ID = ?";
	}
	
	public String getDelayWriteSql() {
		
		return "insert into SAKAI_EVENT_DELAY (EVENT_DELAY_ID, EVENT, EVENT_CODE, PRIORITY, REF, USER_ID) VALUES " + 
		       "(NEXT VALUE FOR SAKAI_EVENT_DELAY_SEQ, ?, ?, ?, ?, ?)";
	}	
}
