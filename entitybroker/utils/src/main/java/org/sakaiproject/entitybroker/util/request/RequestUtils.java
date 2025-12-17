/**
 * $Id$
 * $URL$
 * RequestUtils.java - entity-broker - Jul 28, 2008 7:41:28 AM - azeckoski
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

package org.sakaiproject.entitybroker.util.request;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Order;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.providers.EntityRequestHandler;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * Contains a set of static utility methods for working with requests
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class RequestUtils {

    private static final String DIVIDER = "||";
    private static final String ENTITY_REDIRECT_CHECK = "_entityRedirectCheck";

    /**
     * A map from mimetypes to format constants
     */
    public static Map<String, String> mimeTypeToFormat;
    /**
     * A map from format constants to mimetypes
     */
    public static Map<String, String> formatToMimeType;
    /**
     * A map from extensions to format constants
     */
    public static Map<String, String> extensionsToFormat;

    static {
        mimeTypeToFormat = new LinkedHashMap<String, String>(12);
        mimeTypeToFormat.put(Formats.ATOM_MIME_TYPE, Formats.ATOM);
        mimeTypeToFormat.put(Formats.FORM_MIME_TYPE, Formats.FORM);
        mimeTypeToFormat.put(Formats.HTML_MIME_TYPE, Formats.HTML);
        mimeTypeToFormat.put("application/xhtml+xml", Formats.HTML);
        mimeTypeToFormat.put(Formats.JSON_MIME_TYPE, Formats.JSON);
        mimeTypeToFormat.put("text/json", Formats.JSON); // this is not really valid
        mimeTypeToFormat.put("application/*", Formats.JSON);
        mimeTypeToFormat.put(Formats.RSS_MIME_TYPE, Formats.RSS);
        mimeTypeToFormat.put(Formats.TXT_MIME_TYPE, Formats.TXT);
        mimeTypeToFormat.put("text/*", Formats.TXT);
        mimeTypeToFormat.put(Formats.XML_MIME_TYPE, Formats.XML);
        mimeTypeToFormat.put("text/xml", Formats.XML); // this is not really valid

        formatToMimeType = new LinkedHashMap<String, String>(7);
        formatToMimeType.put(Formats.ATOM, Formats.ATOM_MIME_TYPE);
        formatToMimeType.put(Formats.FORM, Formats.FORM_MIME_TYPE);
        formatToMimeType.put(Formats.HTML, Formats.HTML_MIME_TYPE);
        formatToMimeType.put(Formats.JSON, Formats.JSON_MIME_TYPE);
        formatToMimeType.put(Formats.JSONP, Formats.JSONP_MIME_TYPE);
        formatToMimeType.put(Formats.RSS, Formats.RSS_MIME_TYPE);
        formatToMimeType.put(Formats.TXT, Formats.TXT_MIME_TYPE);
        formatToMimeType.put(Formats.XML, Formats.XML_MIME_TYPE);

        extensionsToFormat = new LinkedHashMap<String, String>(20);
        extractExtensionsIntoMap(Formats.ATOM, Formats.ATOM_EXTENSIONS, extensionsToFormat);
        extractExtensionsIntoMap(Formats.FORM, Formats.FORM_EXTENSIONS, extensionsToFormat);
        extractExtensionsIntoMap(Formats.HTML, Formats.HTML_EXTENSIONS, extensionsToFormat);
        extractExtensionsIntoMap(Formats.JSON, Formats.JSON_EXTENSIONS, extensionsToFormat);
        extractExtensionsIntoMap(Formats.JSONP, Formats.JSONP_EXTENSIONS, extensionsToFormat);
        extractExtensionsIntoMap(Formats.RSS, Formats.RSS_EXTENSIONS, extensionsToFormat);
        extractExtensionsIntoMap(Formats.TXT, Formats.TXT_EXTENSIONS, extensionsToFormat);
        extractExtensionsIntoMap(Formats.XML, Formats.XML_EXTENSIONS, extensionsToFormat);
    }

    /**
     * 
     */
    private static void extractExtensionsIntoMap(String format, String[] extensions, Map<String, String> map) {
        for (String extension : extensions) {
            map.put(extension, format);
        }
    }

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
                    } else if (EntityView.Method.PUT.name().equals(_method)) {
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

    /**
     * This method will correctly extract the format constant from a request 
     * (extension first and then Accepts header) and then set it in the response
     * as the correct return type, if none is found then the default will be used
     * @param req the Servlet request
     * @param res the Servlet response
     * @param defaultFormat (OPTIONAL) if this is set then it will be the default format assigned when none can be found,
     * otherwise the default format is {@link Formats#HTML}
     * @return the extracted format (will never be null), e.g {@link Formats#XML}
     */
    @SuppressWarnings("unchecked")
    public static String findAndHandleFormat(HttpServletRequest req, HttpServletResponse res, String defaultFormat) {
        if (defaultFormat == null) {
            defaultFormat = Formats.HTML;
        }
        String path = req.getPathInfo();
        String format = TemplateParseUtil.findExtension(path)[2];
        if (format == null) {
            // try to get it from the Accept header
            for (Enumeration<String> enumHeader = req.getHeaderNames(); enumHeader.hasMoreElements();) {
                String headerName = enumHeader.nextElement();
                if ("accept".equalsIgnoreCase(headerName)) {
                    ArrayList<String> accepts = new ArrayList<String>();
                    for (Enumeration<String> enumAccepts = req.getHeaders(headerName); enumAccepts.hasMoreElements();) {
                        String mimeType = enumAccepts.nextElement();
                        if (mimeType == null) {
                            continue;
                        }
                        mimeType = mimeType.trim();
                        // trim out the optional stuff
                        int pos = mimeType.indexOf(';');
                        if (pos > 0) {
                            mimeType = mimeType.substring(0, pos).trim();
                        }
                        accepts.add( mimeType );
                    }
                    // sort the list to longest first and shortest last
                    Collections.sort(accepts, new ShortestStringLastComparator());
                    for (String mimeType : accepts) {
                        String f = mimeTypeToFormat.get(mimeType);
                        if (f != null) {
                            format = f;
                            break; // FOUND A MIME MATCH
                        }
                    }
                    break; // STOP CHECKING HEADERS
                }
            }
        }
        if (format == null || "".equals(format)) {
            // set the default value
            format = defaultFormat;
        }
        RequestUtils.setResponseEncoding(format, res);
        return format;
    }

    /**
     * Comparator which puts the longest strings first and the shortest last
     */
    public static class ShortestStringLastComparator implements Comparator<String>, Serializable {
        public static final long serialVersionUID = 11L;
        public int compare(String o1, String o2) {
            int compare = 0;
            if (o1 == null && o2 == null) {
                compare = 0;
            } else if (o1 == null) {
                compare = 1;
            } else if (o2 == null) {
                compare = -1;
            } else {
                compare = o2.length() - o1.length();
            }
            return compare;
        }
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
    private static synchronized HashSet<String> getIgnoreSet() {
        if (ignoreSet == null) {
            // load the array into a set for easier and faster checks
            ignoreSet = new HashSet<String>();
            for (int i = 0; i < ignoreForSearch.length; i++) {
                ignoreSet.add(ignoreForSearch[i]);
            }
        }
        return ignoreSet;
    }

    /**
     * This looks at request parameters and returns anything it finds in the
     * request parameters that can be put into the search,
     * supports the page params and sorting params
     * 
     * @param params the request params from a request (do not include headers)
     * @return a search filter object
     */
    public static Search makeSearchFromRequestParams(Map<String, Object> params) {
        Search search = new Search();
        int limit, page, start;
        page = start = 0;
        limit = 10;
        try {
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
                                || "perpage".equals(key)
                                || "count".equals(key)
                                || "itemsPerPage".equals(key)) {
                            try {
                                limit = Integer.valueOf(value.toString());
                            } catch (NumberFormatException e) {
                                log.warn("Invalid non-number passed in for _limit/_perpage param: " + value + ":" + e);
                            }
                            continue;
                        } else if ("_start".equals(key)
                                || "startIndex".equals(key)) {
                            try {
                                start = Integer.valueOf(value.toString());
                            } catch (NumberFormatException e) {
                                log.warn("Invalid non-number passed in for '_start' param: " + value + ":" + e);
                            }
                            continue;
                        } else if ("_page".equals(key)
                                || "page".equals(key)
                                || "startPage".equals(key)) {
                            try {
                                page = Integer.valueOf(value.toString());
                            } catch (NumberFormatException e) {
                                log.warn("Invalid non-number passed in for '_page' param: " + value + ":" + e);
                            }
                            continue;
                        } else if ("_order".equals(key)
                                || "_sort".equals(key)
                                || "sort".equals(key)) {
                            String val = value.toString();
                            String[] sortBy = new String[] {val};
                            if (val.indexOf(',') > 0) {
                                // multiple sort params
                                sortBy = val.split(",");
                            }
                            try {
                                for (String sortItem : sortBy) {
                                    sortItem = StringUtils.trimToEmpty(sortItem);
                                    if (sortItem.endsWith("_reverse")) {
                                        search.addOrder(new Order(sortItem.substring(0, sortItem.length() - 8), false));
                                    } else if (sortItem.endsWith("_desc")) {
                                        search.addOrder(new Order(sortItem.substring(0, sortItem.length() - 5), false));
                                    } else if (sortItem.endsWith("_asc")) {
                                        search.addOrder(new Order(sortItem.substring(0, sortItem.length() - 4)));
                                    } else {
                                        search.addOrder(new Order(sortItem));
                                    }
                                }
                            } catch (RuntimeException e) {
                                log.warn("Failure while getting the sort/order param: {}:{}", val, e.getMessage());
                            }
                            continue;
                        } else if ("_searchTerms".equals(key) 
                                || "searchTerms".equals(key)) {
                            // indicates a space delimited list of search terms
                            String val = value.toString();
                            String[] terms = val.split(" ");
                            search.addRestriction( new Restriction("searchTerms", terms) );
                            continue;
                        }
                    }
                    search.addRestriction( new Restriction(key, value) );
                }
            }
        } catch (Exception e) {
            // failed to translate the request to a search, not really much to do here
            log.warn("Could not translate entity request into search params: " + e.getMessage() + ":" + e);
        }

        // if paging has been specified ignore start
        int end;
        if (page > 0) {
            // translate page into start/end
            start = ((page - 1) * limit);
            end = page * limit;
        } else {
            end = start + limit;
        }
        search.setStart(start);
        search.setLimit(end);

        return search;
    }

    /**
     * This will set the response mime type correctly based on the format constant,
     * also sets the response encoding to UTF_8
     * @param format the format constant, example {@link Formats#XML}
     * @param res the current outgoing response
     */
    public static void setResponseEncoding(String format, HttpServletResponse res) {
        String encoding = Formats.TXT_MIME_TYPE;
        if (format != null) {
            String mimeType = formatToMimeType.get(format);
            if (mimeType != null) {
                encoding = mimeType;
            }
        }
        res.setContentType(encoding);
        res.setCharacterEncoding(Formats.UTF_8);
    }

    /**
     * This finds the correct servlet path or returns the default one,
     * will not return "" or null
     * @param req the incoming request
     * @return the servlet context path (/ + servletName)
     */
    public static String getServletContext(HttpServletRequest req) {
        String context = null;
        if (req != null) {
            context = req.getContextPath();
            if ("".equals(context)) {
                context = req.getServletPath();
            }
        }
        if (context == null || "".equals(context)) {
            context = EntityView.DIRECT_PREFIX;
        }
        return context;
    }

}
