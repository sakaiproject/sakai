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

package org.sakaiproject.mailarchive.tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuDivider;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.javax.Order;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.javax.Search;
import org.sakaiproject.javax.SearchFilter;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveChannelEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessage;
import org.sakaiproject.mailarchive.cover.MailArchiveService;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * MailboxAction is a the Sakai mailbox tool.
 * </p>
 */
@Slf4j
public class MailboxAction extends PagedResourceActionII
{
	private static ResourceLoader rb = new ResourceLoader("email");

	/** portlet configuration parameter names. */
	private static final String PARAM_CHANNEL = "channel";

	private static final String PARAM_SITE = "site";
	
	private static final String PARAM_SHOW_NON_ALIAS = "showNonAlias";

	/** Configure form field names. */
	private static final String FORM_CHANNEL = "channel";

	private static final String FORM_PAGESIZE = "pagesize";

	private static final String FORM_OPEN = "open";
	
	private static final String FORM_REPLY = "reply";

	private static final String FORM_SENDTO = "sendto";

	private static final String FORM_ALIAS = "alias";

	private static final int FORM_ALIAS_MAX_LENGTH = 99;

        private static final String FORM_ITEM_NUMBER  = "item_number";

	/** List request parameters. */
	private static final String VIEW_ID = "view-id";

	/** state attribute names. */
	private static final String STATE_CHANNEL_REF = "channelId";

	private static final String STATE_ASCENDING = "ascending";

	private static final String STATE_SORT = "sort";

	private static final String STATE_VIEW_HEADERS = "view-headers";

	private static final String STATE_OPTION_PAGESIZE = "optSize";

	private static final String STATE_OPTION_OPEN = "optOpen";
	
	private static final String STATE_OPTION_REPLY = "optReply";

	private static final String STATE_OPTION_SENDTO = "optSendTo";

	private static final String STATE_OPTION_ALIAS = "optAlias";

	private static final String STATE_SHOW_NON_ALIAS = "showNonAlias";
	
	private static final String STATE_ALERT_MESSAGE = "alertMessage";

	private static final String STATE_DELETE_CONFIRM_ID = "confirmDeleteId";
	
	/** Sort codes. */
	private static final int SORT_FROM = 0;

	private static final int SORT_DATE = 1;

	private static final int SORT_SUBJECT = 2;

	/** paging */
	
	private static final String STATE_MSG_VIEW_ID = "msg-id";
    
    	/** State to cache the count of messages */
    
	private static final String STATE_COUNT = "state-cached-count";
    
	private static final String STATE_COUNT_SEARCH = "state-cached-count-search";   

	/** Default for search suppression threshold */
        private final int MESSAGE_THRESHOLD_DEFAULT = 2500;

	private AliasService aliasService;

	public MailboxAction() {
		super();
		aliasService = ComponentManager.get(AliasService.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#sizeResources(org.sakaiproject.service.framework.session.SessionState)
	 */
	protected int sizeResources(SessionState state)
	{
		String search = (String) state.getAttribute(STATE_SEARCH);

		// We cache the count at the tool level because it is not done perfectly
		// at the lower layer
		Integer lastCount = (Integer) state.getAttribute(STATE_COUNT);
		String countSearch = (String) state.getAttribute(STATE_COUNT_SEARCH);
	
		if ( search == null && countSearch == null && lastCount != null )
		{
			return lastCount.intValue();
		}
		if ( countSearch != null && countSearch.equals(search))
		{
			return lastCount.intValue();
		}
	
		// We must talk to the Storage to count the messages
        	try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));

			int cCount = 0;
			if(search == null) {
			    cCount = channel.getCount();
			} else {
                cCount = channel.getCount((Filter) getSearchFilter(search, 0, 0));
			}

			lastCount = new Integer(cCount);
			state.setAttribute(STATE_COUNT, lastCount);
			state.setAttribute(STATE_COUNT_SEARCH, search);
			return cCount;
		}
		catch (Exception e)
		{
		 	log.warn("failed search={} exeption={}", search, e.getMessage());
		}
        	return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#readResourcesPage(org.sakaiproject.service.framework.session.SessionState, int, int)
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// read all channel messages
		List allMessages = null;
		boolean ascending = ((Boolean) state.getAttribute(STATE_ASCENDING)).booleanValue();
		int sort = ((Integer) state.getAttribute(STATE_SORT)).intValue();		
		String search = (String) state.getAttribute(STATE_SEARCH);
		
		
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			Search f = getSearchFilter(search, first, last);
			if ( sort == SORT_FROM ) 
			{
				f.setOrders(new Order[] { new Order("OWNER",ascending) } );
			}
			else if ( sort == SORT_SUBJECT ) 
			{
				f.setOrders(new Order[] { new Order("SUBJECT",ascending) } );
			}

			allMessages = channel.getMessages((Filter) f, ascending, null);

		}
		catch (Exception e)
		{
		 	log.warn("not able to retrieve messages sort={} search={} first={} last={}", sort, search, first, last);
		}
	
		// deal with no messages
		if (allMessages == null) return new Vector();
			
		return allMessages;

	} // readPagedResources

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		if (state.getAttribute(STATE_CHANNEL_REF) == null)
		{
			PortletConfig config = portlet.getPortletConfig();

			// start in list mode
			state.setAttribute(STATE_MODE, "list");

			// read the channel from configuration, or, if not specified, use the default for the request
			String channel = StringUtils.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = MailArchiveService.channelReference(ToolManager.getCurrentPlacement().getContext(),
						SiteService.MAIN_CONTAINER);
			}
			state.setAttribute(STATE_CHANNEL_REF, channel);

			if (state.getAttribute(STATE_SHOW_NON_ALIAS) == null)
			{
				Boolean showNonAlias = Boolean.parseBoolean(config.getInitParameter(PARAM_SHOW_NON_ALIAS, "false"));
				state.setAttribute(STATE_SHOW_NON_ALIAS, showNonAlias);
			}
			
			if (state.getAttribute(STATE_ASCENDING) == null)
			{
				state.setAttribute(STATE_ASCENDING, Boolean.valueOf(false));
			}

			if (state.getAttribute(STATE_SORT) == null)
			{
				state.setAttribute(STATE_SORT, Integer.valueOf(SORT_DATE));
			}

			if (state.getAttribute(STATE_VIEW_HEADERS) == null)
			{
				state.setAttribute(STATE_VIEW_HEADERS, Boolean.valueOf(false));
			}

		}

	} // initState

	/**
	 * build the context for the main panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		String mode = (String) state.getAttribute(STATE_MODE);

		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		
		String alertMessage = (String) state.getAttribute(STATE_ALERT_MESSAGE);
		if ( alertMessage != null )
		{
			state.setAttribute(STATE_ALERT_MESSAGE, null);
			context.put(STATE_ALERT_MESSAGE, alertMessage);
		}

		// Put this back only for Confirm
		String deleteConfirm  = (String) state.getAttribute(STATE_DELETE_CONFIRM_ID);
		state.removeAttribute(STATE_DELETE_CONFIRM_ID);

		if ("list".equals(mode))
		{
			return buildListModeContext(portlet, context, rundata, state);
		}

		else if ("confirm-remove".equals(mode))
		{
			if ( deleteConfirm != null && deleteConfirm.length() > 0 ) 
			{
				state.setAttribute(STATE_DELETE_CONFIRM_ID,deleteConfirm);
			}
			return buildConfirmModeContext(portlet, context, rundata, state);
		}

		else if ("view".equals(mode))
		{
			return buildViewModeContext(portlet, context, rundata, state);
		}

		else if (MODE_OPTIONS.equals(mode)) 
		{
			return buildOptionsPanelContext(portlet, context, rundata, state);
		}

		else
		{
		 	log.warn("invalid mode: {}", mode);
			return null;
		}

	} // buildMainPanelContext

	/**
	 * build the context for the View mode (in the Main panel)
	 * 
	 * @return (optional) template name for this panel
	 */
	private String buildViewModeContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		boolean allowDelete = false;

		int viewPos = ((Integer) state.getAttribute(FORM_ITEM_NUMBER)).intValue();
		int numMessages = sizeResources(state);
		int prevPos = viewPos - 1;
		int nextPos = viewPos + 1;

		state.setAttribute(STATE_PREV_EXISTS, "");
		state.setAttribute(STATE_NEXT_EXISTS, "");

		// Message numbers are one-based
		boolean goPrev = (prevPos > 0);
		boolean goNext = (nextPos <= numMessages);

		context.put("viewPos",viewPos);

		context.put("nextPos",nextPos);
		context.put("goNPButton", Boolean.valueOf(goNext));

		context.put("prevPos",prevPos);
		context.put("goPPButton", Boolean.valueOf(goPrev));

		// prepare the sort of messages
		context.put("tlang", rb);

		String channelRef = (String) state.getAttribute(STATE_CHANNEL_REF);
		MailArchiveChannel channel = null;
		try 
		{
			channel = MailArchiveService.getMailArchiveChannel(channelRef);
		}
		catch (Exception e)
		{
		 	log.warn("Cannot find channel {}", channelRef);
		}

                // Read a single message
                List messagePage = readResourcesPage(state, viewPos, viewPos);
		if ( messagePage != null ) {
			Message msg = (Message) messagePage.get(0);
			context.put("email",msg);
			allowDelete = channel.allowRemoveMessage(msg);
			// Sadly this is the only way to send this to a menu pick :(
                	state.setAttribute(STATE_DELETE_CONFIRM_ID, msg.getId());
		} else {
		 	log.warn("Could not retrieve message {}", channelRef);
			context.put("message", rb.getString("thiemames1"));
		}

		context.put("viewheaders", state.getAttribute(STATE_VIEW_HEADERS));

		context.put("contentTypeImageService", ContentTypeImageService.getInstance());

		// build the menu
		Menu bar = new MenuImpl(portlet, rundata, (String) state.getAttribute(STATE_ACTION));

		// bar.add( new MenuEntry(rb.getString("listall"), "doList"));
		// addViewPagingMenus(bar, state);

		if (((Boolean) state.getAttribute(STATE_VIEW_HEADERS)).booleanValue())
		{
			bar.add(new MenuEntry(rb.getString("hidehead"), "doHide_headers"));
		}
		else
		{
			bar.add(new MenuEntry(rb.getString("viehea"), "doView_headers"));
		}
		if (allowDelete) bar.add(new MenuEntry(rb.getString("del"), "doRemove"));

		// make sure there's not leading or trailing dividers
		bar.adjustDividers();

		context.put(Menu.CONTEXT_MENU, bar);

		return (String) getContext(rundata).get("template") + "-view";

	} // buildViewModeContext

	/**
	 * Build the context for the confirm remove mode (in the Main panel).
	 */
	private String buildConfirmModeContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// get the message
		context.put("tlang", rb);

                String id = (String) state.getAttribute(STATE_DELETE_CONFIRM_ID);

		MailArchiveMessage message = null;
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			message = channel.getMailArchiveMessage(id);
			context.put("email", message);
			context.put(STATE_DELETE_CONFIRM_ID, message.getId());
		}
		catch (IdUnusedException e)
		{
		}
		catch (PermissionException e)
		{

		}
		if (message == null)
		{
 			context.put("message", rb.getString("thiemames1"));
 		}

		context.put("viewheaders", state.getAttribute(STATE_VIEW_HEADERS));

		return (String) getContext(rundata).get("template") + "-confirm_remove";

	} // buildConfirmModeContext

	/**
	 * build the context for the list mode (in the Main panel).
	 */
	private String buildListModeContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{

		// prepare the page of messages
		context.put("tlang", rb);
		List messages = prepPage(state);
		context.put("messages", messages);

		// build the menu
		Menu bar = new MenuImpl(portlet, rundata, (String) state.getAttribute(STATE_ACTION));

		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			bar.add(new MenuDivider());

			// add options if allowed
			addOptionsMenu(bar, (JetspeedRunData) rundata);

			bar.add(new MenuEntry(rb.getString("perm"), "doPermissions"));
		}

		// make sure there's not leading or trailing dividers
		bar.adjustDividers();

		context.put(Menu.CONTEXT_MENU, bar);

		// Decide if we are going to allow searching...
		int numMessages = sizeResources(state);
		int messageLimit = getMessageThreshold();
		context.put("allow-search",Boolean.valueOf(numMessages <= messageLimit));

		// output the search field
		context.put(STATE_SEARCH, state.getAttribute(STATE_SEARCH));

		// eventSubmit value and id field for drill down
		context.put("view-id", VIEW_ID);

		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		context.put("sort-by", state.getAttribute(STATE_SORT));
		context.put("sort-order", state.getAttribute(STATE_ASCENDING));

		pagingInfoToContext(state, context);

		// the aliases for the channel
		List all = aliasService.getAliases((String) state.getAttribute(STATE_CHANNEL_REF));

		// and the aliases for the site (context)
		Reference channelRef = EntityManager.newReference((String) state.getAttribute(STATE_CHANNEL_REF));
		String siteRef = SiteService.siteReference(channelRef.getContext());
		all.addAll(aliasService.getAliases(siteRef));

		context.put("aliases", all);
		
		if (all.size() == 0 || (Boolean)state.getAttribute(STATE_SHOW_NON_ALIAS))
		{
			context.put("nonAlias", channelRef.getContext());
		}
		
		context.put("serverName", ServerConfigurationService.getServerName());

		// if the user has permission to send mail, drop in the email address
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			if (channel.getEnabled())
			{
				if (channel.getOpen())
				{
					// if open, mail from anywhere
					context.put("validFrom", "*");
				}
				else if (channel.allowAddMessage())
				{
					User user = UserDirectoryService.getCurrentUser();
					String email = user.getEmail();
					context.put("validFrom", email);
				}
				context.put(STATE_OPTION_SENDTO, channel.getSendToList());
			}
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("thismaiis"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youdonot1"));
		}
		catch (Exception e)
		{
		}

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		return (String) getContext(rundata).get("template") + "-List";

	} // buildListModeContext

	/**
	 * Handle a user drill down request.
	 */
	public void doView(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// switch to view mode
		state.setAttribute(STATE_MODE, "view");

		String id = runData.getParameters().getString(VIEW_ID);
		state.setAttribute(STATE_MSG_VIEW_ID, id);

		String position = runData.getParameters().getString(FORM_ITEM_NUMBER);
		state.setAttribute(FORM_ITEM_NUMBER, Integer.valueOf(position));
		
		// disable auto-updates while in view mode
		disableObservers(state);

	} // doView

	/**
	 * Handle a return-to-list-view request.
	 */
	public void doList(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// switch to view mode
		state.setAttribute(STATE_MODE, "list");

		// make sure auto-updates are enabled
		enableObserver(state);

		state.removeAttribute(STATE_MSG_VIEW_ID);

	} // doList

	/**
	 * Handle a view headers request.
	 */
	public void doView_headers(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// switch to view mode
		state.setAttribute(STATE_VIEW_HEADERS, Boolean.valueOf(true));

	} // doView_headers

	/**
	 * Handle a hide headers request.
	 */
	public void doHide_headers(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// switch to view mode
		state.setAttribute(STATE_VIEW_HEADERS, Boolean.valueOf(false));

	} // doHide_headers

	/**
	 * Handle a user request to change the sort to "from"
	 */
	public void doSort_from(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);
		
		// we are changing the sort, so start from the first page again
		resetPaging(state);

		// if already from, swap the order
		if (((Integer) state.getAttribute(STATE_SORT)).intValue() == SORT_FROM)
		{
			boolean order = !((Boolean) state.getAttribute(STATE_ASCENDING)).booleanValue();
			state.setAttribute(STATE_ASCENDING, Boolean.valueOf(order));
		}

		// set state
		else
		{
			state.setAttribute(STATE_SORT, Integer.valueOf(SORT_FROM));
		}

	} // doSort_from

	/**
	 * Handle a user request to change the sort to "date"
	 */
	public void doSort_date(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// we are changing the sort, so start from the first page again
		resetPaging(state);

		// if already date, swap the order
		if (((Integer) state.getAttribute(STATE_SORT)).intValue() == SORT_DATE)
		{
			boolean order = !((Boolean) state.getAttribute(STATE_ASCENDING)).booleanValue();
			state.setAttribute(STATE_ASCENDING, Boolean.valueOf(order));
		}

		// set state
		else
		{
			state.setAttribute(STATE_SORT, Integer.valueOf(SORT_DATE));
		}

	} // doSort_date

	/**
	 * Handle a user request to change the sort to "subject"
	 */
	public void doSort_subject(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);
		
		// we are changing the sort, so start from the first page again
		resetPaging(state);

		// if already subject, swap the order
		if (((Integer) state.getAttribute(STATE_SORT)).intValue() == SORT_SUBJECT)
		{
			boolean order = !((Boolean) state.getAttribute(STATE_ASCENDING)).booleanValue();
			state.setAttribute(STATE_ASCENDING, Boolean.valueOf(order));
		}

		// set state
		else
		{
			state.setAttribute(STATE_SORT, Integer.valueOf(SORT_SUBJECT));
		}

	} // doSort_subject

	/**
	 * doRemove called when "eventSubmit_doRemove" is in the request parameters to confirm removal of the group
	 */
	public void doRemove(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// go to remove confirm mode
		state.setAttribute(STATE_MODE, "confirm-remove");

		// disable auto-updates while in confirm mode
		disableObservers(state);

	} // doRemove

	/**
	 * doRemove_confirmed called when "eventSubmit_doRemove_confirmed" is in the request parameters to remove the group
	 */
	public void doRemove_confirmed(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// Grab and remove immedaitely
                String msgId = (String) state.getAttribute(STATE_DELETE_CONFIRM_ID);
                state.removeAttribute(STATE_DELETE_CONFIRM_ID);
		state.removeAttribute(STATE_COUNT);
		state.removeAttribute(STATE_COUNT_SEARCH);

		// remove
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			
			if (msgId != null)
				channel.removeMessage(msgId);
			else
				addAlert(state, rb.getString("thimeshas"));
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("youdonot3"));
		}
		catch (IdUnusedException e)
		{
			addAlert(state, rb.getString("thimeshas"));
		}

		// go to list mode
		doList(data, context);

	} // doRemove_confirmed

	/**
	 * doCancel_remove called when "eventSubmit_doCancel_remove" is in the request parameters to cancel group removal
	 */
	public void doRemove_cancel(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// Clean up state
		state.removeAttribute(STATE_MSG_VIEW_ID);

		// return to view mode
		state.setAttribute(STATE_MODE, "view");

		// disable auto-updates while in view mode
		disableObservers(state);

	} // doRemove_cancel

	/**
	 * Handle a request to set options.
	 */
	public void doOptions(RunData runData, Context context)
	{
		super.doOptions(runData, context);

		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// if we ended up in options mode, do whatever else ...
		if (!MODE_OPTIONS.equals(state.getAttribute(STATE_MODE))) return;

	} // doOptions

	/**
	 * Setup for options.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{

		context.put("tlang", rb);
		// provide "pagesize" with the current page size setting
		context.put("pagesize", ((Integer) state.getAttribute(STATE_PAGESIZE)).toString());

		// provide form names
		context.put("form-pagesize", FORM_PAGESIZE);
		context.put("form-open", FORM_OPEN);
		context.put("form-reply", FORM_REPLY);
		context.put("form-sendto", FORM_SENDTO);
		context.put("form-alias", FORM_ALIAS);
		context.put("form-alias-max-length",FORM_ALIAS_MAX_LENGTH);
		context.put("form-submit", BUTTON + "doUpdate");
		context.put("form-cancel", BUTTON + "doCancel");

		// in progress values
		if (state.getAttribute(STATE_OPTION_PAGESIZE) != null)
			context.put(STATE_OPTION_PAGESIZE, state.getAttribute(STATE_OPTION_PAGESIZE));
		if (state.getAttribute(STATE_OPTION_OPEN) != null)
			context.put(STATE_OPTION_OPEN, state.getAttribute(STATE_OPTION_OPEN));
		if (state.getAttribute(STATE_OPTION_REPLY) != null)
			context.put(STATE_OPTION_REPLY, state.getAttribute(STATE_OPTION_REPLY));
		if (state.getAttribute(STATE_OPTION_SENDTO) != null)
			context.put(STATE_OPTION_SENDTO, state.getAttribute(STATE_OPTION_SENDTO));
		if (state.getAttribute(STATE_OPTION_ALIAS) != null)
			context.put(STATE_OPTION_ALIAS, state.getAttribute(STATE_OPTION_ALIAS));

		// provide the channel
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			context.put("channel", channel);
		}
		catch (Exception ignore)
		{
		}

		// place the current alias, if any, in to context
		List all = aliasService.getAliases((String) state.getAttribute(STATE_CHANNEL_REF), 1, 1);
		if (!all.isEmpty()) context.put("alias", ((Alias) all.get(0)).getId());

		context.put("serverName", ServerConfigurationService.getServerName());

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(rundata).get("template");
		return template + "-customize";

	} // buildOptionsPanelContext

	/**
	 * doUpdate called for form input tags type="submit" named="eventSubmit_doUpdate" update/save from the options process
	 */
	public void doUpdate(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// collect & save in state (for possible form re-draw)
		// String pagesize = StringUtil.trimToZero(data.getParameters().getString(FORM_PAGESIZE));
		// state.setAttribute(STATE_OPTION_PAGESIZE, pagesize);
		String open = data.getParameters().getString(FORM_OPEN);
		state.setAttribute(STATE_OPTION_OPEN, open);
		String replyToList = data.getParameters().getString(FORM_REPLY);
		state.setAttribute(STATE_OPTION_REPLY, replyToList);
		String sendToList = data.getParameters().getString(FORM_SENDTO);
		state.setAttribute(STATE_OPTION_SENDTO, sendToList);
		String alias = StringUtils.trimToNull(data.getParameters().getString(FORM_ALIAS));
		state.setAttribute(STATE_OPTION_ALIAS, alias);

		MailArchiveChannel channel = null;
		try
		{
			channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
		}
		catch (Exception e)
		{
			addAlert(state, rb.getString("cannot1"));
            channel = null;
		}

		if (channel == null)
		{
			addAlert(state, rb.getString("theemaarc"));
			return;
		}
		// validate the email alias
		if (alias != null)
		{
			if (!Validator.checkEmailLocal(alias))
			{
				addAlert(state, rb.getString("theemaali"));
			}
		}

		// make sure we can get to the channel
		MailArchiveChannelEdit edit = null;
		try {
			edit = (MailArchiveChannelEdit) MailArchiveService.editChannel(channel.getReference());
		} catch (IdUnusedException e1) {
			addAlert(state, rb.getString("theemaali"));
		} catch (PermissionException e1) {
			addAlert(state, rb.getString("theemaali"));
		} catch (InUseException e1) {
			addAlert(state, rb.getString("theemaali"));		}
		
		// if all is well, save
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// get any current alias for this channel
			List all = aliasService.getAliases((String) state.getAttribute(STATE_CHANNEL_REF), 1, 1);
			String curAlias = null;
			if (!all.isEmpty()) curAlias = ((Alias) all.get(0)).getId();

			// alias from the form
			if (StringUtil.different(curAlias, alias))
			{
				boolean ok = false;

				// see if this alias exists
				if (alias != null)
				{
					try
					{
						String target = aliasService.getTarget(alias);

						// if so, is it this channel?
						ok = target.equals(channel.getReference());
					}
					catch (IdUnusedException e)
					{
						// not in use
						ok = true;
					}
				}
				else
				{
					// no alias is desired
					ok = true;
				}

				if (ok)
				{
					try
					{
						if ( alias == null ) {
							aliasService.removeTargetAliases(channel.getReference());
						} else {
							aliasService.setAlias(alias, channel.getReference());
						}
					}
          catch (IdInvalidException iie) {
              addAlert(state, rb.getString("theemaali4"));
          }
					catch (Exception any)
					{
						addAlert(state, rb.getString("theemaali2"));
					}
				}
				else
				{
					addAlert(state, rb.getString("theemaali3"));
				}
			}

			// if the alias saving went well, go on to the rest
			if (state.getAttribute(STATE_MESSAGE) == null)
			{
				boolean modified = false;
				// update the channel for open (if changed)
				boolean ss = Boolean.valueOf(open).booleanValue();
				if (channel.getOpen() != ss)
				{
					edit.setOpen(ss);
					modified = true;
				}
				
				ss = Boolean.valueOf(replyToList).booleanValue();
				if (channel.getReplyToList() != ss)
				{
					edit.setReplyToList(ss);
					modified = true;
				}
				ss = Boolean.valueOf(sendToList).booleanValue();
				if (channel.getSendToList() != ss)
				{
					edit.setSendToList(ss);
					modified = true;
				}
				
				if (modified)
				{
					MailArchiveService.commitChannel(edit);
				}
				else
				{
					MailArchiveService.cancelChannel(edit);
				}
				edit = null;

				// we are done with customization... back to the main (list) mode
				state.setAttribute(STATE_MODE, "list");

				// clear state temps.
				state.removeAttribute(STATE_OPTION_PAGESIZE);
				state.removeAttribute(STATE_OPTION_OPEN);
				state.removeAttribute(STATE_OPTION_REPLY);
				state.removeAttribute(STATE_OPTION_SENDTO);
				state.removeAttribute(STATE_OPTION_ALIAS);

				// re-enable auto-updates when going back to list mode
				enableObserver(state);
			}
		}

		// before leaving, make sure the edit was cleared
		if (edit != null)
		{
			MailArchiveService.cancelChannel(edit);
			edit = null;
		}

	} // doUpdate

	/**
	 * doCancel called for form input tags type="submit" named="eventSubmit_doCancel" cancel the options process
	 */
	public void doCancel(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// cancel the options
		cancelOptions();

		// we are done with customization... back to the main (list) mode
		state.setAttribute(STATE_MODE, "list");

		// clear state temps.
		state.removeAttribute(STATE_OPTION_PAGESIZE);
		state.removeAttribute(STATE_OPTION_OPEN);
		state.removeAttribute(STATE_OPTION_REPLY);
		state.removeAttribute(STATE_OPTION_SENDTO);
		state.removeAttribute(STATE_OPTION_ALIAS);

		// re-enable auto-updates when going back to list mode
		enableObserver(state);

	} // doCancel

	/**
	 * Fire up the permissions editor
	 */
	public void doPermissions(RunData data, Context context)
	{
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String channelRefStr = (String) state.getAttribute(STATE_CHANNEL_REF);
		Reference channelRef = EntityManager.newReference(channelRefStr);
		String siteRef = SiteService.siteReference(channelRef.getContext());

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setperm")
				+ SiteService.getSiteDisplay(channelRef.getContext()));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "mail.");

		// ... pass the resource loader object
		ResourceLoader pRb = new ResourceLoader("permissions");
		HashMap<String, String> pRbValues = new HashMap<String, String>();
		for (Iterator<Entry<String, String>> iKeys = pRb.entrySet().iterator();iKeys.hasNext();)
		{
			Entry<String, String> entry = iKeys.next(); 
			String key = entry.getKey();
			pRbValues.put(key, entry.getValue());
		}
		state.setAttribute("permissionDescriptions",  pRbValues);
		
	} // doPermissions

	private Search getSearchFilter(String search, int first, int last)
	{
		return new MailMessageSearchFilter(search, first, last);
	}

	protected class MailMessageSearchFilter extends Search implements SearchFilter
	{
		public MailMessageSearchFilter(String searchString, int first, int last)
		{
			super(searchString);
			this.setStart(first);
			this.setLimit(last);
		}

		// Deal with the name mis-match
		public String getSearchString()
		{
			return this.getQueryString();
		}

		/**
		 * Does this object satisfy the criteria of the filter?
		 * 
		 * @param o
		 *        The object to test.
		 * @return true if the object is accepted by the filter, false if not.
		 */
		public boolean accept(Object o)
		{
			// we want to test only messages
			if (!(o instanceof MailArchiveMessage))
			{
				return false;
			}

			String searchStr = getSearchString();
			if ( searchStr != null ) 
			{
				MailArchiveMessage msg = (MailArchiveMessage) o;
				if (StringUtils.containsIgnoreCase(msg.getMailArchiveHeader().getSubject(), searchStr)
					|| StringUtils.containsIgnoreCase(msg.getMailArchiveHeader().getFromAddress(), searchStr)
					|| StringUtils.containsIgnoreCase(FormattedText.convertFormattedTextToPlaintext(msg.getBody()), searchStr))

				{
					return false;
				}
			}

			return true;
		}
	}

        /**
         * get the Message Threshold - above which searching is disabled
         */
        private int getMessageThreshold()
        {
                return ServerConfigurationService.getInt("sakai.mailbox.search-threshold",
                                MESSAGE_THRESHOLD_DEFAULT);
        }


} // MailboxAction

