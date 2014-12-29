/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

/**
 * <p>
 * TimeRange ...
 * </p>
 */
public interface TimeRange extends Cloneable
{
	/**
	 * Check if this Time in my range.
	 * 
	 * @param time
	 *        The time to check for inclusion.
	 * @return true if the time is in the range, false if not.
	 */
	boolean contains(Time time);

	/**
	 * Check if this TimeRange overlap this other TimeRange at all.
	 * 
	 * @param range
	 *        The time range to check for overlap.
	 * @return true if any time in the other range is in this range, false if not.
	 */
	boolean overlaps(TimeRange range);

	/**
	 * Check if this TimeRange completely contains the other range.
	 * 
	 * @param range
	 *        The time range to check for containment.
	 * @return true if the other TimeRange is within this TimeRange, false if not.
	 */
	boolean contains(TimeRange range);

	/**
	 * Access the first Time of the range.
	 * 
	 * @return the first Time actually in the range.
	 */
	Time firstTime();

	/**
	 * Access the last Time in the range.
	 * 
	 * @return the last Time actually in the range.
	 */
	Time lastTime();

	/**
	 * Access the first Time of the range (fudged).
	 * 
	 * @param fudge
	 *        How many ms to increase if the first is not included.
	 * @return the first Time actually in the range (fudged).
	 */
	Time firstTime(long fudge);

	/**
	 * Access the last Time of the range (fudged).
	 * 
	 * @param fudge
	 *        How many ms to decrease if the last is not included.
	 * @return the first Time actually in the range (fudged).
	 */
	Time lastTime(long fudge);

	/**
	 * Format the TimeRange - human readable.
	 * 
	 * @return The TimeRange in string format.
	 */
	String toStringHR();

	/**
	 * Access the duration of the TimeRange, in milliseconds.
	 * 
	 * @return The duration.
	 */
	long duration();

	/**
	 * Shift the time range back an interval
	 * 
	 * @param i
	 *        time intervel in ms
	 */
	void shiftBackward(long i);

	/**
	 * Shift the time range forward an interval
	 * 
	 * @param i
	 *        time intervel in ms
	 */
	void shiftForward(long i);

	/**
	 * Enlarge or shrink the time range by multiplying a zooming factor.
	 * 
	 * @param f
	 *        zooming factor
	 */
	void zoom(double f);

	/**
	 * Adjust this time range based on the difference between the origRange and the modRange, if any.
	 * 
	 * @param original
	 *        the original time range.
	 * @param modified
	 *        the modified time range.
	 */
	void adjust(TimeRange original, TimeRange modified);

	/**
	 * Clone the TimeRange
	 * 
	 * @return A cloned TimeRange.
	 */
	Object clone();

	/**
	 * Check if the TimeRange is really just a single Time.
	 * 
	 * @return true if the tiTimeRange is a single Time, false if it is not.
	 */
	boolean isSingleTime();
}
