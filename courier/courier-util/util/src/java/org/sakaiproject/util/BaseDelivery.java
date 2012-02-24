/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.util;

import org.sakaiproject.courier.api.Delivery;

/**
 * <p>
 * BaseDelivery is a base class for all Delivery objects.
 * </p>
 */
public class BaseDelivery implements Delivery
{
	/** The address. */
	protected String m_address = null;

	/** The elementId. */
	protected String m_elementId = null;

	/**
	 * Construct.
	 * 
	 * @param address
	 *        The address.
	 * @param elementId
	 *        The elementId.
	 */
	public BaseDelivery(String address, String elementId)
	{
		m_address = address;
		m_elementId = elementId;
	}

	/**
	 * Set the delivery address.
	 * 
	 * @param address
	 *        The delivery address.
	 */
	public void setAddress(String address)
	{
		m_address = address;
	}

	/**
	 * Access the delivery address.
	 * 
	 * @return The delivery address.
	 */
	public String getAddress()
	{
		return m_address;
	}

	/**
	 * Set the HTML Element Id that this delivery is in reference to.
	 * 
	 * @param id
	 *        The HTML Element Id that this delivery is in reference to.
	 */
	public void setElement(String id)
	{
		m_elementId = id;
	}

	/**
	 * Access the HTML Element Id that this delivery is in reference to.
	 * 
	 * @return The HTML Element Id that this delivery is in reference to.
	 */
	public String getElement()
	{
		return m_elementId;
	}

	/**
	 * Perform any pre-delivery actions. Note: this is run in the same usage session as is being delivered to.
	 */
	public void act()
	{
		// do nothing...
	}

	/**
	 * Compose a javascript message for delivery to the browser client window.
	 * 
	 * @return The javascript message to send to the browser client window.
	 */
	public String compose()
	{
		return "";
	}

	/**
	 * Display.
	 */
	public String toString()
	{
		return businessKey();
	}

	/**
	 * Are these the same?
	 * 
	 * @return true if obj is the same Delivery as this one.
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof BaseDelivery)) return false;

		BaseDelivery bob = (BaseDelivery) obj;
		return businessKey().equals(bob.businessKey());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.businessKey().hashCode();
	}
	
	/**
	 * Useful for equals and hashCode method implementations.
	 * @return
	 */
	private String businessKey()
	{
		return m_address + ":" + m_elementId;
	}
}
