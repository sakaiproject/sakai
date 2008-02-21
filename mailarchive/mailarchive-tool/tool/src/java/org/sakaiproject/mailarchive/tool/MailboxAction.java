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

package org.sakaiproject.mailarchive.tool;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.cover.AliasService;
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
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.mailarchive.api.MailArchiveChannel;
import org.sakaiproject.mailarchive.api.MailArchiveChannelEdit;
import org.sakaiproject.mailarchive.api.MailArchiveMessage;
import org.sakaiproject.mailarchive.cover.MailArchiveService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * MailboxAction is a the Sakai mailbox tool.
 * </p>
 */
public class MailboxAction extends PagedResourceActionII
{
	private static ResourceLoader rb = new ResourceLoader("email");

	/** portlet configuration parameter names. */
	private static final String PARAM_CHANNEL = "channel";

	private static final String PARAM_SITE = "site";

	/** Configure form field names. */
	private static final String FORM_CHANNEL = "channel";

	private static final String FORM_PAGESIZE = "pagesize";

	private static final String FORM_OPEN = "open";

	private static final String FORM_ALIAS = "alias";

	/** List request parameters. */
	private static final String VIEW_ID = "view-id";

	/** state attribute names. */
	private static final String STATE_CHANNEL_REF = "channelId";

	private static final String STATE_ASCENDING = "ascending";

	private static final String STATE_SORT = "sort";

	private static final String STATE_VIEW_HEADERS = "view-headers";

	private static final String STATE_OPTION_PAGESIZE = "optSize";

	private static final String STATE_OPTION_OPEN = "optOpen";

	private static final String STATE_OPTION_ALIAS = "optAlias";

	/** Sort codes. */
	private static final int SORT_FROM = 0;

	private static final int SORT_DATE = 1;

	private static final int SORT_SUBJECT = 2;

	/** paging */
	private static final String STATE_ALL_MESSAGES = "allMessages";

	private static final String STATE_MSG_VIEW_ID = "msg-id";

	/** paging */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#readResourcesPage(org.sakaiproject.service.framework.session.SessionState, int, int)
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{

		List rv = new Vector();
		if (state.getAttribute(STATE_ALL_MESSAGES) != null)
		{
			rv = (List) state.getAttribute(STATE_ALL_MESSAGES);
		}

		PagingPosition page = new PagingPosition(first, last);
		page.validate(rv.size());
		rv = rv.subList(page.getFirst() - 1, page.getLast());

		return rv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.cheftool.PagedResourceActionII#sizeResources(org.sakaiproject.service.framework.session.SessionState)
	 */
	protected int sizeResources(SessionState state)
	{
		int size = 0;
		if (state.getAttribute(STATE_ALL_MESSAGES) != null)
		{
			size = ((List) state.getAttribute(STATE_ALL_MESSAGES)).size();
		}
		return size;
	}

	/**
	 * Sort support for email messages.
	 */
	private class MyComparator implements Comparator
	{
		/** the criteria - a sort code. */
		private int m_criteria = SORT_DATE;

		/** True for ascending sort, false for descending. */
		private boolean m_asc = true;

		/**
		 * constructor
		 * 
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. "true" if ascending; "false" otherwise.
		 */
		public MyComparator(int criteria, boolean asc)
		{
			m_criteria = criteria;
			m_asc = asc;

		} // MyComparator

		/**
		 * Compares its two arguments for order.
		 * 
		 * @param o1
		 *        The first object.
		 * @param o2
		 *        The second object.
		 * @return a a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
		 */
		public int compare(Object o1, Object o2)
		{
			// make these MailArchiveMessages - let the class cast exception be thrown if not
			MailArchiveMessage m1 = (MailArchiveMessage) o1;
			MailArchiveMessage m2 = (MailArchiveMessage) o2;

			// if these are the same object (or equal - it may be cheaper to just let the comparison find the equality)
			if ((m1 == m2) /* || (m1.equals(m2)) */) return 0;

			// if it's ascending, use the compareTo value - if descending, reverse it
			int orderFactor = (m_asc ? 1 : -1);

			switch (m_criteria)
			{
				case SORT_FROM:
				{
					String addr1 = m1.getMailArchiveHeader().getFromAddress();
					String addr2 = m2.getMailArchiveHeader().getFromAddress();
					String rex = "^[^<>]*<[^<>@]*@[^<>@]*>[^<>@]*";
					if (addr1.matches(rex))
					{
						addr1 = addr1.substring(addr1.indexOf('<') + 1, addr1.indexOf('>'));
					}
					if (addr2.matches(rex))
					{
						addr2 = addr2.substring(addr2.indexOf('<') + 1, addr2.indexOf('>'));
					}
					return orderFactor * addr1.compareTo(addr2);
				}

				case SORT_DATE:
				{
					return orderFactor * m1.getMailArchiveHeader().getDateSent().compareTo(m2.getMailArchiveHeader().getDateSent());
				}

				case SORT_SUBJECT:
				{
					String subj1 = m1.getMailArchiveHeader().getSubject().toUpperCase();
					String subj2 = m2.getMailArchiveHeader().getSubject().toUpperCase();
					while (subj1.startsWith("RE:"))
					{
						subj1 = subj1.substring(subj1.indexOf(':') + 1).trim();
					}
					while (subj2.startsWith("RE:"))
					{
						subj2 = subj2.substring(subj2.indexOf(':') + 1).trim();
					}
					return orderFactor * subj1.compareTo(subj2);
				}
			}

			// trouble!
			Log.warn("chef", "MailboxAction.MyComparator - invalid sort: " + m_criteria);
			return 0;

		} // compare

	} // class MyComparator

	/**
	 * Implement this to return alist of all the resources that there are to page. Sort them as appropriate.
	 */
	protected List readAllResources(SessionState state)
	{
		// read all channel messages
		List allMessages = null;
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));

			allMessages = channel.getMessages(null, true);
		}
		catch (PermissionException e)
		{
		}
		catch (IdUnusedException e)
		{
		}

		// deal with no messages
		if (allMessages == null) return new Vector();

		String search = (String) state.getAttribute(STATE_SEARCH);

		// filter if searching
		if (search != null)
		{
			Vector filtered = new Vector();
			for (Iterator iMsgs = allMessages.iterator(); iMsgs.hasNext();)
			{
				MailArchiveMessage msg = (MailArchiveMessage) iMsgs.next();

				if (StringUtil.containsIgnoreCase(msg.getMailArchiveHeader().getSubject(), search)
						|| StringUtil.containsIgnoreCase(msg.getMailArchiveHeader().getFromAddress(), search)
						|| StringUtil.containsIgnoreCase(FormattedText.convertFormattedTextToPlaintext(msg.getBody()), search))
				{
					filtered.add(msg);
				}
			}

			allMessages = filtered;
		}

		// if other than ascending date, sort them all
		boolean ascending = ((Boolean) state.getAttribute(STATE_ASCENDING)).booleanValue();
		int sort = ((Integer) state.getAttribute(STATE_SORT)).intValue();
		if ((allMessages.size() > 1) && ((!ascending) || (sort != SORT_DATE)))
		{
			Collections.sort(allMessages, new MyComparator(sort, ascending));
		}

		return allMessages;

	} // readAllResources

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
			String channel = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = MailArchiveService.channelReference(ToolManager.getCurrentPlacement().getContext(),
						SiteService.MAIN_CONTAINER);
			}
			state.setAttribute(STATE_CHANNEL_REF, channel);

			if (state.getAttribute(STATE_ASCENDING) == null)
			{
				state.setAttribute(STATE_ASCENDING, new Boolean(false));
			}

			if (state.getAttribute(STATE_SORT) == null)
			{
				state.setAttribute(STATE_SORT, new Integer(SORT_DATE));
			}

			if (state.getAttribute(STATE_VIEW_HEADERS) == null)
			{
				state.setAttribute(STATE_VIEW_HEADERS, new Boolean(false));
			}

			// // setup the observer to notify our main panel
			// if (state.getAttribute(STATE_OBSERVER) == null)
			// {
			// // the delivery location for this tool
			// String deliveryId = clientWindowId(state, portlet.getID());
			//				
			// // the html element to update on delivery
			// String elementId = mainPanelUpdateId(portlet.getID());
			//				
			// // the event resource reference pattern to watch for
			// Reference r = EntityManager.newReference(channel);
			// String pattern = MailArchiveService.messageReference(r.getContext(), r.getId(), "");
			//
			// state.setAttribute(STATE_OBSERVER, new EventObservingCourier(deliveryId, elementId, pattern));
			// }
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

		if ("list".equals(mode))
		{
			return buildListModeContext(portlet, context, rundata, state);
		}

		else if ("confirm-remove".equals(mode))
		{
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
			Log.warn("chef", this + ".buildMainPanelContext: invalid mode: " + mode);
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

		// get the message
		MailArchiveMessage message = null;

		// prepare the sort of messages
		context.put("tlang", rb);
		prepPage(state);

		List messages = (List) state.getAttribute(STATE_ALL_MESSAGES);
		int pos = ((Integer) state.getAttribute(STATE_VIEW_ID)).intValue();
		message = (MailArchiveMessage) messages.get(pos);
		
		// fix for SAK-5879
		// make sure STATE_MSG_VIEW_ID is updated so that Confirm mode will have access to the correct message id
		state.setAttribute(STATE_MSG_VIEW_ID, message.getId());
		//
		

		boolean goNext = state.getAttribute(STATE_NEXT_EXISTS) != null;
		boolean goPrev = state.getAttribute(STATE_PREV_EXISTS) != null;
		context.put("goPPButton", new Boolean(goPrev));
		context.put("goNPButton", new Boolean(goNext));

		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			context.put("email", message);

			allowDelete = channel.allowRemoveMessage(message);
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

	/*
	 * 
	 */
	/*
	 * private MailArchiveMessage adjustPaging(Context context, SessionState state) { MailArchiveMessage message = null; List messages = (List) state.getAttribute(STATE_ALL_MESSAGES); int pos = ((Integer) state.getAttribute(STATE_VIEW_ID)).intValue();
	 * boolean goNext = state.getAttribute(STATE_GO_NEXT) != null; boolean goPrev = state.getAttribute(STATE_GO_PREV) != null; state.removeAttribute(STATE_GO_NEXT); state.removeAttribute(STATE_GO_PREV); if (goNext || goPrev) { if (goNext) { if (pos <
	 * messages.size()-1) { pos++; } } if (goPrev) { if (pos > 0) { pos--; } } } if (pos == -1) { pos = 0; } if (pos >= messages.size()) { pos = messages.size() -1; } message = (MailArchiveMessage) messages.get(pos); //get the message id if (pos > 0) {
	 * context.put("goPPButton", Boolean.TRUE); } else { context.put("goPPButton", Boolean.FALSE); } if (pos < sizeResources(state)-1) { context.put("goNPButton", Boolean.TRUE); } else { context.put("goNPButton", Boolean.FALSE); } return message; }
	 */

	/**
	 * Build the context for the confirm remove mode (in the Main panel).
	 */
	private String buildConfirmModeContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// get the message
		context.put("tlang", rb);
		MailArchiveMessage message = null;
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			message = channel.getMailArchiveMessage((String) state.getAttribute(STATE_MSG_VIEW_ID));
			context.put("email", message);
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
		state.setAttribute(STATE_ALL_MESSAGES, readAllResources(state));

		// prepare the page of messages
		context.put("tlang", rb);
		List messages = prepPage(state);
		context.put("messages", messages);

		// build the menu
		Menu bar = new MenuImpl(portlet, rundata, (String) state.getAttribute(STATE_ACTION));

		// add paging commands
		// addListPagingMenus(bar, state);

		// add the search commands
		// addSearchMenus(bar, state);

		// add the refresh commands
		// addRefreshMenus(bar, state);

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

		// output the search field
		context.put(STATE_SEARCH, state.getAttribute(STATE_SEARCH));

		// if (state.getAttribute(STATE_NUM_MESSAGES) != null)
		// {
		// context.put("num-messages", state.getAttribute(STATE_NUM_MESSAGES));
		// }

		// eventSubmit value and id field for drill down
		context.put("view-id", VIEW_ID);

		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		context.put("sort-by", state.getAttribute(STATE_SORT));
		context.put("sort-order", state.getAttribute(STATE_ASCENDING));

		pagingInfoToContext(state, context);

		// the aliases for the channel
		List all = AliasService.getAliases((String) state.getAttribute(STATE_CHANNEL_REF));

		// and the aliases for the site (context)
		Reference channelRef = EntityManager.newReference((String) state.getAttribute(STATE_CHANNEL_REF));
		String siteRef = SiteService.siteReference(channelRef.getContext());
		all.addAll(AliasService.getAliases(siteRef));

		context.put("aliases", all);
		context.put("nonAlias", channelRef.getContext());
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

		int pos = -1;
		List resources = (List) state.getAttribute(STATE_ALL_MESSAGES);
		if (resources != null)
		{
			for (int i = 0; i < resources.size(); i++)
			{
				// if this is the one, return this index
				if (((MailArchiveMessage) (resources.get(i))).getId().equals(id)) pos = i;
			}
		}
		state.setAttribute(STATE_VIEW_ID, new Integer(pos));

		// disable auto-updates while in view mode
		disableObservers(state);

		// set the message id
		// state.setAttribute(STATE_VIEW_ID, runData.getParameters().getString(VIEW_ID));

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

		// cleanup
		state.removeAttribute(STATE_VIEW_ID);
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
		state.setAttribute(STATE_VIEW_HEADERS, new Boolean(true));

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
		state.setAttribute(STATE_VIEW_HEADERS, new Boolean(false));

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
			state.setAttribute(STATE_ASCENDING, new Boolean(order));
		}

		// set state
		else
		{
			state.setAttribute(STATE_SORT, new Integer(SORT_FROM));
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
			state.setAttribute(STATE_ASCENDING, new Boolean(order));
		}

		// set state
		else
		{
			state.setAttribute(STATE_SORT, new Integer(SORT_DATE));
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
			state.setAttribute(STATE_ASCENDING, new Boolean(order));
		}

		// set state
		else
		{
			state.setAttribute(STATE_SORT, new Integer(SORT_SUBJECT));
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

		// remove
		try
		{
			MailArchiveChannel channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));

			String msgId = (String) state.getAttribute(STATE_MSG_VIEW_ID);
			
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
		context.put("form-alias", FORM_ALIAS);
		context.put("form-submit", BUTTON + "doUpdate");
		context.put("form-cancel", BUTTON + "doCancel");

		// in progress values
		if (state.getAttribute(STATE_OPTION_PAGESIZE) != null)
			context.put(STATE_OPTION_PAGESIZE, state.getAttribute(STATE_OPTION_PAGESIZE));
		if (state.getAttribute(STATE_OPTION_OPEN) != null) context.put(STATE_OPTION_OPEN, state.getAttribute(STATE_OPTION_OPEN));
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
		List all = AliasService.getAliases((String) state.getAttribute(STATE_CHANNEL_REF), 1, 1);
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
		String alias = StringUtil.trimToNull(data.getParameters().getString(FORM_ALIAS));
		state.setAttribute(STATE_OPTION_ALIAS, alias);

		MailArchiveChannel channel = null;
		try
		{
			channel = MailArchiveService.getMailArchiveChannel((String) state.getAttribute(STATE_CHANNEL_REF));
		}
		catch (Exception e)
		{
			addAlert(state, rb.getString("cannot1"));
		}

		// first validate - page size
		// int size = 0;
		// try
		// {
		// size = Integer.parseInt(pagesize);
		// if (size <= 0)
		// {
		// addAlert(state,rb.getString("pagsiz"));
		// }
		// }
		// catch (Exception any)
		// {
		// addAlert(state, rb.getString("pagsiz"));
		// }

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
		try
		{
			edit = (MailArchiveChannelEdit) MailArchiveService.editChannel(channel.getReference());
		}
		catch (Exception any)
		{
			addAlert(state, rb.getString("theemaarc"));
		}

		// if all is well, save
		if (state.getAttribute(STATE_MESSAGE) == null)
		{
			// get any current alias for this channel
			List all = AliasService.getAliases((String) state.getAttribute(STATE_CHANNEL_REF), 1, 1);
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
						String target = AliasService.getTarget(alias);

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
						// first, clear any alias set to this channel
						AliasService.removeTargetAliases(channel.getReference());

						// then add the desired alias
						if (alias != null)
						{
							//check if alias = "postmaster"
							// SAK-13054
							if (alias.equalsIgnoreCase("postmaster")) {
								addAlert(state, rb.getString("theemaali4"));
							} else {
								AliasService.setAlias(alias, channel.getReference());
							}
						}
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
				// update the channel for open (if changed)
				boolean ss = new Boolean(open).booleanValue();
				if (channel.getOpen() != ss)
				{
					edit.setOpen(ss);
					MailArchiveService.commitChannel(edit);
				}
				else
				{
					MailArchiveService.cancelChannel(edit);
				}
				edit = null;

				// // update the tool config & state for page size (if different)
				// if (size != ((Integer) state.getAttribute(STATE_PAGESIZE)).intValue())
				// {
				// state.setAttribute(STATE_PAGESIZE, new Integer(size));
				// Placement placement = ToolManager.getCurrentPlacement();
				// placement.getPlacementConfig().setProperty(PARAM_PAGESIZE, Integer.toString(size));
				//		
				// // commit the options change
				// saveOptions();
				// }
				// else
				// {
				// cancelOptions();
				// }

				// we are done with customization... back to the main (list) mode
				state.setAttribute(STATE_MODE, "list");

				// clear state temps.
				state.removeAttribute(STATE_OPTION_PAGESIZE);
				state.removeAttribute(STATE_OPTION_OPEN);
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

	} // doPermissions

} // MailboxAction

