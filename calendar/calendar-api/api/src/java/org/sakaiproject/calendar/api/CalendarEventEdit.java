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

import org.sakaiproject.entity.api.AttachmentContainerEdit;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.TimeRange;

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
	 * Sets the exclusion recurrence rule.
	 * 
	 * @param rule
	 *        The recurrence rule, or null to clear out the rule.
	 */
	public void setExclusionRule(RecurrenceRule rule);
		
	/**
	 * Set these as the event's groups, replacing the access and groups already defined.
	 * 
	 * @param Collection
	 *        groups The colelction of Group objects to use for this event.
    * @param own
    *        boolean flag true if setting access for own event
	 * @throws PermissionException
	 *         if the end user does not have permission to remove from the groups that would be removed or add to the groups that would be added.
	 */
	void setGroupAccess(Collection groups, boolean own) throws PermissionException;

	/**
	 * Remove any grouping for this event; the access mode reverts to site and any groups are removed.
	 * 
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void clearGroupAccess() throws PermissionException;

	/**
	* Set the event creator (cover for PROP_CREATOR) to current user
	*/
	public void setCreator();
	
	/**
	* Set the event modifier (cover for PROP_MODIFIED_BY) to current user
	*/
	public void setModifiedBy();
	
}	// CalendarEventEdit



