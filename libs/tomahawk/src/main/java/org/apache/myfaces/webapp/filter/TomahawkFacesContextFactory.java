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
package org.apache.myfaces.webapp.filter;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.myfaces.renderkit.html.util.AddResource;
import org.apache.myfaces.renderkit.html.util.AddResourceFactory;
import org.apache.myfaces.tomahawk.util.ExternalContextUtils;

/**
 * The objective of this factory is this:
 * <ol>
 * <li> Wrap a multipart request (used for t:inputFileUpload), 
 *    so the request could be correctly decoded.</li>
 * <li>If a buffered instance of AddResource is configured, 
 *    ExtensionsFilter must buffer and add the resource reference 
 *    to the head of jsf pages (for example when it is used 
 *    DefaultAddResource)</li>
 * </ol>
 * 
 * @since 1.1.7
 * @author Martin Marinschek (latest modification by $Author: lu4242 $)
 * @version $Revision: 697311 $ $Date: 2008-09-19 20:14:01 -0500 (Fri, 19 Sep 2008) $
 */
public class TomahawkFacesContextFactory extends FacesContextFactory {

    /**
     * Disable or enable this factory to wrap the request using 
     * TomahawkFacesContextWrapper as an alternative to ExtensionsFilter.
     * If ExtensionsFilter is configured, use of TomahawkFacesContextWrapper
     * is skipped
     */
    public static final String DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER = 
        "org.apache.myfaces.DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER";
    
    public static final boolean DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER_DEFAULT = true;
    
    private FacesContextFactory delegate;

    public TomahawkFacesContextFactory(FacesContextFactory delegate) {
        this.delegate = delegate;
    }

    public FacesContext getFacesContext(Object context, Object request, Object response, Lifecycle lifecycle) throws FacesException {
        
        if(!ExternalContextUtils.getRequestType(context, request).isPortlet())
        {
            ServletRequest servletRequest = (ServletRequest) request;
            ServletContext servletContext = (ServletContext) context;
            // If no ExtensionsFilter wraps before the call, and 
            //there is not a value indicating disabling the wrapping,
            //use TomahawkFacesContextWrapper 
            if (servletRequest.getAttribute(ExtensionsFilter.DOFILTER_CALLED) == null &&
                    !getBooleanValue(servletContext.getInitParameter(
                            DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER),
                            DISABLE_TOMAHAWK_FACES_CONTEXT_WRAPPER_DEFAULT))
            {
                //This is servlet world
                //For handle buffered response we need to wrap response object here,
                //so all response will be written and then on facesContext
                //release() method write to the original response.
                //This could not be done on TomahawkFacesContextWrapper
                //constructor, because the delegate ExternalContext do
                //calls like dispatch, forward and redirect, that requires
                //the wrapped response instance to work properly.            
                AddResource addResource = AddResourceFactory.getInstance((HttpServletRequest)request,(ServletContext)context);
                
                if (addResource.requiresBuffer())
                {
                    ExtensionsResponseWrapper extensionsResponseWrapper = new ExtensionsResponseWrapper((HttpServletResponse)response);
                    return new TomahawkFacesContextWrapper(delegate.getFacesContext(context, request, extensionsResponseWrapper, lifecycle),
                            extensionsResponseWrapper);
                }
                else
                {
                    return new TomahawkFacesContextWrapper(delegate.getFacesContext(context, request, response, lifecycle));
                }
            }
            else
            {
                //The wrapping was done by extensions filter, so 
                //TomahawkFacesContextWrapper is not needed
                return delegate.getFacesContext(context, request, response, lifecycle);
            }
        }
        else
        {
            if (!PortletUtils.isDisabledTomahawkFacesContextWrapper(context))
            {            
                return new TomahawkFacesContextWrapper(delegate.getFacesContext(context, request, response, lifecycle));
            }
        }    
        return delegate.getFacesContext(context, request, response, lifecycle);
    }
    
    private static boolean getBooleanValue(String initParameter, boolean defaultVal)
    {
        if(initParameter == null || initParameter.trim().length()==0)
            return defaultVal;

        return (initParameter.equalsIgnoreCase("on") || initParameter.equals("1") || initParameter.equalsIgnoreCase("true"));
    }    
}
