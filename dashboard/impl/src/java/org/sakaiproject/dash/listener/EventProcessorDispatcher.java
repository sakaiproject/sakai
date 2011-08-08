/**********************************************************************************
* $URL:  $
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
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

package org.sakaiproject.dash.listener;

// imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.site.api.Site;


import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.component.cover.ComponentManager;

/**
* <p>The is an event watcher and serves also as dispatcher for event listenors</p>
* 
* @author 
* @version $
*/
public class EventProcessorDispatcher implements Observer
{
	
	private static Log log = LogFactory.getLog(EventProcessorDispatcher.class);
	
	/*******************************************************************************
	* Dependencies and their setter methods
	*******************************************************************************/

	/** Dependency: event tracking service */
	protected EventTrackingService m_eventTrackingService = null;

	/**
	 * Dependency: event tracking service.
	 * @param service The event tracking service.
	 */
	public void setEventTrackingService(EventTrackingService service)
	{
		m_eventTrackingService = service;
	}
	
	protected SakaiProxy sakaiProxy;
	
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	AssignmentNewEventProcessor assnProcessor;
	AnnouncementEventProcessor annProcessor;
	ContentNewEventProcessor contProcessor;

	/*******************************************************************************
	* Init and Destroy
	*******************************************************************************/
	
	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			log.warn(this);
			log.warn(m_eventTrackingService);
			// start watching the events - only those generated on this server, not those from elsewhere
			m_eventTrackingService.addLocalObserver(this);
			
			annProcessor = (org.sakaiproject.dash.listener.AnnouncementEventProcessor) ComponentManager.get("org.sakaiproject.dash.listener.AnnouncementEventProcessor");
			assnProcessor = (org.sakaiproject.dash.listener.AssignmentNewEventProcessor) ComponentManager.get("org.sakaiproject.dash.listener.AssignmentNewEventProcessor");
			contProcessor = (org.sakaiproject.dash.listener.ContentNewEventProcessor) ComponentManager.get("org.sakaiproject.dash.listener.ContentNewEventProcessor");
			
			
			log.info(this +".init()");
		}
		catch (Throwable t)
		{
			log.warn(this +".init(): ", t);
		}
	}

	/**
	* Returns to uninitialized state.
	*/
	public void destroy()
	{
		// done with event watching
		m_eventTrackingService.deleteObserver(this);

		log.info(this +".destroy()");
	}

	/*******************************************************************************
	* Observer implementation
	*******************************************************************************/
	/**
	* This method is called whenever the observed object is changed. An
	* application calls an <tt>Observable</tt> object's
	* <code>notifyObservers</code> method to have all the object's
	* observers notified of the change.
	*
	* default implementation is to cause the courier service to deliver to the
	* interface controlled by my controller.  Extensions can override.
	*
	* @param   o     the observable object.
	* @param   arg   an argument passed to the <code>notifyObservers</code>
	*                 method.
	*/
	public void update(Observable o, Object arg)
	{
		// arg is Event
		if (!(arg instanceof Event))
			return;
		Event event = (Event) arg;
		
		// check the event function against the functions we have notifications watching for
		String function = event.getEvent();
		if (function.startsWith(sakaiProxy.EVENT_ANNOUNCEMENT_ROOT))
		{
			annProcessor.processEvent(event);
		}
	} // update
	
} // EventWatcher



