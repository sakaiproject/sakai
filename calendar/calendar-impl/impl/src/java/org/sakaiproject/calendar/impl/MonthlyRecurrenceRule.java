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

package org.sakaiproject.calendar.impl;

import java.util.GregorianCalendar;
import java.util.Stack;

import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* <p>MonthlyRecurrenceRule is a time range generating rule that is based on a monthly recurrence.</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class MonthlyRecurrenceRule extends RecurrenceRuleBase
{
	/** The unique type / short frequency description. */
	protected final static String FREQ = "month";

	/**
	 * Default constructor
	 */
	public MonthlyRecurrenceRule()
	{
		super();
	}	// MonthlyRecurrenceRule
	
	/**
	* Construct with no  limits.
	* @param interval Every this many number of months: 1 would be every month.
	*/
	public MonthlyRecurrenceRule(int interval)
	{
		super(interval);

	}	// MonthlyRecurrenceRule

	/**
	* Construct with count limit.
	* @param interval Every this many number of months: 1 would be every month.
	* @param count For this many occurrences - if 0, does not limit.
	*/
	public MonthlyRecurrenceRule(int interval, int count)
	{
		super(interval, count);
	}	// MonthlyRecurrenceRule

	/**
	* Construct with time limit.
	* @param interval Every this many number of months: 1 would be every month.
	* @param until No time ranges past this time are generated - if null, does not limit.
	*/
	public MonthlyRecurrenceRule(int interval, Time until)
	{
		super(interval, until);
	}	// MonthlyRecurrenceRule
	

	/* (non-Javadoc)
	 * @see org.chefproject.service.calendar.RecurrenceRule#toXml(org.w3c.dom.Document, java.util.Stack)
	 */
	public Element toXml(Document doc, Stack stack)
	{
		// add the "rule" element to the stack'ed element
		Element rule = doc.createElement("rule");
		((Element)stack.peek()).appendChild(rule);

		// set the class name - old style for CHEF 1.2.10 compatibility
		rule.setAttribute("class", "org.chefproject.osid.calendar.MonthlyRecurrenceRule");

		// set the rule class name w/o package, for modern usage
		rule.setAttribute("name", "MonthlyRecurrenceRule");
		
		// Do the base class part.
		setBaseClassXML(rule);

		return rule;
	}


	/* (non-Javadoc)
	 * @see org.chefproject.service.calendar.RecurrenceRuleBase#getRecurrenceType()
	 */
	protected int getRecurrenceType()
	{
		return GregorianCalendar.MONTH;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getFrequencyDescription()
	{
		return FREQ;
	}
}



