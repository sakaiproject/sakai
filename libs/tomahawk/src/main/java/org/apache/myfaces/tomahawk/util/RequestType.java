/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.tomahawk.util;

/**
 * TODO: This class is a copy of 
 * org.apache.myfaces.commons.util.RequestType
 * 
 * Since tomahawk should be compatible with 1.1, this is placed
 * here and there is not a dependency for myfaces-commons-utils, because
 * this stuff only works for 1.2 (this class is also compatible
 * with jdk 1.4)
 * 
 * Represents the type of request currently in the ExternalContext.
 * All servlet requests will be of the SERVLET requestType whereas
 * all of the other RequestTypes will be portlet type requests.  There
 * are a number of convenience methods on the RequestType enumeration
 * which can be used to determine the capabilities of the current request.
 * 
 * @since 1.1.7
 */
public class RequestType
{
    /**
     * The type for all servlet requests.  SERVLET request types are
     * both client requests and response writable.
     */
    final public static RequestType SERVLET = new RequestType(true, true, false);
    
    /**
     * The type for a portlet RenderRequest.  RENDER request types are
     * for portlets and are response writable but are NOT client
     * requests.
     */
    final public static RequestType RENDER = new RequestType(false, true, true);
    
    /**
     * The type for a portlet ActionRequest.  ACTION request types are
     * for portlets and are client requests but are NOT response 
     * writable.
     */
    final public static RequestType ACTION = new RequestType(true, false, true);
    
    /**
     * The type for a portlet ResourceRequest.  RESOURCE request types
     * are for portlets and are both client requests and response 
     * writable.  RESOURCE request types will only be returned in a
     * Portlet 2.0 portlet container.
     */
    final public static RequestType RESOURCE = new RequestType(true, true, true);
    
    /**
     * The type for a portlet EventRequest.  EVENT request types
     * are for portlets and are neither client requests nor response 
     * writable.  EVENT request types will only be returned in a
     * Portlet 2.0 portlet container.
     */        
    final public static RequestType EVENT  = new RequestType(false, false, true);
    
    private boolean _client;
    private boolean _writable;
    private boolean _portlet;
    
    RequestType(boolean client, boolean writable, boolean portlet)
    {
        _client = client;
        _writable  = writable;
        _portlet    = portlet;
    }
        
    /**
     * Returns <code>true</code> if this request was a direct
     * result of a call from the client.  This implies that
     * the current application is the "owner" of the current
     * request and that it has access to the inputStream, can
     * get and set character encodings, etc.  Currently all
     * SERVLET, ACTION, and RESOURCE RequestTypes are client
     * requests.
     * 
     * @return <code>true</code> if the current request is a
     *         client data type request and <code>false</code>
     *         if it is not.
     */
    public boolean isRequestFromClient()
    {
        return _client;
    }
    
    /**
     * Returns <code>true</code> if the response for this
     * RequestType is intended to produce output to the client.
     * Currently the SERVLET, RENDER, and RESOURCE request are
     * response writable.
     *  
     * @return <code>true</code> if the current request is 
     *         intended to produce output and <code>false</code>
     *         if it is not.
     */
    public boolean isResponseWritable()
    {
        return _writable;
    }
    
    /**
     * Returns <code>true</code> if the response for this
     * RequestType originated from a JSR-168 or JSR-286 
     * portlet container.  Currently RENDER, ACTION,
     * RESOURCE, and EVENT RequestTypes are all portlet
     * requests.
     * 
     * @return <code>true</code> if the current request
     *         originated inside of a JSR-168 or JSR-286
     *         Portlet Container or <code>false</code> if
     *         it did not.
     */
    public boolean isPortlet()
    {
        return _portlet;
    }
}
