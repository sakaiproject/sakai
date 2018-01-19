/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.alias.api.Alias;
import org.sakaiproject.alias.api.AliasService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
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
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.courier.api.ObservingCourier;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.cover.EventTrackingService;
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
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * <p>
 * SitesAction is the Sakai admin sites editor.
 * </p>
 */
@Slf4j
public class AdminSitesAction extends PagedResourceActionII
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** State holding the site id for site id search. */
	protected static final String STATE_SEARCH_SITE_ID = "search_site";

	/** State holding the user id for user id search. */
	protected static final String STATE_SEARCH_USER_ID = "search_user";

	protected static final String FORM_SEARCH_SITEID = "search_site";

	protected static final String FORM_SEARCH_USERID = "search_user";
	
	private final static String FORM_URL_BASE = "form_url_base";
	
	private final static String FORM_URL_ALIAS = "form_url_alias";

	private final static String FORM_URL_ALIAS_FULL = "form_url_alias_full";

	private static ResourceLoader rb = new ResourceLoader("sites");
	
	/** Name of state attribute for Site instance id */
	private static final String STATE_SITE_INSTANCE_ID = "site.instance.id";

	private AliasService aliasService;
	private AuthzGroupService authzGroupService;

	public AdminSitesAction() {
		super();
		aliasService = ComponentManager.get(AliasService.class);
		authzGroupService = ComponentManager.get(AuthzGroupService.class);
	}

	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));
		String siteId = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH_SITE_ID));
		String userId = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH_USER_ID));

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
            Site userSite = findUserSiteByUserIdCriteria(userId);
            if ( userSite == null ) {
                return rv;
            }
            rv.add(userSite);
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
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));
		String siteId = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH_SITE_ID));
		String userId = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH_USER_ID));

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
		    Site userSite = findUserSiteByUserIdCriteria(userId);
            if ( userSite == null ) {
                return 0;
            }
            return 1;
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
     * Searches for a user's workspace site by resolving the given ID to
     * a {@link User} object and calculating the target {@link Site}'s
     * ID from that object. As implemented, supports user PK and EID
     * inputs. Be aware that searching for users by EID can result in
     * the lazy creation of Sakai user records, or at least user ID
     * mapping records.
     * 
     * @see #findUserByPk(String)
     * @see #findUserByEid(String)
     * @see #findUserSite(User)
     * @param formSubmittedUserId typically end-user provided search
     *   criteria; must not be <code>null</code>
     * @return the user's workspace site or <code>null</code> if no
     *   such user or no such site
     */
    protected Site findUserSiteByUserIdCriteria(String formSubmittedUserId) {
        
        User user = findUserByPk(formSubmittedUserId);
        if ( user == null ) {
            user = findUserByEid(formSubmittedUserId); // be warned, this might lazily create a user record
        }
        
        if ( user == null ) {
            return null;
        }
        
        Site userSite = findUserSite(user);
        return userSite;
        
    }
	
    /**
     * Search for a {@link User} object by primary key (i.e. <code>User.id</code>).
     * 
     * @see UserDirectoryService#getUser(String)
     * @param userPk a String to be treated as a user's Sakai-internal primary key;
     *   must not be <code>null</code>
     * @return a resolved {@link User} or <code>null</code>, signifying no results
     */
    protected User findUserByPk(String userPk) {
        
        try {
            User user = UserDirectoryService.getUser(userPk);
            return user;
        } catch ( UserNotDefinedException e ) {
			log.debug("Failed to find a user record by PK [pk = {}]", userPk, e);
            return null;
        }
        
    }

    /**
     * Search for a {@link User} object by user "enterprise identifier", 
     * (i.e. <code>User.eid</code>).
     * 
     * @see UserDirectoryService#getUserByEid(String)
     * @param eid a String to be treated as a user's "enterprise identifier";
     *   must not be <code>null</code>
     * @return a resolved {@link User} or <code>null</code>, signifying no results
     */
    protected User findUserByEid(String eid) {
        
        try {
            User user = UserDirectoryService.getUserByEid(eid);
            return user;
        } catch ( UserNotDefinedException e ) {
			log.debug("Failed to find a user record by EID [eid = {}]", eid, e);
            return null;
        }
        
    }    
    /**
     * Search for the given {@link User}'s workspace {@link Site}.
     * 
     * @param knownUser user having a Sakai primary key (doesn't necessarily
     *   mean the user has actually signed in yet). Must not be <code>null</code>
     * @return the user's workspace site or <code>null<code> if no such thing, e.g.
     *   if the user has not yet logged in.
     */
    protected Site findUserSite(User knownUser) {
        String userDbId =  knownUser.getId();
        String userEid = knownUser.getEid();
        String userMyWorkspaceSiteDbId = SiteService.getUserSiteId(userDbId);
        try {
            Site userSite = SiteService.getSite(userMyWorkspaceSiteDbId); // exceptional if no results
            return userSite;
        } catch ( IdUnusedException e ) {
			log.debug("Failed to locate a workspace for user [user id = {}][user eid = {}][site id = {}]", userDbId, userEid, userMyWorkspaceSiteDbId, e);
            return null;
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
		
		else if (mode.equals("pageProperties"))
		{
			template = buildPagePropertiesContext(state, context);
		}
		
		else if (mode.equals("toolProperties"))
		{
			template = buildToolPropertiesContext(state, context);
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
		 	log.warn("SitesAction: mode: {}", mode);
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
		context.put("realms", authzGroupService);

		// build the menu
		Menu bar = new MenuImpl();
		if (SiteService.allowAddSite(null))
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

		context.put("startEndTotalNumbers", new Integer[]{Integer.valueOf(startNumber),Integer.valueOf(endNumber),Integer.valueOf(totalNumber)});
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

		
		// get alias base path
		String aliasBaseUrl = ServerConfigurationService.getPortalUrl() + Entity.SEPARATOR + "site" + Entity.SEPARATOR;
		
		boolean displaySiteAlias = displaySiteAlias();
		String alias;
		
		context.put("displaySiteAlias", Boolean.valueOf(displaySiteAlias));
		if (displaySiteAlias)
		{
			alias = getSiteAlias(site!=null?site.getReference():"");
			if (alias != null) {
				String urlAliasFull = aliasBaseUrl + alias;
				context.put(FORM_URL_ALIAS_FULL, urlAliasFull);
			}
			context.put(FORM_URL_BASE, aliasBaseUrl);
			context.put(FORM_URL_ALIAS, alias);
		}
		
		// build the menu
		// we need the form fields for the remove...
		Menu bar = new MenuImpl();
		if (site!= null && SiteService.allowRemoveSite(site.getId()))
		{
			bar.add(new MenuEntry(rb.getString("sitact.remsit"), null, true, MenuItem.CHECKED_NA, "doRemove", null));
		}

		bar.add(new MenuEntry(rb.getString("sitact.savas"), null, true, MenuItem.CHECKED_NA, "doSaveas_request", null));

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
		context.put("titleCustom", String.valueOf( page.getTitleCustom() ) );

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
	 * Build the context for the properties edit in edit mode.
	 */
	private String buildPagePropertiesContext(SessionState state, Context context)
	{
		context.put("tlang", rb);

		SitePage page = (SitePage) state.getAttribute("page");
		if(page != null) {
			// read the form - if rejected, leave things as they are
			context.put("page", page);
		}
		
		return "_page_properties";
	}
	
	/**
	 * Build the context for the properties edit in edit mode.
	 */
	private String buildToolPropertiesContext(SessionState state, Context context)
	{
		context.put("tlang", rb);
		
				ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");
		if(tool != null) {
			// read the form - if rejected, leave things as they are
			context.put("tool", tool);
		}
		
		return "_tool_properties";
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

				// RealmEdit realm = authzGroupService.editRealm("/site/" + id); // %%% use a site service call -ggolden
				// state.setAttribute("realm", realm);

				state.setAttribute("mode", "edit");

				// disable auto-updates while in view mode
				ObservingCourier courier = (ObservingCourier) state.getAttribute(STATE_OBSERVER);
				if (courier != null) courier.disable();
			}
			catch (IdUnusedException e)
			{
				 log.warn("site not found: {}", id);

				addAlert(state, rb.getFormattedMessage("siteact.site", new Object[]{id}));
				state.removeAttribute("mode");

				// make sure auto-updates are enabled
				enableObserver(state);
			}
		}

		else
		{
			addAlert(state, rb.getFormattedMessage("youdonot1", new Object[]{id}));
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
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// get the current site published status
		boolean currentSitePublished = false;
		Site site = (Site) state.getAttribute("site");
		if (site != null)
		{
			currentSitePublished = site.isPublished();
		}
		// the input form setting for site publish status
		boolean afterSitePublished = data.getParameters().getBoolean("published");

		// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;

		doSave_edit(data, context);
		
		// throw events
		if (currentSitePublished && !afterSitePublished)
		{
			// site unpublished
			EventTrackingService.post(EventTrackingService.newEvent(
					SiteService.EVENT_SITE_UNPUBLISH,
					site.getReference(), true));
		}
		else if (!currentSitePublished && afterSitePublished)
		{
			// site published
			EventTrackingService.post(EventTrackingService.newEvent(
					SiteService.EVENT_SITE_PUBLISH,
					site.getReference(), true));
		}

	} // doSave

	/**
	 * Handle a request to save the edit from either page or tools list mode - no form to read in.
	 */
	public void doSave_props_edit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// read the properties form
		readPropertiesForm(data, state);

		doSave_edit(data, context);
	}
	
	/**
	 * Handle a request to save the edit from either page or tools list mode - no form to read in.
	 */
	public void doSave_page_props_edit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// read the properties form
		readPagePropertiesForm(data, state);

		doSave_edit(data, context);
	}
	
	/**
	 * Handle a request to save the edit from either page or tools list mode - no form to read in.
	 */
	public void doSave_tool_props_edit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		// read the properties form
		readToolPropertiesForm(data, state);

		doSave_edit(data, context);
	}
	
	/**
	 * Handle a request to save the edit from either page or tools list mode - no form to read in.
	 */
	public void doSave_edit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// commit the change
		Site site = (Site) state.getAttribute("site");
		if (site != null)
		{
			String url_alias = StringUtils.trimToNull(data.getParameters().getString("url_alias"));
			// set an alias for the site
			if(url_alias!=null)
			{
			state.setAttribute(STATE_SITE_INSTANCE_ID, site.getId());
			setSiteAlias(url_alias, site.getReference(), state);
			}

			// bring the mail archive service's channel for this site in sync with the site's setting
			// syncWithMailArchive(site);

			try
			{
				//Remove the new property if it exists on the site
				site.getPropertiesEdit().removeProperty("new");
				//Remove the new property on all pages and tools in this site
				List <SitePage> pages = site.getPages();
				for (SitePage page : pages) {
					//Clear new from page
					page.getPropertiesEdit().removeProperty("new");
					List <ToolConfiguration> tools = page.getTools();
					for (ToolConfiguration tool : tools) {
						//Clear new from tool
						tool.getPlacementConfig().remove("new");
					}
				}
				
				SiteService.save(site);
			}
			catch (PermissionException | IdUnusedException e)
			{
			 	log.warn(e.getMessage());
			}

			// save the realm, too
			// RealmEdit realm = (RealmEdit) state.getAttribute("realm");
			// authzGroupService.commitEdit(realm);
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
		//if (!readSiteForm(data, state)) return;

		// go to saveas mode
		state.setAttribute("mode", "saveas");

	} // doSaveas_request

	/**
	 * Handle a request to save-as the site as a new site.
	 */
	public void doSaveas(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

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
			addAlert(state, rb.getFormattedMessage("sitact.thesitid", new Object[]{id}));
			return;
		}
		catch (IdInvalidException e)
		{
			addAlert(state, rb.getFormattedMessage("sitact.thesitid2", new Object[]{id}));
			return;
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("sitact.youdonot2", new Object[]{id}));
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
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// cancel the realm edit - it will be removed if the site is removed
		// RealmEdit realm = (RealmEdit) state.getAttribute("realm");
		// if (realm != null)
		// {
		// authzGroupService.cancelEdit(realm);
		// }

		// get the site
		Site site = (Site) state.getAttribute("site");
		if (site != null)
		{
			String property = site.getProperties().getProperty("new");
			// if this was a new, delete the site
			if ("true".equals(property))
			{
				// remove the site
				try
				{
					SiteService.removeSite(site);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getFormattedMessage("sitact.youdonot3", new Object[]{site.getId()}));
				} catch (IdUnusedException e) {
					addAlert(state, rb.getFormattedMessage("sitact.thesitid2", new Object[]{site.getId()}));
				}
//				catch (IdUnusedException e)
//				{
//					addAlert(state, rb.getFormattedMessage("sitact.notfound", new Object[]{site.getId()}));
//				}
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
		//if (!readSiteForm(data, state)) return;

		// go to remove confirm mode
		state.setAttribute("mode", "confirm");

	} // doRemove

	/**
	 * doRemove_confirmed called when "eventSubmit_doRemove_confirmed" is in the request parameters to remove the site
	 */
	public void doRemove_confirmed(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// get the site
		Site site = (Site) state.getAttribute("site");

		// cancel the realm edit - the site remove will remove the realm
		// RealmEdit realm = (RealmEdit) state.getAttribute("realm");
		// authzGroupService.cancelEdit(realm);

		// remove the site
		try
		{
			SiteService.removeSite(site);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getFormattedMessage("sitact.youdonot3", new Object[]{site.getId()}));
		} catch (IdUnusedException e) {
			addAlert(state, rb.getFormattedMessage("sitact.thesitid2", new Object[]{site.getId()}));
		}
//		catch (IdUnusedException e)
//		{
//			addAlert(state, rb.getFormattedMessage("sitact.notfound", new Object[]{site.getId()}));
//		}

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
		String id = StringUtils.trimToNull(data.getParameters().getString("id"));
		String title = StringUtils.trimToNull(data.getParameters().getString("title"));
		String type = StringUtils.trimToNull(data.getParameters().getString("type"));
		String shortDescription = StringUtils.trimToNull(data.getParameters().getString("shortDescription"));
		String description = StringUtils.trimToNull(data.getParameters().getString("description"));
		boolean joinable = data.getParameters().getBoolean("joinable");
		String joinerRole = StringUtils.trimToNull(data.getParameters().getString("joinerRole"));
		String icon = StringUtils.trimToNull(data.getParameters().getString("icon"));
		String info = StringUtils.trimToNull(data.getParameters().getString("info"));
		boolean published = data.getParameters().getBoolean("published");
		boolean softlyDeleted = data.getParameters().getBoolean("softlyDeleted");
		String skin = StringUtils.trimToNull(data.getParameters().getString("skin"));
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
				// mark the site as new, so on cancel it can be deleted
				site.getPropertiesEdit().addProperty("new", "true");

				// put the site in the state
				state.setAttribute("site", site);
			}
			catch (IdUsedException e)
			{
				addAlert(state, rb.getFormattedMessage("sitact.thesitid", new Object[]{id}));
				return false;
			}
			catch (IdInvalidException e)
			{
				addAlert(state, rb.getFormattedMessage("sitact.thesitid2", new Object[]{id}));
				return false;
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getFormattedMessage("sitact.youdonot2", new Object[]{id}));
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
					realm = authzGroupService.getAuthzGroup(site.getReference());
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
						AuthzGroup r = authzGroupService.getAuthzGroup(realmTemplate);
						roles.addAll(r.getRoles());
					}
					catch (GroupNotDefinedException err)
					{
						try
						{
							AuthzGroup rr = authzGroupService.getAuthzGroup("!site.template");
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
			site.setSoftlyDeleted(softlyDeleted);
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

		Site site = (Site) state.getAttribute("site");
			// read the form - if rejected, leave things as they are
		if (!readSiteForm(data, state)) return;
		state.setAttribute("mode", "properties");
	
	}
	
	/**
	 * Switch to property edit mode within a tool edit.
	 */
	public void doToolProperties(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");
		// read the form - if rejected, leave things as they are
		if (!readToolForm(data, state)) return;
		state.setAttribute("mode", "toolProperties");

	
	}
	
	/**
	 * Switch to property edit mode within a page edit.
	 */
	public void doPageProperties(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}
		
		SitePage page = (SitePage) state.getAttribute("page");
		// read the form - if rejected, leave things as they are
		if (!readPageForm(data, state)) return;
		state.setAttribute("mode", "pageProperties");

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
		page.getPropertiesEdit().addProperty("new","true");
		state.setAttribute("page", page);

	} // doNew_page

	/**
	 * Handle a request to create a new property in the site edit.
	 */
	public void doNew_property(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

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
		
		SitePage page = (SitePage) state.getAttribute("page");
		//Clean up new page property
		page.getPropertiesEdit().removeProperty("new");

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

		String property = page.getProperties().getProperty("new");
		// if the page was new, remove it
		if ("true".equals(property))
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
	 * cancel a page edit, return to the pages list
	 */
	public void doCancel_page_props(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute("mode", "editPage");

	} // doCancel_page_prop

	/**
	 * cancel a page edit, return to the pages list
	 */
	public void doCancel_tool_props(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute("mode", "editTool");

	} // doCancel_tool_prop

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
			try {
				site.deleteGroup(group);
			} catch (IllegalStateException e) {
				log.error(".doCancel_group: Group with id {} cannot be removed because is locked", group.getId());
			}
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
		try {
			site.deleteGroup(group);
		} catch (IllegalStateException e) {
			log.error(".doRemove_group: Group with id {} cannot be removed because is locked", group.getId());
		}

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

		if(state.getAttribute("mode").equals("properties")) {
			state.setAttribute("mode", "edit");
		}
	}
	
	/**
	 * Switch back to edit main info mode properties edit mode
	 */
	public void doEdit_props_to_page(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the properties form
		readPropertiesForm(data, state);

		if(state.getAttribute("mode").equals("pageProperties")) {
			state.setAttribute("mode", "editPage");
		}
	}
	
	/**
	 * Switch back to edit main info mode properties edit mode
	 */
	public void doEdit_props_to_tool(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the properties form
		readPropertiesForm(data, state);

		if(state.getAttribute("mode").equals("toolProperties")) {
			state.setAttribute("mode", "editTool");
		}
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
		String title = StringUtils.trimToNull(data.getParameters().getString("title"));
		page.setTitle(title);

		try
		{
			// this comes in 1 based, convert to 0 based
			int layout = Integer.parseInt(data.getParameters().getString("layout")) - 1;
			page.setLayout(layout);
		}
		catch (Exception e)
		{
		 	log.warn("reading layout: {}" + e.getMessage());
		}

		boolean popup = data.getParameters().getBoolean("popup");
		page.setPopup(popup);

		boolean custom = data.getParameters().getBoolean("custom");
		page.setTitleCustom(custom);

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
		if(state.getAttribute("tool")!= null) {
			
			return readToolPropertiesForm(data, state);
		}
		
		if(state.getAttribute("page")!= null) {
			
			return readPagePropertiesForm(data, state);
		}
		
		if(state.getAttribute("site")!= null) {
		
			return readSitePropertiesForm(data, state);
		}
		return true;
	}

	/**
	 * Read the properties form and update the site in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readSitePropertiesForm(RunData data, SessionState state) {
		// get the site
		Site site = (Site) state.getAttribute("site");

		ResourcePropertiesEdit props = site.getPropertiesEdit();
		
		// check each property for possible update
		for (Iterator i = props.getPropertyNames(); i.hasNext();)
		{
			String name = (String) i.next();
			String formValue = StringUtils.trimToNull(data.getParameters().getString("param_" + name));
			
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
		String formName = StringUtils.trimToNull(data.getParameters().getString("new_name"));
		if (formName != null)
		{
			String formValue = StringUtils.trimToNull(data.getParameters().getString("new_value"));
			if (formValue != null)
			{
				props.addProperty(formName, formValue);
			}
		}

		return true;
	}

	/**
	 * Read the properties form and update the page in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readPagePropertiesForm(RunData data, SessionState state) {
		// get the site
		SitePage page = (SitePage) state.getAttribute("page");

		ResourcePropertiesEdit props = page.getPropertiesEdit();
		
		// check each property for possible update
		for (Iterator i = props.getPropertyNames(); i.hasNext();)
		{
			String name = (String) i.next();
			String formValue = StringUtils.trimToNull(data.getParameters().getString("param_" + name));
			
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
		String formName = StringUtils.trimToNull(data.getParameters().getString("new_name"));
		if (formName != null)
		{
			String formValue = StringUtils.trimToNull(data.getParameters().getString("new_value"));
			if (formValue != null)
			{
				props.addProperty(formName, formValue);
			}
		}

		return true;
	}
	
	/**
	 * Read the properties form and update the page in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readToolPropertiesForm(RunData data, SessionState state) {
		// get the site
		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");

		Tool t = tool.getTool();
		//update properties
		if (t != null)
		{
			// read in any params
			for (Enumeration iParams = tool.getPlacementConfig().propertyNames(); iParams.hasMoreElements();)
			{
				String paramName = (String) iParams.nextElement();
				String formValue = StringUtils.trimToNull(data.getParameters().getString("param_" + paramName));
				
				// update the properties or remove
				if (formValue != null)
				{
					tool.getPlacementConfig().setProperty(paramName, formValue);
				}
				else
				{
					tool.getPlacementConfig().remove(paramName);
				}
			}
		}
		// see if there's a new one
		String formName = StringUtils.trimToNull(data.getParameters().getString("new_name"));
		if (formName != null)
		{
			String formValue = StringUtils.trimToNull(data.getParameters().getString("new_value"));
			if (formValue != null)
			{
				tool.getPlacementConfig().setProperty(formName, formValue);
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
		String title = StringUtils.trimToNull(data.getParameters().getString("title"));
		group.setTitle(title);

		String description = StringUtils.trimToNull(data.getParameters().getString("description"));
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
		tool.getPlacementConfig().setProperty("new", "true");
		state.setAttribute("tool", tool);

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
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

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
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// read the form - if rejected, leave things as they are
		if (!readToolForm(data, state)) return;

		ToolConfiguration tool = (ToolConfiguration) state.getAttribute("tool");
		//Clean up new tool property
		tool.getPlacementConfig().remove("new");

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

		String property = tool.getPlacementConfig().getProperty("new");
		
		// if the tool was new, remove it
		if ("true".equals(property))
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
		String search = StringUtils.trimToNull(runData.getParameters().getString(FORM_SEARCH_SITEID));

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
		String search = StringUtils.trimToNull(runData.getParameters().getString(FORM_SEARCH_USERID));

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
		String title = StringUtils.trimToNull(data.getParameters().getString("title"));
		tool.setTitle(title);

		// read the layout hints
		String hints = StringUtils.trimToNull(data.getParameters().getString("layoutHints"));
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
					value = StringUtils.trimToNull(value);

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
			tool.setTitle(ToolManager.getTool(feature).getTitle());
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

		// clear the search, so after an edit or delete, the search is not automatically re-run
		state.removeAttribute(STATE_SEARCH);
		state.removeAttribute(STATE_SEARCH_SITE_ID);
		state.removeAttribute(STATE_SEARCH_USER_ID);

	} // cleanState
	
	public boolean displaySiteAlias() {
		if (ServerConfigurationService.getBoolean("wsetup.disable.siteAlias", false)) {
			return false;
		}
		return true;
	}

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
	
	/**
	 * get one alias for site, if it exists
	 * @param channelReference
	 * @return
	 */
	private String getSiteAlias(String reference)
	{
		String alias = null;
		if (reference != null)
		{
			// get the email alias when an Email Archive tool has been selected
			List aliases = aliasService.getAliases(reference, 1, 1);
			if (aliases.size() > 0) {
				alias = ((Alias) aliases.get(0)).getId();
			}
		}
		return alias;
	}
	
	private void setSiteAlias(String alias, String siteReference, SessionState state)
	{
		
		/*
		 * The point of these site aliases is to have easy-to-recall,
		 * easy-to-guess URLs. So we take a very conservative approach
		 * here and disallow any aliases which would require special 
		 * encoding or would simply be ignored when building a valid 
		 * resource reference or outputting that reference as a URL.
		 */
		boolean isSimpleResourceName = alias.equals(Validator.escapeResourceName(alias));
		boolean isSimpleUrl = alias.equals(Validator.escapeUrl(alias));
		if ( !(isSimpleResourceName) || !(isSimpleUrl) ) {
			addAlert(state, rb.getFormattedMessage("sitedipag.alias.isinval", new Object[]{alias}));
			log.warn("{}.updateSiteInfo: {}", this, rb.getFormattedMessage("sitedipag.alias.isinval", new Object[]{alias}));
		} 
		else if (StringUtils.trimToNull(alias) != null && StringUtils.trimToNull(siteReference) != null) 
		{
			String currentAlias = StringUtils.trimToNull(getSiteAlias(siteReference));

			if (currentAlias == null || !currentAlias.equals(alias))
			{
				try {
					aliasService.setAlias(alias, siteReference);
				} catch (IdUsedException ee) {
					addAlert(state, rb.getFormattedMessage("sitedipag.alias.exists", new Object[]{alias}));
					log.warn("{}.setSiteAlias: {}", this, rb.getFormattedMessage("sitedipag.alias.exists", new Object[]{alias}));
				} catch (IdInvalidException ee) {
					addAlert(state, rb.getFormattedMessage("sitedipag.alias.isinval", new Object[]{alias}));
					log.warn("{}.setSiteAlias: {}", this, rb.getFormattedMessage("sitedipag.alias.isinval", new Object[]{alias}));
				} catch (PermissionException ee) {
					addAlert(state, rb.getFormattedMessage("sitedipag.alias.nopermission", new Object[]{SessionManager.getCurrentSessionUserId()}));
					log.warn("{}.setSiteAlias: {}", this, rb.getFormattedMessage("sitedipag.alias.nopermission", new Object[]{SessionManager.getCurrentSessionUserId()}));
				}
			}
		}
	}
}
