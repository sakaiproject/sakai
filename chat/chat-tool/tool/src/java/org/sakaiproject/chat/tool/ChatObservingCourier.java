/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2003, 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.chat.tool;

import java.util.Observable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.courier.cover.CourierService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.util.EventObservingCourier;

/**
 * <p>
 * ChatObservingCourier is an ObservingCourier that watches chat events and delivers them with extra information, specifically the reference to the message referenced by the event.
 * </p>
 */
public class ChatObservingCourier extends EventObservingCourier
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ChatObservingCourier.class);

	protected boolean m_alertEnabled;

	/**
	 * Construct.
	 * 
	 * @param deliveryId
	 *        The key identifying the Portal Page Instance.
	 * @param elementId
	 *        The key identifying the element on the Portal Page that would need a courier delivered message when things change.
	 * @param The
	 *        event resource pattern - we watch for only events whose ref start with this.
	 */
	public ChatObservingCourier(String deliveryId, String elementId, String resourcePattern, boolean wantsBeeps)
	{
		super(deliveryId, elementId, resourcePattern);
		m_alertEnabled = wantsBeeps;

	} // ChatObservingCourier

	/**
	 * This method is called whenever the observed object is changed. An application calls an <tt>Observable</tt> object's <code>notifyObservers</code> method to have all the object's observers notified of the change. default implementation is to
	 * cause the courier service to deliver to the interface controlled by my controller. Extensions can override.
	 * 
	 * @param o
	 *        the observable object.
	 * @param arg
	 *        an argument passed to the <code>notifyObservers</code> method.
	 */
	public void update(Observable o, Object arg)
	{
		// ignore changes when not enabled
		if (!getEnabled())
		{
			if (M_log.isDebugEnabled()) M_log.debug("update [DISABLED]: " + ((arg == null) ? "null" : arg.toString()));
			return;
		}

		if (!check(arg)) return;

		CourierService.deliver(new ChatDelivery(getDeliveryId(), getElementId(), ((Event) arg).getResource(), m_alertEnabled));

	} // update

	public void alertEnabled(boolean newVal)
	{
		m_alertEnabled = newVal;

	} // alertEnabled
}
