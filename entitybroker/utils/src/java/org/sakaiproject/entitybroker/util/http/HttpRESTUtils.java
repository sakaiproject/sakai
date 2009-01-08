/**
 * $Id$
 * $URL$
 * HttpUtils.java - entity-broker - Jul 20, 2008 11:42:19 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.util.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;


/**
 * Utilities for generating and processing http requests,
 * allows the developer to fire off a request and get back information about the response<br/>
 * All encoding is automatically UTF-8
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class HttpRESTUtils {

    public static final String CONTENT_TYPE_UTF8 = "text/xml; charset=UTF-8";
    public static final String ENCODING_UTF8 = "UTF-8";
    public static enum Method {POST, GET, PUT, DELETE, HEAD};

    /**
     * Fire off a request to a URL using the specified method,
     * include optional params and data in the request,
     * the response data will be returned in the object if the request can be carried out
     * 
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @return an object representing the response, includes data about the response
     * @throws RuntimeException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(String URL, Method method) {
        return fireRequest(URL, method, null, null, false);
    }

    /**
     * Fire off a request to a URL using the specified method,
     * include optional params and data in the request,
     * the response data will be returned in the object if the request can be carried out
     * 
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @param params (optional) params to send along with the request, will be encoded in the query string or in the body depending on the method
     * @return an object representing the response, includes data about the response
     * @throws RuntimeException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(String URL, Method method, Map<String, String> params) {
        return fireRequest(null, URL, method, params, null, false);
    }

    /**
     * Fire off a request to a URL using the specified method,
     * include optional params and data in the request,
     * the response data will be returned in the object if the request can be carried out
     * 
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @param params (optional) params to send along with the request, will be encoded in the query string or in the body depending on the method
     * @param data (optional) data to send along in the body of the request, this only works for POST and PUT requests, ignored for the other types
     * @param guaranteeSSL if this is true then the request is sent in a mode which will allow self signed certs to work,
     * otherwise https requests will fail if the certs cannot be centrally verified
     * @return an object representing the response, includes data about the response
     * @throws RuntimeException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(String URL, Method method, Map<String, String> params, Object data, boolean guaranteeSSL) {
        return fireRequest(null, URL, method, params, data, guaranteeSSL);
    }

    /**
     * Fire off a request to a URL using the specified method but reuse the client for efficiency,
     * include optional params and data in the request,
     * the response data will be returned in the object if the request can be carried out
     * 
     * @param httpClientWrapper (optional) allows the http client to be reused for efficiency,
     * if null a new one will be created each time, use {@link #makeReusableHttpClient(boolean, int)} to
     * create a reusable instance
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @param params (optional) params to send along with the request, will be encoded in the query string or in the body depending on the method
     * @param data (optional) data to send along in the body of the request, this only works for POST and PUT requests, ignored for the other types
     * @param guaranteeSSL if this is true then the request is sent in a mode which will allow self signed certs to work,
     * otherwise https requests will fail if the certs cannot be centrally verified
     * @return an object representing the response, includes data about the response
     * @throws RuntimeException if the request cannot be processed for some reason (this is unrecoverable)
     */
    @SuppressWarnings("deprecation")
    public static HttpResponse fireRequest(HttpClientWrapper httpClientWrapper, String URL, Method method, Map<String, String> params, Object data, boolean guaranteeSSL) {
        if (guaranteeSSL) {
            // added this to attempt to force the SSL self signed certs to work
            Protocol myhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
            Protocol.registerProtocol("https", myhttps);
        }

        if (httpClientWrapper == null || httpClientWrapper.getHttpClient() == null) {
            httpClientWrapper = makeReusableHttpClient(false, 0, null);
        }

        HttpMethod httpMethod = null;
        if (method.equals(Method.GET)) {
            GetMethod gm = new GetMethod(URL);
            // put params into query string
            gm.setQueryString( mergeQueryStringWithParams(gm.getQueryString(), params) );
            // warn about data being set
            if (data != null) {
                System.out.println("WARN: data cannot be passed in GET requests, data will be ignored (org.sakaiproject.entitybroker.util.http.HttpUtils#fireRequest)");
            }
            httpMethod = gm;
        } else if (method.equals(Method.POST)) {
            PostMethod pm = new PostMethod(URL);
            // special handling for post params
            if (params != null) {
                for (Entry<String, String> entry : params.entrySet()) {
                    if (entry.getKey() == null
                            || entry.getValue() == null) {
                        System.out.println("WARN: null value supplied for param name ("+entry.getKey()+") or value ("
                                +entry.getValue()+") (org.sakaiproject.entitybroker.util.http.HttpUtils#fireRequest)");
                    }
                    pm.addParameter(entry.getKey(), entry.getValue());
                }
            }
            // handle data
            handleRequestData(pm, data);
            httpMethod = pm;
        } else if (method.equals(Method.PUT)) {
            PutMethod pm = new PutMethod(URL);
            // put params into query string
            pm.setQueryString( mergeQueryStringWithParams(pm.getQueryString(), params) );
            // handle data
            handleRequestData(pm, data);
            httpMethod = pm;
        } else if (method.equals(Method.DELETE)) {
            DeleteMethod dm = new DeleteMethod(URL);
            // put params into query string
            dm.setQueryString( mergeQueryStringWithParams(dm.getQueryString(), params) );
            // warn about data being set
            if (data != null) {
                System.out.println("WARN: data cannot be passed in DELETE requests, data will be ignored (org.sakaiproject.entitybroker.util.http.HttpUtils#fireRequest)");
            }
            httpMethod = dm;
        } else {
            throw new IllegalArgumentException("Cannot handle method: " + method);
        }

        // set the standard stuff we use for all requests
        httpMethod.setFollowRedirects(true);

        HttpResponse response = null;
        try {
            int responseCode = httpClientWrapper.getHttpClient().executeMethod(httpMethod);
            response = new HttpResponse(responseCode);
            String body = httpMethod.getResponseBodyAsString();
            //         byte[] responseBody = httpMethod.getResponseBody();
            //         if (responseBody != null) {
            //            body = new String(responseBody, "UTF-8");
            //         }
            response.setResponseBody(body);
            response.setResponseMessage(httpMethod.getStatusText());
            // now get the headers
            HashMap<String, String[]> headerMap = new HashMap<String, String[]>();
            Header[] headers = httpMethod.getResponseHeaders();
            for (int i = 0; i < headers.length; i++) {
                Header header = headers[i];
                String[] values = new String[] {header.toExternalForm()};
//                HeaderElement[] elements = header.getElements();
//                for (int j = 0; j < elements.length; j++) {
//                }
                headerMap.put(header.getName(), values);
            }
        }
        catch (HttpException he) {
            // error contained in he.getMessage()
            throw new RuntimeException("Fatal HTTP Request Error: " +
                    "Could not sucessfully fire request to url ("+URL+") using method ("+method+")  :: " + he.getMessage(), he);
        }
        catch (IOException ioe) {
            // other exception
            throw new RuntimeException("IOException (transport/connection) Error: " +
                    "Could not sucessfully fire request to url ("+URL+") using method ("+method+")  :: " + ioe.getMessage(), ioe);
        } finally {
            httpMethod.releaseConnection();
        }
        return response;
    }

    /**
     * Merges an existing queryString with a set of params to create one queryString
     * @param queryString the query string in URL encoded form, without a leading '?'
     * @param params a set of key->value strings to use as params for the request
     * @return the merged queryString with the params included
     */
    public static String mergeQueryStringWithParams(String queryString, Map<String, String> params) {
        if (queryString == null) {
            queryString = "";
        }
        if (params != null) {
            StringBuilder sb = new StringBuilder(queryString.trim());
            for (Entry<String, String> entry : params.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append( urlEncodeUTF8(entry.getKey()) );
                sb.append("=");
                sb.append( urlEncodeUTF8(entry.getValue()) );
            }
            queryString = sb.toString();
        }
        return queryString;
    }

    /**
     * Get the query parameters out of a query string and return them as a map,
     * this can be 
     * @param urlString a complete URL with query string at the end or a partial URL
     * or just a query string only (e.g. /thing/stuff?blah=1&apple=fruit
     * @return the map of all query string parameters (e.g. blah => 1, apple => fruit)
     */
    public static Map<String, String> parseURLintoParams(String urlString) {
        if (urlString == null) {
            throw new IllegalArgumentException("URLstring must not be null");
        }
        Map<String, String> map = new HashMap<String, String>();
        // first reduce it to everything after the ?
        int questionLoc = urlString.indexOf('?');
        if (questionLoc >= 0) {
            if (questionLoc+1 < urlString.length()) {
                urlString = urlString.substring(questionLoc+1);
            }
        }
        // next make sure we have at least one param
        if (urlString.indexOf('=') > 0) {
            String[] params = urlString.split("&");
            for (String param : params) {
                int eqLoc = param.indexOf('=');
                if (eqLoc > 0 
                        && eqLoc < param.length()) {
                    String name = param.substring(0, eqLoc);
                    String value = param.substring(eqLoc+1);
                    map.put(name, value);
                }
            }
        }
        return map;
    }

    protected static String urlEncodeUTF8(String toEncode) {
        String encoded = null;
        if (toEncode != null) {
            try {
                encoded = URLEncoder.encode(toEncode, ENCODING_UTF8);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Encoding to URL using UTF8 failed for: " + toEncode + " :: " + e.getMessage(), e);
            }
        }
        return encoded;
    }

    protected static void handleRequestData(EntityEnclosingMethod method, Object data) {
        if (method == null) {
            throw new IllegalArgumentException("Invalid method, cannot be null");
        }
        if (data != null) {
            RequestEntity re = null;
            if (data.getClass().isAssignableFrom(InputStream.class)) {
                re = new InputStreamRequestEntity((InputStream) data, CONTENT_TYPE_UTF8);
            } else if (data.getClass().isAssignableFrom(byte[].class)) {
                re = new ByteArrayRequestEntity((byte[]) data, CONTENT_TYPE_UTF8);
            } else if (data.getClass().isAssignableFrom(File.class)) {
                re = new FileRequestEntity((File) data, CONTENT_TYPE_UTF8);
            } else {
                // handle as a string
                try {
                    re = new StringRequestEntity(data.toString(), "text/xml", ENCODING_UTF8);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Encoding data using UTF8 failed :: " + e.getMessage(), e);
                }
            }
            method.setRequestEntity(re);
        }
    }

    /**
     * Parses a url string into component pieces,
     * unlike the java URL class, this will work with partial urls
     * @param urlString any URL string
     * @return the URL data object
     */
    public static URLData parseURL(String urlString) {
        return new URLData(urlString);
    }

    /**
     * Turns a method string ("get") into a {@link Method} enum object
     * @param method a method string (case is not important) e.g. GET, Post, put, DeLeTe
     * @return the corresponding {@link Method} enum
     */
    public static Method makeMethodFromString(String method) {
        Method m;
        if (method == null || "".equals(method)) {
            throw new IllegalArgumentException("method cannot be null");
        }
        if (method.equalsIgnoreCase("GET")) {
            m = Method.GET;
        } else if (method.equalsIgnoreCase("POST")) {
            m = Method.POST;
        } else if (method.equalsIgnoreCase("PUT")) {
            m = Method.PUT;
        } else if (method.equalsIgnoreCase("DELETE")) {
            m = Method.DELETE;
        } else if (method.equalsIgnoreCase("HEAD")) {
            m = Method.HEAD;
        } else {
            throw new IllegalArgumentException("Unknown http method type ("+method+"): should be GET, POST, PUT, DELETE, or HEAD");
        }
        return m;
    }

    /**
     * Generates a reusable http client wrapper which can be given to {@link #fireRequest(HttpClientWrapper, String, Method, Map, Object, boolean)}
     * as an efficiency mechanism
     * 
     * @param multiThreaded true if you want to allow the client to run in multiple threads
     * @param idleConnectionTimeout if this is 0 then it will use the defaults, otherwise connections will be timed out after this long (ms)
     * @param cookies to send along with every request from this client
     * @return the reusable http client wrapper
     */
    public static HttpClientWrapper makeReusableHttpClient(boolean multiThreaded, int idleConnectionTimeout, Cookie[] cookies) {
        HttpClientWrapper wrapper;
        HttpClient client;
        MultiThreadedHttpConnectionManager connectionManager = null;
        if (multiThreaded) {
            connectionManager = new MultiThreadedHttpConnectionManager();
            client = new HttpClient(connectionManager);
        } else {
            client = new HttpClient();
        }
        if (idleConnectionTimeout <= 0) {
            idleConnectionTimeout = 5000;
        }
        client.getHttpConnectionManager().closeIdleConnections(idleConnectionTimeout);
        client.getHttpConnectionManager().getParams().setConnectionTimeout(idleConnectionTimeout);
        // create the initial state
        HttpState initialState = new HttpState();
        if (cookies != null && cookies.length > 0) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie c = cookies[i];
                org.apache.commons.httpclient.Cookie mycookie = 
                    new org.apache.commons.httpclient.Cookie(c.getDomain(), c.getName(), c.getValue(), c.getPath(), c.getMaxAge(), c.getSecure());
                initialState.addCookie(mycookie);
            }
            client.setState(initialState);
        }
        // set some defaults
        client.getParams().setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1");
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        client.getParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER, true);
        wrapper = new HttpClientWrapper(client, connectionManager, initialState);
        return wrapper;
    }

}
