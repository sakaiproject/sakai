/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.driver.url.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Copied from mockrunner 0.3.8
 * Mock implementation of <code>HttpServletRequest</code>.
 */
public class MockHttpServletRequest implements HttpServletRequest
{
    private Map attributes;
    private Map parameters;
    private Vector locales;
    private Map requestDispatchers;
    private HttpSession session;
    private String method;
    private String authType;
    private Map headers;
    private String contextPath;
    private String pathInfo;
    private String pathTranslated;
    private String queryString;
    private StringBuffer requestUrl;
    private String requestUri;
    private String servletPath;
    private Principal principal;
    private String remoteUser;
    private boolean requestedSessionIdIsFromCookie;
    private String protocol;
    private String serverName;
    private int serverPort;
    private String scheme;
    private String remoteHost;
    private String remoteAddr;
    private Map roles;
    private String characterEncoding;
    private int contentLength;
    private String contentType;
    private List cookies;
    private String localAddr;
    private String localName;
    private int localPort;
    private int remotePort;
    private boolean sessionCreated;
    
    public MockHttpServletRequest()
    {
        resetAll();
    }

    /**
     * Resets the state of this object to the default values
     */
    public void resetAll()
    {
        this.attributes = new HashMap();
        this.parameters = new HashMap();
        this.locales = new Vector();
        this.requestDispatchers = new HashMap();
        this.method = "GET";
        headers = new HashMap();
        requestedSessionIdIsFromCookie = true;
        protocol = "HTTP/1.1";
        serverName = "localhost";
        serverPort = 8080;
        scheme = "http";
        remoteHost = "localhost";
        remoteAddr = "127.0.0.1";
        roles = new HashMap();
        contentLength = -1;
        cookies = new ArrayList();
        localAddr = "127.0.0.1";
        localName = "localhost";
        localPort = 8080;
        remotePort = 5000;
        sessionCreated = false;
    }
    
    public String getParameter(String key)
    {
        String[] values = getParameterValues(key);
        if (null != values && 0 < values.length)
        {
            return values[0];
        }
        return null;
    }
    
    public void clearParameters()
    {
        parameters.clear();
    }

    public String[] getParameterValues(String key)
    {
        return (String[])parameters.get(key);
    }

    public void setupAddParameter(String key, String[] values)
    {
        parameters.put(key, values);
    }

    public void setupAddParameter(String key, String value)
    {
        setupAddParameter(key, new String[] { value });
    }

    public Enumeration getParameterNames()
    {
        Vector parameterKeys = new Vector(parameters.keySet());
        return parameterKeys.elements();
    }

    public Map getParameterMap()
    {
        return Collections.unmodifiableMap(parameters);
    }
    
    public void clearAttributes()
    {
        attributes.clear();
    }

    public Object getAttribute(String key)
    {
        return attributes.get(key);
    }

    public Enumeration getAttributeNames()
    {
        Vector attKeys = new Vector(attributes.keySet());
        return attKeys.elements();
    }

    public void removeAttribute(String key)
    {
        Object value = attributes.get(key);
        attributes.remove(key);
        if(null != value)
        {
            //callAttributeListenersRemovedMethod(key, value);
        }
    }

    public void setAttribute(String key, Object value)
    {
        Object oldValue = attributes.get(key);
        if(null == value)
        {
            attributes.remove(key);
        }
        else
        {
            attributes.put(key, value);
        }
        //handleAttributeListenerCalls(key, value, oldValue);
    }
    
    public HttpSession getSession()
    {
        sessionCreated = true;
        return session; 
    }
    
    public HttpSession getSession(boolean create)
    {
        if(!create && !sessionCreated) return null;
        return getSession();
    }

    public void setSession(HttpSession session) 
    {
        this.session = session;   
    }

    public RequestDispatcher getRequestDispatcher(String path)
    {
    	return null;
    }
    
    /**
     * Returns the map of <code>RequestDispatcher</code> objects. The specified path
     * maps to the corresponding <code>RequestDispatcher</code> object.
     * @return the map of <code>RequestDispatcher</code> objects
     */
    public Map getRequestDispatcherMap()
    {
        return Collections.unmodifiableMap(requestDispatchers);
    }
    
    /**
     * Clears the map of <code>RequestDispatcher</code> objects. 
     */
    public void clearRequestDispatcherMap()
    {
        requestDispatchers.clear();
    }
    
    public void setRequestDispatcher(String path, RequestDispatcher dispatcher)
    {
    }
    
    public Locale getLocale()
    {
        if(locales.size() < 1) return Locale.getDefault();
        return (Locale)locales.get(0);
    }

    public Enumeration getLocales()
    {
        return locales.elements();
    }
    
    public void addLocale(Locale locale)
    {
        locales.add(locale);
    }
    
    public void addLocales(List localeList)
    {
        locales.addAll(localeList);
    }
    
    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }
    
    public String getAuthType()
    {
        return authType;
    }
    
    public void setAuthType(String authType)
    {
        this.authType = authType;
    }

    public long getDateHeader(String key)
    {
    	return -1;
    }

    public String getHeader(String key)
    {
        List headerList = (List)headers.get(key);
        if(null == headerList || 0 == headerList.size()) return null;
        return (String)headerList.get(0);
    }

    public Enumeration getHeaderNames()
    {
        return new Vector(headers.keySet()).elements();
    }

    public Enumeration getHeaders(String key)
    {
        List headerList = (List)headers.get(key);
        if(null == headerList) return null;
        return new Vector(headerList).elements();
    }

    public int getIntHeader(String key)
    {
        String header = getHeader(key);
        if(null == header) return -1;
        return new Integer(header).intValue();
    }
    
    public void addHeader(String key, String value)
    {
        List valueList = (List) headers.get(key);
        if (null == valueList)
        {
            valueList = new ArrayList();
            headers.put(key, valueList);
        }
        valueList.add(value);
    }
    
    public void setHeader(String key, String value)
    {
        List valueList = new ArrayList();
        headers.put(key, valueList);
        valueList.add(value);
    }
    
    public void clearHeaders()
    {
        headers.clear();
    }
    
    public String getContextPath()
    {
        return contextPath;
    }
    
    public void setContextPath(String contextPath)
    {
        this.contextPath = contextPath;
    }
    
    public String getPathInfo()
    {
        return pathInfo;
    }
    
    public void setPathInfo(String pathInfo)
    {
        this.pathInfo = pathInfo;
    }
    
    public String getPathTranslated()
    {
        return pathTranslated;
    }
    
    public void setPathTranslated(String pathTranslated)
    {
        this.pathTranslated = pathTranslated;
    }
    
    public String getQueryString()
    {
        return queryString;
    }
    
    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }
    
    public String getRequestURI()
    {
        return requestUri;
    }
    
    public void setRequestURI(String requestUri)
    {
        this.requestUri = requestUri;
    }
    
    public StringBuffer getRequestURL()
    {
        return requestUrl;
    }
    
    public void setRequestURL(String requestUrl)
    {
        this.requestUrl = new StringBuffer(requestUrl);
    }
    
    public String getServletPath()
    {
        return servletPath;
    }
    
    public void setServletPath(String servletPath)
    {
        this.servletPath = servletPath;
    }
    
    public Principal getUserPrincipal()
    {
        return principal;
    }
    
    public void setUserPrincipal(Principal principal)
    {
        this.principal = principal;
    }
    
    public String getRemoteUser()
    {
        return remoteUser;
    }

    public void setRemoteUser(String remoteUser)
    {
        this.remoteUser = remoteUser;
    }

    public Cookie[] getCookies()
    {
        return (Cookie[])cookies.toArray(new Cookie[cookies.size()]);
    }
    
    public void addCookie(Cookie cookie)
    {
        cookies.add(cookie);
    }

    public String getRequestedSessionId()
    {
        HttpSession session = getSession();
        if(null == session) return null;
        return session.getId();
    }

    public boolean isRequestedSessionIdFromCookie()
    {
        return requestedSessionIdIsFromCookie;
    }

    public boolean isRequestedSessionIdFromUrl()
    {
        return isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromURL()
    {
        return !requestedSessionIdIsFromCookie;
    }
    
    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdIsFromCookie)
    {
        this.requestedSessionIdIsFromCookie = requestedSessionIdIsFromCookie;
    }

    public boolean isRequestedSessionIdValid()
    {
        HttpSession session = getSession();
        if(null == session) return false;
        return true;
    }

    public boolean isUserInRole(String role)
    {
        return ((Boolean)roles.get(role)).booleanValue();
    }
    
    public void setUserInRole(String role, boolean isInRole)
    {
        roles.put(role, new Boolean(isInRole));
    }

    public String getCharacterEncoding()
    {
        return characterEncoding;
    }
    
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException
    {
        this.characterEncoding = characterEncoding;
    }

    public int getContentLength()
    {
        return contentLength;
    }
    
    public void setContentLength(int contentLength)
    {
        this.contentLength = contentLength;
    }

    public String getContentType()
    {
        return contentType;
    }
    
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public String getProtocol()
    {
        return protocol;
    }
    
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }
    
    public String getServerName()
    {
        return serverName;
    }
    
    public void setServerName(String serverName)
    {
        this.serverName = serverName;
    }
    
    public int getServerPort()
    {
        return serverPort;
    }
    
    public void setServerPort(int serverPort)
    {
        this.serverPort = serverPort;
    }
    
    public String getScheme()
    {
        return scheme;
    }
    
    public void setScheme(String scheme)
    {
        this.scheme = scheme;
    }
    
    public String getRemoteAddr()
    {
        return remoteAddr;
    }
    
    public void setRemoteAddr(String remoteAddr)
    {
        this.remoteAddr = remoteAddr;
    }
    
    public String getRemoteHost()
    {
        return remoteHost;
    }
    
    public void setRemoteHost(String remoteHost)
    {
        this.remoteHost = remoteHost;
    }

    public BufferedReader getReader() throws IOException
    {
        return null;
    }
    
    public ServletInputStream getInputStream() throws IOException
    {
        return null;
    }
    
    public void setBodyContent(byte[] data)
    {
    }
    
    public void setBodyContent(String bodyContent)
    {
    }

    public String getRealPath(String path)
    {
        HttpSession session = getSession();
        if(null == session) return null;
        return session.getServletContext().getRealPath(path);
    } 
    
    public boolean isSecure()
    {
        String scheme = getScheme();
        if(null == scheme) return false;
        return scheme.equals("https");
    }
    
    public String getLocalAddr()
    {
        return localAddr;
    }
    
    public void setLocalAddr(String localAddr)
    {
        this.localAddr = localAddr;
    }

    public String getLocalName()
    {
        return localName;
    }
    
    public void setLocalName(String localName)
    {
        this.localName = localName;
    }

    public int getLocalPort()
    {
        return localPort;
    }
    
    public void setLocalPort(int localPort)
    {
        this.localPort = localPort;
    }

    public int getRemotePort()
    {
        return remotePort;
    }

    public void setRemotePort(int remotePort)
    {
        this.remotePort = remotePort;
    }
}
