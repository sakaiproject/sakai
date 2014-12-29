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

import java.util.List;

/**
 * <p>
 * CourierService is the Interface for a Sakai service which can be used to push messages from the Sakai server components to the user interface in the browser.<br />
 * It is used mostly to cause a tool in a particular portal instance to be refreshed to respond to a change noticed at the server.
 * </p>
 * <p>
 * An Address identifies a particular client's window: it merges the Usage session, the window's portal page location, and perhaps the tool id (for floating tool windows).
 * </p>
 * <p>
 * A Delivery object captures the Address, the HTML Element Id involved, and any other details of a particular type of delivery.
 * </p>
 * 
 * @deprecated The CourierService is no longer a preferred way to communicate with clients.
 * 		It will be removed in a future release of Sakai (after 10.0)
 * 		There are better technologies to use, please do not use.
 *      <a href="https://jira.sakaiproject.org/browse/SAK-22053">SAK-22053</a>
 */
@Deprecated
public interface CourierService
{
	/** This string can be used to find the service in the service manager. */
	static final String SERVICE_NAME = CourierService.class.getName();

	/**
	 * Queue up a delivery for the client window identified in the Delivery object. The particular form of delivery is determined by the type of Delivery object sent.
	 * 
	 * @param delivery
	 *        The Delivery (or extension) object to deliver.
	 */
	void deliver(Delivery delivery);

	/**
	 * Clear any pending delivery requests to the particular client window for this element.
	 * 
	 * @param address
	 *        The address of the client window.
	 * @param elementId
	 *        The id of the html element.
	 */
	void clear(String address, String elementId);

	/**
	 * Clear any pending delivery requests to this session client window.
	 * 
	 * @param address
	 *        The address of client window.
	 */
	void clear(String address);

	/**
	 * Access and de-queue the Deliveries queued up for a particular session client window.
	 * 
	 * @param address
	 *        The address of client window.
	 * @return a List of Delivery objects addressed to this session client window.
	 */
	List getDeliveries(String address);

	/**
	 * Check to see if there are any deliveries queued up for a particular session client window.
	 * 
	 * @param address
	 *        The address of the client window.
	 * @return true if there are deliveries for this client window, false if not.
	 */
	boolean hasDeliveries(String address);
	
	/**
	 * Access a list of DeliveryProviders registered with the CourierService. 
	 * @return A list of DeliveryProviders or null, if no DeliveryProviders 
	 * 	are registered with the CourierService
	 */
	List<DeliveryProvider> getDeliveryProviders();
	
	/**
	 * Register a DeliveryProvider with the CourierService.
	 * @param provider
	 */
	void registerDeliveryProvider(DeliveryProvider provider);
}
