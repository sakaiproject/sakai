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
package org.sakaiproject.calendar.impl;

// import
import org.sakaiproject.service.legacy.time.TimeRange;

/**
* <p>RecurrenceInstance is one instance of a recurrence sequence, with the time range and sequence # of the instance.</p>
*
* @author University of Michigan, CHEF Software Development Team
* @version $Revision$
*/
public class RecurrenceInstance
{
	/** The time range of the instance. */
	protected TimeRange m_range = null;

	/** The sequence number (1 based) of the instance. */
	protected Integer m_sequence = null;

	/**
	* Construct.
	*/
	public RecurrenceInstance(TimeRange range, int sequence)
	{
		m_range = range;
		m_sequence = new Integer(sequence);

	}	// RecurrenceInstance

	/**
	* Access the time range.
	* @return the TimeRange.
	*/
	public TimeRange getRange()
	{
		return m_range;

	}	// getRange

	/**
	* Access the sequence number.
	* @return the sequence number.
	*/
	public Integer getSequence()
	{
		return m_sequence;

	}	// getSequence

}	// RecurrenceInstance



