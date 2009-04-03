/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-util/util/src/java/org/sakaiproject/util/PresenceObservingCourier.java $
 * $Id: PresenceObservingCourier.java 8204 2006-04-24 19:35:57Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.chat2.tool;

import org.sakaiproject.chat2.model.PresenceObserver;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.presence.cover.PresenceService;

/**
 * <p>
 * PresenceObservingCourier is an EventObservingCourier which watches for only presence service events at a particular location, and delivers a direct refresh delivery.
 * </p>
 */
public class PresenceObserverHelper implements Observer
{
   /** Constructor discovered injected EventTrackingService. */
   protected EventTrackingService m_eventTrackingService = null;
   
   private String m_resourcePattern;
   private String location;
   
   private PresenceObserver presenceObserver;
	
   /**
	 * This variant, watches presense changes at 
    * the specified location, and sends the notifications to 
	 * that same location.  The elementID is null so the main window is refreshed when the notification 
	 * is received.
	 * 
	 * @param location
	 *        The location under observation *and* the location for the delivery of the events.
	 */
	public PresenceObserverHelper(PresenceObserver presenceObserver, String location)
	{
      this.presenceObserver = presenceObserver;
      this.location = location;
      m_resourcePattern = PresenceService.presenceReference(location);

      // "inject" a eventTrackingService
      m_eventTrackingService = org.sakaiproject.event.cover.EventTrackingService.getInstance();

      // register to listen to events
      m_eventTrackingService.addObserver(this);
      // %%% add the pattern to have it filtered there?
	}

   protected void finalize()
   {
      // stop observing the presence location
      m_eventTrackingService.deleteObserver(this);
   }
   
   public void endObservation()
   {
      // stop observing the presence location
      m_eventTrackingService.deleteObserver(this);
   }
   
   public void updatePresence()
   {
      PresenceService.setPresence(location);
   }
   
   public void removePresence()
   {
      PresenceService.removePresence(location);
   }
   
   public List getPresentUsers()
   {
      return PresenceService.getPresentUsers(location);
   }
   
   public String getLocation()
   {
      return location;
   }

	/**
	 * Check to see if we want to process or ignore this update.
	 * 
	 * @param arg
	 *        The arg from the update.
	 * @return true to continue, false to quit.
	 */
	public boolean check(Object arg)
	{
		// arg is Event
		if (!(arg instanceof Event)) return false;
		Event event = (Event) arg;
		String key = event.getResource();

		// reject non presence events
		String function = event.getEvent();
		if (!(function.equals(PresenceService.EVENT_PRESENCE) || function.equals(PresenceService.EVENT_ABSENCE))) return false;

		// look for matches to the pattern
		if (m_resourcePattern != null)
		{
			if (!key.equals(m_resourcePattern)) return false;
		}

		return true;
	}

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
		if (!check(arg)) return;

      Event event = (Event) arg;
      
      if(event.getEvent().equals(PresenceService.EVENT_PRESENCE))
         presenceObserver.userJoined(location, "");
      else
         presenceObserver.userLeft(location, "");
         
	}
}

