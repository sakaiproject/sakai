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

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

/**
 * Allows us to cleanly wrap an httpclient object without exposing the actual object class
 *
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class HttpClientWrapper {

    private HttpClient httpClient;
    private final int requestTimeout;
    private final CookieManager cookieManager;
    private final List<HttpCookie> initialCookies;
    private boolean initialCookiesLoaded = false;

    /**
     * This is meant for system use so you should not be constructing this,
     * use the {@link HttpRESTUtils#makeReusableHttpClient(boolean, int, Cookie[])} instead
     */
    public HttpClientWrapper(HttpClient httpClient, int requestTimeout, CookieManager cookieManager, Cookie[] cookies) {
        super();
        this.httpClient = httpClient;
        this.requestTimeout = requestTimeout;
        this.cookieManager = cookieManager;
        this.initialCookies = makeHttpCookies(cookies);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    int getRequestTimeout() {
        return requestTimeout;
    }

    void seedInitialCookies(URI requestUri) {
        if (!initialCookiesLoaded && requestUri != null) {
            for (HttpCookie cookie : initialCookies) {
                cookieManager.getCookieStore().add(requestUri, cookie);
            }
            initialCookiesLoaded = true;
        }
    }

    /**
     * Resets the http client state between requests,
     * this is not necessarily required but might be a good idea
     */
    public void resetState() {
        cookieManager.getCookieStore().removeAll();
        initialCookiesLoaded = false;
    }

    /**
     * cleanup and shutdown the http client
     */
    public void shutdown() {
        this.httpClient = null;
        this.cookieManager.getCookieStore().removeAll();
        this.initialCookiesLoaded = false;
    }

    static CookieManager makeCookieManager() {
        return new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    }

    private static List<HttpCookie> makeHttpCookies(Cookie[] source) {
        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        if (source == null) {
            return cookies;
        }
        for (Cookie cookie : source) {
            if (cookie != null) {
                HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
                if (cookie.getDomain() != null) {
                    httpCookie.setDomain(cookie.getDomain());
                }
                if (cookie.getPath() != null) {
                    httpCookie.setPath(cookie.getPath());
                } else {
                    httpCookie.setPath("/");
                }
                httpCookie.setHttpOnly(cookie.isHttpOnly());
                httpCookie.setMaxAge(cookie.getMaxAge());
                httpCookie.setSecure(cookie.getSecure());
                httpCookie.setVersion(cookie.getVersion());
                cookies.add(httpCookie);
            }
        }
        return cookies;
    }
}
