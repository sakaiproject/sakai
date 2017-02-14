/**
 * $Id$
 * $URL$
 * EntityRedirectsManager.java - entity-broker - Jul 26, 2008 9:58:00 AM - azeckoski
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

package org.sakaiproject.entitybroker.rest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.entitybroker.EntityBrokerManager;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectControllable;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.URLRedirect;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.ProcessedTemplate;

import lombok.extern.slf4j.Slf4j;


/**
 * Handles everything related the URL redirects handling and processing
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class EntityRedirectsManager {

    /**
     * Empty constructor
     */
    protected EntityRedirectsManager() { }

    /**
     * Base constructor
     * @param entityProviderMethodStore the provider method store service
     * @param requestStorage the request storage service
     */
    public EntityRedirectsManager(EntityBrokerManager entityBrokerManager, EntityProviderMethodStore entityProviderMethodStore, RequestStorageWrite requestStorage) {
        this.entityBrokerManager = entityBrokerManager;
        this.entityProviderMethodStore = entityProviderMethodStore;
        this.requestStorage = requestStorage;
    }

    private EntityBrokerManager entityBrokerManager;
    public void setEntityBrokerManager(EntityBrokerManager entityBrokerManager) {
        this.entityBrokerManager = entityBrokerManager;
    }

    private EntityProviderMethodStore entityProviderMethodStore;
    public void setEntityProviderMethodStore(EntityProviderMethodStore entityProviderMethodStore) {
        this.entityProviderMethodStore = entityProviderMethodStore;
    }

    private RequestStorageWrite requestStorage;
    public void setRequestStorage(RequestStorageWrite requestStorage) {
        this.requestStorage = requestStorage;
    }

    private String getServletContext() {
        return this.entityBrokerManager.getServletContext();
    }

    /**
     * Do a check to see if the current incoming URL is a match to any of the redirect templates for
     * the given entity provider
     * @param entityProvider an entity provider
     * @param queryString the query string (e.g. auto=true) for the incoming URL
     * @param incomingUrl the incoming URL to try to match
     * @return the URL to redirect to (will start with /direct if it should be forwarded) OR null if no matches were found
     */
    public String checkForTemplateMatch(EntityProvider entityProvider, String incomingURL, String queryString) {
        String prefix = entityProvider.getEntityPrefix();
        String targetURL = null;
        List<URLRedirect> redirects = entityProviderMethodStore.getURLRedirects(prefix);
        if (redirects.size() > 0) {
            List<PreProcessedTemplate> preprocessed = new ArrayList<PreProcessedTemplate>();
            for (URLRedirect redirect : redirects) {
                preprocessed.add( redirect.preProcessedTemplate );
            }
            ProcessedTemplate processedTemplate;
            try {
                processedTemplate = TemplateParseUtil.parseTemplate(incomingURL, preprocessed);
            } catch (IllegalArgumentException e) {
                processedTemplate = null;
            }
            if (processedTemplate != null) {
                URLRedirect redirect = null;
                for (URLRedirect urlRedirect : redirects) {
                    if (processedTemplate.template.equals(urlRedirect.template)) {
                        // found the matching urlRedirect
                        redirect = urlRedirect;
                        break;
                    }
                }

                if (redirect == null) {
                    // TODO should this be a warning instead?
                    throw new IllegalStateException("Failed to find a matching redirect for the matched template ("+processedTemplate.template+") for the incoming URL ("+incomingURL+")");
                } else {
                    // handle the redirect
                    Map<String, String> segmentValues = new HashMap<String, String>( processedTemplate.segmentValues );
                    // add in the prefix and the extension so they can be referenced as variables
                    segmentValues.put(TemplateParseUtil.PREFIX, prefix);
                    if (processedTemplate.extension == null || "".equals(processedTemplate.extension)) {
                        segmentValues.put(TemplateParseUtil.EXTENSION, "");
                        segmentValues.put(TemplateParseUtil.DOT_EXTENSION, "");
                    } else {
                        segmentValues.put(TemplateParseUtil.EXTENSION, processedTemplate.extension);
                        segmentValues.put(TemplateParseUtil.DOT_EXTENSION, TemplateParseUtil.PERIOD + processedTemplate.extension);
                    }
                    if (queryString != null && queryString.length() > 2) {
                        segmentValues.put(TemplateParseUtil.QUERY_STRING, queryString);
                        segmentValues.put(TemplateParseUtil.QUESTION_QUERY_STRING, '?' + queryString);
                    } else {
                        segmentValues.put(TemplateParseUtil.QUERY_STRING, "");
                        segmentValues.put(TemplateParseUtil.QUESTION_QUERY_STRING, "");
                    }
                    // add these to the request vars
                    for (Entry<String, String> entry : segmentValues.entrySet()) {
                        try {
                            requestStorage.setStoredValue(entry.getKey(), entry.getValue());
                        } catch (IllegalArgumentException e) {
                            log.warn("EntityRedirectsManager: Had to skip key (" + entry.getKey() + ") while adding keys to request storage: " + e);
                        }
                    }
                    // do the redirect
                    if (redirect.controllable) {
                        // call the executable
                        if (RedirectControllable.class.isAssignableFrom(entityProvider.getClass())) {
                            targetURL = ((RedirectControllable)entityProvider).handleRedirects(processedTemplate.template, 
                                    incomingURL, 
                                    processedTemplate.variableNames.toArray(new String[processedTemplate.variableNames.size()]), 
                                    segmentValues);
                        } else {
                            throw new IllegalStateException("Invalid URL Redirect Object, marked as controllable when this entity broker does not have the capability: " + RedirectControllable.class);
                        }
                    } else if (redirect.methodName != null) {
                        // call the redirect method
                        Object result = null;
                        Method method = redirect.getMethod();
                        if (method == null) {
                            try {
                                // Note: this is really expensive, need to cache the Method lookup
                                method = entityProvider.getClass().getMethod(redirect.methodName, redirect.methodArgTypes);
                            } catch (SecurityException e1) {
                                throw new RuntimeException("Fatal error trying to get URL redirect method: " + redirect, e1);
                            } catch (NoSuchMethodException e1) {
                                throw new RuntimeException("Fatal error trying to get URL redirect method: " + redirect, e1);
                            }
                            redirect.setMethod(method); // cache this method lookup
                        }
                        Object[] args = new Object[redirect.methodArgTypes.length];
                        for (int i = 0; i < redirect.methodArgTypes.length; i++) {
                            Class<?> argType = redirect.methodArgTypes[i];
                            if (String.class.equals(argType)) {
                                args[i] = incomingURL;
                            } else if (String[].class.equals(argType)) {
                                args[i] = processedTemplate.variableNames.toArray(new String[processedTemplate.variableNames.size()]);
                            } else if (Map.class.equals(argType)) {
                                args[i] = segmentValues;
                            } else {
                                throw new IllegalStateException("URL redirect method ("+redirect+") contains an invalid methodArgTypes, " +
                                "only valid types allowed: String, String[], Map");
                            }
                        }
                        try {
                            result = method.invoke(entityProvider, args);
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Fatal error trying to execute URL redirect method: " + redirect, e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Fatal error trying to execute URL redirect method: " + redirect, e);
                        } catch (InvocationTargetException e) {
                            String reference = incomingURL;
                            if (e.getCause() != null) {
                                if (e.getCause().getClass().isAssignableFrom(IllegalArgumentException.class)) {
                                    throw new IllegalArgumentException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                                } else if (e.getCause().getClass().isAssignableFrom(EntityNotFoundException.class)) {
                                    throw new EntityNotFoundException(e.getCause().getMessage() + " (rethrown)", reference, e.getCause());
                                } else if (e.getCause().getClass().isAssignableFrom(FormatUnsupportedException.class)) {
                                    String format = ((FormatUnsupportedException)e.getCause()).format;
                                    throw new FormatUnsupportedException(e.getCause().getMessage() + " (rethrown)", e.getCause(), reference, format);
                                } else if (e.getCause().getClass().isAssignableFrom(UnsupportedOperationException.class)) {
                                    throw new UnsupportedOperationException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                                } else if (e.getCause().getClass().isAssignableFrom(EntityException.class)) {
                                    int code = ((EntityException)e.getCause()).responseCode;
                                    throw new EntityException(e.getCause().getMessage() + " (rethrown)", reference, code);
                                } else if (e.getCause().getClass().isAssignableFrom(IllegalStateException.class)) {
                                    throw new IllegalStateException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                                } else if (e.getCause().getClass().isAssignableFrom(SecurityException.class)) {
                                    throw new SecurityException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                                }
                            }
                            throw new RuntimeException("Fatal error trying to execute URL redirect method: " + redirect, e);
                        }
                        if (result != null) {
                            targetURL = result.toString();
                        } else {
                            targetURL = null;
                        }
                    } else if (redirect.outgoingTemplate != null) {
                        // handle the straight processing
                        try {
                            targetURL = TemplateParseUtil.mergeTemplate(redirect.outgoingTemplate, segmentValues);
                        } catch (IllegalArgumentException e) {
                            targetURL = null;
                            log.warn("EntityRedirectsManager: Unable to merge target template ("+redirect.outgoingTemplate+") with available variables: " + e);
                        }
                    } else {
                        // should never get here
                        throw new IllegalStateException("Invalid URL Redirect Object, could not determine operation: " + redirect);
                    }
                }
            }
        }
        if (targetURL != null && targetURL.length() > 0) {
            // fix up the outgoing URL if needed (must end up non-relative)
            if (targetURL.charAt(0) == TemplateParseUtil.SEPARATOR 
                    || targetURL.startsWith("http:") || targetURL.startsWith("https:")) {
                // leave it as is
            } else {
                // append the servlet path stuff so we know to forward this
                targetURL = getServletContext() + TemplateParseUtil.SEPARATOR + targetURL;
            }
        }
        return targetURL;
    }

}
