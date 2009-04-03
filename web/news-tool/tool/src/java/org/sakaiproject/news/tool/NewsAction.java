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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.news.tool;

import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.news.api.NewsChannel;
import org.sakaiproject.news.api.NewsConnectionException;
import org.sakaiproject.news.api.NewsFormatException;
import org.sakaiproject.news.cover.NewsService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * NewsAction is the Sakai RSS news tool.
 * </p>
 */
public class NewsAction extends VelocityPortletPaneledAction
{
	private static final long serialVersionUID = 1L;

	private static ResourceLoader rb = new ResourceLoader("news");

	/** portlet configuration parameter names. */
	protected static final String PARAM_CHANNEL_URL = "channel-url";

	/** state attribute names. */
	private static final String STATE_CHANNEL_TITLE = "channelTitle";

	protected static final String STATE_CHANNEL_URL = "channelUrl";
	
	private static final String STATE_PAGE_TITLE = "pageTitle";

	/** names of form fields for options panel. */
	private static final String FORM_CHANNEL_TITLE = "title-of-channel";
	
	private static final String FORM_CHANNEL_URL = "address-of-channel";
	
	private static final String FORM_PAGE_TITLE = "title-of-page";

	/** State and init and context names for text options. */
	private static final String GRAPHIC_VERSION_TEXT = "graphic_version";

	private static final String FULL_STORY_TEXT = "full_story";

	/** Basic feed access event. */
	private static final String FEED_ACCESS = "news.read";
	
	/** Basic feed update event. */
	private static final String FEED_UPDATE = "news.revise";
	
	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		PortletConfig config = portlet.getPortletConfig();

		// detect that we have not done this, yet
		if (state.getAttribute(STATE_CHANNEL_TITLE) == null)
		{
			state.setAttribute(STATE_CHANNEL_TITLE, config.getTitle());

			String channelUrl = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL_URL));
			if (channelUrl == null)
			{
				channelUrl = "";
			}
			state.setAttribute(STATE_CHANNEL_URL, channelUrl);

		}
		
		if (state.getAttribute(STATE_PAGE_TITLE) == null)
		{
			SitePage p = SiteService.findPage(getCurrentSitePageId());
			state.setAttribute(STATE_PAGE_TITLE, p.getTitle());
		}

		if (state.getAttribute(GRAPHIC_VERSION_TEXT) == null)
		{
			state.setAttribute(GRAPHIC_VERSION_TEXT, config.getInitParameter(GRAPHIC_VERSION_TEXT));
		}

		if (state.getAttribute(FULL_STORY_TEXT) == null)
		{
			state.setAttribute(FULL_STORY_TEXT, config.getInitParameter(FULL_STORY_TEXT));
		}

		if (state.getAttribute(STATE_ACTION) == null)
		{
			state.setAttribute(STATE_ACTION, "NewsAction");
		}

	} // initState

	/**
	 * build the context for the Main (Layout) panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// // if we are in edit permissions...
		// String helperMode = (String) state.getAttribute(PermissionsAction.STATE_MODE);
		// if (helperMode != null)
		// {
		// String template = PermissionsAction.buildHelperContext(portlet, context, rundata, state);
		// if (template == null)
		// {
		// addAlert(state, rb.getString("theisone"));
		// }
		// else
		// {
		// return template;
		// }
		// }

		context.put("tlang", rb);

		String mode = (String) state.getAttribute(STATE_MODE);
		if (MODE_OPTIONS.equals(mode))
		{
			return buildOptionsPanelContext(portlet, context, rundata, state);
		}

		context.put(GRAPHIC_VERSION_TEXT, state.getAttribute(GRAPHIC_VERSION_TEXT));
		context.put(FULL_STORY_TEXT, state.getAttribute(FULL_STORY_TEXT));

		// build the menu
		Menu bar = new MenuImpl(portlet, rundata, (String) state.getAttribute(STATE_ACTION));

		// add options if allowed
		addOptionsMenu(bar, (JetspeedRunData) rundata);
		if (!bar.getItems().isEmpty())
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}
		
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		context.put(GRAPHIC_VERSION_TEXT, state.getAttribute(GRAPHIC_VERSION_TEXT));
		context.put(FULL_STORY_TEXT, state.getAttribute(FULL_STORY_TEXT));

		String url = (String) state.getAttribute(STATE_CHANNEL_URL);

		NewsChannel channel = null;
		List items = new Vector();		
		
		try
		{
			channel = NewsService.getChannel(url);
			items = NewsService.getNewsitems(url);
		}
		catch (NewsConnectionException e)
		{
			// display message
			addAlert(state, rb.getString("unavailable") + "\n\n[" + e.getLocalizedMessage() + "]");
		}
		catch (NewsFormatException e)
		{
			// display message
			addAlert(state, rb.getString("unavailable") + "\n\n[" + e.getLocalizedMessage() + "]");
		}
		catch (Exception e)
		{
			// display message
			addAlert(state, rb.getString("unavailable") + "\n\n[" + e.getLocalizedMessage() + "]");
		}

		context.put("channel", channel);
		context.put("news_items", items);
 
		try 
		{
			EventTrackingService.post(EventTrackingService.newEvent(FEED_ACCESS, "/news/site/" +
				SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getId() +
				"/placement/" + SessionManager.getCurrentToolSession().getPlacementId(), false));
			
		} 
		catch (IdUnusedException e)
		{
			//should NEVER actually happen
			if (Log.getLogger("chef").isDebugEnabled())
			{
				Log.debug("chef", "failed to log news access event due to invalid siteId");
			}
		}
		
		return (String) getContext(rundata).get("template") + "-Layout";

	} // buildMainPanelContext

	/**
	 * Setup for the options panel.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// provide "filter_type_form" with form field name for selecting a message filter
		context.put("formfield_channel_title", FORM_CHANNEL_TITLE);

		// provide "filter_type" with the current default value for filtering messages
		context.put("current_channel_title", (String) state.getAttribute(STATE_CHANNEL_TITLE));

		// provide "filter_type_form" with form field name for selecting a message filter
		context.put("formfield_channel_url", FORM_CHANNEL_URL);

		// provide "filter_type" with the current default value for filtering messages
		context.put("current_channel_url", (String) state.getAttribute(STATE_CHANNEL_URL));

		// provide "filter_type_form" with form field name for selecting a message filter
		context.put("formfield_page_title", FORM_PAGE_TITLE);

		// provide "filter_type" with the current default value for filtering messages
		context.put("current_page_title", (String) state.getAttribute(STATE_PAGE_TITLE));
		
		SitePage p = SiteService.findPage(getCurrentSitePageId());
		if (p.getTools() != null && p.getTools().size() == 1)
		{
			// if this is the only tool on that page, display the input for the page's title
			context.put ("pageTitleEditable", Boolean.TRUE);
		}
		
		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		context.put("form-submit", BUTTON + "doUpdate");
		context.put("form-cancel", BUTTON + "doCancel");

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(rundata).get("template");

		return template + "-customize";

	} // buildOptionsPanelContext

	/**
	 * Handle a user clicking the "Done" button in the Options panel
	 */
	public void doUpdate(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String newChannelTitle = data.getParameters().getString(FORM_CHANNEL_TITLE);
		String currentChannelTitle = (String) state.getAttribute(STATE_CHANNEL_TITLE);

		if (StringUtil.trimToNull(newChannelTitle) == null) 
		{
			//TODO: add more verbose message; requires language pack addition
			addAlert(state, rb.getString("cus.franam"));
			return;			
		}
		else if (!newChannelTitle.equals(currentChannelTitle))
		{
			state.setAttribute(STATE_CHANNEL_TITLE, newChannelTitle);
			if (Log.getLogger("chef").isDebugEnabled())
				Log.debug("chef", this + ".doUpdate(): newChannelTitle: " + newChannelTitle);

			// update the tool config
			Placement placement = ToolManager.getCurrentPlacement();
			placement.setTitle(newChannelTitle);

			// deliver an update to the title panel (to show the new title)
			String titleId = titlePanelUpdateId(peid);
			schedulePeerFrameRefresh(titleId);
		}
		
		String newPageTitle = data.getParameters().getString(FORM_PAGE_TITLE);
		String currentPageTitle = (String) state.getAttribute(STATE_PAGE_TITLE);
		
		SitePage p = SiteService.findPage(getCurrentSitePageId());
		// if the news tool is the only tool on the page, then we can edit the page title
		if (p.getTools() != null && p.getTools().size() == 1)
		{
			if (StringUtil.trimToNull(newPageTitle) == null)
			{
				//TODO: add more verbose message; requires language pack addition
				addAlert(state, rb.getString("cus.pagnam"));
				return;
			}
			else if (!newPageTitle.equals(currentPageTitle))
			{
				// if this is the only tool on that page, update the page's title also
				try
				{
					// TODO: save site page title? -ggolden
					Site sEdit = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
					SitePage pEdit = sEdit.getPage(p.getId());
					pEdit.setTitle(newPageTitle);
					SiteService.save(sEdit);
					state.setAttribute(STATE_PAGE_TITLE, newPageTitle);
				}
				catch (PermissionException e) 
				{
					if(Log.getLogger("chef").isDebugEnabled()) {
						Log.debug("chef", " Caught Exception " + e + " user doesn't seem to have " +
							"rights to update site: " + ToolManager.getCurrentPlacement().getContext());
					}
				}
				catch (Exception e)
				{	
					//Probably will never happen unless the ToolManager returns bogus Site or null
					if(Log.getLogger("chef").isDebugEnabled()) {
						Log.debug("chef", "NewsAction.doUpdate() caught Exception " + e);
					}
				} 
			}
		}

		String newChannelUrl = data.getParameters().getString(FORM_CHANNEL_URL);
		String currentChannelUrl = (String) state.getAttribute(STATE_CHANNEL_URL);

		if (newChannelUrl == null && currentChannelUrl == null)
		{
			// return to options panel with message %%%%%%%%%%%%
			addAlert(state, rb.getString("plepro"));
			return;

		}

		if (newChannelUrl != null)
		{
			state.setAttribute(STATE_CHANNEL_URL, newChannelUrl);
			try
			{
				URL url = new URL(newChannelUrl);
				NewsService.getChannel(url.toExternalForm());

				if (!newChannelUrl.equals(currentChannelUrl)) {
					if (Log.getLogger("chef").isDebugEnabled())
						Log.debug("chef", this + ".doUpdate(): newChannelUrl: " + newChannelUrl);
					state.setAttribute(STATE_CHANNEL_URL, url.toExternalForm());
	
					// update the tool config
					Placement placement = ToolManager.getCurrentPlacement();
					placement.getPlacementConfig().setProperty(PARAM_CHANNEL_URL, url.toExternalForm());

				}
			}
			catch (NewsConnectionException e)
			{
				// display message
				addAlert(state, newChannelUrl + " " + rb.getString("invalidfeed"));
				return;
			}
			catch (NewsFormatException e)
			{
				// display message
				addAlert(state, newChannelUrl + " " + rb.getString("invalidfeed"));
				return;
			}
			catch (Exception e)
			{
				// display message
				addAlert(state, newChannelUrl + " " + rb.getString("invalidfeed"));
				return;
			}

			try 
			{
				EventTrackingService.post(EventTrackingService.newEvent(FEED_UPDATE, "/news/site/" +
					SiteService.getSite(ToolManager.getCurrentPlacement().getContext()).getId() +
					"/placement/" + SessionManager.getCurrentToolSession().getPlacementId(), true));
				
			} 
			catch (IdUnusedException e)
			{
				//should NEVER actually happen
				if (Log.getLogger("chef").isDebugEnabled())
				{
					Log.debug("chef", "failed to log news update event due to invalid siteId");
				}
			}
		}

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);

		// re-enable auto-updates when leaving options
		enableObservers(state);

		// commit the change
		saveOptions();

		// refresh the whole page, title may have changed
		scheduleTopRefresh();

	} // doUpdate

	/**
	 * Handle a user clicking the "Done" button in the Options panel
	 */
	public void doCancel(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);
		state.removeAttribute(STATE_CHANNEL_URL);
		state.removeAttribute(STATE_CHANNEL_TITLE);

		// re-enable auto-updates when leaving options
		enableObservers(state);

		// cancel the options
		cancelOptions();

	} // doCancel

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
}
