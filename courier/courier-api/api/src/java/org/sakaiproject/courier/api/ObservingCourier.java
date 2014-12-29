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

package org.sakaiproject.courier.api;

import java.util.Observer;

/**
 * <p>
 * ObservingCourier is an observer which uses the courier service to notify when things change.
 * </p>
 */
public interface ObservingCourier extends Observer
{
	/**
	 * Check to see if we want to process or ignore this update.
	 * 
	 * @param arg
	 *        The arg from the update.
	 * @return true to continue, false to quit.
	 */
	boolean check(Object arg);

	/**
	 * Disable.
	 */
	void disable();

	/**
	 * Enable.
	 */
	void enable();

	/**
	 * @return the delivery id.
	 */
	String getDeliveryId();

	/**
	 * @return The element id.
	 */
	String getElementId();

	/**
	 * @return the enabled status.
	 */
	boolean getEnabled();

	/**
	 * @return the location this observer is observing.
	 */
	String getLocation();

	/**
	 * Accept notification that the element has just been delivered. If there are pending requests to deliver, they can be cleared.
	 */
	void justDelivered();

	/**
	 * Set the delivery id.
	 * 
	 * @param id
	 *        The delivery id.
	 */
	void setDeliveryId(String id);

	/**
	 * Set the element id.
	 * 
	 * @param id
	 *        the element id.
	 */
	void setElementId(String id);
}
