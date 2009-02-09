/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 Sakai Foundation
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

package org.sakaiproject.site.tool;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuDivider;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuField;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.courier.api.ObservingCourier;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * SitesAction is the Sakai admin sites editor.
 * </p>
 */
public class AdminSitesAction extends PagedResourceActionII
{
	/** State holding the site id for site id search. */
	protected static final String STATE_SEARCH_SITE_ID = "search_site";

	/** State holding the user id for user id search. */
	protected static final String STATE_SEARCH_USER_ID = "search_user";

	protected static final String FORM_SEARCH_SITEID = "search_site";

	protected static final String FORM_SEARCH_USERID = "search_user";

	private static ResourceLoader rb = new ResourceLoader("admin");

	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));
		String siteId = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH_SITE_ID));
		String userId = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH_USER_ID));

		// Boolean userOnly = (Boolean) state.getAttribute(STATE_SEARCH_USER);

		// if site id specified, use that
		if (siteId != null)
		{
			List rv = new Vector();
			try
			{
				Site site = SiteService.getSite(siteId);
				rv.add(site);
			}
			catch (IdUnusedException e)
			{
			}

			return rv;
		}

		// if userId specified, use that
		else if (userId != null)
		{
			List rv = new Vector();
			try
			{
				Site userSite = SiteService.getSite(SiteService.getUserSiteId(userId));
				rv.add(userSite);
			}
			catch (IdUnusedException e)
			{
			}

			return rv;
		}

		// search for non-user sites, using the criteria
		else if (search != null)
		{
			return SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.NON_USER, null, search, null,
					org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, new PagingPosition(first, last));
		}

		// otherwise just show a page of all
		else
		{
			return SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY, null, search, null,
					org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, new PagingPosition(first, last));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		// search?
		String search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));
		String siteId = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH_SITE_ID));
		String userId = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH_USER_ID));

		// search for non-user sites, using the criteria
		if (siteId != null)
		{
			try
			{
				Site site = SiteService.getSite(siteId);
				return 1;
			}
			catch (IdUnusedException e)
			{
			}

			return 0;
		}

		else if (userId != null)
		{
			try
			{
				Site userSite = SiteService.getSite(SiteService.getUserSiteId(userId));
				return 1;
			}
			catch (IdUnusedException e)
			{
			}

			return 0;
		}

		else if (search != null)
		{
			return SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.NON_USER, null, search, null);
		}

		else
		{
			return SiteService.countSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY, null, search, null);
		}
	}

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

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
		// String pattern = SiteService.siteReference("");
		//
		// state.setAttribute(STATE_OBSERVER, new EventObservingCourier(deliveryId, elementId, pattern));
		// }
	}

	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		
		// if not logged in as the super user, we won't do anything
		if (!SecurityService.isSuperUser())
		{
			context.put("tlang",rb);
			return (String) getContext(rundata).get("template") + "_noaccess";
		}

		String template = null;

		// get the Sakai session
		Session session = SessionManager.getCurrentSession();

		// get the Tool session
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		// check mode and dispatch
		String mode = (String) state.getAttribute("mode");
		if (mode == null)
		{
			template = buildListContext(state, context);
		}
		else if (mode.equals("new"))
		{
			template = buildNewContext(state, context);
		}
		else if (mode.equals("edit"))
		{
			template = buildEditContext(state, context);
		}
		else if (mode.equals("confirm"))
		{
			template = buildConfirmRemoveContext(state, context);
		}
		else if (mode.equals("saveas"))
		{
			template = buildSaveasContext(state, context);
		}

		else if (mode.equals("pages"))
		{
			template = buildPagesContext(state, context);
		}
		else if (mode.equals("newPage"))
		{
			template = buildNewPageContext(state, context);
		}
		else if (mode.equals("editPage"))
		{
			template = buildEditPageContext(state, context);
		}

		else if (mode.equals("properties"))
		{
			template = buildPropertiesContext(state, context);
		}

		else if (mode.equals("groups"))
		{
			template = buildGroupsContext(state, context);
		}
		else if (mode.equals("newGroup"))
		{
			template = buildNewGroupContext(state, context);
		}
		else if (mode.equals("editGroup"))
		{
			template = buildEditGroupContext(state, context);
		}

		else if (mode.equals("tools"))
		{
			template = buildToolsContext(state, context);
		}
		else if (mode.equals("newTool"))
		{
			template = buildNewToolContext(state, context);
		}
		else if (mode.equals("editTool"))
		{
			template = buildEditToolContext(state, context);
		}

		// else if (mode.equals("newMember"))
		// {
		// template = buildNewMemberContext(state, context);
		// }
		else
		{
			Log.warn("chef", "SitesAction: mode: " + mode);
			template = buildListContext(state, context);
		}

		String prefix = (String) getContext(rundata).get("template");
		return prefix + template;

	} // buildMainPanelContext

	/**
	 * Build the context for the main list mode.
	 */
	private String buildListContext(SessionState state, Context context)
	{
		// put the service in the context (used for allow update calls on each site)
		context.put("service", SiteService.getInstance());

		// prepare the paging of realms
		List sites = prepPage(state);
		context.put("sites", sites);

		// we need the Realms, too!
		context.put("realms", AuthzGroupService.getInstance());

		// build the menu
		Menu bar = new MenuImpl();
		if (SiteService.allowAddSite(""))
		{
			bar.add(new MenuEntry(rb.getString("sitact.newsit"), "doNew"));
		}

		// add the paging commands
		//addListPagingMenus(bar, state);
		int pageSize = Integer.valueOf(state.getAttribute(STATE_PAGESIZE).toString()).intValue();
		int currentPageNubmer = Integer.valueOf(state.getAttribute(STATE_CURRENT_PAGE).toString()).intValue();
		int startNumber = pageSize * (currentPageNubmer - 1) + 1;
		int endNumber = pageSize * currentPageNubmer;

		int totalNumber = 0;
		try
		{
			totalNumber = Integer.valueOf(state.getAttribute(STATE_NUM_MESSAGES).toString()).intValue();
		}
		catch (java.lang.NullPointerException ignore) {}
		catch (java.lang.NumberFormatException ignore) {}

		if (totalNumber < endNumber) endNumber = totalNumber;

		context.put("startNumber", Integer.valueOf(startNumber));
		context.put("endNumber", Integer.valueOf(endNumber));
		context.put("totalNumber", Integer.valueOf(totalNumber));
		pagingInfoToContext(state, context);

		// add the search commands
		addSearchMenus(bar, state);

		// more search
		bar.add(new MenuDivider());
		bar
				.add(new MenuField(FORM_SEARCH_SITEID, "toolbar2", "doSearch_site_id", (String) state
						.getAttribute(STATE_SEARCH_SITE_ID)));
		bar.add(new MenuEntry(rb.getString("sitlis.sid"), null, true, MenuItem.CHECKED_NA, "doSearch_site_id", "toolbar2"));
		if (state.getAttribute(STATE_SEARCH_SITE_ID) != null)
		{
			bar.add(new MenuEntry(rb_praII.getString("sea.cleasea"), "doSearch_clear"));
		}
		bar.add(new MenuDivider());
		bar
				.add(new MenuField(FORM_SEARCH_USERID, "toolbar3", "doSearch_user_id", (String) state
						.getAttribute(STATE_SEARCH_USER_ID)));
		bar.add(new MenuEntry(rb.getString("sitlis.uid"), null, true, MenuItem.CHECKED_NA, "doSearch_user_id", "toolbar3"));
		if (state.getAttribute(STATE_SEARCH_USER_ID) != null)
		{
			bar.add(new MenuEntry(rb_praII.getString("sea.cleasea"), "doSearch_clear"));
		}

		// add the refresh commands
		addRefreshMenus(bar, state);

		if (bar.size() > 0)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		return "_list";

	} // buildListContext

	/**
	 * Build the context for the new site mode.
	 */
	private String buildNewContext(SessionState state, Context context)
	{
		// name the html form for user edit fields
		context.put("tlang", rb);
		context.put("form-name", "site-form");

		return "_edit";

	} // buildNewContext

	/**
	 * Build the context for the edit site mode.
	 */
	private String buildEditContext(SessionState state, Context context)
	{
		// get the site to edit
		context.put("tlang", rb);
		Site site = (Site) state.getAttribute("site");
		context.put("site", site);

		// name the html form for user edit fields
		context.put("form-name", "site-form");

		// build the menu
		// we need the form fields for the remove...
		Menu bar = new MenuImpl();
		if (SiteService.allowRemoveSite(site.getId()))
		{
			bar.add(new MenuEntry(rb.getString("sitact.remsit"), null, true, MenuItem.CHECKED_NA, "doRemove", "site-form"));
		}

		bar.add(new MenuEntry(rb.getString("sitact.savas"), null, true, MenuItem.CHECKED_NA, "doSaveas_request", "site-form"));

		context.put(Menu.CONTEXT_MENU, bar);

		return "_edit";

	} // buildEditContext

	/**
	 * Build the context for the new site mode.
	 */
	private String buildConfirmRemoveContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// get the site to edit
		Site site = (Site) state.getAttribute("site");
		context.put("site", site);

		return "_confirm_remove";

	} // buildConfirmRemoveContext

	/**
	 * Build the context for the new site mode.
	 */
	private String buildSaveasContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// get the site to edit
		Site site = (Site) state.getAttribute("site");
		context.put("site", site);

		return "_saveas";

	} // buildSaveasContext

	/**
	 * Build the context for the new member mode
	 */
	/*
	 * private String buildNewMemberContext(SessionState state, Context context) { return "_add_member"; } // buildNewMemberContext
	 */

	/**
	 * Build the context for the pages display in edit mode.
	 */
	private String buildPagesContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// get the site to edit
		Site site = (Site) state.getAttribute("site");
		context.put("site", site);

		// put all site's pages into the context
		List pages = site.getPages();
		context.put("pages", pages);

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("sitact.newpag"), "doNew_page"));
		context.put(Menu.CONTEXT_MENU, bar);

		return "_pages";

	} // buildPagesContext

	/**
	 * Build the context for the new page mode.
	 */
	private String buildNewPageContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// name the html form for user edit fields
		context.put("form-name", "page-form");

		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");

		context.put("site", site);
		context.put("page", page);

		context.put("layouts", layoutsList());

		return "_edit_page";

	} // buildNewPageContext

	/**
	 * Build the context for the edit page mode.
	 */
	private String buildEditPageContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// name the html form for user edit fields
		context.put("form-name", "page-form");

		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");

		context.put("site", site);
		context.put("page", page);

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("sitact.rempag"), null, true, MenuItem.CHECKED_NA, "doRemove_page"));
		context.put(Menu.CONTEXT_MENU, bar);

		context.put("layouts", layoutsList());

		return "_edit_page";

	} // buildEditPageContext

	/**
	 * Build the context for the properties edit in edit mode.
	 */
	private String buildPropertiesContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// get the site to edit
		Site site = (Site) state.getAttribute("site");
		context.put("site", site);

		return "_properties";
	}

	/**
	 * Build the context for the groups display in edit mode.
	 */
	private String buildGroupsContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// get the site to edit
		Site site = (Site) state.getAttribute("site");
		context.put("site", site);

		// put all site's groups into the context
		Collection groups = site.getGroups();
		context.put("groups", groups);

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("sitact.newgrp"), "doNew_group"));
		context.put(Menu.CONTEXT_MENU, bar);

		return "_groups";

	} // buildGroupsContext

	/**
	 * Build the context for the new group mode.
	 */
	private String buildNewGroupContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// name the html form for user edit fields
		context.put("form-name", "page-form");

		Site site = (Site) state.getAttribute("site");
		Group group = (Group) state.getAttribute("group");

		context.put("site", site);
		context.put("group", group);

		return "_edit_group";

	} // buildNewGroupContext

	/**
	 * Build the context for the edit group mode.
	 */
	private String buildEditGroupContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// name the html form for user edit fields
		context.put("form-name", "group-form");

		Site site = (Site) state.getAttribute("site");
		Group group = (Group) state.getAttribute("group");

		context.put("site", site);
		context.put("group", group);

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("sitact.remgrp"), null, true, MenuItem.CHECKED_NA, "doRemove_group"));
		context.put(Menu.CONTEXT_MENU, bar);

		return "_edit_group";

	} // buildEditGroupContext

	/**
	 * Build the context for the tools display in edit mode.
	 */
	private String buildToolsContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// get the site to edit
		Site site = (Site) state.getAttribute("site");
		context.put("site", site);

		// get the page being edited
		SitePage page = (SitePage) state.getAttribute("page");
		context.put("page", page);

		// put all page's tools into the context
		List tools = page.getTools();
		context.put("tools", tools);

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("sitact.newtoo"), "doNew_tool"));
		context.put(Menu.CONTEXT_MENU, bar);

		return "_tools";

	} // buildToolsContext

	/**
	 * Build the context for the new tool mode.
	 */
	private String buildNewToolContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// name the html form for user edit fields
		context.put("form-name", "tool-form");

		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");

		context.put("site", site);
		context.put("page", page);
		context.put("tool", tool);

		List features = findNonHelperTools();
		context.put("features", features);

		return "_edit_tool";

	} // buildNewToolContext

	/**
	 * Get the list of all tools that are not helper tools.
	 * 
	 * @return The list of all tools that are not helper to.
	 */
	private List findNonHelperTools()
	{
		class ToolTitleComparator implements Comparator{
			public int compare(Object tool0, Object tool1) {
				return ((Tool)tool0).getTitle().compareTo( ((Tool)tool1).getTitle() );
			}
		}
		
		// get all tools
		Set all = ToolManager.findTools(null, null);

		// get the helpers
		Set categories = new HashSet();
		categories.add("sakai.helper");
		Set helpers = ToolManager.findTools(categories, null);

		// remove the helpers from all
		all.removeAll(helpers);

		// make a list for sorting
		List features = new Vector();
		features.addAll(all);
		//Collections.sort(features);
		Collections.sort(features, new ToolTitleComparator());


		return features;
	}

	/**
	 * Build the context for the edit tool mode.
	 */
	private String buildEditToolContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		// name the html form for user edit fields
		context.put("form-name", "tool-form");

		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");

		context.put("site", site);
		context.put("page", page);
		context.put("tool", tool);

		List features = findNonHelperTools();
		context.put("features", features);

		context.put("toolReg", tool.getTool());

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("sitact.remtoo"), null, true, MenuItem.CHECKED_NA, "doRemove_tool"));
		context.put(Menu.CONTEXT_MENU, bar);

		return "_edit_tool";

	} // buildEditToolContext

	/**
	 * Handle a request for a new site.
	 */
	public void doNew(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "new");

		// mark the site as new, so on cancel it can be deleted
		state.setAttribute("new", "true");

		// disable auto-updates while in view mode
		ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
		if (courier != null) courier.disable();

	} // doNew

	/**
	 * Handle a request to edit a site.
	 */
	public void doEdit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");

		if (SiteService.allowUpdateSite(id))
		{
			// get the site
			try
			{
				Site site = SiteService.getSite(id);
				state.setAttribute("site", site);

				// RealmEdit realm = AuthzGroupService.editRealm("/site/" + id); // %%% use a site service call -ggolden
				// state.setAttribute("realm", realm);

				state.setAttribute("mode", "edit");

				// disable auto-updates while in view mode
				ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
				if (courier != null) courier.disable();
			}
			catch (IdUnusedException e)
			{
				Log.warn("chef", "SitesAction.doEdit: site not found: " + id);

				addAlert(state, rb.getString("siteact.site") + " " + id + " " + rb.getString("siteact.notfou"));
				state.removeAttribute("mode");

				// make sure auto-updates are enabled
				enableObserver(state);
			}
		}

		else
		{
			addAlert(state, rb.getString("youdonot1") + " " + id);
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

	} // doEdit

	/**
	 * Handle a request to save the site edit (from the site edit form).
	 */
	public void doSave(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		doSave_edit(data, context);

	} // doSave

	/**
	 * Handle a request to save the edit from either page or tools list mode - no form to read in.
	 */
	public void doSave_props_edit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		// read the properties form
		readPropertiesForm(data, state);

		doSave_edit(data, context);
	}

	/**
	 * Handle a request to save the edit from either page or tools list mode - no form to read in.
	 */
	public void doSave_edit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// commit the change
		Site site = (Site) state.getAttribute("site");
		if (site != null)
		{
			// bring the mail archive service's channel for this site in sync with the site's setting
			// syncWithMailArchive(site);

			try
			{
				SiteService.save(site);
			}
			catch (PermissionException e)
			{
				Log.warn("chef", "SitesAction.doSave_edit: " + e);
			}
			catch (IdUnusedException e)
			{
				Log.warn("chef", "SitesAction.doSave_edit: " + e);
			}

			// save the realm, too
			// RealmEdit realm = (RealmEdit) state.getAttribute("realm");
			// AuthzGroupService.commitEdit(realm);
		}

		// cleanup
		cleanState(state);

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");

	} // doSave_edit

	/**
	 * Go into saveas mode
	 */
	public void doSaveas_request(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		// go to saveas mode
		state.setAttribute("mode", "saveas");

	} // doSaveas_request

	/**
	 * Handle a request to save-as the site as a new site.
	 */
	public void doSaveas(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form
		String id = data.getParameters().getString("id");

		// get the site to copy from
		Site site = (Site) state.getAttribute("site");

		try
		{
			// make a new site with this id and as a structural copy of site
			Site newSite = SiteService.addSite(id, site);
		}
		catch (IdUsedException e)
		{
			addAlert(state, rb.getString("sitact.thesitid"));
			return;
		}
		catch (IdInvalidException e)
		{
			addAlert(state, rb.getString("sitact.thesitid2"));
			return;
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("sitact.youdonot2"));
			return;
		}

		cleanState(state);

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");

	} // doSaveas

	/**
	 * cancel the saveas request, return to edit
	 */
	public void doCancel_saveas(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// return to main mode
		state.setAttribute("mode", "edit");

	} // doCancel_saveas

	/**
	 * doCancel called when "eventSubmit_doCancel" is in the request parameters to cancel site edits
	 */
	public void doCancel(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// cancel the realm edit - it will be removed if the site is removed
		// RealmEdit realm = (RealmEdit) state.getAttribute("realm");
		// if (realm != null)
		// {
		// AuthzGroupService.cancelEdit(realm);
		// }

		// get the site
		Site site = (Site) state.getAttribute("site");
		if (site != null)
		{
			// if this was a new, delete the site
			if ("true".equals(state.getAttribute("new")))
			{
				// remove the site
				try
				{
					SiteService.removeSite(site);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("sitact.youdonot3") + " " + site.getId());
				}
			}
		}

		// cleanup
		cleanState(state);

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

	} // doCancel

	/**
	 * doRemove called when "eventSubmit_doRemove" is in the request parameters to confirm removal of the site
	 */
	public void doRemove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		// go to remove confirm mode
		state.setAttribute("mode", "confirm");

	} // doRemove

	/**
	 * doRemove_confirmed called when "eventSubmit_doRemove_confirmed" is in the request parameters to remove the site
	 */
	public void doRemove_confirmed(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the site
		Site site = (Site) state.getAttribute("site");

		// cancel the realm edit - the site remove will remove the realm
		// RealmEdit realm = (RealmEdit) state.getAttribute("realm");
		// AuthzGroupService.cancelEdit(realm);

		// remove the site
		try
		{
			SiteService.removeSite(site);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("sitact.youdonot3") + " " + site.getId());
		}

		// cleanup
		cleanState(state);

		// go to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");

	} // doRemove_confirmed

	/**
	 * doCancel_remove called when "eventSubmit_doCancel_remove" is in the request parameters to cancel site removal
	 */
	public void doCancel_remove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// return to edit mode
		state.setAttribute("mode", "edit");

	} // doCancel_remove

	/**
	 * Read the site form and update the site in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readSiteForm(RunData data, SessionState state)
	{
		// read the form
		String id = StringUtil.trimToNull(data.getParameters().getString("id"));
		String title = StringUtil.trimToNull(data.getParameters().getString("title"));
		String type = StringUtil.trimToNull(data.getParameters().getString("type"));
		String shortDescription = StringUtil.trimToNull(data.getParameters().getString("shortDescription"));
		String description = StringUtil.trimToNull(data.getParameters().getString("description"));
		boolean joinable = data.getParameters().getBoolean("joinable");
		String joinerRole = StringUtil.trimToNull(data.getParameters().getString("joinerRole"));
		String icon = StringUtil.trimToNull(data.getParameters().getString("icon"));
		String info = StringUtil.trimToNull(data.getParameters().getString("info"));
		boolean published = data.getParameters().getBoolean("published");
		String skin = StringUtil.trimToNull(data.getParameters().getString("skin"));
		boolean pubView = data.getParameters().getBoolean("pubView");
		boolean customOrder = data.getParameters().getBoolean("customOrder");

		// get the site
		Site site = (Site) state.getAttribute("site");

		// add if needed
		if (site == null)
		{
			try
			{
				site = SiteService.addSite(id, type);

				// put the site in the state
				state.setAttribute("site", site);
			}
			catch (IdUsedException e)
			{
				addAlert(state, rb.getString("sitact.thesitid"));
				return false;
			}
			catch (IdInvalidException e)
			{
				addAlert(state, rb.getString("sitact.thesitid2"));
				return false;
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("sitact.youdonot2"));
				return false;
			}
		}

		// update
		if (site != null)
		{
			if (joinable)
			{
				// check if there is a qualifed role in the role field
				if ((joinerRole == null) || (joinerRole.equals("")))
				{
					addAlert(state, rb.getString("sitact.sperol"));
					return false;
				}
				Vector roles = new Vector();
				Vector roleIds = new Vector();
				AuthzGroup realm = null;
				try
				{
					realm = AuthzGroupService.getAuthzGroup(site.getReference());
					roles.addAll(realm.getRoles());
				}
				catch (GroupNotDefinedException e)
				{
					// use the type's template, if defined
					String realmTemplate = "!site.template";
					if (type != null)
					{
						realmTemplate = realmTemplate + "." + type;
					}
					try
					{
						AuthzGroup r = AuthzGroupService.getAuthzGroup(realmTemplate);
						roles.addAll(r.getRoles());
					}
					catch (GroupNotDefinedException err)
					{
						try
						{
							AuthzGroup rr = AuthzGroupService.getAuthzGroup("!site.template");
							roles.addAll(rr.getRoles());
						}
						catch (GroupNotDefinedException ee)
						{
						}
					}
				}

				for (int i = 0; i < roles.size(); i++)
				{
					roleIds.add(((Role) roles.elementAt(i)).getId());
				}

				if (!roleIds.contains(joinerRole))
				{
					addAlert(state, rb.getString("sitact.sperol"));
					return false;
				}
			}

			site.setTitle(title);
			site.setShortDescription(shortDescription);
			site.setDescription(description);
			site.setJoinable(joinable);
			site.setJoinerRole(joinerRole);
			site.setIconUrl(icon);
			site.setInfoUrl(info);
			site.setSkin(skin);
			site.setType(type);
			site.setPubView(pubView);
			site.setPublished(published);
			site.setCustomPageOrdered(customOrder);
		}

		return true;

	} // readSiteForm

	/**
	 * Switch to page display mode within a site edit.
	 */
	public void doPages(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		state.setAttribute("mode", "pages");

	} // doPages

	/**
	 * Switch to property edit mode within a site edit.
	 */
	public void doProperties(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		state.setAttribute("mode", "properties");
	}

	/**
	 * Handle a request to create a new page in the site edit.
	 */
	public void doNew_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "newPage");

		// make the page so we have the id
		Site site = (Site) state.getAttribute("site");
		SitePage page = site.addPage();
		state.setAttribute("page", page);

		// mark the site as new, so on cancel it can be deleted
		state.setAttribute("newPage", "true");

	} // doNew_page

	/**
	 * Handle a request to create a new property in the site edit.
	 */
	public void doNew_property(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the properties form
		readPropertiesForm(data, state);
	}

	/**
	 * Edit an existing page.
	 */
	public void doEdit_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "editPage");

		String id = data.getParameters().getString("id");

		// get the page
		Site site = (Site) state.getAttribute("site");
		SitePage page = site.getPage(id);
		state.setAttribute("page", page);

	} // doEdit_page

	/**
	 * Move the page up in the order.
	 */
	public void doEdit_page_up(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String id = data.getParameters().getString("id");

		// get the page
		Site site = (Site) state.getAttribute("site");
		SitePage page = site.getPage(id);
		state.setAttribute("page", page);

		// move it
		page.moveUp();

	} // doEdit_page_up

	/**
	 * Move the page down in the order.
	 */
	public void doEdit_page_down(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String id = data.getParameters().getString("id");

		// get the page
		Site site = (Site) state.getAttribute("site");
		SitePage page = site.getPage(id);
		state.setAttribute("page", page);

		// move it
		page.moveDown();

	} // doEdit_page_down

	/**
	 * save the page edited, and save the site edit
	 */
	public void doSave_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readPageForm(data, state)) return;

		// done with the page
		state.removeAttribute("page");

		// commit the entire site edit
		doSave_edit(data, context);

	} // doSave_page

	/**
	 * save the page edited, and return to the pages mode
	 */
	public void doDone_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readPageForm(data, state)) return;

		// done with the page
		state.removeAttribute("page");

		// return to main mode
		state.setAttribute("mode", "pages");

	} // doDone_page

	/**
	 * cancel a page edit, return to the pages list
	 */
	public void doCancel_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");

		// if the page was new, remove it
		if ("true".equals(state.getAttribute("newPage")))
		{
			site.removePage(page);
		}

		// %%% do we need the old page around for a restore; did we already modify it? - ggolden

		// done with the page
		state.removeAttribute("page");

		// return to main mode
		state.setAttribute("mode", "pages");

	} // doCancel_page

	/**
	 * Handle a request to remove the page being edited.
	 */
	public void doRemove_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");

		// remove the page (no confirm)
		site.removePage(page);

		// done with the page
		state.removeAttribute("page");

		// return to pages mode
		state.setAttribute("mode", "pages");

	} // doRemove_page

	/**
	 * Switch to group display mode within a site edit.
	 */
	public void doGroups(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		state.setAttribute("mode", "groups");

	} // doGroups

	/**
	 * Edit an existing group.
	 */
	public void doEdit_group(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "editGroup");

		String id = data.getParameters().getString("id");

		// get the group
		Site site = (Site) state.getAttribute("site");
		Group group = site.getGroup(id);
		state.setAttribute("group", group);

	} // doEdit_group

	/**
	 * save the group edited, and save the site edit
	 */
	public void doSave_group(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readGroupForm(data, state)) return;

		// done with the group
		state.removeAttribute("group");

		// commit the entire site edit
		doSave_edit(data, context);

	} // doSave_group

	/**
	 * save the group edited, and return to the groups mode
	 */
	public void doDone_group(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readGroupForm(data, state)) return;

		// done with the group
		state.removeAttribute("group");

		// return to main mode
		state.setAttribute("mode", "groups");

	} // doDone_group

	/**
	 * cancel a group edit, return to the groups list
	 */
	public void doCancel_group(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site site = (Site) state.getAttribute("site");
		Group group = (Group) state.getAttribute("group");

		// if the page was new, remove it
		if ("true".equals(state.getAttribute("newGroup")))
		{
			site.removeGroup(group);
		}

		// %%% do we need the old group around for a restore; did we already modify it? - ggolden

		// done with the group
		state.removeAttribute("group");

		// return to main mode
		state.setAttribute("mode", "groups");

	} // doCancel_group

	/**
	 * Handle a request to remove the group being edited.
	 */
	public void doRemove_group(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site site = (Site) state.getAttribute("site");
		Group group = (Group) state.getAttribute("group");

		// remove the page (no confirm)
		site.removeGroup(group);

		// done with the page
		state.removeAttribute("group");

		// return to pages mode
		state.setAttribute("mode", "groups");

	} // doRemove_group

	/**
	 * Handle a request to create a new group in the site edit.
	 */
	public void doNew_group(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "newGroup");

		// make the page so we have the id
		Site site = (Site) state.getAttribute("site");
		Group group = site.addGroup();
		state.setAttribute("group", group);

		// mark the site as new, so on cancel it can be deleted
		state.setAttribute("newGroup", "true");

	} // doRemove_group

	/**
	 * Switch back to edit main info mode from another edit mode (like pages).
	 */
	public void doEdit_to_main(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute("mode", "edit");

	} // doEdit_to_main

	/**
	 * Switch back to edit main info mode properties edit mode
	 */
	public void doEdit_props_to_main(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the properties form
		readPropertiesForm(data, state);

		state.setAttribute("mode", "edit");
	}

	/**
	 * Read the page form and update the site in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readPageForm(RunData data, SessionState state)
	{
		// get the page - it's there
		SitePage page = (SitePage) state.getAttribute("page");

		// read the form
		String title = StringUtil.trimToNull(data.getParameters().getString("title"));
		page.setTitle(title);

		try
		{
			// this comes in 1 based, convert to 0 based
			int layout = Integer.parseInt(data.getParameters().getString("layout")) - 1;
			page.setLayout(layout);
		}
		catch (Exception e)
		{
			Log.warn("chef", this + ".readPageForm(): reading layout: " + e);
		}

		boolean popup = data.getParameters().getBoolean("popup");
		page.setPopup(popup);

		if (title == null)
		{
			addAlert(state, rb.getString("sitact.plespe"));
			return false;
		}
		else
		{
			return true;
		}

	} // readPageForm

	/**
	 * Read the properties form and update the site in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readPropertiesForm(RunData data, SessionState state)
	{
		// get the site
		Site site = (Site) state.getAttribute("site");

		ResourcePropertiesEdit props = site.getPropertiesEdit();
		
		// check each property for possible update
		for (Iterator i = props.getPropertyNames(); i.hasNext();)
		{
			String name = (String) i.next();
			String formValue = StringUtil.trimToNull(data.getParameters().getString("param_" + name));
			
			// update the properties or remove
			if (formValue != null)
			{
				props.addProperty(name, formValue);
			}
			else
			{
				props.removeProperty(name);
			}
		}
		
		// see if there's a new one
		String formName = StringUtil.trimToNull(data.getParameters().getString("new_name"));
		if (formName != null)
		{
			String formValue = StringUtil.trimToNull(data.getParameters().getString("new_value"));
			if (formValue != null)
			{
				props.addProperty(formName, formValue);
			}
		}

		return true;
	}

	/**
	 * Read the group form and update the site in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readGroupForm(RunData data, SessionState state)
	{
		// get the group - it's there
		Group group = (Group) state.getAttribute("group");

		// read the form
		String title = StringUtil.trimToNull(data.getParameters().getString("title"));
		group.setTitle(title);

		String description = StringUtil.trimToNull(data.getParameters().getString("description"));
		group.setDescription(description);

		if (title == null)
		{
			addAlert(state, rb.getString("sitgrp.plespe"));
			return false;
		}
		else
		{
			return true;
		}

	} // readGroupForm

	/**
	 * Switch to tools display mode within a site edit.
	 */
	public void doTools(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readPageForm(data, state)) return;

		state.setAttribute("mode", "tools");

	} // doTools

	/**
	 * create a new tool in the page edit.
	 */
	public void doNew_tool(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "newTool");

		// make the tool so we have the id
		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = page.addTool();
		state.setAttribute("tool", tool);

		// mark the site as new, so on cancel it can be deleted
		state.setAttribute("newTool", "true");

	} // doNew_tool

	/**
	 * Edit an existing tool.
	 */
	public void doEdit_tool(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "editTool");

		String id = data.getParameters().getString("id");

		// get the tool
		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = page.getTool(id);
		state.setAttribute("tool", tool);

	} // doEdit_tool

	/**
	 * Move the tool up in the order.
	 */
	public void doEdit_tool_up(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String id = data.getParameters().getString("id");

		// get the tool
		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = page.getTool(id);

		// move it
		tool.moveUp();

	} // doEdit_tool_up

	/**
	 * Move the tool down in the order.
	 */
	public void doEdit_tool_down(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		String id = data.getParameters().getString("id");

		// get the tool
		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = page.getTool(id);

		// move it
		tool.moveDown();

	} // doEdit_tool_down

	/**
	 * save the tool edited, and save the site edit.
	 */
	public void doSave_tool(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readToolForm(data, state)) return;

		// done with the tool
		state.removeAttribute("tool");

		// commit the entire site edit
		doSave_edit(data, context);

	} // doSave_tool

	/**
	 * save the tool edited, and return to the tools mode
	 */
	public void doDone_tool(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readToolForm(data, state)) return;

		// done with the tool
		state.removeAttribute("tool");

		// return to main mode
		state.setAttribute("mode", "tools");

	} // doDone_tool

	/**
	 * save the tool's selected feature, continue editing the tool
	 */
	public void doDone_feature(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readToolFeatureForm(data, state)) return;

		// go into edit mode
		state.setAttribute("mode", "editTool");

	} // doDone_feature

	/**
	 * cancel a tool edit, return to the tools list
	 */
	public void doCancel_tool(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		Site site = (Site) state.getAttribute("site");
		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");

		// if the tool was new, remove it
		if ("true".equals(state.getAttribute("newTool")))
		{
			page.removeTool(tool);
		}

		// %%% do we need the old tool around for a restore; did we already modify it? - ggolden

		// done with the tool
		state.removeAttribute("tool");

		// return to tools mode
		state.setAttribute("mode", "tools");

	} // doCancel_tool

	/**
	 * Handle a request to remove the tool being edited.
	 */
	public void doRemove_tool(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		SitePage page = (SitePage) state.getAttribute("page");
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");

		// remove the tool (no confirm)
		page.removeTool(tool);

		// done with the tool
		state.removeAttribute("tool");

		// return to tools mode
		state.setAttribute("mode", "tools");

	} // doRemove_tool

	/**
	 * Switch back to edit page info mode from another edit mode (like tools).
	 */
	public void doEdit_to_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute("mode", "editPage");

	} // doEdit_to_page

	/**
	 * Handle a request to regenerate the ids in the site under edit.
	 */
	public void doIds(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the site
		Site site = (Site) state.getAttribute("site");

		site.regenerateIds();
		addAlert(state, rb.getString("sitact.thesit"));
	}

	/**
	 * Handle a Search request.
	 */
	public void doSearch(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// clear the search(s)
		state.removeAttribute(STATE_SEARCH);
		state.removeAttribute(STATE_SEARCH_SITE_ID);
		state.removeAttribute(STATE_SEARCH_USER_ID);

		super.doSearch(runData, context);

	} // doSearch

	/**
	 * Handle a Search request - for site id
	 */
	public void doSearch_site_id(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// clear the search(s)
		state.removeAttribute(STATE_SEARCH);
		state.removeAttribute(STATE_SEARCH_SITE_ID);
		state.removeAttribute(STATE_SEARCH_USER_ID);

		// read the search form field into the state object
		String search = StringUtil.trimToNull(runData.getParameters().getString(FORM_SEARCH_SITEID));

		// set the flag to go to the prev page on the next list
		if (search != null)
		{
			state.setAttribute(STATE_SEARCH_SITE_ID, search);
		}

	} // doSearch_site_id

	/**
	 * Handle a Search request - for user my workspace
	 */
	public void doSearch_user_id(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// clear the search(s)
		state.removeAttribute(STATE_SEARCH);
		state.removeAttribute(STATE_SEARCH_SITE_ID);
		state.removeAttribute(STATE_SEARCH_USER_ID);

		// read the search form field into the state object
		String search = StringUtil.trimToNull(runData.getParameters().getString(FORM_SEARCH_USERID));

		// set the flag to go to the prev page on the next list
		if (search != null)
		{
			state.setAttribute(STATE_SEARCH_USER_ID, search);
		}

		// start paging again from the top of the list
		resetPaging(state);

	} // doSearch_user_id

	/**
	 * Handle a Search Clear request.
	 */
	public void doSearch_clear(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// clear the search(s)
		state.removeAttribute(STATE_SEARCH);
		state.removeAttribute(STATE_SEARCH_SITE_ID);
		state.removeAttribute(STATE_SEARCH_USER_ID);

		// start paging again from the top of the list
		resetPaging(state);

		// turn on auto refresh
		enableObserver(state);

	} // doSearch_clear

	/**
	 * Read the tool form and update the site in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readToolForm(RunData data, SessionState state)
	{
		// get the tool - it's there
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");

		// read the form
		String title = StringUtil.trimToNull(data.getParameters().getString("title"));
		tool.setTitle(title);

		// read the layout hints
		String hints = StringUtil.trimToNull(data.getParameters().getString("layoutHints"));
		if (hints != null)
		{
			// strip all whitespace
			hints = hints.replaceAll("\\s",""); // strip all whitespace
		}
		tool.setLayoutHints(hints);

		Tool t = tool.getTool();
		if (t != null)
		{
			// read in any params
			for (Enumeration iParams = t.getRegisteredConfig().propertyNames(); iParams.hasMoreElements();)
			{
				String paramName = (String) iParams.nextElement();
				String formName = "param_" + paramName;
				String value = data.getParameters().getString(formName);
				if (value != null)
				{
					value = StringUtil.trimToNull(value);

					// if we have a value
					if (value != null)
					{
						// if this value is not the same as the tool's registered, set it in the placement
						if (!value.equals(t.getRegisteredConfig().getProperty(paramName)))
						{
							tool.getPlacementConfig().setProperty(paramName, value);
						}

						// otherwise clear it
						else
						{
							tool.getPlacementConfig().remove(paramName);
						}
					}

					// if no value
					else
					{
						tool.getPlacementConfig().remove(paramName);
					}
				}
			}
		}
		else
		{
			addAlert(state, rb.getString("sitact.plesel"));
			return false;
		}

		return true;

	} // readToolForm

	/**
	 * Read the tool feature form and update the site in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readToolFeatureForm(RunData data, SessionState state)
	{
		// get the tool - it's there
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");

		// read the form
		String feature = data.getParameters().getString("feature");

		// get this feature
		Tool t = tool.getTool();

		// if the feature has changed, update the default configuration
		if ((t == null) || (!feature.equals(t.getId())))
		{
			tool.setTool(feature, ToolManager.getTool(feature));
			tool.setTitle(null);
			tool.getPlacementConfig().clear();
		}

		return true;

	} // readToolFeatureForm

	/**
	 * Clean up all possible state value when done an edit.
	 */
	private void cleanState(SessionState state)
	{
		state.removeAttribute("site");
		state.removeAttribute("page");
		state.removeAttribute("tool");
		state.removeAttribute("new");
		state.removeAttribute("newPage");
		state.removeAttribute("newTool");

		// clear the search, so after an edit or delete, the search is not automatically re-run
		state.removeAttribute(STATE_SEARCH);
		state.removeAttribute(STATE_SEARCH_SITE_ID);
		state.removeAttribute(STATE_SEARCH_USER_ID);

	} // cleanState

	/**
	 * Create a list of the valid layout names.
	 * 
	 * @return A List (String) of the value layout names.
	 */
	private List layoutsList()
	{
		List rv = new Vector();
		String[] layoutNames = SiteService.getLayoutNames();
		for (int i = 0; i < layoutNames.length; i++)
		{
			rv.add(layoutNames[i]);
		}
		return rv;

	} // layoutsList
}
