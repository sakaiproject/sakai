/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.api;

import org.sakaiproject.entity.api.AttachmentContainer;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.TimeRange;

/**
* <p>CalendarEvent is the interface for events placed into a Calendar Service Calendar.</p>
* <p>Each event has a time range, and other information in the event's (Resource) properties.</p>
*/
public interface CalendarEvent
	extends Entity, Comparable, AttachmentContainer
{
	/**
	* Access the time range
	* @return The event time range
	*/
	public TimeRange getRange();

	/**
	* Access the display name property (cover for PROP_DISPLAY_NAME).
	* @return The event's display name property.
	*/
	public String getDisplayName();

	/**
	* Access the description property as plain text (cover for PROP_DESCRIPTION).
	* @return The event's description property.
	*/
	public String getDescription();
	
	/**
	* Access the description property as formatted text (cover for PROP_DESCRIPTION).
	* @return The event's description property.
	*/
	public String getDescriptionFormatted();
	
	/**
	* Access the type (cover for PROP_CALENDAR_TYPE).
	* @return The event's type property.
	*/
	public String getType();
	
	/**
	* Access the location property (cover for PROP_CALENDAR_LOCATION).
	* @return The event's location property.
	*/
	public String getLocation();	

    /**
	* Get the value of an "extra" event field.
	* @param name The name of the field.
	* @return the value of the "extra" event field.
	*/
	public String getField(String name);

	/**
	* Gets the containing calendar's reference.
	* @return The containing calendar reference.
	*/
	public String getCalendarReference();

	/**
	* Gets the recurrence rule, if any.
	* @return The recurrence rule, or null if none.
	*/
	public RecurrenceRule getRecurrenceRule();

}	// CalendarEvent



