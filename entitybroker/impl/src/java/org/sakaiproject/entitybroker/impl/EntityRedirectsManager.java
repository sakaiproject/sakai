/**
 * $Id$
 * $URL$
 * EntityRedirectsManager.java - entity-broker - Jul 26, 2008 9:58:00 AM - azeckoski
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

package org.sakaiproject.entitybroker.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.azeckoski.reflectutils.ReflectUtils;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectControllable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorageWrite;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.impl.util.URLRedirect;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.ProcessedTemplate;
import org.sakaiproject.entitybroker.util.request.RequestUtils;


/**
 * Handles everything related the URL redirects handling and processing
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityRedirectsManager {

    private static Log log = LogFactory.getLog(EntityRedirectsManager.class);

    private Map<String, List<URLRedirect>> entityRedirects = new ConcurrentHashMap<String, List<URLRedirect>>();

    private RequestStorageWrite requestStorage;
    public void setRequestStorage(RequestStorageWrite requestStorage) {
        this.requestStorage = requestStorage;
    }

    // allow the servlet name to be more flexible
    private String servletContext;
    private String getServletContext() {
        if (this.servletContext == null) {
            return RequestUtils.getServletContext(null);
        }
        return this.servletContext;
    }
    public void setServletContext(String servletContext) {
        if (servletContext != null) {
            this.servletContext = servletContext;
        }
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
        List<URLRedirect> redirects = getURLRedirects(prefix);
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
                            log.warn("Had to skip key (" + entry.getKey() + ") while adding keys to request storage: " + e.getMessage());
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
                                        "only valid types allowed: " + ReflectUtils.arrayToString(validParamTypes));
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
                            log.warn("Unable to merge target template ("+redirect.outgoingTemplate+") with available variables: " + e.getMessage());
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

    /**
     * Looks for redirect methods in the given entity provider
     * @param entityProvider an entity provider
     * @return an array of redirect objects
     * @throws IllegalArgumentException if the methods are setup incorrectly
     */
    public URLRedirect[] findURLRedirectMethods(EntityProvider entityProvider) {
        ArrayList<URLRedirect> redirects = new ArrayList<URLRedirect>();
        Method[] methods = entityProvider.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EntityURLRedirect.class)) {
                EntityURLRedirect eurAnnote = method.getAnnotation(EntityURLRedirect.class);
                String template = eurAnnote.value();
                if (null == template || "".equals(template)) {
                    throw new IllegalArgumentException("there is no template set for the annotation: " + EntityURLRedirect.class);
                }
                URLRedirect redirect = null;
                try {
                    redirect = new URLRedirect(template, method.getName(), validateParamTypes(method.getParameterTypes()));
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("Failed to validate redirect templates from methods for prefix ("
                            +entityProvider.getEntityPrefix() + "): " + e.getMessage(), e);
                }
                redirect.setMethod(method); // cache to reduce lookup cost
                redirects.add(redirect);
            }
        }
        return redirects.toArray(new URLRedirect[redirects.size()]);
    }

    /**
     * Validates the provided URL templates in an entity provider and outputs the
     * URL redirect objects as an array
     * @param configDefinable the entity provider
     * @return the array of URL redirects
     */
    public static URLRedirect[] validateDefineableTemplates(RedirectDefinable configDefinable) {
        List<URLRedirect> redirects = new ArrayList<URLRedirect>();
        TemplateMap[] urlMappings = configDefinable.defineURLMappings();
        if (urlMappings == null || urlMappings.length == 0) {
            // this is ok then, or is it?
            log.warn("RedirectDefinable: no templates defined for url redirect");
        } else {
            for (TemplateMap templateMap : urlMappings) {
                String incomingTemplate = templateMap.getIncomingTemplate();
                String outgoingTemplate = templateMap.getOutgoingTemplate();
                URLRedirect redirect = null;
                try {
                    redirect = new URLRedirect(incomingTemplate, outgoingTemplate);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("Failed to validate defined redirect templates for prefix ("
                            +configDefinable.getEntityPrefix() + "): " + e.getMessage(), e);
                }
                if (incomingTemplate.equals(outgoingTemplate)) {
                    throw new IllegalArgumentException("Invalid outgoing redirect template ("
                            +outgoingTemplate+") for entity prefix ("+configDefinable.getEntityPrefix()
                            +"), template is identical to incoming template ("+incomingTemplate+") and would cause an infinite redirect");
                }
                // make sure that we check the target vars match the template vars
                List<String> incomingVars = new ArrayList<String>( redirect.preProcessedTemplate.variableNames );
                incomingVars.add(TemplateParseUtil.PREFIX);
                incomingVars.add(TemplateParseUtil.EXTENSION);
                incomingVars.add(TemplateParseUtil.DOT_EXTENSION);
                incomingVars.add(TemplateParseUtil.QUERY_STRING);
                incomingVars.add(TemplateParseUtil.QUESTION_QUERY_STRING);
                List<String> outgoingVars = redirect.outgoingPreProcessedTemplate.variableNames;
                if (incomingVars.containsAll(outgoingVars)) {
                    // all is ok
                    redirects.add(redirect);
                } else {
                    throw new IllegalArgumentException("Outgoing template ("+outgoingTemplate+") has variables which do not occur in "
                            + "incoming template ("+incomingTemplate+") and " + TemplateParseUtil.PREFIX 
                            + ", please make sure your outgoing template only includes variables"
                            + " which can be found in the incoming template and " + TemplateParseUtil.PREFIX);
                }
            }
        }
        return redirects.toArray(new URLRedirect[redirects.size()]);
    }

    /**
     * Execute this validate and get the templates so they can be registered
     * @param configControllable the entity provider
     * @return the array of URL redirects
     */
    public static URLRedirect[] validateControllableTemplates(RedirectControllable configControllable) {
        List<URLRedirect> redirects = new ArrayList<URLRedirect>();
        String[] templates = configControllable.defineHandledTemplatePatterns();
        if (templates == null || templates.length == 0) {
            throw new IllegalArgumentException("RedirectControllable: invalid defineHandledTemplatePatterns: " +
            "this should return a non-empty array of templates or the capability should not be used");
        } else {
            for (String template : templates) {
                URLRedirect redirect = null;
                try {
                    redirect = new URLRedirect(template);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("Failed to validate redirect templates from handled template patterns for prefix ("
                            +configControllable.getEntityPrefix() + "): " + e.getMessage(), e);
                }
                redirects.add(redirect);
            }
        }
        return redirects.toArray(new URLRedirect[redirects.size()]);
    }

    protected static Class<?>[] validParamTypes = {
        String.class,
        String[].class,
        Map.class
    };

    /**
     * Validates the parameter types on a method to make sure they are valid
     * @param paramTypes an array of parameter types
     * @return the new valid array of param types
     * @throws IllegalArgumentException if the param types are invalid
     */
    protected static Class<?>[] validateParamTypes(Class<?>[] paramTypes) {
        Class<?>[] validParams = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            boolean found = false;
            Class<?> paramType = paramTypes[i];
            for (int j = 0; j < validParamTypes.length; j++) {
                if (validParamTypes[j].isAssignableFrom(paramType)) {
                    validParams[i] = validParamTypes[j];
                    found = true;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Invalid redirect method: param type is not allowed: " + paramType.getName() 
                        + " : valid types include: " + ReflectUtils.arrayToString(validParamTypes));
            }
        }
        return validParams;
    }

    /**
     * Add all URL redirects to the following prefix,
     * maintains any existing ones
     * @param prefix an entity prefix
     * @param redirects an array of redirects
     * @throws IllegalArgumentException if any of the URL redirects are invalid
     */
    public void addURLRedirects(String prefix, URLRedirect[] redirects) {
        if (redirects != null && redirects.length > 0) {
            ArrayList<URLRedirect> urlRedirects = new ArrayList<URLRedirect>();
            int templateKeys = 0;
            if (entityRedirects.containsKey(prefix)) {
                List<URLRedirect> current = entityRedirects.get(prefix);
                urlRedirects.addAll(current);
                templateKeys += urlRedirects.size();
            }
            StringBuilder sb = new StringBuilder();
            for (URLRedirect redirect : redirects) {
                if (redirect == null || redirect.template == null || "".equals(redirect.template)) {
                    throw new IllegalArgumentException("url redirect and pattern template must not be null");
                }
                if (redirect.outgoingTemplate == null 
                        && redirect.methodName == null
                        && redirect.controllable == false) {
                    throw new IllegalArgumentException("url redirect targetTemplate or methodName must not be null");            
                }
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                if (urlRedirects.contains(redirect)) {
                    throw new IllegalArgumentException("Duplicate redirect template definition: " +
                            "The redirect set already contains this template: " + redirect.template + ", it cannot contain 2 identical templates");
                }
                urlRedirects.add(redirect);
                sb.append(redirect.template);
            }
            entityRedirects.put(prefix, urlRedirects);
            log.info("Registered "+redirects.length+" url redirects for entity prefix ("+prefix+"): " + sb.toString());
        }
    }

    /**
     * Remove any and all redirects for this prefix
     * @param prefix an entity prefix
     */
    public void removeURLRedirects(String prefix) {
        entityRedirects.remove(prefix);
    }

    /**
     * Get the list of all redirects for this prefix
     * @param prefix the entity prefix
     * @return a list of url redirects, may be empty if there are none
     */
    public List<URLRedirect> getURLRedirects(String prefix) {
        List<URLRedirect> redirects = new ArrayList<URLRedirect>();
        if (entityRedirects.containsKey(prefix)) {
            redirects.addAll( entityRedirects.get(prefix) );
        }
        return redirects;
    }

}
