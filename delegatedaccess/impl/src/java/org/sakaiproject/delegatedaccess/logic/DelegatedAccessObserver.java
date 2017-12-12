/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.logic;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.SiteService;

/**
 * This is an Observer for Delegated Access.  It listens for a user to login and checks
 * if they have any delegated access.  If so, then it populates the user's session with
 * the access information.
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
@Slf4j
public class DelegatedAccessObserver implements Observer {
	@Getter @Setter
	private ProjectLogic projectLogic;
	@Getter @Setter
	private EventTrackingService eventTrackingService;

	public void init() {
		log.info("init()");
		eventTrackingService.addObserver(this);
	}

	public void update(Observable arg0, Object arg) {
		if (!(arg instanceof Event))
			return;

		Event event = (Event) arg;

		// check the event function against the functions we have notifications watching for
		if (UsageSessionService.EVENT_LOGIN.equals(event.getEvent())
				|| UsageSessionService.EVENT_LOGIN_CONTAINER.equals(event.getEvent())) {
			projectLogic.initializeDelegatedAccessSession();
		}else if(SiteService.SECURE_REMOVE_SITE.equals(event.getEvent())){
			//Site has been deleted, check if it exists and remove all nodes:
			boolean deleted = false;
			Map<String, List<String>> nodeIds = projectLogic.getNodesBySiteRef(new String[]{event.getResource()}, DelegatedAccessConstants.HIERARCHY_ID);
			if(nodeIds != null && nodeIds.containsKey(event.getResource())){
				for(String nodeId : nodeIds.get(event.getResource())){
					projectLogic.removeNode(nodeId);
					deleted = true;
				}
				if(deleted){
					projectLogic.clearNodeCache();
				}
			}
		}else if(DelegatedAccessConstants.EVENT_CHECK_ACCESS.equals(event.getEvent())){
			//this will set the Session attribute for this site and user
			projectLogic.getCurrentUsersAccessToSite(event.getResource());
		}
	}

}
