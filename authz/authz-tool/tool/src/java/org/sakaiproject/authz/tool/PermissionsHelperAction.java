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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * This is a helper interface to the Permissions tool.
 */
public class PermissionsHelperAction extends VelocityPortletPaneledAction
{
	private static ResourceLoader rb = new ResourceLoader("authz-tool");

	private static final String STARTED = "sakaiproject.permissions.started";

	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		SessionState sstate = getState(req);
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		String mode = (String) sstate.getAttribute(PermissionsAction.STATE_MODE);
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
		String mode = (String) sstate.getAttribute(PermissionsAction.STATE_MODE);

		if (mode == null)
		{
			initHelper(portlet, context, rundata, sstate);
		}

		String template = PermissionsAction.buildHelperContext(portlet, context, rundata, sstate);
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
		state.setAttribute(PermissionsAction.STATE_REALM_ID, targetRef);
		
		// use the roles from this ref's AuthzGroup
		state.setAttribute(PermissionsAction.STATE_REALM_ROLES_ID, rolesRef);

		// ... with this description
		state.setAttribute(PermissionsAction.STATE_DESCRIPTION, description);

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsAction.STATE_PREFIX, prefix);

		// start the helper
		state.setAttribute(PermissionsAction.STATE_MODE, PermissionsAction.MODE_MAIN);
	}
}
