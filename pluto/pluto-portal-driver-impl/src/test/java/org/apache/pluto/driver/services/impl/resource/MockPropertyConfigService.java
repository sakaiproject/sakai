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

import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.pluto.driver.config.DriverConfigurationException;
import org.apache.pluto.driver.services.portal.PropertyConfigService;

/**
 * A mock PropertyConfigService.
 *
 * The init() and destroy() methods are no-ops.
 *
 * @since Feb 28, 2007
 * @version $Id: MockPropertyConfigService.java 516329 2007-03-09 08:43:46Z cziegeler $
 */
class MockPropertyConfigService implements PropertyConfigService
{

    private ResourceConfig config;
    private static final String configFile = "/pluto-portal-driver-config.xml";

    public MockPropertyConfigService()
    {
        this(configFile);
    }

    public MockPropertyConfigService(String configFile)
    {
        ResourceConfigReader r = ResourceConfigReader.getFactory();
        try
        {
            config = r.parse(this.getClass().getResourceAsStream(configFile));
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Unable to create ResourceConfig.", e );
        }
    }

    public String getContainerName()
    {
        return config.getContainerName();
    }

    public String getPortalName()
    {
        return config.getPortalName();
    }

    public String getPortalVersion()
    {
        return config.getPortalVersion();
    }

    public Set getSupportedPortletModes()
    {
        return config.getSupportedPortletModes();
    }

    public Set getSupportedWindowStates()
    {
        return config.getSupportedWindowStates();
    }

    public void destroy() throws DriverConfigurationException
    {
        // TODO Auto-generated method stub

    }

    public void init(ServletContext ctx) throws DriverConfigurationException
    {
        // TODO Auto-generated method stub

    }

}
