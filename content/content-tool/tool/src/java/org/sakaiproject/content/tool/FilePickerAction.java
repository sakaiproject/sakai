/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * The FilePickerAction drives the FilePicker helper.<br />
 * This works with the ResourcesTool to show a file picker / attachment editor that can be used by any Sakai tools as a helper.<br />
 * If the user ends without a cancel, the original collection of attachments is replaced with the edited list - otherwise it is left unchanged.
 */
public class FilePickerAction extends VelocityPortletPaneledAction
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("helper");

	protected void toolModeDispatch(String methodBase, String methodExt, HttpServletRequest req, HttpServletResponse res)
			throws ToolException
	{
		SessionState sstate = getState(req);

		if (ResourcesAction.MODE_ATTACHMENT_DONE.equals(sstate.getAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE)))
		{
			ToolSession toolSession = SessionManager.getCurrentToolSession();

			if (sstate.getAttribute(ResourcesAction.STATE_HELPER_CANCELED_BY_USER) == null)
			{
				// not canceled, so populate the original list with the results
				List attachments = (List) sstate.getAttribute(ResourcesAction.STATE_ATTACHMENTS);

				if (attachments != null)
				{
					// get the original list
					Collection original = (Collection) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
					if(original == null)
					{
						original = EntityManager.newReferenceList();
						toolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, original);
					}

					// replace its contents with the edited attachments
					original.clear();
					original.addAll(attachments);
				}

				// otherwise the original list remains unchanged

				else if (sstate.getAttribute(ResourcesAction.STATE_EDIT_ID) == null)
				{
					toolSession.setAttribute(FilePickerHelper.FILE_PICKER_CANCEL, "true");
				}
			}
			else
			{
				toolSession.setAttribute(FilePickerHelper.FILE_PICKER_CANCEL, "true");
			}

			cleanup(sstate);

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
		else
		{
			super.toolModeDispatch(methodBase, methodExt, req, res);
		}
	}

	protected void cleanup(SessionState sstate)
	{
		sstate.removeAttribute(ResourcesAction.STATE_MODE);
		sstate.removeAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE);
		sstate.removeAttribute(ResourcesAction.STATE_ATTACHMENTS);
		sstate.removeAttribute(ResourcesAction.STATE_HELPER_CANCELED_BY_USER);
      
      sstate.removeAttribute(ResourcesAction.STATE_ATTACH_FILTER);
      sstate.removeAttribute(ResourcesAction.STATE_ATTACH_CARDINALITY);
      
      ToolSession toolSession = SessionManager.getCurrentToolSession();
      if (toolSession != null) {
         toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS);
         toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER);
      }
	}

	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		// if we are in edit attachments...
		String mode = (String) sstate.getAttribute(ResourcesAction.STATE_MODE);
		ToolSession toolSession = SessionManager.getCurrentToolSession();

		if (mode == null)
		{
			mode = initHelperAction(portlet, context, rundata, sstate, toolSession);
		}

		return ResourcesAction.buildHelperContext(portlet, context, rundata, sstate);
	}

	protected String initHelperAction(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate,
			ToolSession toolSession)
	{
		initPicker(portlet, context, rundata, sstate);
		sstate.setAttribute(ResourcesAction.STATE_MODE, ResourcesAction.MODE_HELPER);
		sstate.setAttribute(ResourcesAction.STATE_RESOURCES_HELPER_MODE, ResourcesAction.MODE_ATTACHMENT_SELECT);
		
		// TODO: Should check sakai.properties
		sstate.setAttribute(ResourcesAction.STATE_SHOW_ALL_SITES, Boolean.toString(true));

		// state attribute ResourcesAction.STATE_ATTACH_TOOL_NAME should be set with a string to indicate name of tool
		// String toolName = ToolManager.getCurrentTool().getTitle();
		// sstate.setAttribute(ResourcesAction.STATE_ATTACH_TOOL_NAME, toolName);

		if (toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS) != null)
		{
			sstate.setAttribute(ResourcesAction.STATE_ATTACH_LINKS, toolSession
					.getAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS));
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS);
		}

		if (toolSession.getAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS) != null)
		{
			sstate.setAttribute(ResourcesAction.STATE_ATTACH_CARDINALITY, toolSession
					.getAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS));
		}

		return ResourcesAction.MODE_HELPER;
	}

	protected void initPicker(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		ToolSession toolSession = SessionManager.getCurrentToolSession();
		List attachments = (List) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);

		// start with a copy of the original attachment list
		if (attachments != null)
		{
			sstate.setAttribute(ResourcesAction.STATE_ATTACHMENTS, EntityManager.newReferenceList(attachments));
		}

		initMessage(toolSession, sstate);

		sstate.setAttribute(ResourcesAction.STATE_ATTACH_FILTER, toolSession
				.getAttribute(FilePickerHelper.FILE_PICKER_RESOURCE_FILTER));
	}

	protected void initMessage(ToolSession toolSession, SessionState sstate)
	{
		String message = (String) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		if (message == null)
		{
			message = rb.getString(FilePickerHelper.FILE_PICKER_TITLE_TEXT);
		}
		sstate.setAttribute(ResourcesAction.STATE_ATTACH_TITLE, message);

		message = (String) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		if (message == null)
		{
			message = rb.getString(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT);
		}
		sstate.setAttribute(ResourcesAction.STATE_ATTACH_INSTRUCTION, message);
	}
}
