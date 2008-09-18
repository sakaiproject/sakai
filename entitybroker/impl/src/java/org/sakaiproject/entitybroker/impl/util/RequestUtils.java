/**
 * $Id$
 * $URL$
 * RequestUtils.java - entity-broker - Jul 28, 2008 7:41:28 AM - azeckoski
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

package org.sakaiproject.entitybroker.impl.util;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityRequestHandler;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;


/**
 * Contains a set of static utility methods for working with requests
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class RequestUtils {

    private static final String DIVIDER = "||";
    private static final String ENTITY_REDIRECT_CHECK = "_entityRedirectCheck";
    private static Log log = LogFactory.getLog(RequestUtils.class);

    /**
     * Handles the redirect to a URL from the current location,
     * the URL should be relative for a forward, otherwise it will be a redirect <br/>
     * NOTE: You should perform no actions after call this method,
     * you should simply pass control back to the handler
     * @param redirectURL the URL to redirect to (relative or absolute)
     * @param forward if false, use redirect (this should be the default), 
     * if true use forward, note that we can only forward from your webapp back to your servlets and
     * a check will be performed to see if this is the case, if it is not
     * (anything with a "http", a non-matching prefix, and anything with a query string) will be switched to redirect automatically
     * @param req the current request
     * @param res the current response
     * @throws IllegalArgumentException is the params are invalid
     */
    public static void handleURLRedirect(String redirectURL, boolean forward, HttpServletRequest req, HttpServletResponse res) {
        if (redirectURL == null || "".equals(redirectURL)) {
            throw new IllegalArgumentException("The redirect URL must be set and cannot be null");
        }
        if (req == null || res == null) {
            throw new IllegalArgumentException("The request and response must be set and cannot be null");         
        }
        if (redirectURL.startsWith("http:") || redirectURL.startsWith("https:") 
                || RequestUtils.containsQueryString(redirectURL)) {
            forward = false;
        } else {
            // we allow forwarding ONLY if the current webapp path matches the redirect path
            String webapp = req.getContextPath();
            if (webapp != null && webapp.length() > 0) {
                if (redirectURL.startsWith(webapp + "/")) {
                    redirectURL = redirectURL.substring(webapp.length());
                    forward = true;
                } else if (redirectURL.length() > 1 
                        && redirectURL.startsWith(webapp.substring(1) + "/")) {
                    redirectURL = redirectURL.substring(webapp.length() - 1);
                    forward = true;
                } else {
                    forward = false;
                }
            }
        }

        if (forward) {
            // check for infinite forwarding
            String curRedirect = DIVIDER + redirectURL + DIVIDER;
            if (req.getAttribute(ENTITY_REDIRECT_CHECK) != null) {
                String redirectCheck = (String) req.getAttribute(ENTITY_REDIRECT_CHECK);
                if (redirectCheck.contains(curRedirect)) {
                    throw new IllegalStateException("Infinite forwarding loop detected with attempted redirect to ("+redirectURL+"), path to failure: " 
                            + redirectCheck.replace(DIVIDER+DIVIDER, " => ").replace(DIVIDER, "") + " => " + redirectURL);
                }
                redirectCheck += curRedirect;
                req.setAttribute(ENTITY_REDIRECT_CHECK, redirectCheck);
            } else {
                req.setAttribute(ENTITY_REDIRECT_CHECK, curRedirect);
            }

            RequestDispatcher rd = req.getRequestDispatcher(redirectURL);
            try {
                rd.forward(req, res);
            } catch (ServletException e) {
                throw new RuntimeException("Failure with servlet while forwarding to '"+redirectURL+"': " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RuntimeException("Failure with encoding while forwarding to '"+redirectURL+"': " + e.getMessage(), e);
            }
        } else {
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            try {
                res.sendRedirect(redirectURL);
            } catch (IOException e) {
                throw new RuntimeException("Failure with encoding while redirecting to '"+redirectURL+"': " + e.getMessage(), e);
            }
        }
    }

    /**
     * Simple check to see if a URL appears to contain a query string,
     * true if it does, false otherwise
     */
    private static boolean containsQueryString(String URL) {
        int lastEquals = URL.lastIndexOf('=');
        int qMark = URL.indexOf('?');
        if (lastEquals > 0 && qMark > 0 && lastEquals > qMark) {
            return true;
        }
        return false;
    }

    /**
     * Gets the correct info out of the request method and places it into the entity view and
     * identifies if this is an output (read) or input (write) request
     * @param req the request
     * @param view the entity view to update
     * @return true if output request OR false if input request
     * @throws EntityException if the request has problems
     */
    public static boolean isRequestOutput(HttpServletRequest req, EntityView view) {
        boolean output = false;
        String method = req.getMethod() == null ? EntityView.Method.GET.name() : req.getMethod().toUpperCase().trim();
        if (EntityView.Method.GET.name().equals(method)) {
            view.setMethod(EntityView.Method.GET);
            output = true;
        } else if (EntityView.Method.HEAD.name().equals(method)) {
            view.setMethod(EntityView.Method.HEAD);
            output = true;
        } else {
            // identify the action based on the method type or "_method" attribute
            if (EntityView.Method.DELETE.name().equals(method)) {
                view.setViewKey(EntityView.VIEW_DELETE);
                view.setMethod(EntityView.Method.DELETE);
            } else if (EntityView.Method.PUT.name().equals(method)) {
                view.setViewKey(EntityView.VIEW_EDIT);
                view.setMethod(EntityView.Method.PUT);
            } else if (EntityView.Method.POST.name().equals(method)) {
                String _method = req.getParameter(EntityRequestHandler.COMPENSATE_METHOD);
                if (_method == null) {
                    if (view.getEntityReference().getId() == null) {
                        // this better be a create request or list post
                        view.setViewKey(EntityView.VIEW_NEW);
                    } else {
                        // this could be an edit
                        view.setViewKey(EntityView.VIEW_EDIT);
                    }
                } else {
                    _method = _method.toUpperCase().trim();
                    if (EntityView.Method.DELETE.name().equals(_method)) {
                        view.setViewKey(EntityView.VIEW_DELETE);
                    } else if (EntityView.Method.PUT.equals(_method)) {
                        if (view.getEntityReference().getId() == null) {
                            // this should be a modification of a list
                            view.setViewKey(EntityView.VIEW_NEW);
                        } else {
                            // this better be an edit of an entity
                            view.setViewKey(EntityView.VIEW_EDIT);
                        }
                    } else {
                        throw new EntityException("Unable to handle POST request with _method, unknown method (only PUT/DELETE allowed): " + _method, 
                                view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);                        
                    }
                }
                view.setMethod(EntityView.Method.POST);
            } else {
                throw new EntityException("Unable to handle request method, unknown method (only GET/POST/PUT/DELETE allowed): " + method, 
                        view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
            }

            // check that the request is valid (delete requires an entity id)
            if ( EntityView.VIEW_DELETE.equals(view.getViewKey()) 
                    && view.getEntityReference().getId() == null) {
                throw new EntityException("Unable to handle entity ("+view.getEntityReference()+") delete request without entity id, url=" 
                        + view.getOriginalEntityUrl(), 
                        view.getEntityReference()+"", HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return output;
    }

    // put the keys which should be ignored in this array which will be placed in a set and ignored
    public static String[] ignoreForSearch = new String[] {
        EntityRequestHandler.COMPENSATE_METHOD,
        "queryString", 
        "pathInfo", 
        "method", 
        RequestStorage.ReservedKeys._locale.name(),
        RequestStorage.ReservedKeys._requestActive.name(),
        RequestStorage.ReservedKeys._requestEntityReference.name(),
        RequestStorage.ReservedKeys._requestOrigin.name(),
        "entity-format"
    };
    private static HashSet<String> ignoreSet = null;
    private static HashSet<String> getIgnoreSet() {
        if (ignoreSet == null) {
            // load the array into a set for easier and faster checks
            synchronized (ignoreSet) {
                ignoreSet = new HashSet<String>();
                for (int i = 0; i < ignoreForSearch.length; i++) {
                    ignoreSet.add(ignoreForSearch[i]);
                }
            }
        }
        return ignoreSet;
    }

    /**
     * This looks at search parameters and returns anything it finds in the
     * request parameters that can be put into the search,
     * supports the page params
     * 
     * @param req a servlet request
     * @return a search filter object
     */
    public static Search makeSearchFromRequestStorage(RequestStorageImpl requestStorage) {
        Search search = new Search();
        int page = -1;
        int limit = -1;
        try {
            if (requestStorage != null) {
                Map<String, Object> params = requestStorage.getStorageMapCopy(true, false, true, true); // leave out headers
                if (params != null) {
                    for (Entry<String, Object> entry : params.entrySet()) {
                        String key = entry.getKey();
                        // filter out certain keys
                        if (getIgnoreSet().contains(key)) {
                            continue; // skip this key
                        }
                        Object value = entry.getValue();
                        if (value == null) {
                            // in theory this should not happen
                            continue;
                        } else if (value.getClass().isArray()) {
                            // use the value as is
                        } else {
                            // get paging values out if possible
                            if ("_limit".equals(key) 
                                    || "_perpage".equals(key)
                                    || ":perpage".equals(key)) {
                                try {
                                    limit = Integer.valueOf(value.toString()).intValue();
                                    search.setLimit(limit);
                                } catch (NumberFormatException e) {
                                    log.warn("Invalid non-number passed in for _limit/_perpage param: " + value, e);
                                }
                                continue;
                            } else if ("_start".equals(key)) {
                                try {
                                    int start = Integer.valueOf(value.toString()).intValue();
                                    search.setStart(start);
                                } catch (NumberFormatException e) {
                                    log.warn("Invalid non-number passed in for '_start' param: " + value, e);
                                }
                                continue;
                            } else if ("_page".equals(key)
                                    || ":page".equals(key)) {
                                try {
                                    page = Integer.valueOf(value.toString()).intValue();
                                } catch (NumberFormatException e) {
                                    log.warn("Invalid non-number passed in for '_page' param: " + value, e);
                                }
                                continue;
                            }
                        }
                        search.addRestriction( new Restriction(key, value) );
                    }
                }
            }
        } catch (Exception e) {
            // failed to translate the request to a search, not really much to do here
            log.warn("Could not translate entity request into search params: " + e.getMessage(), e);
        }
        // translate page into start/limit
        if (page > 0) {
            if (limit <= -1) {
                limit = 10; // set to a default value
                search.setLimit(limit);
                log.warn("page is set without a limit per page, setting per page limit to default value of 10");
            }
            search.setStart( (page-1) * limit );
        }
        return search;
    }

    /**
     * This will set the response mime type correctly based on the format constant,
     * also sets the response encoding to UTF_8
     * @param format the format constant, example {@link Formats#XML}
     * @param res the current outgoing response
     */
    public static void setResponseEncoding(String format, HttpServletResponse res) {
        String encoding;
        if (Formats.XML.equals(format)) {
            encoding = Formats.XML_MIME_TYPE;
        } else if (Formats.HTML.equals(format)) {
            encoding = Formats.HTML_MIME_TYPE;
        } else if (Formats.JSON.equals(format)) {
            encoding = Formats.JSON_MIME_TYPE;
        } else if (Formats.RSS.equals(format)) {
            encoding = Formats.RSS_MIME_TYPE;                        
        } else if (Formats.ATOM.equals(format)) {
            encoding = Formats.ATOM_MIME_TYPE;                        
        } else {
            encoding = Formats.TXT_MIME_TYPE;
        }
        res.setContentType(encoding);
        res.setCharacterEncoding(Formats.UTF_8);
    }

}
