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

package org.apache.myfaces.renderkit.html.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.shared_tomahawk.config.MyfacesConfig;
import org.apache.myfaces.shared_tomahawk.util.ClassUtils;
import org.apache.myfaces.shared_tomahawk.webapp.webxml.FilterMapping;
import org.apache.myfaces.shared_tomahawk.webapp.webxml.WebXml;
import org.apache.myfaces.tomahawk.util.ExternalContextUtils;
import org.apache.myfaces.webapp.filter.ExtensionsFilter;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import javax.portlet.PortletContext;
import javax.portlet.PortletResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * This class provides the ability to instantiate AddResource objects. By
 * default, this class will instantiate instances of
 * org.apache.myfaces.renderkit.html.util.DefaultAddResource. However, the
 * context parameter org.apache.myfaces.ADD_RESOURCE_CLASS can specify an
 * alternative implementation of the AddResource interface. The class must have
 * a constructor with a single String argument, representing the context path.
 * <p/> Mostly used to avoid having to include [script src="..."][/script] in
 * the head of the pages before using a component.
 * 
 * @author Peter Mahoney
 * @author Sylvain Vieujot (latest modification by $Author: mmarinschek $)
 * @version $Revision: 358042 $ $Date: 2005-12-20 17:12:56 +0000 (Tue, 20 Dec
 *          2005) $
 */
public class AddResourceFactory
{
    public static class RequestMapWrapper implements Map
    {
        private final HttpServletRequest request;

        public RequestMapWrapper(final HttpServletRequest request)
        {
            this.request = request;
        }

        public int size()
        {
            throw new UnsupportedOperationException();
        }

        public boolean isEmpty()
        {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(Object key)
        {
            if (key == null)
            {
                throw new UnsupportedOperationException(
                    "'null' key not supported");
            }
            return request.getAttribute(key.toString()) != null;
        }

        public boolean containsValue(Object value)
        {
            throw new UnsupportedOperationException();
        }

        public Object get(Object key)
        {
            if (key == null)
            {
                throw new UnsupportedOperationException(
                    "'null' key not supported");
            }
            return request.getAttribute(key.toString());
        }

        public Object put(Object key, Object value)
        {
            if (key == null)
            {
                throw new UnsupportedOperationException(
                    "'null' key not supported");
            }

            Object old = request.getAttribute(key.toString());
            request.setAttribute(key.toString(), value);
            return old;
        }

        public Object remove(Object key)
        {
            if (key == null)
            {
                throw new UnsupportedOperationException(
                    "'null' key not supported");
            }

            Object old = request.getAttribute(key.toString());
            request.removeAttribute(key.toString());
            return old;
        }

        public void putAll(Map arg)
        {
            throw new UnsupportedOperationException();
        }

        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        public Set keySet()
        {
            throw new UnsupportedOperationException();
        }

        public Collection values()
        {
            throw new UnsupportedOperationException();
        }

        public Set entrySet()
        {
            throw new UnsupportedOperationException();
        }

    }

    protected static final Log log = LogFactory
        .getLog(AddResourceFactory.class);

    private final static String CACHE_MAP_KEY = "org.apache.myfaces.AddResourceFactory.CACHE_MAP_KEY";
    private final static String ENV_CHECKED_KEY = "org.apache.myfaces.AddResourceFactory.ENV_CHECKED_KEY";

    /**
     * Internal factory method. <p/> Return an instance of AddResource keyed by
     * context path, or create one if no such instance already exists. The
     * instance will be cached using the given Map, most likely this will the
     * the request map of your servlet request. Therefore every request uses its
     * own AddResource instance.
     * </p>
     * <p/> Note that this method is package-scope for the purposes of
     * unit-testing only. This method should be treated as private by non-test
     * code.
     * </p>
     * 
     * @param cacheMap
     *            the map used for caching of the instance. if null, a new
     *            instance will be created all the time (for tests)
     * @param contextPath
     *            context path of your web-app
     * @param addResourceClassName
     *            class name of a class implementing the
     * @link AddResource interface
     */
    static AddResource getInstance(Object context, Map cacheMap, String contextPath,
            String addResourceClassName)
    {
        AddResource instance = null;

        if (cacheMap != null)
        {
            instance = (AddResource) cacheMap.get(CACHE_MAP_KEY);
        }

        if (instance == null)
        {
            if (addResourceClassName == null)
            {
                // For efficiency don't use reflection unless it is necessary
                if(context instanceof PortletContext) {
                    instance = new NonBufferingAddResource();
                    instance.setContextPath(contextPath);
                }
                else {
                    instance = new DefaultAddResource();
                    instance.setContextPath(contextPath);
                }
            }
            else
            {
                try
                {
                    Class addResourceClass = ClassUtils
                        .classForName(addResourceClassName);

                    if (AddResource.class.isAssignableFrom(addResourceClass))
                    {
                        AddResource tmpInstance = (AddResource) addResourceClass
                            .newInstance();
                        tmpInstance.setContextPath(contextPath);

                        // only use a fully initialized instance
                        instance = tmpInstance;
                    }
                    else
                    {
                        log
                            .error("Invalid AddResource class ("
                                    + addResourceClass.getName()
                                    + "). Must implement the AddResource interface.");
                    }
                }
                catch (ClassNotFoundException e)
                {
                    log
                        .error(
                            "AddResource class not found. Using default class instead",
                            e);
                }
                catch (IllegalArgumentException e)
                {
                    // This should not happen as the constructor has been
                    // checked
                    log.error(e);
                }
                catch (InstantiationException e)
                {
                    log.error(
                        "Invalid AddResource class. Must be non-abstract", e);
                }
                catch (IllegalAccessException e)
                {
                    log.error("Could not access AddResource class", e);
                }
                finally
                {
                    // Ensure there is always an AddResource object available
                    if (instance == null)
                    {
                        instance = new DefaultAddResource();
                        instance.setContextPath(contextPath);
                    }
                }
            }

            if (cacheMap != null)
            {
                cacheMap.put(CACHE_MAP_KEY, instance);
            }
        }

        return instance;
    }

    public static AddResource getInstance(FacesContext context)
    {
        ExternalContext externalContext = context
            .getExternalContext();

        AddResource addResource = getInstance(externalContext.getContext(), externalContext.getRequestMap(),
                externalContext.getRequestContextPath(), MyfacesConfig
            .getCurrentInstance(externalContext)
            .getAddResourceClass());
        checkEnvironment(context, addResource);
        return addResource;
    }

    /**
     * @deprecated use getInstance(HttpServletRequest request, ServletContext servletContext) instead
     */
    public static AddResource getInstance(HttpServletRequest request)
    {
        ServletContext servletContext = request
            .getSession().getServletContext();
        Map requestMap = new RequestMapWrapper(request);
        AddResource addResource = getInstance(servletContext, requestMap, request
            .getContextPath(), MyfacesConfig
            .getAddResourceClassFromServletContext(servletContext));
        //
        // this will be called by the ExtensionsFilter itself, so no need to
        // check the environment
        //
        return addResource;
    }
    
    public static AddResource getInstance(HttpServletRequest request, ServletContext servletContext)
    {
        Map requestMap = new RequestMapWrapper(request);
        AddResource addResource = getInstance(servletContext, requestMap, request
            .getContextPath(), MyfacesConfig
            .getAddResourceClassFromServletContext(servletContext));
        //
        // this will be called by the ExtensionsFilter itself, so no need to
        // check the environment
        //
        return addResource;        
    }

    /**
     * check if the extensionsFilter has been correctly setup.
     */
    private static void checkEnvironment(FacesContext context, AddResource addResource)
    {
        ExternalContext extctx = context.getExternalContext();

        //a check for a servlet-filter only makes sense in a servlet-environment....
        //attention: checking for servlet-response doesn't work here - quite often, the portlet-response
        //is implemented as a servlet-response, and therefore the check is implemented inversely
        
        //if(extctx.getResponse() instanceof PortletResponse) {        
        if(ExternalContextUtils.getRequestType(extctx).isPortlet()) {
            return;
        }

        if (extctx.getApplicationMap().containsKey(ENV_CHECKED_KEY))
        {
            // already checked
            return;
        }

        if (!MyfacesConfig.getCurrentInstance(extctx).isCheckExtensionsFilter())
        {
            // checks disabled by user request
            extctx.getApplicationMap().put(ENV_CHECKED_KEY, Boolean.TRUE);
            return;
        }

        synchronized (extctx.getApplicationMap())
        {
            if (addResource.requiresBuffer())
            {
                if (!extctx.getRequestMap().containsKey(ExtensionsFilter.DOFILTER_CALLED))
                {
                    throwExtensionsFilterMissing("JSF mapping missing. JSF pages not covered.");
                }
            }

            boolean foundMapping = false;

            List facesServletMappings = WebXml.getWebXml(extctx).getFacesExtensionsFilterMappings();
            for (Iterator iterServletMappings = facesServletMappings.iterator(); iterServletMappings.hasNext();)
            {
                FilterMapping filterMapping = (FilterMapping) iterServletMappings.next();
                if (checkFilterPattern(extctx, filterMapping))
                {
                    foundMapping = true;
                    break;
                }
            }

            if (!foundMapping)
            {
                throwExtensionsFilterMissing("Resource mapping missing. Resources cant be delivered.");
            }

            extctx.getApplicationMap().put(ENV_CHECKED_KEY, Boolean.TRUE);
        }
    }

    protected static boolean checkFilterPattern(ExternalContext extCtxt, FilterMapping filterMapping)
    {
        if (filterMapping.getUrlPattern() != null &&
                ("/faces/*".equals(filterMapping.getUrlPattern()) ||
                        (MyfacesConfig.getCurrentInstance(extCtxt).getResourceVirtualPath()+"/*").equals(filterMapping.getUrlPattern())))
        {
            return true;
        }
        
        return false;
    }

    private static void throwExtensionsFilterMissing(String detailReason)
    {
        throw new IllegalStateException(
            "ExtensionsFilter not correctly configured. " + detailReason + " Please see: http://myfaces.apache.org/tomahawk/extensionsFilter.html");
    }
}
