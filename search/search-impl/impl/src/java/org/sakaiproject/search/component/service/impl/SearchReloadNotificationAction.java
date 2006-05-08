/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
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
import org.sakaiproject.search.api.SearchService;
import org.w3c.dom.Element;

/**
 * @author ieb
 */
public class SearchReloadNotificationAction implements NotificationAction
{

	private static Log dlog = LogFactory
			.getLog(SearchReloadNotificationAction.class);

	private SearchService searchService;

	private SearchReloadNotificationAction()
	{
		dlog.debug("Constructor()");

	}

	public SearchReloadNotificationAction(SearchService searchService)
	{
		dlog.debug("Constructor() " + searchService);
		this.searchService = searchService;

	}

	/**
	 * @{inheritDoc}
	 */
	public void set(Element arg0)
	{
		dlog.debug("set Element");

	}

	/**
	 * @{inheritDoc}
	 */
	public void set(NotificationAction arg0)
	{
		dlog.debug("set Action");

	}

	/**
	 * @{inheritDoc}
	 */
	public NotificationAction getClone()
	{
		dlog.debug("Clone");

		SearchReloadNotificationAction clone = new SearchReloadNotificationAction(
				searchService);
		clone.set(this);
		return clone;
	}

	/**
	 * @{inheritDoc}
	 */
	public void toXml(Element arg0)
	{
	}

	/**
	 * @{inheritDoc}
	 */
	public void notify(Notification arg0, Event event)
	{
		dlog.debug("notify");

		if (!event.getEvent().equals(SearchService.EVENT_TRIGGER_INDEX_RELOAD))
		{
			return;
		}
		// this is done so that if we want to persist the events, we can do so
		// without
		// being forced to keep a reference to the SearchService
		searchService.reload();
	}

}
