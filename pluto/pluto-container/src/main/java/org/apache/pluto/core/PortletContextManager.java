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
package org.apache.pluto.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.Configuration;
import org.apache.pluto.internal.InternalPortletConfig;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.internal.PortletDescriptorRegistry;
import org.apache.pluto.internal.impl.PortletConfigImpl;
import org.apache.pluto.internal.impl.PortletContextImpl;
import org.apache.pluto.spi.optional.PortletRegistryEvent;
import org.apache.pluto.spi.optional.PortletRegistryListener;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.apache.pluto.util.ClasspathScanner;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Manager used to cache the portlet configurations which have
 * been previously parsed.
 *
 * @version 1.0
 * @since Sep 20, 2004
 */
public class PortletContextManager implements PortletRegistryService {

    /**
     * Log Instance
     */
    private static final Log LOG = LogFactory.getLog(PortletContextManager.class);

    /**
     * The singleton manager instance.
     */
    private static final PortletContextManager MANAGER = new PortletContextManager();

    /**
     * List of application id resolvers. *
     */
    private static final List APP_ID_RESOLVERS = new ArrayList();

    // Private Member Variables ------------------------------------------------

    /**
     * The PortletContext cache map: key is servlet context, and value is the
     * associated portlet context.
     */
    private final Map portletContexts = new HashMap();


    private final Map portletConfigs = new HashMap();


    /**
     * The registered listeners that should be notified upon
     * registry events.
     */
    private final List registryListeners = new ArrayList();

    // Constructor -------------------------------------------------------------

    /**
     * Private constructor that prevents external instantiation.
     */
    private PortletContextManager() {
        // Do nothing.
    }

    /**
     * Returns the singleton manager instance.
     *
     * @return the singleton manager instance.
     */
    public static PortletContextManager getManager() {
        return MANAGER;
    }

    // Public Methods ----------------------------------------------------------

    /**
     * Retrieves the PortletContext associated with the given ServletContext.
     * If one does not exist, it is created.
     *
     * @param config the servlet config.
     * @return the InternalPortletContext associated with the ServletContext.
     * @throws PortletContainerException
     */
    public String register(ServletConfig config) throws PortletContainerException {
        InternalPortletContext portletContext = register(config.getServletContext());

        PortletAppDD portletAppDD =
            portletContext.getPortletApplicationDefinition();
        PortletDD portletDD = null;

        LOG.info("Registering "+portletAppDD.getPortlets().size()+" portlets for context "+portletContext.getApplicationId());

        for (Iterator it = portletAppDD.getPortlets().iterator(); it.hasNext();) {
            portletDD = (PortletDD) it.next();
            portletConfigs.put(
                portletContext.getApplicationId() + "/" + portletDD.getPortletName(),
                new PortletConfigImpl(config, portletContext, portletDD)
            );
        }

        return portletContext.getApplicationId();
    }

    /**
     * @param servletContext
     * @return
     * @throws PortletContainerException
     * @deprecated Use {@link #register(ServletConfig)}
     */
    public InternalPortletContext register(ServletContext servletContext)
        throws PortletContainerException {
        String applicationId = getContextPath(servletContext);
        if (!portletContexts.containsKey(applicationId)) {

            PortletAppDD portletAppDD = PortletDescriptorRegistry.getRegistry()
                .getPortletAppDD(servletContext);

            PortletContextImpl portletContext = new PortletContextImpl(
                applicationId, servletContext, portletAppDD);

            if (portletContext.getApplicationId() == null) {
                throw new IllegalStateException("Unable to resolve unique identifier for portletContext.");
            }
            portletContexts.put(applicationId, portletContext);

            fireRegistered(portletContext);

            if (LOG.isInfoEnabled()) {
                LOG.info("Registered portlet application with application id '" + applicationId + "'");
            }
        } else {
             if (LOG.isInfoEnabled()) {
                LOG.info("Portlet application with application id '" + applicationId + "' already registered.");
            }
        }


        return (InternalPortletContext) portletContexts.get(applicationId);
    }

    public void remove(InternalPortletContext context) {
        portletContexts.remove(context.getApplicationId());
        Iterator configs = portletConfigs.keySet().iterator();
        while (configs.hasNext()) {
            String key = (String) configs.next();
            if (key.startsWith(context.getApplicationId() + "/")) {
                configs.remove();
            }
        }
        fireRemoved(context);
    }

    public Iterator getRegisteredPortletApplicationIds() {
        return new HashSet(portletContexts.keySet()).iterator();

    }

    /**
     * Retrieve an iterator of all PortletContext instances
     * which exist within this application.
     *
     * @return
     */
    public Iterator getRegisteredPortletApplications() {
        return new HashSet(portletContexts.values()).iterator();
    }

    public PortletContext getPortletContext(String applicationId)
        throws PortletContainerException {
        return (InternalPortletContext) portletContexts.get(applicationId);
    }

    public PortletConfig getPortletConfig(String applicationId, String portletName) {
        String lookup = applicationId + "/" + portletName;
        if(!portletConfigs.containsKey(lookup)) {
            LOG.info("Unable to locate portlet config [applicationId="+applicationId+"]/["+portletName+"].");
        }
        return (InternalPortletConfig) portletConfigs.get(lookup);
    }

    public PortletDD getPortletDescriptor(String applicationId, String portletName) {
        InternalPortletConfig ipc = (InternalPortletConfig) portletConfigs.get(applicationId + "/" + portletName);
        if (ipc != null) {
            return ipc.getPortletDefinition();
        }
        LOG.warn("Unable to retrieve portlet descriptor: '"+applicationId+"/"+portletName+"'");
        return null;

    }

    public PortletAppDD getPortletApplicationDescriptor(String applicationId) throws PortletContainerException {
        InternalPortletContext ipc = (InternalPortletContext) portletContexts.get(applicationId);
        if (ipc != null) {
            return ipc.getPortletApplicationDefinition();
        }
        String msg = "Unable to retrieve portlet application descriptor: '"+applicationId+"'"; 
        LOG.warn(msg);
        throw new PortletContainerException(msg);
    }

    public void addPortletRegistryListener(PortletRegistryListener listener) {
        registryListeners.add(listener);
    }

    public void removePortletRegistryListener(PortletRegistryListener listener) {
        registryListeners.remove(listener);
    }

    private void fireRegistered(InternalPortletContext context) {
        PortletRegistryEvent event = new PortletRegistryEvent();
        event.setApplicationId(context.getApplicationId());
        event.setPortletApplicationDescriptor(context.getPortletApplicationDefinition());

        Iterator i = registryListeners.iterator();
        while (i.hasNext()) {
            ((PortletRegistryListener) i.next()).portletApplicationRegistered(event);
        }

        LOG.info("Portlet Context '" + context.getApplicationId() + "' registered.");
    }

    private void fireRemoved(InternalPortletContext context) {
        PortletRegistryEvent event = new PortletRegistryEvent();
        event.setApplicationId(context.getApplicationId());
        event.setPortletApplicationDescriptor(context.getPortletApplicationDefinition());

        Iterator i = registryListeners.iterator();
        while (i.hasNext()) {
            ((PortletRegistryListener) i.next()).portletApplicationRemoved(event);
        }

        LOG.info("Portlet Context '" + context.getApplicationId() + "' removed.");
    }

//
// Utility

    public static ServletContext getPortletContext(ServletContext portalContext, String portletContextPath) {
        if (Configuration.preventUnecessaryCrossContext()) {
            String portalPath = getContextPath(portalContext);
            if (portalPath.equals(portletContextPath)) {
                return portalContext;
            }
        }
        
        //Hack to deal with inconsistence in root context handling between
        //ServletContext.getContextPath and ServletContext.getContext
        if ("".equals(portletContextPath)) {
            portletContextPath = "/";
        }
        return portalContext.getContext(portletContextPath);
    }

    /**
     * Servlet 2.5 ServletContext.getContextPath() method.
     */
    private static Method contextPathGetter;

    static {
        try {
            contextPathGetter = ServletContext.class.getMethod("getContextPath", new Class[0]);
        }
        catch (NoSuchMethodException e) {
            LOG.warn("Servlet 2.4 or below detected.  Unable to find getContextPath on ServletContext.");
        }
    }

    protected static String getContextPath(ServletContext context) {
        String contextPath = null;
        if (contextPathGetter != null) {
            try {
                contextPath = (String) contextPathGetter.invoke(context, new Class[0]);
            } catch (Exception e) {
                LOG.warn("Unable to directly retrieve context path from ServletContext. Computing. . . ");
            }
        }

        if (contextPath == null) {
            contextPath = computeContextPath(context);
        }

        return contextPath;
    }


    protected static String computeContextPath(ServletContext context) {
        if (APP_ID_RESOLVERS.size() < 1) {
            List classes = null;
            try {
                classes = ClasspathScanner.findConfiguredImplementations(ApplicationIdResolver.class);
            } catch (IOException e) {
                throw new RuntimeException("Unable to find any ApplicationIdResolvers");
            }
            Iterator i = classes.iterator();
            while (i.hasNext()) {
                Class c = (Class) i.next();
                try {
                    APP_ID_RESOLVERS.add(c.newInstance());
                } catch (Exception e) {
                    LOG.warn("Unable to instantiate ApplicationIdResolver for class " + c.getName());
                }
            }
            if (LOG.isInfoEnabled()) {
                LOG.info("Found " + APP_ID_RESOLVERS.size() + " application id resolvers.");
            }
        }

        String path = null;
        int authority = Integer.MAX_VALUE;

        Iterator i = APP_ID_RESOLVERS.iterator();
        while (i.hasNext()) {
            ApplicationIdResolver resolver = (ApplicationIdResolver) i.next();
            if (resolver.getAuthority() < authority || path == null) {
                authority = resolver.getAuthority();
                String temp = resolver.resolveApplicationId(context);
                if (temp != null) {
                    path = temp;
                }
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved application id '" + path + "' with authority " + authority);
        }
        return path;
    }

}
