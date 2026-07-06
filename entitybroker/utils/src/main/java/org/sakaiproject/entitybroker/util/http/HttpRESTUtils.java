/**
 * $Id$
 * $URL$
 * HttpUtils.java - entity-broker - Jul 20, 2008 11:42:19 AM - azeckoski
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

package org.sakaiproject.entitybroker.util.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;


/**
 * Utilities for generating and processing http requests,
 * allows the developer to fire off a request and get back information about the response<br/>
 * All encoding is automatically UTF-8
 *
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class HttpRESTUtils {

    public static final String CONTENT_TYPE_UTF8 = "text/xml; charset=UTF-8";
    public static final String FORM_CONTENT_TYPE_UTF8 = "application/x-www-form-urlencoded; charset=UTF-8";
    public static final String ENCODING_UTF8 = "UTF-8";
    public static enum Method {POST, GET, PUT, DELETE, HEAD, OPTIONS, TRACE};
    public static final int MAX_RESPONSE_SIZE_CHARS = 1024*1024; // about a million chars max
    private static final int DEFAULT_TIMEOUT = 5000;
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1";

    /**
     * Fire off a request to a URL using the specified method,
     * include optional params and data in the request,
     * the response data will be returned in the object if the request can be carried out
     *
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @return an object representing the response, includes data about the response
     * @throws HttpRequestException if the request cannot be processed for some reason (this is unrecoverable)
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
     * @throws HttpRequestException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(String URL, Method method, Map<String, String> params) {
        return fireRequest(null, URL, method, params, null, null, false);
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
     * @param guaranteeSSL ignored; HTTPS now uses the JVM trust configuration
     * @return an object representing the response, includes data about the response
     * @throws HttpRequestException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(String URL, Method method, Map<String, String> params, Object data, boolean guaranteeSSL) {
        return fireRequest(null, URL, method, params, null, data, guaranteeSSL);
    }

    /**
     * Fire off a request to a URL using the specified method,
     * include optional params, headers, and data in the request,
     * the response data will be returned in the object if the request can be carried out
     *
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @param params (optional) params to send along with the request, will be encoded in the query string or in the body depending on the method
     * @param headers (optional) headers to send along with the request
     * @param data (optional) data to send along in the body of the request, this only works for POST and PUT requests, ignored for the other types
     * @param guaranteeSSL ignored; HTTPS now uses the JVM trust configuration
     * @return an object representing the response, includes data about the response
     * @throws HttpRequestException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(String URL, Method method, Map<String, String> params, Map<String, String> headers, Object data, boolean guaranteeSSL) {
        return fireRequest(null, URL, method, params, headers, data, guaranteeSSL);
    }

    /**
     * Fire off a request to a URL using the specified method but reuse the client for efficiency,
     * include optional params and data in the request,
     * the response data will be returned in the object if the request can be carried out
     *
     * @param httpClientWrapper (optional) allows the http client to be reused for efficiency,
     * if null a new one will be created each time, use {@link #makeReusableHttpClient(boolean, int, Cookie[])} to
     * create a reusable instance
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @param params (optional) params to send along with the request, will be encoded in the query string or in the body depending on the method
     * @param data (optional) data to send along in the body of the request, this only works for POST and PUT requests, ignored for the other types
     * @param guaranteeSSL ignored; HTTPS now uses the JVM trust configuration
     * @return an object representing the response, includes data about the response
     * @throws HttpRequestException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(HttpClientWrapper httpClientWrapper, String URL, Method method, Map<String, String> params, Object data, boolean guaranteeSSL) {
        return fireRequest(httpClientWrapper, URL, method, params, null, data, guaranteeSSL);
    }

    /**
     * Fire off a request to a URL using the specified method but reuse the client for efficiency,
     * include optional params and data in the request,
     * the response data will be returned in the object if the request can be carried out
     *
     * @param httpClientWrapper (optional) allows the http client to be reused for efficiency,
     * if null a new one will be created each time, use {@link #makeReusableHttpClient(boolean, int, Cookie[])} to
     * create a reusable instance
     * @param URL the url to send the request to (absolute or relative, can include query params)
     * @param method the method to use (e.g. GET, POST, etc.)
     * @param params (optional) params to send along with the request, will be encoded in the query string or in the body depending on the method
     * @param headers (optional) headers to send along with the request
     * @param data (optional) data to send along in the body of the request, this only works for POST and PUT requests, ignored for the other types
     * @param guaranteeSSL ignored; HTTPS now uses the JVM trust configuration
     * @return an object representing the response, includes data about the response
     * @throws HttpRequestException if the request cannot be processed for some reason (this is unrecoverable)
     */
    public static HttpResponse fireRequest(HttpClientWrapper httpClientWrapper, String URL, Method method, Map<String, String> params, Map<String, String> headers, Object data, boolean guaranteeSSL) {
        if (guaranteeSSL) {
            log.debug("Ignoring guaranteeSSL=true for request to {}; HTTPS uses JVM trust configuration", URL);
        }

        if (httpClientWrapper == null || httpClientWrapper.getRestTemplate() == null) {
            httpClientWrapper = makeReusableHttpClient(false, 0, null);
        }

        // figure out the request URI plus any body and content type for this method
        final URI uri;
        final Object body;
        final String contentType;
        if (method.equals(Method.GET)) {
            uri = buildUriWithParams(URL, params);
            if (data != null) {
                log.warn("Data cannot be passed in GET requests, data will be ignored (org.sakaiproject.entitybroker.util.http.HttpUtils#fireRequest)");
            }
            body = null;
            contentType = null;
        } else if (method.equals(Method.POST)) {
            if (data == null) {
                // post the params as a url encoded form body
                uri = buildUriWithParams(URL, null);
                body = mergeQueryStringWithParams("", params);
                contentType = FORM_CONTENT_TYPE_UTF8;
            } else {
                uri = buildUriWithParams(URL, params);
                body = data;
                contentType = CONTENT_TYPE_UTF8;
            }
        } else if (method.equals(Method.PUT)) {
            uri = buildUriWithParams(URL, params);
            body = data;
            contentType = (data != null) ? CONTENT_TYPE_UTF8 : null;
        } else if (method.equals(Method.DELETE)) {
            uri = buildUriWithParams(URL, params);
            if (data != null) {
                log.warn("Data cannot be passed in DELETE requests, data will be ignored (org.sakaiproject.entitybroker.util.http.HttpUtils#fireRequest)");
            }
            body = null;
            contentType = null;
        } else {
            throw new HttpRequestException("Cannot handle method: " + method);
        }

        final Map<String, String> requestHeaders = headers;
        RequestCallback requestCallback = request -> {
            HttpHeaders reqHeaders = request.getHeaders();
            reqHeaders.set(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT);
            if (contentType != null) {
                reqHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
            }
            // caller supplied headers take precedence over the defaults above
            if (requestHeaders != null) {
                for (Entry<String, String> entry : requestHeaders.entrySet()) {
                    reqHeaders.set(entry.getKey(), entry.getValue());
                }
            }
            if (body != null) {
                writeRequestBody(request.getBody(), body);
            }
        };
        ResponseExtractor<HttpResponse> responseExtractor = response -> extractResponse(URL, response);

        httpClientWrapper.seedInitialCookies(uri.getHost());
        try {
            return httpClientWrapper.getRestTemplate().execute(uri, HttpMethod.valueOf(method.name()),
                    requestCallback, responseExtractor);
        } catch (ResourceAccessException e) {
            throw new HttpIOException("IOException (transport/connection) Error: "
                    + "Could not sucessfully fire request to url (" + URL + ") using method (" + method + ")  :: " + e.getMessage(), e);
        } catch (RestClientException | IllegalArgumentException e) {
            throw new HttpRequestException("Fatal HTTP Request Error: "
                    + "Could not sucessfully fire request to url (" + URL + ") using method (" + method + ")  :: " + e.getMessage(), e);
        }
    }

    private static URI buildUriWithParams(String url, Map<String, String> params) {
        String requestUrl = url;
        String queryString = mergeQueryStringWithParams(null, params);
        if (queryString.length() > 0) {
            requestUrl += requestUrl.contains("?") ? "&" : "?";
            requestUrl += queryString;
        }
        URI uri = URI.create(requestUrl);
        if (!uri.isAbsolute()) {
            String path = requestUrl.startsWith("/") ? requestUrl : "/" + requestUrl;
            uri = URI.create("http://localhost" + path);
        }
        return uri;
    }

    private static void writeRequestBody(OutputStream output, Object data) throws IOException {
        if (data instanceof InputStream) {
            try (InputStream input = (InputStream) data) {
                StreamUtils.copy(input, output);
            }
        } else if (data instanceof byte[]) {
            output.write((byte[]) data);
        } else if (data instanceof File) {
            try (InputStream input = Files.newInputStream(((File) data).toPath())) {
                StreamUtils.copy(input, output);
            }
        } else {
            output.write(data.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Reads the response body as UTF-8, aborting early if it grows past the maximum allowed size.
     * Streaming the read (instead of buffering the whole response first) preserves the protection
     * added for SAK-20405 against responses large enough to exhaust memory.
     */
    private static HttpResponse extractResponse(String url, ClientHttpResponse response) throws IOException {
        HttpResponse httpResponse = new HttpResponse(response.getRawStatusCode());
        httpResponse.setResponseMessage(response.getStatusText() != null ? response.getStatusText() : "");
        httpResponse.setResponseHeaders(buildResponseHeaders(response.getHeaders()));

        StringBuilder out = new StringBuilder();
        char[] buffer = new char[4096];
        try (Reader reader = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8)) {
            for (int n; (n = reader.read(buffer)) != -1;) {
                out.append(buffer, 0, n);
                if (out.length() > MAX_RESPONSE_SIZE_CHARS) {
                    throw new HttpRequestException("Response size (" + out.length() + " chars) from url (" + url + ") exceeded the maximum allowed batch response size (" + MAX_RESPONSE_SIZE_CHARS + " chars) while processing the response");
                }
            }
        }
        httpResponse.setResponseBody(out.toString());
        return httpResponse;
    }

    private static HashMap<String, String[]> buildResponseHeaders(HttpHeaders headers) {
        HashMap<String, String[]> responseHeaders = new HashMap<String, String[]>();
        for (Entry<String, List<String>> header : headers.entrySet()) {
            List<String> values = header.getValue();
            if (values != null && !values.isEmpty()) {
                responseHeaders.put(header.getKey(), values.toArray(new String[0]));
            }
        }
        return responseHeaders;
    }

    /**
     * Merges an existing queryString with a set of params to create one queryString,
     * this basically just adds the params to the end of the existing query string and will not insert a "?"
     * but will take care of the "&"s
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
            encoded = URLEncoder.encode(toEncode, StandardCharsets.UTF_8);
        }
        return encoded;
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
        } else if (method.equalsIgnoreCase("OPTIONS")) {
            m = Method.OPTIONS;
        } else if (method.equalsIgnoreCase("TRACE")) {
            m = Method.TRACE;
        } else {
            throw new IllegalArgumentException("Unknown http method type ("+method+"): should be GET, POST, PUT, DELETE, HEAD, OPTIONS, or TRACE");
        }
        return m;
    }

    /**
     * Generates a reusable http client wrapper which can be given to {@link #fireRequest(HttpClientWrapper, String, Method, Map, Object, boolean)}
     * as an efficiency mechanism
     *
     * @param multiThreaded ignored; Java HTTP clients are safe to reuse across requests
     * @param idleConnectionTimeout if this is 0 then it will use the defaults, otherwise requests will time out after this long (ms)
     * @param cookies to send along with every request from this client
     * @return the reusable http client wrapper
     */
    public static HttpClientWrapper makeReusableHttpClient(boolean multiThreaded, int idleConnectionTimeout, Cookie[] cookies) {
        int requestTimeout = idleConnectionTimeout <= 0 ? DEFAULT_TIMEOUT : idleConnectionTimeout;
        CookieStore cookieStore = HttpClientWrapper.makeCookieStore();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(requestTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .setSocketTimeout(requestTimeout)
                .build();
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(new PoolingHttpClientConnectionManager())
                .setDefaultCookieStore(cookieStore)
                .setDefaultRequestConfig(requestConfig)
                .build();
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
        // status codes (including 4xx/5xx) are reported back in the response, never thrown
        restTemplate.setErrorHandler(new PassThroughResponseErrorHandler());
        return new HttpClientWrapper(restTemplate, client, cookieStore, cookies);
    }

    /**
     * Treats every response as a success so the status code is returned to the caller
     * (in {@link HttpResponse#getResponseCode()}) instead of being raised as an exception.
     */
    private static class PassThroughResponseErrorHandler implements ResponseErrorHandler {
        public boolean hasError(ClientHttpResponse response) {
            return false;
        }
        public void handleError(ClientHttpResponse response) {
            // no-op: callers inspect the status code themselves
        }
    }

    /**
     * Encode a date into an http date string
     * @param date the date
     * @return the date string or an empty string if the date is null
     */
    public static String encodeDateHttp(Date date) {
        String str = "";
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z"); // RFC 2822
            str = sdf.format(date);
        }
        return str;
    }

    /**
     * Indicates a general failure
     */
    public static class HttpRequestException extends RuntimeException {
        private static final long serialVersionUID = -5911335085507422663L;
        public HttpRequestException(String message, Throwable cause) {
            super(message, cause);
        }
        public HttpRequestException(String message) {
            super(message);
        }
    }

    /**
     * Indicates an IO failure
     */
    public static class HttpIOException extends RuntimeException {
        private static final long serialVersionUID = 9040247081054206662L;
        public HttpIOException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
