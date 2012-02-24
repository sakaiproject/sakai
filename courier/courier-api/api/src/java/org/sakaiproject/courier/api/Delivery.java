/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

/**
 * <p>
 * Delivery is the core interface for things sent to the Courier service that represent various sorts of deliveries to the client windows.
 * </p>
 * <p>
 * Address is a client window address.
 * </p>
 */
public interface Delivery
{
	/**
	 * Set the delivery address.
	 * 
	 * @param address
	 *        The delivery address.
	 */
	void setAddress(String address);

	/**
	 * Access the delivery address.
	 * 
	 * @return The delivery address.
	 */
	String getAddress();

	/**
	 * Set the HTML Element Id that this delivery is in reference to.
	 * 
	 * @param id
	 *        The HTML Element Id that this delivery is in reference to.
	 */
	void setElement(String id);

	/**
	 * Access the HTML Element Id that this delivery is in reference to.
	 * 
	 * @return The HTML Element Id that this delivery is in reference to.
	 */
	String getElement();

	/**
	 * Perform any pre-delivery actions. Note: this is run in the same usage session as is being delivered to.
	 */
	void act();

	/**
	 * Compose a javascript message for delivery to the browser client window.
	 * 
	 * @return The javascript message to send to the browser client window.
	 */
	String compose();
}

