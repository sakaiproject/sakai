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
 * TimeBreakdown ...
 * </p>
 * @deprecated the use of time is discouraged in favour of {@link java.time.ZonedDateTime}. This interface will be removed in 2.0
 */
public interface TimeBreakdown
{
	/**
	 * Access the year value.
	 * 
	 * @return The year value.
	 */
	int getYear();

	/**
	 * Set the year value.
	 * 
	 * @param year
	 *        The year value.
	 */
	void setYear(int year);

	/**
	 * Access the month value.
	 * 
	 * @return The month value.
	 */
	int getMonth();

	/**
	 * Set the month value.
	 * 
	 * @param month
	 *        The year value.
	 */
	void setMonth(int month);

	/**
	 * Access the day value.
	 * 
	 * @return The day value.
	 */
	int getDay();

	/**
	 * Set the day value.
	 * 
	 * @param day
	 *        The year value.
	 */
	void setDay(int day);

	/**
	 * Access the hour value.
	 * 
	 * @return The hour value.
	 */
	int getHour();

	/**
	 * Set the hour value.
	 * 
	 * @param hour
	 *        The year value.
	 */
	void setHour(int hour);

	/**
	 * Access the minute value.
	 * 
	 * @return The minute. value.
	 */
	int getMin();

	/**
	 * Set the minute value.
	 * 
	 * @param minute
	 *        The year value.
	 */
	void setMin(int minute);

	/**
	 * Access the second value.
	 * 
	 * @return The second value.
	 */
	int getSec();

	/**
	 * Set the second value.
	 * 
	 * @param second
	 *        The year value.
	 */
	void setSec(int second);

	/**
	 * Access the millisecond value.
	 * 
	 * @return The millisecond value.
	 */
	int getMs();

	/**
	 * Set the millisecond value.
	 * 
	 * @param millisecond
	 *        The year value.
	 */
	void setMs(int millisecond);
}
