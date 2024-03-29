/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.VersionException;
import uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ResourceLoaderHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.command.helper.ErrorBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

/**
 * @author andrew
 */
@Slf4j
public class SaveCommand implements HttpCommand
{
	private static final String ATTACHMENT_HELPER = "sakai.filepicker";

	protected RWikiObjectService objectService;

	private String contentChangedPath;

	private String noUpdatePath;

	private String successfulPath;

	private String previewPath;

	private String cancelPath;

	@Getter @Setter
	private String groupsErrorPath;

	private SessionManager sessionManager;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		sessionManager = (SessionManager) load(cm, SessionManager.class
				.getName());
		objectService = (RWikiObjectService) load(cm, RWikiObjectService.class
				.getName());
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	public void execute(Dispatcher dispatcher, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);
		
		ResourceLoaderBean rlb = rssb.getResourceLoaderBean();

		ViewParamsHelperBean vphb = (ViewParamsHelperBean) rssb
				.getNameHelperBean();
		String tid = org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
		String content = vphb.getContent();
		String save = vphb.getSaveType();
		String name = vphb.getGlobalName();
		String realm = vphb.getLocalSpace();
		String[] pageGroups = vphb.getPageGroups();
		String displayTo = vphb.getDisplayTo();
		if (save == null)
		{
			save = EditBean.SAVE_VALUE;
		}
		if (save.equals(EditBean.OVERWRITE_VALUE))
		{
			content = vphb.getSubmittedContent();
			// Set the content as the submitted content in case we have a
			// version exception
			vphb.setContent(content);
		}
		else if (save.equals(EditBean.PREVIEW_VALUE))
		{
			vphb.setSaveState(ViewParamsHelperBean.SAVE_PREVIEW);
			this.previewDispatch(dispatcher,request, response);
			return;
		}
		else if (save.equals(EditBean.LINK_ATTACHMENT_VALUE)
				|| save.equals(EditBean.EMBED_ATTACHMENT_VALUE))
		{

			ToolSession session = sessionManager.getCurrentToolSession();

			Map parameterMap = request.getParameterMap();
			session.setAttribute("STORED_PARAMETERS", parameterMap);

			ViewBean vb = rssb.getViewBean();
			
			// FIXME Knowledge of URL structure assumed!
			WikiPageAction returnAction = WikiPageAction.LINK_ATTACHMENT_RETURN_ACTION;
			if (save.equals(EditBean.EMBED_ATTACHMENT_VALUE))
			{
				returnAction = WikiPageAction.EMBED_ATTACHMENT_RETURN_ACTION;
			}
			
			session.setAttribute(ATTACHMENT_HELPER + Tool.HELPER_DONE_URL,
					request.getContextPath() + request.getServletPath()
					+ vb.getActionUrl(returnAction, true));

			session.setAttribute(FilePickerHelper.FILE_PICKER_ATTACH_LINKS,
					FilePickerHelper.FILE_PICKER_ATTACH_LINKS);

			String fromText;
			

			if (returnAction
					.equals(WikiPageAction.LINK_ATTACHMENT_RETURN_ACTION))
			{
				fromText = vb.getLocalName() + rlb.getString("save.as_link"," as link");
			}
			else
			{
				fromText = vb.getLocalName() + rlb.getString("save.as_embed"," as embed");
			}

			session.setAttribute(FilePickerHelper.FILE_PICKER_FROM_TEXT,
					fromText);

			response.sendRedirect(request.getContextPath()
					+ request.getServletPath() + "/helper/" + ATTACHMENT_HELPER + "/tool");

			return;
		}
		else if (save.equals(EditBean.CANCEL_VALUE))
		{
			vphb.setSaveState(ViewParamsHelperBean.SAVE_CANCEL);
			this.cancelDispatch(dispatcher,request, response);
			ViewBean vb = new ViewBean(name, realm);
			String requestURL = request.getRequestURL().toString();
			sessionManager.getCurrentToolSession().setAttribute(
					RWikiServlet.SAVED_REQUEST_URL,
					requestURL + vb.getViewUrl());
			return;
		}

		String version = vphb.getSubmittedVersion();
		Date versionDate = new Date(Long.parseLong(version));

		if (StringUtils.equals("groupsel", displayTo) && pageGroups == null) {
			vphb.setSaveState(ViewParamsHelperBean.SAVE_GROUPS_ERROR);
			this.groupsErrorDispatch(dispatcher,request, response);
			return;
		}
		
		try
		{
			doUpdate(name, realm, versionDate, content, ((pageGroups != null)? new ArrayList<String>(Arrays.asList(pageGroups)) : new ArrayList<String>()), displayTo);
		}
		catch (VersionException e)
		{
			// The page has changed underneath us...

			// redirect probably back to the edit page
			vphb.setSaveState(ViewParamsHelperBean.SAVE_VERSION_EXCEPTION);
			this.contentChangedDispatch(dispatcher,request, response);
			return;
		}
		catch (PermissionException e)
		{
			// Redirect back to a no permission page...
			this.noUpdateAllowed(dispatcher,request, response);
			return;
		}
		// Successful update
		vphb.setSaveState(ViewParamsHelperBean.SAVE_OK);
		this.successfulUpdateDispatch(dispatcher,request, response);

		// Strip comment numbers off the page name if we're creating/editing a comment
		// This is to ensure that the redirect refreshes the page the comment exists on, rather than rendering a page for the comment itself
		if (this instanceof CommentSaveCommand || this instanceof CommentNewCommand)
		{
			// We can't have repeating capture groups, so we have to iterate.
			// There could be any number of ".###" group occurances at the end of the name, ex:
			// /site/e990d9a6-fdae-4126-b8a8-4a9994642bb9/home.005.006
			// Which indicates this is a comment reply (id 006) to a comment (id 005) on the "home" page
			Pattern pattern = Pattern.compile("^(.*)(\\.\\d\\d\\d)");
			Matcher matcher;
			do
			{
				matcher = pattern.matcher(name);
				if (matcher.matches())
				{
					name = matcher.group(1);
				}
				else
				{
					break;
				}
			} while (matcher.matches());
		}

		ViewBean vb = new ViewBean(name, realm);
		String requestURL = request.getRequestURL().toString();

		// Use PRG pattern to avoid problems of refreshing the page submitting the form again, duplicating the comment.
		response.sendRedirect(requestURL + vb.getViewUrl());
	}

	protected void doUpdate(String name, String realm, Date versionDate,
			String content, List<String> pageGroups, String displayTo)
	{
		if (pageGroups != null && StringUtils.equals("groupsel", displayTo)) {
			objectService.update(name, realm, versionDate, content, pageGroups, null);
		} else if (StringUtils.equals("sitesel", displayTo)) {
			objectService.update(name, realm, versionDate, content, new ArrayList<String>(), null);
		} else {
			objectService.update(name, realm, versionDate, content);
		}
	}

	private void cancelDispatch(Dispatcher dispatcher,HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(cancelPath,request, response);
	}

	private void previewDispatch(Dispatcher dispatcher,HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(previewPath,request, response);
	}
	


	protected void successfulUpdateDispatch(Dispatcher dispatcher,HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		dispatcher.dispatch(successfulPath,request, response);
	}

	private void contentChangedDispatch(Dispatcher dispatcher,HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		ErrorBean errorBean = ErrorBeanHelper.getErrorBean(request);
		ResourceLoaderBean rlb = ResourceLoaderHelperBean.getResourceLoader(request);
		errorBean
				.addError(rlb.getString("save.content_changed","Content has changed since you last viewed it. Please update the new content or overwrite it with the submitted content."));
		dispatcher.dispatch(contentChangedPath,request, response);
	}

	
	private void groupsErrorDispatch(Dispatcher dispatcher,HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		ErrorBean errorBean = ErrorBeanHelper.getErrorBean(request);
		ResourceLoaderBean rlb = ResourceLoaderHelperBean.getResourceLoader(request);
		errorBean.addError(rlb.getString("save.content_no_groups", "You need to choose at least one group"));
		dispatcher.dispatch(groupsErrorPath,request, response);
	}

	private void noUpdateAllowed(Dispatcher dispatcher,HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		ErrorBean errorBean = ErrorBeanHelper.getErrorBean(request);
		ResourceLoaderBean rlb = ResourceLoaderHelperBean.getResourceLoader(request);
		errorBean.addError(rlb.getString("save.noupdate_permission","You do not have permission to update this page."));
		dispatcher.dispatch(noUpdatePath,request, response);
	}

	public String getSuccessfulPath()
	{
		return successfulPath;
	}

	public void setSuccessfulPath(String successfulPath)
	{
		this.successfulPath = successfulPath;
	}

	public String getContentChangedPath()
	{
		return contentChangedPath;
	}

	public void setContentChangedPath(String contentChangedPath)
	{
		this.contentChangedPath = contentChangedPath;
	}

	public String getNoUpdatePath()
	{
		return noUpdatePath;
	}

	public void setNoUpdatePath(String noUpdatePath)
	{
		this.noUpdatePath = noUpdatePath;
	}

	public String getPreviewPath()
	{
		return previewPath;
	}

	public void setPreviewPath(String previewPath)
	{
		this.previewPath = previewPath;
	}

	public String getCancelPath()
	{
		return cancelPath;
	}

	public void setCancelPath(String cancelPath)
	{
		this.cancelPath = cancelPath;
	}

}
