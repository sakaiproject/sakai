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
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.azeckoski.reflectutils.map.ArrayOrderedMap;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletRequest;
import org.sakaiproject.entitybroker.util.http.EntityHttpServletResponse;


/**
 * This handles batch operations internally as much as possible,
 * the idea is to provide for a standard way to reduce huge numbers of calls down to 1 call to the server
 * which puts the data together into a single response
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityBatchHandler {

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
        // validate the the refs param
        String[] refs = req.getParameterValues("refs");
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
        Map<String, Object> results = new ArrayOrderedMap<String, Object>();
        boolean successOverall = false; // true if all ok or partial ok, false if exception occurs or all fail
        boolean failure = false;
        for (int i = 0; i < refs.length; i++) {
            String reference = refs[i];
            if (reference == null || "".equals(reference)) {
                continue; // skip
            }
            if (results.containsKey(reference)) {
                continue; // skip, already done, we do not process twice
            }
            // object will hold the results of this reference request
            Object result;
//            // identify the EB operations
//            EntityReference entityReference = null;
//            if (reference.startsWith(EntityView.DIRECT_PREFIX)) {
//                int loc = reference.indexOf("/", 5);
//                if (loc == -1) {
//                    continue; // skip
//                }
//                reference = reference.substring(loc);
//                // TODO split the URL and query string apart, send URL to parse entity URL, process query string below
//                entityReference = entityBrokerManager.parseReference(reference); //parseEntityURL(reference);
//                // check the prefix is valid
//                Map<String, String> params = HttpRESTUtils.parseURLintoParams(reference);
//                // now execute the request to get the data
//                if (entityReference.getId() == null) {
//                    // space (collection)
//                    
//                } else {
//                    
//                }
//                //entityBrokerManager.getEntityData(ref);
//            }
//            // compile EB responses

            // setup the request and response objects to do the reference request
            RequestDispatcher dispatcher = req.getRequestDispatcher(reference);
            EntityHttpServletRequest entityRequest = new EntityHttpServletRequest(req, reference);
            EntityHttpServletResponse entityResponse = new EntityHttpServletResponse(res);
            // fire off the URLs to the server and get back responses
            ResponseError error = null;
            try {
                // need to forward instead of include to get headers back
                dispatcher.forward(entityRequest, entityResponse);
            } catch (Exception e) {
                String errorMessage = "Failure attempting to process reference ("+reference+"): " + e.getMessage() + ":" + e.getCause();
                error = new ResponseError(reference, errorMessage);
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
                result = new ResponseResult(reference, status, entityResponse.getHeaders(), entityResponse.getContentAsString());
            } else {
                // failure, keep going though
                result = error;
                successOverall = false;
                failure = true;
            }
            results.put(reference, result);
        }
        // determine overall status
        int overallStatus = HttpServletResponse.SC_OK;
        if (failure == true || successOverall == false) {
            overallStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        // compile all the responses into encoded data
        String overallData = entityEncodingManager.encodeData(results, view.getFormat(), "ref", null);
        try {
            res.getWriter().write(overallData);
        } catch (IOException e) {
            throw new RuntimeException("Unable to encode data for overall response: " + e.getMessage(), e);
        }
        // put response, headers, and code into the http response
        res.setStatus(overallStatus);
    }

    /**
     * Holds the error values which will be encoded by the various EB utils
     */
    public static class ResponseError {
        public String reference;
        public String error;
        public ResponseError(String reference, String errorMessage) {
            this.reference = reference;
            this.error = errorMessage;
        }
    }

    /**
     * Holds the results from a successful response request
     */
    public static class ResponseResult {
        public String reference;
        public int status;
        public Map<String, String[]> headers;
        public String data;
        public ResponseResult(String reference, int status, Map<String, String[]> headers, String data) {
            this.reference = reference;
            this.status = status;
            this.headers = headers;
            this.data = data;
        }
    }

}
