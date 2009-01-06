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

import javax.servlet.RequestDispatcher;
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
import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;
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
    private static String UNIQUE_DATA_PREFIX = "X-XqReplaceQX-X-";
    private static Log log = LogFactory.getLog(EntityBatchHandler.class);
    private static String DIRECT_BATCH = EntityView.DIRECT_PREFIX + EntityRequestHandler.SLASH_BATCH;
    private static String INTERNAL_SERVER_ERROR_STATUS_STRING = HttpServletResponse.SC_INTERNAL_SERVER_ERROR+"";

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityEncodingManager entityEncodingManager;
    public void setEntityEncodingManager(EntityEncodingManager entityEncodingManager) {
        this.entityEncodingManager = entityEncodingManager;
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

        // loop through all references
        HashSet<String> processedRefsAndURLs = new HashSet<String>(); // holds all refs which we processed in this batch
        HashMap<String, String> dataMap = new HashMap<String, String>(); // the returned content data from each ref
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
            // fix anything that does not start with a slash or http
            String entityURL = reference;
            if (! reference.startsWith("/") 
                    && ! reference.startsWith("http://")) {
                // assume this is an EB entity url without the slash
                entityURL = EntityView.DIRECT_PREFIX + EntityView.SEPARATOR + reference;
            }
            // make sure no one tries to batch a batch
            if (reference.startsWith(EntityRequestHandler.SLASH_BATCH)
                    || reference.startsWith(DIRECT_BATCH)) {
                throw new EntityException("Failure processing batch request, "
                		+ "batch reference ("+reference+") ("+entityURL+") appears to be another "
           				+ "batch URL (contains "+EntityRequestHandler.SLASH_BATCH+"), "
           				+ "failure in batch request: " + view,
                		EntityRequestHandler.SLASH_BATCH, 
                		HttpServletResponse.SC_BAD_REQUEST);
            }

            // object will hold the results of this reference request
            ResponseBase result;
            ResponseError error = null;

            // parse the entityURL, should hopefully not cause a failure
            URLData ud = new URLData(entityURL);

            /*
             * identify the EB direct operations - 
             * this allows us to strip extensions and cleanup direct URLs as needed,
             * possibly also handle these specially later on if desired
             */
            if (EntityView.DIRECT.equals(ud.servletPath)) {
                if (ud.pathInfo == null || "".equals(ud.pathInfo)) {
                    // looks like /direct only and we do not process that
                    continue;
                }
                try {
                    // parse the entityURL to verify it
                    entityBrokerManager.parseReference(ud.pathInfo);
                } catch (IllegalArgumentException e) {
                    String errorMessage = "Failure parsing direct entityURL ("+entityURL+") from reference ("+reference+") from path ("+ud.pathInfo+"): " + e.getMessage() + ":" + e.getCause();
                    log.warn(errorMessage);
                    error = new ResponseError(reference, entityURL, errorMessage);
                }
                // rebuild the entityURL with the correct extension in there
                StringBuilder sb = new StringBuilder();
                sb.append(EntityView.DIRECT_PREFIX);
                sb.append(ud.pathInfoNoExtension);
                sb.append(EntityView.PERIOD);
                sb.append(view.getFormat());
                if (ud.query.length() > 0) {
                    // add on the query string
                    sb.append('?');
                    sb.append(ud.query);
                }
                entityURL = sb.toString();

//                // TODO split the URL and query string apart, send URL to parse entity URL, process query string below
//                EntityReference entityReference = entityView.getEntityReference();
//                // check the prefix is valid
//                if (entityProviderManager.getProviderByPrefix(entityReference.prefix) == null) {
//                 }
//                Map<String, String> params = HttpRESTUtils.parseURLintoParams(reference);
//                // now execute the request to get the data
//                if (entityReference.getId() == null) {
//                    // space (collection)
//                    
//                } else {
//                    
//                }
//                //entityBrokerManager.getEntityData(ref);
            }

            // skip urls that are already done, we do not process twice
            if (processedRefsAndURLs.contains(entityURL)) {
                continue; // skip
            }

            // setup the request and response objects to do the reference request
            RequestDispatcher dispatcher = req.getRequestDispatcher(entityURL);
            EntityHttpServletRequest entityRequest = new EntityHttpServletRequest(req, entityURL);
            entityRequest.removeParameter(REFS_PARAM_NAME); // make sure this is not passed along
            EntityHttpServletResponse entityResponse = new EntityHttpServletResponse(res);

            // fire off the URLs to the server and get back responses
            try {
                // need to forward instead of include to get headers back
                dispatcher.forward(entityRequest, entityResponse);
            } catch (Exception e) {
                String errorMessage = "Failure attempting to process reference ("+reference+"): " + e.getMessage() + ":" + e;
                log.warn(errorMessage);
                error = new ResponseError(reference, entityURL, errorMessage);
            }
            // create the object to encode and place into the final response
            if (error == null) {
                // all ok, create the result for the response object
                // all cookies go into the main response
                Cookie[] cookies = entityResponse.getCookies();
                for (Cookie cookie : cookies) {
                    res.addCookie(cookie);
                }
                // status codes are compiled
                int status = entityResponse.getStatus();
                if (status >= 200 && status < 300) {
                    successOverall = true;
                }
                // create the result
                // process the content and see if it matches the expected result, if not we have to dump it in escaped
                String content = entityResponse.getContentAsString();
                if (entityEncodingManager.validateFormat(content, view.getFormat())) {
                    // valid for the current format so insert later instead of merging now
                    String uniqueKey = UNIQUE_DATA_PREFIX + refKey;
                    dataMap.put(uniqueKey, content);
                    content = uniqueKey;
                }
                result = new ResponseResult(reference, entityURL, status, entityResponse.getHeaders(), content);
            } else {
                // failure, keep going though
                result = error;
                successOverall = false;
                failure = true;
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
        String overallData = entityEncodingManager.encodeData(results, view.getFormat(), REFS_PARAM_NAME, null);
        // replace the data unique keys if there are any
        for (Entry<String, String> entry : dataMap.entrySet()) {
            if (entry.getKey() != null && ! "".equals(entry.getKey())) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (Formats.XML.equals(view.getFormat())) {
                    value = "\n" + value; // add in a break
                } else if (Formats.JSON.equals(view.getFormat())) {
                    key = '"' + key + '"'; // have to also replace the quotes
                }
                overallData = overallData.replace(key, value);
            }
        }
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
        }
    }

    /**
     * Holds the results from a successful response request
     */
    public static class ResponseResult extends ResponseBase {
        public int status;
        public int getStatus() {
            return status;
        }
        public Map<String, String[]> headers;
        public String data;
        public ResponseResult(String reference, String entityURL, int status, Map<String, String[]> headers, String data) {
            this.reference = reference;
            this.entityURL = entityURL;
            this.status = status;
            this.headers = headers;
            this.data = data;
            this.failure = false;
        }
    }

}
