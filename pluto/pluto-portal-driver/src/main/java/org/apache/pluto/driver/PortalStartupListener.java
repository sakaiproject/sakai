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
package org.apache.pluto.driver;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletContainerFactory;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.driver.config.AdminConfiguration;
import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.driver.config.DriverConfigurationException;
import org.springframework.web.context.WebApplicationContext;

/**
 * Listener used to start up / shut down the Pluto Portal Driver upon startup /
 * showdown of the servlet context in which it resides.
 * <p/>
 * Startup Includes:
 * <ol>
 * <li>Instantiation of the DriverConfiguration</li>
 * <li>Registration of the DriverConfiguration</li>
 * <li>Instantiation of the PortalContext</li>
 * <li>Registration of the PortalContext</li>
 * <li>Instantiation of the ContainerServices</li>
 * <li>Registration of the ContainerServices</li>
 * </ol>
 *
 * @version $Revision$ $Date$
 * @since Sep 22, 2004
 */
public class PortalStartupListener implements ServletContextListener {

    /**
     * Internal logger.
     */
    private static final Log LOG = LogFactory.getLog(
            PortalStartupListener.class);

    /**
     * The KEY with which the container is bound to the context.
     */
    private static final String CONTAINER_KEY = AttributeKeys.PORTLET_CONTAINER;

    /**
     * The KEY with which the driver configuration is bound to the context.
     */
    private static final String DRIVER_CONFIG_KEY = AttributeKeys.DRIVER_CONFIG;

    /**
     * The KEY with which the admin configuration is bound to the context.
     */
    private static final String ADMIN_CONFIG_KEY = AttributeKeys.DRIVER_ADMIN_CONFIG;

    // ServletContextListener Impl ---------------------------------------------

    /**
     * Receives the startup notification and subsequently starts up the portal
     * driver. The following are done in this order:
     * <ol>
     * <li>Retrieve the ResourceConfig File</li>
     * <li>Parse the ResourceConfig File into ResourceConfig Objects</li>
     * <li>Create a Portal Context</li>
     * <li>Create the ContainerServices implementation</li>
     * <li>Create the Portlet Container</li>
     * <li>Initialize the Container</li>
     * <li>Bind the configuration to the ServletContext</li>
     * <li>Bind the container to the ServletContext</li>
     * <ol>
     *
     * @param event the servlet context event.
     */
    public void contextInitialized(ServletContextEvent event) {
        LOG.info("Starting up Pluto Portal Driver. . .");

        ServletContext servletContext = event.getServletContext();

        WebApplicationContext springContext = (WebApplicationContext)
                servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        LOG.debug(" [1a] Loading DriverConfiguration. . . ");
        DriverConfiguration driverConfiguration = (DriverConfiguration)
                springContext.getBean("DriverConfiguration");

        driverConfiguration.init(servletContext);

        LOG.debug(" [1b] Registering DriverConfiguration. . .");
        servletContext.setAttribute(DRIVER_CONFIG_KEY, driverConfiguration);


        LOG.debug(" [2a] Loading Optional AdminConfiguration. . .");
        AdminConfiguration adminConfiguration = (AdminConfiguration)
                springContext.getBean("AdminConfiguration");

        if (adminConfiguration != null) {
            LOG.debug(" [2b] Registering Optional AdminConfiguration");
            servletContext.setAttribute(ADMIN_CONFIG_KEY, adminConfiguration);
        } else {
            LOG.info("Optional AdminConfiguration not found. Ignoring.");
        }

        initContainer(servletContext);

        LOG.info("********** Pluto Portal Driver Started **********\n\n");
    }

    /**
     * Recieve notification that the context is being shut down and subsequently
     * destroy the container.
     *
     * @param event the destrubtion event.
     */
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        if (LOG.isInfoEnabled()) {
            LOG.info("Shutting down Pluto Portal Driver...");
        }
        destroyContainer(servletContext);
        destroyAdminConfiguration(servletContext);
        destroyDriverConfiguration(servletContext);
        if (LOG.isInfoEnabled()) {
            LOG.info("********** Pluto Portal Driver Shut Down **********\n\n");
        }
    }


    /**
     * Initializes the portlet container. This method constructs and initializes
     * the portlet container, and saves it to the servlet context scope.
     *
     * @param servletContext the servlet context.
     */
    private void initContainer(ServletContext servletContext) {

        WebApplicationContext springContext = (WebApplicationContext)
                servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        // Retrieve the driver configuration from servlet context.
        DriverConfiguration driverConfig = (DriverConfiguration)
                servletContext.getAttribute(DRIVER_CONFIG_KEY);

        try {
            LOG.info("Initializing Portlet Container. . .");

            LOG.debug(" [1] Loading RequiredContainerServices. . .");
            RequiredContainerServices required =
                    (RequiredContainerServices) springContext.getBean("RequiredContainerServices");

            LOG.debug(" [2] Loading OptionalContainerServices. . .");
            OptionalContainerServices optional =
                    (OptionalContainerServices) springContext.getBean("OptionalContainerServices");


            // Create portlet container.
            LOG.debug(" [3] Creating portlet container...");
            PortletContainerFactory factory =
                    PortletContainerFactory.getInstance();
            PortletContainer container = factory.createContainer(
                driverConfig.getContainerName(), required, optional
            );

            // Initialize portlet container.
            LOG.debug(" [4] Initializing portlet container...");
            container.init(servletContext);

            // Save portlet container to the servlet context scope.
            servletContext.setAttribute(CONTAINER_KEY, container);
            LOG.info("Pluto portlet container started.");

        } catch (DriverConfigurationException ex) {
            LOG.error("Unable to retrieve driver configuration "
                    + "due to configuration error: " + ex.getMessage(), ex);
        } catch (PortletContainerException ex) {
            LOG.error("Unable to start up portlet container: "
                    + ex.getMessage(), ex);
        }
    }

    // Private Destruction Methods ---------------------------------------------

    /**
     * Destroyes the portlet container and removes it from servlet context.
     *
     * @param servletContext the servlet context.
     */
    private void destroyContainer(ServletContext servletContext) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Shutting down Pluto Portal Driver...");
        }
        PortletContainer container = (PortletContainer)
                servletContext.getAttribute(CONTAINER_KEY);
        if (container != null) {
            try {
                container.destroy();
                if (LOG.isInfoEnabled()) {
                    LOG.info("Pluto Portal Driver shut down.");
                }
            } catch (PortletContainerException ex) {
                LOG.error("Unable to shut down portlet container: "
                        + ex.getMessage(), ex);
            } finally {
                servletContext.removeAttribute(CONTAINER_KEY);
            }
        }
    }

    /**
     * Destroyes the portal driver config and removes it from servlet context.
     *
     * @param servletContext the servlet context.
     */
    private void destroyDriverConfiguration(ServletContext servletContext) {
        DriverConfiguration driverConfig = (DriverConfiguration)
                servletContext.getAttribute(DRIVER_CONFIG_KEY);
        if (driverConfig != null) {
            try {
                driverConfig.destroy();
                if (LOG.isInfoEnabled()) {
                    LOG.info("Pluto Portal Driver Config destroyed.");
                }
            } catch (DriverConfigurationException ex) {
                LOG.error("Unable to destroy portal driver config: "
                        + ex.getMessage(), ex);
            } finally {
                servletContext.removeAttribute(DRIVER_CONFIG_KEY);
            }
        }
    }

    /**
     * Destroyes the portal admin config and removes it from servlet context.
     *
     * @param servletContext the servlet context.
     */
    private void destroyAdminConfiguration(ServletContext servletContext) {
        AdminConfiguration adminConfig = (AdminConfiguration)
                servletContext.getAttribute(ADMIN_CONFIG_KEY);
        if (adminConfig != null) {
            try {
                adminConfig.destroy();
                if (LOG.isInfoEnabled()) {
                    LOG.info("Pluto Portal Admin Config destroyed.");
                }
            } catch (DriverConfigurationException ex) {
                LOG.error("Unable to destroy portal admin config: "
                        + ex.getMessage(), ex);
            } finally {
                servletContext.removeAttribute(ADMIN_CONFIG_KEY);
            }
        }
    }

}

