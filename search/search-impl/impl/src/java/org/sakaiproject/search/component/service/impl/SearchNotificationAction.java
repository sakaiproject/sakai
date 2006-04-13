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
import org.sakaiproject.search.SearchIndexBuilder;
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
