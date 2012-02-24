/**
 * $Id$
 * $URL$
 * HttpClientWrapper.java - entity-broker - Jan 7, 2009 4:29:17 PM - azeckoski
 ***********************************************************************************
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
 **********************************************************************************/

package org.sakaiproject.entitybroker.util.http;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * Allows us to cleanly wrap an httpclient object without exposing the actual object class
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class HttpClientWrapper {

    private HttpClient httpClient;
    public HttpClient getHttpClient() {
        return httpClient;
    }
    private MultiThreadedHttpConnectionManager connectionManager;
    private HttpState initialHttpState;
    /**
     * This is meant for system use so you should not be constructing this,
     * use the {@link HttpRESTUtils#makeReusableHttpClient(boolean, int, javax.servlet.http.Cookie[])} instead
     */
    public HttpClientWrapper(HttpClient httpClient, 
            MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager,
            HttpState initialHttpState) {
        super();
        this.httpClient = httpClient;
        this.connectionManager = multiThreadedHttpConnectionManager;
        this.initialHttpState = initialHttpState;
    }
    /**
     * Resets the http client state between requests,
     * this is not necessarily required but might be a good idea
     */
    public void resetState() {
        if (initialHttpState != null) {
            httpClient.setState(initialHttpState);
        } else {
            httpClient.setState( new HttpState() );
        }
    }
    /**
     * cleanup and shutdown the http client
     */
    public void shutdown() {
        if (this.connectionManager != null) {
            this.connectionManager.shutdown();
        }
        this.httpClient = null;
    }

}
