/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.OpaqueUrlDao;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;


public class SubscribeBean {
	
	private transient SiteService siteService = (SiteService) ComponentManager.get(SiteService.class.getName());
	private transient CalendarService calendarService = (CalendarService) ComponentManager.get(CalendarService.class.getName());
	private transient ToolManager toolManager = (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private transient SessionManager sessionManager	= (SessionManager) ComponentManager.get(SessionManager.class.getName());
	private transient OpaqueUrlDao opaqueUrlDao = (OpaqueUrlDao) ComponentManager.get(OpaqueUrlDao.class.getName());
	private transient EntityManager entityManager = (EntityManager) ComponentManager.get(EntityManager.class.getName());
	private transient ServerConfigurationService serverConfigurationService = (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class.getName());
	
	private String siteId = null;
	
	public SubscribeBean() {
	}
	
	private String siteId() {
		if(siteId == null){
			Placement placement = toolManager.getCurrentPlacement();
			siteId = placement.getContext();
		}
		return siteId;
	}
	
	private String calendarId() {
		return calendarService.calendarReference(siteId(), SiteService.MAIN_CONTAINER);
	}

	public String cancel() {
		return "calendar";
	}
	
	public String generate() {
		opaqueUrlDao.newOpaqueUrl(sessionManager.getCurrentSessionUserId(), calendarId());
		return "subscribe";
	}
	
	public String regenerate() {
		String userUUID = sessionManager.getCurrentSessionUserId();
		String calendarId = calendarId();
		opaqueUrlDao.deleteOpaqueUrl(userUUID, calendarId);
		opaqueUrlDao.newOpaqueUrl(userUUID, calendarId);
		return "subscribe";
	}

	public String delete() {
		opaqueUrlDao.deleteOpaqueUrl(sessionManager.getCurrentSessionUserId(), calendarId());
		return "subscribe";
	}
	
	public boolean isMyWorkspace() {
		return siteService.isUserSite(siteId());
	}
	
	public boolean isOpaqueUrlExists() {
		return opaqueUrlDao.getOpaqueUrl(sessionManager.getCurrentSessionUserId(), 
				calendarId()) != null;
	}

	public Map<String,String> getUrl() {
		HashMap<String,String> url = new HashMap<String,String>();
		Reference calendarRef = entityManager.newReference(calendarId());
		String httpForm = serverConfigurationService.getAccessUrl()
			+ calendarService.calendarOpaqueUrlReference(calendarRef);
		url.put("httpForm", httpForm);
		url.put("webcalForm", httpForm.replaceFirst("http", "webcal"));
		return url;
	}
}
