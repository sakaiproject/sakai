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
import java.util.HashMap;

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

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.impl.DBLTIService; // HACK

import org.sakaiproject.util.foorm.SakaiFoorm;

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
	
	private static String STATE_POST = "lti:state_post";
	
	/** Service Implementations */
	protected static ToolManager toolManager = null; 
	protected static LTIService ltiService = null; 

	protected static SakaiFoorm foorm = new SakaiFoorm();

	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices()
	{
		if ( toolManager == null ) toolManager = (ToolManager) ComponentManager.get("org.sakaiproject.tool.api.ToolManager");
		// HACK if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		/* HACK to save many restarts during development */ 
		if ( ltiService == null ) { 
			ltiService = (LTIService) new DBLTIService(); 
			((org.sakaiproject.lti.impl.DBLTIService) ltiService).init(); 
		} 
	}

	// HACK
	protected ResourceLoader getLTILoader()
	{
		if ( ltiService instanceof org.sakaiproject.lti.impl.DBLTIService)
		{
			return ((org.sakaiproject.lti.impl.DBLTIService) ltiService).getResourceLoader();
		}
		return null;
	}

	/**
	 * Populate the state with configuration settings
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		getServices();

		// Not currently needed
		// Placement placement = toolManager.getCurrentPlacement();
		// Properties config = placement.getConfig();
	}
	
	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, 
		RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// TOTO: Retrieve tools here
		state.removeAttribute(STATE_POST);
		return "lti_main";
	}

	public String buildToolInsertPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
                context.put("doToolInsert", BUTTON + "doToolInsert");
		String [] mappingForm = LTIService.ADMIN_TOOL_MODEL;
		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		String formInput = foorm.formInput(previousPost, mappingForm, getLTILoader());
		context.put("formInput",formInput);
		state.removeAttribute(STATE_POST);
		return "lti_tool_insert";
	}

	public void doToolInsert(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// String setting = data.getParameters().getString("setting");
		Properties reqProps = data.getParameters().getProperties();
		String errors = ltiService.insertTool(reqProps);
		if ( errors != null ) 
		{
		    state.setAttribute(STATE_POST,reqProps);
			addAlert(state, errors);
			return;
		}
		switchPanel(state, "Main");
	}

	/**
	 * Setup the velocity context and choose the template for options.
	 */
	public String buildMappingPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		return "lti_mapping";
	}

	public String buildMappingInsertPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
                context.put("doMappingInsert", BUTTON + "doMappingInsert");
		String [] mappingForm = LTIService.ADMIN_MAPPING_MODEL;
		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		String formInput = foorm.formInput(previousPost, mappingForm, getLTILoader());
		context.put("formInput",formInput);
		return "lti_mapping_insert";
	}

	/**
	 * Save Mapping
	 */
	public void doMappingInsert(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// String setting = data.getParameters().getString("setting");
		Properties reqProps = data.getParameters().getProperties();
		String errors = ltiService.insertMapping(reqProps);
		if ( errors != null ) 
		{
   		    state.setAttribute(STATE_POST,reqProps);
			addAlert(state, errors);
			return;
		}
		switchPanel(state, "Mapping");
	}

}
