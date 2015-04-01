/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.presence.tool;

import java.util.*;

import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuDivider;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.cluster.api.ClusterNode;
import org.sakaiproject.cluster.api.ClusterService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.presence.cover.PresenceService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.PresenceObservingCourier;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * PresenceToolAction is the presence display tool showing everyone everywhere.
 * </p>
 */
public class PresenceToolAction extends VelocityPortletPaneledAction
{
	private static final long serialVersionUID = 1L;

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("admin");

	/** The display modes. */
	protected static final String STATE_DISPLAY_MODE = "display_mode";

	protected static final String MODE_LOCATIONS = "locations";

	protected static final String MODE_SESSIONS = "sessions";

	protected static final String MODE_SERVERS = "servers";

    protected ClusterService clusterService;

	public PresenceToolAction() {
		clusterService = (ClusterService) ComponentManager.get(ClusterService.class);
	}
	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		// setup the observer to notify our main panel
		if (state.getAttribute(STATE_OBSERVER) == null)
		{
			// get the current tool placement
			Placement placement = ToolManager.getCurrentPlacement();

			// location is just placement
			String location = placement.getId();

			// setup the observer to watch for all presence, disabled so we start in manual mode
			PresenceObservingCourier courier = new PresenceObservingCourier(location);
			courier.setResourcePattern(null);
			courier.disable();
			state.setAttribute(STATE_OBSERVER, courier);

			// init the display mode
			state.setAttribute(STATE_DISPLAY_MODE, MODE_SERVERS);
		}

	} // initState

	/**
	 * build the context for the Main (List) panel
	 * 
	 * @return (optional) template name for this panel
	 */
	@SuppressWarnings("unchecked")
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);

		// if not logged in as the super user, we won't do anything
		if (!SecurityService.isSuperUser())
		{
			return (String) getContext(rundata).get("template") + "_noaccess";
		}

		String template = (String) getContext(rundata).get("template");

		if (!SecurityService.isSuperUser())
		{
			addAlert(state, rb.getString("presence.import"));
			return template;
		}

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		PresenceObservingCourier observer = (PresenceObservingCourier) state.getAttribute(STATE_OBSERVER);
		observer.justDelivered();

		// build the menu
		Menu bar = new MenuImpl();
		bar.add(new MenuEntry(rb.getString("presence.locations"), "doLocations"));
		bar.add(new MenuEntry(rb.getString("presence.sessions"), "doSessions"));
		bar.add(new MenuEntry(rb.getString("presence.servers"), "doServers"));
		bar.add(new MenuDivider());
		bar.add(new MenuEntry((observer.getEnabled() ? rb.getString("presence.manualref") : rb.getString("presence.autoref")),
				"doAuto"));
		if (!observer.getEnabled())
		{
			bar.add(new MenuEntry(rb.getString("presence.ref"), "doRefresh"));
		}
		context.put(Menu.CONTEXT_MENU, bar);

		// for locations list mode
		if (MODE_LOCATIONS.equals(state.getAttribute(STATE_DISPLAY_MODE)))
		{
			template += "-List";

			// get the list of all presence locations
			List<String> locations = PresenceService.getLocations();
			context.put("locations", locations);

			context.put("service", PresenceService.getInstance());
		}

		// for sessions display mode
		else if (MODE_SESSIONS.equals(state.getAttribute(STATE_DISPLAY_MODE)))
		{
			template += ".sessions-List";

			// get sessions by server (keys are already sorted by server)
			Map<String,List<UsageSession>> sessionsByServer = UsageSessionService.getOpenSessionsByServer();
			context.put("servers", sessionsByServer);

			List<String> serverList = new Vector<String>();
			serverList.addAll(sessionsByServer.keySet());
			context.put("serverList", serverList);

			int count = 0;
			for (List<UsageSession> sessions : sessionsByServer.values())
			{
				count += sessions.size();
			}
			context.put("total", Integer.valueOf(count));
		}

		// for servers display mode
		else if (MODE_SERVERS.equals(state.getAttribute(STATE_DISPLAY_MODE)))
		{
			template += ".servers-List";

			// get the set of all servers with current presence
			Map<String,List<UsageSession>> session = UsageSessionService.getOpenSessionsByServer();
			context.put("serverSessions", session);

			Map<String, ClusterNode>nodes = clusterService.getServerStatus();
			// Get a map of statuses
			Map<String, ClusterService.Status> status = new HashMap<>();
			for (Map.Entry<String, ClusterNode> entry: nodes.entrySet()) {
				status.put(entry.getKey(), entry.getValue().getStatus());
			}

			context.put("serverStatus", status);

			Set<String> serverList = new TreeSet<String>();
			serverList.addAll(nodes.keySet());
			serverList.addAll(session.keySet());
			context.put("serverList", serverList);

			int count = 0;
			for (List<UsageSession> sessions : session.values())
			{
				count += sessions.size();
			}
			context.put("total", Integer.valueOf(count));
		}

		// the url for the online courier, using a 30 second refresh
		setVmCourier(rundata.getRequest(), 30);

		return template;

	} // buildMainPanelContext

	/**
	 * Switch to locations mode
	 */
	public void doLocations(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// go to remove confirm mode
		state.setAttribute(STATE_DISPLAY_MODE, MODE_LOCATIONS);

	} // doLocations

	/**
	 * Switch to sessions mode
	 */
	public void doSessions(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// go to remove confirm mode
		state.setAttribute(STATE_DISPLAY_MODE, MODE_SESSIONS);

	} // doSessions

	/**
	 * Switch to servers mode
	 */
	public void doServers(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// go to remove confirm mode
		state.setAttribute(STATE_DISPLAY_MODE, MODE_SERVERS);

	} // doServers

	/**
	 * Toggle auto-update
	 */
	public void doAuto(RunData data, Context context)
	{
		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// get the observer
		PresenceObservingCourier observer = (PresenceObservingCourier) state.getAttribute(STATE_OBSERVER);
		boolean enabled = observer.getEnabled();
		if (enabled)
		{
			observer.disable();
		}
		else
		{
			observer.enable();
		}

	} // doAuto

	public void doSwitch(RunData data, Context context)
	{
		// We look at the status in the request so that if someone else has changed the status
		// of a node we don't switch it back.
		String id = data.getParameters().getString("server_id");
		String status = data.getParameters().getString("status");
		clusterService.markClosing(id, !ClusterService.Status.CLOSING.toString().equals(status));
	}

	/**
	 * The action for when the user want's an update
	 */
	public void doRefresh(RunData data, Context context)
	{

	} // doRefresh

} // PresenceToolAction

