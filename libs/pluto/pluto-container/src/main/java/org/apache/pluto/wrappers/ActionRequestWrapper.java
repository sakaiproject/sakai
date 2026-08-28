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

import javax.portlet.ActionRequest;

public class ActionRequestWrapper extends PortletRequestWrapper
    implements ActionRequest {

    /**
     * Creates a ServletRequest adaptor wrapping the given request object.
     * @throws java.lang.IllegalArgumentException
     *          if the request is null.
     */
    public ActionRequestWrapper(ActionRequest actionRequest) {
        super(actionRequest);

        if (actionRequest == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
    }

    // javax.portlet.ActionRequest implementation -------------------------------------------------    
    public java.io.InputStream getPortletInputStream()
        throws java.io.IOException {
        return this.getActionRequest().getPortletInputStream();
    }

    public void setCharacterEncoding(String enc)
        throws java.io.UnsupportedEncodingException {
        this.getActionRequest().setCharacterEncoding(enc);
    }

    public java.io.BufferedReader getReader()
        throws java.io.UnsupportedEncodingException, java.io.IOException {
        return this.getActionRequest().getReader();
    }

    public java.lang.String getCharacterEncoding() {
        return this.getActionRequest().getCharacterEncoding();
    }

    public java.lang.String getContentType() {
        return this.getActionRequest().getContentType();
    }

    public int getContentLength() {
        return this.getActionRequest().getContentLength();
    }
    
    // --------------------------------------------------------------------------------------------
    
    // additional methods -------------------------------------------------------------------------
    /**
     * Return the wrapped ServletRequest object.
     */
    public ActionRequest getActionRequest() {
        return (ActionRequest) getPortletRequest();
    }

    // --------------------------------------------------------------------------------------------
}

