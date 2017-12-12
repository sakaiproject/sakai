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

package org.sakaiproject.site.tool;

import java.io.IOException;

import java.util.Properties;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

/**
 * <p>
 * LinkAction allows site owners to link a site to a parent.
 * </p>
 */
@Slf4j
public class LinkAction extends VelocityPortletPaneledAction
{
	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("link");

	/** Name of state attribute for Site instance id */
	private static final String STATE_SITE_INSTANCE_ID = "site.instance.id";

	/**
	 * Get the current site page our current tool is placed on.
	 * 
	 * @return The site page id on which our tool is placed.
	 */
	protected String getCurrentSitePageId()
	{
		ToolSession ts = SessionManager.getCurrentToolSession();
		if (ts != null)
		{
			ToolConfiguration tool = SiteService.findTool(ts.getPlacementId());
			if (tool != null)
			{
				return tool.getPageId();
			}
		}
		
		return null;
	}

	/**
	 * Get the current site id
	 * @param state SessionState
	 * @throws SessionDataException
	 * @return Site id (GUID)
	 */
	private String getSiteId(SessionState state) throws SessionDataException
	{
		// Check if it is state (i.e. we are a helper in site.info)
		String retval = (String) state.getAttribute(STATE_SITE_INSTANCE_ID);
		if ( retval != null ) return retval;

		// If it is not in state, we must be stand alone
		Placement placement = ToolManager.getCurrentPlacement();

		if (placement == null)
		{
			throw new SessionDataException("No current tool placement");
		}
		return placement.getContext();
	}

	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// set the resource bundle with our strings
		context.put("tlang", rb);

                context.put("doSave", BUTTON + "doSave");
                context.put("doCancel", BUTTON + "doCancel");
                context.put("doRemove", BUTTON + "doRemove");
		try 
		{ 
			Site site;
			String siteId = getSiteId(state);

			site = SiteService.getSite(siteId);
			String parentId = site.getProperties().getProperty("sakai:parent-id");
                        context.put("currentSite", site);
			if ( parentId != null ) {
				// Make sure parent site exists before we show it.
				// If the parent site does not exist, clear the property
				try {
					Site parentSite = SiteService.getSite(parentId);
                			context.put("parentId", parentId);
                			context.put("parentTitle", parentSite.getTitle());
					return "sakai_link";
				} catch (Exception e) {
					addAlert(state,rb.getFormattedMessage("alert.parent.removed", new Object[]{parentId}));
					ResourcePropertiesEdit rpe = site.getPropertiesEdit();
					rpe.removeProperty("sakai:parent-id");
					SiteService.save(site);
				}
			}

			// Give the user a list of sites to select as parent
			List<Site> sites = SiteService.getSites(
				org.sakaiproject.site.api.SiteService.SelectionType.UPDATE,
				null, null, null, SortType.TITLE_ASC, null);

			List<Site> goodSites = new ArrayList<Site> ();
			// Do not include any sites which point to us as a parent
			// Do not include ourself in the candidate list
                       	for (Iterator i = sites.iterator(); i.hasNext(); ) {
				Site thisSite = (Site) i.next();
				String pid = thisSite.getProperties().getProperty("sakai:parent-id");
				if ( siteId.equals(pid) ) continue;
				if ( siteId.equals(thisSite.getId()) ) continue;
				goodSites.add(thisSite);
			}
			if ( goodSites.size() > 0 ) context.put("sites", goodSites);
		} 
		catch (Exception e) 
		{
			addAlert(state,rb.getString("error.cannot.access"));
		}

		return "sakai_link";
	}

	/**
	 * Handle the configure context's update button
	 */
	public void doSave(RunData data, Context context)
	{
		// TODO: if we do limit the initState() calls, we need to make sure we get a new one after this call -ggolden

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
                ParameterParser params = data.getParameters();
		String parentId = params.getString("parentSite");

		if ( ! SiteService.allowUpdateSite(parentId) ) 
		{
			addAlert(state,rb.getString("error.cannot.update"));
			return;
		}

		try
		{
			Site site;
			site = SiteService.getSite(getSiteId(state));
			ResourcePropertiesEdit rpe = site.getPropertiesEdit();
			rpe.addProperty("sakai:parent-id", parentId);
			SiteService.save(site);
			SessionManager.getCurrentToolSession().setAttribute(HELPER_LINK_MODE, HELPER_MODE_DONE);
		} 
		catch (Exception e)
		{
			addAlert(state,rb.getString("error.cannot.update"));
		}

	}

	/**
	 * doRemove - Clear the parent id value
	 */
	public void doRemove(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		try
		{
			Site site;
			site = SiteService.getSite(getSiteId(state));
			ResourcePropertiesEdit rpe = site.getPropertiesEdit();
			rpe.removeProperty("sakai:parent-id");
			SiteService.save(site);
			SessionManager.getCurrentToolSession().setAttribute(HELPER_LINK_MODE, HELPER_MODE_DONE);
		} 
		catch (Exception e)
		{
			addAlert(state,rb.getString("error.cannot.remove"));
		}
	}

	/**
	 * doCancel called for form input tags type="submit" named="eventSubmit_doCancel" cancel the options process
	 */
	public void doCancel(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		SessionManager.getCurrentToolSession().setAttribute(HELPER_LINK_MODE, HELPER_MODE_DONE);
	}

	/**
	 * Note a "local" problem (we failed to get session or site data)
	 */
	private static class SessionDataException extends Exception
	{
		public SessionDataException(String text)
		{
			super(text);
		}
	}
}
