/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.component.common.edu.person;

import java.util.Observable;
import java.util.Observer;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.user.api.UserDirectoryService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This observer watches for user.del events and removes the profiles associated with the user
 * @author dhorwitz
 *
 */
@Setter @Slf4j
public class SakaiPersonObserver implements Observer {

	private EventTrackingService eventTrackingService;	
	private SakaiPersonManager sakaiPersonManager;	
	private EntityManager entityManager;	
	private ServerConfigurationService serverConfigurationService;

	public void init() {
		log.info("init()");
		if (serverConfigurationService.getBoolean("profile.autoCleanUp", true)) {
			eventTrackingService.addObserver(this);
		}
	}
	
	public void destroy() {
		log.info("destroy()");
		eventTrackingService.deleteObserver(this);
	}
	
	public void update(Observable o, Object arg) {
		// arg is Event
		if (!(arg instanceof Event))
			return;
		Event event = (Event) arg;
		
		
		// check the event function against the functions we have notifications watching for
		String function = event.getEvent();
		
		if (UserDirectoryService.SECURE_REMOVE_USER.equals(function)) {
			//then delete this users profiles
			
			
			Reference ref = entityManager.newReference(event.getResource());
			
			// look for group reference. Need to replace it with parent site reference
			String refId = ref.getId();
			
			///TODO we need a security advisor
			
			SakaiPerson sp = sakaiPersonManager.getSakaiPerson(refId, sakaiPersonManager.getUserMutableType());
			if (sp != null) {
				sakaiPersonManager.delete(sp);
			}
			
			sp = sakaiPersonManager.getSakaiPerson(refId, sakaiPersonManager.getSystemMutableType());
			if (sp != null) {
				sakaiPersonManager.delete(sp);
			}
		}
	}
}
