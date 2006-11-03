/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.content.tool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;

public class ResourcesHelperAction extends VelocityPortletPaneledAction 
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("content");

	protected  static final String ACCESS_HTML_TEMPLATE = "resources/sakai_access_html";
	protected  static final String ACCESS_TEXT_TEMPLATE = "resources/sakai_access_text";
	protected  static final String ACCESS_UPLOAD_TEMPLATE = "resources/sakai_access_upload";
	protected  static final String ACCESS_URL_TEMPLATE = "resources/sakai_access_url";
	
	protected  static final String CREATE_HTML_TEMPLATE = "resources/sakai_create_html";
	protected  static final String CREATE_TEXT_TEMPLATE = "resources/sakai_create_text";
	protected  static final String CREATE_UPLOAD_TEMPLATE = "resources/sakai_create_upload";
	protected  static final String CREATE_URL_TEMPLATE = "resources/sakai_create_url";
	
	protected  static final String REVISE_HTML_TEMPLATE = "resources/sakai_revise_html";
	protected  static final String REVISE_TEXT_TEMPLATE = "resources/sakai_revise_text";
	protected  static final String REVISE_UPLOAD_TEMPLATE = "resources/sakai_revise_upload";
	protected  static final String REVISE_URL_TEMPLATE = "resources/sakai_revise_url";
	
	public String buildMainPanelContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		context.put("tlang", rb);
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		String actionId = (String) toolSession.getAttribute(ResourceToolAction.ACTION);
		
		String template = "";

		if(ResourceToolAction.CREATE.equals(actionId))
		{
			template = buildCreateContext(portlet, context, data, state);
		}
		
		
		return template;
	}

	public String buildAccessContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		String template = ACCESS_TEXT_TEMPLATE;
		return template;
	}

	public String buildCreateContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		String template = CREATE_UPLOAD_TEMPLATE;
		
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
		String typeId = (String) toolSession.getAttribute(ResourceToolAction.RESOURCE_TYPE);

		if(ResourceType.TYPE_TEXT.equals(typeId))
		{
			template = CREATE_TEXT_TEMPLATE;
		}
		else if(ResourceType.TYPE_HTML.equals(typeId))
		{
			template = CREATE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_URL.equals(typeId))
		{
			template = CREATE_URL_TEMPLATE;
		}
		else // assume ResourceType.TYPE_UPLOAD
		{
			template = CREATE_UPLOAD_TEMPLATE;
		}
		
		return template;
	}

	public String buildReviseContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		String template = REVISE_TEXT_TEMPLATE;
		return template;
	}
	
	public void doCancel(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		Tool tool = ToolManager.getCurrentTool();
		String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

		toolSession.setAttribute(ResourceToolAction.ACTION_CANCELED, Boolean.TRUE);

	}
	
	public void doContinue(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String content = params.getString("content");
		if(content == null)
		{
			addAlert(state, "Please enter the contents of the text document");
			return;
		}
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		toolSession.setAttribute(ResourceToolAction.RESOURCE_CONTENT, content);
		
		toolSession.setAttribute(ResourceToolAction.ACTION_SUCCEEDED, Boolean.TRUE);
		
		Tool tool = ToolManager.getCurrentTool();
		String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

	}

}
