/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.memory.tool;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.memory.cover.MemoryServiceLocator;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * MemoryAction is the Sakai memory tool.
 * </p>
 */
@SuppressWarnings("serial")
public class MemoryAction extends VelocityPortletPaneledAction
{

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("memory");
	/** Kernel api **/
	private SecurityService securityService;
	
	
	public MemoryAction() {
		super();
		securityService = ComponentManager.get(SecurityService.class);
	}
	/**
	 * build the context
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		
		// if not logged in as the super user, we won't do anything
		if (!securityService.isSuperUser())
		{
			return (String) getContext(rundata).get("template") + "_noaccess";
		}

		// put $action into context for menus, forms and links
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		// put the current available memory into the context
		context.put("memory", Long.toString(MemoryServiceLocator.getInstance()
				.getAvailableMemory()));

		// status, if there
		if (state.getAttribute("status") != null)
		{
			context.put("status", state.getAttribute("status"));
			state.removeAttribute("status");
		}

		return (String) getContext(rundata).get("template");

	} // buildMainPanelContext

	/**
	 * doNew called when "eventSubmit_doReset" is in the request parameters to reset memory useage (caches)
	 */
	public void doReset(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		try
		{
			MemoryServiceLocator.getInstance().resetCachers();
		}
		catch (SecurityException e)
		{
			state.setAttribute("message", rb.getString("memory.notpermis"));
		}

	} // doReset
	
	
	/**
	 * doNew called when "eventSubmit_doReset" is in the request parameters to reset memory useage (caches)
	 */
	public void doEvict(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		try
		{
			MemoryServiceLocator.getInstance().evictExpiredMembers();
		}
		catch (SecurityException e)
		{
			state.setAttribute("message", rb.getString("memory.notpermis"));
		}

	} // doReset

	/**
	 * doNew called when "eventSubmit_doStatus" is in the request parameters to reset memory useage (caches)
	 */
	public void doStatus(RunData data, Context context)
	{
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(((JetspeedRunData) data).getJs_peid());

		state.setAttribute("status", MemoryServiceLocator.getInstance()
				.getStatus());

	} // doReset

} // MemoryAction

