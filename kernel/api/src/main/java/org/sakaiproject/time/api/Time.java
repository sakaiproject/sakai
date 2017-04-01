/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.time.api;

import java.io.Serializable;

/**
 * <p>
 * Time ...
 * </p>
 * @deprecated the use of time is discouraged in favour of {@link java.time.Instant}. This interface will be removed in 2.0
 */
public interface Time extends Cloneable, Comparable, Serializable
{
	/**
	 * Format as a string, GMT, for a SQL statement.
	 * 
	 * @return Time in string format.
	 */
	String toStringSql();

	/**
	 * Format as a string, Local time zone.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocal();

	/**
	 * Format as a string, Human Readable, full format, GMT.
	 * 
	 * @return Time in string format.
	 */
	String toStringGmtFull();

	/**
	 * Format as a string, Human Readable, full format, Local.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalFull();

	/**
	 * Format as a string, Human Readable, full format, Local, with zone.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalFullZ();

	/**
	 * Format as a string, Human Readable, short (time only) format, GMT.
	 * 
	 * @return Time in string format.
	 */
	String toStringGmtShort();

	/**
	 * Format as a string, Human Readable, short (time only) format, Local.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalShort();

	/**
	 * Format as a string, Human Readable, time only format, GMT.
	 * 
	 * @return Time in string format.
	 */
	String toStringGmtTime();

	/**
	 * Format as a string, Human Readable, time only format, Local.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalTime();

	/**
	 * Format as a string, Human Readable, time only format, 24hour Local.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalTime24();

	/**
	 * Format as a string, Human Readable, time only format, Local, with zone.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalTimeZ();

	/**
	 * Format as a string, Human Readable, date only format, GMT.
	 * 
	 * @return Time in string format.
	 */
	String toStringGmtDate();

	/**
	 * Format as a string, Human Readable, date only format, Local.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalDate();

	/**
	 * Format as a string, short format: MM/DD/YY, Local.
	 * 
	 * @return Time in string format.
	 */
	String toStringLocalShortDate();

	/**
	 * Format as a string, RFC822 format: 
	 *   Sun, 14 Aug 2005 16:13:03 UTC.
         *
	 * http://www.w3.org/Protocols/rfc822/
	 * 
	 * @return Time in string format per RFC822.
	 */
	String toStringRFC822Local();

	/**
	 * Format as a file path based on the date and time.
	 * 
	 * @return Time is string format.
	 */
	String toStringFilePath();

	/**
	 * Set the time in milliseconds since.
	 * 
	 * @param value
	 *        The milliseconds since value for the time.
	 */
	void setTime(long value);

	/**
	 * Access the milliseconds since.
	 * 
	 * @return The milliseconds since value.
	 */
	long getTime();

	/**
	 * Is this time before the other time?
	 * 
	 * @param other
	 *        The other time for the comparison.
	 * @return true if this time is before the other, false if not.
	 */
	boolean before(Time other);

	/**
	 * Is this time after the other time?
	 * 
	 * @param other
	 *        The other time for the comparison.
	 * @return true if this time is after the other, false if not.
	 */
	boolean after(Time other);

	/**
	 * Make a clone.
	 * 
	 * @return The clone.
	 */
	Object clone();

	/**
	 * Access the time value as a TimeBreakdown object, in GMT
	 * 
	 * @return A TimeBreakdown object representing this time's value in GMT
	 */
	TimeBreakdown breakdownGmt();

	/**
	 * Access the time value as a TimeBreakdown object, in Local
	 * 
	 * @return A TimeBreakdown object representing this time's value in GMT
	 */
	TimeBreakdown breakdownLocal();

	/**
	 * Access the time in a common human readable display format
	 * 
	 * @return The time string in human readable format.
	 */
	String getDisplay();
}
