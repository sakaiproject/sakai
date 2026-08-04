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

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.PortletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.internal.InternalPortletResponse;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.spi.ResourceURLProvider;
import org.apache.pluto.util.ArgumentUtility;
import org.apache.pluto.util.PrintWriterServletOutputStream;

/**
 * Abstract <code>javax.portlet.PortletResponse</code> implementation.
 * This class also implements InternalPortletResponse.
 *
 */
public abstract class PortletResponseImpl extends HttpServletResponseWrapper
implements PortletResponse, InternalPortletResponse {

	// Private Member Variables ------------------------------------------------

	/** The portlet container. */
    private final PortletContainer container;

    /** The internal portlet window. */
    private final InternalPortletWindow internalPortletWindow;

    /** The servlet request of the target/portlet's web module. */
    private final HttpServletRequest httpServletRequest;

    private boolean usingWriter;
    private boolean usingStream;

    private ServletOutputStream wrappedWriter;


    // Constructor -------------------------------------------------------------

    public PortletResponseImpl(PortletContainer container,
                               InternalPortletWindow internalPortletWindow,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) {
        super(servletResponse);
        this.container = container;
        this.httpServletRequest = servletRequest;
        this.internalPortletWindow = internalPortletWindow;
    }


    // PortletResponse Impl ----------------------------------------------------

    public void addProperty(String name, String value) {
    	ArgumentUtility.validateNotNull("propertyName", name);
        container.getRequiredContainerServices()
        		.getPortalCallbackService()
        		.addResponseProperty(
        				getHttpServletRequest(),
        				internalPortletWindow,
        				name, value);
    }

    public void setProperty(String name, String value) {
    	ArgumentUtility.validateNotNull("propertyName", name);
        container.getRequiredContainerServices()
                .getPortalCallbackService()
                .setResponseProperty(
                        getHttpServletRequest(),
                        internalPortletWindow,
                        name, value);
    }

    public String encodeURL(String path) {
        
        if ( path == null ) {
            throw new IllegalArgumentException( "Argument to encodeURL must not be null." );
        }
        
        final String wsrpRewriteToken = "wsrp_rewrite?";        
        
        if ( (path.indexOf("://") == -1 && !path.startsWith("/")) && 
                !path.startsWith( wsrpRewriteToken ) ) {
            throw new IllegalArgumentException(
                "only absolute URLs or full path URIs are allowed");
        }

        ResourceURLProvider provider = getContainer()
        		.getRequiredContainerServices()
        		.getPortalCallbackService()
        		.getResourceURLProvider(
        				httpServletRequest,
        				internalPortletWindow);

        if (isAbsolute(path)) {            
            provider.setAbsoluteURL(path);
        } else {            
            provider.setFullPath(path);
        }

        return getHttpServletResponse().encodeURL(provider.toString());
    }


    // InternalPortletResponse impl --------------------------------------------

    public InternalPortletWindow getInternalPortletWindow() {
        return internalPortletWindow;
    }


    // Internal Methods --------------------------------------------------------

    /**
     * Returns the portlet container.
     * @return the portlet container.
     */
    protected PortletContainer getContainer() {
        return container;
    }

    /**
     * Returns the nested HttpServletRequest instance.
     * @return the nested HttpServletRequest instance.
     */
    protected HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }
    
    /**
     * Determines if the supplied path should be treated as an
     * absolute URL.  This default implementation considers the
     * following conditions when evaluating the path:
     * <ol>
     *   <li>If the path is null, return false</li>
     *   <li>If the path contains the string "://", then return true</li>
     *   <li>If the path starts with the string "wsrp-rewrite?" then
     *      return true</li>
     *   <li>If none of the previous conditions hold true, return false</li>
     * </ol>
     * <p/>
     * If the path is considered absolute, then ResourceURL providers
     * (e.g. ResourceURLProvider implementations) should perform little, 
     * if any, manipulation of the path.
     * <p/>
     * If the path is not considered absolute, then the ResourceURL provider
     * may modify it to be absolute according to the 
     * <code>ResourceURLProvider.toString()</code> contract.  For example,
     * they may pre-pend a scheme and host to the supplied path.
     * 
     * @param path a string representing a resource path
     * @return true if the resource path should be considered absolute
     */
    protected boolean isAbsolute(String path) {
        final String wsrpToken = "wsrp_rewrite?";
        final String schemeToken = "://";
        
        if ( path == null ) {
            return false;
        }
        
        if ( path.indexOf( schemeToken ) != -1 ) {
            return true;
        }
        
        if ( path.startsWith( wsrpToken ) ) {
            return true;
        }
        
        return false;
    }

    /**
     * Returns the nested HttpServletResponse instance.
     * @return the nested HttpServletResponse instance.
     */
    public HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) super.getResponse();
    }


    // HttpServletResponse Methods ---------------------------------------------

    public String encodeUrl(String url) {
        return this.encodeURL(url);
    }

    /**
     * TODO: javadoc about why we are using a wrapped writer here.
     * @see org.apache.pluto.util.PrintWriterServletOutputStream
     */
    public ServletOutputStream getOutputStream()
    throws IllegalStateException, IOException {
        if (usingWriter) {
            throw new IllegalStateException(
            		"getPortletOutputStream can't be used "
            		+ "after getWriter was invoked.");
        }
        if (wrappedWriter == null) {
            wrappedWriter = new PrintWriterServletOutputStream(
            		getHttpServletResponse().getWriter());
        }
        usingStream = true;
        return wrappedWriter;
    }

    public PrintWriter getWriter()
    throws IllegalStateException, IOException {
        if (usingStream) {
            throw new IllegalStateException(
            		"getWriter can't be used "
            		+ "after getOutputStream was invoked.");
        }
        usingWriter = true;
        return getHttpServletResponse().getWriter();
    }

}
