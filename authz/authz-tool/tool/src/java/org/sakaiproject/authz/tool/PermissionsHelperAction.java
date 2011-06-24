/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.authz.tool;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * This is a helper interface to the Permissions tool.
 */
public class PermissionsHelperAction extends VelocityPortletPaneledAction
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(PermissionsHelperAction.class);

	private static ResourceLoader rb = new ResourceLoader("authz-tool");

	private static final String STARTED = "sakaiproject.permissions.started";

	/** State attributes for Permissions mode - when it is MODE_DONE the tool can process the results. */
	public static final String STATE_MODE = "pemissions.mode";

	/** State attribute for the realm id - users should set before starting. */
	public static final String STATE_REALM_ID = "permission.realmId";

	/** State attribute for the realm id - users should set before starting. */
	public static final String STATE_REALM_ROLES_ID = "permission.realmRolesId";

	/** State attribute for the description of what's being edited - users should set before starting. */
	public static final String STATE_DESCRIPTION = "permission.description";

	/** State attribute for the lock/ability string prefix to be presented / edited - users should set before starting. */
	public static final String STATE_PREFIX = "permission.prefix";

	/** State attributes for storing the realm being edited. */
	private static final String STATE_REALM_EDIT = "permission.realm";

	/** State attributes for storing the current selected realm being edited. */
	private static final String STATE_VIEW_REALM_EDIT = "permission.view.realm";

	/** State attributes for storing the abilities, filtered by the prefix. */
	private static final String STATE_ABILITIES = "permission.abilities";

	/** State attribute for storing the roles to display. */
	private static final String STATE_ROLES = "permission.roles";

	/** State attribute for storing the abilities of each role for this resource. */
	private static final String STATE_ROLE_ABILITIES = "permission.rolesAbilities";

	/** State attribute for permission description */
	public static final String STATE_PERMISSION_DESCRIPTIONS = "permission.descriptions";

	/** Modes. */
	public static final String MODE_MAIN = "main";

	/** vm files for each mode. TODO: path too hard coded */
	private static final String TEMPLATE_MAIN = "helper/chef_permissions";
	
	private static final String STATE_GROUP_AWARE = "state_group_aware";

	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		SessionState sstate = getState(req);
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		String mode = (String) sstate.getAttribute(STATE_MODE);
		Object started = toolSession.getAttribute(STARTED);

		if (mode == null && started != null)
		{
			toolSession.removeAttribute(STARTED);
			Tool tool = ToolManager.getCurrentTool();

			String url = (String) SessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			SessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				Log.warn("chef", this + " : ", e);
			}
			return;
		}

		super.toolModeDispatch(methodBase, methodExt, req, res);
	}

	/**
	 * Allow extension classes to control which build method gets called for this pannel
	 * @param panel
	 * @return
	 */
	protected String panelMethodName(String panel)
	{
		// we are always calling buildMainPanelContext
		return "buildMainPanelContext";
	}

	/**
	 * Default is to use when Portal starts up
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		String mode = (String) sstate.getAttribute(STATE_MODE);

		if (mode == null)
		{
			initHelper(portlet, context, rundata, sstate);
		}

		String template = buildHelperContext(portlet, context, rundata, sstate);
		if (template == null)
		{
			addAlert(sstate, rb.getString("java.alert.prbset"));
		}
		else
		{
			return template;
		}

		return null;
	}

	protected void initHelper(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		String prefix = (String) toolSession.getAttribute(PermissionsHelper.PREFIX);
		String targetRef = (String) toolSession.getAttribute(PermissionsHelper.TARGET_REF);
		String description = (String) toolSession.getAttribute(PermissionsHelper.DESCRIPTION);
		String rolesRef = (String) toolSession.getAttribute(PermissionsHelper.ROLES_REF);
		
		if (rolesRef == null) rolesRef = targetRef;

		toolSession.setAttribute(STARTED, Boolean.valueOf(true));

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(STATE_REALM_ID, targetRef);
		
		// use the roles from this ref's AuthzGroup
		state.setAttribute(STATE_REALM_ROLES_ID, rolesRef);

		// ... with this description
		state.setAttribute(STATE_DESCRIPTION, description);

		// ... showing only locks that are prpefixed with this
		state.setAttribute(STATE_PREFIX, prefix);

		// ... set the ResourceLoader object
		state.setAttribute(STATE_PERMISSION_DESCRIPTIONS, toolSession.getAttribute(PermissionsHelper.PERMISSION_DESCRIPTION));
		
		// start the helper
		state.setAttribute(STATE_MODE, MODE_MAIN);
		
		state.setAttribute(STATE_GROUP_AWARE, toolSession.getAttribute("groupAware"));
	}
	
	/**
	 * build the context.
	 * 
	 * @return The name of the template to use.
	 */
	static public String buildHelperContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// in state is the realm id
		context.put("thelp", rb);
		String realmId = (String) state.getAttribute(STATE_REALM_ID);

		// in state is the realm to use for roles - if not, use realmId
		String realmRolesId = (String) state.getAttribute(STATE_REALM_ROLES_ID);
		context.put("viewRealmId", realmRolesId);
		
		// get the realm locked for editing
		AuthzGroup edit = (AuthzGroup) state.getAttribute(STATE_REALM_EDIT);
		if (edit == null)
		{
			if (AuthzGroupService.allowUpdate(realmId))
			{
				try
				{
					edit = AuthzGroupService.getAuthzGroup(realmId);
					state.setAttribute(STATE_REALM_EDIT, edit);
				}
				catch (GroupNotDefinedException e)
				{
					try
					{
						// we can create the realm
						edit = AuthzGroupService.addAuthzGroup(realmId);
						state.setAttribute(STATE_REALM_EDIT, edit);
					}
					catch (GroupIdInvalidException ee)
					{
						M_log.warn("PermissionsAction.buildHelperContext: addRealm: " + ee);
						cleanupState(state);
						return null;
					}
					catch (GroupAlreadyDefinedException ee)
					{
						M_log.warn("PermissionsAction.buildHelperContext: addRealm: " + ee);
						cleanupState(state);
						return null;
					}
					catch (AuthzPermissionException ee)
					{
						M_log.warn("PermissionsAction.buildHelperContext: addRealm: " + ee);
						cleanupState(state);
						return null;
					}
				}
			}

			// no permission
			else
			{
				M_log.warn("PermissionsAction.buildHelperContext: no permission: " + realmId);
				cleanupState(state);
				return null;
			}
		}
		
		AuthzGroup viewEdit = null;
		// check wither the current realm id is of site group type
		if (realmId.indexOf(SiteService.REFERENCE_ROOT) != -1)
		{
			String siteId = realmId.replaceAll(SiteService.REFERENCE_ROOT + "/", "");
			context.put("siteRef", realmId);
			
			if (state.getAttribute(STATE_GROUP_AWARE) != null && ((Boolean) state.getAttribute(STATE_GROUP_AWARE)).booleanValue())
			{
				// only show groups for group-aware tools
				try
				{
					Site site = SiteService.getSite(siteId);
					Collection groups = site.getGroups();
					if (groups != null && !groups.isEmpty())
					{
						Iterator iGroups = groups.iterator();
						for(; iGroups.hasNext();)
						{
							Group group = (Group) iGroups.next();
							// need to either have realm update permission on the group level or better at the site level
							if (!AuthzGroupService.allowUpdate(group.getReference()))
							{
								iGroups.remove();
							}
						}
						context.put("groups", groups);
					}
						
				}
				catch (Exception siteException)
				{
					M_log.warn("PermissionsAction.buildHelperContext: getsite of realm id =  " + realmId + siteException);
				}
			}
			
			// get the realm locked for editing
			viewEdit = (AuthzGroup) state.getAttribute(STATE_VIEW_REALM_EDIT);
			if (viewEdit == null)
			{
				if (AuthzGroupService.allowUpdate(realmRolesId) || AuthzGroupService.allowUpdate(SiteService.siteReference(siteId)))
				{
					try
					{
						viewEdit = AuthzGroupService.getAuthzGroup(realmRolesId);
						state.setAttribute(STATE_VIEW_REALM_EDIT, viewEdit);
					}
					catch (GroupNotDefinedException e)
					{
						M_log.warn("PermissionsAction.buildHelperContext: getRealm with id= " + realmRolesId + " : " + e);
						cleanupState(state);
						return null;
					}
				}
	
				// no permission
				else
				{
					M_log.warn("PermissionsAction.buildHelperContext: no permission: " + realmId);
					cleanupState(state);
					return null;
				}
			}
		}

		// in state is the prefix for abilities to present
		String prefix = (String) state.getAttribute(STATE_PREFIX);

		// in state is the list of abilities we will present
		List functions = (List) state.getAttribute(STATE_ABILITIES);
		if (functions == null)
		{
			// get all functions prefixed with our prefix
			functions = FunctionManager.getRegisteredFunctions(prefix);
		}
		
		if (functions != null && !functions.isEmpty())
		{
			List<String> nFunctions = new Vector<String>();
			if (!realmRolesId.equals(realmId))
			{
				// editing groups within site, need to filter out those permissions only applicable to site level
				for (Iterator iFunctions = functions.iterator(); iFunctions.hasNext();)
				{
					String function = (String) iFunctions.next();
					if (function.indexOf("all.groups") == -1)
					{
						nFunctions.add(function);
					}
				}
			}
			else
			{
				nFunctions.addAll(functions);
			}
			state.setAttribute(STATE_ABILITIES, nFunctions);
			context.put("abilities", nFunctions);
			
			// get function description from passed in HashMap
			// output permission descriptions
			Map<String, String> functionDescriptions = (Map<String, String>) state.getAttribute(STATE_PERMISSION_DESCRIPTIONS);
			if (functionDescriptions != null)
			{
				Set keySet = functionDescriptions.keySet();
				for(Object function : functions)
				{
					String desc = (String) function;
					String descKey = PermissionsHelper.PREFIX_PERMISSION_DESCRIPTION + function;
					if (keySet.contains(descKey))
					{
						// use function description
						desc = (String) functionDescriptions.get(descKey);
					}
	
					functionDescriptions.put((String) function, desc);
				}
				context.put("functionDescriptions", functionDescriptions);
			
			}
		}

		// in state is the description of the edit
		String description = (String) state.getAttribute(STATE_DESCRIPTION);

		// the list of roles
		List roles = (List) state.getAttribute(STATE_ROLES);
		if (roles == null)
		{
			// get the roles from the edit, unless another is specified
			AuthzGroup roleRealm = viewEdit != null ? viewEdit : edit;
			if (realmRolesId != null)
			{
				try
				{
					roleRealm = AuthzGroupService.getAuthzGroup(realmRolesId);
				}
				catch (Exception e)
				{
					M_log.warn("PermissionsHelperAction.buildHelperContext: getRolesRealm: " + realmRolesId + " : " + e);
				}
			}
			roles = new Vector();
			roles.addAll(roleRealm.getRoles());
			Collections.sort(roles);
			state.setAttribute(STATE_ROLES, roles);
		}

		// the abilities not including this realm for each role
		Map rolesAbilities = (Map) state.getAttribute(STATE_ROLE_ABILITIES);
		if (rolesAbilities == null)
		{
			rolesAbilities = new Hashtable();
			state.setAttribute(STATE_ROLE_ABILITIES, rolesAbilities);

			// get this resource's role Realms,those that refine the role definitions, but not it's own
			Reference ref = EntityManager.newReference(viewEdit != null ? viewEdit.getId() : edit.getId());
			Collection realms = ref.getAuthzGroups();
			realms.remove(ref.getReference());

			for (Iterator iRoles = roles.iterator(); iRoles.hasNext();)
			{
				Role role = (Role) iRoles.next();
				Set locks = AuthzGroupService.getAllowedFunctions(role.getId(), realms);
				rolesAbilities.put(role.getId(), locks);
			}
		}

		context.put("realm", viewEdit != null ? viewEdit : edit);
		context.put("prefix", prefix);
		context.put("description", description);
		if (roles.size() > 0)
		{
			context.put("roles", roles);
		}
		context.put("rolesAbilities", rolesAbilities);

		// make sure observers are disabled
		VelocityPortletPaneledAction.disableObservers(state);

		return TEMPLATE_MAIN;
	}
	/**
	 * Remove the state variables used internally, on the way out.
	 */
	private static void cleanupState(SessionState state)
	{
		state.removeAttribute(STATE_REALM_ID);
		state.removeAttribute(STATE_REALM_ROLES_ID);
		state.removeAttribute(STATE_REALM_EDIT);
		state.removeAttribute(STATE_VIEW_REALM_EDIT);
		state.removeAttribute(STATE_PREFIX);
		state.removeAttribute(STATE_ABILITIES);
		state.removeAttribute(STATE_DESCRIPTION);
		state.removeAttribute(STATE_ROLES);
		state.removeAttribute(STATE_ROLE_ABILITIES);
		state.removeAttribute(STATE_PERMISSION_DESCRIPTIONS);
		state.removeAttribute(STATE_MODE);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_HELPER);
		state.removeAttribute(STATE_GROUP_AWARE);

		// re-enable observers
		VelocityPortletPaneledAction.enableObservers(state);
	}

	/**
	 * to show different permission settings based on user selection of authz group
	 * @param data
	 */
	public void doView_permission_option(RunData data)
	{
		String viewAuthzId = data.getParameters().getString("authzGroupSelection");
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		// reset attributes
		state.setAttribute(STATE_REALM_ROLES_ID, viewAuthzId);
		state.removeAttribute(STATE_VIEW_REALM_EDIT);
		state.removeAttribute(STATE_ABILITIES);
		state.removeAttribute(STATE_ROLES);
		state.removeAttribute(STATE_ROLE_ABILITIES);
	}
	
	/**
	 * Handle the eventSubmit_doSave command to save the edited permissions.
	 */
	public void doSave(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// only save the view realm's roles
		AuthzGroup edit = (AuthzGroup) state.getAttribute(STATE_VIEW_REALM_EDIT);
		if (edit == null)
		{
			edit = (AuthzGroup) state.getAttribute(STATE_REALM_EDIT);
		}
		
		if (edit != null)
		{
			// read the form, updating the edit
			readForm(data, edit, state);
	
			// commit the change
			try
			{
				AuthzGroupService.save(edit);
			}
			catch (GroupNotDefinedException e)
			{
				addAlert(state, rb.getFormattedMessage("alert_sitegroupnotdefined", new Object[]{edit.getReference()}));
			}
			catch (AuthzPermissionException e)
			{
				addAlert(state, rb.getFormattedMessage("alert_permission", new Object[]{edit.getReference()}));
			}
		}

		// clean up state
		cleanupState(state);
	}

	/**
	 * Handle the eventSubmit_doCancel command to abort the edits.
	 */
	public void doCancel(RunData data)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// clean up state
		cleanupState(state);
	}

	/**
	 * Read the permissions form.
	 */
	private void readForm(RunData data, AuthzGroup edit, SessionState state)
	{
		List abilities = (List) state.getAttribute(STATE_ABILITIES);
		List roles = (List) state.getAttribute(STATE_ROLES);

		// look for each role's ability field
		for (Iterator iRoles = roles.iterator(); iRoles.hasNext();)
		{
			Role role = (Role) iRoles.next();

			for (Iterator iLocks = abilities.iterator(); iLocks.hasNext();)
			{
				String lock = (String) iLocks.next();

				String checked = data.getParameters().getString(role.getId() + lock);
				if (checked != null)
				{
					// we have an ability! Make sure there's a role
					Role myRole = edit.getRole(role.getId());
					if (myRole == null)
					{
						try
						{
							myRole = edit.addRole(role.getId());
						}
						catch (RoleAlreadyDefinedException e)
						{
							M_log.warn("PermissionsAction.readForm: addRole after getRole null: " + role.getId() + " : " + e);
						}
					}
					if (myRole != null) {
						myRole.allowFunction(lock);
					}
				}
				else
				{
					// if we do have this role, make sure there's not this lock
					Role myRole = edit.getRole(role.getId());
					if (myRole != null)
					{
						myRole.disallowFunction(lock);
					}
				}
			}
		}
	}
}
