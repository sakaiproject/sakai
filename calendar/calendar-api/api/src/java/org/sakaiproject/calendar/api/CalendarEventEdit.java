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

import org.sakaiproject.entity.api.AttachmentContainerEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.exception.PermissionException;

/**
* <p>CalendarEventEdit is an editable CalendarEvent</p>
*/
public interface CalendarEventEdit
	extends CalendarEvent, Edit, AttachmentContainerEdit
{
	/**
	* Replace the time range
	* @param The new event time range
	*/
	public void setRange(TimeRange range);

	/**
	* Set the display name property (cover for PROP_DISPLAY_NAME).
	* @param name The event's display name property.
	*/
	public void setDisplayName(String name);

	/**
	* Set the description property as plain text (cover for PROP_DESCRIPTION).
	* @param description The event's description property.
	*/
	public void setDescription(String description);
	
	/**
	* Set the description property as formatted text (cover for PROP_DESCRIPTION).
	* @param description The event's description property.
	*/
	public void setDescriptionFormatted(String description);
	
	/**
	* Set the type (cover for PROP_CALENDAR_TYPE).
	* @param type The event's type property.
	*/
	public void setType(String type);
	
	/**
	* Set the location (cover for PROP_CALENDAR_LOCATION).
	* @param location The event's location property.
	*/
	public void setLocation(String location);

	/**
	* Set the value of an "extra" event field.
	* @param name The "extra" field name
	* @param value The value to set, or null to remove the field.
	*/
	public void setField(String name, String value);

	/**
	* Sets the recurrence rule.
	* @param rule The recurrence rule, or null to clear out the rule.
	*/
	public void setRecurrenceRule(RecurrenceRule rule);
	
	/**
	 * Add a Group to the list of groups for this event.
	 * 
	 * @param group
	 *        The Group to add to those for this event.
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void addGroup(Group group) throws PermissionException;

	/**
	 * Remove this Group from the list of groups for this event.
	 * 
	 * @param group
	 *        The Group to remove from those for this event.
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void removeGroup(Group group) throws PermissionException;

	/**
	 * Set the access mode for the event - how we compute who has access to the event.
	 * 
	 * @param access
	 *        The EventAccess access mode for the event.
	 */
	void setAccess(EventAccess access);

}	// CalendarEventEdit



