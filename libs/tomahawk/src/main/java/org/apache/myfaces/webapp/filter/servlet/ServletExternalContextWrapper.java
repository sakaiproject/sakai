/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.webapp.filter.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.faces.context.ExternalContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This class enhances a standard ExternalContext with support for handling a request
 * that is a multi-part-mime request.
 * <p>
 * In particular, this is needed to provide support for the inputFileUpload component.
 * When an html "file upload" element is embedded in a form, the browser will generate
 * a multi-part-mime message to the server containing the posted form input fields
 * in one mime part, and the contents of the selected files in additional mime parts.
 * JSF only expects one mime part, so we need to provide access by default to just
 * the posted form params, but allow the inputFileUpload component to access the
 * additional mime parts.
 * <p>
 * NOTE: This class is an internal implementation detail of the Tomahawk library, intended
 * for use only by the TomahawkFacesContextWrapper class. This class is NOT part of the
 * Tomahawk stable API and may be modified in minor releases. User code should not use or
 * subclass this class.
 * </p>
 * 
 * @since  1.1.7
 * @author Martin Marinschek
 */

public class ServletExternalContextWrapper extends ExternalContext {
    private ExternalContext _delegate;
    private ServletRequest _servletRequest;
    private ServletResponse _servletResponse;
    private boolean _multipartContent;

    //This maps are only used if multipartContent = true
    private Map _sessionMap;
    private Map _requestMap;
    private Map _requestParameterMap;
    private Map _requestParameterValuesMap;
    private Map _requestHeaderMap;
    private Map _requestHeaderValuesMap;
    private Map _requestCookieMap;
    //end multipartContent = true;

    public ServletExternalContextWrapper(ExternalContext delegate, ServletRequest request, ServletResponse response, boolean multipartContent) {
        this._delegate = delegate;
        this._servletRequest = request;
        this._servletResponse = response;
        this._multipartContent = multipartContent;
    }

    public void dispatch(String path) throws IOException {
        _delegate.dispatch(path);
    }

    // Added to ExternalContext in JSF 2.3; this wrapper originates from 2.1.
    public String encodeWebsocketURL(String url) {
        return _delegate.encodeWebsocketURL(url);
    }

    public String encodeActionURL(String url) {
        return _delegate.encodeActionURL(url);
    }

    public String encodeNamespace(String name) {
        return _delegate.encodeNamespace(name);
    }

    public String encodeResourceURL(String url) {
        return _delegate.encodeResourceURL(url);
    }

    public Map getApplicationMap() {
        return _delegate.getApplicationMap();
    }

    public String getAuthType() {
        return _delegate.getAuthType();
    }

    public Object getContext() {
        return _delegate.getContext();
    }

    public String getInitParameter(String name) {
        return _delegate.getInitParameter(name);
    }

    public Map getInitParameterMap() {
        return _delegate.getInitParameterMap();
    }

    public String getRemoteUser() {
        return _delegate.getRemoteUser();
    }

    public Object getRequest() {
        // Why is this done?
        return _servletRequest==null?_delegate.getRequest():_servletRequest;
    }

    public String getRequestContextPath() {
        return _delegate.getRequestContextPath();
    }

    public Map getRequestCookieMap() {
        if (_multipartContent)
        {
            if (_requestCookieMap == null)
            {
                _requestCookieMap = new CookieMap((HttpServletRequest)_servletRequest);
            }
            return _requestCookieMap;
        }
        else
        {
            return _delegate.getRequestCookieMap();
        }
    }

    public Map getRequestHeaderMap() {
        if (_multipartContent)
        {
            if (_requestHeaderMap == null)
            {
                _requestHeaderMap = new RequestHeaderMap((HttpServletRequest)_servletRequest);
            }
            return _requestHeaderMap;                        
        }
        else
        {
            return _delegate.getRequestHeaderMap();
        }
    }

    public Map getRequestHeaderValuesMap() {
        if (_multipartContent)
        {
            if (_requestHeaderValuesMap == null)
            {
                _requestHeaderValuesMap = new RequestHeaderValuesMap((HttpServletRequest)_servletRequest);
            }
            return _requestHeaderValuesMap;
        }
        else
        {        
            return _delegate.getRequestHeaderValuesMap();
        }
    }

    public Locale getRequestLocale() {
        return _delegate.getRequestLocale();
    }

    public Iterator getRequestLocales() {
        return _delegate.getRequestLocales();
    }

    public Map getRequestMap() {
        if (_multipartContent)
        {            
            if (_requestMap == null)
            {
                _requestMap = new RequestMap(_servletRequest);
            }
            return _requestMap;
        }
        else
        {
            return _delegate.getRequestMap();
        }
    }

    public Map getRequestParameterMap() {
        if (_multipartContent)
        {            
            if (_requestParameterMap == null)
            {
                _requestParameterMap = new RequestParameterMap(_servletRequest);
            }
            return _requestParameterMap;
        }
        else
        {        
            return _delegate.getRequestParameterMap();
        }
    }

    public Iterator getRequestParameterNames() {
        if (_multipartContent)
        {            
            final Enumeration enumer = _servletRequest.getParameterNames();
            Iterator it = new Iterator()
            {
                public boolean hasNext() {
                    return enumer.hasMoreElements();
                }

                public Object next() {
                    return enumer.nextElement();
                }

                public void remove() {
                    throw new UnsupportedOperationException(this.getClass().getName() + " UnsupportedOperationException");
                }
            };
            return it;
        }
        else
        {        
            return _delegate.getRequestParameterNames();
        }
    }

    public Map getRequestParameterValuesMap() {
        if (_multipartContent)
        {            
            if (_requestParameterValuesMap == null)
            {
                _requestParameterValuesMap = new RequestParameterValuesMap(_servletRequest);
            }
            return _requestParameterValuesMap;
        }
        else
        {        
            return _delegate.getRequestParameterValuesMap();
        }
    }

    public String getRequestPathInfo() {
        return _delegate.getRequestPathInfo();
    }

    public String getRequestServletPath() {
        return _delegate.getRequestServletPath();
    }

    public URL getResource(String path) throws MalformedURLException {
        return _delegate.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        return _delegate.getResourceAsStream(path);
    }

    public Set getResourcePaths(String path) {
        return _delegate.getResourcePaths(path);
    }

    public Object getResponse() {
        return _servletResponse==null?_delegate.getResponse():_servletResponse;
    }

    public Object getSession(boolean create) {
        return _delegate.getSession(create);
    }

    public Map getSessionMap() {
        if (_multipartContent)
        {            
            if (_sessionMap == null)
            {
                _sessionMap = new SessionMap((HttpServletRequest) _servletRequest);
            }
            return _sessionMap;
        }
        else
        {
            return _delegate.getSessionMap();            
        }
    }

    public Principal getUserPrincipal() {
        return _delegate.getUserPrincipal();
    }

    public boolean isUserInRole(String role) {
        return _delegate.isUserInRole(role);
    }

    public void log(String message) {
        _delegate.log(message);
    }

    public void log(String message, Throwable exception) {
        _delegate.log(message, exception);
    }

    public void redirect(String url) throws IOException {
        _delegate.redirect(url);
    }
    
    //Methods since 1.2
    
    public String getResponseContentType()
    {
        try
        {
            Method method = _delegate.getClass().getMethod(
                    "getResponseContentType", 
                    null);
            return (String) method.invoke(_delegate, null);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }

    public void setRequest(java.lang.Object request)
    {
        try
        {
            Method method = _delegate.getClass().getMethod(
                    "setRequest", 
                    new Class[]{java.lang.Object.class});
            method.invoke(_delegate, new Object[]{request});
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }

    public void setRequestCharacterEncoding(java.lang.String encoding)
        throws java.io.UnsupportedEncodingException{

        try
        {
            Method method = _delegate.getClass().getMethod(
                    "setRequestCharacterEncoding", 
                    new Class[]{java.lang.String.class});
            method.invoke(_delegate, new Object[]{encoding});
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }
    
    public void setResponse(java.lang.Object response)
    {
        try
        {
            Method method = _delegate.getClass().getMethod(
                    "setResponse", 
                    new Class[]{java.lang.Object.class});
            method.invoke(_delegate, new Object[]{response});
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }
    
    public void setResponseCharacterEncoding(java.lang.String encoding)
    {
        try
        {
            Method method = _delegate.getClass().getMethod(
                    "setResponseCharacterEncoding", 
                    new Class[]{java.lang.String.class});
            method.invoke(_delegate, new Object[]{encoding});
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }

    public String getResponseCharacterEncoding()
    {
        try
        {
            Method method = _delegate.getClass().getMethod(
                    "getResponseCharacterEncoding", 
                    null);
            return (String) method.invoke(_delegate, null);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }

    public String getRequestCharacterEncoding()
    {
        try
        {
            Method method = _delegate.getClass().getMethod(
                    "getRequestCharacterEncoding", 
                    null);
            return (String) method.invoke(_delegate, null);
        }
        catch (NoSuchMethodException e)
        {
            throw new RuntimeException("JSF 1.2 method not implemented: "+e.getMessage());
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error calling JSF 1.2 method: "+e.getMessage());
        }
    }
}
