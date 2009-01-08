/**
 * $Id$
 * $URL$
 * HttpClientWrapper.java - entity-broker - Jan 7, 2009 4:29:17 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

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
