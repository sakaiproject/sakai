/**********************************************************************************
 * $URL:  $
 * $Id:  $
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
package org.sakaiproject.site.tool.helper.managegroupsectionrole.impl;

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
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.util.SiteConstants;

/**
* <p>RoleGroupEventWatcher is for </p>
* 
* @author University of Michigan, Sakai Software Development Team
* @version $Revision$
*/
public class RoleGroupEventWatcher implements Observer
{
	
	private static Log log = LogFactory.getLog(RoleGroupEventWatcher.class);
	
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
	
	/** Dependency: site service */
	protected SiteService m_siteService = null;

	/**
	 * Dependency: site service.
	 * @param service The site service.
	 */
	public void setSiteService(SiteService service)
	{
		m_siteService = service;
	}
	
	/** Dependency: AuthzGroup service */
	protected AuthzGroupService m_authzGroupService = null;

	/**
	 * Dependency: AuthzGroup service.
	 * @param service The AuthzGroup service.
	 */
	public void setAuthzGroupService(AuthzGroupService service)
	{
		m_authzGroupService = service;
	}

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
			// start watching the events - only those generated on this server, not those from elsewhere
			m_eventTrackingService.addLocalObserver(this);
			
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
		Session session = SessionManager.getCurrentSession();
		
		// check the event function against the functions we have notifications watching for
		String function = event.getEvent();

		if (function.equals(AuthzGroupService.SECURE_UPDATE_AUTHZ_GROUP) || function.equals(AuthzGroupService.SECURE_REMOVE_AUTHZ_GROUP))
		{
			Reference ref = EntityManager.newReference(event.getResource());
			
			// look for group reference. Need to replace it with parent site reference
			String refId = ref.getId();
			String groupType = Entity.SEPARATOR + SiteService.GROUP_SUBTYPE;
			if (refId.indexOf(groupType) != -1)
			{
				refId = refId.substring(0, refId.indexOf(groupType));
				ref = EntityManager.newReference(refId);
			}
			
			// this is the current realm reference
			if (ThreadLocalManager.get("current.event.resource.ref") == null)
			{
				ThreadLocalManager.set("current.event.resource.ref", ref);
				
				// importing from template, bypass the permission checking:
				// temporarily allow the user to read and write to site, authzgroup, etc.
		        SecurityService.pushAdvisor(new SecurityAdvisor()
	            {
	                public SecurityAdvice isAllowed(String userId, String function, String reference)
	                {
	                    return SecurityAdvice.ALLOWED;
	                }
	            });
		        
				try
				{
					String realmId = ref.getId();
					String siteId = realmId.replace(SiteService.REFERENCE_ROOT + "/", "");
					AuthzGroup r = m_authzGroupService.getAuthzGroup(realmId);
					Site site = m_siteService.getSite(siteId);
					
					// whether saving site is needed because some groups need updates
					boolean needSave = false;
					
					for (Object g : site.getGroups())
					{
						ResourceProperties properties = ((Group) g).getProperties();
						if (properties.getProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED) != null && properties.getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID) != null)
						{
							needSave = true;
							
							// this is a role provided group, need to sync with realm user now.
							String role = properties.getProperty(SiteConstants.GROUP_PROP_ROLE_PROVIDERID);
							
							// clean the group list first
							((Group) g).removeMembers();
							
							// update the group member with current realm users
							Set roleUserSet = r.getUsersHasRole(role);
							if (roleUserSet != null)
							{
								for(Object userId:roleUserSet)
								{
									((Group) g).addMember((String) userId, role, true, false);
								}
							}
						}	
					}
					if (needSave)
					{
						// Save only site group membership updates to this site
						m_siteService.saveGroupMembership(site);
					}
					
				}
				catch (Exception e)
				{
					log.warn(this + ".update:" + e.getMessage() + ": " + event.getResource());
				}
				
				SecurityService.clearAdvisors();
				
				// reset
				ThreadLocalManager.set("current.event.resource.ref", null);
			}
		}
	} // update
	
} // RoleGroupEventWatcher




