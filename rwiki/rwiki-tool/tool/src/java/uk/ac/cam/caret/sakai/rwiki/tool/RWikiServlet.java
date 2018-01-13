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
package uk.ac.cam.caret.sakai.rwiki.tool;
import org.sakaiproject.component.cover.ServerConfigurationService;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PrePopulateBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher;
import uk.ac.cam.caret.sakai.rwiki.tool.command.SaveCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;
import uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger;

/**
 * @author andrew
 */
@Slf4j
public class RWikiServlet extends HttpServlet
{

	/**
	 * Required for serialization... also to stop eclipse from giving me a
	 * warning!
	 */
	private static final long serialVersionUID = 676743152200357706L;

	public static final String SAVED_REQUEST_URL = "uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet.last-request-url";

	private WebApplicationContext wac;

	private String headerPreContent;

	private String headerScriptSource;

	private String footerScript;
	
	private Dispatcher dispatcher = null;

	public void init(ServletConfig servletConfig) throws ServletException
	{

		super.init(servletConfig);

		ServletContext sc = servletConfig.getServletContext();

		wac = WebApplicationContextUtils.getWebApplicationContext(sc);
		headerPreContent = servletConfig.getInitParameter("headerPreContent");
		headerScriptSource = servletConfig
				.getInitParameter("headerScriptSource");
		footerScript = servletConfig.getInitParameter("footerScript");
		try
		{
			boolean logResponse = "true".equalsIgnoreCase(servletConfig
					.getInitParameter("log-response"));
			TimeLogger.setLogResponse(logResponse);
		}
		catch (Exception ex)
		{

		}
		try
		{
			boolean logFullResponse = "true".equalsIgnoreCase(servletConfig
					.getInitParameter("log-full-response"));
			TimeLogger.setLogFullResponse(logFullResponse);
		}
		catch (Exception ex)
		{

		}
		
		
		String basePath = servletConfig.getServletContext().getRealPath("/");
		dispatcher = new MapDispatcher(sc);

	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		try {
			execute(request, response);
		} finally {
			RequestScopeSuperBean.clearInstance();
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		try {
			execute(request, response);
		} finally {
			RequestScopeSuperBean.clearInstance();
		}
	}

	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
	
	
		if (wac == null)
		{
			wac = WebApplicationContextUtils
					.getRequiredWebApplicationContext(this.getServletContext());
			if (wac == null)
			{
				response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
						"Cannot get WebApplicationContext");
				return;
			}
		}
		log.debug("========================Page Start==========");
		request.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);
		
		String targetURL = persistState(request);

		String action = request.getParameter ("action");
		if (!StringUtils.isEmpty (targetURL) && !StringUtils.equals (action, "search") && !StringUtils.equals (action, "full_search")) 
		{
			response.sendRedirect(targetURL);
 			return;
		}

		// Must be done on every request
		prePopulateRealm(request);

		addWikiStylesheet(request);

		request.setAttribute("footerScript", footerScript);
		if ( headerScriptSource != null && headerScriptSource.length() > 0 ) 
		{
			request.setAttribute("headerScriptSource", headerScriptSource);
		}

		RequestHelper helper = (RequestHelper) wac.getBean(RequestHelper.class
				.getName());

		HttpCommand command = helper.getCommandForRequest(request);
		
		// fix for IE6's poor cache capabilities
		String userAgent = request.getHeader("User-Agent");
		if ( userAgent != null && userAgent.indexOf("MSIE 6") >= 0 ) {
			response.addHeader("Expires","0");
			response.addHeader("Pragma","cache");
			response.addHeader("Cache-Control","private");
		}
		
		command.execute(dispatcher,request, response);

		request.removeAttribute(Tool.NATIVE_URL);
		log.debug("=====================Page End=============");
	}

	public void prePopulateRealm(HttpServletRequest request)
	{
		RequestScopeSuperBean rssb = RequestScopeSuperBean.createAndAttach(
				request, wac);
		
		
		

		PrePopulateBean ppBean = rssb.getPrePopulateBean();

		ppBean.doPrepopulate();
	}

	public void addWikiStylesheet(HttpServletRequest request)
	{
		String sakaiHeader = (String) request.getAttribute("sakai.html.head");
		request.setAttribute("sakai.html.head", headerPreContent + sakaiHeader);
	}

	/**
	 * returns the request state for the tool. If the state is restored, we set
	 * the request attribute RWikiServlet.REQUEST_STATE_RESTORED to Boolean.TRUE
	 * and a Thread local named RWikiServlet.REQUEST_STATE_RESTORED to
	 * Boolean.TRUE. These MUST be checked by anything that modifies state, to
	 * ensure that a reinitialisation of Tool state does not result in a repost
	 * of data.
	 * 
	 * @param request
	 * @return
	 */
	private String persistState(HttpServletRequest request)
	{
		ToolSession ts = SessionManager.getCurrentToolSession();
		if (isPageToolDefault(request))
		{
			if (log.isDebugEnabled())
			{
				log.debug("Incomming URL is " + request.getRequestURL().toString() + "?" + request.getQueryString());
				log.debug("Restore " + ts.getAttribute(SAVED_REQUEST_URL));
			}
			return (String) ts.getAttribute(SAVED_REQUEST_URL);
		}
		if (isPageRestorable(request))
		{
			ts.setAttribute(SAVED_REQUEST_URL, request.getRequestURL()
					.toString()
					+ "?" + request.getQueryString());
			if (log.isDebugEnabled())
			{
				log.debug("Saved " + ts.getAttribute(SAVED_REQUEST_URL));
			}
		}
		return null;
	}

	/**
	 * Check to see if the reques represents the Tool default page. This is not
	 * the same as the view Home. It is the same as first entry into a Tool or
	 * when the page is refreshed
	 * 
	 * @param request
	 * @return true if the page is the Tool default page
	 */
	// XXX this should not be here!! The RequestHelper should perform this
	// functionality.
	private boolean isPageToolDefault(HttpServletRequest request)
	{
		// SAK-13408 - Tomcat and WAS have different URL structures; Attempting to add a 
		// link or image would lead to site unavailable errors in websphere if the tomcat
		// URL structure is used.
		if("websphere".equals(ServerConfigurationService.getString("servlet.container"))){
			String tid = org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
			if ( request.getPathInfo() != null && request.getPathInfo().startsWith("/tool/" + tid + "/helper/") ) {
				return false;
			}
		}
		else {
			if ( request.getPathInfo() != null && request.getPathInfo().startsWith("/helper/") ) {
				return false;
			}
		}
		
		String action = request.getParameter(RequestHelper.ACTION);
		if (action != null && action.length() > 0) {
			return false;
		}

		String pageName = request.getParameter(ViewBean.PAGE_NAME_PARAM);
		if  (pageName == null || pageName.trim().length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Check to see if the request represents a page that can act as a restor
	 * point.
	 * 
	 * @param request
	 * @return true if it is possible to restore to this point.
	 */
	private boolean isPageRestorable(HttpServletRequest request)
	{
		if (RequestHelper.TITLE_PANEL.equals(request
				.getParameter(RequestHelper.PANEL))) return false;

		if (WikiPageAction.PUBLICVIEW_ACTION.getName().equals(
				request.getParameter(RequestHelper.ACTION))) return false;

		
		if ("GET".equalsIgnoreCase(request.getMethod())) return true;

		return false;
	}

}
