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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupAlreadyDefinedException;
import org.sakaiproject.authz.api.GroupIdInvalidException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * This is a helper interface to the Permissions tool.
 */
@Slf4j
public class PermissionsHelperAction extends VelocityPortletPaneledAction
{

	private static final long serialVersionUID = 1L;

	private static ResourceLoader rb = new ResourceLoader("authz-tool");

	private static final String STARTED = "sakaiproject.permissions.started";

	/** State attributes for Permissions mode - when it is MODE_DONE the tool can process the results. */
	public static final String STATE_MODE = "pemissions.mode";

	/** State attribute for the realm id - users should set before starting. */
	public static final String STATE_REALM_ID = "permission.realmId";

	/** State attribute for the realm id - users should set before starting. */
	public static final String STATE_REALM_ROLES_IDS = "permission.realmRolesId";

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

	/** the prefix to permission title for permission description entry in bundle file */
	public static final String PREFIX_PERMISSION_DESCRIPTION = "desc-";

	/** Modes. */
	public static final String MODE_MAIN = "main";

	private static final String STATE_GROUP_AWARE = "state_group_aware";

	private AuthzGroupService authzGroupService;
	private FunctionManager functionManager;
	private SecurityService securityService;
	private EntityManager entityManager;
	private SiteService siteService;
	private SessionManager sessionManager;
	private ToolManager toolManager;
	private ServerConfigurationService serverConfigurationService;

	public PermissionsHelperAction() {
		super();
		authzGroupService = ComponentManager.get(AuthzGroupService.class);
		functionManager = ComponentManager.get(FunctionManager.class);
		securityService = ComponentManager.get(SecurityService.class);
		entityManager = ComponentManager.get(EntityManager.class);
		siteService = ComponentManager.get(SiteService.class);
		sessionManager = ComponentManager.get(SessionManager.class);
		toolManager = ComponentManager.get(ToolManager.class);
		serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
	}

	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		SessionState sstate = getState(req);
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		String mode = (String) sstate.getAttribute(STATE_MODE);
		Object started = toolSession.getAttribute(STARTED);

		if (mode == null && started != null)
		{
			toolSession.removeAttribute(STARTED);
			Tool tool = toolManager.getCurrentTool();

			String url = (String) sessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			sessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
			 	log.warn(e.getMessage());
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
		// May be null.
		return template;
	}

	protected void initHelper(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		ToolSession toolSession = sessionManager.getCurrentToolSession();

		String prefix = (String) toolSession.getAttribute(PermissionsHelper.PREFIX);
		String targetRef = (String) toolSession.getAttribute(PermissionsHelper.TARGET_REF);
		String description = (String) toolSession.getAttribute(PermissionsHelper.DESCRIPTION);
		Object rolesRef = toolSession.getAttribute(PermissionsHelper.ROLES_REF);
		if (rolesRef == null) rolesRef = targetRef;

		Collection<String> rolesRefs;
		if (rolesRef instanceof Collection) {
			rolesRefs = (Collection<String>) rolesRef;
		} else {
			rolesRefs = Collections.singletonList((String) rolesRef);
		}

		toolSession.setAttribute(STARTED, Boolean.valueOf(true));

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(STATE_REALM_ID, targetRef);
		
		// use the roles from this ref's AuthzGroup
		state.setAttribute(STATE_REALM_ROLES_IDS, rolesRefs);

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
	 * @return The name of the template to use. <code>null</code> can be returned.
	 */
	public String buildHelperContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// in state is the realm id
		context.put("thelp", rb);
		String realmId = (String) state.getAttribute(STATE_REALM_ID);

		// in state is the realm to use for roles - if not, use realmId
		Collection<String> realmRolesIds = (Collection<String>) state.getAttribute(STATE_REALM_ROLES_IDS);
		context.put("viewRealmIds", realmRolesIds);
		
		// get the realm locked for editing
		AuthzGroup edit = (AuthzGroup) state.getAttribute(STATE_REALM_EDIT);
		if (edit == null)
		{
			if (authzGroupService.allowUpdate(realmId))
			{
				try
				{
					edit = authzGroupService.getAuthzGroup(realmId);
					state.setAttribute(STATE_REALM_EDIT, edit);
				}
				catch (GroupNotDefinedException e)
				{
					try
					{
						// we can create the realm
						edit = authzGroupService.addAuthzGroup(realmId);
						state.setAttribute(STATE_REALM_EDIT, edit);
					}
					catch (GroupIdInvalidException ee)
					{
						log.warn("PermissionsAction.buildHelperContext: addRealm: " + ee);
						cleanupState(state);
						return null;
					}
					catch (GroupAlreadyDefinedException ee)
					{
						log.warn("PermissionsAction.buildHelperContext: addRealm: " + ee);
						cleanupState(state);
						return null;
					}
					catch (AuthzPermissionException ee)
					{
						log.warn("PermissionsAction.buildHelperContext: addRealm: " + ee);
						cleanupState(state);
						return null;
					}
				}
			}

			// no permission
			else
			{
				log.warn("PermissionsAction.buildHelperContext: no permission: {}", realmId);
				cleanupState(state);
				addAlert(state, rb.getFormattedMessage("alert_permission", new Object[]{realmId}));
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
					Site site = siteService.getSite(siteId);
					Collection groups = site.getGroups();
					if (groups != null && !groups.isEmpty())
					{
						Iterator iGroups = groups.iterator();
						for(; iGroups.hasNext();)
						{
							Group group = (Group) iGroups.next();
							// need to either have realm update permission on the group level or better at the site level
							if (!authzGroupService.allowUpdate(group.getReference()))
							{
								iGroups.remove();
							}
						}
						context.put("groups", groups);
					}
						
				}
				catch (Exception siteException)
				{
					log.warn("PermissionsAction.buildHelperContext: getsite of realm id = {} {}", realmId, siteException);
				}
			}
			
			// get the realm locked for editing
			viewEdit = (AuthzGroup) state.getAttribute(STATE_VIEW_REALM_EDIT);
			if (viewEdit == null)
			{
				// I have no idea why this step is performed since we should never edit the template realm, so let's not be too serious about it
				String realmRolesId = realmRolesIds.iterator().next();
				
				if (authzGroupService.allowUpdate(realmRolesId) || authzGroupService.allowUpdate(siteService.siteReference(siteId)))
				{
					try
					{
						viewEdit = authzGroupService.getAuthzGroup(realmRolesId);
						state.setAttribute(STATE_VIEW_REALM_EDIT, viewEdit);
					}
					catch (GroupNotDefinedException e)
					{
						log.warn("PermissionsAction.buildHelperContext: getRealm with id={} : {}", realmRolesId, e);
						cleanupState(state);
						return null;
					}
				}
	
				// no permission
				else
				{
					log.warn("PermissionsAction.buildHelperContext: no permission: {}", realmId);
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
			functions = functionManager.getRegisteredFunctions(prefix);
		}
		
		if (functions != null && !functions.isEmpty())
		{
			List<String> nFunctions = new Vector<String>();
			if (!realmRolesIds.contains(realmId))
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
					String descKey = PREFIX_PERMISSION_DESCRIPTION + function;
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
		if (roles == null && realmRolesIds != null)
		{
			// get the roles from the edit, unless another is specified
			AuthzGroup roleRealm = viewEdit != null ? viewEdit : edit;
			Collection<Role> rolesUnique = new HashSet<Role>();
			for (String realmRoleId : realmRolesIds)
			{
				if (realmRoleId != null)
				{
					try
					{
						roleRealm = authzGroupService.getAuthzGroup(realmRoleId);
					}
					catch (Exception e)
					{
						log.warn("PermissionsHelperAction.buildHelperContext: getRolesRealm: {} : {}", realmRoleId, e);
					}
				}

				rolesUnique.addAll(roleRealm.getRoles());
			}
			roles = new Vector(rolesUnique);
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
			Reference ref = entityManager.newReference(viewEdit != null ? viewEdit.getId() : edit.getId());
			Collection realms = ref.getAuthzGroups();
			realms.remove(ref.getReference());

			for (Iterator iRoles = roles.iterator(); iRoles.hasNext();)
			{
				Role role = (Role) iRoles.next();
				Set locks = authzGroupService.getAllowedFunctions(role.getId(), realms);
				rolesAbilities.put(role.getId(), locks);
			}
		}

		PermissionLimiter limiter = getPermissionLimiter();

		context.put("limiter", limiter);

        context.put("roleName", new RoleNameLookup());
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

		return getContext(rundata).get("template");
	}
	/**
	 * Find a map of permissions based on a config prefix.
	 * If there aren't any permissions list all are allowed.
	 *
	 * @param configPrefix The prefix to get permissions for.
	 */
	private Map<String, Set<String>> getPermissions(String configPrefix)
	{
		Map<String, Set<String>> roleMap = new HashMap<String, Set<String>>();
		String roleList = serverConfigurationService.getString(configPrefix+ "roles", "");
		Set<String> defaultPermissionSet = createPermissionSet(configPrefix, "default");
		for (String roleName :roleList.split(","))
		{
			roleName = roleName.trim();
			if (roleName.length() == 0)
			{
				continue;
			}
			Set<String> permissionSet = createPermissionSet(configPrefix, roleName);
			roleMap.put(roleName, (permissionSet.size() > 0)?permissionSet:defaultPermissionSet);

		}
		return roleMap;
	}
	
	private Set<String> createPermissionSet(String config, String roleName)
	{
		String permissionList = org.sakaiproject.component.cover.ServerConfigurationService.getString(config +roleName,"");
		Set<String> permissionSet = new HashSet<String>();
		for (String permissionName : permissionList.split(","))
		{
			permissionName = permissionName.trim();
			if (permissionName.length() > 0)
			{
				permissionSet.add(permissionName);
			}
		}
		return permissionSet;
	}


	/**
	 * Remove the state variables used internally, on the way out.
	 */
	private void cleanupState(SessionState state)
	{
		state.removeAttribute(STATE_REALM_ID);
		state.removeAttribute(STATE_REALM_ROLES_IDS);
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
		Collection<String> viewAuthzIds = Collections.singletonList(viewAuthzId);
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		// reset attributes
		state.setAttribute(STATE_REALM_ROLES_IDS, Collections.singletonList(viewAuthzId));
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

		if (!"POST".equals(data.getRequest().getMethod())) {
			log.warn("PermissionsAction.doSave: user did not submit with a POST! IP={}", data.getRequest().getRemoteAddr());
			return;
		}

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
				removeEmptyRoles(edit);

				if (hasNothingSet(edit)) {
					authzGroupService.removeAuthzGroup(edit);
				} else {
					authzGroupService.save(edit);
				}
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
	 * Removes all the roles in an AuthzGroup that don't have any permissions set on them.
	 * @param edit The AuthzGroup to cleanup.
	 */
	private void removeEmptyRoles(AuthzGroup edit) {
		for (Role role : edit.getRoles()) {
			if(role.getAllowedFunctions().isEmpty()) {
				edit.removeRole(role.getId());
			}
		}
	}

	/**
	 * @param edit The AuthzGroup to check.
	 * @return <code>true</code> if there are no roles and no members in this AuthzGroup.
	 */
	private boolean hasNothingSet(AuthzGroup edit) {
		return edit.getRoles().isEmpty() && edit.getMembers().isEmpty();
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

		PermissionLimiter limiter = getPermissionLimiter();
		// look for each role's ability field
		for (Iterator iRoles = roles.iterator(); iRoles.hasNext();)
		{
			Role role = (Role) iRoles.next();

			for (Iterator iLocks = abilities.iterator(); iLocks.hasNext();)
			{
				String lock = (String) iLocks.next();
				boolean checked = (data.getParameters().getString(role.getId() + lock) != null);
				// Don't allow changes to some permissions.
				if ( !(limiter.isEnabled(role.getId(), lock, role.isAllowed(lock))) )
				{
					log.debug("Can't change permission '{}' on role '{}'.", lock, role.getId());
					continue;
				}

				if (checked)
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
							log.warn("PermissionsAction.readForm: addRole after getRole null: {} : {}", role.getId(), e);
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

	public PermissionLimiter getPermissionLimiter() {
		Map allowedPermissions = getPermissions("realm.allowed."); // Whitelisted permissions for some roles
		Map frozenPermissions = getPermissions("realm.frozen."); // Permissions that can't be changed
		Map addOnlyPermissions = getPermissions("realm.add.only."); // Permissions that can only be added.	}
		return new PermissionLimiter(allowedPermissions, frozenPermissions, addOnlyPermissions);
	}

	/**
	 * The class is put into the velocity context to limit the permission that can be set.
	 */
	public class PermissionLimiter
	{
		private Map<String, Set<String>> allowedPermissions;
		private Map<String, Set<String>> frozenPermissions;
		private Map<String, Set<String>> addOnlyPermissions;

		/**
		 * Create a permission limiter. This is put into the velocity context to remove complex logic from the template.
		 *
		 * @param allowedPermissions A complete set of permissions for a role. If the role exists in this map but the
		 *                           permission isn't present the user can't set it.
		 * @param frozenPermissions A set of permissions that can't be changed for each role.
		 * @param addOnlyPermissions A set of permissions which the user can only grant and can't take away.
		 */
		public PermissionLimiter(Map<String, Set<String>> allowedPermissions, Map<String, Set<String>> frozenPermissions,
								 Map<String, Set<String>> addOnlyPermissions)
		{
			this.allowedPermissions = allowedPermissions;
			this.frozenPermissions = frozenPermissions;
			this.addOnlyPermissions = addOnlyPermissions;
		}

		public boolean isEnabled(String roleId, String permission, boolean enabled)
		{
			// Sysadmin doesn't have any restrictions
			if (securityService.isSuperUser()) {
				return true;
			}
			if (frozenPermissions.containsKey(roleId)) {
				if ( frozenPermissions.get(roleId).contains(permission) ) {
					return false;
				}
			}
			// Only check when permission is enabled.
			if (enabled && addOnlyPermissions.containsKey(roleId)) {
				if ( addOnlyPermissions.get(roleId).contains(permission) ) {
					return false;
				}
			}
			if (allowedPermissions.containsKey(roleId)) {
				return allowedPermissions.get(roleId).contains(permission);
			}
			return true;
		}
	}

 	public class RoleNameLookup {

 		public String getName(String roleId) {
 			return authzGroupService.getRoleName(roleId);
 		}
 	}
}
