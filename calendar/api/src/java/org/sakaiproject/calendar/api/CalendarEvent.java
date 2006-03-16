/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

// package
package org.sakaiproject.calendar.api;

// import
import org.sakaiproject.service.legacy.entity.AttachmentContainer;
import org.sakaiproject.service.legacy.entity.Entity;
import org.sakaiproject.service.legacy.time.TimeRange;

/**
* <p>CalendarEvent is the interface for events placed into a CHEF Calendar Service Calendar.</p>
* <p>Each event has a time range, and other information in the event's (Resource) properties.</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
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



