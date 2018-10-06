/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.su;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author zach.thomas@txstate.edu
 */
@Slf4j
public class SuTool
{
	private static final long serialVersionUID = 1L;

	protected static final String SU_BECOME_USER = "su.become";
	protected static final String SU_VIEW_USER = "su.view";

	ResourceLoader msgs = new ResourceLoader("tool-tool-su");

	// Service instance variables
	private AuthzGroupService M_authzGroupService = ComponentManager.get(AuthzGroupService.class);

	private UserDirectoryService M_uds = org.sakaiproject.user.cover.UserDirectoryService.getInstance();

	private SecurityService M_security = org.sakaiproject.authz.cover.SecurityService.getInstance();

	private SessionManager M_session = org.sakaiproject.tool.cover.SessionManager.getInstance();

	private ServerConfigurationService M_config = org.sakaiproject.component.cover.ServerConfigurationService
			.getInstance();
	
	private EventTrackingService M_event_service = org.sakaiproject.event.cover.EventTrackingService.getInstance();

	private UserTimeService userTimeService = (UserTimeService) ComponentManager.get(UserTimeService.class);

	// getters for these vars
	private String username;

	private String validatedUserId;
	
	private String validatedUserEid;

	private User userinfo;

	private boolean allowed = false;

	// internal only vars
	private String message = "";

	private boolean confirm = false;
	
	private Class delegatedAccessLogicHelper = null;
	private Object delegatedAccessLogic = null;
	private Method hasDelegatedAccessNodes = null;
	private Method initializeDelegatedAccessSession = null;
	private Method isUserAllowBecomeUser = null;
	private boolean allowDelegatedAccessBecomeUser = false;
	
	// base constructor
	public SuTool()
	{
		try{
			delegatedAccessLogicHelper = RequestFilter.class.getClassLoader().loadClass("org.sakaiproject.delegatedaccess.logic.ProjectLogic");
			delegatedAccessLogic = ComponentManager.get(delegatedAccessLogicHelper);
			hasDelegatedAccessNodes = delegatedAccessLogicHelper.getMethod("hasDelegatedAccessNodes", new Class[]{String.class});
			initializeDelegatedAccessSession = delegatedAccessLogicHelper.getMethod("initializeDelegatedAccessSession", new Class[]{});
			isUserAllowBecomeUser = delegatedAccessLogicHelper.getMethod("isUserAllowBecomeUser", new Class[]{String.class, String.class});
			//only allow become user logic for Delegated Access if the allowBecomeUser method exist
			if(isUserAllowBecomeUser != null){
				allowDelegatedAccessBecomeUser = true;
			}
		}catch(Exception e){
			log.info("Could not inject Delegated Access logic bean, either doesn't exist or there is a bigger problem");
		}
	}

	/**
	 * Functions
	 */
	public String su()
	{

		Session sakaiSession = M_session.getCurrentSession();
		FacesContext fc = FacesContext.getCurrentInstance();
		userinfo = null;
		message = "";

		try
		{
			// try with the user id
			userinfo = M_uds.getUser(username.trim());
			validatedUserId = userinfo.getId();
			validatedUserEid = userinfo.getEid();
		}
		catch (UserNotDefinedException e)
		{
			try
			{
				// try with the user eid
				userinfo = M_uds.getUserByEid(username.trim());
				validatedUserId = userinfo.getId();
				validatedUserEid = userinfo.getEid();
			}
			catch (UserNotDefinedException ee)
			{
				message = msgs.getString("no_such_user") + ": " + username;
				fc.addMessage("su", new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message + ":" + ee));
				log.warn("[SuTool] Exception: " + message);
				confirm = false;
				return "error";
			}
		}
		
		if (!getAllowed(userinfo))
		{
			confirm = false;
			userinfo = null;
			return "unauthorized";
		}
		
		// don't try to become yourself
		if (sakaiSession.getUserEid().equals(validatedUserEid)) {
			confirm = false;
			message = msgs.getFormattedMessage("already_that_user", new Object[] {username});
			fc.addMessage("su", new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
			log.warn("[SuTool] Exception: " + message);
			confirm = false;
			return "error";
		}

		if (!confirm)
		{
			message = msgs.getString("displaying_info_for") + ": " + validatedUserEid;
			fc.addMessage("su", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message + ":" + userinfo.getDisplayName()));
			Event event = M_event_service.newEvent(SU_VIEW_USER, M_uds.userReference(validatedUserId), false);
			M_event_service.post(event);
			return "unconfirmed";
		}

		// set the session user from the value supplied in the form
      message = "Username " + sakaiSession.getUserEid() + " becoming " + validatedUserEid;
		log.info("[SuTool] " + message);
      message = msgs.getString("title");
		fc.addMessage("su", new FacesMessage(FacesMessage.SEVERITY_INFO, message, message + ": "
				+ userinfo.getDisplayName()));
		
		// while keeping the official usage session under the real user id, switch over everything else to be the SU'ed user
		// Modeled on UsageSession's logout() and login()
		
		// Post an event
		Event event = M_event_service.newEvent(SU_BECOME_USER, M_uds.userReference(validatedUserId), false);
		M_event_service.post(event);

		// logout - clear, but do not invalidate, preserve the usage session's current session
		Vector saveAttributes = new Vector();
		saveAttributes.add(UsageSessionService.USAGE_SESSION_KEY);
		saveAttributes.add(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE);
		sakaiSession.clearExcept(saveAttributes);
		
		// login - set the user id and eid into session, and refresh this user's authz information
		sakaiSession.setUserId(validatedUserId);
		sakaiSession.setUserEid(validatedUserEid);
		M_authzGroupService.refreshUser(validatedUserId);
		//if DA is present, initialize the user's DA settings:
		if(initializeDelegatedAccessSession != null){
			try{
				initializeDelegatedAccessSession.invoke(delegatedAccessLogic, new Object[]{});
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
		}

		//as user is authorised redirect back to portal instead of going to admin workspace
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		try {
			context.redirect(getPortalUrl());
		} catch (IOException e) {
			log.error("Failed to redirect to portal : " + e.getMessage());
		}
		return "";
	}

	// simple way to support 2 buttons that do almost the same thing
	public String confirm()
	{
		confirm = true;
		return su();
	}

	/**
	 * Specialized Getters
	 */
	public boolean getAllowed()
	{
		Session sakaiSession = M_session.getCurrentSession();
		FacesContext fc = FacesContext.getCurrentInstance();
		//allow the user to access the tool if they are either a DA user or Super Admin
		if (!M_security.isSuperUser() && sakaiSession.getAttribute("delegatedaccess.accessmapflag") != null)
		{
			message = msgs.getString("unauthorized") + " " + sakaiSession.getUserId();
			log.error("[SuTool] Fatal Error: " + message);
			fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message));
			allowed = false;
		}
		else
		{
			allowed = true;
		}

		return allowed;
	}
	
	public boolean getAllowed(User userinfo)
	{
		Session sakaiSession = M_session.getCurrentSession();
		FacesContext fc = FacesContext.getCurrentInstance();

		if (!M_security.isSuperUser())
		{
			//current user is not a super admin, let's make sure they are not becoming a user they are not allowed to become
			if(!M_security.isSuperUser(userinfo.getId())){
				//Delegated Access check
				//is the user a DA user?  If so, check their access as well as the become user accsess, otherwise, deny
				if(allowDelegatedAccessBecomeUser && getDelegatedAccessUser()){
					//this flag is only set when a user is a DA user
					//now check if the become user is a DA user, if so, then do not allow a DA user to become another DA user
					if(!getDelegatedAccessUser(userinfo.getId())){
						//the user is not a DA user, so lets check if the user is a member of any sites that the 
						//current user has DA access to visit
						String currentUserId = sakaiSession.getUserId();
						String currentUserEid = sakaiSession.getUserEid();
						List siteList = null;
						try{
							sakaiSession.setUserId(userinfo.getId());
							sakaiSession.setUserEid(userinfo.getEid());
							siteList = org.sakaiproject.site.cover.SiteService.getSites(SelectionType.ACCESS, null, null, null, null, null);
						}catch(Exception e){
							log.info(e.getMessage(), e);
						}finally{
							sakaiSession.setUserId(currentUserId);
							sakaiSession.setUserEid(currentUserEid);
						}
						if(siteList != null && siteList.size() > 0){
							boolean anyAccess = false;
							try{
								for(Site site : (List<Site>) siteList){
									Object val = isUserAllowBecomeUser.invoke(delegatedAccessLogic, new Object[]{sakaiSession.getUserId(), site.getReference()});
									if(val != null && val instanceof Boolean && ((Boolean) val)){
										//this user has site access and "become user" permission for this site
										anyAccess = true;
										break;
									}
								}
							}catch(Exception e){
								log.info(e.getMessage(), e);
							}
							if(anyAccess){
								//this means that the current user has access to a site that the userinfo user is a member of, so
								//let them become this user
								allowed = true;
							}else{
								//current user can't become a user that isn't within their DA access
								message = msgs.getString("unauthorized_danoaccess");
								log.error("[SuTool] Fatal Error: " + message + " " + sakaiSession.getUserId());
								fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message));
								allowed = false;
							}
						}else{
							//the userinfo user either doesn't have any sites to access or there was an error
							message = msgs.getString("unauthorized_danoaccess");
							log.info("[SuTool] Fatal Error: " + message + " " + sakaiSession.getUserId());
							fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message));
							allowed = false;
						}
					}else{
						//current user is trying to become a DA user, which isn't allowed
						message = msgs.getString("unauthorized_da");
						log.info("[SuTool] Fatal Error: " + message + " " + sakaiSession.getUserId());
						fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message));
						allowed = false;
					}
				}else{
					//current user is not a DA user and not a super user, so they have no access
					message = msgs.getString("unauthorized");
					log.info("[SuTool] Fatal Error: " + message + " " + sakaiSession.getUserId());
					fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message));
					allowed = false;
				}
			}else{
				//non admin users can't become an admin user
				message = msgs.getString("unauthorized_superuser");
				log.info("[SuTool] Fatal Error: " + message + " " + sakaiSession.getUserId());
				fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, message));
				allowed = false;
			}
		}
		else
		{
			allowed = true;
		}

		return allowed;
	}

	/**
	 * Basic Getters and setters
	 */
	public String getUsername()
	{
		return username;
	}

	public String getPortalUrl()
	{
		return M_config.getPortalUrl();
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public User getUserinfo()
	{
		return userinfo;
	}

	public void setUserinfo(User userinfo)
	{
		this.userinfo = userinfo;
	}

	public String getUserCreatedTime()
	{
		if (userinfo == null)
		{
			return "";
		}

		DateFormat dsf = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, msgs.getLocale());
		dsf.setTimeZone(userTimeService.getLocalTimeZone());
		Date createdDate = userinfo.getCreatedDate();
		return dsf.format(createdDate == null ? new Date() : createdDate);
	}

	public String getMessage(){
		return message;
	}
	
	public Boolean getSuperUser(){
		return M_security.isSuperUser();
	}
	
	public Boolean getDelegatedAccessUser(){
		return getDelegatedAccessUser(M_session.getCurrentSessionUserId());
	}
	
	public Boolean getDelegatedAccessUser(String userId){
		if(hasDelegatedAccessNodes != null){
			try {
				Object hasAccess = hasDelegatedAccessNodes.invoke(delegatedAccessLogic, new Object[]{userId});
				if(hasAccess != null && hasAccess instanceof Boolean && ((Boolean) hasAccess)){
					return Boolean.TRUE;
				}
			} catch (Exception e){
				log.error(e.getMessage(), e);
			}
		}
		return Boolean.FALSE;	
	}
}
