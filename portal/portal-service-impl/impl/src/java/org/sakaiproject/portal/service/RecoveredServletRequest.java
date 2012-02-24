/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.portal.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * @author ieb
 * @since Sakai 2.4
 * @version $Rev$
 */

public class RecoveredServletRequest extends HttpServletRequestWrapper
{

	private SessionRequestHolder holder = null;

	public RecoveredServletRequest(HttpServletRequest request,
			SessionRequestHolder requestHolder)
	{
		super(request);
		this.holder = requestHolder;
	}

	@Override
	public String getAuthType()
	{
		return super.getAuthType();
	}

	@Override
	public String getContextPath()
	{
		return super.getContextPath();
	}

	@Override
	public Cookie[] getCookies()
	{
		return super.getCookies();
	}

	@Override
	public long getDateHeader(String arg0)
	{
		return super.getDateHeader(arg0);
	}

	@Override
	public String getHeader(String arg0)
	{
		return super.getHeader(arg0);
	}

	@Override
	public Enumeration getHeaderNames()
	{
		return super.getHeaderNames();
	}

	@Override
	public Enumeration getHeaders(String arg0)
	{
		return super.getHeaders(arg0);
	}

	@Override
	public int getIntHeader(String arg0)
	{
		return super.getIntHeader(arg0);
	}

	@Override
	public String getMethod()
	{
		return holder.getMethod();
	}

	@Override
	public String getPathInfo()
	{
		return super.getPathInfo();
	}

	@Override
	public String getPathTranslated()
	{
		return super.getPathTranslated();
	}

	@Override
	public String getQueryString()
	{
		return holder.getQueryString();
	}

	@Override
	public String getRemoteUser()
	{
		return super.getRemoteUser();
	}

	@Override
	public String getRequestedSessionId()
	{
		return super.getRequestedSessionId();
	}

	@Override
	public String getRequestURI()
	{
		return super.getRequestURI();
	}

	@Override
	public StringBuffer getRequestURL()
	{
		return super.getRequestURL();
	}

	@Override
	public String getServletPath()
	{
		return super.getServletPath();
	}

	@Override
	public HttpSession getSession()
	{
		return super.getSession();
	}

	@Override
	public HttpSession getSession(boolean arg0)
	{
		return super.getSession(arg0);
	}

	@Override
	public Principal getUserPrincipal()
	{
		return super.getUserPrincipal();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie()
	{
		return super.isRequestedSessionIdFromCookie();
	}

	@Override
	public boolean isRequestedSessionIdFromUrl()
	{
		return super.isRequestedSessionIdFromUrl();
	}

	@Override
	public boolean isRequestedSessionIdFromURL()
	{
		return super.isRequestedSessionIdFromURL();
	}

	@Override
	public boolean isRequestedSessionIdValid()
	{
		return super.isRequestedSessionIdValid();
	}

	@Override
	public boolean isUserInRole(String arg0)
	{
		return super.isUserInRole(arg0);
	}

	@Override
	public Object getAttribute(String arg0)
	{
		return super.getAttribute(arg0);
	}

	@Override
	public Enumeration getAttributeNames()
	{
		return super.getAttributeNames();
	}

	@Override
	public String getCharacterEncoding()
	{
		return super.getCharacterEncoding();
	}

	@Override
	public int getContentLength()
	{
		return super.getContentLength();
	}

	@Override
	public String getContentType()
	{
		return super.getContentType();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return super.getInputStream();
	}

	@Override
	public String getLocalAddr()
	{
		return super.getLocalAddr();
	}

	@Override
	public Locale getLocale()
	{
		return super.getLocale();
	}

	@Override
	public Enumeration getLocales()
	{
		return super.getLocales();
	}

	@Override
	public String getLocalName()
	{
		return super.getLocalName();
	}

	@Override
	public int getLocalPort()
	{
		return super.getLocalPort();
	}

	@Override
	public String getParameter(String arg0)
	{
		String value = holder.getParameter(arg0);
		return value;
	}

	@Override
	public Map getParameterMap()
	{
		Map m = holder.getParameterMap();
		for (Iterator i = m.keySet().iterator(); i.hasNext();)
		{
			Object key = i.next();
		}
		return m;

	}

	@Override
	public Enumeration getParameterNames()
	{
		return holder.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String arg0)
	{
		String[] values = holder.getParameterValues(arg0);
		return values;
	}

	private String valueOf(Object o)
	{
		if (o instanceof String[])
		{
			String[] values = (String[]) o;
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			for (int i = 0; i < values.length; i++)
			{
				if (i != 0)
				{
					sb.append(",");
				}
				sb.append(values[i]);
			}
			sb.append("}");
			return sb.toString();
		}
		else if (o instanceof String)
		{
			return (String) o;
		}
		else if (o != null)
		{
			return o.toString();
		}
		else
		{
			return null;
		}
	}

	@Override
	public String getProtocol()
	{
		return super.getProtocol();
	}

	@Override
	public BufferedReader getReader() throws IOException
	{
		return super.getReader();
	}

	@Override
	public String getRealPath(String arg0)
	{
		return super.getRealPath(arg0);
	}

	@Override
	public String getRemoteAddr()
	{
		return super.getRemoteAddr();
	}

	@Override
	public String getRemoteHost()
	{
		return super.getRemoteHost();
	}

	@Override
	public int getRemotePort()
	{
		return super.getRemotePort();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0)
	{
		RequestDispatcher dispatcher = super.getRequestDispatcher(arg0);
		return dispatcher;
	}

	@Override
	public String getScheme()
	{
		return super.getScheme();
	}

	@Override
	public String getServerName()
	{
		return super.getServerName();
	}

	@Override
	public int getServerPort()
	{
		return super.getServerPort();
	}

	@Override
	public boolean isSecure()
	{
		return super.isSecure();
	}

	@Override
	public void removeAttribute(String arg0)
	{
		super.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1)
	{
		super.setAttribute(arg0, arg1);
	}

	@Override
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException
	{
		super.setCharacterEncoding(arg0);
	}
}
