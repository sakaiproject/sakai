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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.sakaiproject.util.Web;

import uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ResourceLoaderHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher;
import uk.ac.cam.caret.sakai.rwiki.utils.UserDisplayHelper;

/**
 * @author ieb
 *
 */
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
			
			vengine.setApplicationAttribute(ServletContext.class.getName(),
					context);


			Properties p = new Properties();
			p.load(this.getClass().getResourceAsStream("rwikivelocity.properties"));
			vengine.init(p);
		}
		catch (Exception ex)
		{
			throw new ServletException(ex);
		}
	}


	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rwiki.tool.command.Dispatcher#dispatch(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public void dispatch(String path, HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		VelocityContext vcontext = new VelocityContext();
		vcontext.put("session", request.getSession());
		vcontext.put("request",request);
		vcontext.put("requestScope",RequestScopeSuperBean.getFromRequest(request));
		vcontext.put("util",utilBean);
		try
		{
			vengine.getTemplate(inlineMacros);
			String filePath = path + ".vm";
			vengine.mergeTemplate(filePath,
					vcontext,
					response.getWriter());
		}
		catch (Exception e)
		{
			
			throw new ServletException(e);
		}
	}
	
	public class VelocityUtilBean {
		
		public String escapeHtml(String val) {
			return Web.escapeHtml(val);
		}
		public String formatDisplayName(String name) {
			if ( name == null ) {
				return "unknown";
			}
			return UserDisplayHelper.formatDisplayName(name);
		}
		public String formatDateTime(Date date, HttpServletRequest request) {
			if ( date == null ) {
				return "unkown-date";
			}
			ResourceLoaderBean rlb = ResourceLoaderHelperBean.getResourceLoader(request);
			DateFormat formatter = new SimpleDateFormat(rlb.getString("date_format"));
			return formatter.format(date);
		}
		public ViewBean newViewBean(RequestScopeSuperBean rssb) {
			ViewBean vb = new ViewBean();
			vb.setLocalSpace(rssb.getCurrentLocalSpace());
			return vb;
		}

	}

}
