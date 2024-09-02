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

package org.sakaiproject.calendar.impl;

import java.util.GregorianCalendar;
import java.util.Stack;

import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* <p>DailyRecurrenceRule is a time range generating rule that is based on a daily recurrence.</p>
*/
public class DailyRecurrenceRule extends RecurrenceRuleBase
{
	/** The unique type / short frequency description. */
	public static final String FREQ = "day";

	/**
	* Construct.
	*/
	public DailyRecurrenceRule()
	{
		super();
	}	// DailyRecurrenceRule

	/**
	* Construct with no  limits.
	* @param interval Every this many number of days: 1 would be daily.
	*/
	public DailyRecurrenceRule(int interval)
	{
		super(interval);
	}	// DailyRecurrenceRule

	/**
	* Construct with count limit.
	* @param interval Every this many number of days: 1 would be daily.
	* @param count For this many occurrences - if 0, does not limit.
	*/
	public DailyRecurrenceRule(int interval, int count)
	{
		super(interval, count);
	}	// DailyRecurrenceRule

	/**
	* Construct with time limit.
	* @param interval Every this many number of days: 1 would be daily.
	* @param until No time ranges past this time are generated - if null, does not limit.
	*/
	public DailyRecurrenceRule(int interval, Time until)
	{
		super(interval, until);
	}	// DailyRecurrenceRule

	/**
	* Serialize the resource into XML, adding an element to the doc under the top of the stack element.
	* @param doc The DOM doc to contain the XML (or null for a string return).
	* @param stack The DOM elements, the top of which is the containing element of the new "resource" element.
	* @return The newly added element.
	*/
	public Element toXml(Document doc, Stack stack)
	{
		// add the "rule" element to the stack'ed element
		Element rule = doc.createElement("rule");
		((Element)stack.peek()).appendChild(rule);

		// set the class name - old style for CHEF 1.2.10 compatibility
		rule.setAttribute("class", "org.chefproject.osid.calendar.DailyRecurrenceRule");

		// set the rule class name w/o package, for modern usage
		rule.setAttribute("name", "DailyRecurrenceRule");

		// Do the base class part.
		setBaseClassXML(rule);

		return rule;

	}	// toXml


	/* (non-Javadoc)
	 * @see org.chefproject.service.calendar.RecurrenceRuleBase#getRecurrenceType()
	 */
	protected int getRecurrenceType()
	{
		return GregorianCalendar.DAY_OF_MONTH;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFrequencyDescription()
	{
		return rb.getString("set.days");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFrequency()
	{
		return FREQ;
	}

}	



