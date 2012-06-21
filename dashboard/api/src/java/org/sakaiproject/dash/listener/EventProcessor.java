/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.dash.listener;

import org.sakaiproject.event.api.Event;

/**
 * EventProcessor implementations are registered with DashboardCommonLogic. 
 * DashboardCommonLogic will invoke EventProcessor.getEventIdentifer() to 
 * get the unique identifier for a type of sakai event handled by that
 * implementation and then call its EventProcessor.processEvent(Event) 
 * whenever DashboardCommonLogic encounters an event with that identifier.   
 * 
 * Implementations of EventProcessor.processEvent(Event) should do 
 * whatever is appropriate in response to the event.  Usually that 
 * would be adding, updating or deleting dashboard items. For example,
 * an implementation targeting "content.new" events might create one 
 * NewsItem and many NewsLinks (one for each user with access to the 
 * resource referenced by the event). An implementation for a 
 * "content.revise" event might change an existing NewsItem (if the 
 * event results from a change in the display name for the resource, 
 * for example), or in many NewsLinks (if the availability or access 
 * for the resource changed).  An "asn.new.assignment" event might lead 
 * to creation of a NewsItem and a CalendarItem, so the creation of the
 * assignment would be reported in the dashboard's "News" section, and 
 * the due date for the assignment would appear in the "Calendar" 
 * section of the dashboard. 
 * 
 * If there are not already entries in the dashboard's database tables
 * for the event's context (i.e. site), for all of the users who should
 * see the item in their dashboards, and for the realm(s) to which the
 * item belongs, appropriate entries should be added for any missing 
 * Context, Person or Realm items.
 * 
 * THIS WILL BE MOVED TO KERNEL
 */
public interface EventProcessor
{
    /**
     * Get the unique identifier for the events that will be handled 
     * by this processor
     */
    public String getEventIdentifer();
    
    /**
     * Deal with one event -- adding, updating or deleting dashboard 
     * items as appropriate.
     * @param event The event to be processed.
     */     
    public void processEvent(Event event);

}
