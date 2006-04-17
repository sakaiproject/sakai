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

// package
package org.sakaiproject.tool.admin;

// imports
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.util.java.ResourceLoader;
import org.sakaiproject.api.common.authentication.Authentication;
import org.sakaiproject.api.common.authentication.AuthenticationException;
import org.sakaiproject.api.common.authentication.Evidence;
import org.sakaiproject.api.common.authentication.cover.AuthenticationManager;
import org.sakaiproject.api.kernel.session.cover.SessionManager;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PagedResourceActionII;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.menu.Menu;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuItem;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.service.framework.config.cover.ServerConfigurationService;
import org.sakaiproject.service.framework.current.cover.CurrentService;
import org.sakaiproject.service.framework.session.SessionState;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.UserEdit;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;
import org.sakaiproject.util.ExternalTrustedEvidence;
import org.sakaiproject.util.LoginUtil;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.java.StringUtil;

/**
 * <p>
 * UsersAction is the Sakai users editor.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision$
 */
public class UsersAction extends PagedResourceActionII
{
	private static ResourceLoader rb = new ResourceLoader("admin");

	/**
	 * {@inheritDoc}
	 */
	protected List readResourcesPage(SessionState state, int first, int last)
	{
		// search?
		String search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));

		if (search != null)
		{
			return UserDirectoryService.searchUsers(search, first, last);
		}

		return UserDirectoryService.getUsers(first, last);
	}

	/**
	 * {@inheritDoc}
	 */
	protected int sizeResources(SessionState state)
	{
		// search?
		String search = StringUtil.trimToNull((String) state.getAttribute(STATE_SEARCH));

		if (search != null)
		{
			return UserDirectoryService.countSearchUsers(search);
		}

		return UserDirectoryService.countUsers();
	}

	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		PortletConfig config = portlet.getPortletConfig();

		if (state.getAttribute("single-user") == null)
		{
			state.setAttribute("single-user", new Boolean(config.getInitParameter("single-user", "false")));
			state.setAttribute("include-password", new Boolean(config.getInitParameter("include-password", "true")));
		}

		if (state.getAttribute("create-user") == null)
		{
			state.setAttribute("create-user", new Boolean(config.getInitParameter("create-user", "false")));
			state.setAttribute("create-login", new Boolean(config.getInitParameter("create-login", "false")));
		}

		if (state.getAttribute("create-type") == null)
		{
			state.setAttribute("create-type", config.getInitParameter("create-type", ""));
		}

		// if (!(((Boolean) state.getAttribute("single-user")).booleanValue()
		// || ((Boolean) state.getAttribute("create-user")).booleanValue()))
		// {
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
		// String pattern = UserDirectoryService.userReference("");
		//
		// state.setAttribute(STATE_OBSERVER, new EventObservingCourier(deliveryId, elementId, pattern));
		// }
		// }

	} // initState

	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		String template = null;

		// for the create-user create-login case, we set this in the do so we can process the redirect here
		if (state.getAttribute("redirect") != null)
		{
			state.removeAttribute("redirect");
			sendParentRedirect((HttpServletResponse) CurrentService.getInThread(RequestFilter.CURRENT_HTTP_RESPONSE),
					ServerConfigurationService.getPortalUrl());
			return template;
		}

		// put $action into context for menus, forms and links
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		// check mode and dispatch
		String mode = (String) state.getAttribute("mode");
		boolean singleUser = ((Boolean) state.getAttribute("single-user")).booleanValue();
		boolean createUser = ((Boolean) state.getAttribute("create-user")).booleanValue();

		if ((singleUser) && (mode != null) && (mode.equals("edit")))
		{
			template = buildEditContext(state, context);
		}
		else if (singleUser)
		{
			String id = SessionManager.getCurrentSessionUserId();
			state.setAttribute("user-id", id);
			template = buildViewContext(state, context);
		}
		else if (createUser)
		{
			template = buildCreateContext(state, context);
		}
		else if (mode == null)
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
		else
		{
			Log.warn("chef", "UsersAction: mode: " + mode);
			template = buildListContext(state, context);
		}

		String prefix = (String) getContext(rundata).get("template");
		return prefix + template;

	} // buildNormalContext

	/**
	 * Build the context for the main list mode.
	 */
	private String buildListContext(SessionState state, Context context)
	{
		// put the service in the context
		context.put("service", UserDirectoryService.getInstance());

		// put all (internal) users into the context
		context.put("users", prepPage(state));

		// build the menu
		Menu bar = new Menu();
		if (UserDirectoryService.allowAddUser(""))
		{
			bar.add(new MenuEntry(rb.getString("useact.newuse"), null, true, MenuItem.CHECKED_NA, "doNew"));
		}

		// add the paging commands
		addListPagingMenus(bar, state);

		// add the search commands
		addSearchMenus(bar, state);

		// add the refresh commands
		addRefreshMenus(bar, state);

		if (bar.size() > 0)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		return "_list";

	} // buildListContext

	/**
	 * Build the context for the new user mode.
	 */
	private String buildNewContext(SessionState state, Context context)
	{
		// include the password fields?
		context.put("incPw", state.getAttribute("include-password"));

		context.put("incType", Boolean.valueOf(true));
		
		return "_edit";

	} // buildNewContext

	/**
	 * Build the context for the create user mode.
	 */
	private String buildCreateContext(SessionState state, Context context)
	{
		// is the type to be pre-set
		context.put("type", state.getAttribute("create-type"));
		
		// password is required when using Gateway New Account tool
		// attribute "create-user" is true only for New Account tool
		context.put("pwRequired", state.getAttribute("create-user"));

		return "_create";

	} // buildCreateContext

	/**
	 * Build the context for the new user mode.
	 */
	private String buildEditContext(SessionState state, Context context)
	{
		// name the html form for user edit fields
		context.put("form-name", "user-form");

		// get the user to edit
		UserEdit user = (UserEdit) state.getAttribute("user");
		context.put("user", user);

		// include the password fields?
		context.put("incPw", state.getAttribute("include-password"));

		// include type fields (not if single user)
		boolean singleUser = ((Boolean) state.getAttribute("single-user")).booleanValue();
		context.put("incType", Boolean.valueOf(!singleUser));

		// build the menu
		// we need the form fields for the remove...
		boolean menuPopulated = false;
		Menu bar = new Menu();
		if ((!singleUser) && (UserDirectoryService.allowRemoveUser(user.getId())))
		{
			bar.add(new MenuEntry(rb.getString("useact.remuse"), null, true, MenuItem.CHECKED_NA, "doRemove", "user-form"));
			menuPopulated = true;
		}

		if (menuPopulated)
		{
			context.put(Menu.CONTEXT_MENU, bar);
		}

		return "_edit";

	} // buildEditContext

	/**
	 * Build the context for the view user mode.
	 */
	private String buildViewContext(SessionState state, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled())
		{
			Log.debug("chef", this + ".buildViewContext");
		}

		// get current user's id
		String id = (String) state.getAttribute("user-id");

		// get the user and put in state as "user"
		try
		{
			User user = UserDirectoryService.getUser(id);
			context.put("user", user);

			// name the html form for user edit fields
			context.put("form-name", "user-form");

			state.setAttribute("mode", "view");

			// make sure we can do an edit
			try
			{
				UserEdit edit = UserDirectoryService.editUser(id);
				UserDirectoryService.cancelEdit(edit);
				context.put("enableEdit", "true");
			}
			catch (IdUnusedException e)
			{
			}
			catch (PermissionException e)
			{
			}
			catch (InUseException e)
			{
			}

			// disable auto-updates while not in list mode
			disableObservers(state);
		}
		catch (IdUnusedException e)
		{
			Log.warn("chef", "UsersAction.doEdit: user not found: " + id);

			addAlert(state, rb.getString("useact.use") + " " + id + " " + rb.getString("useact.notfou"));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

		return "_view";

	} // buildViewContext

	/**
	 * Build the context for the new user mode.
	 */
	private String buildConfirmRemoveContext(SessionState state, Context context)
	{
		// get the user to edit
		UserEdit user = (UserEdit) state.getAttribute("user");
		context.put("user", user);

		return "_confirm_remove";

	} // buildConfirmRemoveContext

	/**
	 * doNew called when "eventSubmit_doNew" is in the request parameters to add a new user
	 */
	public void doNew(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		state.setAttribute("mode", "new");

		// mark the user as new, so on cancel it can be deleted
		state.setAttribute("new", "true");

		// disable auto-updates while not in list mode
		disableObservers(state);

	} // doNew

	/**
	 * doEdit called when "eventSubmit_doEdit" is in the request parameters to edit a user
	 */
	public void doEdit(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");
		state.removeAttribute("user");
		state.removeAttribute("newuser");

		// get the user
		try
		{
			UserEdit user = UserDirectoryService.editUser(id);
			state.setAttribute("user", user);
			state.setAttribute("mode", "edit");

			// disable auto-updates while not in list mode
			disableObservers(state);
		}
		catch (IdUnusedException e)
		{
			Log.warn("chef", "UsersAction.doEdit: user not found: " + id);

			addAlert(state, rb.getString("useact.use") + " " + id + " " + rb.getString("useact.notfou"));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("useact.youdonot1") + " " + id);
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (InUseException e)
		{
			addAlert(state, rb.getString("useact.somone") + " " + id);
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

	} // doEdit

	/**
	 * doModify called when "eventSubmit_doModify" is in the request parameters to edit a user
	 */
	public void doModify(RunData data, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled())
		{
			Log.debug("chef", this + ".doModify");
		}

		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());
		String id = data.getParameters().getString("id");
		state.removeAttribute("user");
		state.removeAttribute("newuser");

		// get the user
		try
		{
			UserEdit user = UserDirectoryService.editUser(id);
			state.setAttribute("user", user);
			state.setAttribute("mode", "edit");

			// disable auto-updates while not in list mode
			disableObservers(state);
		}
		catch (IdUnusedException e)
		{
			Log.warn("chef", "UsersAction.doEdit: user not found: " + id);

			addAlert(state, rb.getString("useact.use") + " " + id + " " + rb.getString("useact.notfou"));
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("useact.youdonot1") + " " + id);
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}
		catch (InUseException e)
		{
			addAlert(state, rb.getString("useact.somone") + " " + id);
			state.removeAttribute("mode");

			// make sure auto-updates are enabled
			enableObserver(state);
		}

	} // doModify

	/**
	 * doSave called when "eventSubmit_doSave" is in the request parameters to save user edits
	 */
	public void doSave(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readUserForm(data, state)) return;

		// commit the change
		UserEdit edit = (UserEdit) state.getAttribute("user");
		if (edit != null)
		{
			try
			{
				UserDirectoryService.commitEdit(edit);
			}
			catch (IdUsedException e)
			{
				// TODO: this means the EID value is not unique... when we implement EID fully, we need to check this and send it back to the user
				Log.warn("chef", "UsersAction.doSave()" + e);
			}
		}

		User user = edit;
		if (user == null)
		{
			user = (User) state.getAttribute("newuser");
		}

		// cleanup
		state.removeAttribute("user");
		state.removeAttribute("newuser");
		state.removeAttribute("new");

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

		if ((user != null) && ((Boolean) state.getAttribute("create-login")).booleanValue())
		{
			try
			{
				// login - use the fact that we just created the account as external evidence
				Evidence e = new ExternalTrustedEvidence(user.getId());
				Authentication a = AuthenticationManager.authenticate(e);
				if (!LoginUtil.login(a, (HttpServletRequest) CurrentService.getInThread(RequestFilter.CURRENT_HTTP_REQUEST)))
				{
					addAlert(state, rb.getString("useact.tryloginagain"));
				}
			}
			catch (AuthenticationException ex)
			{
				Log.warn("chef", "UsersAction.doSave: authentication failure: " + ex);
			}

			// redirect to home (on next build)
			state.setAttribute("redirect", "");
		}

	} // doSave

	/**
	 * doCancel called when "eventSubmit_doCancel" is in the request parameters to cancel user edits
	 */
	public void doCancel(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the user
		UserEdit user = (UserEdit) state.getAttribute("user");
		if (user != null)
		{
			// if this was a new, delete the user
			if ("true".equals(state.getAttribute("new")))
			{
				// remove
				try
				{
					UserDirectoryService.removeUser(user);
				}
				catch (PermissionException e)
				{
					addAlert(state, rb.getString("useact.youdonot2") + " " + user.getId());
				}
			}
			else
			{
				UserDirectoryService.cancelEdit(user);
			}
		}

		// cleanup
		state.removeAttribute("user");
		state.removeAttribute("newuser");
		state.removeAttribute("new");

		// return to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

	} // doCancel

	/**
	 * doRemove called when "eventSubmit_doRemove" is in the request par ameters to confirm removal of the user
	 */
	public void doRemove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// read the form - if rejected, leave things as they are
		if (!readUserForm(data, state)) return;

		// go to remove confirm mode
		state.setAttribute("mode", "confirm");

	} // doRemove

	/**
	 * doRemove_confirmed called when "eventSubmit_doRemove_confirmed" is in the request parameters to remove the user
	 */
	public void doRemove_confirmed(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// get the user
		UserEdit user = (UserEdit) state.getAttribute("user");

		// remove
		try
		{
			UserDirectoryService.removeUser(user);
		}
		catch (PermissionException e)
		{
			addAlert(state, rb.getString("useact.youdonot2") + " " + user.getId());
		}

		// cleanup
		state.removeAttribute("user");
		state.removeAttribute("newuser");
		state.removeAttribute("new");

		// go to main mode
		state.removeAttribute("mode");

		// make sure auto-updates are enabled
		enableObserver(state);

	} // doRemove_confirmed

	/**
	 * doCancel_remove called when "eventSubmit_doCancel_remove" is in the request parameters to cancel user removal
	 */
	public void doCancel_remove(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		// return to edit mode
		state.setAttribute("mode", "edit");

	} // doCancel_remove

	/**
	 * Read the user form and update the user in state.
	 * 
	 * @return true if the form is accepted, false if there's a validation error (an alertMessage will be set)
	 */
	private boolean readUserForm(RunData data, SessionState state)
	{
		//boolean parameters and values
		// --------------Mode--singleUser-createUser-typeEnable
		// Admin New-----new---false------false------true
		// Admin Update--edit--false------false------true
		// Gateway New---null---false------true-------false
		// Account Edit--edit--true-------false------false
		
		// read the form
		String id = StringUtil.trimToNull(data.getParameters().getString("id"));
		String firstName = StringUtil.trimToNull(data.getParameters().getString("first-name"));
		String lastName = StringUtil.trimToNull(data.getParameters().getString("last-name"));
		String email = StringUtil.trimToNull(data.getParameters().getString("email"));
		String pw = StringUtil.trimToNull(data.getParameters().getString("pw"));

		String mode = (String) state.getAttribute("mode");
		boolean singleUser = ((Boolean) state.getAttribute("single-user")).booleanValue();
		boolean createUser = ((Boolean) state.getAttribute("create-user")).booleanValue();
		
		// if in Gateway New Account tool, password is required
		if (createUser)
		{
			if (pw == null)
			{
				addAlert(state, rb.getString("usecre.pasismis"));
				return false;
			}
		}
		
		boolean typeEnable = false;
		String type = null;
		if ((mode != null) && (mode.equalsIgnoreCase("new")))
		{
			typeEnable = true;
		}
		else if ((mode != null) && (mode.equalsIgnoreCase("edit")) && (!singleUser))
		{
			typeEnable = true;
		}

		if (typeEnable)
		{
			// for the case of Admin User tool creating new user
			type = StringUtil.trimToNull(data.getParameters().getString("type"));
		}
		else
		{
			if (createUser)
			{
				// for the case of Gateway Account tool creating new user
				type = (String) state.getAttribute("create-type");
			}
		}

		// get the user
		UserEdit user = (UserEdit) state.getAttribute("user");

		// add if needed
		if (user == null)
		{
			try
			{
				// add the user in one step so that all you need is add not update permission
				// (the added might be "anon", and anon has add but not update permission)
				User newUser = UserDirectoryService.addUser(id, firstName, lastName, email, pw, type, null);

				// put the user in the state
				state.setAttribute("newuser", newUser);
			}
			catch (IdUsedException e)
			{
				addAlert(state, rb.getString("useact.theuseid1"));
				return false;
			}
			catch (IdInvalidException e)
			{
				addAlert(state, rb.getString("useact.theuseid2"));
				return false;
			}
			catch (PermissionException e)
			{
				addAlert(state, rb.getString("useact.youdonot3"));
				return false;
			}
		}

		// update
		else
		{
			user.setFirstName(firstName);
			user.setLastName(lastName);
			user.setEmail(email);
			if (pw != null) user.setPassword(pw);
			if (type != null) user.setType(type);
		}

		return true;

	} // readUserForm

} // UsersAction



