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

package org.sakaiproject.authz.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * RealmsAction is the Sakai Admin realms editor.
 * </p>
 */
@Slf4j
public class RealmsAction extends PagedResourceActionII
{

	private static final long serialVersionUID = 1L;

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("authz-tool");

	private AuthzGroupService authzGroupService;
	private FunctionManager functionManager;
	private GroupProvider groupProvider;
	private SecurityService securityService;
	private SessionManager sessionManager;
	private ToolManager toolManager;
	private UserDirectoryService userDirectoryService;
	private UserAuditRegistration userAuditRegistration;
	private UserAuditService userAuditService;

	public RealmsAction()
	{
		authzGroupService = ComponentManager.get(AuthzGroupService.class);
		functionManager = ComponentManager.get(FunctionManager.class);
		groupProvider = ComponentManager.get(GroupProvider.class);
		securityService = ComponentManager.get(SecurityService.class);
		sessionManager = ComponentManager.get(SessionManager.class);
		toolManager = ComponentManager.get(ToolManager.class);
		userDirectoryService = ComponentManager.get(UserDirectoryService.class);
		userAuditRegistration = ComponentManager.get(UserAuditRegistration.class);
		userAuditService = ComponentManager.get(UserAuditService.class);
	}

	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		return authzGroupService.getAuthzGroups(search, new PagingPosition(first, last));
	}

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		// search?
		String search = StringUtils.trimToNull((String) state.getAttribute(STATE_SEARCH));

		return authzGroupService.countAuthzGroups(search);
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
		// String pattern = authzGroupService.realmReference("");
		//
		// state.setAttribute(STATE_OBSERVER, new EventObservingCourier(deliveryId, elementId, pattern));
		// }

	} // initState

	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);

		// if not allowed, we won't do anything
		if (!isAccessAllowed())
		{
			return (String) getContext(rundata).get("template") + "_noaccess";
		}

		String template = null;

		// check mode and dispatch
		String mode = (String) state.getAttribute("mode");
		if (mode == null)
		{
			template = buildListContext(state, context);
		}
		else if ("new".equals(mode))
		{
			template = buildNewContext(state, context);
		}
		else if ("edit".equals(mode))
		{
			template = buildEditContext(state, context);
		}
		else if ("confirm".equals(mode))
		{
			template = buildConfirmRemoveContext(state, context);
		}
		else if ("saveas".equals(mode))
		{
			template = buildSaveasContext(state, context);
		}

		else if ("newRole".equals(mode))
		{
			template = buildNewRoleContext(state, context);
		}
		else if ("editRole".equals(mode))
		{
			template = buildEditRoleContext(state, context);
		}
		else if ("saveasRole".equals(mode))
		{
			template = buildSaveasRoleContext(state, context);
		}

		else if ("newUser".equals(mode))
		{
			template = buildNewUserContext(state, context);
		}
		else if ("editUser".equals(mode))
		{
			template = buildEditUserContext(state, context);
		}
		else if ("view".equals(mode))
		{
			template = buildViewContext(state, context);
		}
		else if ("viewRole".equals(mode))
		{
			template = buildViewRoleContext(state, context);
		}

		else
		{
		 	log.warn("mode: {}", mode);
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
		// prepare the paging of realms
		List realms = prepPage(state);

		// put the service in the context (used for allow update calls on each realm)
		context.put("service", authzGroupService);

		// put all realms into the context
		context.put("realms", realms);
		
		pagingInfoToContext(state, context);

		// build the menu
		Menu bar = new MenuImpl();
		if (authzGroupService.allowAdd(""))
		{
			bar.add(new MenuEntry(rb.getString("realm.new"), "doNew"));
		}

		// add the paging commands
		//addListPagingMenus(bar, state);

		// add the search commands
		addSearchMenus(bar, state);

		// add the refresh commands
		addRefreshMenus(bar, state);

		if (bar.size() > 0)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}
		
		context.put("viewAllowed", isAccessAllowed());


		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		return "_list";

	} // buildListContext

	/**
	 * Build the context for the new realm mode.
	 */
	private String buildNewContext(SessionState state, Context context)
	{
		// name the html form for user edit fields
		context.put("form-name", "realm-form");

		return "_edit";

	} // buildNewContext

	/**
	 * Build the context for the edit realm mode.
	 */
	private String buildEditContext(SessionState state, Context context)
	{
		// get the realm to edit
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		// get the roles defined in the realm
		List roles = new Vector();
		roles.addAll(realm.getRoles());
		Collections.sort(roles);
		context.put("roles", roles);

		// get a list of the users who have individual grants in the realm
		List grants = new Vector();
		grants.addAll(realm.getMembers());
		Collections.sort(grants);
		context.put("grants", grants);

		// name the html form for user edit fields
		context.put("form-name", "realm-form");

		// build the menu
		// we need the form fields for the remove...
		Menu bar = new MenuImpl();
		if (realm != null && authzGroupService.allowRemove(realm.getId()))
		{
			bar.add(new MenuEntry(rb.getString("realm.remove"), null, true, MenuItem.CHECKED_NA, "doRemove", "realm-form"));
		}

		bar.add(new MenuEntry(rb.getString("realm.add"), null, true, MenuItem.CHECKED_NA, "doNew_role", "realm-form"));
		bar.add(new MenuEntry(rb.getString("realm.grant"), null, true, MenuItem.CHECKED_NA, "doNew_user", "realm-form"));

		bar.add(new MenuEntry(rb.getString("realm.save"), null, true, MenuItem.CHECKED_NA, "doSaveas_request", "realm-form"));

		context.put(Menu.CONTEXT_MENU, bar);

		return "_edit";

	} // buildEditContext
	
	/**
	 * Build the context for the view realm mode.
	 */
	private String buildViewContext(SessionState state, Context context)
	{
		// get the realm to edit
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		// get the roles defined in the realm
		List roles = new Vector();
		roles.addAll(realm.getRoles());
		Collections.sort(roles);
		context.put("roles", roles);

		// get a list of the users who have individual grants in the realm
		List grants = new Vector();
		grants.addAll(realm.getMembers());
		Collections.sort(grants);
		context.put("grants", grants);

		return "_view";

	} // buildEditContext

	/**
	 * Build the context for the new realm mode.
	 */
	private String buildConfirmRemoveContext(SessionState state, Context context)
	{
		// get the realm to edit
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		return "_confirm_remove";

	} // buildConfirmRemoveContext

	/**
	 * Build the context for the role save as
	 */
	private String buildSaveasRoleContext(SessionState state, Context context)
	{
		// get the realm to edit
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		// get the role
		Role role = (Role) state.getAttribute("role");
		context.put("role", role);

		return "_saveas_role";

	} // buildSaveasRoleContext

	/**
	 * Build the context for the new role mode.
	 */
	private String buildNewRoleContext(SessionState state, Context context)
	{
		// name the html form for user edit fields
		context.put("form-name", "role-form");

		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		// get all functions
		List allFunctions = functionManager.getRegisteredFunctions();
		Collections.sort(allFunctions);
		context.put("allLocks", allFunctions);

		// get all roles
		List allRoles = new Vector();
		if (realm != null)
			allRoles.addAll(realm.getRoles());
		Collections.sort(allRoles);
		context.put("allRoles", allRoles);

		return "_edit_role";

	} // buildNewRoleContext

	/**
	 * Build the context for the edit role mode.
	 */
	private String buildEditRoleContext(SessionState state, Context context)
	{
		// name the html form for user edit fields
		context.put("form-name", "role-form");

		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		// get the role
		Role role = (Role) state.getAttribute("role");
		context.put("role", role);

		// get all functions
		List allFunctions = functionManager.getRegisteredFunctions();
		Collections.sort(allFunctions);
		context.put("allLocks", allFunctions);

		// get all roles
		List allRoles = new Vector();
		if (realm != null)
			allRoles.addAll(realm.getRoles());
		Collections.sort(allRoles);
		context.put("allRoles", allRoles);

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("realm.removerol"), null, true, MenuItem.CHECKED_NA, "doRemove_role"));
		bar.add(new MenuEntry(rb.getString("realm.copyrol"), null, true, MenuItem.CHECKED_NA, "doSaveas_role"));
		context.put(Menu.CONTEXT_MENU, bar);

		return "_edit_role";

	} // buildEditRoleContext
	
	/**
	 * Build the context for the view role mode.
	 */
	private String buildViewRoleContext(SessionState state, Context context)
	{

		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		// get the role
		Role role = (Role) state.getAttribute("role");
		context.put("role", role);

		// get all functions
		List allFunctions = functionManager.getRegisteredFunctions();
		Collections.sort(allFunctions);
		context.put("allLocks", allFunctions);

		// get all roles
		List allRoles = new Vector();
		if (realm != null)
			allRoles.addAll(realm.getRoles());
		Collections.sort(allRoles);
		context.put("allRoles", allRoles);

		return "_view_role";

	} // buildViewRoleContext


	/**
	 * Build the context for the new user grant mode.
	 */
	private String buildNewUserContext(SessionState state, Context context)
	{
		// name the html form for user edit fields
		context.put("form-name", "user-form");

		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");

		context.put("realm", realm);

		// get all roles
		List allRoles = new Vector();
		if (realm != null)
			allRoles.addAll(realm.getRoles());
		Collections.sort(allRoles);
		context.put("allRoles", allRoles);

		return "_edit_user";

	} // buildNewUserContext

	/**
	 * Build the context for the edit user grant mode.
	 */
	private String buildEditUserContext(SessionState state, Context context)
	{
		// name the html form for user edit fields
		context.put("form-name", "user-form");

		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		User user = (User) state.getAttribute("user");

		context.put("realm", realm);
		context.put("user", user);

		// get this user's role - only if not provided
		if (realm != null)
		{
			Member grant = realm.getMember(user.getId());
			context.put("grant", grant);
			if ((grant != null) && (!grant.isProvided()) && (grant.getRole() != null))
			{
				context.put("roles", grant.getRole());
			}
		}

		// get all roles
		List allRoles = new Vector();
		if (realm != null)
			allRoles.addAll(realm.getRoles());
		Collections.sort(allRoles);
		context.put("allRoles", allRoles);

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("realm.removeall"), null, true, MenuItem.CHECKED_NA, "doRemove_user"));
		context.put(Menu.CONTEXT_MENU, bar);

		return "_edit_user";

	} // buildEditUserContext

	/**
	 * Build the context for the save-as mode.
	 */
	private String buildSaveasContext(SessionState state, Context context)
	{
		// get the realm being edited
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		context.put("realm", realm);

		return "_saveas";

	} // buildSaveasContext

	/**
	 * Handle a request to save-as the realm as a new realm.
	 */
	public void doSaveas(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// read the form
		String id = data.getParameters().getString("id");

		// get the realm to copy from
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");

		try
		{
			// make a new site with this id and as a structural copy of site
			AuthzGroup newRealm = authzGroupService.addAuthzGroup(id, realm, userDirectoryService.getCurrentUser().getId());
		}
		catch (GroupAlreadyDefinedException e)
		{
			addAlert(state, rb.getString("realm.iduse"));
			return;
		}
		catch (GroupIdInvalidException e)
		{
			addAlert(state, rb.getString("realm.idinvalid"));
			return;
		}
		catch (AuthzPermissionException e)
		{
			addAlert(state, rb.getString("realm.notpermis"));
			return;
		}

		doCancel(data, context);

		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		schedulePeerFrameRefresh("sitenav");

	} // doSaveas

	/**
	 * cancel the saveas request, return to edit
	 */
	public void doCancel_saveas(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// return to main mode
		state.setAttribute("mode", "edit");

	} // doCancel_saveas

	/**
	 * Go into saveas mode
	 */
	public void doSaveas_request(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		// go to saveas mode
		state.setAttribute("mode", "saveas");

	} // doSaveas_request

	/**
	 * Handle a request for a new realm.
	 */
	public void doNew(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "new");

		// mark the realm as new, so on cancel it can be deleted
		state.setAttribute("new", "true");

		// disable auto-updates while in view mode
		disableObservers(state);

	} // doNew

	/**
	 * Handle a request to edit a realm.
	 */
	public void doEdit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		String id = data.getParameters().getString("id");

		// get the realm
		try
		{
			AuthzGroup realm = authzGroupService.getAuthzGroup(id);
			state.setAttribute("realm", realm);

			state.setAttribute("mode", "edit");

			// disable auto-updates while in view mode
			disableObservers(state);
		}
		catch (GroupNotDefinedException e)
		{
		 	log.warn("realm not found: {}", id);

			addAlert(state, rb.getFormattedMessage("realm.notfound", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		// catch (AuthzPermissionException e)
		// {
		// addAlert(state, rb.getString("realm.notpermis1") + " " + id);
		// state.removeAttribute("mode");
		//
		// // make sure auto-updates are enabled
		// enableObserver(state);
		// }

	} // doEdit

	/**
	 * Handle a request to save the realm edit (from the realm edit form).
	 */
	public void doSave(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		doSave_edit(data, context);

	} // doSave

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
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		if (realm != null)
		{
			try
			{
				authzGroupService.save(realm);
				// Grab the list from session state and save it, if appropriate
				List<String[]> userAuditList = (List<String[]>) state.getAttribute("userAuditList");
				if (userAuditList!=null && !userAuditList.isEmpty())
				{
					userAuditRegistration.addToUserAuditing(userAuditList);
					state.removeAttribute("userAuditList");
				}
			}
			catch (GroupNotDefinedException e)
			{
				// TODO: GroupNotDefinedException
			}
			catch (AuthzPermissionException e)
			{
				// TODO: AuthzPermissionException
			}
			catch (Exception e)
			{
			 	log.warn("realmId = {} {}", realm.getId(), e.getMessage());
			}
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
	 * doCancel called when "eventSubmit_doCancel" is in the request parameters to cancel realm edits
	 */
	public void doCancel(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		if (realm != null)
		{
			// if this was a new, delete the realm
			if ("true".equals(state.getAttribute("new")))
			{
				// remove the realm
				try
				{
					authzGroupService.removeAuthzGroup(realm);
				}
				catch (AuthzPermissionException e)
				{
					addAlert(state, rb.getFormattedMessage("realm.notpermis2", new Object[]{realm.getId()}));
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
	 * doRemove called when "eventSubmit_doRemove" is in the request parameters to confirm removal of the realm
	 */
	public void doRemove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		// go to remove confirm mode
		state.setAttribute("mode", "confirm");

		// disable auto-updates while in view mode
		disableObservers(state);

	} // doRemove

	/**
	 * doRemove_confirmed called when "eventSubmit_doRemove_confirmed" is in the request parameters to remove the realm
	 */
	public void doRemove_confirmed(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		if (realm != null)
		{
			// remove the realm
			try
			{
				authzGroupService.removeAuthzGroup(realm);
			}
			catch (AuthzPermissionException e)
			{
				addAlert(state, rb.getFormattedMessage("realm.notpermis2", new Object[]{realm.getId()}));
			}
	
			// cleanup
			cleanState(state);
	
			// go to main mode
			state.removeAttribute("mode");
	
			// make sure auto-updates are enabled
			enableObserver(state);
		}

	} // doRemove_confirmed

	/**
	 * doCancel_remove called when "eventSubmit_doCancel_remove" is in the request parameters to cancel realm removal
	 */
	public void doCancel_remove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		if (!"POST".equals(data.getRequest().getMethod())) {
			return;
		}

		// return to edit mode
		state.setAttribute("mode", "edit");

	} // doCancel_remove
	
	/**
	 * Handle a request to view a realm.
	 */
	public void doView(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		
		String id = data.getParameters().getString("id");

		// get the realm
		try
		{
			AuthzGroup realm = authzGroupService.getAuthzGroup(id);
			state.setAttribute("realm", realm);

			state.setAttribute("mode", "view");

			// disable auto-updates while in view mode
			disableObservers(state);
		}
		catch (GroupNotDefinedException e)
		{
		 	log.warn("realm not found: {}", id);

			addAlert(state, rb.getFormattedMessage("realm.notfound", new Object[]{id}));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

	} // doView
	
	/**
	 * View a role.
	 */
	public void doView_role(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		state.setAttribute("mode", "viewRole");

		String id = data.getParameters().getString("target");

		// get the role
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		if (realm != null)
		{
			Role role = realm.getRole(id);
			state.setAttribute("role", role);
		}

	} // doView_role

	/**
	 * Read the realm form and update the realm in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readRealmForm(RunData data, SessionState state)
	{
		// read the form
		String id = data.getParameters().getString("id");
		String provider = data.getParameters().getString("provider");
		String maintain = data.getParameters().getString("maintain");
		
		// verify provider information
		if (StringUtils.trimToNull(provider) != null)
		{
			String[] providers = groupProvider.unpackId(provider);
			for (int i = 0; i<providers.length; i++)
			{
				// no Exception is defined to be thrown from GroupProvider's getuserRolesForGroup(String) call
				// we will check for the null or empty returned value as an indicator for invalid provider id.
				//Map<String, String> userRoles = groupProvider.getUserRolesForGroup(providers[i]);
			    if (!groupProvider.groupExists(providers[i]))
				{
					// if provider id isn't found or is null then an empty collection should be returned.
					// is it proper to issue the following alert?
					addAlert(state, rb.getFormattedMessage("realm.noProviderIdFound", new Object[]{providers[i]}));
				}
			}
			
			if (state.getAttribute(STATE_MESSAGE) != null)
			{
				return false;
			}
		}

		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");

		// add if needed
		if (realm == null)
		{
			try
			{
				realm = authzGroupService.addAuthzGroup(id);

				// put the realm in the state
				state.setAttribute("realm", realm);
			}
			catch (GroupAlreadyDefinedException e)
			{
				addAlert(state, rb.getString("realm.iduse"));
				return false;
			}
			catch (GroupIdInvalidException e)
			{
				addAlert(state, rb.getString("realm.idinvalid"));
				return false;
			}
			catch (AuthzPermissionException e)
			{
				addAlert(state, rb.getString("realm.notpermis3"));
				return false;
			}
		}

		// update
		if (realm != null)
		{
			realm.setProviderGroupId(provider);
			realm.setMaintainRole(maintain);
		}

		return true;

	} // readRealmForm

	/**
	 * Handle a request to create a new role in the realm edit.
	 */
	public void doNew_role(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "newRole");

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		// mark the realm as new, so on cancel it can be deleted
		state.setAttribute("newRole", "true");

	} // doNew_role

	/**
	 * Edit an existing page.
	 */
	public void doEdit_role(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		state.setAttribute("mode", "editRole");

		String id = data.getParameters().getString("target");

		// get the role
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		if (realm != null)
		{
			Role role = realm.getRole(id);
			state.setAttribute("role", role);
		}

	} // doEdit_role

	/**
	 * Handle a request to remove the role being edited.
	 */
	public void doRemove_role(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		Role role = (Role) state.getAttribute("role");
		if (realm != null && role != null)
		{
			// remove the role (no confirm)
			realm.removeRole(role.getId());
	
			// done with the role
			state.removeAttribute("role");
	
			// return to edit mode
			state.setAttribute("mode", "edit");
		}

	} // doRemove_role

	/**
	 * Handle a request to remove the role being edited.
	 */
	public void doSaveas_role(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRoleForm(data, state)) return;

		// go to saveas_role mode
		state.setAttribute("mode", "saveasRole");

	} // doSaveas_role

	/**
	 * Handle a request to saveas with this name.
	 */
	public void doSave_role_as(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form
		if (!readRoleSaveAsForm(data, state)) return;

		// cleanup
		state.removeAttribute("role");

		// go back to edit mode
		state.setAttribute("mode", "edit");

	} // doSave_role_as

	/**
	 * Handle a request to cancel role saveas.
	 */
	public void doCancel_role_saveas(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// cleanup
		state.removeAttribute("role");

		// go back to edit mode
		state.setAttribute("mode", "edit");

	} // doCancel_role_saveas

	/**
	 * Handle a request to save the realm edit (from the role edit form).
	 */
	public void doSave_role(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRoleForm(data, state)) return;

		doSave_edit(data, context);

	} // doSave_role

	/**
	 * Handle a request to be done role editing - return to the edit mode.
	 */
	public void doDone_role(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRoleForm(data, state)) return;

		// cleanup
		state.removeAttribute("role");

		// go back to edit mode
		state.setAttribute("mode", "edit");

	} // doDone_role

	/**
	 * Read the user form and update the realm in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readRoleForm(RunData data, SessionState state)
	{
		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");

		// get the locks
		String[] locks = data.getParameters().getStrings("locks");

		// we are setting for either a new role or this role
		Role role = (Role) state.getAttribute("role");

		//Read the locks if they're null and the role is not null to avoid passing them around
		if (locks == null && role != null) {
			Collection realms = new ArrayList<String>(Arrays.asList(realm.getId()));
			List <String> newlocks = new ArrayList(authzGroupService.getAllowedFunctions(role.getId(), realms));
			locks = newlocks.toArray(new String[0]);
		}

		if (realm != null && role == null)
		{
			// read the form
			String id = StringUtils.trimToNull(data.getParameters().getString("id"));

			// if the field is missing, and there are no locks, just be done with no change
			if ((id == null) && (locks == null)) return true;

			if (id == null)
			{
				addAlert(state, rb.getString("realm.please"));
				return false;
				// TODO: would be nice to read the locks, and restore them when the form returns -ggolden
			}

			// create the role
			try
			{
				role = realm.addRole(id);
			}
			catch (RoleAlreadyDefinedException e)
			{
				addAlert(state, rb.getFormattedMessage("realm.defined", new Object[]{id}) );
				return false;
				// TODO: would be nice to read the locks, and restore them when the form returns -ggolden
			}
		}

		// clear out the role
		role.disallowAll();

		String descriptionString = StringUtils.trimToNull(data.getParameters().getString("description"));
		String providerOnlyString = StringUtils.trimToNull(data.getParameters().getString("providerOnly"));
		
		//Role can't be null at this point
		if (descriptionString == null) {
			descriptionString = role.getDescription();
		}
		
		if (providerOnlyString == null) {
			providerOnlyString = String.valueOf(role.isProviderOnly());
		}

		// description
		role.setDescription(StringUtils.trimToNull(descriptionString));

		// providerOnly
		role.setProviderOnly("true".equals(providerOnlyString));		

		// for each lock set, give it to the role
		if (locks != null)
		{
			for (int i = 0; i < locks.length; i++)
			{
				role.allowFunction(locks[i]);
			}
		}

		return true;

	} // readRoleForm

	/**
	 * Read the role save as form and make the new role in the realm in edit.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readRoleSaveAsForm(RunData data, SessionState state)
	{
		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");

		// we will copy this role
		Role role = (Role) state.getAttribute("role");

		// read the form
		String id = StringUtils.trimToNull(data.getParameters().getString("id"));

		if (id == null)
		{
			addAlert(state, rb.getString("realm.please"));
			return false;
		}

		if (realm != null)
		{
			// create the role
			try
			{
				realm.addRole(id, role);
			}
			catch (RoleAlreadyDefinedException e)
			{
				addAlert(state, rb.getFormattedMessage("realm.defined", new Object[]{id}) );
				return false;
			}
		}

		return true;

	} // readRoleSaveAsForm

	/**
	 * create a new user ability grant in the realm edit.
	 */
	public void doNew_user(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		state.setAttribute("mode", "newUser");

		// mark the realm as new, so on cancel it can be deleted
		state.setAttribute("newUser", "true");

	} // doNew_user

	/**
	 * Edit an existing user ability grant.
	 */
	public void doEdit_user(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readRealmForm(data, state)) return;

		String id = data.getParameters().getString("target");
		try
		{
			User user = userDirectoryService.getUser(id);
			state.setAttribute("user", user);
			state.setAttribute("mode", "editUser");
		}
		catch (UserNotDefinedException e)
		{
		 	log.warn("user not found: {}", id);
			addAlert(state, rb.getString("realm.user.notfound"));
		}

	} // doEdit_user

	/**
	 * Handle a request to remove all grants to the user.
	 */
	public void doRemove_user(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// the user we are editing
		User user = (User) state.getAttribute("user");

		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		
		if (realm != null && user != null)
		{
			// Need to grab the role before removing the user from the realm
			// Need to grab the role before removing the user from the realm
			Role role = realm.getUserRole(user.getId());
			String roleId = "";
			if (role != null) {
				roleId = role.getId();
			}
			
			// clear out this user's settings
			realm.removeMember(user.getId());
			
			// user auditing
			addToAuditLogList(state, realm, user.getEid(), roleId);
	
			// done with the user
			state.removeAttribute("user");
	
			// return to edit mode
			state.setAttribute("mode", "edit");
		}

	} // doRemove_user

	/**
	 * Handle a request to save the realm edit (from the user edit form).
	 */
	public void doSave_user(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readUserForm(data, state)) return;

		doSave_edit(data, context);

	} // doSave_user

	/**
	 * Handle a request to be done user editing - return to the edit mode.
	 */
	public void doDone_user(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readUserForm(data, state)) return;

		// cleanup
		state.removeAttribute("user");

		// go back to edit mode
		state.setAttribute("mode", "edit");

	} // doDone_user

	/**
	 * Read the user form and update the realm in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readUserForm(RunData data, SessionState state)
	{
		// get the role
		String roles = StringUtils.trimToNull(data.getParameters().getString("roles"));
		
		//get status
		Boolean status=true;
		String checkForStatus=data.getParameters().get("status");
		if (!(checkForStatus==null)){
		status=data.getParameters().getBoolean("status");
		}

		// we are setting for either a new user or this user
		User user = (User) state.getAttribute("user");
		if (user == null)
		{
			// read the form
			String eid = StringUtils.trimToNull(data.getParameters().getString("eid"));

			// if the field is missing, and there are no roles, just be done with no change
			if ((eid == null) && (roles == null)) return true;

			try
			{
				user = userDirectoryService.getUserByEid(eid);
			}
			catch (UserNotDefinedException e)
			{
				addAlert(state, rb.getString("realm.user"));
				return false;
				// TODO: would be nice to read the roles, and restore them when the form returns -ggolden
			}
		}

		// get the realm
		AuthzGroup realm = (AuthzGroup) state.getAttribute("realm");
		if (realm != null)
		{
			// if the user is set to have the same role the user already has, do nothing
			Member grant = realm.getMember(user.getId());
	
			// if no change, change nothing
			if (roles == null && (grant == null && (checkForStatus == null) ) ) {
			    // removing this since it would cause a null pointer exception if the code got here so we think the code does not ever get here -AZ
			    //) || (grant.isProvided())))) return true;
			    return true;
			}
			if ((roles != null) && (grant != null) && (grant.getRole().getId().equals(roles) && (grant.isActive()==status)) && !grant.isProvided()) return true;
	
			// clear out this user's settings
			realm.removeMember(user.getId());
	
			// if there's a role, give it
			if (roles != null)
			{
				// TODO: active, provided
				realm.addMember(user.getId(), roles, status, false);
				
				// user auditing
				addToAuditLogList(state, realm, user.getEid(), roles);
			}
		}

		return true;

	} // readUserForm

	/**
	 * Clean up all possible state value when done an edit.
	 */
	private void cleanState(SessionState state)
	{
		state.removeAttribute("realm");
		state.removeAttribute("role");
		state.removeAttribute("user");
		state.removeAttribute("new");
		state.removeAttribute("newRole");
		state.removeAttribute("newUser");
		state.removeAttribute("allRoles");
		state.removeAttribute("allLocks");
		state.removeAttribute("roles");
		state.removeAttribute("locks");
		state.removeAttribute("userAuditList");

	} // cleanState
	
	/**
	 * Check if the current user is allowed to access the tool
	 * 
	 * Super users are allowed, as well as people with the AuthzGroupService.SECURE_VIEW_ALL_AUTHZ_GROUPS permission.
	 */
	private boolean isAccessAllowed() {
		if(securityService.isSuperUser()) {
			return true;
		}
		String siteId = toolManager.getCurrentPlacement().getContext();
		String siteRef = siteId;
		if(siteId != null && !siteId.startsWith(SiteService.REFERENCE_ROOT)) {
			siteRef = SiteService.REFERENCE_ROOT + Entity.SEPARATOR + siteId;
		}
		
		String userId = sessionManager.getCurrentSessionUserId();
		
		if(securityService.unlock(userId, AuthzGroupService.SECURE_VIEW_ALL_AUTHZ_GROUPS, siteRef)) {
			log.debug("Granting view access to Realms tool for userId: {}", userId);
			return true;
		}
		
		return false;
	}
	
	private List<String[]> retrieveAuditLogList(SessionState state)
	{
		// user auditing
		List<String[]> userAuditList = (List<String[]>) state.getAttribute("userAuditList");
		if (userAuditList!=null && !userAuditList.isEmpty())
		{
			state.removeAttribute("userAuditList");
		}
		else
		{
			userAuditList = new ArrayList<String[]>();
		}
		
		return userAuditList;
	}
	
	private void addToAuditLogList(SessionState state, AuthzGroup realm, String userEid, String userRole)
	{
		List<String[]> userAuditList = retrieveAuditLogList(state);
		
		String realmId = realm.getId();
		String siteId = "";
		String fullReferenceRoot = SiteService.REFERENCE_ROOT + Entity.SEPARATOR;
		if (realmId.startsWith(fullReferenceRoot))
		{
			siteId = realmId.substring(fullReferenceRoot.length());
		}
		else
		{
			// this will likely never happen, but adding it in as a backup
			siteId = realmId;
		}
		String newOrExistingUser = (String) state.getAttribute("newUser");
		String userAuditAction = userAuditService.USER_AUDIT_ACTION_UPDATE;
		
		// if this using the Grant As functionality, it will be a new user being added
		if (newOrExistingUser!=null && "true".equals(newOrExistingUser))
		{
			userAuditAction = userAuditService.USER_AUDIT_ACTION_ADD;
		}
		String[] userAuditString = {siteId,userEid,userRole,userAuditAction,userAuditRegistration.getDatabaseSourceKey(),userDirectoryService.getCurrentUser().getEid()};
		userAuditList.add(userAuditString);
		
		state.setAttribute("userAuditList", userAuditList);
	}

} // RealmsAction
