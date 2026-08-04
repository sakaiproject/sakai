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
package org.apache.pluto.internal.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.portlet.PortalContext;
import javax.portlet.PortletContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.WindowState;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.descriptors.common.SecurityRoleRefDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
import org.apache.pluto.internal.InternalPortletRequest;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.internal.PortletEntity;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.RequestAttributeService;
import org.apache.pluto.util.ArgumentUtility;
import org.apache.pluto.util.Enumerator;
import org.apache.pluto.util.StringManager;
import org.apache.pluto.util.StringUtils;

/**
 * Abstract <code>javax.portlet.PortletRequest</code> implementation.
 * This class also implements InternalPortletRequest.
 *
 */
public abstract class PortletRequestImpl extends HttpServletRequestWrapper
    implements PortletRequest, InternalPortletRequest {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(PortletRequestImpl.class);

    private static final StringManager EXCEPTIONS =
        StringManager.getManager(PortletRequestImpl.class.getPackage().getName());

    // Private Member Variables ------------------------------------------------

    /**
     * The parent container within which this request was created.
     */
    private PortletContainer container;

    /**
     * The portlet window which is the target of this portlet request.
     */
    private InternalPortletWindow internalPortletWindow;

    /**
     * The PortletContext associated with this Request. This PortletContext must
     * be initialized from within the <code>PortletServlet</code>.
     */
    private PortletContext portletContext;

    /**
     * The PortalContext within which this request is occuring.
     */
    private PortalContext portalContext;

    /**
     * The portlet session.
     */
    private PortletSession portletSession;

    /**
     * Response content types.
     */
    private Vector contentTypes;

    /**
     * FIXME: do we really need this?
     * Flag indicating if the HTTP-Body has been accessed.
     */
    private boolean bodyAccessed = false;

    // Constructors ------------------------------------------------------------

    public PortletRequestImpl(InternalPortletRequest internalPortletRequest) {
        this(internalPortletRequest.getPortletContainer(),
            internalPortletRequest.getInternalPortletWindow(),
            internalPortletRequest.getHttpServletRequest());
    }

    /**
     * Creates a PortletRequestImpl instance.
     *
     * @param container             the portlet container.
     * @param internalPortletWindow the internal portlet window.
     * @param servletRequest        the underlying servlet request.
     */
    public PortletRequestImpl(PortletContainer container,
                              InternalPortletWindow internalPortletWindow,
                              HttpServletRequest servletRequest) {
        super(servletRequest);
        this.container = container;
        this.internalPortletWindow = internalPortletWindow;
        this.portalContext = container.getRequiredContainerServices().getPortalContext();
    }

    // PortletRequest Impl -----------------------------------------------------

    /**
     * Determine whether or not the specified WindowState is allowed for this
     * portlet.
     *
     * @param state the state in question
     * @return true if the state is allowed.
     */
    public boolean isWindowStateAllowed(WindowState state) {
        for (Enumeration en = portalContext.getSupportedWindowStates();
             en.hasMoreElements();) {
            if (en.nextElement().toString().equalsIgnoreCase(state.toString())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPortletModeAllowed(PortletMode mode) {
        return (isPortletModeAllowedByPortlet(mode)
            && isPortletModeAllowedByPortal(mode));
    }

    public PortletMode getPortletMode() {
        return internalPortletWindow.getPortletMode();
    }

    public WindowState getWindowState() {
        return internalPortletWindow.getWindowState();
    }

    public PortletSession getPortletSession() {
        return getPortletSession(true);
    }

    /**
     * Returns the portlet session.
     * <p/>
     * Note that since portlet request instance is created everytime the portlet
     * container receives an incoming request, the portlet session instance held
     * by the request instance is also re-created for each incoming request.
     * </p>
     */
    public PortletSession getPortletSession(boolean create) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retreiving portlet session (create=" + create + ")");
        }
        //
        // It is critical that we don't retrieve the portlet session until the
        //   cross context dispatch has been completed.  If we do then we risk
        //   having a cached version which is invalid for the context within
        //   which it exists.
        //
        if (portletContext == null) {
            throw new IllegalStateException(
                EXCEPTIONS.getString("error.session.illegalState"));
        }
        //
        // We must make sure that if the session has been invalidated (perhaps
        //   through setMaxIntervalTimeout()) and the underlying request
        //   returns null that we no longer use the cached version.
        // We have to check (ourselves) if the session has exceeded its max
        //   inactive interval. If so, we should invalidate the underlying
        //   HttpSession and recreate a new one (if the create flag is set to
        //   true) -- We just cannot depend on the implementation of
        //   javax.servlet.http.HttpSession!
        //
        HttpSession httpSession = getHttpServletRequest().getSession(create);
        if (httpSession != null) {
            // HttpSession is not null does NOT mean that it is valid.
            int maxInactiveInterval = httpSession.getMaxInactiveInterval();
            long lastAccesstime = httpSession.getLastAccessedTime();
            if (maxInactiveInterval >= 0 && lastAccesstime > 0) {    // < 0 => Never expires.
                long maxInactiveTime = httpSession.getMaxInactiveInterval() * 1000L;
                long currentInactiveTime = System.currentTimeMillis() - lastAccesstime;
                if (currentInactiveTime > maxInactiveTime) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("The underlying HttpSession is expired and "
                            + "should be invalidated.");
                    }
                    httpSession.invalidate();
                    httpSession = getHttpServletRequest().getSession(create);
                    // a cached portletSession is no longer useable.
                    // a new one will be created below.
                    portletSession = null;
                }
            }
        }

        if (httpSession == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("The underlying HttpSession is not available: "
                    + "no session will be returned.");
            }
            return null;
        }

        //
        // If we reach here, we are sure that the underlying HttpSession is
        //   available. If we haven't created and cached a portlet session
        //   instance, we will create and cache one now.
        //
        if (portletSession == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating new portlet session...");
            }
            final OptionalContainerServices optionalContainerServices = container.getOptionalContainerServices();
            final PortletEnvironmentService portletEnvironmentService = optionalContainerServices.getPortletEnvironmentService();
            
            portletSession = portletEnvironmentService.createPortletSession(container, 
                                                                            getHttpServletRequest(),
                                                                            portletContext, 
                                                                            httpSession, 
                                                                            internalPortletWindow);
        }
        return portletSession;
    }

    public String getProperty(String name) throws IllegalArgumentException {
        ArgumentUtility.validateNotNull("propertyName", name);
        String property = this.getHttpServletRequest().getHeader(name);
        if (property == null) {
            Map propertyMap = container.getRequiredContainerServices()
                .getPortalCallbackService()
                .getRequestProperties(
                    getHttpServletRequest(),
                    internalPortletWindow);

            if (propertyMap != null) {
                String[] properties = (String[]) propertyMap.get(name);
                if (properties != null && properties.length > 0) {
                    property = properties[0];
                }
            }
        }
        return property;
    }

    public Enumeration getProperties(String name) {
        ArgumentUtility.validateNotNull("propertyName", name);
        Set v = new HashSet();
        Enumeration props = this.getHttpServletRequest().getHeaders(name);
        if (props != null) {
            while (props.hasMoreElements()) {
                v.add(props.nextElement());
            }
        }

        // get properties from PropertyManager
        Map map = container.getRequiredContainerServices()
            .getPortalCallbackService()
            .getRequestProperties(
                getHttpServletRequest(),
                internalPortletWindow);

        if (map != null) {
            String[] properties = (String[]) map.get(name);

            if (properties != null) {
                // add properties to vector
                for (int i = 0; i < properties.length; i++) {
                    v.add(properties[i]);
                }
            }
        }

        return new Enumerator(v.iterator());
    }

    public Enumeration getPropertyNames() {
        Set v = new HashSet();

        // get properties from PropertyManager
        Map map = container.getRequiredContainerServices()
            .getPortalCallbackService()
            .getRequestProperties(getHttpServletRequest(), internalPortletWindow);

        if (map != null) {
            v.addAll(map.keySet());
        }

        // get properties from request header
        Enumeration props = this.getHttpServletRequest().getHeaderNames();
        if (props != null) {
            while (props.hasMoreElements()) {
                v.add(props.nextElement());
            }
        }

        return new Enumerator(v.iterator());
    }

    public PortalContext getPortalContext() {
        return container.getRequiredContainerServices().getPortalContext();
    }

    public String getAuthType() {
        return this.getHttpServletRequest().getAuthType();
    }

    public String getContextPath() {
        String contextPath = internalPortletWindow.getContextPath();
        if ("/".equals(contextPath)) {
            contextPath = "";
        }
        return contextPath;
    }

    public String getRemoteUser() {
        return this.getHttpServletRequest().getRemoteUser();
    }

    public Principal getUserPrincipal() {
        return this.getHttpServletRequest().getUserPrincipal();
    }

    /**
     * Determines whether a user is mapped to the specified role.  As specified
     * in PLT-20-3, we must reference the &lt;security-role-ref&gt; mappings
     * within the deployment descriptor. If no mapping is available, then, and
     * only then, do we check use the actual role name specified against the web
     * application deployment descriptor.
     *
     * @param roleName the name of the role
     * @return true if it is determined the user has the given role.
     */
    public boolean isUserInRole(String roleName) {
        PortletEntity entity = internalPortletWindow.getPortletEntity();
        PortletDD def = entity.getPortletDefinition();

        SecurityRoleRefDD ref = null;
        Iterator refs = def.getSecurityRoleRefs().iterator();
        while (refs.hasNext()) {
            SecurityRoleRefDD r = (SecurityRoleRefDD) refs.next();
            if (r.getRoleName().equals(roleName)) {
                ref = r;
                break;
            }
        }

        String link;
        if (ref != null && ref.getRoleLink() != null) {
            link = ref.getRoleLink();
        } else {
            link = roleName;
        }

        return this.getHttpServletRequest().isUserInRole(link);
    }

    public Object getAttribute(String name) {
        ArgumentUtility.validateNotNull("attributeName", name);

        final OptionalContainerServices optionalContainerServices = container.getOptionalContainerServices();
        final RequestAttributeService requestAttributeService = optionalContainerServices.getRequestAttributeService();
        return requestAttributeService.getAttribute(this, this.getHttpServletRequest(), this.internalPortletWindow, name);
    }

    public Enumeration getAttributeNames() {
        final OptionalContainerServices optionalContainerServices = container.getOptionalContainerServices();
        final RequestAttributeService requestAttributeService = optionalContainerServices.getRequestAttributeService();
        return requestAttributeService.getAttributeNames(this, this.getHttpServletRequest(), this.internalPortletWindow);
    }

    public String getParameter(String name) {
        ArgumentUtility.validateNotNull("parameterName", name);
        String[] values = (String[]) baseGetParameterMap().get(name);
        if (values != null && values.length > 0) {
            return values[0];
        } else {
            return null;
        }
    }

    public Enumeration getParameterNames() {
        return Collections.enumeration(baseGetParameterMap().keySet());
    }

    public String[] getParameterValues(String name) {
        ArgumentUtility.validateNotNull("parameterName", name);
        String[] values = (String[]) baseGetParameterMap().get(name);
        if (values != null) {
            values = StringUtils.copy(values);
        }
        return values;
    }

    public Map getParameterMap() {
        return StringUtils.copyParameters(baseGetParameterMap());
    }

    public boolean isSecure() {
        return this.getHttpServletRequest().isSecure();
    }

    public void setAttribute(String name, Object value) {
        ArgumentUtility.validateNotNull("attributeName", name);

        final OptionalContainerServices optionalContainerServices = container.getOptionalContainerServices();
        final RequestAttributeService requestAttributeService = optionalContainerServices.getRequestAttributeService();
        requestAttributeService.setAttribute(this, this.getHttpServletRequest(), this.internalPortletWindow, name, value);
    }

    public void removeAttribute(String name) {
        ArgumentUtility.validateNotNull("attributeName", name);

        final OptionalContainerServices optionalContainerServices = container.getOptionalContainerServices();
        final RequestAttributeService requestAttributeService = optionalContainerServices.getRequestAttributeService();
        requestAttributeService.removeAttribute(this, this.getHttpServletRequest(), this.internalPortletWindow, name);
    }

    public String getRequestedSessionId() {
        return this.getHttpServletRequest().getRequestedSessionId();
    }

    public boolean isRequestedSessionIdValid() {
        if (LOG.isDebugEnabled()) {
            LOG.debug(" ***** IsRequestedSessionIdValid? " + getHttpServletRequest().isRequestedSessionIdValid());
        }
        return getHttpServletRequest().isRequestedSessionIdValid();
    }

    public String getResponseContentType() {
        Enumeration enumeration = getResponseContentTypes();
        while (enumeration.hasMoreElements()) {
            return (String) enumeration.nextElement();
        }
        return "text/html";
    }

    public Enumeration getResponseContentTypes() {
        if (contentTypes == null) {
            contentTypes = new Vector();
            PortletDD dd = internalPortletWindow.getPortletEntity().getPortletDefinition();
            Iterator supports = dd.getSupports().iterator();
            while (supports.hasNext()) {
                SupportsDD sup = (SupportsDD) supports.next();
                contentTypes.add(sup.getMimeType());
            }
            if (contentTypes.size() < 1) {
                contentTypes.add("text/html");
            }
        }
        return contentTypes.elements();
    }

    public Locale getLocale() {
        return this.getHttpServletRequest().getLocale();
    }

    public Enumeration getLocales() {
        return this.getHttpServletRequest().getLocales();
    }

    public String getScheme() {
        return this.getHttpServletRequest().getScheme();
    }

    public String getServerName() {
        return this.getHttpServletRequest().getServerName();
    }

    public int getServerPort() {
        return this.getHttpServletRequest().getServerPort();
    }

    // Protected Methods -------------------------------------------------------

    /**
     * The base method that returns the parameter map in this portlet request.
     * All parameter-related methods call this base method. Subclasses may just
     * overwrite this protected method to change behavior of all parameter-
     * related methods.
     *
     * @return the base parameter map from which parameters are retrieved.
     */
    protected Map baseGetParameterMap() {
        bodyAccessed = true;
        return this.getHttpServletRequest().getParameterMap();
    }

    protected void setBodyAccessed() {
        bodyAccessed = true;
    }

    // InternalPortletRequest Impl ---------------------------------------------

    public InternalPortletWindow getInternalPortletWindow() {
        return internalPortletWindow;
    }

    public PortletContainer getPortletContainer() {
        return container;
    }

    public HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) super.getRequest();
    }

    public void init(PortletContext portletContext, HttpServletRequest req) {
        this.portletContext = portletContext;
        setRequest(req);
    }

    /**
     * TODO: Implement this properly.  Not required now
     */
    public void release() {
        // TODO:
    }

    // TODO: Additional Methods of HttpServletRequestWrapper -------------------

    public BufferedReader getReader()
        throws UnsupportedEncodingException, IOException {
        // the super class will ensure that a IllegalStateException is thrown
        //   if getInputStream() was called earlier
        BufferedReader reader = getHttpServletRequest().getReader();
        bodyAccessed = true;
        return reader;
    }

    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream stream = getHttpServletRequest().getInputStream();
        bodyAccessed = true;
        return stream;
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return getHttpServletRequest().getRequestDispatcher(path);
    }

    /**
     * TODO: why check bodyAccessed?
     */
    public void setCharacterEncoding(String encoding)
        throws UnsupportedEncodingException {
        if (bodyAccessed) {
            throw new IllegalStateException("Cannot set character encoding "
                + "after HTTP body is accessed.");
        }
        super.setCharacterEncoding(encoding);
    }

    // Private Methods ---------------------------------------------------------


    private boolean isPortletModeAllowedByPortlet(PortletMode mode) {
        if (isPortletModeMandatory(mode)) {
            return true;
        }

        PortletDD dd = internalPortletWindow.getPortletEntity()
            .getPortletDefinition();

        Iterator mimes = dd.getSupports().iterator();
        while (mimes.hasNext()) {
            Iterator modes = ((SupportsDD) mimes.next()).getPortletModes().iterator();
            while (modes.hasNext()) {
                String m = (String) modes.next();
                if (m.equalsIgnoreCase(mode.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPortletModeAllowedByPortal(PortletMode mode) {
        Enumeration supportedModes = portalContext.getSupportedPortletModes();
        while (supportedModes.hasMoreElements()) {
            if (supportedModes.nextElement().toString().equals(
                (mode.toString()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isPortletModeMandatory(PortletMode mode) {
        return PortletMode.VIEW.equals(mode) || PortletMode.EDIT.equals(mode) || PortletMode.HELP.equals(mode);
    }
}
