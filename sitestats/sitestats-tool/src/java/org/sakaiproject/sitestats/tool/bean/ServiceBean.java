/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
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
package org.sakaiproject.sitestats.tool.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.Authz;
import org.sakaiproject.sitestats.api.JobRun;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;



/**
 * @author nfernandes
 */
public class ServiceBean {
	private static final long				serialVersionUID	= 2279554800802502977L;

	/** Our log (commons). */
	private static Log						LOG					= LogFactory.getLog(ServiceBean.class);

	/** Resource bundle */
	private String							bundleName			= FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	private ResourceLoader					msgs				= new ResourceLoader(bundleName);

	private transient ToolManager			M_tm				= null;
	private transient SiteService			M_ss				= null;
	private transient SessionManager		M_sm				= null;
	private transient AuthzGroupService		M_ags				= null;
	private transient UserDirectoryService	M_uds				= null;
	private transient ContentHostingService	M_chs				= null;
	private transient TimeService			M_time				= null;
	private transient Authz					SST_authz			= null;
	private transient StatsManager			SST_sm				= null;
	private transient StatsUpdateManager	SST_sum				= null;

	private Site							site				= null;
	private String							siteId				= null;
	private long							prefsLastModified	= 1;


	// ################################################################
	// ManagedBean property methods
	// ################################################################
	public void setSstStatsManager(StatsManager sstStatsManager) {
		this.SST_sm = sstStatsManager;
	}
	public StatsManager getSstStatsManager() {
		return SST_sm;
	}
	
	public void setSstStatsUpdateManager(StatsUpdateManager sstStatsUpdateManager) {
		this.SST_sum = sstStatsUpdateManager;
	}
	public StatsUpdateManager getSstStatsUpdateManager() {
		return SST_sum;
	}
	
	public void setSstAuthz(Authz sstAuthz) {
		this.SST_authz = sstAuthz;
	}
	public Authz getSstAuthz() {
		return SST_authz;
	}
	
	public void setToolManager(ToolManager toolManager) {
		this.M_tm = toolManager;
	}
	public ToolManager getToolManager() {
		return M_tm;
	}
	
	public void setSiteService(SiteService siteService) {
		this.M_ss = siteService;
	}
	public SiteService getSiteService() {
		return M_ss;
	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.M_sm = sessionManager;
	}
	public SessionManager getSessionManager() {
		return M_sm;
	}
	
	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.M_ags = authzGroupService;
	}
	public AuthzGroupService getAuthzGroupService() {
		return M_ags;
	}
	
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.M_uds = userDirectoryService;
	}
	public UserDirectoryService getUserDirectoryService() {
		return M_uds;
	}
	
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.M_chs = contentHostingService;
	}
	public ContentHostingService getContentHostingService() {
		return M_chs;
	}
	
	public void setTimeService(TimeService timeService) {
		this.M_time = timeService;
	}
	public TimeService getTimeService() {
		return M_time;
	}

	// ################################################################
	// Utility methods
	// ################################################################	
	public boolean isAllowed() {
		boolean allowed = SST_authz.isUserAbleToViewSiteStats(M_tm.getCurrentPlacement().getContext());
		
		if(!allowed){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, msgs.getString("unauthorized"), null));
		}
		return allowed;
	}
	
	public ResourceLoader getBundle(){
		return msgs;
	}
	
	public long getPreferencesLastModified() {
		return prefsLastModified;
	}
	
	public void setPreferencesModified(){
		prefsLastModified = new Date().getTime();
	}
	
	public String getSiteId() {
		if(siteId == null){
			try{
				Placement placement = M_tm.getCurrentPlacement();
				siteId = placement.getContext();
			}catch(Exception e){
				LOG.error("Error determinig siteId.",e);
				return null;
			}
		}
		return siteId;
	}

	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}
	
	public Set<Role> getSiteRoles() {
		try{
			AuthzGroup agz = M_ags.getAuthzGroup(M_ss.siteReference(getSiteId()));
			return agz.getRoles();
		}catch(GroupNotDefinedException e){
			LOG.warn("ServiceBean: GroupNotDefinedException with id: " + M_ss.siteReference(getSiteId()));
			return new HashSet<Role>();
		}
	}
	
	public List<String> getSiteRolesAsString() {
		List<String> rolesStr = new ArrayList<String>();
		try{
			Set<Role> roles =  M_ss.getSite(getSiteId()).getRoles();
			Iterator<Role> i = roles.iterator();
			while(i.hasNext()){
				Role r = i.next();
				rolesStr.add(r.getId());
			}
		}catch(IdUnusedException e){
			LOG.warn("Inexistent site for site id: "+siteId, e);
		}
		return rolesStr;
	}
	
	public boolean isRoleEmpty(String role){
		return getSite().getUsersHasRole(role).isEmpty();
	}
	
	public Collection<Group> getSiteGroups() {
		return getSite().getGroups();
	}
	
	public String getSiteGroupTitle(String groupId) {
		return getSite().getGroup(groupId).getTitle();
	}
	
	public boolean isSiteGroupEmpty(String groupId) {
		return getSite().getGroup(groupId).getUsers().isEmpty();
	}
	
	public List<User> getSiteUsers() {
		List<String> userIds = new ArrayList<String>();
		userIds.addAll(getSite().getUsers()); 
		return M_uds.getUsers(userIds);
	}
	
	public String getUserDisplayId(String userId) {
		try{
			return M_uds.getUser(userId).getDisplayId();
		}catch(UserNotDefinedException e){
			return userId;
		}
	}
	
	public String getUserDisplayName(String userId){
		try{
			return M_uds.getUser(userId).getDisplayName();
		}catch(UserNotDefinedException e1){
			return "";
		}
	}

	public Site getSite() {
		try{
			site = M_ss.getSite(getSiteId());
		}catch(IdUnusedException e){
			LOG.warn("ServiceBean: no site found with id: " + siteId);
		}
		return site;
	}
	
	public boolean isAdminView(){
		return M_tm.getCurrentTool().getId().equals("sakai.sitestats.admin")
			&& (SecurityService.isSuperUser() || new Boolean(SST_authz.isUserAbleToViewSiteStats(M_tm.getCurrentPlacement().getContext())));
	}
	
	public String getSiteTitle() {
		return getSite().getTitle();
	}
	
	public String processActionSiteId() {
		try{
			ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
			Map paramMap = context.getRequestParameterMap();
			String id = (String) paramMap.get("siteId");
			setSiteId(id);
			return "overview";
		}catch(Exception e){
			LOG.error("Error getting siteId",e);
			return "sitelist";
		}
	}

	
	public boolean getSiteVisitsEnabled() {
		return SST_sm.isEnableSiteVisits();
	}
	
	public boolean isLastJobRunDateVisible(){
		return !SST_sum.isCollectThreadEnabled() && SST_sm.isLastJobRunDateVisible();
	}
	
	public Date getLastJobRunDate(){
		try{
			return SST_sum.getEventDateFromLatestJobRun();
		}catch(Exception e){
			LOG.error("Error getting latestJobRun",e);
		}
		return new Date();
	}
}
