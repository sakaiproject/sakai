/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.pages;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolException;
import org.sakaiproject.tool.api.ToolSession;

public class FilePickerPage extends BaseToolPage
{
	private static final long serialVersionUID = 1L;

	public FilePickerPage()
	{
		add(new AjaxFallbackLink("sendToHelper")
		{
			@Override
			public void onClick(Optional target)
			{
				HttpServletRequest req = (HttpServletRequest) getRequest().getContainerRequest();
				HttpServletResponse res = (HttpServletResponse) getResponse().getContainerResponse();

				try
				{
					sendToHelper(req, res);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		});
	}

	protected List<Reference> sendToHelper(HttpServletRequest req, HttpServletResponse res) throws ToolException
	{
		SessionManager sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
		EntityManager entityManager = (EntityManager) ComponentManager.get(EntityManager.class);
		ActiveToolManager activeToolManager = (ActiveToolManager) ComponentManager.get(ActiveToolManager.class);

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		List<Reference> filePickerList = entityManager.newReferenceList();
		toolSession.setAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS, filePickerList);

		ActiveTool helperTool = activeToolManager.getActiveTool("sakai.filepicker");
		toolSession.setAttribute(helperTool.getId() + Tool.HELPER_DONE_URL, req.getContextPath() + req.getServletPath());

		String toolContext = req.getContextPath() + req.getServletPath();
		String toolPath = "sakai.filepicker.helper/tool";

		helperTool.help(req, res, toolContext, toolPath);
		return filePickerList;
	}
}
