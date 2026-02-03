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

package org.sakaiproject.message.tool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * SynopticMessageAction is a the Sakai synopsis tool for messages (chat, announcement, discussion).
 * </p>
 */
@Slf4j
public class SynopticMessageAction extends VelocityPortletPaneledAction
{

	private static ResourceLoader rb = new ResourceLoader("recent");

	/** portlet configuration parameter names. */
	private static final String PARAM_CHANNEL = "channel";

	private static final String PARAM_DAYS = "days";

	private static final String PARAM_ITEMS = "items";

	private static final String PARAM_LENGTH = "length";

	private static final String PARAM_SHOW_BODY = "show-body";

	private static final String PARAM_SHOW_NEWLINES = "show-newlines";

	private static final String PARAM_SERVICE = "message-service";

	private static final String PARAM_SHOW_SUBJECT = "show-subject";
	
	private static final String PARAM_HIDE_OPTIONS = "hide.options";

	/** Configure form field names. */
	private static final String FORM_CHANNEL = "channel";

	private static final String FORM_DAYS = "days";

	private static final String FORM_ITEMS = "items";

	private static final String FORM_LENGTH = "length";

	private static final String FORM_SHOW_BODY = "show-body";

	private static final String FORM_SHOW_SUBJECT = "show-subject";

	/** Control form field names. */
	private static final String FORM_MESSAGE = "message";
	
	private static final String STATE_HIDE_OPTIONS = "hide.options";

	/** state attribute names. */
	private static final String STATE_CHANNEL_REF = "channelId";

	private static final String STATE_ERROR = "error";

	private static final String STATE_SERVICE = "service";

	private static final String STATE_SERVICE_NAME = "service-name";

	private static final String STATE_UPDATE = "update";

	private static final String STATE_CHANNEL_PROBLEM = "channel-problem";

	private static final String STATE_DAYS = "days";

	private static final String STATE_AFTER_DATE = "afterdate";

	private static final String STATE_ITEMS = "items";

	private static final String STATE_LENGTH = "length";

	private static final String STATE_SHOW_BODY = "show-body";

	private static final String STATE_SHOW_SUBJECT = "show-subject";

	private static final String STATE_SHOW_NEWLINES = "show-newlines";

	private static final String STATE_SUBJECT_OPTION = "allow-option-of-showing-subject";

	private static final String SERVICENAME_ANNOUNCEMENT = "org.sakaiproject.announcement.api.AnnouncementService";

	private static final String SERVICENAME_DISCUSSION = "org.sakaiproject.discussion.api.DiscussionService";

	private static final String MESSAGEPROP_TIME_RELEASEDATE = "releaseDate";

	private static final String MESSAGEPROP_TIME_EXPIREDATE = "retractDate";

	/**
	 * Populate the state object, if needed - override to do something!
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		if (state.getAttribute(STATE_CHANNEL_REF) == null)
		{
			PortletConfig config = portlet.getPortletConfig();

			MessageService service = (MessageService) state.getAttribute(STATE_SERVICE);
			if (service == null)
			{
				// which message service?
				String serviceName = config.getInitParameter(PARAM_SERVICE);

				// deal with old CHEF 1.2.10 settings
				if (serviceName.startsWith("org.chefproject"))
				{
					// get the registered setting, ignoring the placement
					serviceName = config.get3InitParameter(PARAM_SERVICE)[1];
				}

				state.setAttribute(STATE_SERVICE_NAME, serviceName);

				service = getMessageService(serviceName);
				state.setAttribute(STATE_SERVICE, service);
			}

			// read the channel from configuration, or, if not specified, use the default for the page
			String channel = StringUtils.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = service.channelReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
			}
			state.setAttribute(STATE_CHANNEL_REF, channel);

			// read the days parameter
			if (state.getAttribute(STATE_DAYS) == null)
			{
				int days = 10;
				String daysParam = config.getInitParameter(PARAM_DAYS);
				try
				{
					if (daysParam != null)
					{
						days = Integer.parseInt(daysParam);
					}
				}
				catch (Exception e)
				{
					log.debug("reading days parameter: [{}], {}", daysParam, e.toString());
				}

				state.setAttribute(STATE_DAYS, Integer.valueOf(days));

				long startTime = System.currentTimeMillis() - ((long) days * 24l * 60l * 60l * 1000l);
				state.setAttribute(STATE_AFTER_DATE, TimeService.newTime(startTime));
			}

			// read the items parameter
			if (state.getAttribute(STATE_ITEMS) == null)
			{
				String itemsParam = config.getInitParameter(PARAM_ITEMS);
				try
				{
					if (itemsParam != null)
					{
						state.setAttribute(STATE_ITEMS, Integer.valueOf(itemsParam));
					}
					else
					{
						state.setAttribute(STATE_ITEMS, Integer.valueOf(3));
					}
				}
				catch (Exception e)
				{
					log.debug("reading items parameter: [{}], {}", itemsParam, e.toString());
					// use a default value
					state.setAttribute(STATE_ITEMS, Integer.valueOf(3));
				}
			}

			// read the length parameter
			if (state.getAttribute(STATE_LENGTH) == null)
			{
				String lengthParam = config.getInitParameter(PARAM_LENGTH);
				try
				{
					if (lengthParam != null)
					{
						state.setAttribute(STATE_LENGTH, Integer.valueOf(lengthParam));
					}
					else
					{
						state.setAttribute(STATE_LENGTH, 50);
					}
				}
				catch (Exception e)
				{
					log.debug("reading length parameter: [{}], {}", lengthParam, e.toString());
					// use a default value
					state.setAttribute(STATE_LENGTH, 50);
				}
			}

			// read the show-subject parameter
			if (state.getAttribute(STATE_SHOW_SUBJECT) == null)
			{
				String showSubjectParam = config.getInitParameter(PARAM_SHOW_SUBJECT);
				try
				{
					if (showSubjectParam != null)
					{
						state.setAttribute(STATE_SHOW_SUBJECT, Boolean.valueOf(showSubjectParam));
					}
					else
					{
						state.setAttribute(STATE_SHOW_SUBJECT, Boolean.FALSE);
					}
				}
				catch (Exception e)
				{
					log.debug("reading show-subject parameter: [{}], {}", showSubjectParam, e.toString());
					// use a default value
					state.setAttribute(STATE_SHOW_SUBJECT, Boolean.FALSE);
				}
			}

			// read the show-body parameter
			if (state.getAttribute(STATE_SHOW_BODY) == null)
			{
				String showBodyParam = config.getInitParameter(PARAM_SHOW_BODY);
				try
				{
					if (showBodyParam != null)
					{
						state.setAttribute(STATE_SHOW_BODY, Boolean.valueOf(showBodyParam));
					}
					else
					{
						state.setAttribute(STATE_SHOW_BODY, Boolean.FALSE);
					}
				}
				catch (Exception e)
				{
					log.debug("reading show-body parameter: [{}], {}", showBodyParam, e.toString());
					// use a default value
					state.setAttribute(STATE_SHOW_BODY, Boolean.FALSE);
				}
			}
			
			if (state.getAttribute(STATE_HIDE_OPTIONS) == null)
			{
				String hideOptionsParam = config.getInitParameter(PARAM_HIDE_OPTIONS);
				try
				{
					if (hideOptionsParam != null)
					{
						state.setAttribute(STATE_HIDE_OPTIONS, Boolean.valueOf(hideOptionsParam));
					}
					else
					{
						state.setAttribute(STATE_HIDE_OPTIONS, Boolean.FALSE);
					}
				}
				catch (Exception e)
				{
					log.debug("reading hide-options parameter: [{}], {}", hideOptionsParam, e.toString());
					// use a default value
					state.setAttribute(STATE_HIDE_OPTIONS, Boolean.FALSE);
				}
			}

			// read the show-newlines parameter
			if (state.getAttribute(STATE_SHOW_NEWLINES) == null)
			{
				initStateShowNewlines(state, config);
			}
		}

	} // initState

	private void initStateShowNewlines(SessionState state, PortletConfig config) {
		try
		{
			state.setAttribute(STATE_SHOW_NEWLINES, Boolean.valueOf(config.getInitParameter(PARAM_SHOW_NEWLINES)));
		}
		catch (Exception e)
		{
			// use a default value
			state.setAttribute(STATE_SHOW_NEWLINES, Boolean.FALSE);
		}
	}

	/**
	 * build the context for the Main panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{

		context.put("tlang", rb);

		context.put("contentTypeImageService", ContentTypeImageService.getInstance());

		// if the synoptic options have just been imported, we need to update
		// the state
		if (state.getAttribute(STATE_UPDATE) != null)
		{
			updateState(state, portlet);
			state.removeAttribute(STATE_UPDATE);
		}

		// // TODO: TIMING
		// if (CurrentService.getInThread("DEBUG") == null)
		// CurrentService.setInThread("DEBUG", new StringBuilder());
		// long startTime = System.currentTimeMillis();

		// different title of Option link for different tools.
		Tool tool = ToolManager.getCurrentTool();
		context.put("toolId", tool.getId());

		// handle options mode
		if (MODE_OPTIONS.equals(state.getAttribute(STATE_MODE)))
		{
			return buildOptionsPanelContext(portlet, context, rundata, state);
		}

		// build the menu
		Menu bar = new MenuImpl(portlet, rundata, (String) state.getAttribute(STATE_ACTION));

		// add options if allowed
		if (!(Boolean)state.getAttribute(STATE_HIDE_OPTIONS))
		{
		addOptionsMenu(bar, (JetspeedRunData) rundata);
		}
		
		
		if (!bar.getItems().isEmpty())
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		// set the message length (leave as an Integer)
		context.put("length", state.getAttribute(STATE_LENGTH));

		// set useSubject - true to display the message subject (else use the body)
		context.put("showSubject", state.getAttribute(STATE_SHOW_SUBJECT));

		// set showBody - true to display the message body
		// message subject is always displayed for recent discussion tool - handled by vm
		context.put("showBody", state.getAttribute(STATE_SHOW_BODY));

		// whether to show newlines in the message body, or not
		if (state.getAttribute(STATE_SHOW_NEWLINES) == null)
		{
			initStateShowNewlines(state, portlet.getPortletConfig());
		}
		context.put("show_newlines", ((Boolean) state.getAttribute(STATE_SHOW_NEWLINES)).toString());

		try
		{
			MessageService service = (MessageService) state.getAttribute(STATE_SERVICE);
			String channelRef = (String) state.getAttribute(STATE_CHANNEL_REF);
			Time afterDate = (Time) state.getAttribute(STATE_AFTER_DATE);
			int items = 3;
			// read the items parameter
			if (state.getAttribute(STATE_ITEMS) != null)
			{
				items = ((Integer) state.getAttribute(STATE_ITEMS)).intValue();
			}

			String serviceName = (String) state.getAttribute(STATE_SERVICE_NAME);

			List messages = retrieveMessages(service, serviceName, channelRef,afterDate, items);
			context.put("messages", messages);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youdonot"));
		}

		String rv = (String) getContext(rundata).get("template") + "-List";

		// // TODO: TIMING
		// long endTime = System.currentTimeMillis();
		// if (endTime-startTime > /*5*/000)
		// {
		// StringBuilder buf = (StringBuilder) CurrentService.getInThread("DEBUG");
		// if (buf != null)
		// {
		// buf.insert(0,"synopticMessageAction: "
		// + state.getAttribute(STATE_CHANNEL_REF)
		// + " time: " + (endTime - startTime));
		// }
		// }
		
		//SAK-19700 put name of tool into context so it can be rendered with the option link, for screenreaders
		context.put("toolTitle", ToolManager.getCurrentPlacement().getTitle());

		return rv;

	} // buildMainPanelContext

	private List<Message> retrieveMessages(final MessageService messageService,
										   final String serviceName,
										   final String channelRef,
										   final Time afterDate,
										   final int numberOfMessages) 
			throws PermissionException {
		List<Message> result;
		if (numberOfMessages <= 0 || serviceName == null) {
			result = new ArrayList<>();
		} else if (SERVICENAME_DISCUSSION.equals(serviceName)) {
			result = messageService.getMessages(channelRef, afterDate, numberOfMessages, false, true, false);
		} else if (SERVICENAME_ANNOUNCEMENT.equals(serviceName)) {
			result = messageService.getMessages(channelRef, afterDate, 0, false, false, false);
			filterAnnouncementMessages(result, afterDate);
			if (result.size() > numberOfMessages) {
				result.subList(numberOfMessages, result.size()).clear();
			}
		} else {
			result = messageService.getMessages(channelRef, afterDate, numberOfMessages, false, false, false);
		}
		return result;
	}

	private void filterAnnouncementMessages(final List<Message> messages, final Time afterDate) {
		final Time currentTime = TimeService.newTime();
		messages.removeIf(message -> {
			// before release-date filter
			Time releaseDate = getTimeProperty(message, MESSAGEPROP_TIME_RELEASEDATE);
			if (releaseDate != null && releaseDate.after(currentTime)) {
				return true;
			}

			// before afterDate (user-option) filter
			// if no release-date is set, use the header-date as release-date instead
			if (releaseDate == null) {
				releaseDate = message.getHeader().getDate();
			}
			if (releaseDate != null && releaseDate.before(afterDate)) {
				return true;
			}

			// after expire-date filter
			Time expireDate = getTimeProperty(message, MESSAGEPROP_TIME_EXPIREDATE);
			if (expireDate != null && expireDate.before(currentTime)) {
				return true;
			}

			return false;
		});
	}

	private Time getTimeProperty(Message message, String propertyName) {
		try {
			return message.getProperties().getTimeProperty(propertyName);
		} catch (EntityPropertyNotDefinedException e) {
			return null;
		} catch (EntityPropertyTypeException e) {
			log.warn("the property {} is not a valid time, {}", propertyName, e.toString());
			return null;
		}
	}

	/**
	 * Setup for the options panel.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		String serviceName = (String) state.getAttribute(STATE_SERVICE_NAME);
		String tool_title = rb.getString("tool_title");
		String tool_name = rb.getString("tool_name");
		String one_item = rb.getString("one_item");
		String all_items = rb.getString("channel_analog");
		String channel_analog = rb.getString("channel_analog");
		Boolean allow_show_subject = Boolean.TRUE;
		Boolean allow_channel_choice = Boolean.FALSE;

		if (serviceName.equals(SERVICENAME_DISCUSSION))
		{
			tool_title = rb.getString("dtool_title");
			tool_name = rb.getString("dtool_name");
			one_item = rb.getString("done_item");
			all_items = rb.getString("dall_items");
		}
		else if (serviceName.equals(SERVICENAME_ANNOUNCEMENT))
		{
			tool_title = rb.getString("atool_title");
			tool_name = rb.getString("atool_name");
			one_item = rb.getString("aone_item");
			all_items = rb.getString("aall_items");
		}

		// provide "tool_title" as title for options page
		context.put("tool_title", tool_title);

		// provide "tool_name" as name of the tool
		context.put("tool_name", tool_name);

		// provide "one_item" as a reference to a single message
		context.put("one_item", one_item);

		// provide "all_items" as a reference to all messages collectively
		context.put("all_items", all_items);

		// provide "allow_show_subject" with the value for whether to allow user to choose between subject or body
		context.put("allow_show_subject", allow_show_subject.toString());
		if (allow_show_subject.booleanValue())
		{
			context.put("showBody", ((Boolean) state.getAttribute(STATE_SHOW_BODY)).toString());

			// provide "showSubject" with the value for showing subject (true) or body (false)
			context.put("showSubject", ((Boolean) state.getAttribute(STATE_SHOW_SUBJECT)).toString());
		}

		// provide "allow_channel_choice" with the value for whether to allow user to choose the channel
		context.put("allow_channel_choice", allow_channel_choice.toString());
		if (allow_channel_choice.booleanValue())
		{
			// provide "channel_analog" with the word(s) used to refer to a channel, such as "Chat Room"
			context.put("channel_analog", channel_analog);

			// provide "default_channel" with the dafault channel-id for the user/group
			context.put("default_channel", SiteService.MAIN_CONTAINER);

			// provide "channel" with the current channel's id
			String placementContext = ToolManager.getCurrentPlacement().getContext();
			String defaultChannel = ((MessageService) state.getAttribute(STATE_SERVICE)).channelReference(placementContext,
					SiteService.MAIN_CONTAINER);
			String sitePrefix = defaultChannel.substring(0, defaultChannel.lastIndexOf(SiteService.MAIN_CONTAINER));
			String currentChannel = ((String) state.getAttribute(STATE_CHANNEL_REF)).substring(sitePrefix.length());
			context.put("channel", currentChannel);

			// provide "channels" as a list of channels belonging to this site

			// // TODO: TIMING
			// if (CurrentService.getInThread("DEBUG") == null)
			// CurrentService.setInThread("DEBUG", new StringBuilder());
			// long startTime = System.currentTimeMillis();

			Iterator aChannel = ((MessageService) state.getAttribute(STATE_SERVICE)).getChannelIds(placementContext).iterator();

			// // TODO: TIMING
			// long endTime = System.currentTimeMillis();
			// if (endTime-startTime > /*5*/000)
			// {
			// StringBuilder buf = (StringBuilder) CurrentService.getInThread("DEBUG");
			// if (buf != null)
			// {
			// buf.insert(0,"synopticMessageAction.options: "
			// + state.getAttribute(STATE_CHANNEL_REF)
			// + " time: " + (endTime - startTime));
			// }
			// }

			List<String> channel_list = new ArrayList<>();
			while (aChannel.hasNext())
			{
				String theChannel = (String) aChannel.next();
				if (!theChannel.equals(SiteService.MAIN_CONTAINER))
				{
					channel_list.add(theChannel);
				}
			}
			context.put("channels", channel_list);
		}

		// provide "days" with the days value
		context.put("days", ((Integer) state.getAttribute(STATE_DAYS)).toString());

		// provide "items" with the items value
		context.put("items", ((Integer) state.getAttribute(STATE_ITEMS)).toString());

		// provide "length" with the items value
		context.put("length", ((Integer) state.getAttribute(STATE_LENGTH)).toString());

		// provide the form field names
		context.put("channel_form", FORM_CHANNEL);
		context.put("days_form", FORM_DAYS);
		context.put("items_form", FORM_ITEMS);
		context.put("length_form", FORM_LENGTH);
		context.put("show_body_form", FORM_SHOW_BODY);
		context.put("show_subject_form", FORM_SHOW_SUBJECT);

		// set the action for form processing
		context.put("action", state.getAttribute(STATE_ACTION));
		context.put("form-submit", BUTTON + "doUpdate");
		context.put("form-cancel", BUTTON + "doCancel");

		context.put("selectedChars", state.getAttribute(STATE_LENGTH));

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(rundata).get("template");
		return template + "-customize";

	} // buildOptionsPanelContext

	/**
	 * doUpdate handles user clicking "Done" in Options panel (called for form input tags type="submit" named="eventSubmit_doUpdate")
	 */
	public void doUpdate(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String serviceName = (String) state.getAttribute(STATE_SERVICE_NAME);

		boolean allow_show_subject = true;
		boolean allow_channel_choice = false;

		// showSubject
		if (allow_show_subject)
		{
			if (serviceName.equals(SERVICENAME_DISCUSSION))
			{
				// always show subject for Recent Discussion
				String showBody = data.getParameters().getString(FORM_SHOW_BODY);
				Boolean sb = Boolean.valueOf(showBody);
				if (!sb.equals(state.getAttribute(STATE_SHOW_BODY)))
				{
					state.setAttribute(STATE_SHOW_BODY, sb);
					state.setAttribute(STATE_UPDATE, STATE_UPDATE);
				}
			}
			else if (serviceName.equals(SERVICENAME_ANNOUNCEMENT))
			{
				String showSubject = data.getParameters().getString(FORM_SHOW_SUBJECT);
				Boolean ss = Boolean.valueOf(showSubject);
				if (!ss.equals(state.getAttribute(STATE_SHOW_SUBJECT)))
				{
					state.setAttribute(STATE_SHOW_SUBJECT, ss);
					state.setAttribute(STATE_UPDATE, STATE_UPDATE);
				}
			}
		}

		// channel
		if (allow_channel_choice)
		{
			String placementContext = ToolManager.getCurrentPlacement().getContext();
			String newChannel = data.getParameters().getString(FORM_CHANNEL);
			String currentChannel = ((String) state.getAttribute(STATE_CHANNEL_REF)).substring(placementContext.length() + 1);

			if (newChannel != null && !newChannel.equals(currentChannel))
			{
				String channel_ref = ((MessageService) state.getAttribute(STATE_SERVICE)).channelReference(placementContext,
						newChannel);
				state.setAttribute(STATE_CHANNEL_REF, channel_ref);
				log.debug("newChannel: {}", channel_ref);

				// update the tool config
				Placement placement = ToolManager.getCurrentPlacement();
				placement.getPlacementConfig().setProperty(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF));

				// deliver an update to the title panel (to show the new title)
				String titleId = titlePanelUpdateId(peid);
				schedulePeerFrameRefresh(titleId);
			}
		}

		// days
		String daysValue = data.getParameters().getString(FORM_DAYS);
		if (daysValue != null) {
			try {
				Integer days = Integer.valueOf(daysValue);
				if (!days.equals(state.getAttribute(STATE_DAYS))) {
					state.setAttribute(STATE_DAYS, days);
					state.setAttribute(STATE_UPDATE, STATE_UPDATE);

					// recompute this which is used for selecting the messages for display
					long startTime = System.currentTimeMillis() - (days.longValue() * 24L * 60L * 60L * 1000L);
					state.setAttribute(STATE_AFTER_DATE, TimeService.newTime(startTime));
				}
			} catch (NumberFormatException nfe) {
				log.warn("Invalid days value provided: [{}], {}", daysValue, nfe.toString());
			}
		}

		// items
		String itemsValue = data.getParameters().getString(FORM_ITEMS);
		if (itemsValue != null) {
			try {
				Integer items = Integer.valueOf(itemsValue);
				if (!items.equals(state.getAttribute(STATE_ITEMS))) {
					state.setAttribute(STATE_ITEMS, items);
					state.setAttribute(STATE_UPDATE, STATE_UPDATE);
				}
			} catch (NumberFormatException nfe) {
				log.warn("Invalid items value provided: [{}], {}", itemsValue, nfe.toString());
			}
		}
		// length
		String lengthValue = data.getParameters().getString(FORM_LENGTH);
		try {
			if (lengthValue != null) {
				Integer length = Integer.valueOf(lengthValue);
				if (!length.equals(state.getAttribute(STATE_LENGTH))) {
					state.setAttribute(STATE_LENGTH, length);
					state.setAttribute(STATE_UPDATE, STATE_UPDATE);
				}
			}
		} catch (NumberFormatException nfe) {
			log.warn("Invalid length value provided: [{}], {}", lengthValue, nfe.toString());
		}

		// update the tool config
		Placement placement = ToolManager.getCurrentPlacement();
		placement.getPlacementConfig().setProperty(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF));
		placement.getPlacementConfig().setProperty(PARAM_DAYS, ((Integer) state.getAttribute(STATE_DAYS)).toString());
		placement.getPlacementConfig().setProperty(PARAM_ITEMS, ((Integer) state.getAttribute(STATE_ITEMS)).toString());
		placement.getPlacementConfig().setProperty(PARAM_LENGTH, ((Integer) state.getAttribute(STATE_LENGTH)).toString());
		placement.getPlacementConfig().setProperty(PARAM_SHOW_BODY, ((Boolean) state.getAttribute(STATE_SHOW_BODY)).toString());
		placement.getPlacementConfig().setProperty(PARAM_SHOW_SUBJECT,
				((Boolean) state.getAttribute(STATE_SHOW_SUBJECT)).toString());

		// commit the change
		saveOptions();

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);

	} // doUpdate

	/**
	 * doCancel handles user clicking "Cancel" in Options panel
	 */
	public void doCancel(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);

		// cancel the options
		cancelOptions();

	} // doCancel

	/**
	 * update state (synoptic tool options)
	 */
	private void updateState(SessionState state, VelocityPortlet portlet)
	{
		PortletConfig config = portlet.getPortletConfig();

		try {
			int days = Integer.parseInt(config.getInitParameter(PARAM_DAYS));
			state.setAttribute(STATE_DAYS, days);

			long startTime = System.currentTimeMillis() - (long) days * 24L * 60L * 60L * 1000L;
			state.setAttribute(STATE_AFTER_DATE, TimeService.newTime(startTime));

			state.setAttribute(STATE_ITEMS, Integer.valueOf(config.getInitParameter(PARAM_ITEMS)));
			state.setAttribute(STATE_LENGTH, Integer.valueOf(config.getInitParameter(PARAM_LENGTH)));
			state.setAttribute(STATE_SHOW_SUBJECT, Boolean.valueOf(config.getInitParameter(PARAM_SHOW_SUBJECT)));
			state.setAttribute(STATE_SHOW_BODY, Boolean.valueOf(config.getInitParameter(PARAM_SHOW_BODY)));
			state.setAttribute(STATE_SHOW_NEWLINES, Boolean.valueOf(config.getInitParameter(PARAM_SHOW_NEWLINES)));
			state.setAttribute(STATE_HIDE_OPTIONS, Boolean.valueOf(config.getInitParameter(PARAM_HIDE_OPTIONS)));
		} catch (Exception e) {
			// don't update the state if there are any errors
			log.warn("Could not update state from configuration parameters: {}", e.toString());
		}
	} 

	/**
	 * Improves performance by returning the appropriate MessageService through the service Cover classes instead of through the ComponentManager (for certain well-known services)
	 */
	private static final MessageService getMessageService(String ifaceName)
	{
		return (MessageService) ComponentManager.get(ifaceName);
	}
}
