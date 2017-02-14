/**
 * $Id$
 * $URL$
 * EntityBatchHandler.java - entity-broker - Dec 18, 2008 11:40:39 AM - azeckoski
 **********************************************************************************
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

package org.sakaiproject.entitybroker.rest;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;
import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.providers.ExternalIntegrationProvider;
import org.sakaiproject.entitybroker.rest.caps.BatchProvider;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;
import org.sakaiproject.entitybroker.util.http.HttpClientWrapper;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils.Method;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.URLData;
import org.sakaiproject.entitybroker.util.request.RequestUtils;

import lombok.extern.slf4j.Slf4j;


/**
 * This handles batch operations internally as much as possible,
 * the idea is to provide for a standard way to reduce huge numbers of calls down to 1 call to the server
 * which puts the data together into a single response
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class EntityBatchHandler {

    public static final String CONFIG_BATCH_ENABLE = "entitybroker.batch.enable";
    public static final boolean CONFIG_BATCH_DEFAULT = false;

    private static final String HEADER_BATCH_STATUS = "batchStatus";
    private static final String HEADER_BATCH_ERRORS = "batchErrors";
    private static final String HEADER_BATCH_MAPPING = "batchMapping";
    private static final String HEADER_BATCH_URLS = "batchURLs";
    private static final String HEADER_BATCH_REFS = "batchRefs";
    private static final String HEADER_BATCH_KEYS = "batchKeys";
    private static final String HEADER_BATCH_METHOD = "batchMethod";
    private static final String UNREFERENCED_PARAMS = "NoRefs";
    /**
     * This is the name of the parameter which is used to pass along the reference URLs to be batch processed
     */
    public static final String REFS_PARAM_NAME = "_refs";
    private static final String UNIQUE_DATA_PREFIX = "X-XqReplaceQX-X-";

    private static String INTERNAL_SERVER_ERROR_STATUS_STRING = HttpServletResponse.SC_INTERNAL_SERVER_ERROR+"";

    /**
     * Empty constructor, must use setters to set the needed services
     */
    public EntityBatchHandler() { }

    /**
     * Full constructor, use this to correctly construct this class,
     * note that after construction, the entityRequestHandler must be set also
     */
    public EntityBatchHandler(EntityBrokerManager entityBrokerManager,
            EntityEncodingManager entityEncodingManager,
            ExternalIntegrationProvider externalIntegrationProvider) {
        super();
        this.entityBrokerManager = entityBrokerManager;
        this.entityEncodingManager = entityEncodingManager;
        this.externalIntegrationProvider = externalIntegrationProvider;
        init();
    }

    private EntityProvider batchEP = null;

    public void init() {
        // register the batch EP handler
        if (this.externalIntegrationProvider.getConfigurationSetting(CONFIG_BATCH_ENABLE, CONFIG_BATCH_DEFAULT)) {
            batchEP = new BatchProvider() {
                public String getEntityPrefix() {
                    return EntityRequestHandler.BATCH;
                }
                public String getBaseName() {
                    return getEntityPrefix();
                }
                public ClassLoader getResourceClassLoader() {
                    return EntityDescriptionManager.class.getClassLoader();
                }
                public String[] getHandledOutputFormats() {
                    return EntityEncodingManager.HANDLED_OUTPUT_FORMATS;
                }
            };
            this.entityBrokerManager.getEntityProviderManager().registerEntityProvider(batchEP);
        } else {
            // batch provider is disabled so do not show the docs for it - this empty on purpose
        }
    }

    public void destroy() {
        log.info("EntityBatchHandler: destroy()");
        if (batchEP != null) {
            try {
                this.entityBrokerManager.getEntityProviderManager().unregisterEntityProvider(batchEP);
            } catch (RuntimeException e) {
                log.warn("EntityBatchHandler: Unable to unregister the batch provider: " + e);
            }
        }
    }


    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityEncodingManager entityEncodingManager;
    public void setEntityEncodingManager(EntityEncodingManager entityEncodingManager) {
        this.entityEncodingManager = entityEncodingManager;
    }

    private ExternalIntegrationProvider externalIntegrationProvider;
    public void setExternalIntegrationProvider(ExternalIntegrationProvider externalIntegrationProvider) {
        this.externalIntegrationProvider = externalIntegrationProvider;
    }

    /**
     * Can only set this after the class is constructed since it forms a circular dependency,
     * this is being set by the setter/constructor in the EntityHandlerImpl
     */
    private EntityHandlerImpl entityRequestHandler;
    public void setEntityRequestHandler(EntityHandlerImpl entityRequestHandler) {
        this.entityRequestHandler = entityRequestHandler;
    }

    private String getServletContext() {
        return this.entityBrokerManager.getServletContext();
    }

    private String getServletBatch() {
        return getServletContext() + EntityRequestHandler.SLASH_BATCH;
    }

    /**
     * Handle the batch operations encoded in this view and request
     * @param view the current view
     * @param req the current request
     * @param res the current response
     */
    public void handleBatch(EntityView view, HttpServletRequest req, HttpServletResponse res) {
        if (view == null || req == null || res == null) {
            throw new IllegalArgumentException("Could not process batch: invalid arguments, no args can be null (view="+view+",req="+req+",res="+res+")");
        }

        if (!externalIntegrationProvider.getConfigurationSetting(CONFIG_BATCH_ENABLE, CONFIG_BATCH_DEFAULT)) {
            //log.info("Batch provider is disabled by default/property. See SAK-22619");
            try {
                res.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "Batch provider is disabled by sakai config: "+CONFIG_BATCH_ENABLE+"=false. Enable this config setting with "+CONFIG_BATCH_ENABLE+"=true to enable batch handling. See SAK-22619 for details.");
            } catch (IOException e) {
                throw new RuntimeException("Cannot send error: res.sendError: "+e, e);
            }
            return;
        }

        // first find out which METHOD we are dealing with
        String reqMethod = req.getMethod() == null ? EntityView.Method.GET.name() : req.getMethod().toUpperCase().trim();
        Method method = HttpRESTUtils.makeMethodFromString(reqMethod);

        if (Method.GET.equals(method) 
                || Method.POST.equals(method)
                || Method.PUT.equals(method)
                || Method.DELETE.equals(method)) {
            // valid methods
            res.setHeader(HEADER_BATCH_METHOD, method.name());
        } else {
            throw new IllegalArgumentException("Cannot batch "+reqMethod+" request method, cannot continue processing request: " + view);
        }

        // now get to handling stuff
        String format = view.getFormat();
        String servletContext = getServletContext(); // will be the servlet context (e.g. /direct)

        // validate the the refs param
        String[] refs = getRefsOrFail(req);

        // decode the params into a set of reference params
        Map<String, Map<String, String[]>> referencedParams = extractReferenceParams(req, method, refs);

        // loop through all references
        HashSet<String> processedRefsAndURLs = new HashSet<String>(); // holds all refs which we processed in this batch
        HashMap<String, String> dataMap = new ArrayOrderedMap<String, String>(); // the returned content data from each ref
        Map<String, ResponseBase> results = new ArrayOrderedMap<String, ResponseBase>(); // the results of all valid refs
        boolean successOverall = false; // true if all ok or partial ok, false if exception occurs or all fail
        boolean failure = false;
        for (int i = 0; i < refs.length; i++) {
            String refKey = "ref" + i;
            String reference = refs[i];
            // validate the reference is not blank
            if (reference == null || "".equals(reference)) {
                continue; // skip
            }
            // skip refs that are already done, we do not process twice unless it is a POST
            // NOTE: this duplicate check happens again slightly down below so change both at once
            if (! Method.POST.equals(method) 
                    && processedRefsAndURLs.contains(reference)) {
                log.warn("EntityBatchHandler: Found a duplicate reference, this will not be processed: " + reference);
                continue; // skip for GET/DELETE/PUT
            }
            // fix anything that does not start with a slash or http
            String entityURL = reference;
            if (! reference.startsWith("/") 
                    && ! reference.startsWith("http://")) {
                // assume this is an EB entity url without the slash
                entityURL = servletContext + EntityView.SEPARATOR + reference;
            }
            // make sure no one tries to batch a batch
            if (reference.startsWith(EntityRequestHandler.SLASH_BATCH)
                    || reference.startsWith( getServletBatch() )) {
                throw new EntityException("Failure processing batch request, "
                        + "batch reference ("+reference+") ("+entityURL+") appears to be another "
                        + "batch URL (contains "+EntityRequestHandler.SLASH_BATCH+"), "
                        + "failure in batch request: " + view,
                        EntityRequestHandler.SLASH_BATCH, 
                        HttpServletResponse.SC_BAD_REQUEST);
            }

            // in case there are external ones we will reuse this httpclient
            HttpClientWrapper clientWrapper = null;

            // object will hold the results of this reference request
            ResponseBase result = null;

            // parse the entityURL, should hopefully not cause a failure
            URLData ud = new URLData(entityURL);

            /*
             * identify the EB direct operations - 
             * this allows us to strip extensions and cleanup direct URLs as needed,
             * possibly also handle these specially later on if desired,
             * only EB operations can be handled internally
             */
            if ( servletContext.equals(ud.contextPath) ) {
                if (ud.pathInfo == null || "".equals(ud.pathInfo)) {
                    // looks like this servlet only with no path and we do not process that
                    continue;
                }

                boolean success = false;
                try {
                    // parse the entityURL to verify it
                    entityBrokerManager.parseReference(ud.pathInfo);
                    success = true;
                } catch (IllegalArgumentException e) {
                    String errorMessage = "Failure parsing direct entityURL ("+entityURL+") from reference ("+reference+") from path ("+ud.pathInfo+"): " + e.getMessage() + ":" + e.getCause();
                    log.warn("EntityBatchHandler: " + errorMessage);
                    result = new ResponseError(reference, entityURL, errorMessage);
                }

                if (success) {
                    if (Method.GET.equals(method)) {
                        // rebuild the entityURL with the correct extension in there for GET
                        StringBuilder sb = new StringBuilder();
                        sb.append( servletContext );
                        sb.append(ud.pathInfoNoExtension);
                        sb.append(EntityView.PERIOD);
                        sb.append(format);
                        if (ud.query.length() > 0) {
                            // add on the query string
                            sb.append('?');
                            sb.append(ud.query);
                        }
                        entityURL = sb.toString();
                    }

                    // skip URLs that are already done, we do not process twice unless it is a POST
                    // NOTE: this duplicate check happens again slightly above so change both at once
                    if (! Method.POST.equals(method) 
                            && processedRefsAndURLs.contains(entityURL)) {
                        log.warn("EntityBatchHandler: Found a duplicate entityURL, this will not be processed: " + entityURL);
                        continue; // skip
                    }

                    result = generateInternalResult(refKey, reference, entityURL, req, res, method, referencedParams);
                }

            } else {
                // non-EB URL so we have to fire it off using the HttpUtils

                // http utils requires full URLs
                entityURL = makeFullExternalURL(req, entityURL);

                // set the client wrapper with cookies so we can reuse it for efficiency
                if (clientWrapper == null) {
                    clientWrapper = HttpRESTUtils.makeReusableHttpClient(false, 0, req.getCookies());
                }
                result = generateExternalResult(refKey, reference, entityURL, method, referencedParams, clientWrapper);
            }

            // special handling for null result (should really not happen unless there was a logic error)
            if (result == null) {
                successOverall = false;
                failure = true;
                throw new IllegalStateException("Somehow the result is null, this should never happen, fatal error");
            }

            if (result instanceof ResponseError) {
                // looks like a failure occurred, keep going though
                successOverall = false;
                failure = true;
            } else {
                // all ok, process data
                int status = result.getStatus();
                if (status >= 200 && status < 300) {
                    successOverall = true;
                }
                if (status == HttpServletResponse.SC_NO_CONTENT) {
                    // no content to process
                    ((ResponseResult)result).content = null;
                    ((ResponseResult)result).data = null;
                } else {
                    // process the content and see if it matches the expected result, if not we have to dump it in escaped
                    String content = ((ResponseResult)result).content;
                    String dataKey = checkContent(format, content, refKey, dataMap);
                    ((ResponseResult)result).setDataKey(dataKey);
                }
            }

            // store the processed ref and url so we do not do them again
            processedRefsAndURLs.add(reference);
            processedRefsAndURLs.add(entityURL);
            results.put(refKey, result); // use an artificial key
        }

        // determine overall status
        int overallStatus = HttpServletResponse.SC_OK;
        if (failure == true || successOverall == false) {
            overallStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        // die if every ref was invalid
        if (results.size() == 0) {
            throw new EntityException("Invalid request which resulted in no valid references to batch process, original _refs=("
                    +ArrayUtils.arrayToString(refs)+")", EntityRequestHandler.SLASH_BATCH, HttpServletResponse.SC_BAD_REQUEST);
        }

        // compile all the responses into encoded data
        String overallData = entityEncodingManager.encodeData(results, format, "refs", null);
        if (Formats.XML.equals(format)) {
            overallData = EntityEncodingManager.XML_HEADER + overallData;
        }
        // replace the data unique keys if there are any
        overallData = reintegrateDataContent(format, dataMap, overallData);

        // put response, headers, and code into the http response
        applyOverallHeaders(res, results);
        // put content into the response
        try {
            res.getWriter().write(overallData);
        } catch (IOException e) {
            throw new RuntimeException("Unable to encode data for overall response: " + e.getMessage(), e);
        }
        // set encoding
        RequestUtils.setResponseEncoding(format, res);
        // set overall status code
        res.setStatus(overallStatus);
    }

    /**
     * This will decode the set of params into a group of reference params based on the set of references
     * @param req the current request
     * @param method the current method
     * @param refs the array of references
     * @return the map of reference params with unreferenced params under the {@value #UNREFERENCED_PARAMS} key
     * and everything else in ref# -> params map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, String[]>> extractReferenceParams(
            HttpServletRequest req, Method method, String[] refs) {
        // Decode params into a reference map based on the refs for POST/PUT
        ArrayOrderedMap<String, Map<String, String[]>> referencedParams = null;
        if (Method.POST.equals(method) || Method.PUT.equals(method) ) {
            referencedParams = new ArrayOrderedMap<String, Map<String, String[]>>();
            Map<String, String[]> params = req.getParameterMap();
            // create the maps to hold the params
            referencedParams.put(UNREFERENCED_PARAMS, new ArrayOrderedMap<String, String[]>(params.size()));
            for (int i = 0; i < refs.length; i++) {
                String refKey = "ref" + i + '.';
                referencedParams.put(refKey, new ArrayOrderedMap<String, String[]>(params.size()));
            }
            // put all request params into the map
            for (Entry<String, String[]> entry : params.entrySet()) {
                if (REFS_PARAM_NAME.equals(entry.getKey())) {
                    continue; // skip over the refs param
                }
                boolean found = false;
                for (String refKey : referencedParams.keySet()) {
                    if (entry.getKey().startsWith(refKey)) {
                        String key = entry.getKey();
                        // fix key by removing the ref#. prefix
                        key = key.substring(refKey.length());
                        if (key.length() == 0) {
                            log.warn("EntityBatchHandler: " +
                            		"Skipping invalid reference param name ("+entry.getKey()+"), " +
                            		"name must start with ref#. but MUST have the actual name of the param after that");
                        } else {
                            referencedParams.get(refKey).put(key, entry.getValue());
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // put the values into the unreferenced params portion of the map
                    referencedParams.get(UNREFERENCED_PARAMS).put(entry.getKey(), entry.getValue());
                }
            }
        }
        return referencedParams;
    }

    /**
     * Apply the headers to the batched response,
     * these headers are applied to all responses
     * @param res the response to apply headers to
     * @param results the results of the requests
     */
    private void applyOverallHeaders(HttpServletResponse res, Map<String, ResponseBase> results) {
        // set overall headers - batchRefs, batchKeys, batchStatus, batchErrors, batchInvalidRefs
        int count = 0;
        for (Entry<String, ResponseBase> entry : results.entrySet()) {
            String refKey = entry.getKey();
            ResponseBase refResp = entry.getValue();
            if (count == 0) {
                res.setHeader(HEADER_BATCH_KEYS, refKey);
                res.setHeader(HEADER_BATCH_REFS, refResp.getReference());
                res.setHeader(HEADER_BATCH_URLS, refResp.getEntityURL());
                res.setHeader(HEADER_BATCH_MAPPING, refKey + "=" + refResp.getReference());
            } else {
                res.addHeader(HEADER_BATCH_KEYS, refKey);
                res.addHeader(HEADER_BATCH_REFS, refResp.getReference());
                res.addHeader(HEADER_BATCH_URLS, refResp.getEntityURL());
                res.addHeader(HEADER_BATCH_MAPPING, refKey + "=" + refResp.getReference());
            }
            if (refResp.isFailure()) {
                res.addHeader(HEADER_BATCH_ERRORS, refKey);
                res.addHeader(HEADER_BATCH_STATUS, INTERNAL_SERVER_ERROR_STATUS_STRING);
            } else {
                int status = ((ResponseResult) refResp).getStatus();
                res.addHeader(HEADER_BATCH_STATUS, Integer.toString(status));
            }
            count++;
        }
    }


    /**
     * Processing internal (EB) requests
     * @return the result from the request (may be an error)
     */
    private ResponseBase generateInternalResult(String refKey, String reference, String entityURL, HttpServletRequest req, 
            HttpServletResponse res, Method method, Map<String, Map<String, String[]>> referencedParams) {
        ResponseBase result = null;
        ResponseError error = null;

        /* WARNING: This is important to understand why this was done as is
         * First of all, forget the servlet forwarding, it is hopeless.
         * Why you ask? This is why, tomcat 5 has issues with calling forward using a set of custom
         * httpservelet* objects, it REQUIRES objects that are tomcat objects and attempts to use and
         * even cast to those objects. This causes 2 failures:
         * 1) tomcat attempts to access specific attributes from the request which are not there in
         * other request objects, thus it skips over processing the request entirely... without marking it as failed...
         * 2) tomcat attempts to cast the response object to a tomcat object after most of the processing
         * is complete and causes a ClassCastException which blows up everything
         * 
         * The alternatives are using httpclient for everything (maybe not a bad plan)
         * or calling the entity request handler directly, this has its own issues in that
         * it actually causes problems with redirection and requires injecting things
         * which depend on each other
         * 
         * One last note on this, this all works fine in Jetty... tomcat fail
         * Fun times for all
         */

        EntityHttpServletRequest entityRequest = new EntityHttpServletRequest(req, entityURL);
        entityRequest.setContextPath("");
        if (Method.POST.equals(method) || Method.PUT.equals(method) ) {
            // set only the unreferenced and correct referenced params for this request
            entityRequest.clearParameters(); // also clears REFS_PARAM_NAME
            entityRequest.setParameters( referencedParams.get(UNREFERENCED_PARAMS) );
            String key = refKey + '.';
            if (referencedParams.containsKey(key)) {
                entityRequest.setParameters( referencedParams.get(key) );
            }
            // set the params from the query itself again
            entityRequest.setParameters( entityRequest.pathQueryParams );
            //log.info("All request params: " + entityRequest.getStringParameters());
        } else {
            entityRequest.removeParameter(REFS_PARAM_NAME); // make sure this is not passed along
        }
        entityRequest.setUseRealDispatcher(false); // we do not want to actually have the container handle forwarding
        EntityHttpServletResponse entityResponse = new EntityHttpServletResponse(res);

        boolean redirected = false;
        do {
            try {
                entityRequestHandler.handleEntityAccess(entityRequest, entityResponse, null);
                redirected = false; // assume no redirect
            } catch (Exception e) {
                String errorMessage = "Failure attempting to process reference ("+reference+") for url ("+entityURL+"): " + e.getMessage() + ":" + e;
                log.warn("EntityBatchHandler: " + errorMessage);
                error = new ResponseError(reference, entityURL, errorMessage);
                break; // take us out if there is a failure
            }
            // Must handle all redirects manually despite the annoyance - this is really crappy but oh well
            if (entityResponse.isRedirected()) {
                String redirectURL = entityResponse.getRedirectedUrl();
                if (redirectURL == null || redirectURL.length() == 0) {
                    throw new EntityException("Failed to find redirect URL when redirect was indicated by status ("+entityResponse.getStatus()+") for reference ("+reference+")", reference);
                }
                entityURL = redirectURL;
                // check that the redirect is not external
                if ( entityURL.startsWith(getServletContext()) ) {
                    // internal
                    entityRequest.setPathString(redirectURL);
                    entityResponse.reset();
                    redirected = true;
                } else {
                    // TODO find a way to handle an external URL here
                    redirected = false;
                }
            }
        } while (redirected);

        /** OLD CODE which can't work in tomcat 5
        // setup the request and response objects to do the reference request
        RequestDispatcher dispatcher = req.getRequestDispatcher(entityURL); // should only be the relative path from this webapp
        // the request needs to get the full url or path though
        EntityHttpServletRequest entityRequest = new EntityHttpServletRequest(req, req.getContextPath() + entityURL);
        entityRequest.setContextPath("");
        entityRequest.removeParameter(REFS_PARAM_NAME); // make sure this is not passed along
        EntityHttpServletResponse entityResponse = new EntityHttpServletResponse(res);

        // fire off the URLs to the server and get back responses
        try {
            // need to forward instead of include to get headers back
            dispatcher.forward(entityRequest, entityResponse);
        } catch (Exception e) {
            String errorMessage = "Failure attempting to process reference ("+reference+"): " + e.getMessage() + ":" + e;
            log.warn(errorMessage, e);
            error = new ResponseError(reference, entityURL, errorMessage);
        }
         **/

        // create the result object to encode and place into the final response
        if (error == null && entityResponse != null) {
            // all ok, create the result for the response object
            // all cookies go into the main response
            Cookie[] cookies = entityResponse.getCookies();
            for (Cookie cookie : cookies) {
                res.addCookie(cookie);
            }
            // status codes are compiled
            int status = entityResponse.getStatus();
            // create the result (with raw content)
            result = new ResponseResult(reference, entityURL, status, entityResponse.getHeaders(), entityResponse.getContentAsString());
        } else {
            result = error;
        }
        return result;
    }

    /**
     * Processing external (non-EB) requests
     * @return the result from the request (may be an error)
     */
    private ResponseBase generateExternalResult(String refKey, String reference, String entityURL, Method method, 
            Map<String, Map<String, String[]>> referencedParams, HttpClientWrapper clientWrapper) {
        ResponseBase result = null;
        ResponseError error = null;

        boolean guaranteeSSL = false;
        // TODO allow enabling SSL?
        Map<String, String> params = null;
        if (referencedParams != null && ! referencedParams.isEmpty()) {
            params = new ArrayOrderedMap<String, String>(referencedParams.size());
            // put all unreferenced params in
            Map<String, String[]> urp = referencedParams.get(UNREFERENCED_PARAMS);
            for (Entry<String, String[]> entry : urp.entrySet()) {
                String name = entry.getKey();
                String value;
                if (entry.getValue() == null || entry.getValue().length == 0) {
                    value = "";
                } else {
                    value = entry.getValue()[0];
                }
                params.put(name, value);
            }
            if (Method.POST.equals(method) || Method.PUT.equals(method) ) {
                // put all referenced params in for this key
                String key = refKey + '.';
                Map<String, String[]> rp = referencedParams.get(key);
                if (rp != null) {
                    for (Entry<String, String[]> entry : rp.entrySet()) {
                        String name = entry.getKey();
                        String value;
                        if (entry.getValue() == null || entry.getValue().length == 0) {
                            value = "";
                        } else {
                            value = entry.getValue()[0];
                        }
                        params.put(name, value);
                    }
                }
            }
        }
        // fire off the request and hope it does not die horribly
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpRESTUtils.fireRequest(clientWrapper, 
                    entityURL, 
                    method,
                    params, null, guaranteeSSL);
        } catch (RuntimeException e) {
            String errorMessage = "Failure attempting to process external URL ("+entityURL+") from reference ("+reference+"): " + e.getMessage() + ":" + e;
            log.warn("EntityBatchHandler: " + errorMessage);
            error = new ResponseError(reference, entityURL, errorMessage);
        }

        // create the result object to encode and place into the final response
        if (error == null && httpResponse != null) {
            result = new ResponseResult(reference, entityURL, httpResponse.getResponseCode(), 
                    httpResponse.getResponseHeaders(), httpResponse.getResponseBody());
        } else {
            result = error;
        }
        return result;
    }

    /**
     * Creates a full URL so that the request can be sent
     * @param req the request
     * @param entityURL the partial URL (e.g. /thing/blah)
     * @return a full URL (e.g. http://server/thing/blah)
     */
    private String makeFullExternalURL(HttpServletRequest req, String entityURL) {
        if (entityURL.startsWith("/")) {
            // http client can only deal in complete URLs - e.g. "http://localhost:8080/thing"
            String serverName = "localhost"; // req.getServerName();
            try {
                InetAddress i4 = Inet4Address.getLocalHost();
                serverName = i4.getHostAddress();
            } catch (UnknownHostException e) {
                // could not get address, try the fallback
                serverName = "localhost";
            }
            int serverPort = req.getLocalPort(); // getServerPort();
            String protocol = req.getScheme();
            if (protocol == null || "".equals(protocol)) {
                protocol = "http";
            }
            StringBuilder sb = new StringBuilder(); // the server URL
            sb.append(protocol);
            sb.append("://");
            sb.append(serverName);
            if (serverPort > 0) {
                sb.append(":");
                sb.append(serverPort);
            }
            // look up the server URL using a service?
            entityURL = sb.toString() + entityURL;
        }
        return entityURL;
    }

    /**
     * Gets the refs (batch URLs to handle) from the request if possible
     * @param req the request
     * @return the array of references
     * @throws IllegalArgumentException if the refs canot be found
     */
    private String[] getRefsOrFail(HttpServletRequest req) {
        String[] refs = req.getParameterValues(REFS_PARAM_NAME);
        if (refs == null || refs.length == 0) {
            throw new IllegalArgumentException(REFS_PARAM_NAME + " parameter must be set (e.g. /direct/batch.json?"+REFS_PARAM_NAME+"=/sites/popular,/sites/newest)");
        }
        if (refs.length == 1) {
            // process separated list, assume comma separated
            String separator = req.getParameter("separator");
            if (separator == null || "".equals(separator)) {
                separator = ",";
            }
            String presplit = refs[0];
            refs = presplit.split(separator);
            if (refs == null || refs.length == 0) {
                throw new IllegalStateException("Failure attempting to process the _refs ("+presplit+") listing, could not get the final list of refs out by splitting using the separator ("+separator+")");
            }
        }
        if (refs.length <= 0) {
            throw new IllegalArgumentException(REFS_PARAM_NAME + " parameter must be set and there must be at least 1 reference (e.g. /direct/batch.json?"+REFS_PARAM_NAME+"=/sites/popular,/sites/newest)");
        }
        return refs;
    }

    /**
     * Takes the overall data and reintegrates any content data that is waiting to be merged,
     * this may do nothing if there is no content to merge
     * @return the integrated data content
     */
    private String reintegrateDataContent(String format, HashMap<String, String> dataMap,
            String overallData) {
        StringBuilder sb = new StringBuilder();
        int curLoc = 0;
        for (Entry<String, String> entry : dataMap.entrySet()) {
            // looping order is critical here, it must be in the same order it was added
            if (entry.getKey() != null && ! "".equals(entry.getKey())) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (Formats.XML.equals(format)) {
                    value = "\n" + value; // add in a break
                } else if (Formats.JSON.equals(format)) {
                    key = '"' + key + '"'; // have to also replace the quotes
                }
                int keyLoc = overallData.indexOf(key);
                if (keyLoc > -1) {
                    sb.append( overallData.subSequence(curLoc, keyLoc) );
                    sb.append( value );
                    curLoc = keyLoc + key.length();
                }
                //overallData = overallData.replace(key, value);
            }
        }
        // add in the remainder of the overall data string
        sb.append( overallData.subSequence(curLoc, overallData.length()) );
        return sb.toString();
    }

    /**
     * Checks that the content is in the correct format and is not too large,
     * if it is too large it will not be processed and if it is in the wrong format it will be encoded as a data chunk,
     * it is OK it will be placed into the dataMap and reintegrated after encoding
     * @param content this it the content of the response body (if null then no processing occurs, null returned)
     * @return the dataKey which maps to the real content, need replace the key later OR null if the content is empty or too large
     */
    private String checkContent(String format, String content, String refKey,
            HashMap<String, String> dataMap) {
        String dataKey = null;
        if (content != null) {
            content = content.trim();
            if (! "".equals(content)) {
                if (entityEncodingManager.validateFormat(content, format)) {
                    if (Formats.XML.equals(format) 
                            || Formats.HTML.equals(format)) {
                        // strip off the xml header and doctype if it exists
                        content = stripOutXMLTag(content, "<?", "?>");
                        content = stripOutXMLTag(content, "<!DOCTYPE", ">");
                    }
                    // valid for the current format so insert later instead of merging now
                    dataKey = UNIQUE_DATA_PREFIX + refKey;
                    dataMap.put(dataKey, content);
                }
            }
        }
        return dataKey;
    }

    /**
     * This will strip any tag out of an xml file by finding the startTag (if possible),
     * and then the endTag and chopping this out of the given content and returning
     * the new value
     * @param content any XML like content
     * @param startTag the starting tag (e.g. "<?" or "<blah")
     * @param endTag the ending tag (e.g "?>" or "/blah")
     * @return the content without the chopped out part if found
     */
    public String stripOutXMLTag(String content, String startTag, String endTag) {
        if (startTag != null 
                && ! "".equals(startTag)
                && endTag != null 
                && ! "".equals(endTag)) {
            int pos = content.indexOf(startTag);
            if (pos >= 0) {
                int end = content.indexOf(endTag, pos);
                if (end > 0) {
                    StringBuilder sb = new StringBuilder();
                    if (pos > 0) {
                        sb.append( content.substring(0, pos) );
                    }
                    sb.append( content.substring(end + endTag.length()) );
                    content = sb.toString().trim();
                }
            }
        }
        return content;
    }

    /**
     * Base class for all response data which will be encoded and output
     */
    public static class ResponseBase {
        public String reference;
        public String getReference() {
            return reference;
        }
        public String entityURL;
        public String getEntityURL() {
            return entityURL;
        }
        public int status;
        public int getStatus() {
            return status;
        }
        public transient boolean failure = false;
        public boolean isFailure() {
            return failure;
        }
    }

    /**
     * Holds the error values which will be encoded by the various EB utils
     */
    public static class ResponseError extends ResponseBase {
        public String error;
        public ResponseError(String reference, String entityURL, String errorMessage) {
            this.reference = reference;
            this.entityURL = entityURL;
            this.error = errorMessage;
            this.failure = true;
            this.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Holds the results from a successful response request
     */
    public static class ResponseResult extends ResponseBase {
        public Map<String, String[]> headers;
        public String data;
        /**
         * Set the data key (clears the raw content) if the key is non-null
         * @param data processed data
         */
        public void setDataKey(String dataKey) {
            if (dataKey != null) {
                this.data = dataKey;
                this.content = null;
            }
        }
        /**
         * The raw content from the request
         */
        public String content;
        public ResponseResult(String reference, String entityURL, int status, Map<String, String[]> headers) {
            this.reference = reference;
            this.entityURL = entityURL;
            this.status = status;
            this.headers = headers;
            this.failure = false;
            this.content = null;
            this.data = null;
        }
        public ResponseResult(String reference, String entityURL, int status, Map<String, String[]> headers, String content) {
            this.reference = reference;
            this.entityURL = entityURL;
            this.status = status;
            this.headers = headers;
            this.content = content;
            this.data = null;
            this.failure = false;
        }
    }

}
