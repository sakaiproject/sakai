package org.sakaiproject.portal.charon;

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

public class RecoveredServletRequest extends HttpServletRequestWrapper
{

	private SessionRequestHolder holder = null;

	private String marker = null;

	private String replacement = null;

	public RecoveredServletRequest(HttpServletRequest request,
			SessionRequestHolder requestHolder, String marker, String replacement)
	{
		super(request);
		this.holder = requestHolder;
		this.marker = marker;
		this.replacement = replacement;
	}

	public String getAuthType()
	{
		return super.getAuthType();
	}

	public String getContextPath()
	{
		return super.getContextPath();
	}

	public Cookie[] getCookies()
	{
		return super.getCookies();
	}

	public long getDateHeader(String arg0)
	{
		return super.getDateHeader(arg0);
	}

	public String getHeader(String arg0)
	{
		return super.getHeader(arg0);
	}

	public Enumeration getHeaderNames()
	{
		return super.getHeaderNames();
	}

	public Enumeration getHeaders(String arg0)
	{
		return super.getHeaders(arg0);
	}

	public int getIntHeader(String arg0)
	{
		return super.getIntHeader(arg0);
	}

	public String getMethod()
	{
		return holder.getMethod();
	}

	public String getPathInfo()
	{
		return super.getPathInfo();
	}

	public String getPathTranslated()
	{
		return super.getPathTranslated();
	}

	public String getQueryString()
	{
		return holder.getQueryString();
	}

	public String getRemoteUser()
	{
		return super.getRemoteUser();
	}

	public String getRequestedSessionId()
	{
		return super.getRequestedSessionId();
	}

	public String getRequestURI()
	{
		return super.getRequestURI();
	}

	public StringBuffer getRequestURL()
	{
		return super.getRequestURL();
	}

	public String getServletPath()
	{
		return super.getServletPath();
	}

	public HttpSession getSession()
	{
		return super.getSession();
	}

	public HttpSession getSession(boolean arg0)
	{
		return super.getSession(arg0);
	}

	public Principal getUserPrincipal()
	{
		return super.getUserPrincipal();
	}

	public boolean isRequestedSessionIdFromCookie()
	{
		return super.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromUrl()
	{
		return super.isRequestedSessionIdFromUrl();
	}

	public boolean isRequestedSessionIdFromURL()
	{
		return super.isRequestedSessionIdFromURL();
	}

	public boolean isRequestedSessionIdValid()
	{
		return super.isRequestedSessionIdValid();
	}

	public boolean isUserInRole(String arg0)
	{
		return super.isUserInRole(arg0);
	}

	public Object getAttribute(String arg0)
	{
		return super.getAttribute(arg0);
	}

	public Enumeration getAttributeNames()
	{
		return super.getAttributeNames();
	}

	public String getCharacterEncoding()
	{
		return super.getCharacterEncoding();
	}

	public int getContentLength()
	{
		return super.getContentLength();
	}

	public String getContentType()
	{
		return super.getContentType();
	}

	public ServletInputStream getInputStream() throws IOException
	{
		return super.getInputStream();
	}

	public String getLocalAddr()
	{
		return super.getLocalAddr();
	}

	public Locale getLocale()
	{
		return super.getLocale();
	}

	public Enumeration getLocales()
	{
		return super.getLocales();
	}

	public String getLocalName()
	{
		return super.getLocalName();
	}

	public int getLocalPort()
	{
		return super.getLocalPort();
	}

	public String getParameter(String arg0)
	{
		String value = holder.getParameter(arg0);
		return value;
	}

	public Map getParameterMap()
	{
		Map m = holder.getParameterMap();
		for (Iterator i = m.keySet().iterator(); i.hasNext();)
		{
			Object key = i.next();
		}
		return m;

	}

	public Enumeration getParameterNames()
	{
		return holder.getParameterNames();
	}

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
			StringBuffer sb = new StringBuffer();
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

	public String getProtocol()
	{
		return super.getProtocol();
	}

	public BufferedReader getReader() throws IOException
	{
		return super.getReader();
	}

	public String getRealPath(String arg0)
	{
		return super.getRealPath(arg0);
	}

	public String getRemoteAddr()
	{
		return super.getRemoteAddr();
	}

	public String getRemoteHost()
	{
		return super.getRemoteHost();
	}

	public int getRemotePort()
	{
		return super.getRemotePort();
	}

	public RequestDispatcher getRequestDispatcher(String arg0)
	{
		// TODO Auto-generated method stub
		RequestDispatcher dispatcher = super.getRequestDispatcher(arg0);
		return dispatcher;
	}

	public String getScheme()
	{
		// TODO Auto-generated method stub
		return super.getScheme();
	}

	public String getServerName()
	{
		// TODO Auto-generated method stub
		return super.getServerName();
	}

	public int getServerPort()
	{
		// TODO Auto-generated method stub
		return super.getServerPort();
	}

	public boolean isSecure()
	{
		// TODO Auto-generated method stub
		return super.isSecure();
	}

	public void removeAttribute(String arg0)
	{
		// TODO Auto-generated method stub
		super.removeAttribute(arg0);
	}

	public void setAttribute(String arg0, Object arg1)
	{
		// TODO Auto-generated method stub
		super.setAttribute(arg0, arg1);
	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException
	{
		// TODO Auto-generated method stub
		super.setCharacterEncoding(arg0);
	}
}
