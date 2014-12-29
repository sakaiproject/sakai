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

/**
 * 
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 * 
 * This is a marker interface that denotes a class that can be
 * used to lookup Rules that must be evaluated in response to an event.
 * 
 * e.g. a GradebookAssignmentKey is an EventKey that contains a 
 * Gradebook ID and an Assignment name. Together these two Strings
 * uniquely identify a single Gradebook assignment that one or more
 * Rules can be bound to.
 * 
 * When the ConditionService is notified of an event, it creates an EventKey
 * from the data in the event. The EventKey can then be used as a lookup key
 * to retrieve Rules that must be evaluated in response to the event.
 *
 */
public interface EventKey {

}
