/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.charon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * @author ieb
 *
 */
public class ToolHelperImpl
{
	private static final Log log = LogFactory.getLog(ToolHelperImpl.class);

	
	public static final String TOOLCONFIG_REQUIRED_PERMISSIONS = "functions.require";
	public static final String PORTAL_VISIBLE = "sakai-portal:visible";

	/**
	 * The optional tool configuration tag "functions.require" describes a
	 * set of permission lists which decide the visibility of the tool link
	 * for this site user. Lists are separated by "|" and permissions within a
	 * list are separated by ",". Users must have all the permissions included in
	 * at least one of the permission lists.
	 *
	 * For example, a value like "section.role.student,annc.new|section.role.ta"
	 * would let a user with "section.role.ta" see the tool, and let a user with
	 * both "section.role.student" AND "annc.new" see the tool, but not let a user
	 * who only had "section.role.student" see the tool.
	 *
	 * If the configuration tag is not set or is null, then all users see the tool.
	 */
	public boolean allowTool(Site site, Placement placement)
	{
		if(allowToolHelper(site, placement)){
			if(!SecurityService.isSuperUser()){
				try{
					//delegated access sets a session attribute that determines if the user can't view a tool in a site
					//delegatedaccess.deniedToolsMap = SiteId => List{toolid, toolid ...}
					//if this tool shows up, return false, otherwise return true

					Session session = SessionManager.getCurrentSession();
					if(session.getAttribute("delegatedaccess.deniedToolsMap") != null && ((Map) session.getAttribute("delegatedaccess.deniedToolsMap")).containsKey(site.getReference())
							&& arrayContains(((Map) session.getAttribute("delegatedaccess.deniedToolsMap")).get(site.getReference()), placement.getToolId())){
						return false;
					}
					if(session.getAttribute("delegatedaccess.deniedToolsMap") == null ||
							!((Map<String, String[]>) session.getAttribute("delegatedaccess.deniedToolsMap")).containsKey(site.getReference())
							|| ((Map<String, String[]>) session.getAttribute("delegatedaccess.deniedToolsMap")).get(site.getReference()) == null){
						//a delegated access admin would have this map and site (even if it was set to null), if its null, that means the user is just has access to a different site and not this one
						if(site.getMember(session.getUserId()) == null && 
								(site.getProperties().get("shopping-period-public-tools") != null || site.getProperties().get("shopping-period-auth-tools") != null)){
							//this is .anon or .auth role in a site that needs to restrict the tools:
							boolean anonAccess = site.getProperties().get("shopping-period-public-tools") != null 
									&& arrayContains(((String) site.getProperties().get("shopping-period-public-tools")).split(";"), placement.getToolId());
							if(session.getUserId() == null){
								return anonAccess;
							}else{
								return anonAccess || (site.getProperties().get("shopping-period-auth-tools") != null && arrayContains(((String) site.getProperties().get("shopping-period-auth-tools")).split(";"), placement.getToolId()));
							}
						}
					}
				}catch (Exception e) {
				}
			}
			return true;
		}else{
			return false;
		}
	}
	
	private boolean arrayContains(Object obj, String item){
		if(obj != null && obj instanceof String[]){
			String[] array = (String[]) obj;
			for(int i = 0; i < array.length; i++){
				if(array[i].equals(item))
					return true;
			}
		}
		return false;
	}
	
	public boolean allowToolHelper(Site site, Placement placement)
	{
		// No way to render an opinion
		if (placement == null || site == null) return true;

		String requiredPermissionsString = placement.getConfig().getProperty(TOOLCONFIG_REQUIRED_PERMISSIONS);
		if (log.isDebugEnabled()) log.debug("requiredPermissionsString=" + requiredPermissionsString + " for " + placement.getToolId());
		if (requiredPermissionsString == null)
			return true;
		requiredPermissionsString = requiredPermissionsString.trim();
		if (requiredPermissionsString.length() == 0)
			return true;

		String[] allowedPermissionSets = requiredPermissionsString.split("\\|");
		for (int i = 0; i < allowedPermissionSets.length; i++)
		{
			String[] requiredPermissions = allowedPermissionSets[i].split(",");
			if (log.isDebugEnabled()) log.debug("requiredPermissions=" + Arrays.asList(requiredPermissions));
			boolean gotAllInList = true;
			for (int j = 0; j < requiredPermissions.length; j++)
			{
				if (!SecurityService.unlock(requiredPermissions[j].trim(), site.getReference()))
				{
					gotAllInList = false;
					break;
				}
			}
			if (gotAllInList)
			{
				return true;
			}
		}

		// No permission sets were matched.
		return false;
	}

	/**
	 * Check if the placement is hidden.
	 * @param placement
	 * @return <code>true</code> if the current placement is hidden.
	 */
	public boolean isHidden(Placement placement)
	{
		if (placement == null) return true;
		String visibility = placement.getConfig().getProperty(PORTAL_VISIBLE);
		if ( "false".equals(visibility) ) return true;
		String requiredPermissionsString = StringUtils.trimToNull(placement.getConfig().getProperty(TOOLCONFIG_REQUIRED_PERMISSIONS));
		if (requiredPermissionsString == null)
			return false;
		return requiredPermissionsString.contains("site.upd");
	}
}
