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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.portlet.WindowState;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.descriptors.portlet.CustomWindowStateDD;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.config.DriverConfigurationException;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.services.portal.PropertyConfigService;
import org.apache.pluto.driver.services.portal.SupportedWindowStateService;
import org.apache.pluto.spi.optional.PortletRegistryService;

public class SupportedWindowStateServiceImpl implements
        SupportedWindowStateService 
{
    
    /** Logger **/
    private static final Log LOG = LogFactory.getLog(SupportedWindowStateServiceImpl.class);
    
    /**
     * Servlet context used to get a handle on the portlet container
     */
    private ServletContext servletContext = null;
    
    /** 
     * PropertyConfigService is injected by Spring.  We
     * use it to obtain the window states that the portal
     * supports.  It is protected only so that the unit
     * tests have access to the field.
     */
    protected PropertyConfigService propertyService = null;
    
    /**
     * PortletRegistry is obtained from the PortletContainer on 
     * this service's initialization.  It is protected only
     * so that the unit tests have access to the field.
     * 
     * Note that it is an optional container service, but 
     * this implmentation requires it.
     */
    protected PortletRegistryService portletRegistry = null;
    
    /**
     * Contains String objects of window states supported
     * by the portal (obtained from PropertyConfigService).
     * It is protected only so that the unit tests have 
     * access to the field.
     */
    protected Set portalSupportedWindowStates = new HashSet(3);
    
    /**
     * Window States that are specified in PLT.9
     */
    protected static final Set JSR168_WINDOW_STATES;
    static
    {
        JSR168_WINDOW_STATES = new HashSet(3);
        JSR168_WINDOW_STATES.add(WindowState.MAXIMIZED);
        JSR168_WINDOW_STATES.add(WindowState.MINIMIZED);
        JSR168_WINDOW_STATES.add(WindowState.NORMAL);
    }
    
    private SupportedWindowStateServiceImpl()
    {
        // this impl requires a PropertyConfigService on construction.
    }
    
    public SupportedWindowStateServiceImpl( PropertyConfigService propertyService )
    {
        this.propertyService = propertyService;
    }
    
    public boolean isWindowStateSupported(String portletId, String state)
    {
        // If the supplied window state is a JSR 168 window state,
        // we can return immediately
        if ( JSR168_WINDOW_STATES.contains(state) )
        {
            return true;
        }
        
        // Otherwise we need to check for custom window states
        
        return isWindowStateSupportedByPortal(state) && 
            isWindowStateSupportedByPortlet(portletId, state);
    }

    public boolean isWindowStateSupportedByPortal(String state)
    {
        return portalSupportedWindowStates.contains(state);
    }

    public boolean isWindowStateSupportedByPortlet(String portletId, String state)
    {        
        if ( portletId == null || 
             state == null || 
             portletId.trim().equals("") || 
             state.trim().equals(""))
        {
            StringBuffer errMsg = new StringBuffer( "Cannot determine supported window " +
                "states for portletId [" + portletId + "] and window state [" + state + "].  " );
            String msg = errMsg.append( "One or both of the arguments is empty or null." ).toString();
            LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }
        
        // We can short-circut the registry lookup if the window state is
        // one from PLT.9
        if ( JSR168_WINDOW_STATES.contains(new WindowState(state)) )
        {
            return true;
        }
        
        // If the supplied window state isn't a JSR 168 window state,
        // we look to see if it is a custom window state.
        
        String appId = PortletWindowConfig.parseContextPath(portletId);
        PortletAppDD portletAppDD = null;
        
        if (portletRegistry == null) 
        {                        
            portletRegistry = getPortletRegistryService();
            if ( portletRegistry == null )
            {
                return false;
            }
        }
        
        try
        {
            portletAppDD = portletRegistry.getPortletApplicationDescriptor(appId);
        }
        catch ( PortletContainerException e )
        {
            StringBuffer errMsg = new StringBuffer( "Cannot determine supported window " +
                    "states for portletId [" + portletId + "] and window state [" + state + "].  " );
            String msg = errMsg.append( "Unable to access the Portlet Registry Service." ).toString();
            LOG.error( msg, e );
        }
        
        List customWindowStates = portletAppDD.getCustomWindowStates();
        if ( customWindowStates != null )
        {
            for ( Iterator i = customWindowStates.iterator(); i.hasNext(); )
            {
                CustomWindowStateDD customState = (CustomWindowStateDD)i.next();
                if ( customState.getWindowState().equals(state))
                {
                    return true;
                }
            }
        }
        
        return false;
    }

    public void destroy() throws DriverConfigurationException
    {
        LOG.debug( "Destroying SupportedWindowStateService... " );
        portletRegistry = null;
        propertyService = null;
        portalSupportedWindowStates = null;
        LOG.debug( "SupportedWindowStateService destroyed." );
    }

    public void init(ServletContext ctx) throws DriverConfigurationException
    {
        LOG.debug( "Initializing SupportedWindowStateService... " );

        servletContext = ctx;
        
        portalSupportedWindowStates = propertyService.getSupportedWindowStates();
        if ( LOG.isDebugEnabled() )
        {
            StringBuffer msg = new StringBuffer();
            
            if ( portalSupportedWindowStates != null )
            {
                msg.append( "Portal supports [" + portalSupportedWindowStates.size() + "] window states.  ");
                for ( Iterator i = portalSupportedWindowStates.iterator(); i.hasNext(); )
                {
                    msg.append( "[" + i.next() + "]" );
                    if ( i.hasNext() )
                    {
                        msg.append(", ");
                    }
                }
                LOG.debug(msg.toString());
            }    
        }
        
        if ( portalSupportedWindowStates == null )
        {
            final String msg = "Portal supported window states is null!";
            LOG.error( msg );
            throw new DriverConfigurationException( msg );
        }
        LOG.debug( "SupportedWindowStateService initialized." );        
    }

    private PortletRegistryService getPortletRegistryService()
    {
        PortletContainer container = ((PortletContainer)servletContext                    
                .getAttribute(AttributeKeys.PORTLET_CONTAINER));
        
        if ( container == null )
        {
            // should never happen
            final String msg = "Unable to obtain an instance of the container.";
            LOG.fatal( msg );
            throw new NullPointerException( msg );
        }       
        
        if ( container.getOptionalContainerServices() == null ||
             container.getOptionalContainerServices().getPortletRegistryService() == null )
        {
            final String msg = "Unable to obtain the portlet registry.  The supported window state " +
                "service cannot support custom window states.";
            LOG.info( msg );
            return null;
        }
        
        return container.getOptionalContainerServices().getPortletRegistryService();
    }
}
