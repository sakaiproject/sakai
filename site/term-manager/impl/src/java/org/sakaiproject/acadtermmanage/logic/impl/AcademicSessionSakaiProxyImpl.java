/**
 * Copyright (c) 2003-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.acadtermmanage.logic.impl;

import static org.sakaiproject.acadtermmanage.AcademicTermConstants.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.acadtermmanage.exceptions.DuplicateKeyException;
import org.sakaiproject.acadtermmanage.logic.AcademicSessionSakaiProxy;
import org.sakaiproject.acadtermmanage.model.Semester;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementAdministration;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.exception.IdExistsException;
import org.sakaiproject.coursemanagement.api.exception.IdNotFoundException;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AcademicSessionSakaiProxyImpl implements AcademicSessionSakaiProxy {
	

	@Setter
	private EventTrackingService eventService;
	
	@Setter
	private SiteService siteService;
	
	@Setter
	private CourseManagementService cmService;
	@Setter
	private CourseManagementAdministration cmAdmin;
	
	@Setter
	private ServerConfigurationService serverConfigurationService;
	
	@Setter
	private SessionManager sessionManager;
	
	@Setter
	private ToolManager toolManager;
	
	@Setter
	private SecurityService securityService;
	
	@Setter
	private PreferencesService prefService;
	
	@Getter @Setter
	private FunctionManager functionManager;

	@Override
	public AcademicSession getAcademicSession(String eid) throws IdNotFoundException{	
		return cmService.getAcademicSession(eid);
	}

	@Override
	public List<AcademicSession> getAcademicSessions() {		
		return cmService.getAcademicSessions();
	}


	@Override
	public AcademicSession createAcademicSession(String eid, String title,
			String description, Date startDate, Date endDate) {
		return cmAdmin.createAcademicSession(eid, title, description, startDate, endDate);
	}

	@Override
	public void setCurrentAcademicSessions(List<String> eids) {		 
		cmAdmin.setCurrentAcademicSessions(eids);		
	}

	@Override
	public void removeAcademicSession(String eid) {
		cmAdmin.removeAcademicSession(eid);
		
	}
	@Override
	public void updateAcademicSession(AcademicSession session){
		cmAdmin.updateAcademicSession(session);
	}

	
	@Override
	public void registerPermission(String fn){	
		if (fn != null && fn.trim().length()>0){
			List<String> fns = functionManager.getRegisteredFunctions();
			if (fns == null || !fns.contains(fn)){
				functionManager.registerFunction(fn);
			}
		}
	}


	@Override
	public List<AcademicSession> getCurrentAcademicSessions() {
		return cmService.getCurrentAcademicSessions();
	}


	
	@Override
	public List<Site> getSitesForTerm (String termEID) {
		HashMap<String, String> propCriteria = new HashMap<String,String>(1);
		propCriteria.put(PROP_NAME_TERM_EID, termEID);
		return siteService.getSites(SelectionType.ANY, null, null, propCriteria, SortType.NONE ,null);		
	}
	
	@Override
	public Site getSite(String siteID) throws IdUnusedException {
		return siteService.getSite(siteID);
	}

	@Override
	public void saveSite(Site site) throws IdUnusedException, PermissionException{
		siteService.save(site);
	}
	
	@Override
	public void notifyEventServiceOfUpdate (String termEid) {	
		Event update = eventService.newEvent(EVENTSERVICE_EVENT_ACADEMICSESSION_UPDATE, 
				EVENTSERVICE_EVENT_RESOURCE_PREFIX+termEid, true);
		eventService.post(update);		
	}
	
	@Override
	public void notifyEventServiceOfInsert (String termEid) {	
		Event update = eventService.newEvent(EVENTSERVICE_EVENT_ACADEMICSESSION_ADD, 
				EVENTSERVICE_EVENT_RESOURCE_PREFIX+termEid, true);
		eventService.post(update);		
	}
	
	
	/**
 	* {@inheritDoc}
 	*/
	@Override
	public String getSkinRepoProperty(){
		return serverConfigurationService.getString("skin.repo");
	}
	
	/**
 	* {@inheritDoc}
 	*/
	@Override
	public String getToolSkinCSS(String skinRepo){
		
		String skin = siteService.findTool(sessionManager.getCurrentToolSession().getPlacementId()).getSkin();	
		if(skin == null) {
			skin = serverConfigurationService.getString("skin.default");
		}
		
		return skinRepo + "/" + skin + "/tool.css";
	}
	
	
	@Override
	public boolean isCurrentlyAllowed(String functionName) {
		String uid = getCurrentUserId();
		String sid = getCurrentSiteId();
		if (sid != null && uid != null) {
			return isUserAllowedFunction(uid, sid, functionName);
		}
		return false;
	}
	
	@Override
	public String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}
	
	@Override
	public String getCurrentSiteId() {
		Placement pl = toolManager.getCurrentPlacement();
		if (pl != null) {
			return pl.getContext();
		}
		else {
			return null;
		}
	}
	
	@Override
	public boolean isUserAllowedFunction(String userID, String siteID, String function) {
		String siteRef = siteService.siteReference(siteID);
		return securityService.unlock(userID, function, siteRef);
	}
	
	@Override
	public boolean isSuperUser() {
		return securityService.isSuperUser();
	}
	
	
	@Override
	public void pushSecurityAdvisor(SecurityAdvisor securityOverride) {
		securityService.pushAdvisor(securityOverride);
	}

	@Override
	public void popSecurityAdvisor(SecurityAdvisor securityOverride) {
		securityService.popAdvisor(securityOverride);
	}
	
	@Override
	public Locale getCurrentUserLocale(){
		return prefService.getLocale(getCurrentUserId());
	}
	
	@Override
	public AcademicSession addAcademicSession(Semester t) throws DuplicateKeyException {
		String eid = t.getEid();
		try {
			return createAcademicSession(eid, t.getTitle(),
				t.getDescription(), t.getStartDate(), t.getEndDate());
		}
		catch (IdExistsException e) {
			String msg ="EID already in DB: \""+eid+"\"";		
			if  (log.isDebugEnabled()) {				 
				log.debug(msg);
				// WTF? The API seems to throw IdExistsExceptions for every problem.
				// Not only existing IDs, but also when you try to insert a null value into a "NOT NULL" DB column
				// (e.g. when the Semester doesn't have a description or title).
				// Let's see if the exception message is at least different in those cases..:  
				log.debug("original message: "+e.getMessage());
			}			
			throw new DuplicateKeyException(msg);
		}
	}

	public void init() {
		// just because we've defined it in the components.xml and it
	}
	
}
