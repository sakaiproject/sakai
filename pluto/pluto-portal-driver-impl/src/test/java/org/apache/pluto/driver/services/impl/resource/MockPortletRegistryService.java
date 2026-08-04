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
package org.apache.pluto.driver.services.impl.resource;

import java.io.InputStream;
import java.util.Iterator;

import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;

import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.services.castor.PortletAppDescriptorServiceImpl;
import org.apache.pluto.spi.optional.PortletRegistryListener;
import org.apache.pluto.spi.optional.PortletRegistryService;

/**
 * A mock PortletRegistryService.  Supply an InputStream
 * to a portlet.xml file on construction.
 *
 * All methods are no-ops except for getPortletApplicationDescriptor(String).
 *
 *
 * @since Feb 28, 2007
 * @version $Id: MockPortletRegistryService.java 516329 2007-03-09 08:43:46Z cziegeler $
 */
public class MockPortletRegistryService implements PortletRegistryService
{
    PortletAppDD portletApp;

    public MockPortletRegistryService(InputStream portletXml)
    {
        PortletAppDescriptorServiceImpl svc = new PortletAppDescriptorServiceImpl();
        try
        {
            portletApp = svc.read(portletXml);
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Could not parse portlet xml.", e );
        }
    }

    public void addPortletRegistryListener(PortletRegistryListener listener)
    {

    }

    public PortletAppDD getPortletApplicationDescriptor(String name)
            throws PortletContainerException
    {
        return portletApp;
    }

    public PortletConfig getPortletConfig(String applicationId,
            String portletName) throws PortletContainerException
    {
        return null;
    }

    public PortletContext getPortletContext(String applicationId)
            throws PortletContainerException
    {
        return null;
    }

    public PortletDD getPortletDescriptor(String applicationId,
            String portletName) throws PortletContainerException
    {
        return null;
    }

    public Iterator getRegisteredPortletApplicationIds()
    {
        return null;
    }

    public Iterator getRegisteredPortletApplications()
    {
        return null;
    }

    public void removePortletRegistryListener(PortletRegistryListener listener)
    {

    }

}
