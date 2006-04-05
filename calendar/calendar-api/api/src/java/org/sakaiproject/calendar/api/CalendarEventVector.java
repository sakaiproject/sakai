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
import java.util.Iterator;
import java.util.Vector;

import org.sakaiproject.service.legacy.time.TimeRange;

/**
* <p>CalendarEventVector is a helper class for the Calendar service.  It will read in
* a bunch of CalendarEvents from an iterator, place them into a vector, and provide
* time range controlled access to the events.</p>
* <p>Use this to make larger, more user action aligned service requests, but still have fine
* grained time range access to the events returned.</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
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



