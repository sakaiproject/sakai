/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
	private static ResourceLoader rb = new ResourceLoader("news");

	/** names and values of request parameters to select sub-panels */
	private static final String MONITOR_PANEL = "List";

	private static final String CONTROL_PANEL = "Control";

	/** portlet configuration parameter names. */
	protected static final String PARAM_CHANNEL_URL = "channel-url";

	/** state attribute names. */
	private static final String STATE_CHANNEL_TITLE = "channelTitle";

	protected static final String STATE_CHANNEL_URL = "channelUrl";

	/** names of form fields for options panel. */
	private static final String FORM_CHANNEL_TITLE = "title-of-channel";

	private static final String FORM_CHANNEL_URL = "address-of-channel";

	/** State and init and context names for text options. */
	private static final String GRAPHIC_VERSION_TEXT = "graphic_version";

	private static final String FULL_STORY_TEXT = "full_story";

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
			else
			{
				try
				{
					NewsChannel ch = NewsService.getChannel(channelUrl);
				}
				catch (NewsConnectionException e)
				{
					// display message
					addAlert(state, e.getMessage());
					return;
				}
				catch (NewsFormatException e)
				{
					// deal with it...
					addAlert(state, e.getMessage());
					return;
				}
				catch (Exception e)
				{
					// ??
				}
			}
			state.setAttribute(STATE_CHANNEL_URL, channelUrl);

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
	 * Setup our observer to be watching for change events for our channel.
	 * 
	 * @param peid
	 *        The portlet id.
	 */
	private void updateObservationOfChannel(SessionState state, String peid)
	{

	} // updateObservationOfChannel

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

		String mode = (String) state.getAttribute(STATE_MODE);
		if (MODE_OPTIONS.equals(mode))
		{
			return buildOptionsPanelContext(portlet, context, rundata, state);
		}

		context.put("panel-control", CONTROL_PANEL);
		context.put("panel-monitor", MONITOR_PANEL);

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

		String url = (String) state.getAttribute(STATE_CHANNEL_URL);

		NewsChannel channel = null;
		try
		{
			channel = NewsService.getChannel(url);
		}
		catch (NewsConnectionException e)
		{
			// display message
			addAlert(state, e.getMessage());
		}
		catch (NewsFormatException e)
		{
			// display message
			addAlert(state, e.getMessage());
		}
		catch (Exception e)
		{
			// display message
			addAlert(state, e.getMessage());
		}

		context.put("channel", channel);

		return (String) getContext(rundata).get("template") + "-Layout";

	} // buildMainPanelContext

	/**
	 * build the context for the List panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildListPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// put this pannel's name for the return url
		context.put("panel-control", CONTROL_PANEL);
		context.put("panel-monitor", MONITOR_PANEL);

		context.put(GRAPHIC_VERSION_TEXT, state.getAttribute(GRAPHIC_VERSION_TEXT));
		context.put(FULL_STORY_TEXT, state.getAttribute(FULL_STORY_TEXT));

		String channel = (String) state.getAttribute(STATE_CHANNEL_URL);

		List items = new Vector();
		try
		{
			items = NewsService.getNewsitems(channel);
		}
		catch (NewsConnectionException e)
		{
			// display message
			addAlert(state, e.getMessage());
		}
		catch (NewsFormatException e)
		{
			// display message
			addAlert(state, e.getMessage());
		}
		catch (Exception e)
		{
			// display message
			addAlert(state, e.getMessage());
		}

		context.put("news_items", items.iterator());

		return (String) getContext(rundata).get("template") + "-List";

	} // buildListPanelContext

	/**
	 * build the context for the Control panel (has a send field)
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildControlPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// put this pannel's name for the return url
		context.put("panel-control", CONTROL_PANEL);
		context.put("panel-monitor", MONITOR_PANEL);

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		// set the form field name for the send button
		context.put("form-submit", BUTTON + "doGet_update");

		// is an update available?
		boolean updateAvailable = false;

		context.put("update_available", new Boolean(updateAvailable));

		return (String) getContext(rundata).get("template") + "-Control";
		// return null;

	} // buildControlPanelContext

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

		if (newChannelTitle != null && !newChannelTitle.equals(currentChannelTitle))
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

			SitePage p = SiteService.findPage(getCurrentSitePageId());
			if (p.getTools() != null && p.getTools().size() == 1)
			{
				// if this is the only tool on that page, update the page's title also
				try
				{
					// TODO: save site page title? -ggolden
					Site sEdit = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
					SitePage pEdit = sEdit.getPage(p.getId());
					pEdit.setTitle(newChannelTitle);
					SiteService.save(sEdit);
				}
				catch (Exception ignore)
				{
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

		if (newChannelUrl != null && !newChannelUrl.equals(currentChannelUrl))
		{
			state.setAttribute(STATE_CHANNEL_URL, newChannelUrl);
			try
			{
				URL url = new URL(newChannelUrl);
				NewsService.getChannel(url.toExternalForm());

				if (Log.getLogger("chef").isDebugEnabled())
					Log.debug("chef", this + ".doUpdate(): newChannelUrl: " + newChannelUrl);
				state.setAttribute(STATE_CHANNEL_URL, url.toExternalForm());

				// update the tool config
				Placement placement = ToolManager.getCurrentPlacement();
				placement.getPlacementConfig().setProperty(PARAM_CHANNEL_URL, url.toExternalForm());
			}
			catch (NewsConnectionException e)
			{
				// display message
				addAlert(state, newChannelUrl + rb.getString("invalidfeed"));
				return;
			}
			catch (NewsFormatException e)
			{
				// display message
				addAlert(state, newChannelUrl + rb.getString("invalidfeed"));
				return;
			}
			catch (Exception e)
			{
				// display message
				addAlert(state, newChannelUrl + " " + rb.getString("invalidfeed"));
				return;
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
