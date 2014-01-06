/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.calendar.api;

import java.util.Collection;

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
	
	/**
	 * Gets the exclusion recurrence rule, if any.
	 * 
	 * @return The exclusionrecurrence rule, or null if none.
	 */
	public RecurrenceRule getExclusionRule();
		 
	/**
	* Gets the event creator (userid), if any (cover for PROP_CREATOR).
	* @return The event's creator property.
	*/
	public String getCreator();	

	/**
	* Returns true if current user is thhe event's owner/creator
	* @return boolean true or false
	*/
	public boolean isUserOwner();	

	/**
	* Gets the event modifier (userid), if any (cover for PROP_MODIFIED_BY).
	* @return The event's modified-by property.
	*/
	public String getModifiedBy();	


	/**
	* Gets the event's site name
	* @return The event's site name
	*/
	public String getSiteName();	

	/**
	 * <p>
	 * EventAccess enumerates different access modes for the event: site-wide or grouped.
	 * </p>
	 */
	public class EventAccess
	{
		private final String m_id;

		private EventAccess(String id)
		{
			m_id = id;
		}

		public String toString()
		{
			return m_id;
		}

		static public EventAccess fromString(String access)
		{
			// if (PUBLIC.m_id.equals(access)) return PUBLIC;
			if (SITE.m_id.equals(access)) return SITE;
			if (GROUPED.m_id.equals(access)) return GROUPED;
			return null;
		}

		/** public access to the event: pubview */
		// public static final EventAccess PUBLIC = new EventAccess("public");

		/** site level access to the event */
		public static final EventAccess SITE = new EventAccess("site");

		/** grouped access; only members of the getGroup() groups (authorization groups) have access */
		public static final EventAccess GROUPED = new EventAccess("grouped");
	}
	
	/**
	 * Access the groups defined for this event.
	 * 
	 * @return A Collection (String) of group refs (authorization group ids) defined for this event; empty if none are defined.
	 */
	Collection getGroups();
	
	/**
	 * Access the groups defined for this event, as Group objects.
	 * 
	 * @return A Collection (Group) of group objects defined for this event; empty if none are defined.
	 */
	Collection getGroupObjects();

	/**
	 * Access the groups defined for this .
	 * 
	 * @return A Collection (String) of group refs (authorization group ids) defined for this event; empty if none are defined.
	 */
	String getGroupRangeForDisplay(Calendar calendar);

	/**
	 * Access the access mode for the event - how we compute who has access to the event.
	 * 
	 * @return The EventAccess access mode for the event.
	 */
	EventAccess getAccess();

}	// CalendarEvent



