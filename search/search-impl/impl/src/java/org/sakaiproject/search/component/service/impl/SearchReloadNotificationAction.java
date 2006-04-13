/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2006 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.search.component.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.search.SearchService;
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
