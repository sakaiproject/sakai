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
import java.util.List;
import java.util.Map;
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
	private static String STATE_SUCCESS = "lti:state_success";
	
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
	}
	
	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, 
		RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
		        addAlert(state,"Must be site administrator");
		        return "lti_main";
		}
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		List<Map<String,Object>> tools = ltiService.getTools(null,null,0,100);
		context.put("tools", tools);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_main";
	}
	
        public String buildToolViewPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
		        addAlert(state,"Must be site administrator");
		        return "lti_main";
		}
                context.put("doToolEdit", BUTTON + "doToolEdit");
                context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel();
		String id = data.getParameters().getString("id");
		System.out.println("id="+id);
		Long key = new Long(id);
		Map<String,Object> tool = ltiService.getTool(key);
		if (  tool == null ) return "lti_main";		
		String formOutput = ltiService.formOutput(tool, mappingForm);
		context.put("formOutput", formOutput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_view";
	}
	
	public String buildToolEditPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
		        addAlert(state,"Must be site maintainer");
		        return "lti_main";
		}
                context.put("doToolAction", BUTTON + "doToolEdit");
                context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel();
		String id = data.getParameters().getString("id");
		System.out.println("id="+id);
		Long key = new Long(id);
		Map<String,Object> tool = ltiService.getTool(key);
		if (  tool == null ) return "lti_main";		
		String formInput = ltiService.formInput(tool, mappingForm);
		context.put("formInput", formInput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_insert";
	}

	public String buildToolInsertPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
		        addAlert(state,"Must be site maintainer");
		        return "lti_main";
		}
                context.put("doToolAction", BUTTON + "doToolInsert");
                context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel();
		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		String formInput = ltiService.formInput(previousPost, mappingForm);
		context.put("formInput",formInput);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_insert";
	}

	public void doToolInsert(RunData data, Context context)
	{

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		
		if ( ! ltiService.isMaintain() ) {
		        addAlert(state,"Must be site maintainer");
		        return;
		}
		// String setting = data.getParameters().getString("setting");
		Properties reqProps = data.getParameters().getProperties();
		Object retval = ltiService.insertTool(reqProps);
		if ( retval instanceof String ) 
		{
	                state.setAttribute(STATE_POST,reqProps);
			addAlert(state, (String) retval);
			return;
		}
		System.out.println("Yahoo="+((Long)retval).toString());
		state.setAttribute(STATE_SUCCESS,"Added");
		switchPanel(state, "Main");
	}

	/**
	 * Setup the velocity context and choose the template for options.
	 */
	public String buildMappingPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isAdmin() ) {
		        addAlert(state,"Must be site administrator");
		        return "lti_main";
		}
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		return "lti_mapping";
	}

	public String buildMappingInsertPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isAdmin() ) {
		        addAlert(state,"Must be site administrator");
		        return "lti_main";
		}
                context.put("doMappingInsert", BUTTON + "doMappingInsert");
                context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		String [] mappingForm = ltiService.getMappingModel();
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

		if ( ! ltiService.isAdmin() ) {
		        addAlert(state,"Must be site administrator");
		        return;
		}

		// String setting = data.getParameters().getString("setting");
		Properties reqProps = data.getParameters().getProperties();
		Object retval = ltiService.insertMapping(reqProps);
		if ( retval instanceof String ) 
		{
   		        state.setAttribute(STATE_POST,reqProps);
			addAlert(state, (String) retval);
			return;
		}
		System.out.println("Yahoo="+((Long)retval).toString());
		state.setAttribute(STATE_SUCCESS,"Added");
		switchPanel(state, "Mapping");
	}

}
