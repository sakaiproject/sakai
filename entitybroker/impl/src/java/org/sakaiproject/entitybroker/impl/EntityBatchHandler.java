/**
 * $Id$
 * $URL$
 * EntityBatchHandler.java - entity-broker - Dec 18, 2008 11:40:39 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.azeckoski.reflectutils.ArrayUtils;
import org.azeckoski.reflectutils.map.ArrayOrderedMap;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.util.RequestUtils;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;
import org.sakaiproject.entitybroker.util.http.HttpClientWrapper;
import org.sakaiproject.entitybroker.util.http.HttpRESTUtils;
import org.sakaiproject.entitybroker.util.http.HttpResponse;
import org.sakaiproject.entitybroker.util.http.URLData;


/**
 * This handles batch operations internally as much as possible,
 * the idea is to provide for a standard way to reduce huge numbers of calls down to 1 call to the server
 * which puts the data together into a single response
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityBatchHandler {

    private static final String HEADER_BATCH_STATUS = "batchStatus";
    private static final String HEADER_BATCH_ERRORS = "batchErrors";
    private static final String HEADER_BATCH_MAPPING = "batchMapping";
    private static final String HEADER_BATCH_URLS = "batchURLs";
    private static final String HEADER_BATCH_REFS = "batchRefs";
    private static final String HEADER_BATCH_KEYS = "batchKeys";
    /**
     * This is the name of the parameter which is used to pass along the reference URLs to be batch processed
     */
    private static final String REFS_PARAM_NAME = "refs";
    private static final String UNIQUE_DATA_PREFIX = "X-XqReplaceQX-X-";
    private static final Log log = LogFactory.getLog(EntityBatchHandler.class);
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
            EntityEncodingManager entityEncodingManager) {
        super();
        this.entityBrokerManager = entityBrokerManager;
        this.entityEncodingManager = entityEncodingManager;
    }

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityEncodingManager entityEncodingManager;
    public void setEntityEncodingManager(EntityEncodingManager entityEncodingManager) {
        this.entityEncodingManager = entityEncodingManager;
    }

    /**
     * Can only set this after the class is constructed since it forms a circular dependency
     */
    private EntityHandlerImpl entityRequestHandler;
    public void setEntityRequestHandler(EntityHandlerImpl entityRequestHandler) {
        this.entityRequestHandler = entityRequestHandler;
    }

    // allow the servlet name to be more flexible
    private String servletContext;
    private String getServletContext() {
        if (this.servletContext == null) {
            setServletContext(null); // set defaults
        }
        return this.servletContext;
    }

    public void setServletContext(String servletContext) {
        if (servletContext == null) {
            servletContext = RequestUtils.getServletContext(null);
        }
        this.servletContext = servletContext;
        this.servletBatch = servletContext + EntityRequestHandler.SLASH_BATCH;
    }

    private String servletBatch;
    private String getServletBatch() {
        if (this.servletBatch == null) {
            setServletContext(null); // set defaults
        }
        return this.servletBatch;
    }

    /**
     * Handle the batch operations encoded in this view and request
     * @param view the current view
     * @param req the current request
     * @param res the current response
     * @return true if the operation was handled, false if it could not be handled
     */
    public boolean handleBatch(EntityView view, HttpServletRequest req, HttpServletResponse res) {
        // first find out which METHOD we are dealing with
        boolean handled = false;
        // set up the servlet context if this is the first time
        if (this.servletContext == null) {
            setServletContext( RequestUtils.getServletContext(req) );
        }
        // now get to handling stuff
        String method = req.getMethod() == null ? EntityView.Method.GET.name() : req.getMethod().toUpperCase().trim();
        if (EntityView.Method.GET.name().equals(method)) {
            handleBatchGet(view, req, res);
            handled = true;
        } else if (EntityView.Method.HEAD.name().equals(method)) {
            throw new java.lang.RuntimeException("Method not implemented yet");
        } else if (EntityView.Method.DELETE.name().equals(method)) {
            throw new java.lang.RuntimeException("Method not implemented yet");
        } else if (EntityView.Method.PUT.name().equals(method)) {
            throw new java.lang.RuntimeException("Method not implemented yet");
        } else if (EntityView.Method.POST.name().equals(method)) {
            throw new java.lang.RuntimeException("Method not implemented yet");
        } else {
            throw new IllegalArgumentException("Unknown HTTP METHOD ("+method+"), cannot continue processing request: " + view);
        }
        return handled;
    }

    /**
     * Handles batching all get operations
     */
    public void handleBatchGet(EntityView view, HttpServletRequest req, HttpServletResponse res) {
        if (view == null || req == null || res == null) {
            throw new IllegalArgumentException("Could not process batch: invalid arguments, no args can be null (view="+view+",req="+req+",res="+res+")");
        }
        // validate the the refs param
        String[] refs = req.getParameterValues(REFS_PARAM_NAME);
        if (refs == null || refs.length == 0) {
            throw new IllegalArgumentException("refs parameter must be set (e.g. /direct/batch.json?refs=/sites/popular,/sites/newest)");
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
                throw new IllegalStateException("Failure attempting to process the refs ("+presplit+") listing, could not get the final list of refs out by splitting using the separator ("+separator+")");
            }
        }
        if (refs.length <= 0) {
            throw new IllegalArgumentException("refs parameter must be set and there must be at least 1 reference (e.g. /direct/batch.json?refs=/sites/popular,/sites/newest)");
        }
        String format = view.getFormat();

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
            // skip ones that are already done, we do not process twice
            if (processedRefsAndURLs.contains(reference)) {
                continue; // skip
            }
            String servletContext = getServletContext(); // will be the servlet context (e.g. /direct)
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

            // in case there are external ones we will reuse the httpclient
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
                    log.warn(errorMessage);
                    result = new ResponseError(reference, entityURL, errorMessage);
                }

                if (success) {
                    // rebuild the entityURL with the correct extension in there
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
    
                    // skip urls that are already done, we do not process twice
                    if (processedRefsAndURLs.contains(entityURL)) {
                        continue; // skip
                    }
    
                    result = generateInternalResult(reference, entityURL, req, res);
                }

//                // split the URL and query string apart, send URL to parse entity URL, process query string below
//                EntityReference entityReference = entityView.getEntityReference();
//                // check the prefix is valid
//                if (entityProviderManager.getProviderByPrefix(entityReference.prefix) == null) {
//                 }
//                Map<String, String> params = HttpRESTUtils.parseURLintoParams(reference);
//                // now execute the request to get the data
//                if (entityReference.getId() == null) {
//                    // space (collection)
//                } else {
//                }
//                //entityBrokerManager.getEntityData(ref);
            } else {
                // non-EB URL so we have to fire it off using the HttpUtils

                // set the client wrapper with cookies
                if (clientWrapper == null) {
                    clientWrapper = HttpRESTUtils.makeReusableHttpClient(false, 0, req.getCookies());
                }

                if (entityURL.startsWith("/")) {
                    // http client can only deal in complete URLs - e.g. "http://localhost:8080/thing"
                    String serverName = req.getServerName();
                    int serverPort = req.getServerPort();
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

                result = generateExternalResult(reference, entityURL, view.getMethod(), clientWrapper);
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
                // process the content and see if it matches the expected result, if not we have to dump it in escaped
                String content = ((ResponseResult)result).content;
                String dataKey = checkContent(format, content, refKey, dataMap);
                ((ResponseResult)result).setDataKey(dataKey);
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
            throw new EntityException("Invalid request which resulted in no valid references to batch process, original refs=("
                    +ArrayUtils.arrayToString(refs)+")", EntityRequestHandler.SLASH_BATCH, HttpServletResponse.SC_BAD_REQUEST);
        }

        // compile all the responses into encoded data
        String overallData = entityEncodingManager.encodeData(results, format, REFS_PARAM_NAME, null);
        // replace the data unique keys if there are any
        overallData = reintegrateDataContent(format, dataMap, overallData);

        // put response, headers, and code into the http response
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
        // put content into the response
        try {
            res.getWriter().write(overallData);
        } catch (IOException e) {
            throw new RuntimeException("Unable to encode data for overall response: " + e.getMessage(), e);
        }
        // set overall status code
        res.setStatus(overallStatus);
    }


    /**
     * Processing internal (EB) requests
     * @return the result from the request (may be an error)
     */
    private ResponseBase generateInternalResult(String reference, String entityURL, HttpServletRequest req, HttpServletResponse res) {
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
        entityRequest.removeParameter(REFS_PARAM_NAME); // make sure this is not passed along
        entityRequest.setUseRealDispatcher(false); // we do not want to actually have the container handle forwarding
        EntityHttpServletResponse entityResponse = new EntityHttpServletResponse(res);

        boolean redirected = false;
        do {
            try {
                entityRequestHandler.handleEntityAccess(entityRequest, entityResponse, null);
                redirected = false; // assume no redirect
            } catch (Exception e) {
                String errorMessage = "Failure attempting to process reference ("+reference+") for url ("+entityURL+"): " + e.getMessage() + ":" + e;
                log.warn(errorMessage, e);
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
                entityRequest.setPathString(redirectURL);
                entityResponse.reset();
                redirected = true;
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
        if (error == null) {
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
    private ResponseBase generateExternalResult(String reference, String entityURL, String method, HttpClientWrapper clientWrapper) {
        ResponseBase result = null;
        ResponseError error = null;

        boolean guaranteeSSL = false;
        // TODO allow enabling SSL?
        // fire off the request and hope it does not die horribly
        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpRESTUtils.fireRequest(clientWrapper, 
                    entityURL, 
                    HttpRESTUtils.makeMethodFromString(method), 
                    null, null, guaranteeSSL);
        } catch (RuntimeException e) {
            String errorMessage = "Failure attempting to process external URL ("+entityURL+") from reference ("+reference+"): " + e.getMessage() + ":" + e;
            log.warn(errorMessage, e); // TODO remove ,e to reduce error here
            error = new ResponseError(reference, entityURL, errorMessage);
        }

        // create the result object to encode and place into the final response
        if (error == null) {
            result = new ResponseResult(reference, entityURL, httpResponse.getResponseCode(), 
                    httpResponse.getResponseHeaders(), httpResponse.getResponseBody());
        } else {
            result = error;
        }
        return result;
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
     * @return the dataKey which maps to the real content, need replace the key later
     */
    private String checkContent(String format, String content, String refKey,
            HashMap<String, String> dataMap) {
        String dataKey = null;
        if (entityEncodingManager.validateFormat(content, format)) {
            // valid for the current format so insert later instead of merging now
            dataKey = UNIQUE_DATA_PREFIX + refKey;
            dataMap.put(dataKey, content);
        }
        return dataKey;
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
