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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lti.impl;

import java.util.Map;
import java.util.ArrayList;
import java.util.Properties;

import org.sakaiproject.lti.api.LTIService;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import org.sakaiproject.util.foorm.SakaiFoorm;

/**
 * <p>
 * Implements the LTIService, all but a Storage model.
 * </p>
 */
public abstract class BaseLTIService implements LTIService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BaseLTIService.class);
	
	/** Constants */
	private final String ADMIN_SITE = "!admin";

	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("ltiservice");
	protected static SakaiFoorm foorm = new SakaiFoorm();

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		m_sessionManager = service;
	}

	/** Dependency: UsageSessionService */
	protected UsageSessionService m_usageSessionService = null;

	/**
	 * Dependency: UsageSessionService.
	 * 
	 * @param service
	 *        The UsageSessionService.
	 */
	public void setUsageSessionService(UsageSessionService service)
	{
		m_usageSessionService = service;
	}

	/** Dependency: UserDirectoryService */
	protected UserDirectoryService m_userDirectoryService = null;

	/**
	 * Dependency: UserDirectoryService.
	 * 
	 * @param service
	 *        The UserDirectoryService.
	 */
	public void setUserDirectoryService(UserDirectoryService service)
	{
		m_userDirectoryService = service;
	}

	/** Dependency: EventTrackingService */
	protected EventTrackingService m_eventTrackingService = null;

	/**
	 * Dependency: EventTrackingService.
	 * 
	 * @param service
	 *        The EventTrackingService.
	 */
	public void setEventTrackingService(EventTrackingService service)
	{
		m_eventTrackingService = service;
	}
	
	protected SecurityService securityService = null;
	protected SiteService siteService = null;
	protected ToolManager toolManager = null;
	
	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices()
	{
		if ( securityService == null ) securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		if ( siteService == null ) siteService = (SiteService) ComponentManager.get("org.sakaiproject.site.api.SiteService");
		if ( toolManager == null ) toolManager = (ToolManager) ComponentManager.get("org.sakaiproject.tool.api.ToolManager");
	}
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			M_log.info("init()");
			
		}
		catch (Exception t)
		{
			M_log.warn("init(): ", t);
		}
		
		getServices();

		// Check to see if all out properties are defined
		ArrayList<String> strings = foorm.checkI18NStrings(LTIService.TOOL_MODEL, rb);
		for ( String str : strings ) {
			System.out.println(str+"=Missing LTIService Translation");
		}

		strings = foorm.checkI18NStrings(LTIService.CONTENT_MODEL, rb);
		for ( String str : strings ) {
			System.out.println(str+"=Missing LTIService Translation");
		}

		strings = foorm.checkI18NStrings(LTIService.MAPPING_MODEL, rb);
		for ( String str : strings ) {
			System.out.println(str+"=Missing LTIService Translation");
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * LTIService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

 	/** getMappingModel */
        public String [] getMappingModel() 
	{
		if ( isAdmin() ) return MAPPING_MODEL;
	        return null;
	}
	
	/** getToolModel */
        public String [] getToolModel() 
	{
		if ( isAdmin() ) return TOOL_MODEL;
		if ( isMaintain() ) return foorm.filterForm(null, TOOL_MODEL, null, ".*:role=admin.*");
		return null;
	}

	public String [] getContentModel(Long tool_id)
	{
                if ( !isMaintain() ) return null;
	        Map<String,Object> tool = getTool(tool_id);
		if (  tool == null ) return null;
                String[] retval = foorm.filterForm(tool, CONTENT_MODEL);
		if ( !isAdmin() ) retval = foorm.filterForm(null, retval, null, ".*:role=admin.*");
	        return retval;
	}
	
	protected String getContext()
	{
	        String retval = toolManager.getCurrentPlacement().getContext();
                return retval;
        }
        
        public String formOutput(Object row, String fieldInfo)
        {
                return foorm.formOutput(row, fieldInfo, rb);
        }
        
        public String formOutput(Object row, String [] fieldInfo)
        {
                return foorm.formOutput(row, fieldInfo, rb);
        }
        
	public String formInput(Object row, String fieldInfo)
	{
	        return foorm.formInput(row, fieldInfo, rb);
	}
        
	public String formInput(Object row, String [] fieldInfo)
	{
	        return foorm.formInput(row, fieldInfo, rb);
	}
	
	
	
	public boolean isAdmin()
	{
	        if ( ! ADMIN_SITE.equals(getContext()) ) return false;
	        return isMaintain();
	}
	
	public boolean isMaintain()
	{
	        return siteService.allowUpdateSite(getContext());
	}
	
	/** Simple API signatures for the update series of methods */

	public Object updateTool(Long key, Map<String,Object> newProps)
	{
	        return updateTool(key, (Object) newProps);
	}
	
	public Object updateTool(Long key, Properties newProps)
	{
	        return updateTool(key, (Object) newProps);
	}
	
	public abstract Object updateTool(Long key, Object newProps); 
	
	public Object updateMapping(Long key, Map<String,Object> newProps)
	{
	        return updateMapping(key, (Object) newProps);
	}
	
	public Object updateMapping(Long key, Properties newProps)
	{
	        return updateMapping(key, (Object) newProps);
	}
	
	public abstract Object updateMapping(Long key, Object newProps); 

	public Object updateContent(Long key, Map<String,Object> newProps)
	{
	        return updateContent(key, (Object) newProps);
	}
	
	public Object updateContent(Long key, Properties newProps)
	{
	        return updateContent(key, (Object) newProps);
	}
	
	public abstract Object updateContent(Long key, Object newProps); 

}
