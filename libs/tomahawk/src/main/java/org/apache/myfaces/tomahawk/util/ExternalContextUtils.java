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
package org.apache.myfaces.tomahawk.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import jakarta.faces.context.ExternalContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: This class is a copy of 
 * org.apache.myfaces.commons.util.ExternalContextUtils
 * 
 * Since tomahawk should be compatible with 1.1, this is placed
 * here and there is not a dependency for myfaces-commons-utils, because
 * this stuff only works for 1.2
 * 
 * This provides some functionality for determining some things about the
 * native request object that is not provided by JSF.  This class is useful
 * for use in places where Portlet API's may or may not be present and can
 * also provide access to some request-specific items which are not available on
 * the JSF ExternalContext.  If portlet API's are not present, this class simply 
 * handles the Servlet Request type.
 * 
 * @since 1.1.7
 */
public final class ExternalContextUtils
{
    // prevent this from being instantiated
    private ExternalContextUtils()
    {
    }

    /**
     * Returns the content length or -1 if the unknown.
     *
     * @param externalContext
     *          the ExternalContext
     * @return the length or -1
     */
    public static final int getContentLength(ExternalContext externalContext)
    {
        RequestType type = getRequestType(externalContext);
        
        if(type.isRequestFromClient())
        {
            try
            {
                Object request = externalContext.getRequest();
                Method contentLenMethod = request.getClass().getMethod("getContentLength",new Class[]{});
                return ((Integer) contentLenMethod.invoke(request,new Object[]{})).intValue(); //this will autobox
            }
            catch(Exception e)
            {
                _LOG.error("Unsupported request type.", e);
            }
        }
            
        return -1;
    }

    /**
     * Returns the request input stream if one is available
     *
     * @param externalContext
     * @return
     * @throws IOException
     */
    public static final InputStream getRequestInputStream(ExternalContext externalContext)
            throws IOException
    {
        RequestType type = getRequestType(externalContext);
        
        if(type.isRequestFromClient())
        {
          try
            {
              Object request = externalContext.getRequest();
              
              Method method = request.getClass().getMethod(type.isPortlet()?"getPortletInputStream":"getInputStream",new Class[]{});
              return (InputStream) method.invoke(request,new Object[]{});
            }
            catch (Exception e)
            {
                _LOG.error("Unable to get the request input stream because of an error", e);
            }
        }
        return null;
    }
    
    /**
     * Returns the requestType of this ExternalContext.
     * 
     * @param externalContext the current external context
     * @return the appropriate RequestType for this external context
     * @see RequestType
     */
    public static final RequestType getRequestType(ExternalContext externalContext)
    {
        //Stuff is laid out strangely in this class in order to optimize
        //performance.  We want to do as few instanceof's as possible so
        //things are laid out according to the expected frequency of the
        //various requests occurring.
        if(_PORTLET_CONTEXT_CLASS != null)
        {
            if (_PORTLET_CONTEXT_CLASS.isInstance(externalContext.getContext()))
            {
                //We are inside of a portlet container
                Object request = externalContext.getRequest();
                
                if(_PORTLET_RENDER_REQUEST_CLASS.isInstance(request))
                {
                    return RequestType.RENDER;
                }
                
                if(_PORTLET_RESOURCE_REQUEST_CLASS != null)
                {
                    if(_PORTLET_ACTION_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.ACTION;
                    }

                    //We are in a JSR-286 container
                    if(_PORTLET_RESOURCE_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.RESOURCE;
                    }
                    
                    return RequestType.EVENT;
                }
                
                return RequestType.ACTION;
            }
        }
        
        return RequestType.SERVLET;
    }

    /**
     * This method is used when a ExternalContext object is not available,
     * like in TomahawkFacesContextFactory.
     * 
     * According to TOMAHAWK-1331, the object context could receive an
     * instance of javax.portlet.PortletContext or javax.portlet.PortletConfig,
     * so we check both cases.
     * 
     * @param context
     * @param request
     * @return
     */
    public static final RequestType getRequestType(Object context, Object request)
    {
        //Stuff is laid out strangely in this class in order to optimize
        //performance.  We want to do as few instanceof's as possible so
        //things are laid out according to the expected frequency of the
        //various requests occurring.

        if(_PORTLET_CONTEXT_CLASS != null)
        {
            if (_PORTLET_CONFIG_CLASS.isInstance(context) ||
                _PORTLET_CONTEXT_CLASS.isInstance(context))
            {
                //We are inside of a portlet container
                
                if(_PORTLET_RENDER_REQUEST_CLASS.isInstance(request))
                {
                    return RequestType.RENDER;
                }
                
                if(_PORTLET_RESOURCE_REQUEST_CLASS != null)
                {
                    if(_PORTLET_ACTION_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.ACTION;
                    }

                    //We are in a JSR-286 container
                    if(_PORTLET_RESOURCE_REQUEST_CLASS.isInstance(request))
                    {
                        return RequestType.RESOURCE;
                    }
                    
                    return RequestType.EVENT;
                }
                
                return RequestType.ACTION;
            }
        }
        
        return RequestType.SERVLET;
    }

    private static final Log _LOG = LogFactory.getLog(ExternalContextUtils.class);

    private static final Class    _PORTLET_ACTION_REQUEST_CLASS;
    private static final Class _PORTLET_RENDER_REQUEST_CLASS;
    private static final Class _PORTLET_RESOURCE_REQUEST_CLASS; //Will be present in JSR-286 containers only
    private static final Class    _PORTLET_CONTEXT_CLASS;
    private static final Class    _PORTLET_CONFIG_CLASS;
    
    static
    {
        Class context;
        Class config;
        Class actionRequest;
        Class renderRequest;
        Class resourceRequest;
        try
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            context = loader.loadClass("javax.portlet.PortletContext");
            config = loader.loadClass("javax.portlet.PortletConfig");
            actionRequest = loader.loadClass("javax.portlet.ActionRequest");
            renderRequest = loader.loadClass("javax.portlet.RenderRequest");
            
            try
            {
                resourceRequest = loader.loadClass("javax.portlet.ResourceRequest");
            }
            catch (ClassNotFoundException e)
            {
                resourceRequest = null;
            }
        }
        catch (ClassNotFoundException e)
        {
            context = null;
            config = null;
            actionRequest = null;
            renderRequest = null;
            resourceRequest = null;
        }

        _PORTLET_CONTEXT_CLASS = context;
        _PORTLET_CONFIG_CLASS = config;
        _PORTLET_ACTION_REQUEST_CLASS = actionRequest;
        _PORTLET_RENDER_REQUEST_CLASS = renderRequest;
        _PORTLET_RESOURCE_REQUEST_CLASS = resourceRequest;
    }    
}
