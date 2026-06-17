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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Allows us to cleanly wrap an httpclient object without exposing the actual object class
 *
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class HttpClientWrapper {

    private final RestTemplate restTemplate;
    private final CloseableHttpClient httpClient;
    private final CookieStore cookieStore;
    private final List<BasicClientCookie> initialCookies;
    private boolean initialCookiesSeeded = false;

    /**
     * This is meant for system use so you should not be constructing this,
     * use the {@link HttpRESTUtils#makeReusableHttpClient(boolean, int, Cookie[])} instead
     */
    public HttpClientWrapper(RestTemplate restTemplate, CloseableHttpClient httpClient, CookieStore cookieStore,
            Cookie[] cookies) {
        super();
        this.restTemplate = restTemplate;
        this.httpClient = httpClient;
        this.cookieStore = cookieStore;
        this.initialCookies = makeClientCookies(cookies);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * Seeds the initial cookies into the cookie store once, before the first request is fired.
     * Cookies forwarded from a servlet request usually carry no domain, so we adopt the host of
     * the first request (matching the previous behaviour) so the cookie store will actually send them.
     */
    void seedInitialCookies(String host) {
        if (!initialCookiesSeeded) {
            for (BasicClientCookie cookie : initialCookies) {
                if (cookie.getDomain() == null && host != null) {
                    cookie.setDomain(host);
                }
                cookieStore.addCookie(cookie);
            }
            initialCookiesSeeded = true;
        }
    }

    /**
     * Resets the http client state between requests,
     * this is not necessarily required but might be a good idea
     */
    public void resetState() {
        cookieStore.clear();
        initialCookiesSeeded = false;
    }

    /**
     * cleanup and shutdown the http client
     */
    public void shutdown() {
        cookieStore.clear();
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.warn("Failure while closing the reusable http client :: {}", e.getMessage());
            }
        }
    }

    static CookieStore makeCookieStore() {
        return new BasicCookieStore();
    }

    private static List<BasicClientCookie> makeClientCookies(Cookie[] source) {
        List<BasicClientCookie> cookies = new ArrayList<BasicClientCookie>();
        if (source == null) {
            return cookies;
        }
        for (Cookie cookie : source) {
            if (cookie != null) {
                BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
                if (cookie.getDomain() != null) {
                    clientCookie.setDomain(cookie.getDomain());
                }
                if (cookie.getPath() != null) {
                    clientCookie.setPath(cookie.getPath());
                } else {
                    clientCookie.setPath("/");
                }
                clientCookie.setSecure(cookie.getSecure());
                cookies.add(clientCookie);
            }
        }
        return cookies;
    }
}
