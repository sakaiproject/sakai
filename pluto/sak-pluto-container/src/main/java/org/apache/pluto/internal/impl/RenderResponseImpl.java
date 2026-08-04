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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
import org.apache.pluto.internal.Configuration;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.internal.InternalRenderResponse;
import org.apache.pluto.spi.PortalCallbackService;
import org.apache.pluto.util.ArgumentUtility;
import org.apache.pluto.util.NamespaceMapper;
import org.apache.pluto.util.StringManager;
import org.apache.pluto.util.StringUtils;
import org.apache.pluto.util.impl.NamespaceMapperImpl;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Locale;

/**
 * Implementation of the <code>javax.portlet.RenderResponse</code> interface.
 *
 */
public class RenderResponseImpl extends PortletResponseImpl
    implements RenderResponse, InternalRenderResponse {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(RenderResponseImpl.class);

    private static final StringManager EXCEPTIONS = StringManager.getManager(
        RenderResponseImpl.class.getPackage().getName());

    // Private Member Variables ------------------------------------------------

    /**
     * True if we are in an include call.
     */
    private boolean included = false;

    /**
     * The current content type.
     */
    private String currentContentType;

    private final NamespaceMapper mapper = new NamespaceMapperImpl();

    // Constructor -------------------------------------------------------------

    public RenderResponseImpl(PortletContainer container,
                              InternalPortletWindow internalPortletWindow,
                              HttpServletRequest servletRequest,
                              HttpServletResponse servletResponse) {
        super(container, internalPortletWindow, servletRequest, servletResponse);
    }

    // RenderResponse Impl -----------------------------------------------------

    public String getContentType() {
        // NOTE: in servlet 2.4 we could simply use this:
        //   return super.getHttpServletResponse().getContentType();
        return currentContentType;
    }

    public PortletURL createRenderURL() {
        return createURL(false);
    }

    public PortletURL createActionURL() {
        return createURL(true);
    }

    public String getNamespace() {
        String namespace = mapper.encode(getInternalPortletWindow().getId(), "");
        StringBuffer validNamespace = new StringBuffer();
        for (int i = 0; i < namespace.length(); i++) {
            char ch = namespace.charAt(i);
            if (Character.isJavaIdentifierPart(ch)) {
                validNamespace.append(ch);
            } else {
                validNamespace.append('_');
            }
        }
        return validNamespace.toString();
    }

    public void setTitle(String title) {
        PortalCallbackService callback = getContainer()
            .getRequiredContainerServices()
            .getPortalCallbackService();
        callback.setTitle(this.getHttpServletRequest(),
            getInternalPortletWindow(),
            title);
    }

    public void setContentType(String contentType)
        throws IllegalArgumentException {
        ArgumentUtility.validateNotNull("contentType", contentType);
        String mimeType = StringUtils.getMimeTypeWithoutEncoding(contentType);
        if (!isValidContentType(mimeType)) {
            throw new IllegalArgumentException("Specified content type '"
                + mimeType + "' is not supported.");
        }
        getHttpServletResponse().setContentType(mimeType);
        this.currentContentType = mimeType;
    }

    public String getCharacterEncoding() {
        return getHttpServletResponse().getCharacterEncoding();
    }

    /**
     * @see PortletResponseImpl#getOutputStream()
     * @see #getWriter()
     */
    public OutputStream getPortletOutputStream()
        throws IOException, IllegalStateException {
        if (currentContentType == null) {
            String message = EXCEPTIONS.getString("error.contenttype.null", "getPortletOutputStream");
            if (LOG.isWarnEnabled()) {
                LOG.warn("Current content type is not set.");
            }
            throw new IllegalStateException(message);
        }
        return super.getOutputStream();
    }

    /**
     * @see PortletResponseImpl#getWriter()
     * @see #getPortletOutputStream()
     */
    public PrintWriter getWriter() throws IOException, IllegalStateException {
        if (currentContentType == null) {
            String message = EXCEPTIONS.getString("error.contenttype.null", "getWriter");
            if (LOG.isWarnEnabled()) {
                LOG.warn("Current content type is not set.");
            }
            throw new IllegalStateException(message);
        }
        return super.getWriter();
    }

    public Locale getLocale() {
        return getHttpServletRequest().getLocale();
    }

    public void setBufferSize(int size) {
        if (Configuration.isBufferingSupported()) {
            getHttpServletResponse().setBufferSize(size);
        } else {
            throw new IllegalStateException(
                "portlet container does not support buffering");
        }
    }

    public int getBufferSize() {
        if (Configuration.isBufferingSupported()) {
            return getHttpServletResponse().getBufferSize();
        }
        return 0;
    }

    public void flushBuffer() throws IOException {
        getHttpServletResponse().flushBuffer();
    }

    public void resetBuffer() {
        getHttpServletResponse().resetBuffer();
    }

    public boolean isCommitted() {
        return getHttpServletResponse().isCommitted();
    }

    public void reset() {
        getHttpServletResponse().reset();
    }

    // InternalRenderResponse Impl ---------------------------------------------

    public void setIncluded(boolean included) {
        this.included = included;
    }

    public boolean isIncluded() {
        return included;
    }

    // Included HttpServletResponse (Limited) Impl -----------------------------

    /**
     * TODO
     */
    public String encodeRedirectUrl(String url) {
        if (included) {
            return null;
        } else {
            return super.encodeRedirectUrl(url);
        }
    }

    /**
     * TODO
     */
    public String encodeRedirectURL(String url) {
        if (included) {
            return null;
        } else {
            return super.encodeRedirectURL(url);
        }
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Creates a portlet URL.
     * TODO: make dynamic? as service?
     *
     * @param isAction true for an action URL, false for a render URL.
     * @return the created portlet (action/render) URL.
     */
    private PortletURL createURL(boolean isAction) {
        return new PortletURLImpl(getContainer(),
            getInternalPortletWindow(),
            getHttpServletRequest(),
            getHttpServletResponse(),
            isAction);
    }

    /**
     * Checks if the specified content type is valid (supported by the portlet).
     * The specified content type should be a tripped mime type without any
     * character encoding suffix.
     *
     * @param contentType the content type to check.
     * @return true if the content type is valid, false otherwise.
     */
    private boolean isValidContentType(String contentType) {
        boolean valid = false;

        PortletDD portletDD = getInternalPortletWindow().getPortletEntity()
            .getPortletDefinition();
        for (Iterator it = portletDD.getSupports().iterator();
             !valid && it.hasNext();) {

            SupportsDD supportsDD = (SupportsDD) it.next();
            String supportedType = supportsDD.getMimeType();

            // Content type is supported by an exact match.
            if (supportedType.equals(contentType)) {
                valid = true;
            }
            // The supported type contains a wildcard.
            else if (supportedType.indexOf("*") >= 0) {

                int index = supportedType.indexOf("/");
                String supportedPrefix = supportedType.substring(0, index);
                String supportedSuffix = supportedType.substring(index + 1);

                index = contentType.indexOf("/");
                String typePrefix = contentType.substring(0, index);
                String typeSuffix = contentType.substring(index + 1);

                // Check if the prefixes match AND the suffixes match.
                if (supportedPrefix.equals("*") || supportedPrefix.equals(typePrefix)) {
                    if (supportedSuffix.equals("*") || supportedSuffix.equals(typeSuffix)) {
                        valid = true;
                    }
                }
            }
        }
        // Return the check result.
        return valid;
    }

}
