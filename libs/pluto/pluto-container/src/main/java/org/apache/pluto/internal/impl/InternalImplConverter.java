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

import org.apache.pluto.internal.InternalPortletRequest;
import org.apache.pluto.internal.InternalPortletResponse;
import org.apache.pluto.wrappers.PortletRequestWrapper;
import org.apache.pluto.wrappers.PortletResponseWrapper;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * Static class that provides utility methods to convert a generic
 * PortletRequest or PortletResponse object into an Internal respresentation
 * of the same object.
 */
class InternalImplConverter {

	/**
	 * Private constructor that prevents external instantiation.
	 */
	private InternalImplConverter() {
		// Do nothing.
	}


	// Public Static Utility Methods -------------------------------------------

    /**
     * The scary part about this is that there is not yet a
     * PortletRequestWrapper defined by the spec.  Because of this, there's a
     * chance someone might implement their own wrapper and we won't be able to
     * get the real internal one!
     * @param request the portlet request to be converted.
     * @return the internal request.
     */
    public static InternalPortletRequest getInternalRequest(
    		PortletRequest request) {
        while (!(request instanceof InternalPortletRequest)) {
            request = ((PortletRequestWrapper) request).getPortletRequest();
            if (request == null) {
                throw new IllegalStateException(
                		"The internal portlet request cannot be found.");
            }
        }
        return (InternalPortletRequest) request;
    }

    /**
     * The scary part about this is that there is not yet a
     * PortletRequestWrapper defined by the spec.  Because of this, there's a
     * chance someone might implement their own wrapper and we won't be able to
     * get the real internal one!
     * @param response the portlet response to be converted.
     * @return the internal response.
     */
    public static InternalPortletResponse getInternalResponse(
    		PortletResponse response) {
        while (!(response instanceof InternalPortletResponse)) {
            response = ((PortletResponseWrapper) response).getPortletResponse();
            if (response == null) {
                throw new IllegalStateException(
                		"The internal portlet response cannot be found.");
            }
        }
        return (InternalPortletResponse) response;
    }

}
