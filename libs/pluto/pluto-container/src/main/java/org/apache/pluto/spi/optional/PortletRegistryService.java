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
package org.apache.pluto.spi.optional;

import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.PortletContainerException;

import javax.portlet.PortletContext;
import javax.portlet.PortletConfig;
import java.util.Iterator;

/**
 * Interface defining the services used by the container
 * to access portlet application descriptors.  The registry
 * acts as both internally as descriptor cache and publically
 * as a mechanism for notifying the container of new applications.
 *
 * @since 1.1.0
 *
 */
public interface PortletRegistryService {

    /**
     * Retrieve all registered applications.  This list
     * will only contain those applications which have
     * been registered with the container.  Others may
     * or may not be available within the servers.
     *
     * @return iterator of all application descriptors.
     */
    Iterator getRegisteredPortletApplications();

    /**
     * Retrieve the ids of all registered applications.
     * This list will only contain those applications
     * which have been registered with the container.
     * Others may or may not be available within
     * the servers.
     *
     * @return iterator of all ids (strings).
     */
    Iterator getRegisteredPortletApplicationIds();

    /**
     * Retrieve the portlet descriptor for the specified
     * portlet application. If the name does not match
     * the name of a contextPath registered with the container
     * the portlet application name must be checked.
     *
     * @param name the name of the portlet application.
     * @return the named portlet application descriptor.
     * @throws PortletContainerException if the descriptor
     *         can not be found or if the portlet.xml can not be parsed.
     */
    PortletAppDD getPortletApplicationDescriptor(String name)
        throws PortletContainerException;

    /**
     * Retrieve the PortletContext for the specified applicationId
     *
     * @param applicationId context identifier
     * @return portlet context
     * @throws PortletContainerException if internal error occurs
     */
    PortletContext getPortletContext(String applicationId)
        throws PortletContainerException;

    /**
     * Retreive the portlet descriptor for the given portlet.
     *
     * @param applicationId context identifier
     * @param portletName portlet name
     * @return descriptor
     * @throws PortletContainerException if unexpected error
     */
    PortletDD getPortletDescriptor(String applicationId, String portletName)
        throws PortletContainerException;

    /**
     * Retrieve the portlet configuration for the specified portlet
     * @param applicationId context identifier
     * @param portletName portlet name
     * @return portletconfig
     * @throws PortletContainerException if internal error occurs
     */
    PortletConfig getPortletConfig(String applicationId, String portletName)
        throws PortletContainerException;

    /**
     * Add a listener which will recieve notifications of newly
     * registered applications.
     *
     * @param listener the listener to add
     */
    void addPortletRegistryListener(PortletRegistryListener listener);

    /**
     * Remove a previously registered listener.
     *
     * @param listener the listener to remove
     */
    void removePortletRegistryListener(PortletRegistryListener listener);



}
