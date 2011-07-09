/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
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

package org.sakaiproject.blti.tool;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.component.cover.ComponentManager;
// import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * <p>
 * LTIAdminTool is a Simple Velocity-based Tool
 * </p>
 */
public class LTIAdminTool extends VelocityPortletPaneledAction
{
	private static Log M_log = LogFactory.getLog(LTIAdminTool.class);
	
	/** Resource bundle using current language locale */
	protected static ResourceLoader rb = new ResourceLoader("sample");

	/** Service Implementations */
	protected static ToolManager toolManager = null; 

	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices()
	{
		if ( toolManager == null ) toolManager = (ToolManager) ComponentManager.get("org.sakaiproject.tool.api.ToolManager");
	}

	/**
	 * Populate the state with configuration settings
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		getServices();

		Placement placement = toolManager.getCurrentPlacement();
		Properties config = placement.getConfig();

		// Get  value from tool registration in /src/webapp/tools/sakai.velocity.sample.xml
		String configValue = config.getProperty("key", "not found");
		System.out.println("initState config key="+configValue);
	}
	
	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, 
		RunData rundata, SessionState state)
	{
		// set the resource bundle with our strings
		context.put("tlang", rb);
		context.put("framework", "Velocity");
		return "sample_main";
	}

	/**
	 * Setup the velocity context and choose the template for options.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
                context.put("doSave", BUTTON + "doSave");
		
		// Put the old value for setting from the placement
		Placement placement = toolManager.getCurrentPlacement();
		context.put("setting", placement.getPlacementConfig().getProperty("setting"));

		return "sample_options";
	}

	/**
	 * Save Options
	 */
	public void doSave(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String setting = data.getParameters().getString("setting");

		// Lets store our setting in the tool placement - a great place
		// to stash little things that are per placement (i.e. not per user)
		try
		{
			Placement placement = toolManager.getCurrentPlacement();
			placement.getPlacementConfig().setProperty("setting", setting);
			placement.save();
			addAlert(state,rb.getString("gen.success"));
		} catch(Exception e) {
			addAlert(state,rb.getString("gen.faulire"));
		}

	}

}
