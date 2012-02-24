/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.cheftool.api;

/**
 * <p>
 * Alert is a interface for a set of messages intended for user display in the user interface.
 * </p>
 */
public interface Alert
{
	/**
	 * Add a new alert line. A line separator will be appended as needed.
	 * 
	 * @param alert
	 *        The alert message to add.
	 */
	void add(String alert);

	/**
	 * Access the alert message. Once accessed, the message is cleared.
	 * 
	 * @return The alert message.
	 */
	String getAlert();

	/**
	 * Access the alert message, but unlike getAlert(), do not clear the message.
	 * 
	 * @return The alert message.
	 */
	String peekAlert();

	/**
	 * Check to see if the alert is empty, or has been populated.
	 * 
	 * @return true of the alert is empty, false if there have been alerts set.
	 */
	boolean isEmpty();

	/**
	 * Remove any messages in the Alert.
	 */
	void clear();
}
