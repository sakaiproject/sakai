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
package org.apache.pluto.wrappers;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.OutputStream;

public class RenderResponseWrapper extends PortletResponseWrapper
    implements RenderResponse {
    /**
     * Creates a ServletResponse adaptor wrapping the given response object.
     * @throws java.lang.IllegalArgumentException
     *          if the response is null.
     */
    public RenderResponseWrapper(RenderResponse renderResponse) {
        super(renderResponse);

        if (renderResponse == null) {
            throw new IllegalArgumentException("Response cannot be null");
        }
    }

    // javax.portlet.RenderResponse implementation ------------------------------------------------
    public String getContentType() {
        return this.getRenderResponse().getContentType();
    }

    public PortletURL createRenderURL() {
        return this.getRenderResponse().createRenderURL();
    }

    public PortletURL createActionURL() {
        return this.getRenderResponse().createActionURL();
    }

    public String getNamespace() {
        return this.getRenderResponse().getNamespace();
    }

    public void setTitle(String title) {
        this.getRenderResponse().setTitle(title);
    }

    public void setContentType(String type) {
        this.getRenderResponse().setContentType(type);
    }

    public String getCharacterEncoding() {
        return this.getRenderResponse().getCharacterEncoding();
    }

    public java.io.PrintWriter getWriter() throws java.io.IOException {
        return this.getRenderResponse().getWriter();
    }

    public java.util.Locale getLocale() {
        return this.getRenderResponse().getLocale();
    }

    public void setBufferSize(int size) {
        this.getRenderResponse().setBufferSize(size);
    }

    public int getBufferSize() {
        return this.getRenderResponse().getBufferSize();
    }

    public void flushBuffer() throws java.io.IOException {
        this.getRenderResponse().flushBuffer();
    }

    public void resetBuffer() {
        this.getRenderResponse().resetBuffer();
    }

    public boolean isCommitted() {
        return this.getRenderResponse().isCommitted();
    }

    public void reset() {
        this.getRenderResponse().reset();
    }

    public OutputStream getPortletOutputStream() throws IOException {
        return this.getRenderResponse().getPortletOutputStream();
    }
    // --------------------------------------------------------------------------------------------

    // additional methods -------------------------------------------------------------------------
    /**
     * Return the wrapped ServletResponse object.
     */
    public RenderResponse getRenderResponse() {
        return (RenderResponse) getPortletResponse();
    }
    // --------------------------------------------------------------------------------------------

}

