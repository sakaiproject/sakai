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
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ToolManager;

import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.SearchBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;

/**
 * @author andrew
 */
@Slf4j
public class AddAttachmentReturnCommand implements HttpCommand
{
	private static final String MULTIPLE_ATTACHMENT_HEADER_START = "";

	private static final String MULTIPLE_ATTACHMENT_HEADER_BODY = "";

	private static final String MULTIPLE_ATTACHMENT_HEADER_END = "";

	private static final String MULTIPLE_ATTACHMENT_ITEM = "\n ";

	private static final String MULTIPLE_ATTACHMENT_ITEMS_END = "\n";

	private SessionManager sessionManager;

	private Map wikiMarkupTemplates;

	private String editPath;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		sessionManager = (SessionManager) load(cm, SessionManager.class
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void execute(Dispatcher dispatcher,HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);
		ToolSession session = sessionManager.getCurrentToolSession();

		// TODO EEK! constantise this
		Map storedParameters = (Map) session.getAttribute("STORED_PARAMETERS");

		String content = retrieveString(storedParameters
				.get(EditBean.CONTENT_PARAM));
		content = content.replaceAll("\r\n?", "\n");
		String caretPosition = retrieveString(storedParameters
				.get(EditBean.STORED_CARET_POSITION));
		String pageName = retrieveString(storedParameters
				.get(ViewBean.PAGE_NAME_PARAM));

		List refs = (List) session
				.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);

		if (session.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null
				&& refs != null && refs.size() > 0)
		{
			// File Picking was successful.
			content = generateNewContent(refs, content, caretPosition);
		}
		else
		{
			ErrorBean errorBean = rssb.getErrorBean();
			ResourceLoaderBean rlb = rssb.getResourceLoaderBean();
			errorBean.addError(rlb.getString("addattacheret.cancel_add_attach","Cancelled add attachment"));
		}

		// Eek! I think we need a better way of doing this...
		ViewParamsHelperBean vphb = rssb.getNameHelperBean();

		vphb.setContent(content);
		vphb.setGlobalName(pageName);
		vphb.setLocalSpace(retrieveString(storedParameters
				.get(SearchBean.REALM_PARAM)));
		vphb.setSaveType(vphb.getSaveTypeFromParameters(storedParameters));
		vphb.setWithBreadcrumbs(retrieveString(storedParameters
				.get(ViewBean.PARAM_BREADCRUMB_NAME)));
		vphb.setSubmittedVersion(retrieveString(storedParameters
				.get(EditBean.VERSION_PARAM)));
		vphb.setSubmittedContent(retrieveString(storedParameters
				.get(EditBean.SUBMITTED_CONTENT_PARAM)));
		// FIXME sort out caretPosition

		session.removeAttribute(Tool.HELPER_DONE_URL);
		session.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
		session.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);

		// finally dispatch to edit
		dispatcher.dispatch(editPath, request, response);

	}

	private String generateNewContent(List refs, String content,
			String caretPosition)
	{
		char[] charContent = content.toCharArray();

		int startPos = charContent.length;
		int endPos = charContent.length;

		if (caretPosition == null)
		{
			caretPosition = "";
		}

		String[] caretPositions = caretPosition.split(":");

		if (caretPositions.length > 0)
		{
			try
			{
				startPos = Integer.parseInt(caretPositions[0]);
			}
			catch (NumberFormatException e)
			{
				startPos = charContent.length;
			}
		}

		if (caretPositions.length > 1)
		{
			try
			{
				endPos = Integer.parseInt(caretPositions[1]);
			}
			catch (NumberFormatException e)
			{
				endPos = startPos;
			}
		}
		else
		{
			endPos = startPos;
		}

		if (startPos > charContent.length || startPos < 0)
		{
			startPos = charContent.length;
		}

		if (endPos > charContent.length || endPos < 0)
		{
			endPos = charContent.length;
		}

		if (endPos < startPos)
		{
			// I did have the XOR trick here, but I'm sure that's the JIT's job.
			int temp = startPos;
			startPos = endPos;
			endPos = temp;
		}

		if (refs.size() > 1)
		{
			return multipleRefsContent(charContent, startPos, endPos, refs);
		}
		else
		{
			return singleRefContent(charContent, startPos, endPos,
					(Reference) refs.get(0));
		}

	}

	private String retrieveString(Object fromMap)
	{
		if (fromMap == null)
		{
			return null;
		}
		if (fromMap instanceof String[])
		{
			String[] array = (String[]) fromMap;

			return array[0];

		}
		return fromMap.toString();
	}

	private String singleRefContent(char[] charContent, int startPos,
			int endPos, Reference ref)
	{
		ResourceProperties refProps = ref.getProperties();
		String contentType = refProps.getProperty(refProps
				.getNamePropContentType());
		String template = getTemplate(contentType);
		Object[] args = getTemplateArgs(ref);

		if (startPos != endPos)
		{
			// We have to use a different name for the "link"
			args[2] = new String(charContent, startPos, endPos - startPos);
		}

		// Work out length properly
		StringBuffer newContent = new StringBuffer(charContent.length
				+ template.length());

		newContent.append(charContent, 0, startPos);

		(new MessageFormat(template)).format(args, newContent, null);

		if (endPos < charContent.length)
		{
			newContent.append(charContent, endPos, charContent.length - endPos);
		}
		return newContent.toString();
	}

	private String multipleRefsContent(char[] charContent, int startPos,
			int endPos, List refs)
	{
		StringBuffer newContent = new StringBuffer(charContent.length);
		newContent.append(charContent, 0, startPos);

		newContent.append(MULTIPLE_ATTACHMENT_HEADER_START);
		if (startPos != endPos)
		{
			newContent.append(charContent, startPos, endPos - startPos);
		}
		else
		{
			newContent.append(MULTIPLE_ATTACHMENT_HEADER_BODY);
		}
		newContent.append(MULTIPLE_ATTACHMENT_HEADER_END);

		for (Iterator it = refs.iterator(); it.hasNext();)
		{
			Reference ref = (Reference) it.next();
			ResourceProperties refProps = ref.getProperties();
			String contentType = refProps.getProperty(refProps
					.getNamePropContentType());
			String template = getTemplate(contentType);
			Object[] args = getTemplateArgs(ref);
			newContent.append(MULTIPLE_ATTACHMENT_ITEM);
			(new MessageFormat(template)).format(args, newContent, null);

		}
		newContent.append(MULTIPLE_ATTACHMENT_ITEMS_END);

		if (endPos < charContent.length)
		{
			newContent.append(charContent, endPos, charContent.length - endPos);
		}

		return newContent.toString();
	}

	private Object[] getTemplateArgs(Reference ref)
	{
		ResourceProperties refProps = ref.getProperties();

		String name = refProps.getProperty(refProps.getNamePropDisplayName());
		String contentType = refProps.getProperty(refProps
				.getNamePropContentType());
		String referenceString = ref.getReference();
		String url = ref.getUrl();

		Object[] templateArguments = new Object[5];
		templateArguments[0] = url;
		templateArguments[2] = name;
		templateArguments[3] = contentType;
		templateArguments[4] = referenceString;

		// Interpret url as either worksite:/ or sakai:/ or http:/
		String currentSiteId = ToolManager.getCurrentPlacement().getContext();
		if (referenceString.startsWith("/content/group/"))
		{
			url = referenceString.substring("/content/group/".length());
			if (url.startsWith(currentSiteId))
			{
				url = "worksite:/" + url.substring(currentSiteId.length()+1);
			}
			else
			{
				url = "sakai:/" + url;
			}
		}
		else if (referenceString.startsWith("/content/group-user/"))
		{
			url = referenceString.substring("/content/group-user/".length());
			if (url.startsWith(currentSiteId))
			{
				url = "dropbox:/" + url.substring(currentSiteId.length()+1);
			}
			else
			{
				url = "sakai-dropbox:/" + url;
			}
		}
		else if (referenceString.startsWith("/content/user/"))
		{
			url = referenceString.substring("/content/user/".length());
			if ( url.startsWith("~") ) {
				url = url.substring(1);
			}
			if ( currentSiteId.startsWith("~") ) {
				currentSiteId = currentSiteId.substring(1);
			}
			if (url.startsWith(currentSiteId))
			{
				url = "worksite:/" + url.substring(currentSiteId.length()+1);
			}
			else
			{
				url = "sakai:/~" + url;
			}
		}

		templateArguments[1] = url;
		return templateArguments;
	}

	public String getTemplate(String contentType)
	{
		if (wikiMarkupTemplates.containsKey(contentType))
		{
			return (String) wikiMarkupTemplates.get(contentType);
		}
		int slash = contentType.indexOf('/');
		if (slash > -1)
		{
			String key = contentType.substring(0, slash) + "/*";

			if (wikiMarkupTemplates.containsKey(key))
			{
				return (String) wikiMarkupTemplates.get(key);
			}
		}

		return (String) wikiMarkupTemplates.get("*/*");
	}

	public Map getWikiMarkupTemplates()
	{
		return wikiMarkupTemplates;
	}

	public void setWikiMarkupTemplates(Map wikiMarkupTemplates)
	{
		this.wikiMarkupTemplates = wikiMarkupTemplates;
	}

	

	public String getEditPath()
	{
		return editPath;
	}

	public void setEditPath(String editPath)
	{
		this.editPath = editPath;
	}

}
