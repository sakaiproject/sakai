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
package org.apache.pluto.internal;

import org.apache.pluto.PlutoConfigurationException;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.services.PortletAppDescriptorService;
import org.apache.pluto.util.StringManager;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple caching mechanism used to manage portlet descriptors. This mechanism
 * takes special considerations to make sure that the cache is invalidated for
 * any ServletContext that is destroyed, thus allowing for a the context to be
 * redeployed.
 *
 * NOTE: This should only be used internally.  Do not access it from embedding
 * portals, instead, utilize the PortletRegistryService.
 *
 * @version 1.0
 * @since Nov 3, 2004
 */
public class PortletDescriptorRegistry {

    /** Portlet deployment descriptor location. */
    private static final String PORTLET_XML = "/WEB-INF/portlet.xml";

    /** Exception Messages. */
    private static final StringManager EXCEPTIONS = StringManager.getManager(
            PortletDescriptorRegistry.class.getPackage().getName());

    /** The static singleton registry instance. */
    private static final PortletDescriptorRegistry REGISTRY =
    		new PortletDescriptorRegistry();


    // Private Member Variables ------------------------------------------------

    /** The portlet application descriptor service. */
    private PortletAppDescriptorService portletDDService = null;

    /**
     * Cache of descriptors.  WeakHashMap is used so that
     * once the context is destroyed (kinda), the cache is eliminated.
     * Ideally we'd use a ServletContextListener, but at this
     * point I'm wondering if we really want to add another
     * config requirement in the servlet xml? Hmm. . .
     */
    private final Map cache = new WeakHashMap();


    // Constructor -------------------------------------------------------------

    /**
     * Returns the singleton registry instance.
     * @return the singleton registry instance.
     */
    public static PortletDescriptorRegistry getRegistry() {
        return REGISTRY;
    }

    /**
     * Private constructor that prevents external instantiation.
     * We must modify the context class loader in order for
     * the Configuration utility to find the properties file.
     * @throws PlutoConfigurationException  if fail to instantiate portlet
     *         application descriptor service.
     */
    private PortletDescriptorRegistry()
    throws PlutoConfigurationException {
        String className = Configuration.getPortletAppDescriptorServiceImpl();
        try {
            Class clazz = Class.forName(className);
            portletDDService = (PortletAppDescriptorService) clazz.newInstance();
        } catch (ClassNotFoundException ex) {
            throw new PlutoConfigurationException(
            		"Unable to find class " + className, ex);
        } catch (InstantiationException ex) {
            throw new PlutoConfigurationException(
            		"Unable to instantiate class " + className, ex);
        } catch (IllegalAccessException ex) {
            throw new PlutoConfigurationException(
            		"Unable to access class " + className, ex);
        }
    }


    // Public Methods ----------------------------------------------------------

    /**
     * Retrieve the Portlet Application Deployment Descriptor for the given
     * servlet context.  Create it if it does not allready exist.
     *
     * @param servletContext  the servlet context.
     * @return The portlet application deployment descriptor.
     * @throws PortletContainerException if the descriptor can not be found or parsed
     */
    public PortletAppDD getPortletAppDD(ServletContext servletContext)
    throws PortletContainerException {
        PortletAppDD portletAppDD = (PortletAppDD) cache.get(servletContext);
        if (portletAppDD == null) {
        	portletAppDD = createDefinition(servletContext);
            cache.put(servletContext, portletAppDD);
        }
        return portletAppDD;
    }


    // Private Methods ---------------------------------------------------------

    /**
     * Creates the portlet.xml deployment descriptor representation.
     *
     * @param servletContext  the servlet context for which the DD is requested.
     * @return the Portlet Application Deployment Descriptor.
     * @throws PortletContainerException
     */
    private PortletAppDD createDefinition(ServletContext servletContext)
    throws PortletContainerException {
        PortletAppDD portletAppDD = null;
        try {
            InputStream in = servletContext.getResourceAsStream(PORTLET_XML);
            portletAppDD = portletDDService.read(in);
        } catch (IOException ex) {
            throw new PortletContainerException(EXCEPTIONS.getString(
                    "error.context.descriptor.load",
                    new String[] { servletContext.getServletContextName() }),
                    ex);
        }
        return portletAppDD;
    }

}

