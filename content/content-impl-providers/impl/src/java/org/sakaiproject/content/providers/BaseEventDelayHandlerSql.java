/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 * database methods.
 */
public interface BaseEventDelayHandlerSql 
{
	
	/**
	 * returns the sql statement which adds a new delayed event
	 */
	String getDelayWriteSql();

	/**
	 * returns the sql statement which reads a delayed event  
	 */
	String getDelayReadSql();

	/**
	 * returns the sql statement which reads the id of a delayed event 
	 */
	String getDelayFindFineSql();

	/**
	 * returns the sql statement which reads a delayed event  
	 */
	String getDelayFindEventSql();
	
	/**
	 * returns the sql statement which reads a delayed event 
	 */
	String getDelayFindByRefSql();

	/**
	 * returns the sql statement which reads a delayed event 
	 */
	String getDelayFindByRefEventSql();

	/**
	 * returns the sql statement which deletes a delayed event
	 */
	String getDelayDeleteSql();

}
