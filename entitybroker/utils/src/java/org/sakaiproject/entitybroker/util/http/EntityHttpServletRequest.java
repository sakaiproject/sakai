/**
 * $Id$
 * $URL$
 * CalloutHttpServletRequest.java - entity-broker - Dec 19, 2008 12:03:07 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.util.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.URLData;


/**
 * Makes a copy of the data in a servlet request into a new request object
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
public class EntityHttpServletRequest implements HttpServletRequest {

    /**
     * default protocol: http
     */
    public static final String DEFAULT_PROTOCOL = "http";
    /**
     * default server address: 127.0.0.1
     */
    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";
    /**
     * default server name: localhost
     */
    public static final String DEFAULT_SERVER_NAME = "localhost";
    /**
     * default server port: 80
     */
    public static final int DEFAULT_SERVER_PORT = 80;
    /**
     * default remote address: 127.0.0.1
     */
    public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";
    /**
     * The default remote host: 'localhost'.
     */
    public static final String DEFAULT_REMOTE_HOST = "localhost";

    public HttpServletRequest copy = null;

    ConcurrentHashMap<String, Vector<String>> headers = new ConcurrentHashMap<String, Vector<String>>();
    ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
    ConcurrentHashMap<String, String[]> parameters = new ConcurrentHashMap<String, String[]>();
    Vector<Cookie> cookies = new Vector<Cookie>();
    Locale locale = Locale.getDefault();
    String contentType = "text/plain";
    String characterEncoding = "UTF-8";
    InputStream contentStream = null;
    int contentLength = 0;

    String scheme = DEFAULT_PROTOCOL;
    String protocol = DEFAULT_PROTOCOL;
    String serverName = DEFAULT_SERVER_NAME;
    int serverPort = DEFAULT_SERVER_PORT;
    String remoteAddr = DEFAULT_REMOTE_ADDR;
    String remoteHost = DEFAULT_REMOTE_HOST;

    String method = "GET";
    String pathInfo = null;
    String contextPath = "";
    String queryString = null;
    String requestURI = "";
    String servletPath = "";

    /**
     * Create a new request from a given request and modify it based on the path string
     * @param req any request
     */
    public EntityHttpServletRequest(HttpServletRequest req) {
        copy = req;
        if (req != null) {
            setRequestValues(req);
        } else {
            // no request set, make some fake stuff?
        }
    }

    /**
     * Create a new request from a given request and modify it based on the path string and method
     * @param req any request
     * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
     * this will be set to POST if null or unset
     * @param pathString any path or URL
     */
    public EntityHttpServletRequest(HttpServletRequest req, String method, String pathString) {
        copy = req;
        if (req != null) {
            setRequestValues(req);
        } else {
            // no request set, make some fake stuff?
        }
        if (method == null || "".equals(method)) {
            setMethod("POST");
        }
        // setup the stuff based on the pathString
        setPathString(pathString);
    }

    /**
     * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
     * this will be set to POST if null or unset
     * @param pathInfo the part of the URL specifying extra path information that comes 
     * after the servlet path but before the query string in the request URL
     * Example: http://server/servlet/extra/path/info?thing=1, pathInfo = /extra/path/info
     */
    public EntityHttpServletRequest(String method, String pathInfo) {
        if (method == null || "".equals(method)) {
            setMethod("POST");
        }
        setPathInfo(pathInfo);
    }

    /**
     * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
     * this will be set to POST if null or unset
     * @param params alternating keys and values (starting with keys) to place into the request parameters
     */
    public EntityHttpServletRequest(String method, String... params) {
        if (method == null || "".equals(method)) {
            setMethod("POST");
        }
        for (int i = 0; i < params.length; i++) {
            if (params.length < i + 1) {
               break;
            }
            this.addParameter(params[i], params[i+1]);
            i++;
         }
    }


    /**
     * Set all the values from a request on this request object and set this request
     * as the one which the values were copied from
     * @param req any request
     */
    public void setRequestValues(HttpServletRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("request cannot be null");
        }
        // get the collections of values out
        Enumeration<String> attribNames = req.getAttributeNames();
        while (attribNames.hasMoreElements()) {
            String name = (String) attribNames.nextElement();
            Object obj = req.getAttribute(name);
            if (obj != null) {
                attributes.put(name, obj);
            }
        }
        Cookie[] ck = req.getCookies();
        for (int i = 0; i < ck.length; i++) {
            cookies.add(ck[i]);
        }
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> henum = req.getHeaders(name);
            Vector<String> v = new Vector<String>(1);
            while (henum.hasMoreElements()) {
                String h = henum.nextElement();
                v.add(h);
            }
        }
        for (Entry<String, String[]> entry : (Set<Entry<String, String[]>>) req.getParameterMap().entrySet()) {
            parameters.put(entry.getKey(), entry.getValue());
        }
        // get the basic values out
        locale = req.getLocale();
        method = req.getMethod();
        contentType = req.getContentType();
        characterEncoding = req.getCharacterEncoding() == null ? "UTF-8" : req.getCharacterEncoding();
        contentLength = req.getContentLength();

        scheme = req.getScheme();
        protocol = req.getProtocol();
        serverName = req.getServerName();
        serverPort = req.getServerPort();
        remoteAddr = req.getRemoteAddr();
        remoteHost = req.getRemoteHost();
    }
    /**
     * This will set the given url/path string values into this request
     * @param pathString any url or path string
     * @return the url data object based on the input object OR null if the string is null or empty
     */
    public URLData setPathString(String pathString) {
        URLData ud = null;
        if (pathString != null && pathString.length() > 1) {
            ud = HttpRESTUtils.parseURL(pathString);
            if (ud.pathInfo.length() > 0) {
                this.pathInfo = ud.pathInfo;
            } else {
                this.pathInfo = null;
            }
            if (ud.port.length() > 0) {
                try {
                    this.serverPort = new Integer(ud.port);
                } catch (NumberFormatException e) {
                    this.serverPort = 80;
                }
            }
            if (ud.protocol.length() > 0) {
                this.protocol = ud.protocol;
            }
            if (ud.query.length() > 0) {
                this.queryString = ud.query;
            } else {
                this.queryString = null;
            }
            if (ud.server.length() > 0) {
                this.serverName = ud.server;
            }
            if (ud.servletPath.length() > 0) {
                this.servletPath = ud.servletPath;
                this.contextPath = ud.servletPath;
            } else {
                this.servletPath = "";
                this.contextPath = "";
            }
        }
        return ud;
    }

    /**
     * Allows control over the content data which is used in this request,
     * all data should be UTF-8 encoded
     */
    public void setContent(InputStream contentStream, int contentLength) {
        if (contentStream == null) {
            contentLength = 0;
        }
        this.contentStream = contentStream;
        this.contentLength = contentLength;
    }

    /**
     * Allows control over the content data which is used in this request,
     * all data should be UTF-8 encoded
     * @param content any string content, UTF-8 encoded, replaces existing content
     */
    public void setContent(String content) {
        if (content == null) {
            content = "";
        }
        this.contentLength = content.length();
        this.contentStream = new BufferedInputStream( new ByteArrayInputStream(content.getBytes()) );
    }

    /**
     * Gets the content data out of the request, ensures that the data can be retrieved somehow
     * @return the content data from the request
     * @throws IOException if there is a failure
     */
    public ServletInputStream getContent() throws IOException {
        ServletInputStream sis = null;
        if (contentStream != null) {
            // wrap the existing one
            sis = new EntityServletInputStream(contentStream);
        } else if (copy != null) {
            // lazy load this out of the request
            sis = new LazyRequestInputStream(copy);
        } else {
            // provide a default one then
            sis = new EntityServletInputStream("");
        }
        return sis;
    }

    // API methods

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
    }

    /**
     * Remove all attributes
     */
    public void clearAttributes() {
        this.attributes.clear();
    }

    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        if (env == null || "".equals(env)) {
            env = "UTF-8";
        }
        this.characterEncoding = env;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * Sets the content type for this request content
     * @param contentType
     */
    public void setContentType(String contentType) {
        if (contentType == null || "".equals(contentType)) {
            contentType = "text/plain";
        }
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public ServletInputStream getInputStream() throws IOException {
        // lazy load this on request, don't use the reader
        return getContent();
    }

    public BufferedReader getReader() throws IOException {
        // lazy load this on request, don't use the reader
        return new BufferedReader( new InputStreamReader( getContent() ) );
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public void setContextPath(String contextPath) {
        if (contextPath == null) {
            throw new IllegalArgumentException("contextPath cannot be null");
        }
        this.contextPath = contextPath;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setPathInfo(String pathInfo) {
        if (pathInfo == null) {
            throw new IllegalArgumentException("pathInfo cannot be null");
        }
        this.pathInfo = pathInfo;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getPathTranslated() {
        return copy.getPathTranslated();
    }
    
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRequestURI() {
        return copy.getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return copy.getRequestURL();
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    public String getServletPath() {
        return this.servletPath;
    }

    /**
     * Adds a new cookie to the request
     * @param cookie a cookie to add, will displace existing cookies with the same name
     */
    public void addCookie(Cookie cookie) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie cannot be null");
        }
        for (Iterator<Cookie> iterator = cookies.iterator(); iterator.hasNext();) {
            Cookie c = iterator.next();
            if (cookie.getName().equals(c.getName())) {
                iterator.remove();
            }
        }
        cookies.add(cookie);
    }

    /**
     * Removes the cookie with the given name from the request
     * @param name the name of a cookie
     */
    public void removeCookie(String name) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null");
        }
        for (Iterator<Cookie> iterator = cookies.iterator(); iterator.hasNext();) {
            Cookie c = iterator.next();
            if (name.equals(c.getName())) {
                iterator.remove();
            }
        }
    }

    public Cookie[] getCookies() {
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    /**
     * Remove all cookies
     */
    public void clearCookies() {
        this.cookies.clear();
    }

    public void addHeader(String name, String... content) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("values cannot be null or empty");
        }
        Vector<String> v = new Vector<String>(content.length);
        for (int i = 0; i < content.length; i++) {
            String c = content[i];
            v.add(c);
        }
        headers.put(name, v);
    }

    public void removeHeader(String name) {
        headers.remove(name);
    }

    public String getHeader(String name) {
        String header = null;
        if (headers.containsKey(name)) {
            header = headers.get(name).firstElement();
        }
        return header;
    }

    public Enumeration getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    public Enumeration getHeaders(String name) {
        Vector<String> h = new Vector<String>(0);
        if (headers.containsKey(name)) {
            h = headers.get(name);
        }
        return Collections.enumeration(h);
    }

    public long getDateHeader(String name) {
        long date = -1l;
        String header = getHeader(name);
        if (header != null) {
            try {
                date = new Long(header).longValue();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert header value ("+header+") to long (timestamp)");
            }
        }
        return date;
    }

    public int getIntHeader(String name) {
        int num = -1;
        String header = getHeader(name);
        if (header != null) {
            try {
                num = new Integer(header).intValue();
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot convert header value ("+header+") to integer");
            }
        }
        return num;
    }

    /**
     * Remove all headers
     */
    public void clearHeaders() {
        this.headers.clear();
    }

    public String getAuthType() {
        return copy.getAuthType();
    }

    public String getLocalAddr() {
        return copy.getLocalAddr();
    }

    public Enumeration getLocales() {
        return copy.getLocales();
    }

    public String getLocalName() {
        return copy.getLocalName();
    }

    public int getLocalPort() {
        return copy.getLocalPort();
    }

    public String getParameter(String name) {
        String value = null;
        if (parameters.containsKey(name)) {
            String[] vals = parameters.get(name);
            if (vals != null && vals.length > 0) {
                value = vals[0];
            }
        }
        return value;
    }

    public Map getParameterMap() {
        return Collections.unmodifiableMap(parameters);
    }

    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    /**
     * Add a single parameter,
     * this will append to an existing one
     */
    public void addParameter(String name, String value) {
        addParameter(name, new String[] {value});
    }

    /**
     * Add an array of values for a parameter,
     * these will append to existing ones
     */
    public void addParameter(String name, String[] values) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (values == null || values.length < 1) {
            throw new IllegalArgumentException("values cannot be null or empty");
        }
        String[] oldArr = (String[]) this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        } else {
            this.parameters.put(name, values);
        }
    }

    /**
     * Set a new parameter (overwrite an existing one)
     */
    public void setParameter(String name, String[] values) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (values == null || values.length < 1) {
            throw new IllegalArgumentException("values cannot be null or empty");
        }
        this.parameters.put(name, values);
    }

    /**
     * Remove all parameters
     */
    public void clearParameters() {
        this.parameters.clear();
    }

    public String getProtocol() {
        return copy.getProtocol();
    }

    @SuppressWarnings("deprecation")
    public String getRealPath(String path) {
        return copy.getRealPath(path);
    }

    public String getRemoteAddr() {
        return copy.getRemoteAddr();
    }

    public String getRemoteHost() {
        return copy.getRemoteHost();
    }

    public int getRemotePort() {
        return copy.getRemotePort();
    }

    public String getRemoteUser() {
        return copy.getRemoteUser();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return copy.getRequestDispatcher(path);
    }

    public String getRequestedSessionId() {
        return copy.getRequestedSessionId();
    }

    public String getScheme() {
        return copy.getScheme();
    }

    public String getServerName() {
        return copy.getServerName();
    }

    public int getServerPort() {
        return copy.getServerPort();
    }

    public HttpSession getSession() {
        return copy.getSession();
    }

    public HttpSession getSession(boolean create) {
        return copy.getSession(create);
    }

    public Principal getUserPrincipal() {
        return copy.getUserPrincipal();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return copy.isRequestedSessionIdFromCookie();
    }

    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {
        return copy.isRequestedSessionIdFromUrl();
    }

    public boolean isRequestedSessionIdFromURL() {
        return copy.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdValid() {
        return copy.isRequestedSessionIdValid();
    }

    public boolean isSecure() {
        return copy.isSecure();
    }

    public boolean isUserInRole(String role) {
        return copy.isUserInRole(role);
    }

}
