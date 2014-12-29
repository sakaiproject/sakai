/**
 * $Id$
 * $URL$
 * RequestGetterImpl.java - entity-broker - Apr 8, 2008 9:03:50 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.util.request;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite;


/**
 * Service which will retrieve the current request information if it is available,
 * this allows an application scoped bean to get access to request scoped information
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class RequestGetterImpl implements RequestGetter, RequestGetterWrite {

    /**
     * Stores the request related to the current thread
     */
    private ThreadLocal<HttpServletRequest> requestTL = new ThreadLocal<HttpServletRequest>();
    /**
     * Stores the response related to the current thread
     */
    private ThreadLocal<HttpServletResponse> responseTL = new ThreadLocal<HttpServletResponse>();

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter#getRequest()
     */
    public HttpServletRequest getRequest() {
        HttpServletRequest req = requestTL.get();
        // TODO try to get this from Sakai?
        return req;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter#getResponse()
     */
    public HttpServletResponse getResponse() {
        HttpServletResponse res = responseTL.get();
        // TODO try to get this from Sakai?
        return res;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite#setRequest(javax.servlet.http.HttpServletRequest)
     */
    public void setRequest(HttpServletRequest req) {
        requestTL.set(req);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.extension.RequestGetterWrite#setResponse(javax.servlet.http.HttpServletResponse)
     */
    public void setResponse(HttpServletResponse res) {
        responseTL.set(res);
    }

    /**
     * Cleanup on shutdown
     */
    public void destroy() {
        requestTL.remove();
        responseTL.remove();
    }

}
