/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.content.api.MultiFileUploadPipe;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FileItem;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

public class ResourcesHelperAction extends VelocityPortletPaneledAction 
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("types");

	protected  static final String ACCESS_HTML_TEMPLATE = "resources/sakai_access_html";
	protected  static final String ACCESS_TEXT_TEMPLATE = "resources/sakai_access_text";
	protected  static final String ACCESS_UPLOAD_TEMPLATE = "resources/sakai_access_upload";
	protected  static final String ACCESS_URL_TEMPLATE = "resources/sakai_access_url";
	
	protected  static final String CREATE_HTML_TEMPLATE = "resources/sakai_create_html";
	protected  static final String CREATE_TEXT_TEMPLATE = "resources/sakai_create_text";
	protected  static final String CREATE_UPLOAD_TEMPLATE = "resources/sakai_create_upload";
	protected  static final String CREATE_UPLOADS_TEMPLATE = "resources/sakai_create_uploads";
	protected  static final String CREATE_URL_TEMPLATE = "resources/sakai_create_url";
	
	protected  static final String REVISE_HTML_TEMPLATE = "resources/sakai_revise_html";
	protected  static final String REVISE_TEXT_TEMPLATE = "resources/sakai_revise_text";
	protected  static final String REVISE_UPLOAD_TEMPLATE = "resources/sakai_revise_upload";
	protected  static final String REVISE_URL_TEMPLATE = "resources/sakai_revise_url";
	
	public static final String MODE_MAIN = "main";


	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
		throws ToolException
	{
		SessionState sstate = getState(req);
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		//String mode = (String) sstate.getAttribute(ResourceToolAction.STATE_MODE);
		//Object started = toolSession.getAttribute(ResourceToolAction.STARTED);
		Object done = toolSession.getAttribute(ResourceToolAction.DONE);
		
		if (done != null)
		{
			toolSession.removeAttribute(ResourceToolAction.STARTED);
			Tool tool = ToolManager.getCurrentTool();
		
			String url = (String) SessionManager.getCurrentToolSession().getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			SessionManager.getCurrentToolSession().removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
			try
			{
				res.sendRedirect(url);
			}
			catch (IOException e)
			{
				// Log.warn("chef", this + " : ", e);
			}
			return;
		}
		
		super.toolModeDispatch(methodBase, methodExt, req, res);
	}


	
	public String buildMainPanelContext(VelocityPortlet portlet,
			Context context,
			RunData data,
			SessionState state)
	{
		context.put("tlang", rb);
		
		context.put("validator", new Validator());
		String mode = (String) state.getAttribute(ResourceToolAction.STATE_MODE);

		if (mode == null)
		{
			initHelper(portlet, context, data, state);
		}

		ToolSession toolSession = SessionManager.getCurrentToolSession();
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		if(pipe.isActionCompleted())
		{
			return null;
		}

		String actionId = pipe.getAction().getId();
		
		String template = "";

		switch(pipe.getAction().getActionType())
		{
		case CREATE:
			template = buildCreateContext(portlet, context, data, state);
			break;
		case REVISE_CONTENT:
			template = buildReviseContext(portlet, context, data, state);
			break;
		case REPLACE_CONTENT:
			template = buildReplaceContext(portlet, context, data, state);
			break;
		case NEW_UPLOAD:
			template = buildUploadFilesContext(portlet, context, data, state);
			break;
		default:
			// hmmmm
			break;
		}
		
		return template;
	}

	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String buildUploadFilesContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		MultiFileUploadPipe pipe = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		List<ResourceToolActionPipe> pipes = pipe.getPipes();
		
		context.put("pipes", pipes);
		
		

		return CREATE_UPLOADS_TEMPLATE;
	}



	/**
	 * @param portlet
	 * @param context
	 * @param data
	 * @param state
	 * @return
	 */
	protected String buildReplaceContext(VelocityPortlet portlet, Context context, RunData data, SessionState state)
	{
		// TODO Auto-generated method stub
		return null;
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

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
		String typeId = pipe.getAction().getTypeId();

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
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);

		//Reference reference = (Reference) toolSession.getAttribute(ResourceToolAction.COLLECTION_REFERENCE);
		String typeId = pipe.getAction().getTypeId();
		
		context.put("pipe", pipe);

		if(ResourceType.TYPE_TEXT.equals(typeId))
		{
			template = REVISE_TEXT_TEMPLATE;
		}
		else if(ResourceType.TYPE_HTML.equals(typeId))
		{
			template = REVISE_HTML_TEMPLATE;
		}
		else if(ResourceType.TYPE_URL.equals(typeId))
		{
			template = REVISE_URL_TEMPLATE;
		}
		else // assume ResourceType.TYPE_UPLOAD
		{
			template = REVISE_UPLOAD_TEMPLATE;
		}
		
		return template;
	}
	
	public void doCancel(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		//Tool tool = ToolManager.getCurrentTool();
		//String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		//toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);
		
		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		pipe.setActionCanceled(true);
		pipe.setErrorEncountered(false);
		pipe.setActionCompleted(true);

		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);
		
		
	}
	
	public void doContinue(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();

		String content = params.getString("content");
		if(content == null)
		{
			addAlert(state, rb.getString("text.notext"));
			return;
		}
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
//		Tool tool = ToolManager.getCurrentTool();
//		String url = (String) toolSession.getAttribute(tool.getId() + Tool.HELPER_DONE_URL);
//		toolSession.removeAttribute(tool.getId() + Tool.HELPER_DONE_URL);

		ResourceToolActionPipe pipe = (ResourceToolActionPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		String resourceType = pipe.getAction().getTypeId();
		
		pipe.setRevisedMimeType(pipe.getMimeType());
		if(ResourceType.TYPE_TEXT.equals(resourceType))
		{
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_TEXT);
		}
		else if(ResourceType.TYPE_HTML.equals(resourceType))
		{
			StringBuffer alertMsg = new StringBuffer();
			content = FormattedText.processHtmlDocument(content, alertMsg);
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_HTML);
			if (alertMsg.length() > 0)
			{
				addAlert(state, alertMsg.toString());
				return;
			}
		}
		else if(ResourceType.TYPE_URL.equals(resourceType))
		{
			pipe.setRevisedMimeType(ResourceType.MIME_TYPE_URL);
		}
	
		pipe.setRevisedContent(content.getBytes());
		pipe.setActionCanceled(false);
		pipe.setErrorEncountered(false);
		pipe.setActionCompleted(true);
		
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

	}

	public void doUpload(RunData data)
	{
		SessionState state = ((JetspeedRunData)data).getPortletSessionState (((JetspeedRunData)data).getJs_peid ());
		ParameterParser params = data.getParameters ();
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		
		String max_file_size_mb = (String) state.getAttribute(ResourcesAction.STATE_FILE_UPLOAD_MAX_SIZE);
		int max_bytes = 1024 * 1024;
		try
		{
			max_bytes = Integer.parseInt(max_file_size_mb) * 1024 * 1024;
		}
		catch(Exception e)
		{
			// if unable to parse an integer from the value
			// in the properties file, use 1 MB as a default
			max_file_size_mb = "1";
			max_bytes = 1024 * 1024;
		}

		MultiFileUploadPipe mfp = (MultiFileUploadPipe) toolSession.getAttribute(ResourceToolAction.ACTION_PIPE);
		
		int count = params.getInt("fileCount");
		mfp.setFileCount(count);
		
		List<ResourceToolActionPipe> pipes = mfp.getPipes();
		for(int i = 0; i < pipes.size(); i++)
		{
			ResourceToolActionPipe pipe = pipes.get(i);
			
			FileItem fileitem = null;
			try
			{
				fileitem = params.getFileItem("content" + (i + 1));
			}
			catch(Exception e)
			{
				// TODO: use logger
				e.printStackTrace();
			}
			
			if(fileitem == null)
			{
				// "The user submitted a file to upload but it was too big!"
				addAlert(state, rb.getString("size") + " " + max_file_size_mb + "MB " + rb.getString("exceeded2"));
			}
			else if (fileitem.getFileName() == null || fileitem.getFileName().length() == 0)
			{
				addAlert(state, rb.getString("choosefile7"));
			}
			else if (fileitem.getFileName().length() > 0)
			{
				String filename = Validator.getFileName(fileitem.getFileName());
				byte[] bytes = fileitem.get();
				String contentType = fileitem.getContentType();

				pipe.setRevisedContent(bytes);
				pipe.setRevisedMimeType(contentType);
				pipe.setFileName(filename);
			}
		}

		mfp.setActionCanceled(false);
		mfp.setErrorEncountered(false);
		mfp.setActionCompleted(true);
		
		toolSession.setAttribute(ResourceToolAction.DONE, Boolean.TRUE);

	}
	
	protected void initHelper(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		//toolSession.setAttribute(ResourceToolAction.STARTED, Boolean.TRUE);
		//state.setAttribute(ResourceToolAction.STATE_MODE, MODE_MAIN);
	}

}
