/**
 * $Id$
 * $URL$
 * Example.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.entitybroker.util.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;

/**
 * This is the core abstract DirectServlet class which is meant to extended,
 * extend this to plugin whatever system you have for initiating/retrieving the EB services
 * and for handling logins <br/>
 * <br/>
 * Direct servlet allows unfettered access to entity URLs within the EB system, it also can handle
 * authentication (login) if required (without breaking an entity URL)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@Slf4j
public abstract class DirectServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    protected transient EntityRequestHandler entityRequestHandler;
    public void setEntityRequestHandler(EntityRequestHandler entityRequestHandler) {
        this.entityRequestHandler = entityRequestHandler;
    }


    /**
     * This runs on servlet initialization (it is the first thing to run)
     * This must be implemented to provide the entity request handler itself <br/>
     * This needs to at least load up the entityRequestHandler service (from the entity broker core)
     * in order for things to work, if that service is not loaded then nothing will work,
     * this can be done using a service manager of some kind or by loading it up manually,
     * the default is to create and load it manually by calling this method with the service <br/>
     * Can also call the servlet constructor to set this value but that is really for testing
     * 
     * @return the entity request handler service object
     */
    protected abstract EntityRequestHandler initializeEntityRequestHandler();

    /**
     * This should return the userId for the currently logged in user if there is one,
     * if not user is logged in then this should return null
     * @return the user identifier for the currently logged in user
     */
    protected abstract String getCurrentLoggedInUserId();

    /**
     * Handle the user authentication (login) in a system specific way,
     * by default this will simply fail as entity-broker has no way to authenticate
     * 
     * @param req the http request (from the client)
     * @param res the http response (back to the client)
     * @param path current request path, set ONLY if we want this to be where to redirect the user after a successful login
     */
    protected abstract void handleUserLogin(HttpServletRequest req, HttpServletResponse res, String path);


    /**
     * Default constructor
     */
    public DirectServlet() {
    }

    /**
     * Allow the request handler to be set on this servlet,
     * probably best to only use this for testing
     * @param entityRequestHandler the entity request handler service object
     */
    public DirectServlet(EntityRequestHandler entityRequestHandler) {
        this.entityRequestHandler = entityRequestHandler;
    }

    @Override
    public void init() throws ServletException {
        super.init();
        initialize();
    }

    /**
     * Initializes the servlet, executed automatically by the servlet container<br/>
     * This basically just calls the {@link #initializeEntityRequestHandler()} method and throws exceptions
     * if something is missing
     */
    protected void initialize() {
        if (entityRequestHandler == null) {
            // call the entity request handler method
            entityRequestHandler = initializeEntityRequestHandler();
        }
        if (entityRequestHandler == null) {
            throw new IllegalStateException("FAILURE to get the handler during init of the direct servlet");
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // we send all the incoming requests to the handler
        // force all response encoding to UTF-8 / html by default
        res.setContentType(Formats.HTML_MIME_TYPE);
        res.setCharacterEncoding(Formats.UTF_8);
        // now handle the request
        handleRequest(req, res);
    }

    /**
     * Handle the incoming request (get and post handled in the same way), passes control to the
     * dispatch method or calls the login helper, override this to control it
     * 
     * @param req
     *           (from the client)
     * @param res
     *           (back to the client)
     * @throws ServletException
     * @throws IOException
     */
    protected void handleRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // catch the login helper posts
        // Note that with wrapped requests, URLUtils.getSafePathInfo may return null
        // so we use the request URI
        String uri = req.getRequestURI();
        if ( uri != null ) {
            String[] parts = uri.split("/");
            if ((parts.length > 0) && ("login".equals(parts[parts.length-1]))) {
                handleUserLogin(req, res, null);
            } else {
                dispatch(req, res);
            }
        }
    }

    /**
     * handle all communication from the user not related to login
     * 
     * @param req
     *           (from the client)
     * @param res
     *           (back to the client)
     * @throws ServletException
     */
    public void dispatch(HttpServletRequest req, HttpServletResponse res) throws ServletException {
        // get the path info
        String path = req.getPathInfo();
        if (path == null) {
            path = "";
        }

        // this cannot work because the original request data is lost
        //      // check for the originalMethod and store it in an attribute
        //      if (req.getParameter(ORIGINAL_METHOD) != null) {
        //         req.setAttribute(ORIGINAL_METHOD, req.getParameter(ORIGINAL_METHOD));
        //      }

        // just handle the request if possible or pass along the failure codes so it can be understood
        try {
            try {
                entityRequestHandler.handleEntityAccess(req, res, path);
            } catch (EntityException e) {
                log.info("Could not process entity: "+e.entityReference+" ("+e.responseCode+")["+e.getCause()+"]: "+e.getMessage());
                // no longer catching FORBIDDEN or UNAUTHORIZED here
                //            if (e.responseCode == HttpServletResponse.SC_UNAUTHORIZED ||
                //                  e.responseCode == HttpServletResponse.SC_FORBIDDEN) {
                //               throw new SecurityException(e.getMessage(), e);
                //            }
                sendError(res, e.responseCode, e.getMessage());
            }
        } catch (SecurityException e) {
            // the end user does not have permission - offer a login if there is no user id yet
            // established,  if not permitted, and the user is the anon user, let them login
            if (getCurrentLoggedInUserId() == null) {
                //                log.debug("Attempted to access an entity URL path (" + path
                //                        + ") for a resource which requires authentication without a session", e);
                //            // store the original request type and query string, this is needed because the method gets lost when Sakai handles the login
                //            path = path + (req.getQueryString() == null ? "?" : "?"+req.getQueryString()) + ORIGINAL_METHOD + "=" + req.getMethod();
                path = path + (req.getQueryString() == null ? "" : "?"+req.getQueryString()); // preserve the query string
                handleUserLogin(req, res, path);
                return;
            }
            // otherwise reject the request
            String msg = "Security exception accessing entity URL: " + path + " (current user not allowed): " + e.getMessage();
            log.info(msg);
            sendError(res, HttpServletResponse.SC_FORBIDDEN, msg);
        } catch (Exception e) {
            // all other cases
            String msg = entityRequestHandler.handleEntityError(req, e);
            log.warn("{} :{}", msg, e);
            sendError(res, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        }

    }

    /**
     * handles sending back servlet errors to the client,
     * feel free to override this if you like to handle errors in a particular way
     * 
     * @param res the http servlet response (back to the client)
     * @param code servlet error response code
     * @param message extra info about the error
     */
    protected void sendError(HttpServletResponse res, int code, String message) {
        try {
            res.reset();
            res.sendError(code, message);
        } catch (Exception e) {
            log.warn("Error sending http servlet error code ({}) and message ({}): {}", code, message, e);
        }
    }

}
