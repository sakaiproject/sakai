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

import java.util.Iterator;
import java.util.Vector;

import org.sakaiproject.time.api.TimeRange;

/**
* <p>CalendarEventVector is a helper class for the Calendar service.  It will read in
* a bunch of CalendarEvents from an iterator, place them into a vector, and provide
* time range controlled access to the events.</p>
* <p>Use this to make larger, more user action aligned service requests, but still have fine
* grained time range access to the events returned.</p>
*/
public class CalendarEventVector
	extends Vector
{
	/**
	* Construct empty.
	*/
	public CalendarEventVector()
	{
		super();
	}

	/**
	* Construct
	* @param events An interator on CalendarEvents to load into the vector.
	*/
	public CalendarEventVector(Iterator events)
	{
		super();

		while (events.hasNext())
		{
			add(events.next());
		}

	}	// CalendarEventVector

	/**
	* Return an iterator on events in the CalendarEventVector.
	* The order in which the events will be found in the iteration is by event start date.
	* @param range A time range to limit the iterated events.  May be null; all events will be returned.
	* @return an iterator on CalendarEvent objects in the CalendarEventVector (may be empty).
	*/
	public Iterator getEvents(TimeRange range)
	{
		// pull the range of events from vector
		Vector events = new Vector();
		Iterator it = iterator();
		while (it.hasNext())
		{
			CalendarEvent test = (CalendarEvent)it.next();
			if (range.overlaps(test.getRange()))
			{
				events.add(test);
			}
			// %%% if test is past range, we can stop now...
		}

		return events.iterator();

	}	// getEvents

}	// CalendarEventVector



