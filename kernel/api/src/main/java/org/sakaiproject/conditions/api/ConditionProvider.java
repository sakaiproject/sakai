/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/antivirus/api/VirusFoundException.java $
 * $Id: VirusFoundException.java 68335 2009-10-29 08:18:43Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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
package org.sakaiproject.conditions.api;

import java.util.Map;

/**
 * interface to specify the contract for a service to make conditions available to any other tool or service 
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 *
 */
public interface ConditionProvider {
	
	/**
	 * just a unique name that each <code>ConditionProvider</code> can be accessed by
	 * @return unique name of this <code>ConditionProvider</code>
	 */
	String getId();
	
	/**
	 * return a <code>Map</code> of entities for the specified context
	 * where the context is a Sakai site id,
	 * and the entities are specific to the service this <code>ConditionProvider</code> represents,
	 * e.g. for the gradebook <code>ConditionProvider</code>, the entities will be the assignments for
	 * the specified context.
	 * 
	 * @param contextId
	 * @return
	 */
	Map<String, String> getEntitiesForContext(String contextId);
	
	/**
	 * get a <code>Map</code> of event names, e.g. 'gradebook.updateItemScore'
	 * to the name of the class concerned with that event, e.g. 'org.sakaiproject.conditions.impl.AssignmentGrading'
	 * @return the names of events this <code>ConditionProvider</code> is concerned with,
	 * and the names of classes that correspond to those events
	 */
	Map<String, String> getEventToDomainClassMapping();

  /**
   * make a request for information from the ConditionProvider
   * @param type the name of the information you want back from the ConditionProvider
   * @param context a contextual key, such as an assignment id from the gradebook
   * @return An arbitrary map of keys and values which can be used to evaluate rules
   */
  Map<String, String> getData(String type, String context);
}