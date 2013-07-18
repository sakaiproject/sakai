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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.blti.tool;

import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.HashSet;
import java.util.Properties;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.component.api.ServerConfigurationService;

// TODO: FIX THIS
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
// import org.sakaiproject.lti.impl.DBLTIService; // HACK

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
	protected static ResourceLoader rb = new ResourceLoader("ltitool");

	private boolean inHelper = false;

	private static String STATE_POST = "lti:state_post";
	private static String STATE_SUCCESS = "lti:state_success";
	private static String STATE_ID = "lti:state_id";
	private static String STATE_TOOL_ID = "lti:state_tool_id";
	private static String STATE_CONTENT_ID = "lti:state_content_id";
	private static String STATE_REDIRECT_URL = "lti:state_redirect_url";

	private static String SECRET_HIDDEN = "***************";
	
	private static String ALLOW_MAINTAINER_ADD_SYSTEM_TOOL = "lti:allow_maintainer_add_system_tool";

	/** Service Implementations */
	protected static ToolManager toolManager = null; 
	protected static LTIService ltiService = null;
	protected static ServerConfigurationService serverConfigurationService = null;

	protected static SakaiFoorm foorm = new SakaiFoorm();

	/**
	 * Pull in any necessary services using factory pattern
	 */
	protected void getServices()
	{
		if ( toolManager == null ) toolManager = (ToolManager) ComponentManager.get("org.sakaiproject.tool.api.ToolManager");

		/* HACK to save many restarts during development
		   if ( ltiService == null ) { 
		   ltiService = (LTIService) new DBLTIService(); 
		   ((org.sakaiproject.lti.impl.DBLTIService) ltiService).setAutoDdl("true"); 
		   ((org.sakaiproject.lti.impl.DBLTIService) ltiService).init(); 
		   } 
		   End of HACK */

		if ( ltiService == null ) ltiService = (LTIService) ComponentManager.get("org.sakaiproject.lti.api.LTIService");
		if ( serverConfigurationService == null ) serverConfigurationService = (ServerConfigurationService) ComponentManager.get("org.sakaiproject.component.api.ServerConfigurationService");
	}

	/**
	 * Populate the state with configuration settings
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		getServices();

		Placement placement = toolManager.getCurrentPlacement();
		String toolReg = placement.getToolId();
		inHelper = ! ( "sakai.basiclti.admin".equals(toolReg));
		
		
	}

	/**
	 * Setup the velocity context and choose the template for the response.
	 */
	public String buildErrorPanelContext(VelocityPortlet portlet, Context context, 
			RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		state.removeAttribute(STATE_ID);
		state.removeAttribute(STATE_TOOL_ID);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		state.removeAttribute(STATE_REDIRECT_URL);
		return "lti_error";
	}

	public String buildMainPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		// default to site view
		return buildToolSitePanelContext(portlet, context, data, state);
	}
	
	public String buildToolSitePanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		String returnUrl = data.getParameters().getString("returnUrl");
		// if ( returnUrl != null ) state.setAttribute(STATE_REDIRECT_URL, returnUrl);
		context.put("ltiService", ltiService);
		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		context.put("inHelper",new Boolean(inHelper));
		context.put("getContext",toolManager.getCurrentPlacement().getContext());
		context.put("doEndHelper", BUTTON + "doEndHelper");
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);

		// this is for the "site tools" panel
		List<Map<String,Object>> contents = ltiService.getContents(null,null,0,5000);
		for ( Map<String,Object> content : contents ) {
			
			Long tool_id_long = null;
			try{
				tool_id_long = new Long(content.get("tool_id").toString());
			}
			catch (Exception e)
			{
				// log the error
				M_log.error("error parsing tool id " + content.get("tool_id"));
			}
			content.put("tool_id_long", tool_id_long);
			String plstr = (String) content.get(LTIService.LTI_PLACEMENT);
			ToolConfiguration tool = SiteService.findTool(plstr);
			if ( tool == null ) {
				content.put(LTIService.LTI_PLACEMENT, null);
			}
		}
		context.put("contents", contents);
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		context.put("getContext",toolManager.getCurrentPlacement().getContext());
		
		// top navigation menu
		Menu menu = new MenuImpl(portlet, data, "LTIAdminTool");
		menu.add(new MenuEntry(rb.getString("tool.in.site"), false, "doNav_tool_site"));
		menu.add(new MenuEntry(rb.getString("tool.in.system"), true, "doNav_tool_system"));
		context.put("menu", menu);
		
		return "lti_tool_site";
	}
	
	public String buildToolSystemPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		String contextString = toolManager.getCurrentPlacement().getContext();
		String returnUrl = data.getParameters().getString("returnUrl");
		// if ( returnUrl != null ) state.setAttribute(STATE_REDIRECT_URL, returnUrl);
		context.put("ltiService", ltiService);
		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		context.put("inHelper",new Boolean(inHelper));
		context.put("doEndHelper", BUTTON + "doEndHelper");
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);

		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		// by default, site maintainer can add system-wide LTI tool
		context.put("allowMaintainerAddSystemTool", new Boolean(serverConfigurationService.getBoolean(ALLOW_MAINTAINER_ADD_SYSTEM_TOOL, true)));
		context.put("getContext", contextString);
		
		// this is for the system tool panel
		List<Map<String,Object>> contents = ltiService.getContents(null,null,0,5000);
		List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0);
		if (tools != null && !tools.isEmpty())
		{
			List<Map<String,Object>> siteLtiTools = new ArrayList<Map<String, Object>>();
			List<Map<String,Object>> systemLtiTools = new ArrayList<Map<String, Object>>();
			HashMap<String, Map<String, Object>> systemLtiToolsMap = new HashMap<String, Map<String, Object>>();
			for(Map<String, Object> tool:tools)
			{
				if (!tool.containsKey(ltiService.LTI_SITE_ID) || StringUtils.trimToNull((String) tool.get(ltiService.LTI_SITE_ID)) == null)
				{
					systemLtiTools.add(tool);
				}
				else if (((String) tool.get(ltiService.LTI_SITE_ID)).equals(contextString))
				{
					// if the current user is admin, add the site-range tool;
					// otherwise, add the site-range tool only if the tool's site_id is the same as current site
					systemLtiTools.add(tool);
				}
				else if (ltiService.isAdmin())
				{
					// show all the tools inside Admin MyWorkspace site
					systemLtiTools.add(tool);
				}
			}
			// get invoke count for all lti tools
			HashMap<String, List<String>> ltiToolsCount = getLtiToolUsageCount(contents);
			for (Map<String, Object> toolMap : systemLtiTools ) {
				String ltiToolId = toolMap.get(ltiService.LTI_ID).toString();
				List<String> toolSite = ltiToolsCount.containsKey(ltiToolId)?ltiToolsCount.get(ltiToolId):new ArrayList<String>();
				Set<String> toolUniqueSite = new HashSet<String>();
				toolUniqueSite.addAll(toolSite);
				toolMap.put("tool_count", toolSite.size());
				toolMap.put("tool_unique_site_count", toolUniqueSite.size());
				systemLtiToolsMap.put(ltiToolId, toolMap);
			}
			context.put("systemLtiToolsMap", systemLtiToolsMap);
			context.put("siteLtiTools", siteLtiTools);
		}
		
		// top navigation menu
		Menu menu = new MenuImpl(portlet, data, "LTIAdminTool");
		menu.add(new MenuEntry(rb.getString("tool.in.site"), true, "doNav_tool_site"));
		menu.add(new MenuEntry(rb.getString("tool.in.system"), false, "doNav_tool_system"));
		context.put("menu", menu);
		
		return "lti_tool_system";
	}
	
	/**
	 * iterator through the whole system and find out the lti tool usages pattern, e.g. site count,etc
	 * @param contents
	 * @return
	 */
	private HashMap<String, List<String>> getLtiToolUsageCount(
			List<Map<String, Object>> contents) {
		HashMap<String, List<String>> ltiToolsCount = new HashMap<String, List<String>> ();
		for ( Map<String,Object> content : contents ) {
			String ltiToolId = content.get(ltiService.LTI_TOOL_ID).toString();
			String siteId = StringUtils.trimToNull((String) content.get(ltiService.LTI_SITE_ID));
			if (siteId != null)
			{
				if (ltiToolsCount.containsKey(ltiToolId))
				{
					List<String> siteIds = ltiToolsCount.get(ltiToolId);
					siteIds.add(siteId);
					ltiToolsCount.put(ltiToolId, siteIds);
				}
				else
				{
					// new entry
					List<String> siteIds = new ArrayList<String>();
					siteIds.add(siteId);
					ltiToolsCount.put(ltiToolId, siteIds);
				}
			}
		}
		return ltiToolsCount;
	}

	public void doEndHelper(RunData data, Context context)
	{
		// Request a shortcut transfer back to the tool we are helping
		// This working depends on SAK-20898 
		SessionManager.getCurrentToolSession().setAttribute(HELPER_LINK_MODE, HELPER_MODE_DONE);

		// In case the above fails...
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		switchPanel(state, "Main");
	}

	public String buildToolViewPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.view"));
			return "lti_error";
		}
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}
		Long key = new Long(id);
		Map<String,Object> tool = ltiService.getTool(key);
		if (  tool == null ) return "lti_main";	
		tool.put(LTIService.LTI_SECRET,SECRET_HIDDEN);
		tool.put(LTIService.LTI_CONSUMERKEY,SECRET_HIDDEN);
		String formOutput = ltiService.formOutput(tool, mappingForm);
		context.put("formOutput", formOutput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_view";
	}

	public String buildToolEditPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		String stateId = (String) state.getAttribute(STATE_ID);
		state.removeAttribute(STATE_ID);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) id = stateId;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}		
		Long key = new Long(id);
		Map<String,Object> tool = ltiService.getTool(key);
		if (  tool == null ) return "lti_main";

		// Hide the old tool secret unless it is incomplete
		if ( ! LTIService.LTI_SECRET_INCOMPLETE.equals(tool.get(LTIService.LTI_SECRET)) ) {
			tool.put(LTIService.LTI_SECRET,SECRET_HIDDEN);		
		}
		String formInput = ltiService.formInput(tool, mappingForm);
		context.put("formInput", formInput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_insert";
	}

	public String buildToolDeletePanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolDelete");
		String [] mappingForm = foorm.filterForm(ltiService.getToolModel(), "^title:.*|^launch:.*|^id:.*", null);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}	
		Long key = new Long(id);
		Map<String,Object> tool = ltiService.getTool(key);
		if (  tool == null ) {
			addAlert(state,rb.getString("error.tool.not.found"));
			return "lti_main";
		}
		String formOutput = ltiService.formOutput(tool, mappingForm);
		context.put("formOutput", formOutput);
		context.put("tool",tool);
		
		// get the tool usage count
		HashMap<String, List<String>> ltiToolsCount = getLtiToolUsageCount(ltiService.getContents(null,null,0,5000));
		List<String> toolSite = ltiToolsCount.containsKey(id)?ltiToolsCount.get(id):new ArrayList<String>();
		Set<String> toolUniqueSite = new HashSet<String>();
		toolUniqueSite.addAll(toolSite);
		context.put("tool_count", toolSite.size());
		context.put("tool_unique_site_count", toolUniqueSite.size());
		
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_delete";
	}

	public void doToolDelete(RunData data, Context context)
	{

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			switchPanel(state, "Main");
			return;
		}
		Long key = new Long(id);
		if ( ltiService.deleteTool(key) )
		{
			state.setAttribute(STATE_SUCCESS,rb.getString("success.deleted"));
			
			// remove all content object and site links if any
			// this is for the "site tools" panel
			List<Map<String,Object>> contents = ltiService.getContents(null,null,0,5000);
			for ( Map<String,Object> content : contents ) {
				
				Long tool_id_long = null;
				try{
					tool_id_long = new Long(content.get("tool_id").toString());
					if (tool_id_long.equals(key))
					{
						// the content with same tool id
						// remove the content link first
						String content_id = content.get(LTIService.LTI_ID).toString();
						Long content_key = content_id == null ? null:new Long(content_id);
						
						//TODO: how to handle the errors in content link and content deletion?
						// remove the external tool content site link
						ltiService.deleteContentLink(content_key);
						// remove the external tool content
						ltiService.deleteContent(content_key);
					}
				}
				catch (Exception e)
				{
					// log the error
					M_log.error("error parsing tool id " + content.get("tool_id"));
				}
			}
			
			switchPanel(state, "Main");
		} else {
			addAlert(state,rb.getString("error.delete.fail"));
			switchPanel(state, "Main");
		}
	}

	public String buildToolInsertPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.edit.maintain"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doToolPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getToolModel();
		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		String formInput = ltiService.formInput(previousPost, mappingForm);
		context.put("formInput",formInput);
		state.removeAttribute(STATE_POST);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_tool_insert";
	}

	// Insert or edit
	public void doToolPut(RunData data, Context context)
	{

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			switchPanel(state,"Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		String success = null;

		String newSecret = reqProps.getProperty(LTIService.LTI_SECRET);
		if ( SECRET_HIDDEN.equals(newSecret) ) {
			reqProps.remove(LTIService.LTI_SECRET);
			newSecret = null;
		}

		if ( newSecret != null ) {
			newSecret = SakaiBLTIUtil.encryptSecret(newSecret.trim());
			reqProps.setProperty(LTIService.LTI_SECRET, newSecret);
		}

		if ( id == null ) 
		{
			retval = ltiService.insertTool(reqProps);
			success = rb.getString("success.created");
		} else {
			Long key = new Long(id);
			retval = ltiService.updateTool(key, reqProps);
			success = rb.getString("success.updated");
		}

		if ( retval instanceof String ) 
		{
			state.setAttribute(STATE_POST,reqProps);
			addAlert(state, (String) retval);
			state.setAttribute(STATE_ID,id);
			return;
		}

		state.setAttribute(STATE_SUCCESS,success);
		switchPanel(state, "ToolSystem");
	}

	/**
	 * Setup the velocity context and choose the template for options.
	 */
	public String buildMappingPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isAdmin() ) {
			addAlert(state,rb.getString("error.admin.view"));
			return "lti_error";
		}
		List<Map<String,Object>> mappings = ltiService.getMappings(null,null,0,100);
		context.put("mappings", mappings);
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		return "lti_mapping";
	}

	public String buildMappingInsertPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isAdmin() ) {
			addAlert(state,rb.getString("error.admin.edit"));
			return "lti_error";
		}
		context.put("doMappingAction", BUTTON + "doMappingPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		String [] mappingForm = ltiService.getMappingModel();
		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		String formInput = ltiService.formInput(previousPost, mappingForm);
		context.put("formInput",formInput);
		return "lti_mapping_insert";
	}

	public String buildMappingEditPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		String stateId = (String) state.getAttribute(STATE_ID);
		state.removeAttribute(STATE_ID);

		context.put("tlang", rb);
		if ( ! ltiService.isAdmin() ) {
			addAlert(state,rb.getString("error.admin.edit"));
			return "lti_error";
		}
		context.put("doMappingAction", BUTTON + "doMappingPut");
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		String [] mappingForm = ltiService.getMappingModel();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) id = stateId;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_mapping";
		}
		Long key = new Long(id);
		Map<String,Object> mapping = ltiService.getMapping(key);
		if (  mapping == null ) return "lti_main";		
		String formInput = ltiService.formInput(mapping, mappingForm);
		context.put("formInput", formInput);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_mapping_insert";
	}

	public String buildMappingDeletePanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isAdmin() ) {
			addAlert(state,rb.getString("error.admin.delete"));
			return "lti_error";
		}
		context.put("doToolAction", BUTTON + "doMappingDelete");
		String [] mappingForm = ltiService.getMappingModel();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_mapping";
		}
		Long key = new Long(id);

		Map<String,Object> mapping = ltiService.getMapping(key);

		if (  mapping == null ) {
			addAlert(state,rb.getString("error.mapping.not.found"));
			return "lti_mapping";
		}
		String formOutput = ltiService.formOutput(mapping, mappingForm);
		context.put("formOutput", formOutput);
		context.put("mapping",mapping);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_mapping_delete";
	}

	// Insert or edit
	public void doMappingPut(RunData data, Context context)
	{

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isAdmin() ) {
			addAlert(state,rb.getString("error.admin.edit"));
			switchPanel(state,"Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		String success = null;
		if ( id == null ) 
		{
			retval = ltiService.insertMapping(reqProps);
			success = rb.getString("success.created");
		} else {
			Long key = new Long(id);
			retval = ltiService.updateMapping(key, reqProps);
			success = rb.getString("success.updated");
		}

		if ( retval instanceof String ) 
		{
			state.setAttribute(STATE_POST,reqProps);
			addAlert(state, (String) retval);
			state.setAttribute(STATE_ID,id);
			return;
		}

		state.setAttribute(STATE_SUCCESS,success);
		switchPanel(state, "Mapping");
	}

	public void doMappingDelete(RunData data, Context context)
	{

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isAdmin() ) {
			addAlert(state,rb.getString("error.admin.delete"));
			switchPanel(state,"Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			switchPanel(state, "Mapping");
			return;
		}
		Long key = new Long(id);
		if ( ltiService.deleteMapping(key) )
		{
			state.setAttribute(STATE_SUCCESS,rb.getString("success.deleted"));
			switchPanel(state, "Mapping");
		} else {
			addAlert(state,rb.getString("error.delete.fail"));
			switchPanel(state, "Mapping");
		}
	}

	/** Content related methods ------------------------------ */

	public String buildContentPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.view"));
			return "lti_error";
		}
		List<Map<String,Object>> contents = ltiService.getContents(null,null,0,5000);
		for ( Map<String,Object> content : contents ) {
			String plstr = (String) content.get(LTIService.LTI_PLACEMENT);
			ToolConfiguration tool = SiteService.findTool(plstr);
			if ( tool == null ) {
				content.put(LTIService.LTI_PLACEMENT, null);
			}
		}
		context.put("contents", contents);
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		context.put("getContext",toolManager.getCurrentPlacement().getContext());
		state.removeAttribute(STATE_SUCCESS);
		return "lti_content";
	}


	public String buildContentPutPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		String contextString = toolManager.getCurrentPlacement().getContext();
		context.put("tlang", rb);
		String stateToolId = (String) state.getAttribute(STATE_TOOL_ID);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}
		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		context.put("doAction", BUTTON + "doContentPut");
		state.removeAttribute(STATE_SUCCESS);

		List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0);
		// only list the tools available in the system
		List<Map<String,Object>> systemTools = new ArrayList<Map<String,Object>>();
		for(Map<String, Object> tool:tools)
		{
			String siteId = !tool.containsKey(ltiService.LTI_SITE_ID)?null:StringUtils.trimToNull((String) tool.get(ltiService.LTI_SITE_ID));
			if (siteId == null)
			{
				// add tool for whole system
				systemTools.add(tool);
			}
			else if (siteId.equals(contextString))
			{
				// add the tool for current site only
				systemTools.add(tool);
			}
			else if (ltiService.isAdmin())
			{
				// if in Admin's my workspace, show all tools
				systemTools.add(tool);
			}
		}
		context.put("tools", systemTools);

		Object previousData = null;

		String toolId = data.getParameters().getString(LTIService.LTI_TOOL_ID);
		if ( toolId == null ) toolId = stateToolId;
		// output the tool id value to context
		context.put("tool_id", toolId);
		
		Long key = null;
		if ( toolId != null ) key = new Long(toolId);
		Map<String,Object> tool = null;
		if ( key != null ) {
			tool = ltiService.getTool(key);
			if ( tool == null ) {
				addAlert(state, rb.getString("error.tool.not.found"));
				return "lti_content_insert";
			}
		}

		String contentId = data.getParameters().getString(LTIService.LTI_ID);
		if ( contentId == null ) contentId = (String) state.getAttribute(STATE_CONTENT_ID);

		if ( contentId == null ) {  // Insert
			if ( toolId == null ) {
				return "lti_content_insert";
			}
			previousData = (Properties) state.getAttribute(STATE_POST);

			// Edit
		} else {
			Long contentKey = new Long(contentId);
			Map<String,Object> content = ltiService.getContent(contentKey);
			if ( content == null ) {
				addAlert(state, rb.getString("error.content.not.found"));
				state.removeAttribute(STATE_CONTENT_ID);
				return "lti_content";
			}

			if ( key == null ) {
				key = foorm.getLongNull(content.get(LTIService.LTI_TOOL_ID));
				if ( key != null ) tool = ltiService.getTool(key);
			}
			previousData = content;
			
			// whether the content has a site link created already?
			String plstr = (String) content.get(LTIService.LTI_PLACEMENT);
			ToolConfiguration siteLinkTool = SiteService.findTool(plstr);
			if ( siteLinkTool != null ) {
				context.put(LTIService.LTI_PLACEMENT, plstr);
			}
		}

		// We will handle the tool_id field ourselves in the Velocity code	 
        String [] contentForm = foorm.filterForm(null,ltiService.getContentModel(key), null, "^tool_id:.*");	 
        if ( contentForm == null || key == null ) {	 
                addAlert(state,rb.getString("error.tool.not.found"));	 
                return "lti_error";	 
        }	 
        String formInput = ltiService.formInput(previousData, contentForm);	 

        context.put("formInput",formInput);
		context.put(LTIService.LTI_TOOL_ID,key);
		if ( tool != null ) {
			context.put("tool_description", tool.get(LTIService.LTI_DESCRIPTION));
			Long visible = foorm.getLong(tool.get(LTIService.LTI_VISIBLE));
			context.put("tool_visible", visible);
		}
		
		return "lti_content_insert";
	}

	// Insert or edit
	public void doContentPut(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		state.removeAttribute(STATE_POST);
		
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		String toolId = data.getParameters().getString(LTIService.LTI_TOOL_ID);
		String title = data.getParameters().getString(LTIService.LTI_PAGETITLE);
		Object retval = ltiService.insertToolContent(id, toolId, reqProps);
		
		if ( retval instanceof String ) 
		{
			addAlert(state, (String) retval);
			switchPanel(state, "Error");
			state.setAttribute(STATE_POST,reqProps);
			state.setAttribute(STATE_CONTENT_ID,id);
			return;
		}
		else if ( retval instanceof Boolean )
		{
			// TODO: returns boolean
		}
		else
		{
			// the return value is the content key Long value
			id = ((Long) retval).toString();
		}

		String returnUrl = reqProps.getProperty("returnUrl");
		if ( returnUrl != null )
		{
			if ( id != null ) {
				Long contentKey = new Long(id);
				if ( returnUrl.startsWith("about:blank") ) { // Redirect to the item
					Map<String,Object> content = ltiService.getContent(contentKey);
					if ( content != null ) {
						String launch = (String) ltiService.getContentLaunch(content);
						if ( launch != null ) returnUrl = launch;
					}
					switchPanel(state, "Forward");
				} else {
					if ( returnUrl.indexOf("?") > 0 ) {
						returnUrl += "&ltiItemId=/blti/" + retval;
					} else {
						returnUrl += "?ltiItemId=/blti/" + retval;
					}
					switchPanel(state, "Redirect");
				}
			}
			state.setAttribute(STATE_REDIRECT_URL,returnUrl);
			return;
		}
		
		String success = null;
		if ( id == null ) 
		{
			success = rb.getString("success.created");
		} else {
			success = rb.getString("success.updated");
		}
		state.setAttribute(STATE_SUCCESS,success);
		
		if (reqProps.getProperty("add_site_link") != null)
		{
			// this is to add site link:
			retval = ltiService.insertToolSiteLink(id, title);
			if ( retval instanceof String ) {
				String prefix = ((String) retval).substring(0,2);
				addAlert(state, ((String) retval).substring(2));
				if ("0-".equals(prefix))
				{
					switchPanel(state, "Refresh");
				}
				else if ("1-".equals(prefix))
				{
					switchPanel(state, "Error");
				}
				return;
			}
			else if ( retval instanceof Boolean ) {
				if (((Boolean) retval).booleanValue())
				{
					switchPanel(state, "Refresh");
				}
				else
				{
					switchPanel(state, "Error");
				}
				return;
			}
			
			state.setAttribute(STATE_SUCCESS,rb.getString("success.link.add"));
		}


		switchPanel(state, "ToolSite");
	}

	public String buildRedirectPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		String returnUrl = (String) state.getAttribute(STATE_REDIRECT_URL);
		state.removeAttribute(STATE_REDIRECT_URL);
		if ( returnUrl == null ) return "lti_content_redirect";
		// System.out.println("Redirecting parent frame back to="+returnUrl);
		if ( ! returnUrl.startsWith("about:blank") ) context.put("returnUrl",returnUrl);
		return "lti_content_redirect";
	}

	public String buildForwardPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		String returnUrl = (String) state.getAttribute(STATE_REDIRECT_URL);
		state.removeAttribute(STATE_REDIRECT_URL);
		if ( returnUrl == null ) return "lti_content_redirect";
		// System.out.println("Forwarding frame to="+returnUrl);
		context.put("forwardUrl",returnUrl);
		return "lti_content_redirect";
	}

	// Special panel for Lesson Builder
	// Add New: panel=Config&tool_id=14
	// Edit existing: panel=Config&id=12
	public String buildContentConfigPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		state.removeAttribute(STATE_SUCCESS);

		Properties previousPost = (Properties) state.getAttribute(STATE_POST);
		state.removeAttribute(STATE_POST);

		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.edit"));
			return "lti_error";
		}

		String returnUrl = data.getParameters().getString("returnUrl");
		if ( returnUrl == null && previousPost != null ) returnUrl = previousPost.getProperty("returnUrl");
		if ( returnUrl == null ) {
			addAlert(state,rb.getString("error.missing.return"));
			return "lti_error";
		}

		Map<String,Object> content = null;
		Map<String,Object> tool = null;

		Long toolKey = foorm.getLongNull(data.getParameters().getString(LTIService.LTI_TOOL_ID));

		Long contentKey = foorm.getLongNull(data.getParameters().getString(LTIService.LTI_ID));
		if ( contentKey == null && previousPost != null ) contentKey = foorm.getLongNull(previousPost.getProperty(LTIService.LTI_ID));
		if ( contentKey != null ) {
			content = ltiService.getContent(contentKey);
			if ( content == null ) {
				addAlert(state, rb.getString("error.content.not.found"));
				state.removeAttribute(STATE_CONTENT_ID);
				return "lti_error";
			}
			toolKey = foorm.getLongNull(content.get(LTIService.LTI_TOOL_ID));
		}
		if ( toolKey == null && previousPost != null ) toolKey = foorm.getLongNull(previousPost.getProperty(LTIService.LTI_TOOL_ID));
		if ( toolKey != null ) tool = ltiService.getTool(toolKey);

		// No matter what, we must have a tool
		if ( tool == null ) {
			addAlert(state, rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		Object previousData = null;
		if ( content != null ) { 
			previousData = content;
		} else { 
			previousData = (Properties) state.getAttribute(STATE_POST);
		}

		// We will handle the tool_id field ourselves in the Velocity code
		String [] contentForm = foorm.filterForm(null,ltiService.getContentModel(toolKey), null, "^tool_id:.*|^SITE_ID:.*");
		if ( contentForm == null ) {
			addAlert(state,rb.getString("error.tool.not.found"));
			return "lti_error";
		}

		context.put("isAdmin",new Boolean(ltiService.isAdmin()) );
		context.put("doAction", BUTTON + "doContentPut");
		if ( ! returnUrl.startsWith("about:blank") ) context.put("cancelUrl", returnUrl);
		context.put("returnUrl", returnUrl);
		context.put(LTIService.LTI_TOOL_ID,toolKey);
		context.put("tool_description", tool.get(LTIService.LTI_DESCRIPTION));
		context.put("tool_title", tool.get(LTIService.LTI_TITLE));
		context.put("tool_launch", tool.get(LTIService.LTI_LAUNCH));


		String key = (String) tool.get(LTIService.LTI_CONSUMERKEY);
		String secret = (String) tool.get(LTIService.LTI_SECRET);
		if ( LTIService.LTI_SECRET_INCOMPLETE.equals(secret) && LTIService.LTI_SECRET_INCOMPLETE.equals(key) ) {
			String keyField = foorm.formInput(null,"consumerkey:text:label=need.tool.key:required=true:maxlength=255", rb);
			context.put("keyField", keyField);
			String secretField = foorm.formInput(null,"secret:text:required=true:label=need.tool.secret:maxlength=255", rb);
			context.put("secretField", secretField);
		}
		
		String formInput = ltiService.formInput(previousData, contentForm);
		context.put("formInput",formInput);

		return "lti_content_config";
	}

	public String buildContentDeletePanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doContentDelete");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}	
		Long key = new Long(id);
		Map<String,Object> content = ltiService.getContent(key);
		if (  content == null ) {
			addAlert(state,rb.getString("error.content.not.found"));
			return "lti_main";
		}
		Long tool_id_long = null;
		try{
			tool_id_long = new Long(content.get("tool_id").toString());
		}
		catch (Exception e)
		{
			// log the error
			M_log.error("error parsing tool id " + content.get("tool_id"));
		}
		content.put("tool_id_long", tool_id_long);
		context.put("content",content);
		context.put("ltiService", ltiService);
		
		state.removeAttribute(STATE_SUCCESS);
		return "lti_content_delete";
	}

	// Insert or edit
	public void doContentDelete(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.delete"));
			switchPanel(state, "Error");
			return;
		}
		Properties reqProps = data.getParameters().getProperties();
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Object retval = null;
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			switchPanel(state, "Content");
			return;
		}
		Long key = new Long(id);
		// also remove the link
		if ( ltiService.deleteContent(key) )
		{
			state.setAttribute(STATE_SUCCESS,rb.getString("success.deleted"));
		} else {
			addAlert(state,rb.getString("error.delete.fail"));
		}
		switchPanel(state, "Refresh");
	}

	public String buildLinkAddPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.link"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doSiteLink");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}	
		Long key = new Long(id);
		Map<String,Object> content = ltiService.getContent(key);
		if (  content == null ) {
			addAlert(state,rb.getString("error.content.not.found"));
			return "lti_main";
		}
		context.put("content",content);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_link_add";
	}

	// Insert or edit
	public void doSiteLink(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		String id = data.getParameters().getString(LTIService.LTI_ID);
		String button_text = data.getParameters().getString("button_text");
		
		Object retval = ltiService.insertToolSiteLink(id, button_text);
		if ( retval instanceof String ) {
			String prefix = ((String) retval).substring(0,2);
			addAlert(state, ((String) retval).substring(2));
			if ("0-".equals(prefix))
			{
				switchPanel(state, "Content");
			}
			else if ("1-".equals(prefix))
			{
				switchPanel(state, "Error");
			}
			return;
		}
		
		state.setAttribute(STATE_SUCCESS,rb.getString("success.link.add"));
		switchPanel(state, "Refresh");
	}

	public String buildLinkRemovePanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isMaintain() ) {
			addAlert(state,rb.getString("error.maintain.link"));
			return "lti_error";
		}
		context.put("doAction", BUTTON + "doLinkRemove");
		String id = data.getParameters().getString(LTIService.LTI_ID);
		if ( id == null ) {
			addAlert(state,rb.getString("error.id.not.found"));
			return "lti_main";
		}	
		Long key = new Long(id);
		Map<String,Object> content = ltiService.getContent(key);
		if (  content == null ) {
			addAlert(state,rb.getString("error.content.not.found"));
			return "lti_main";
		}
		context.put("content",content);
		state.removeAttribute(STATE_SUCCESS);
		return "lti_link_remove";
	}

	public void doLinkRemove(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		
		String id = data.getParameters().getString(LTIService.LTI_ID);
		Long key = id == null ? null:new Long(id);
		
		String rv = ltiService.deleteContentLink(key);
		if (rv != null)
		{
			// there is error removing the external tool site link
			addAlert(state, rv);
			switchPanel(state, "Error");
			return;
		}
		else
		{
			// external tool site link removed successfully
			state.setAttribute(STATE_SUCCESS,rb.getString("success.link.remove"));
			switchPanel(state, "Refresh");
		}
	}

	public String buildRefreshPanelContext(VelocityPortlet portlet, Context context, 
			RunData data, SessionState state)
	{
		context.put("tlang", rb);
		context.put("messageSuccess",state.getAttribute(STATE_SUCCESS));
		state.removeAttribute(STATE_SUCCESS);
		return "lti_top_refresh";
	}

	public String buildTestPanelContext(VelocityPortlet portlet, Context context, 
			RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		if ( ! ltiService.isAdmin() ) {
			addAlert(state,rb.getString("error.admin.view"));
			return "lti_error";
		}
		StringBuffer sb = new StringBuffer();

		try { 
			// Get a list of tools
			List<Map<String,Object>> tools = ltiService.getTools(null,null,0,0);
			sb.append(""+tools.size()+" tools available\n");
			if ( tools.size() > 2 ) {
				sb.append("Tools 1-2 (zero-based):\n");
				tools = ltiService.getTools(null,null,1,2);
			} else {
				sb.append("Tools 0-1 (first two):\n");
				tools = ltiService.getTools(null,null,0,1);
			}

			for (Map<String, Object> tool : tools ) {
				sb.append("  Tool\n");
				for ( String key : tool.keySet() ) {
					sb.append("     ");
					sb.append(key);
					sb.append("=");
					Object obj = tool.get(key);
					if ( obj == null ) sb.append("null"); else sb.append(obj.toString());
					sb.append("\n");
				}
			}

			if ( tools.size() < 1 ) {
				sb.append("\n--No Tools Exist---\n");
				context.put("preOutput",sb.toString());
				return "lti_test";
			}

			// Lets grab the tool key...
			Map<String,Object> tool = tools.get(0);

			// We will assume this works
			Long toolKey = foorm.getLong(tool.get(LTIService.LTI_ID));

			sb.append("Long Tool Key=");
			sb.append(toolKey.toString());
			sb.append("\n\n");
			sb.append("Our Context=");
			sb.append(toolManager.getCurrentPlacement().getContext());
			sb.append("\n\n");
			sb.append("Raw/underlying content model (String [])\n");
			for(String field : ltiService.CONTENT_MODEL) {
				sb.append("  ");
				sb.append(field);
				sb.append("\n");
			}
			sb.append("\n");

			String [] contentModel = ltiService.getContentModel(toolKey);
			sb.append("Properly filtered content model (String [])\n");
			for(String field : contentModel) {
				sb.append("  ");
				sb.append(field);
				sb.append("\n");
			}

			// Lets do this with properties (i.e. from a Request Object)
			Properties props = new Properties ();
			props.setProperty(LTIService.LTI_SITE_ID,toolManager.getCurrentPlacement().getContext());
			props.setProperty(LTIService.LTI_TOOL_ID,toolKey.toString());
			props.setProperty(LTIService.LTI_TITLE, "A title");
			props.setProperty(LTIService.LTI_DESCRIPTION, "A title");
			props.setProperty("debug", "0"); 

			sb.append("\nConstructed content Properties for insert\n");
			for ( Object okey : props.keySet() ) {
				String key = (String) okey;
				sb.append("     ");
				sb.append(key);
				sb.append("=");
				String obj = props.getProperty(key);
				if ( obj == null ) sb.append("null"); else sb.append(obj.toString());
				sb.append("\n");
			}

			sb.append("Inserting from Properties...\n");
			Object result = ltiService.insertContent(props);
			Long contentKey = null;
			if ( result instanceof String ) {
				sb.append("Insert failed:");
				sb.append((String) result);
				sb.append("\n");
				context.put("preOutput",sb.toString());
				return "lti_test";
			} else {
				contentKey = foorm.getLong(result);
				sb.append("Returned key=");
				sb.append(contentKey.toString() );
				sb.append("\n");
			}

			if ( contentKey < 0 ) {
				sb.append("\nBad key returned (might be HSQL ickiness)\n");
				context.put("preOutput",sb.toString());
				return "lti_test";
			}

			sb.append("Retrieving content key=");
			sb.append(contentKey.toString());
			sb.append("\n");

			Map<String,Object> contentMap = ltiService.getContent(contentKey);
			if ( contentMap == null ) {
				sb.append("getContent failed\n");
				context.put("preOutput",sb.toString());
				return "lti_test";
			}

			sb.append("\nRetrieved content\n");
			for ( String key : contentMap.keySet() ) {
				sb.append("  ");
				sb.append(key);
				sb.append("=");
				Object obj = tool.get(key);
				if ( obj == null ) sb.append("null"); else sb.append(obj.toString());
				sb.append("\n");
			}

			// Lets update the content (as if we just got a form)
			// We don't need all properties - just the ones we want to update
			// tool_id is required
			props = new Properties ();
			props.setProperty(LTIService.LTI_TITLE, "A NEW AWESOME TITLE");
			props.setProperty(LTIService.LTI_TOOL_ID,toolKey.toString());

			sb.append("\nConstructed content Properties for update\n");
			for ( Object okey : props.keySet() ) {
				String key = (String) okey;
				sb.append("     ");
				sb.append(key);
				sb.append("=");
				String obj = props.getProperty(key);
				if ( obj == null ) sb.append("null"); else sb.append(obj.toString());
				sb.append("\n");
			}

			sb.append("Updating...");
			// Object retval
			result = ltiService.updateContent(contentKey, props);
			if ( result instanceof String ) {
				sb.append("Update failed:");
				sb.append((String) result);
				sb.append("\n");
				context.put("preOutput",sb.toString());
				return "lti_test";
			} else {
				sb.append("Update success\n");
			}

			sb.append("\nRe-retrieving content to verify update key=");
			sb.append(contentKey.toString());
			sb.append("\n");

			// Map<String,Object>
			contentMap = ltiService.getContent(contentKey);
			if ( contentMap == null ) {
				sb.append("getContent failed\n");
				context.put("preOutput",sb.toString());
				return "lti_test";
			}

			sb.append("\nRetrieved content to verify update success\n");
			for ( String key : contentMap.keySet() ) {
				sb.append("  ");
				sb.append(key);
				sb.append("=");
				Object obj = contentMap.get(key);
				if ( obj == null ) sb.append("null"); else sb.append(obj.toString());
				sb.append("\n");
			}

			// Lets make an input form if you want this auto generated for free
			sb.append("\nAn Input Form\n");
			String formInp = ltiService.formInput(contentMap, contentModel);
			sb.append(formInp.replace("><",">\n<").replace("<","&lt;").replace(">","&gt"));

			sb.append("Lets make some mistakes\n");

			// Properties 
			props = new Properties ();
			props.setProperty(LTIService.LTI_SITE_ID,toolManager.getCurrentPlacement().getContext());
			props.setProperty(LTIService.LTI_TOOL_ID,"I should be an integer!");
			props.setProperty(LTIService.LTI_TITLE, "A title");

			sb.append("\nConstructed broken content Properties for insert\n");
			for ( Object okey : props.keySet() ) {
				String key = (String) okey;
				sb.append("     ");
				sb.append(key);
				sb.append("=");
				String obj = props.getProperty(key);
				if ( obj == null ) sb.append("null"); else sb.append(obj.toString());
				sb.append("\n");
			}

			sb.append("Inserting from Properties...\n");

			// OBJECT
			result = ltiService.insertContent(props);
			if ( result instanceof String ) {
				sb.append("Insert correctly failed:");
				sb.append((String) result);
				sb.append("\n");
			}

			sb.append("\nLets forget a required parameter on an update...\n");
			props = new Properties ();
			props.setProperty(LTIService.LTI_TITLE, "YET ANOTHER AWESOME TITLE");
			// FORGET ME: props.setProperty(LTIService.LTI_TOOL_ID,toolKey.toString());

			sb.append("\nConstructed broken content Properties for update\n");
			for ( Object okey : props.keySet() ) {
				String key = (String) okey;
				sb.append("     ");
				sb.append(key);
				sb.append("=");
				String obj = props.getProperty(key);
				if ( obj == null ) sb.append("null"); else sb.append(obj.toString());
				sb.append("\n");
			}

			sb.append("\nUpdating......\n");
			// Object retval
			result = ltiService.updateContent(contentKey, props);
			if ( result instanceof String ) {
				sb.append("Update failed properly:");
				sb.append((String) result);
				sb.append("\n");
			} else {
				sb.append("Update success - not what we wanted to happen\n");
			}

			// Lets delete the content to clean things up (if we got this far)
			sb.append("\nALL DONE... CLEANUP TIME...\nDeleting key=");
			sb.append(contentKey.toString());
			sb.append("\n");
			boolean retval = ltiService.deleteContent(contentKey);
			sb.append("Return value from delete="+retval);
			sb.append("\n");
		} catch (Exception e) {
			sb.append(e.getMessage());
			sb.append(getStackTrace(e));
		}

		context.put("preOutput",sb.toString());
		return "lti_test";
	}

	public static String getStackTrace(Throwable throwable) {
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		throwable.printStackTrace(printWriter);
		return writer.toString();
	}

	public static Site getCurrentSite() {

		try {
			return SiteService.getSite(toolManager.getCurrentPlacement().getContext());
		} catch (Exception impossible) {
			M_log.error("Cannot load site" + impossible);
		}
		return null;
	}

}
