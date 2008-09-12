/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 Sakai Foundation
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

package org.sakaiproject.chat.tool;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.chat.api.ChatChannel;
import org.sakaiproject.chat.api.ChatChannelEdit;
import org.sakaiproject.chat.api.ChatMessage;
import org.sakaiproject.chat.api.ChatMessageEdit;
import org.sakaiproject.chat.cover.ChatService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.presence.cover.PresenceService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.PresenceObservingCourier;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * <p>
 * ChatAction is the Sakai chat tool.
 * </p>
 */
public class ChatAction extends VelocityPortletPaneledAction
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(ChatAction.class);

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("chat");

	private static final String MODE_CONFIRM_DELETE_MESSAGE = "confirmmdeletemessage";

	/** portlet configuration parameter names. */
	private static final String PARAM_CHANNEL = "channel";

	private static final String PARAM_DISPLAY_DATE = "display-date";

	private static final String PARAM_DISPLAY_TIME = "display-time";

	private static final String PARAM_DISPLAY_USER = "display-user";

	private static final String PARAM_SOUND_ALERT = "sound-alert";

	private static final String PARAM_MEMBER_FILTER = "member-filter";

	private static final String PARAM_FILTER_TYPE = "filter-type";

	private static final String PARAM_FILTER_PARAM = "filter-param";

	/** Configure form field names. */
	private static final String FORM_CHANNEL = "channel";

	private static final String FORM_NEW_CHANNEL = "new-channel";

	private static final String FORM_FILTER_TYPE = "filter-type";

	private static final String FORM_FILTER_PARAM_DAYS = "filter-param-days";

	private static final String FORM_FILTER_PARAM_NUMBER = "filter-param-number";

	/** Message filter names */
	private static final String FILTER_BY_NUMBER = "SelectMessagesByNumber";

	private static final String FILTER_BY_TIME = "SelectMessagesByTime";

	private static final String FILTER_TODAY = "SelectTodaysMessages";

	private static final String FILTER_ALL = "SelectAllMessages";

	private static final String[] ALL_FILTERS = { FILTER_BY_NUMBER, FILTER_BY_TIME, FILTER_TODAY, FILTER_ALL };

	/** Default values to use in case of input errors */
	private static final int DEFAULT_PARAM = 0;

	private static final int DEFAULT_DAYS = 3;

	private static final int DEFAULT_MSGS = 12;

	/** Control form field names. */
	private static final String FORM_MESSAGE = "message";

	/** names and values of request parameters to select sub-panels */
	private static final String MONITOR_PANEL = "List";

	private static final String CONTROL_PANEL = "Control";

	private static final String PRESENCE_PANEL = "Presence";

	private static final String TOOLBAR_PANEL = "Toolbar";

	/** state attribute names. */
	private static final String STATE_CHANNEL_REF = "channelId";

	private static final String STATE_SITE = "siteId";

	private static final String STATE_DISPLAY_DATE = "display-date";

	private static final String STATE_DISPLAY_TIME = "display-time";

	private static final String STATE_DISPLAY_USER = "display-user";

	private static final String STATE_SOUND_ALERT = "sound-alert";

	private static final String STATE_UPDATE = "update";

	private static final String STATE_CHANNEL_PROBLEM = "channel-problem";

	private static final String STATE_FILTER_TYPE = "filter-type";

	private static final String STATE_FILTER_PARAM = "filter-param";

	private static final String STATE_MESSAGE_FILTER = "message-filter";

	private static final String STATE_MORE_SELECTED = "more-selected";

	private static final String STATE_MORE_MESSAGES_LABEL = "more-messages-label";

	private static final String STATE_MORE_MESSAGES_FILTER = "more-messages-filter";

	private static final String STATE_FEWER_MESSAGES_LABEL = "fewer-messages-label";

	private static final String STATE_FEWER_MESSAGES_FILTER = "fewer-messages-filter";

	private static final String STATE_BROWSER = "browser";

	private static final String STATE_CHAT_PRESENCE_OBSERVER = STATE_OBSERVER2;

	private static final String STATE_COLOR_MAPPER = "color-mapper";

	private static final String STATE_MAIN_MESSAGE = "message-for-chat-layout";

	private static final String NEW_CHAT_CHANNEL = "new-chat-channel";

	/** Resource property on the message indicating that the message had been deleted */
	private static final String PROPERTY_MESSAGE_DELETED = "deleted";

	private static final String TIME_DATE_SELECT = "selected-time-date-display";

	/** State attribute set when we need to go into permissions mode. */
	private static final String STATE_PERMISSIONS = "sakai:chat:permissions";

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		// detect that we have not done this, yet
		if (state.getAttribute(STATE_CHANNEL_REF) == null)
		{
			PortletConfig config = portlet.getPortletConfig();

			// read the channel from configuration, or, if not specified, use the default for the page
			String channel = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
			}

			state.setAttribute(STATE_CHANNEL_REF, channel);

			if (state.getAttribute(STATE_DISPLAY_DATE) == null)
			{
				state.setAttribute(STATE_DISPLAY_DATE, new Boolean(config.getInitParameter(PARAM_DISPLAY_DATE)));
			}

			if (state.getAttribute(STATE_DISPLAY_TIME) == null)
			{
				state.setAttribute(STATE_DISPLAY_TIME, new Boolean(config.getInitParameter(PARAM_DISPLAY_TIME)));
			}

			if (state.getAttribute(STATE_DISPLAY_USER) == null)
			{
				state.setAttribute(STATE_DISPLAY_USER, new Boolean(config.getInitParameter(PARAM_DISPLAY_USER)));
			}

			if (state.getAttribute(STATE_SOUND_ALERT) == null)
			{
				state.setAttribute(STATE_SOUND_ALERT, new Boolean(config.getInitParameter(PARAM_SOUND_ALERT)));
			}

			if (state.getAttribute(STATE_COLOR_MAPPER) == null)
			{
				ColorMapper mapper = new ColorMapper();

				// always set this user's color to first color (red)
				mapper.getColor(StringUtil.trimToZero(SessionManager.getCurrentSessionUserId()));

				state.setAttribute(STATE_COLOR_MAPPER, mapper);
			}

			if (state.getAttribute(STATE_FILTER_TYPE) == null)
			{
				String filter_type = config.getInitParameter(PARAM_FILTER_TYPE, FILTER_BY_TIME);
				String filter_param = config.getInitParameter(PARAM_FILTER_PARAM, String.valueOf(DEFAULT_DAYS));

				updateMessageFilters(state, filter_type, filter_param);
			}

			// the event resource reference pattern to watch for
			// setup the observer to notify our MONITOR_PANEL panel (inside the Main panel)
			if (state.getAttribute(STATE_OBSERVER) == null)
			{
				// get the current tool placement
				Placement placement = ToolManager.getCurrentPlacement();

				// location is just placement
				String location = placement.getId();

				// the html element to update on delivery
				String elementId = MONITOR_PANEL;
				Reference r = EntityManager.newReference(channel);
				String pattern = ChatService.messageReference(r.getContext(), r.getId(), "");
				boolean wantsBeeps = ((Boolean) state.getAttribute(STATE_SOUND_ALERT)).booleanValue();

				state.setAttribute(STATE_OBSERVER, new ChatObservingCourier(location, elementId, pattern, wantsBeeps));
			}

			// the event resource reference pattern to watch for
			// setup the observer to notify our PRESENCE_PANEL panel (inside the Main panel)
			if (state.getAttribute(STATE_CHAT_PRESENCE_OBSERVER) == null)
			{
				// get the current tool placement
				Placement placement = ToolManager.getCurrentPlacement();

				// location is just placement
				String location = placement.getId();

				// the html element to update on delivery
				String elementId = PRESENCE_PANEL;

				// setup an observer to notify us when presence at this location changes
				PresenceObservingCourier observer = new PresenceObservingCourier(location, elementId);

				state.setAttribute(STATE_CHAT_PRESENCE_OBSERVER, observer);
			}
		}
		// repopulate state object and title bar when default chat room changes
		else
		{
			PortletConfig config = portlet.getPortletConfig();
			// read the channel from configuration, or, if not specified, use the default for the page
			String channel = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
			}
			state.setAttribute(STATE_CHANNEL_REF, channel);
			String channelName = rundata.getParameters().getString(FORM_CHANNEL);
			// update the tool config
			Placement placement = ToolManager.getCurrentPlacement();
			placement.setTitle(rb.getString("chatroom") + "\"" + channelName + "\"");
		}
		// make sure the observer is in sync with state
		updateObservationOfChannel(state, portlet.getID());

	} // initState

	/**
	 * Setup our observer to be watching for change events for our channel.
	 * 
	 * @param peid
	 *        The portlet id.
	 */
	private void updateObservationOfChannel(SessionState state, String peid)
	{
		// make sure the pattern matches the channel we are looking at
		String channel = (String) state.getAttribute(STATE_CHANNEL_REF);
		Reference r = EntityManager.newReference(channel);
		String pattern = ChatService.messageReference(r.getContext(), r.getId(), "");

		// update the observer looking for new messages
		ChatObservingCourier observer1 = (ChatObservingCourier) state.getAttribute(STATE_OBSERVER);
		observer1.setResourcePattern(pattern);

	} // updateObservationOfChannel

	/**
	 * Get the channel from ChatService or create it.
	 */
	private ChatChannel getChannel(SessionState state, String name)
	{
		// deal with the channel not yet existing
		ChatChannel channel = null;
		try
		{
			channel = ChatService.getChatChannel(name);
		}
		catch (IdUnusedException ignore)
		{
		}
		catch (PermissionException ignore)
		{
		}

		if ((channel == null) && (state.getAttribute(STATE_CHANNEL_PROBLEM) == null))
		{
			// create the channel
			try
			{
				ChatChannelEdit edit = ChatService.addChatChannel(name);
				ChatService.commitChannel(edit);
				channel = edit;
			}
			catch (IdUsedException e)
			{
				// strange, the channel already exists!
				try
				{
					channel = ChatService.getChatChannel(name);
				}
				catch (IdUnusedException ignore)
				{
				}
				catch (PermissionException ignore)
				{
				}
			}
			catch (IdInvalidException e)
			{
				// stranger, we cannot use this id!
				state.setAttribute(STATE_CHANNEL_PROBLEM, rb.getString("thischat"));
				M_log.warn("doSend(): creating channel: ", e);
			}
			catch (PermissionException e)
			{
				// rats, this user cannot create the channel
				state.setAttribute(STATE_CHANNEL_PROBLEM, rb.getString("youdonot2"));
			}
		}
		return channel;

	} // getChannel

	/**
	 * Update the state message-filtering attributes to use the filtering criteria specified by the filter_type and filter_param parameters.
	 * 
	 * @param state
	 *        The session state.
	 * @param filter_type
	 *        A string specifying the filter type.
	 * @param filter_param
	 *        A string specifying the filter param.
	 */
	private void updateMessageFilters(SessionState state, String filter_type, String filter_param)
	{
		state.setAttribute(STATE_MORE_MESSAGES_LABEL, rb.getString("showall"));
		state.setAttribute(STATE_MORE_MESSAGES_FILTER, new SelectAllMessages());
		state.setAttribute(STATE_FILTER_PARAM, String.valueOf(DEFAULT_PARAM));

		state.setAttribute(STATE_MORE_SELECTED, new Boolean(false));
		state.setAttribute(STATE_FILTER_TYPE, filter_type);
		if (filter_type.equals(FILTER_ALL))
		{
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByTime(DEFAULT_DAYS));
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showpast") + " " + DEFAULT_DAYS + " "
					+ rb.getString("days"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_MORE_MESSAGES_FILTER));
			state.setAttribute(STATE_MORE_SELECTED, new Boolean(true));
		}
		else if (filter_type.equals(FILTER_TODAY))
		{
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectTodaysMessages());
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showtoday"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else if (filter_type.equals(FILTER_BY_NUMBER))
		{
			int number = DEFAULT_MSGS;
			try
			{
				number = Integer.parseInt(filter_param);
				if (number <= 0)
				{
					throw new Exception();
				}
				state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByNumber(number));
				state.setAttribute(STATE_FILTER_PARAM, filter_param);
			}
			catch (Exception e)
			{
				// M_log.warn("updateMessageFilters() invalid param: " );
			}
			state.setAttribute(STATE_FILTER_PARAM, String.valueOf(number));
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByNumber(number));
			state
					.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showlast") + " " + number + " "
							+ rb.getString("messages"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else if (filter_type.equals(FILTER_BY_TIME))
		{
			int number = DEFAULT_DAYS;
			try
			{
				number = Integer.parseInt(filter_param);
				if (number <= 0)
				{
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				// M_log.warn("updateMessageFilters() invalid param: " );
			}
			state.setAttribute(STATE_FILTER_PARAM, String.valueOf(number));
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByTime(number));
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showpast") + " " + number + " " + rb.getString("days"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else
		{
			state.setAttribute(STATE_FILTER_PARAM, String.valueOf(DEFAULT_DAYS));
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByTime(DEFAULT_DAYS));
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showpast") + " " + DEFAULT_DAYS + " "
					+ rb.getString("days"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}

	} // updateMessageFilters

	/**
	 * build the context for the Main (Layout) panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// if there's an alert message specifically targetted for main fraim, display it
		String msg = (String) state.getAttribute(STATE_MAIN_MESSAGE);
		context.put("alertMessage", msg);
		state.removeAttribute(STATE_MAIN_MESSAGE);

		String mode = (String) state.getAttribute(STATE_MODE);
		if (MODE_OPTIONS.equals(mode))
		{
			return buildOptionsPanelContext(portlet, context, rundata, state);
		}
		else if (MODE_CONFIRM_DELETE_MESSAGE.equals(mode))
		{
			return buildConfirmDeleteMessagePanelContext(portlet, context, rundata, state);
		}

		List focus_elements = new Vector();
		focus_elements.add(CONTROL_PANEL);
		focus_elements.add(FORM_MESSAGE);

		context.put("focus_path", focus_elements);

		context.put("panel-control", CONTROL_PANEL);
		context.put("panel-monitor", MONITOR_PANEL);
		context.put("panel-presence", PRESENCE_PANEL);
		context.put("panel-toolbar", TOOLBAR_PANEL);

		// the url for the chat courier, using a quick 10 second refresh
		setVmCourier(rundata.getRequest(), 10);

		return (String) getContext(rundata).get("template") + "-Layout";

	} // buildLayoutPanelContext

	/**
	 * Handle a user clicking on the view-date menu.
	 */
	public void doToggle_date_display(RunData runData, Context context)
	{
		toggleState(runData, STATE_DISPLAY_DATE);

		// schedule a refresh of the monitor panel
		String peid = ((JetspeedRunData) runData).getJs_peid();
		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_date_display

	/**
	 * Handle a user clicking on the view-time menu.
	 */
	public void doToggle_time_display(RunData runData, Context context)
	{
		toggleState(runData, STATE_DISPLAY_TIME);

		// schedule a refresh of the monitor panel
		String peid = ((JetspeedRunData) runData).getJs_peid();
		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_time_display

	/**
	 * Handle a user clicking on the time-date dropdown.
	 */
	public void doChange_time_date_display(RunData runData, Context context)
	{
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		String time_date = runData.getParameters().getString("changeView");

		boolean oldTime = ((Boolean) state.getAttribute(STATE_DISPLAY_TIME)).booleanValue();
		boolean oldDate = ((Boolean) state.getAttribute(STATE_DISPLAY_DATE)).booleanValue();

		if (time_date.equals(rb.getString("bar.onlytime")))
		{
			// if the time is not shown, toggle to show it
			if (!oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is being shown, toggle to hide it
			if (oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}
		else if (time_date.equals(rb.getString("bar.datetime")))
		{
			// if the time is not shown, toggle to show it
			if (!oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is not shown, toggle to show it
			if (!oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}
		else if (time_date.equals(rb.getString("bar.onlydate")))
		{
			// if the time is being shown, toggle to hide it
			if (oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is not shown, toggle to show it
			if (!oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}
		else if (time_date.equals(rb.getString("bar.nodatetime")))
		{
			// if the time is being shown, toggle to hide it
			if (oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is being shown, toggle to hide it
			if (oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}

		state.setAttribute(TIME_DATE_SELECT, time_date);

		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doChange_time_date_display

	/**
	 * Handle a user clicking on the sound-alert button.
	 */
	public void doToggle_sound_alert(RunData runData, Context context)
	{
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// toggle the state setting
		boolean newValue = !((Boolean) state.getAttribute(STATE_SOUND_ALERT)).booleanValue();
		state.setAttribute(STATE_SOUND_ALERT, new Boolean(newValue));
		ChatObservingCourier observer = (ChatObservingCourier) state.getAttribute(STATE_OBSERVER);
		observer.alertEnabled(newValue);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_sound_alert

	/**
	 * Toggle the state attribute
	 * 
	 * @param stateName
	 *        The name of the state attribute to toggle
	 */
	private void toggleState(RunData runData, String stateName)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// toggle the state setting
		boolean newValue = !((Boolean) state.getAttribute(stateName)).booleanValue();
		state.setAttribute(stateName, new Boolean(newValue));

	} // toggleState

	/**
	 * Handle a user clicking on the show-all/show-some button.
	 */
	public void doToggle_filter(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// toggle the filter setting between show-more and show-fewer
		if (((Boolean) state.getAttribute(STATE_MORE_SELECTED)).booleanValue())
		{
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else
		{
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_MORE_MESSAGES_FILTER));
		}
		toggleState(runData, STATE_MORE_SELECTED);

		if (((String) state.getAttribute(STATE_FILTER_TYPE)).equals(FILTER_BY_NUMBER))
		{
			if (!((Boolean) state.getAttribute(STATE_MORE_SELECTED)).booleanValue())
			{
				try
				{
					int number = Integer.parseInt((String) state.getAttribute(STATE_FILTER_PARAM));
					state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByNumber(number));
					state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		// schedule a refresh of the monitor panel
		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_filter

	/**
	 * build the context for the Toolbar panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildToolbarPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// we might be on the way to a permissions...
		if (state.getAttribute(STATE_PERMISSIONS) != null)
		{
			state.removeAttribute(STATE_PERMISSIONS);
			doPermissionsNow(rundata, context);
		}

		context.put("tlang", rb);
		// build the menu
		Menu bar = new MenuImpl(portlet, rundata, (String) state.getAttribute(STATE_ACTION));
		/*
		 * boolean displayDate = ((Boolean)state.getAttribute(STATE_DISPLAY_DATE)).booleanValue(); bar.add( new MenuEntry((displayDate ? rb.getString("hided") + " " : " " + rb.getString("showd")), null, true, (displayDate ? MenuItem.CHECKED_TRUE :
		 * MenuItem.CHECKED_FALSE), "doToggle_date_display") ); boolean displayTime = ((Boolean)state.getAttribute(STATE_DISPLAY_TIME)).booleanValue(); bar.add( new MenuEntry((displayTime ? rb.getString("hidet") + " " : " " + rb.getString("showt")), null,
		 * true, (displayTime ? MenuItem.CHECKED_TRUE : MenuItem.CHECKED_FALSE), "doToggle_time_display") );
		 */
		context.put("selectedView", state.getAttribute(TIME_DATE_SELECT));

		// if the java beep is disabled, don't offer the alert
		if (ServerConfigurationService.getBoolean("java.beep", false))
		{
			boolean soundAlert = ((Boolean) state.getAttribute(STATE_SOUND_ALERT)).booleanValue();
			bar.add(new MenuEntry((soundAlert ? rb.getString("turnoff") + " " : " " + rb.getString("turnon")), null, true,
					(soundAlert ? MenuItem.CHECKED_TRUE : MenuItem.CHECKED_FALSE), "doToggle_sound_alert"));
		}

		boolean moreSelected = ((Boolean) state.getAttribute(STATE_MORE_SELECTED)).booleanValue();
		/*
		 * bar.add( new MenuEntry((moreSelected ? (String)state.getAttribute(STATE_FEWER_MESSAGES_LABEL) : (String)state.getAttribute(STATE_MORE_MESSAGES_LABEL)), null, true, (moreSelected ? MenuItem.CHECKED_TRUE : MenuItem.CHECKED_FALSE),
		 * "doToggle_filter") );
		 */
		String pastLabel = "";
		String fewerLabel = (String) state.getAttribute(STATE_FEWER_MESSAGES_LABEL);
		String moreLabel = (String) state.getAttribute(STATE_MORE_MESSAGES_LABEL);
		context.put("pastLabel", (moreSelected ? moreLabel : fewerLabel));
		context.put("fewerLabel", fewerLabel);
		context.put("moreLabel", moreLabel);

		// add options if allowed
		addOptionsMenu(bar, (JetspeedRunData) rundata);

		// add permissions, if allowed
		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			bar.add(new MenuEntry(rb.getString("permis"), "doPermissions"));
		}

		context.put(Menu.CONTEXT_MENU, bar);
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		return null; // (String)getContext(rundata).get("template") + "-Toolbar";

	} // buildToolbarPanelContext

	/**
	 * build the context for the List panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildListPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// display info
		context.put("tlang", rb);
		context.put("display_date", state.getAttribute(STATE_DISPLAY_DATE));
		context.put("display_time", state.getAttribute(STATE_DISPLAY_TIME));
		context.put("display_user", state.getAttribute(STATE_DISPLAY_USER));
		context.put("sound_alert", state.getAttribute(STATE_SOUND_ALERT));

		// provide a color mapper to keep track of user color coding for the session
		context.put("color_mapper", (ColorMapper) state.getAttribute(STATE_COLOR_MAPPER));

		// find the channel and get the messages
		try
		{
			ChatFilter filter = (ChatFilter) state.getAttribute(STATE_MESSAGE_FILTER);

			// // TODO: TIMING
			// if (CurrentService.getInThread("DEBUG") == null)
			// CurrentService.setInThread("DEBUG", new StringBuilder());
			// long startTime = System.currentTimeMillis();

			List msgs = ChatService.getMessages((String) state.getAttribute(STATE_CHANNEL_REF), filter.getAfterDate(), filter
					.getLimitedToLatest(), true, // asc
					true, // TODO: inc drafts
					false // not pubview onyl
					);

			// // TODO: TIMING
			// long endTime = System.currentTimeMillis();
			// if (endTime-startTime > /*5*/000)
			// {
			// StringBuilder buf = (StringBuilder) CurrentService.getInThread("DEBUG");
			// if (buf != null)
			// {
			// buf.insert(0,"ChatAction.list: "
			// + state.getAttribute(STATE_CHANNEL_REF)
			// + " time: " + (endTime - startTime));
			// }
			// }

			context.put("chat_messages", msgs);

			// boolean allowed = ChatService.allowRemoveChannel((String)state.getAttribute(STATE_CHANNEL_REF));
			// context.put("allowRemoveMessage", new Boolean(allowed));

			ChatChannel channel = getChannel(state, (String) state.getAttribute(STATE_CHANNEL_REF));
			context.put("channel", channel);

		}
		catch (PermissionException e)
		{
			context.put("alertMessage", rb.getString("youdonot1"));
		}
		catch (Exception e)
		{
			M_log.warn("buildListPanelContext()", e);
		}

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		return null;

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

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		// set the form field name for the send button
		context.put("form-submit", BUTTON + "doSend");

		// set the form field name for the send button
		context.put("form-message", FORM_MESSAGE);

		// is this user going to be able to post (add permission on the channel)
		boolean allowed = ChatService.allowAddChannel((String) state.getAttribute(STATE_CHANNEL_REF));
		if (!allowed)
		{
			context.put("alertMessage", rb.getString("youdonot3")); // %%% or no message?
		}
		context.put("allow-send", new Boolean(allowed));

		return null;

	} // buildControlPanelContext

	/**
	 * build the context for the Presence panel (has a send field)
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildPresencePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		String template = null;

		// get the observer
		PresenceObservingCourier observer = (PresenceObservingCourier) state.getAttribute(STATE_CHAT_PRESENCE_OBSERVER);

		// put into context a list of sessions with chat presence
		String location = observer.getLocation();

		// refresh our presence at the location
		PresenceService.setPresence(location);

		// get the current presence list (User objects) for this page
		List users = PresenceService.getPresentUsers(location);
		context.put("users", users);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		observer.justDelivered();

		return null;

	} // buildPresencePanelContext

	/**
	 * Handle a user posting a new chat message.
	 */
	public void doSend(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// read in the message input
		// %%% JANDERSE - The user enters plaintext, but messages are now stored as formatted text;
		// therefore, the plaintext must be converted to formatted text when it is returned from the browser.
		String message = runData.getParameters().getCleanString(FORM_MESSAGE);
		message = FormattedText.convertPlaintextToFormattedText(message);

		// ignore empty messages
		if ((message == null) || (message.length() == 0)) return;

		// deal with the channel not yet existing
		// TODO: we don't really need to read the channel to post... -ggolden

		// // TODO: TIMING
		// if (CurrentService.getInThread("DEBUG") == null)
		// CurrentService.setInThread("DEBUG", new StringBuilder());
		// long startTime = System.currentTimeMillis();

		ChatChannel channel = getChannel(state, (String) state.getAttribute(STATE_CHANNEL_REF));

		// // TODO: TIMING
		// long endTime = System.currentTimeMillis();
		// if (endTime-startTime > /*5*/000)
		// {
		// StringBuilder buf = (StringBuilder) CurrentService.getInThread("DEBUG");
		// if (buf != null)
		// {
		// buf.insert(0,"ChatAction.doSend: "
		// + state.getAttribute(STATE_CHANNEL_REF)
		// + " time: " + (endTime - startTime));
		// }
		// }

		// post the message
		if (channel != null)
		{
			try
			{
				ChatMessageEdit edit = channel.addChatMessage();
				edit.setBody( Web.cleanHtml(message) );
				channel.commitMessage(edit);
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("youdonot3"));
			}
			catch (Exception e) // %%% why?
			{
				addAlert(state, rb.getString("therewaspro"));
				M_log.warn("doSend()", e);
			}
		}
		else
		{
			addAlert(state, (String) state.getAttribute(STATE_CHANNEL_PROBLEM));
		}

	} // doSend

	/**
	 * Setup for the options panel.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// provide "filter_type" with the current default value for filtering messages
		context.put("filter_type", (String) state.getAttribute(STATE_FILTER_TYPE));

		// provide "filter_type_form" with form field name for selecting a message filter
		context.put("filter_type_form", FORM_FILTER_TYPE);

		// provide "filter_days_param" as current value or default value for number of days
		context.put("filter_days_param", (String) state.getAttribute(STATE_FILTER_PARAM));

		// provide "filter_days_param_form" with form field name for filter parameter (number of days/messages)
		context.put("filter_days_param_form", FORM_FILTER_PARAM_DAYS);

		// provide "filter_param" as current value or default value for number of days/messages
		context.put("filter_number_param", (String) state.getAttribute(STATE_FILTER_PARAM));

		// provide "filter_param_form" with form field name for filter parameter (number of days/messages)
		context.put("filter_number_param_form", FORM_FILTER_PARAM_NUMBER);

		// provide "default_chat_channel" with the dafault channel-id for the user/group
		context.put("default_chat_channel", SiteService.MAIN_CONTAINER);

		// provide "chat_channel" with the current channel's id
		String placementContext = ToolManager.getCurrentPlacement().getContext();
		String defaultChannel = ChatService.channelReference(placementContext, SiteService.MAIN_CONTAINER);
		String sitePrefix = defaultChannel.substring(0, defaultChannel.lastIndexOf(SiteService.MAIN_CONTAINER));
		String currentChannel = ((String) state.getAttribute(STATE_CHANNEL_REF)).substring(sitePrefix.length());
		context.put("chat_channel", currentChannel);

		// provide "chat_channels" as a list of channels belonging to this site

		// // TODO: TIMING
		// if (CurrentService.getInThread("DEBUG") == null)
		// CurrentService.setInThread("DEBUG", new StringBuilder());
		// long startTime = System.currentTimeMillis();

		Iterator aChannel = ChatService.getChannelIds(placementContext).iterator();

		// // TODO: TIMING
		// long endTime = System.currentTimeMillis();
		// if (endTime-startTime > /*5*/000)
		// {
		// StringBuilder buf = (StringBuilder) CurrentService.getInThread("DEBUG");
		// if (buf != null)
		// {
		// buf.insert(0,"ChatAction.options: "
		// + state.getAttribute(STATE_CHANNEL_REF)
		// + " time: " + (endTime - startTime));
		// }
		// }

		List channel_list = new Vector();
		while (aChannel.hasNext())
		{
			String theChannel = (String) aChannel.next();
			if (!theChannel.equals(SiteService.MAIN_CONTAINER) && !theChannel.equals(currentChannel))
			{
				channel_list.add(theChannel);
			}
		}
		context.put("chat_channels", channel_list);

		// provide "new_chat_channel" as flag to create a new channel
		context.put("new_chat_channel", NEW_CHAT_CHANNEL);

		// provide "form_new_channel" with form field name for specifying a new channel name
		context.put("form_new_channel", FORM_NEW_CHANNEL);

		// provide "chat_channel_form" with the form name for the new channel selection field
		context.put("chat_channel_form", FORM_CHANNEL);

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		context.put("form-submit", BUTTON + "doUpdate");
		context.put("form-cancel", BUTTON + "doCancel");

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(rundata).get("template");
		return template + "-customize";

	} // buildOptionsPanelContext

	public String buildConfirmDeleteMessagePanelContext(VelocityPortlet portlet, Context context, RunData rundata,
			SessionState state)
	{
		context.put("tlang", rb);
		// Put the message object into the context (the message that is about to be deleted)
		try
		{
			// String messageRef = ChatService.messageReference(
			// (String) state.getAttribute(STATE_CHANNEL_REF),
			// (String) state.getAttribute("messageid"));
			// Reference msgRef = new Reference(messageRef);

			// // TODO: TIMING
			// if (CurrentService.getInThread("DEBUG") == null)
			// CurrentService.setInThread("DEBUG", new StringBuilder());
			// long startTime = System.currentTimeMillis();

			// Message msg = ChatService.getMessage(msgRef);
			String messageid = (String) state.getAttribute("messageid");
			ChatChannel channel = ChatService.getChatChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			ChatMessage msg = channel.getChatMessage(messageid);

			// // TODO: TIMING
			// long endTime = System.currentTimeMillis();
			// if (endTime-startTime > /*5*/000)
			// {
			// StringBuilder buf = (StringBuilder) CurrentService.getInThread("DEBUG");
			// if (buf != null)
			// {
			// buf.insert(0,"ChatAction.confirmDelete: "
			// + state.getAttribute(STATE_CHANNEL_REF)
			// + " time: " + (endTime - startTime));
			// }
			// }

			context.put("message", msg);
		}
		catch (PermissionException e)
		{
			context.put("alertMessage", rb.getString("youdonot4"));
		}
		catch (IdUnusedException e)
		{
		}
		catch (Exception e)
		{
			M_log.warn("buildConfirmDeleteMessagePanelContext()", e);
		}

		String template = (String) getContext(rundata).get("template");
		return template + "-delete";
	}

	/**
	 * Handle a user clicking the "Done" button in the Options panel
	 */
	public void doUpdate(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String placementContext = ToolManager.getCurrentPlacement().getContext();
		String newChannel = data.getParameters().getString(FORM_CHANNEL);
		String currentChannel = ((String) state.getAttribute(STATE_CHANNEL_REF)).substring(placementContext.length() + 1);

		if (newChannel != null && newChannel.equals(NEW_CHAT_CHANNEL))
		{
			newChannel = data.getParameters().getString(FORM_NEW_CHANNEL);
			// make sure channel name is valid Resource ID (for items entered by user)
			if (!Validator.checkResourceId(newChannel))
			{
				// if name is not valid, save error message and return to Options panel
				addAlert(state, rb.getString("youent") + " \" " + newChannel + " \" " + rb.getString("forchat"));
				return;
			}
		}
		if (newChannel != null && !newChannel.equals(currentChannel))
		{
			state.setAttribute(STATE_CHANNEL_REF, ChatService.channelReference(placementContext, newChannel));
			if (M_log.isDebugEnabled()) M_log.debug("doUpdate(): newChannel: " + newChannel);
			// ChatChannel chan = getChannel(state, newChannel);
			updateObservationOfChannel(state, peid);

			// update the tool config
			Placement placement = ToolManager.getCurrentPlacement();
			placement.getPlacementConfig().setProperty(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF));
			placement.setTitle(rb.getString("chatroom") + " \" " + newChannel + " \" ");

			// deliver an update to the title panel (to show the new title)
			String titleId = titlePanelUpdateId(peid);
			schedulePeerFrameRefresh(titleId);
		}

		// filter
		String filter_type = data.getParameters().getString(FORM_FILTER_TYPE);
		if (filter_type != null)
		{
			if (filter_type.equals(FILTER_ALL))
			{
				if (!filter_type.equals((String) state.getAttribute(STATE_FILTER_TYPE)))
				{
					updateMessageFilters(state, filter_type, null);

					// update the tool config
					Placement placement = ToolManager.getCurrentPlacement();
					placement.getPlacementConfig().setProperty(PARAM_FILTER_TYPE, (String) state.getAttribute(STATE_FILTER_TYPE));
					placement.getPlacementConfig().setProperty(PARAM_FILTER_PARAM, (String) state.getAttribute(STATE_FILTER_PARAM));
				}
			}
			else if (filter_type.equals(FILTER_BY_TIME))
			{
				String filter_days_param = data.getParameters().getString(FORM_FILTER_PARAM_DAYS);
				if (filter_days_param != null)
				{
					if (!filter_type.equals((String) state.getAttribute(STATE_FILTER_TYPE))
							|| !filter_days_param.equals((String) state.getAttribute(STATE_FILTER_PARAM)))
					{
						updateMessageFilters(state, filter_type, filter_days_param);

						// update the tool config
						Placement placement = ToolManager.getCurrentPlacement();
						placement.getPlacementConfig().setProperty(PARAM_FILTER_TYPE,
								(String) state.getAttribute(STATE_FILTER_TYPE));
						placement.getPlacementConfig().setProperty(PARAM_FILTER_PARAM,
								(String) state.getAttribute(STATE_FILTER_PARAM));
					}
				}
			}
			else if (filter_type.equals(FILTER_BY_NUMBER))
			{
				String filter_number_param = data.getParameters().getString(FORM_FILTER_PARAM_NUMBER);
				if (filter_number_param != null)
				{
					if (!filter_type.equals((String) state.getAttribute(STATE_FILTER_TYPE))
							|| !filter_number_param.equals((String) state.getAttribute(STATE_FILTER_PARAM)))
					{
						updateMessageFilters(state, filter_type, filter_number_param);

						// update the tool config
						Placement placement = ToolManager.getCurrentPlacement();
						placement.getPlacementConfig().setProperty(PARAM_FILTER_TYPE,
								(String) state.getAttribute(STATE_FILTER_TYPE));
						placement.getPlacementConfig().setProperty(PARAM_FILTER_PARAM,
								(String) state.getAttribute(STATE_FILTER_PARAM));
					}
				}
			}
		}

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);

		// re-enable auto-updates when leaving options
		enableObservers(state);

		// commit the change
		saveOptions();

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

		// re-enable auto-updates when leaving options
		enableObservers(state);

		// cancel the options
		cancelOptions();

	} // doCancel

	/**
	 * Handle a user deleting a message - put up a confirmation page
	 */
	public void doConfirmDeleteMessage(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String messageid = data.getParameters().getString("messageid");

		state.setAttribute("messageid", messageid);
		state.setAttribute(STATE_MODE, MODE_CONFIRM_DELETE_MESSAGE);

		// schedule a main refresh
		schedulePeerFrameRefresh(mainPanelUpdateId(peid));
	}

	/**
	 * Handle a user deleting a message - they've already confirmed the deletion, just delete it now
	 */
	public void doDeleteMessage(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// find the message and delete it now!
		try
		{
			String messageid = (String) state.getAttribute("messageid");
			ChatChannel channel = ChatService.getChatChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			channel.removeMessage(messageid);
		}
		catch (PermissionException e)
		{
			context.put("alertMessage", rb.getString("youdonot4"));
		}
		catch (IdUnusedException e)
		{
		}
		catch (Exception e)
		{
			M_log.warn("doDeleteMessage()", e);
		}

		state.removeAttribute("messageid");
		state.removeAttribute(STATE_MODE);
	}

	interface ChatFilter
	{
		Time getAfterDate();

		int getLimitedToLatest();
	}

	/** A filter */
	class SelectMessagesByTime implements ChatFilter
	{
		/** The number of days back to accept messages. */
		private int m_days = 0;

		/** The cutoff time - messages before this are rejected. */
		private Time m_cutoff = null;

		/**
		 * Constructor
		 * 
		 * @param days
		 *        The number of days back to accept messages.
		 */
		public SelectMessagesByTime(int days)
		{
			// Log.info("chef", this + ".SelectMessagesByTime(" + days + ")");
			m_days = days;

			// compute the cutoff - Note: use the filter fast - the clock is ticking.
			m_cutoff = TimeService.newTime(System.currentTimeMillis() - ((long) days * 24l * 60l * 60l * 1000l));

		} // SelectMessagesByTime

		public Time getAfterDate()
		{
			return m_cutoff;
		}

		public int getLimitedToLatest()
		{
			return 0;
		}

		public String toString()
		{
			return this.getClass().getName() + " " + Integer.toString(m_days);
		}
	}

	/** A filter */
	class SelectMessagesByNumber implements ChatFilter
	{
		/** The cutoff value - messages before this date/time are rejected. */
		private Time m_first;

		/** the number of messages to select */
		private int m_number;

		/**
		 * Constructor
		 * 
		 * @param number
		 *        The number of the messages to be returned
		 */
		public SelectMessagesByNumber(int number)
		{
			// Log.info("chef", this + ".SelectMessagesByNumber(" + number + ")");
			m_number = number;
		}

		public Time getAfterDate()
		{
			return null;
		}

		public int getLimitedToLatest()
		{
			return m_number;
		}

		public String toString()
		{
			return this.getClass().getName() + " " + m_number;

		} // toString

	} // SelectMessagesByNumber

	/** A filter */
	class SelectAllMessages implements ChatFilter
	{
		/** Constructor for the SelectAllMessages object */
		public SelectAllMessages()
		{
			// Log.info("chef", this + ".SelectAllMessages()");
		} // SelectAllMessages

		public Time getAfterDate()
		{
			return null;
		}

		public int getLimitedToLatest()
		{
			return 0;
		}

		public String toString()
		{
			return this.getClass().getName();

		} // toString

	} // SelectAllMessages

	/** A filter that gets all messages since midnight today, local time */
	class SelectTodaysMessages implements ChatFilter
	{
		/** The cutoff time - messages before this are rejected. */
		private Time m_cutoff = null;

		/** Constructor for the SelectTodaysMessages object */
		public SelectTodaysMessages()
		{
			super();
			// Log.info("chef", this + ".SelectTodaysMessages()");

			TimeBreakdown now = (TimeService.newTime(System.currentTimeMillis())).breakdownLocal();
			// compute the cutoff for midnight today.
			m_cutoff = TimeService.newTimeLocal(now.getYear(), now.getMonth(), now.getDay(), 0, 0, 0, 0);

		} // SelectTodaysMessages

		public Time getAfterDate()
		{
			return m_cutoff;
		}

		public int getLimitedToLatest()
		{
			return 0;
		}

		public String toString()
		{
			return this.getClass().getName();

		} // toString

	} // SelectTodaysMessages

	/**
	 * Handle a request to set options.
	 */
	public void doOptions(RunData runData, Context context)
	{
		super.doOptions(runData, context);

		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// if there's an alert message, divert it to the main frame
		String msg = (String) state.getAttribute(STATE_MESSAGE);
		state.setAttribute(STATE_MAIN_MESSAGE, msg);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_MESSAGE);

	} // doOptions

	/**
	 * Fire up the permissions editor, next request cycle
	 */
	public void doPermissions(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		
		// trigger the switch on the next request (which is going to happen after this action is processed with its redirect response to the build)
		state.setAttribute(STATE_PERMISSIONS, STATE_PERMISSIONS);

		// schedule a main refresh to excape from the toolbar panel
		schedulePeerFrameRefresh(mainPanelUpdateId(peid));
	}

	/**
	 * Fire up the permissions editor
	 */
	protected void doPermissionsNow(RunData data, Context context)
	{
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String channelRefStr = (String) state.getAttribute(STATE_CHANNEL_REF);
		Reference channelRef = EntityManager.newReference(channelRefStr);
		String siteRef = SiteService.siteReference(channelRef.getContext());

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setpermis") + " "
				+ SiteService.getSiteDisplay(channelRef.getContext()));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "chat.");
	}
}
