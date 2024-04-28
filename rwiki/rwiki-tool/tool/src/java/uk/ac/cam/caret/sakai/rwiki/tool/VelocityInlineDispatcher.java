/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.tool;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.velocity.util.SLF4JLogChute;

import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher;
import uk.ac.cam.caret.sakai.rwiki.utils.UserDisplayHelper;

/**
 * @author ieb
 */
@Slf4j
public class VelocityInlineDispatcher implements Dispatcher
{
	private static final String MACROS = "/WEB-INF/vm/macros.vm";

	private VelocityEngine vengine;

	private String inlineMacros;

	private String basePath;

	private VelocityUtilBean utilBean = new VelocityUtilBean();

	public void init(ServletContext context) throws ServletException
	{
		inlineMacros = MACROS;
		try
		{
			vengine = new VelocityEngine();

			vengine.setApplicationAttribute(ServletContext.class.getName(), context);
			vengine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute());

			Properties p = new Properties();
			p.load(this.getClass().getResourceAsStream("rwikivelocity.config"));
			vengine.init(p);
			vengine.getTemplate(inlineMacros);

		}
		catch (Exception ex)
		{
			throw new ServletException(ex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher#dispatch(java.lang.String,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	public void dispatch(String path, HttpServletRequest request,
			HttpServletResponse response) throws ServletException
	{
		VelocityContext vcontext = new VelocityContext();

		// EventCartridge ec = new EventCartridge();
		// ec.addEventHandler(new ExcludeEscapeHtmlReference());
		// ec.attachToContext(vcontext);
		RequestScopeSuperBean requestScopeSuperBean =  RequestScopeSuperBean.getFromRequest(request);

		vcontext.put("session", request.getSession());
		vcontext.put("request", request);
		vcontext.put("requestScope", requestScopeSuperBean);
		vcontext.put("util", utilBean);

		String localPage = requestScopeSuperBean.getViewBean().getLocalName();
		String homePage = requestScopeSuperBean.getHomeBean().getHomeLinkValue();
		try {
			if (!StringUtils.equalsIgnoreCase(homePage, localPage) && requestScopeSuperBean.hasAdminPermission()) {
				Collection<Group> groups = requestScopeSuperBean.getGroups();
				vcontext.put("groups", groups);
			}
		} catch (Exception ex) {
			log.warn("Failing getting the AdminPermissions");
		}
		
		List<String> rwikiObjectGroups = null;
		try {
			rwikiObjectGroups = requestScopeSuperBean.getCurrentRWikiObject().getPageGroupsAsList();
		} catch(Exception ex) {
			log.warn("Failing getting the groups from the current RwikiObject");
		}
		if (rwikiObjectGroups != null) {
			vcontext.put("rwikiObjectGroups", (rwikiObjectGroups.toArray(new String[rwikiObjectGroups.size()])));
		}
		try
		{
			String filePath = path + ".vm";
			response.setContentType("text/html");
			response.setCharacterEncoding("UTF-8");
			vengine.mergeTemplate(filePath, vcontext, response.getWriter());
		}
		catch (MethodInvocationException e)
		{
			Throwable cause = e.getWrappedThrowable();
			if (cause instanceof PermissionException)
			{
				try
				{
					String filePath = "/WEB-INF/vm/permission.vm";
					response.setContentType("text/html");
					response.setCharacterEncoding("UTF-8");
					vengine.mergeTemplate(filePath, vcontext, response.getWriter());
				}
				catch (Exception ex)
				{
					throw new ServletException(ex);
				}
			}
			else
			{
				throw new ServletException(e);

			}

		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	public class VelocityUtilBean
	{

		public String escapeHtml(String val)
		{
			return ComponentManager.get(FormattedText.class).escapeHtml(val, false);
		}

		public String escapeHtmlFormattedText(String val)
		{
			return ComponentManager.get(FormattedText.class).escapeHtmlFormattedText(val);
		}

		public String formatDisplayName(String name)
		{
			if (name == null)
			{
				return "unknown";
			}
			return UserDisplayHelper.formatDisplayName(name, null);
		}

		public String formatDateTime(Date date, HttpServletRequest request)
		{
			if (date == null)
			{
				return "unkown-date";
			}
			ResourceLoader rl = new ResourceLoader();
			DateFormat formatter = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG, rl.getLocale() );
			formatter.setTimeZone(TimeService.getLocalTimeZone());
			return formatter.format(date);
		}

		public ViewBean newViewBean(RequestScopeSuperBean rssb)
		{
			ViewBean vb = new ViewBean();
			vb.setLocalSpace(rssb.getCurrentLocalSpace());
			return vb;
		}

	}

}
