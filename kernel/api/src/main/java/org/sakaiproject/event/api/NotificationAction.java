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

package org.sakaiproject.event.api;

import org.w3c.dom.Element;

/**
 * <p>
 * NotificationAction is the helper class that does the notify() for a notification.
 * </p>
 */
public interface NotificationAction
{
	/**
	 * Set from an xml element.
	 * 
	 * @param el
	 *        The xml element.
	 */
	void set(Element el);

	/**
	 * Set from another.
	 * 
	 * @param other
	 *        The other to copy.
	 */
	void set(NotificationAction other);

	/**
	 * Make a new one like me.
	 * 
	 * @return A new action just like me.
	 */
	NotificationAction getClone();

	/**
	 * Fill this xml element with the attributes.
	 * 
	 * @param el
	 *        The xml element.
	 */
	void toXml(Element el);

	/**
	 * Do the notification.
	 * 
	 * @param notification
	 *        The notification responding to the event.
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 */
	void notify(Notification notification, Event event);
}
