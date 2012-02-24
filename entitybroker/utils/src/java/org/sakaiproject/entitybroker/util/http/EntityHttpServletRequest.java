/**
 * $Id$
 * $URL$
 * CalloutHttpServletRequest.java - entity-broker - Dec 19, 2008 12:03:07 PM - azeckoski
 ***********************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.util.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;


/**
 * Makes a copy of the data in a servlet request into a new request object
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings({ "unchecked", "deprecation" })
public class EntityHttpServletRequest implements HttpServletRequest {

    /**
     * default schema: http
     */
    public static final String DEFAULT_SCHEMA = "http";
    /**
     * default protocol: HTTP/1.0
     */
    public static final String DEFAULT_PROTOCOL = "HTTP/1.0";
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
    HttpSession internalSession;

    String scheme = DEFAULT_PROTOCOL;
    String protocol = DEFAULT_PROTOCOL;
    String serverName = DEFAULT_SERVER_NAME;
    int serverPort = DEFAULT_SERVER_PORT;
    String remoteAddr = DEFAULT_REMOTE_ADDR;
    String remoteHost = DEFAULT_REMOTE_HOST;

    String method = "GET";
    String contextPath = ""; // always starts with "/", "" indicates root
    String pathInfo = null;
    String queryString = null;
    String requestURI = "";
    String servletPath = ""; // always starts with "/", "" indicates /* used to match

    @Override
    public String toString() {
        return method + " " + contextPath + (pathInfo == null ? "" : pathInfo) 
            + (queryString == null ? "" : "?"+queryString) + " "+ super.toString();
    }

    /**
     * Create a new request from a given request
     * @param req any request
     */
    public EntityHttpServletRequest(HttpServletRequest req) {
        this(req, null, null);
    }

    /**
     * Create a new request from a given request and modify it based on the path string
     * @param req any request
     * @param pathString any full path or URL (/direct/prefix/id.xml)
     */
    public EntityHttpServletRequest(HttpServletRequest req, String pathString) {
        this(req, null, pathString);
    }

    /**
     * Create a new request from a given request and modify it based on the path string and method
     * @param req any request
     * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
     * this will be set to POST if null or unset
     * @param pathString any full path or URL (/direct/prefix/id.xml)
     */
    public EntityHttpServletRequest(HttpServletRequest req, String method, String pathString) {
        copy = req;
        if (req != null) {
            setRequestValues(req);
        } else {
            throw new IllegalArgumentException("HttpServletRequest must be set and cannot be null");
        }
        if (method == null || "".equals(method)) {
            setMethod( req.getMethod().toUpperCase() );
        }
        // setup the stuff based on the pathString
        setPathString(pathString);
    }

    /**
     * Create a request using the pathString
     * @param pathString any path or URL
     */
    public EntityHttpServletRequest(String pathString) {
        this(null, pathString, (String[]) null);
    }

    /**
     * Create a request using the pathString and setting the method
     * @param pathString any path or URL
     * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
     * this will be set to POST if null or unset
     */
    public EntityHttpServletRequest(String method, String pathString) {
        this(method, pathString, (String[]) null);
    }

    /**
     * Create a request using the pathString and setting the method and params
     * @param method GET, POST, PUT, DELETE (PUT and DELETE not supported by browsers),
     * this will be set to POST if null or unset
     * @param pathString any path or URL
     * @param params alternating keys and values (starting with keys) to place into the request parameters
     */
    public EntityHttpServletRequest(String method, String pathString, String... params) {
        setPathString(pathString);
        if (method == null || "".equals(method)) {
            setMethod("POST");
        } else {
            setMethod( method );
        }
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params.length < i + 1) {
                   break;
                }
                this.addParameter(params[i], params[i+1]);
                i++;
             }
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
        if (ck != null) {
            for (int i = 0; i < ck.length; i++) {
                cookies.add(ck[i]);
            }
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
        this.locale = req.getLocale();
        this.method = req.getMethod();
        this.contentType = req.getContentType();
        this.characterEncoding = req.getCharacterEncoding() == null ? "UTF-8" : req.getCharacterEncoding();
        this.contentLength = req.getContentLength();

        this.contextPath = req.getContextPath();
        this.pathInfo = req.getPathInfo();
        this.queryString = req.getQueryString();
        this.requestURI = req.getRequestURI();
        this.servletPath = req.getServletPath();

        this.scheme = req.getScheme();
        this.protocol = req.getProtocol();
        this.serverName = req.getServerName();
        this.serverPort = req.getServerPort();
        this.remoteAddr = req.getRemoteAddr();
        this.remoteHost = req.getRemoteHost();

        this.realDispatcher = true;
    }

    /**
     * This stores all the query params found when the request was created
     */
    public Map<String, String[]> pathQueryParams = new HashMap<String, String[]>(0);

    /**
     * This will set the given url/path string values into this request,
     * this will override any values that are currently set
     * @param pathString any url or path string
     * @return the url data object based on the input object OR null if the string is null or empty
     */
    public URLData setPathString(String pathString) {
        URLData ud = null;
        if (pathString != null && pathString.length() > 1) {
            this.requestURI = pathString;
            ud = HttpRESTUtils.parseURL(pathString);
            if (ud.pathInfo.length() > 0) {
                this.pathInfo = ud.pathInfo;
            } else {
                this.pathInfo = null;
            }
            if (ud.port.length() > 0) {
                // only if port is set and a number
                try {
                    this.serverPort = new Integer(ud.port);
                } catch (NumberFormatException e) {
                    // set to default if it cannot be set and is not set
                    if (this.serverPort <= 0) {
                        this.serverPort = 80;
                    }
                }
            }
            if (ud.protocol.length() > 0) {
                this.scheme = ud.protocol;
            }
            if (ud.query.length() > 0) {
                this.queryString = ud.query;
                Map<String, String> p = HttpRESTUtils.parseURLintoParams(ud.query);
                for (Entry<String, String> entry : p.entrySet()) {
                    String[] value = new String[] {entry.getValue()};
                    this.pathQueryParams.put(entry.getKey(), value);
                    setParameter(entry.getKey(), value);
                }
            } else {
                this.queryString = null;
            }
            if (ud.server.length() > 0) {
                this.serverName = ud.server;
            }
            if (ud.servletName.length() > 0) {
                this.servletPath = "/" + ud.servletName;
                this.contextPath = ud.contextPath;
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
     * @param content any IS content, UTF-8 encoded, replaces existing content
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
     * @param content any byte[] content, UTF-8 encoded, replaces existing content
     */
    public void setContent(byte[] content) {
        if (content == null) {
            this.contentLength = 0;
            this.contentStream = new ByteArrayInputStream(new byte[] {});
        } else {
            this.contentLength = content.length;
            this.contentStream = new ByteArrayInputStream(content);
        }
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
        this.method = (method != null ? method.toUpperCase() : method);
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
        return this.pathInfo;
    }

    public String getPathTranslated() {
        if (copy != null) {
            return copy.getPathTranslated();
        } else {
            return null;
        }
    }
    
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getRequestURI() {
        return this.requestURI;
    }

    public StringBuffer getRequestURL() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.scheme);
        sb.append("://");
        sb.append(this.serverName);
        if (this.serverPort > 0) {
            sb.append(":");
            sb.append(this.serverPort);
        }
        if (this.contextPath == null || this.contextPath.length() == 0) {
            sb.append("/");
        } else {
            sb.append(this.contextPath);
        }
        sb.append( getRequestURI() );
        return sb;
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
        Cookie[] c;
        if (this.cookies == null || this.cookies.size() == 0) {
            c = new Cookie[0];
        } else {
            c = this.cookies.toArray(new Cookie[this.cookies.size()]);
        }
        return c;
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
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null");
        }
        String header = null;
        if (headers.containsKey(name)) {
            Vector<String> v = headers.get(name);
            if (v != null) {
                header = v.firstElement();
            }
        }
        return header;
    }

    public Enumeration getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    public Enumeration getHeaders(String name) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null");
        }
        Vector<String> h = new Vector<String>(0);
        if (headers.containsKey(name)) {
            Vector<String> v = headers.get(name);
            if (v != null) {
                h = v;
            }
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
        if (copy != null) {
            return copy.getAuthType();
        }
        return null; // no auth
    }

    public String getLocalAddr() {
        if (copy != null) {
            return copy.getLocalAddr();
        }
        return DEFAULT_SERVER_ADDR;
    }

    public Enumeration getLocales() {
        if (copy != null) {
            return copy.getLocales();
        } else {
            ArrayList<Locale> l = new ArrayList<Locale>();
            l.add(locale);
            return Collections.enumeration(l);
        }
    }

    public String getLocalName() {
        if (copy != null) {
            return copy.getLocalName();
        }
        return DEFAULT_SERVER_NAME;
    }

    public int getLocalPort() {
        if (copy != null) {
            return copy.getLocalPort();
        }
        return DEFAULT_SERVER_PORT;
    }

    /**
     * @return all parameters in this request
     */
    public Map<String, String[]> getParameters() {
        return Collections.unmodifiableMap(this.parameters);
    }

    /**
     * @return all parameters in this request as single strings
     */
    public Map<String, String> getStringParameters() {
        Map<String, String> m = new TreeMap<String, String>();
        for (Entry<String, String[]> entry : this.parameters.entrySet()) {
            String key = entry.getKey();
            String value = "";
            String[] sa = entry.getValue();
            if (sa != null && sa.length > 0) {
                value = sa[0];
            }
            m.put(key, value);
        }
        return m;
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
     * Set a large number of params at once,
     * replaces existing params
     * @param params map of params
     */
    public void setParameters(Map<String, String[]> params) {
        if (params != null && params.size() > 0) {
            this.parameters.putAll( params );
        }
    }

    /**
     * Removes a parameter and all values for it
     * @param name
     */
    public void removeParameter(String name) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.parameters.remove(name);
    }
    
    /**
     * Remove all parameters
     */
    public void clearParameters() {
        this.parameters.clear();
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRealPath(String path) {
        if (copy != null) {
            return copy.getRealPath(path);
        } else {
            return path;
        }
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        if (copy != null) {
            return copy.getRemotePort();
        } else {
            return DEFAULT_SERVER_PORT;
        }
    }

    public String getRemoteUser() {
        return copy.getRemoteUser();
    }

    private boolean realDispatcher = false;
    /**
     * @param real if true and there is a real RequestDispatcher available then use it,
     * otherwise just emulate a forward/include call using the fake one 
     * (will always use the fake one if no real one is found)
     */
    public void setUseRealDispatcher(boolean real) {
        this.realDispatcher = real;
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        if (this.copy != null && this.realDispatcher) {
            return this.copy.getRequestDispatcher(path);
        } else {
            return new EntityRequestDispatcher(path);
        }
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getRequestedSessionId() {
        if (copy != null) {
            return copy.getRequestedSessionId();
        } else {
            return getInternalSession().getId();
        }
    }

    public HttpSession getSession() {
        if (copy != null) {
            return copy.getSession();
        } else {
            return getInternalSession();
        }
    }

    public HttpSession getSession(boolean create) {
        if (copy != null) {
            return copy.getSession(create);
        } else {
            return getInternalSession();
        }
    }

    private HttpSession getInternalSession() {
        if (internalSession == null) {
            internalSession = new EntityHttpSession();
        }
        return internalSession;
    }

    public Principal getUserPrincipal() {
        if (copy != null) {
            return copy.getUserPrincipal();
        } else {
            return null;
        }
    }

    public boolean isRequestedSessionIdFromCookie() {
        if (copy != null) {
            return copy.isRequestedSessionIdFromCookie();
        } else {
            return false;
        }
    }

    public boolean isRequestedSessionIdFromUrl() {
        if (copy != null) {
            return copy.isRequestedSessionIdFromUrl();
        } else {
            return false;
        }
    }

    public boolean isRequestedSessionIdFromURL() {
        if (copy != null) {
            return copy.isRequestedSessionIdFromURL();
        } else {
            return false;
        }
    }

    public boolean isRequestedSessionIdValid() {
        if (copy != null) {
            return copy.isRequestedSessionIdValid();
        } else {
            return true;
        }
    }

    public boolean isSecure() {
        if (copy != null) {
            return copy.isSecure();
        } else {
            return false;
        }
    }

    public boolean isUserInRole(String role) {
        if (copy != null) {
            return copy.isUserInRole(role);
        } else {
            return false;
        }
    }

    /**
     * A non-functional request dispatcher, based on the spring mock version
     */
    public static class EntityRequestDispatcher implements RequestDispatcher {

        private final String url;

        /**
         * Create a new EntityRequestDispatcher
         * @param url the URL to dispatch to
         */
        public EntityRequestDispatcher(String url) {
            if (url == null) {
                throw new IllegalArgumentException("url cannot be null");
            }
            this.url = url;
        }


        public void forward(ServletRequest request, ServletResponse response) {
            if (request == null || response == null) {
                throw new IllegalArgumentException("request and response cannot be null");
            }
            if (response.isCommitted()) {
                throw new IllegalStateException("Cannot perform forward - response is already committed");
            }
            getEntityHttpServletResponse(response).setForwardedUrl(this.url);
        }

        public void include(ServletRequest request, ServletResponse response) {
            if (request == null || response == null) {
                throw new IllegalArgumentException("request and response cannot be null");
            }
            getEntityHttpServletResponse(response).setIncludedUrl(this.url);
        }

        protected EntityHttpServletResponse getEntityHttpServletResponse(ServletResponse response) {
            if (response instanceof EntityHttpServletResponse) {
                return (EntityHttpServletResponse) response;
            }
            if (response instanceof HttpServletResponseWrapper) {
                return getEntityHttpServletResponse(((HttpServletResponseWrapper) response).getResponse());
            }
            throw new IllegalArgumentException("EntityRequestDispatcher requires EntityHttpServletResponse");
        }

    }

    
    /**
     * For testing, based on the spring Mock version
     */
    public static class EntityServletContext implements ServletContext {

        public static final String TEMP_DIR_CONTEXT_ATTRIBUTE = "javax.servlet.context.tempdir";
        private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";
        private final String resourceBasePath;
        private String contextPath = "";
        private final Map contexts = new HashMap();
        private final Properties initParameters = new Properties();
        private final Hashtable<String, Object> attributes = new Hashtable<String, Object>();
        private String servletContextName = "MockServletContext";

        /**
         * Create a new MockServletContext, using no base path and a
         * DefaultResourceLoader (i.e. the classpath root as WAR root).
         * @see org.springframework.core.io.DefaultResourceLoader
         */
        public EntityServletContext() {
            this("");
        }

        /**
         * Create a new MockServletContext, using a DefaultResourceLoader.
         * @param resourceBasePath the WAR root directory (should not end with a slash)
         * @see org.springframework.core.io.DefaultResourceLoader
         */
        public EntityServletContext(String resourceBasePath) {
            this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");
            // Use JVM temp dir as ServletContext temp dir.
            String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
            if (tempDir != null) {
                this.attributes.put(TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
            }
        }

        protected File getResourceFile(String path) {
            File f = new File(path);
            return f;
        }

        /**
         * Build a full resource location for the given path,
         * prepending the resource base path of this MockServletContext.
         * @param path the path as specified
         * @return the full resource path
         */
        protected String getResourceLocation(String path) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            return this.resourceBasePath + path;
        }


        public void setContextPath(String contextPath) {
            this.contextPath = (contextPath != null ? contextPath : "");
        }

        /* This is a Servlet API 2.5 method. */
        public String getContextPath() {
            return this.contextPath;
        }

        public void registerContext(String contextPath, ServletContext context) {
            this.contexts.put(contextPath, context);
        }

        public ServletContext getContext(String contextPath) {
            if (this.contextPath.equals(contextPath)) {
                return this;
            }
            return (ServletContext) this.contexts.get(contextPath);
        }

        public int getMajorVersion() {
            return 2;
        }

        public int getMinorVersion() {
            return 5;
        }

        public String getMimeType(String filePath) {
            return MimeTypeResolver.getMimeType(filePath);
        }

        public Set getResourcePaths(String path) {
            String actualPath = (path.endsWith("/") ? path : path + "/");
            try {
                File file = getResourceFile(getResourceLocation(actualPath));
                String[] fileList = file.list();
                if (fileList == null || fileList.length == 0) {
                    return null;
                }
                Set<String> resourcePaths = new LinkedHashSet<String>(fileList.length);
                for (int i = 0; i < fileList.length; i++) {
                    String resultPath = actualPath + fileList[i];
                    File f = getResourceFile(resultPath);
                    if (f.isDirectory()) {
                        resultPath += "/";
                    }
                    resourcePaths.add(resultPath);
                }
                return resourcePaths;
            }
            catch (Exception ex) {
                return null;
            }
        }

        public URL getResource(String path) throws MalformedURLException {
            File file = getResourceFile(getResourceLocation(path));
            if (!file.exists()) {
                return null;
            }
            try {
                return file.toURL();
            }
            catch (MalformedURLException ex) {
                throw ex;
            }
        }

        public InputStream getResourceAsStream(String path) {
            File file = getResourceFile(getResourceLocation(path));
            if (!file.exists()) {
                return null;
            }
            try {
                InputStream stream = new BufferedInputStream( new FileInputStream(file) );
                return stream;
            }
            catch (IOException ex) {
                return null;
            }
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            if (!path.startsWith("/")) {
                throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
            }
            return new EntityRequestDispatcher(path);
        }

        public RequestDispatcher getNamedDispatcher(String path) {
            return null;
        }

        public Servlet getServlet(String name) {
            return null;
        }

        public Enumeration getServlets() {
            return Collections.enumeration(Collections.EMPTY_SET);
        }

        public Enumeration getServletNames() {
            return Collections.enumeration(Collections.EMPTY_SET);
        }

        public void log(String message) {
        }

        public void log(Exception ex, String message) {
        }

        public void log(String message, Throwable ex) {
        }

        public String getRealPath(String path) {
            File file = getResourceFile(getResourceLocation(path));
            return file.getAbsolutePath();
        }

        public String getServerInfo() {
            return "MockServletContext";
        }

        public String getInitParameter(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name cannot be null");
            }
            return this.initParameters.getProperty(name);
        }

        public void addInitParameter(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("name cannot be null");
            }
            this.initParameters.setProperty(name, value);
        }

        public Enumeration getInitParameterNames() {
            return this.initParameters.keys();
        }

        public Object getAttribute(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name cannot be null");
            }
            return this.attributes.get(name);
        }

        public Enumeration getAttributeNames() {
            return this.attributes.keys();
        }

        public void setAttribute(String name, Object value) {
            if (name == null) {
                throw new IllegalArgumentException("name cannot be null");
            }
            if (value != null) {
                this.attributes.put(name, value);
            }
            else {
                this.attributes.remove(name);
            }
        }

        public void removeAttribute(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name cannot be null");
            }
            this.attributes.remove(name);
        }

        public void setServletContextName(String servletContextName) {
            this.servletContextName = servletContextName;
        }

        public String getServletContextName() {
            return this.servletContextName;
        }

    }

    private static class MimeTypeResolver {
        public static String getMimeType(String filePath) {
            return "text/plain";
        }
    }


    /**
     * A fake session for testing, from the spring mock session
     */
    public static class EntityHttpSession implements HttpSession {

        public static final String SESSION_COOKIE_NAME = "JSESSION";
        private static int nextId = 1;
        private final String id;
        private final long creationTime = System.currentTimeMillis();
        private int maxInactiveInterval;
        private long lastAccessedTime = System.currentTimeMillis();
        private final ServletContext servletContext;
        private final Hashtable<String, Object> attributes = new Hashtable<String, Object>();
        private boolean invalid = false;
        private boolean isNew = true;


        /**
         * Create a new MockHttpSession with a default {@link MockServletContext}.
         * @see MockServletContext
         */
        public EntityHttpSession() {
            this(null);
        }

        /**
         * Create a new MockHttpSession.
         * @param servletContext the ServletContext that the session runs in
         */
        public EntityHttpSession(ServletContext servletContext) {
            this(servletContext, null);
        }

        /**
         * Create a new MockHttpSession.
         * @param servletContext the ServletContext that the session runs in
         * @param id a unique identifier for this session
         */
        public EntityHttpSession(ServletContext servletContext, String id) {
            this.servletContext = (servletContext != null ? servletContext : new EntityServletContext());
            this.id = (id != null ? id : Integer.toString(nextId++));
        }


        public long getCreationTime() {
            return this.creationTime;
        }

        public String getId() {
            return this.id;
        }

        public void access() {
            this.lastAccessedTime = System.currentTimeMillis();
            this.isNew = false;
        }

        public long getLastAccessedTime() {
            return this.lastAccessedTime;
        }

        public ServletContext getServletContext() {
            return this.servletContext;
        }

        public void setMaxInactiveInterval(int interval) {
            this.maxInactiveInterval = interval;
        }

        public int getMaxInactiveInterval() {
            return this.maxInactiveInterval;
        }

        public HttpSessionContext getSessionContext() {
            throw new UnsupportedOperationException("getSessionContext");
        }

        public Object getAttribute(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null");
            }
            return this.attributes.get(name);
        }

        public Object getValue(String name) {
            return getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return this.attributes.keys();
        }

        public String[] getValueNames() {
            return (String[]) this.attributes.keySet().toArray(new String[this.attributes.size()]);
        }

        public void setAttribute(String name, Object value) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null");
            }
            if (value != null) {
                this.attributes.put(name, value);
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
                }
            }
            else {
                removeAttribute(name);
            }
        }

        public void putValue(String name, Object value) {
            setAttribute(name, value);
        }

        public void removeAttribute(String name) {
            if (name == null) {
                throw new IllegalArgumentException("name must not be null");
            }
            Object value = this.attributes.remove(name);
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
        }

        public void removeValue(String name) {
            removeAttribute(name);
        }

        /**
         * Clear all of this session's attributes.
         */
        public void clearAttributes() {
            for (Iterator it = this.attributes.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                it.remove();
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }

        public void invalidate() {
            this.invalid = true;
            clearAttributes();
        }

        public boolean isInvalid() {
            return this.invalid;
        }

        public void setNew(boolean value) {
            this.isNew = value;
        }

        public boolean isNew() {
            return this.isNew;
        }


        /**
         * Serialize the attributes of this session into an object that can
         * be turned into a byte array with standard Java serialization.
         * @return a representation of this session's serialized state
         */
        public Serializable serializeState() {
            HashMap<String, Object> state = new HashMap<String, Object>();
            for (Iterator it = this.attributes.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                it.remove();
                if (value instanceof Serializable) {
                    state.put(name, value);
                }
                else {
                    if (value instanceof HttpSessionBindingListener) {
                        ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                    }
                }
            }
            return state;
        }

        /**
         * Deserialize the attributes of this session from a state object
         * created by {@link #serializeState()}.
         * @param state a representation of this session's serialized state
         */
        public void deserializeState(Serializable state) {
            this.attributes.putAll((Map) state);
        }

    }

}
