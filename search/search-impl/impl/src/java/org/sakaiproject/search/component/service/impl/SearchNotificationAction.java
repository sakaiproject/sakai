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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.search.api.SearchIndexBuilder;
import org.w3c.dom.Element;

/**
 * This action pushes the Event and Notification to the SearchIndexBuilder That
 * manages the update of the search indexes.
 * 
 * @author ieb
 */
public class SearchNotificationAction implements NotificationAction
{

	private static Log dlog = LogFactory.getLog(SearchNotificationAction.class);

	private SearchIndexBuilder searchIndexBuilder;

	public SearchNotificationAction()
	{
		dlog.debug("Constructor()");
	}

	public SearchNotificationAction(SearchIndexBuilder searchIndexBuilder)
	{
		dlog.debug("Constructory()" + searchIndexBuilder);
		this.searchIndexBuilder = searchIndexBuilder;
	}

	/**
	 * @{inheritDoc}
	 */
	public void set(Element arg0)
	{
		dlog.debug("set element");
		// copy the element contents to this action

	}

	/**
	 * @{inheritDoc}
	 */
	public void set(NotificationAction arg0)
	{
		dlog.debug("set action");
		// copy the notifiation action to this notification action

	}

	/**
	 * @{inheritDoc}
	 */
	public NotificationAction getClone()
	{
		dlog.debug("Clone");
		SearchNotificationAction clone = new SearchNotificationAction(
				searchIndexBuilder);
		clone.set(this);

		return clone;
	}

	/**
	 * @{inheritDoc}
	 */
	public void toXml(Element arg0)
	{
		// Serialise to XML

	}

	/**
	 * The notify operation will come in with an event that we are registerd to
	 * recieve, and notification.
	 * 
	 * @{inheritDoc}
	 */
	public void notify(Notification notification, Event event)
	{
		dlog.debug("Notify " + event.getEvent());
		// This is done so that we can persist the Actions if we want without
		// having to keep a reference to the SearchIndexBuilder
		searchIndexBuilder.addResource(notification, event);
	}

}
