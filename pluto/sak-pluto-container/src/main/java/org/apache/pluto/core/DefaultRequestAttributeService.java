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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletWindowID;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.UserAttributeDD;
import org.apache.pluto.spi.optional.PortletRegistryService;
import org.apache.pluto.spi.optional.RequestAttributeService;
import org.apache.pluto.spi.optional.UserInfoService;
import org.apache.pluto.util.NamespaceMapper;
import org.apache.pluto.util.impl.NamespaceMapperImpl;

/**
 * Delegates request attribute storage and retrieval to the passed HttpServletRequest. Also includes logic for retrieving
 * and filtering the {@link PortletRequest#USER_INFO} Map in {@link #createUserInfoMap(PortletRequest, PortletWindow)}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DefaultRequestAttributeService implements RequestAttributeService {
    private static final Log LOG = LogFactory.getLog(DefaultRequestAttributeService.class);

    
    private final NamespaceMapper mapper = new NamespaceMapperImpl();

    private OptionalContainerServices optionalContainerServices;
    
    public DefaultRequestAttributeService() {
    }
    
    public DefaultRequestAttributeService(OptionalContainerServices optionalContainerServices) {
        this.optionalContainerServices = optionalContainerServices;
    }
    
    public OptionalContainerServices getOptionalContainerServices() {
        return optionalContainerServices;
    }
    public void setOptionalContainerServices(OptionalContainerServices optionalContainerServices) {
        this.optionalContainerServices = optionalContainerServices;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.RequestAttributeService#getAttribute(javax.portlet.PortletRequest, javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String)
     */
    public Object getAttribute(PortletRequest portletRequest, HttpServletRequest httpServletRequest, PortletWindow portletWindow, String name) {
        if (PortletRequest.USER_INFO.equals(name)) {
            Map userInfo = (Map)httpServletRequest.getAttribute(PortletRequest.USER_INFO);
            // Fix for PLUTO-486, cache the user_info map as a request attribute
            if (userInfo == null) {
                userInfo = this.createUserInfoMap(portletRequest, portletWindow);
                httpServletRequest.setAttribute(PortletRequest.USER_INFO, userInfo);
            }
            return userInfo;
        }
        
        final String encodedName = this.encodeAttributeName(portletWindow, name);

        final Object attribute = httpServletRequest.getAttribute(encodedName);
        if (attribute != null) {
            return attribute;
        }
        
        return httpServletRequest.getAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.RequestAttributeService#getAttributeNames(javax.portlet.PortletRequest, javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow)
     */
    public Enumeration getAttributeNames(PortletRequest portletRequest, HttpServletRequest httpServletRequest, PortletWindow portletWindow) {
        final Enumeration attributes = httpServletRequest.getAttributeNames();

        final Vector portletAttributes = new Vector();
        while (attributes.hasMoreElements()) {
            final String attribute = (String) attributes.nextElement();

            final String portletAttribute;
            if (this.isNameReserved(attribute)) {
                portletAttribute = attribute;
            }
            else {
                final PortletWindowID portletWindowId = portletWindow.getId();
                portletAttribute = this.mapper.decode(portletWindowId, attribute);
            }

            if (portletAttribute != null) { // it is in the portlet's namespace
                portletAttributes.add(portletAttribute);
            }
        }
        
        //TODO should PortletRequest.USER_INFO be added?

        return portletAttributes.elements();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.RequestAttributeService#removeAttribute(javax.portlet.PortletRequest, javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String)
     */
    public void removeAttribute(PortletRequest portletRequest, HttpServletRequest httpServletRequest, PortletWindow portletWindow, String name) {
        final String encodedName = this.encodeAttributeName(portletWindow, name);
        
        httpServletRequest.removeAttribute(encodedName);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.optional.RequestAttributeService#setAttribute(javax.portlet.PortletRequest, javax.servlet.http.HttpServletRequest, org.apache.pluto.PortletWindow, java.lang.String, java.lang.Object)
     */
    public void setAttribute(PortletRequest portletRequest, HttpServletRequest httpServletRequest, PortletWindow portletWindow, String name, Object value) {
        final String encodedName = this.encodeAttributeName(portletWindow, name);
        
        if (value == null) {
            httpServletRequest.removeAttribute(name);
        }
        else {
            httpServletRequest.setAttribute(encodedName, value);
        }
    }

    /**
     * Is this attribute name a reserved name (by the J2EE spec)?. Reserved
     * names begin with "java." or "javax.".
     *
     * @return true if the name is reserved.
     */
    protected boolean isNameReserved(String name) {
        return name.startsWith("java.") || name.startsWith("javax.");
    }

    /**
     * Encodes the attribute name using the portlet window id if it is not reserved 
     */
    protected String encodeAttributeName(PortletWindow portletWindow, String name) {
        final String encodedName;
        if (this.isNameReserved(name)) {
            encodedName = name;
        }
        else {
            final PortletWindowID portletWindowId = portletWindow.getId();
            encodedName = this.mapper.encode(portletWindowId, name);
        }
        return encodedName;
    }

    /**
     * Retrieves the UserInfoService from the OptionalContainerServices, gets the user-info Map
     * and fitlers it for the attributes in the portlet descriptor.
     */
    protected Map createUserInfoMap(PortletRequest portletRequest, PortletWindow portletWindow) {
        final Map userInfoMap = new HashMap();
        try {
            final UserInfoService userInfoService = this.optionalContainerServices.getUserInfoService();

            //PLUTO-388 fix:
            //The PortletWindow is currently ignored in the implementing class
            // See: org.apache.pluto.core.DefaultUserInfoService
            final Map allMap = userInfoService.getUserInfo(portletRequest, portletWindow);

            //PLUTO-477 null attribute maps are ok
            if (null == allMap) {
                return null;
            }

            final PortletRegistryService portletRegistryService = optionalContainerServices.getPortletRegistryService();
            final PortletAppDD dd = portletRegistryService.getPortletApplicationDescriptor(portletWindow.getContextPath());

            final List mappedUserAttributes = dd.getUserAttributes();
            for (final Iterator mappedAttrItr = mappedUserAttributes.iterator(); mappedAttrItr.hasNext(); ) {
                final UserAttributeDD userAttrDD = (UserAttributeDD) mappedAttrItr.next();
                final String mappedName = userAttrDD.getName();
                final Object value = allMap.get(mappedName);
                if (value != null) {
                    userInfoMap.put(mappedName, value);
                }
            }
        }
        catch (PortletContainerException e) {
            LOG.warn("Unable to retrieve user attribute map for user " + portletRequest.getRemoteUser() + ".  Returning null.");
            return null;
        }

        return Collections.unmodifiableMap(userInfoMap);
    }
}
