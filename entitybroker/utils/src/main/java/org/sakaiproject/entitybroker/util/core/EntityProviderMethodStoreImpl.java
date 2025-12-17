/**
 * $Id$
 * $URL$
 * EntityProviderMethodStore.java - entity-broker - Jan 13, 2009 11:02:43 AM - azeckoski
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

package org.sakaiproject.entitybroker.util.core;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsDefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectControllable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.TemplateMap;
import org.sakaiproject.entitybroker.entityprovider.extension.URLRedirect;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * This stores the various methods used to track different methods allowed for providers
 * and also tracks the various methods in the registry
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Slf4j
public class EntityProviderMethodStoreImpl implements EntityProviderMethodStore {

    private Set<String> reservedActions;
    private Map<String, Map<String, CustomAction>> entityActions;
    private Map<String, List<URLRedirect>> entityRedirects;

    public EntityProviderMethodStoreImpl() {
        entityActions = new ConcurrentHashMap<>();
        entityRedirects = new ConcurrentHashMap<>();
        reservedActions = Set.of("describe", "new", "edit", "delete");
    }

    public void destroy() {
        entityActions.clear();
        entityRedirects.clear();
    }

    @Override
    public CustomAction[] findCustomActions(EntityProvider entityProvider, boolean ignoreFailures) {
        List<CustomAction> actions = new ArrayList<>();
        Method[] methods = entityProvider.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EntityCustomAction.class)) {
                EntityCustomAction ecaAnnote = method.getAnnotation(EntityCustomAction.class);
                String action = ecaAnnote.action();
                if (null == action || action.isEmpty()) {
                    action = method.getName();
                }
                String viewKey = ecaAnnote.viewKey();
                if (null == viewKey || viewKey.isEmpty()) {
                    //viewKey = EntityView.VIEW_SHOW;
                    viewKey = null; // allow any type of request
                }
                CustomAction ca = new CustomAction(action, viewKey, method.getName());
                try {
                    ca.methodArgTypes = validateActionParamTypes(method.getParameterTypes(), method.getName());
                } catch(IllegalArgumentException e) {
                    if (! ignoreFailures) {
                        throw new IllegalArgumentException(e);
                    }
                }
                ca.setMethod(method); // store the method in the ca
                actions.add(ca);
            } else if (method.getName().endsWith(ActionsExecutable.ACTION_METHOD_SUFFIX)) {
                String action = method.getName().substring(0, method.getName().length() - ActionsExecutable.ACTION_METHOD_SUFFIX.length());
                CustomAction ca = new CustomAction(action, EntityView.VIEW_SHOW, method.getName());
                try {
                    ca.methodArgTypes = validateActionParamTypes(method.getParameterTypes(), method.getName());
                } catch (IllegalArgumentException e) {
                    log.warn("A method [{}] in the entity provider for prefix [{}] appears to be a custom action method "
                            + "but does not have a valid set of parameter types, this may be ok but should be checked on: {}",
                            method.getName(), entityProvider.getEntityPrefix(), e.getMessage());
                    continue;
                }
                ca.setMethod(method); // store the method in the ca
                actions.add(ca);
            }
        }
        return actions.toArray(new CustomAction[0]);
    }

    @Override
    public void storeCustomActions(String prefix, Map<String,CustomAction> actions) {
        Map<String, CustomAction> cas = new HashMap<>();
        for (Entry<String, CustomAction> ca : actions.entrySet()) {
            CustomAction action = ca.getValue();
            if (action == null || ca.getKey() == null || ca.getKey().isEmpty()) {
                throw new IllegalArgumentException("custom action object and action key must not be null");
            }
            if (reservedActions.contains(ca.getKey().toLowerCase())) {
                StringBuilder rsb = new StringBuilder();
                for (String reserved : reservedActions) {
                    if (!rsb.isEmpty()) {
                        rsb.append(", ");
                    }
                    rsb.append(reserved);
                }
                throw new IllegalArgumentException(ca.getKey()
                        + " is a reserved word and cannot be used as a custom action key "
                        + ", reserved words include: " + rsb);
            }
            cas.put(ca.getKey(), action.copy()); // make a copy to avoid holding objects from another ClassLoader
        }
        entityActions.put(prefix, cas);
        log.info("Registered {} custom actions for entity prefix [{}]: {}", actions.size(), prefix, cas.values());
    }

    @Override
    public void addCustomAction(String prefix, CustomAction customAction) {
        // NOTE: we are always creating a new map here to ensure there are no collisions
        Map<String,CustomAction> actions = new HashMap<>();
        if (entityActions.containsKey(prefix)) {
            // add the existing ones first
            actions.putAll(entityActions.get(prefix));
        }
        // add the new one to the map
        actions.put(customAction.action, customAction);
        // put the new map into the store
        storeCustomActions(prefix, actions);
    }

    @Override
    public CustomAction getCustomAction(String prefix, String action) {
        CustomAction ca = null;
        if (entityActions.containsKey(prefix)) {
            ca = entityActions.get(prefix).get(action);
        }
        return ca;
    }

    @Override
    public void removeCustomActions(String prefix) {
        entityActions.remove(prefix);
    }

    @Override
    public List<CustomAction> getCustomActions(String prefix) {
        List<CustomAction> actions = new ArrayList<>();
        Map<String, CustomAction> actionMap = entityActions.get(prefix);
        if (actionMap != null) {
            for (Entry<String, CustomAction> entry : actionMap.entrySet()) {
                actions.add(entry.getValue());
            }
        }
        return actions;
    }

    @Override
    public URLRedirect[] findURLRedirectMethods(EntityProvider entityProvider) {
        ArrayList<URLRedirect> redirects = new ArrayList<>();
        Method[] methods = entityProvider.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EntityURLRedirect.class)) {
                EntityURLRedirect eurAnnote = method.getAnnotation(EntityURLRedirect.class);
                String template = eurAnnote.value();
                if (null == template || template.isEmpty()) {
                    throw new IllegalArgumentException("there is no template set for the annotation: " + EntityURLRedirect.class);
                }
                URLRedirect redirect;
                try {
                    redirect = new URLRedirect(template, method.getName(), 
                            validateRedirectParamTypes(method.getParameterTypes(), method.getName()));
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("Failed to validate redirect templates from methods for prefix ("
                            + entityProvider.getEntityPrefix() + "): " + e.getMessage(), e);
                }
                redirect.setMethod(method); // cache to reduce lookup cost
                redirects.add(redirect);
            }
        }
        Collections.sort(redirects);
        return redirects.toArray(new URLRedirect[0]);
    }

    @Override
    public void addURLRedirects(String prefix, URLRedirect[] redirects) {
        if (redirects != null && redirects.length > 0) {
            ArrayList<URLRedirect> urlRedirects = new ArrayList<>();
            if (entityRedirects.containsKey(prefix)) {
                List<URLRedirect> current = entityRedirects.get(prefix);
                urlRedirects.addAll(current);
            }
            StringBuilder sb = new StringBuilder();
            for (URLRedirect redirect : redirects) {
                if (redirect == null || redirect.template == null || redirect.template.isEmpty()) {
                    throw new IllegalArgumentException("url redirect and pattern template must not be null");
                }
                if (redirect.outgoingTemplate == null 
                        && redirect.methodName == null
                        && !redirect.controllable) {
                    throw new IllegalArgumentException("url redirect targetTemplate or methodName must not be null");            
                }
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }
                if (urlRedirects.contains(redirect)) {
                    throw new IllegalArgumentException("Duplicate redirect template definition: "
                            + "The redirect set already contains this template: " + redirect.template
                            + ", it cannot contain 2 identical templates");
                }
                urlRedirects.add(redirect);
                sb.append(redirect.template);
            }
            entityRedirects.put(prefix, urlRedirects);
            log.info("Registered {} url redirects for entity prefix [{}]: {}", redirects.length, prefix, sb);
        }
    }

    @Override
    public void removeURLRedirects(String prefix) {
        entityRedirects.remove(prefix);
    }

    @Override
    public List<URLRedirect> getURLRedirects(String prefix) {
        List<URLRedirect> redirects = new ArrayList<>();
        if (entityRedirects.containsKey(prefix)) {
            redirects.addAll( entityRedirects.get(prefix) );
        }
        return redirects;
    }

    protected static Class<?>[] validActionParamTypes = {
        EntityReference.class,
        EntityView.class,
        Search.class,
        String.class,
        OutputStream.class,
        Map.class
    };
    protected static Class<?>[] validRedirectParamTypes = {
        String.class,
        String[].class,
        Map.class
    };

    /**
     * Validates the parameter types on a method to make sure they are valid
     * @param paramTypes an array of parameter types
     * @param methodName the name of the method which is being validated (for debugging mostly)
     * @return the new valid array of param types
     * @throws IllegalArgumentException if the param types are invalid
     */
    protected static Class<?>[] validateActionParamTypes(Class<?>[] paramTypes, String methodName) {
        try {
            return validateParamTypes(paramTypes, validActionParamTypes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid custom action method ("+methodName+"): " + e.getMessage(), e);
        }
    }

    /**
     * Validates the parameter types on a method to make sure they are valid
     * @param paramTypes an array of parameter types
     * @param methodName the name of the method which is being validated (for debugging mostly)
     * @return the new valid array of param types
     * @throws IllegalArgumentException is the param types are invalid
     */
    protected static Class<?>[] validateRedirectParamTypes(Class<?>[] paramTypes, String methodName) {
        try {
            return validateParamTypes(paramTypes, validRedirectParamTypes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid redirect method ("+methodName+"): " + e.getMessage(), e);
        }
    }

    /**
     * Validates param types against a given list of valid types
     * @param paramTypes an array of parameter types
     * @param validTypes the types that are valid
     * @return the new valid array of param types
     * @throws IllegalArgumentException is the param types are invalid
     */
    protected static Class<?>[] validateParamTypes(Class<?>[] paramTypes, Class<?>[] validTypes) {
        Class<?>[] validParams = new Class<?>[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            boolean found = false;
            Class<?> paramType = paramTypes[i];
            if (validTypes != null) {
                for (Class<?> validType : validTypes) {
                    if (validType.isAssignableFrom(paramType)) {
                        validParams[i] = validType;
                        found = true;
                    }
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Invalid method params: param type is not allowed: " + paramType.getName() 
                        + " : valid types include: " + ReflectUtils.arrayToString(validTypes));
            }
        }
        return validParams;
    }

    /**
     * Takes a set of custom actions and validates them
     * @param actionsDefineable an entity provider which uses custom actions
     * @throws IllegalArgumentException if the custom actions are invalid
     */
    public static void validateCustomActionMethods(ActionsDefineable actionsDefineable) {
        CustomAction[] customActions = actionsDefineable.defineActions();
        Method[] methods = actionsDefineable.getClass().getMethods();
        for (CustomAction ca : customActions) {
            if (ca == null) {
                throw new IllegalArgumentException("Custom actions cannot be null");
            }
            if (ca.methodName == null || ca.methodName.isEmpty()) {
                throw new IllegalArgumentException("Method names must be set for all custom actions when using " + ActionsDefineable.class);
            }
            boolean found = false;
            for (Method method : methods) {
                String name = method.getName();
                if (name.equals(ca.methodName)) {
                    ca.methodArgTypes = validateActionParamTypes(method.getParameterTypes(), ca.methodName);
                    ca.setMethod(method); // store the method in the ca
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("No public method found called (" + ca.methodName
                        + ") in the entity provider for prefix (" + actionsDefineable.getEntityPrefix() + "), "
                        + "the method was defined as a custom action by " + ActionsDefineable.class);
            }
        }
    }

    /**
     * Validates the provided URL templates in an entity provider and outputs the
     * URL redirect objects as an array
     * @param configDefinable the entity provider
     * @return the array of URL redirects
     */
    public static URLRedirect[] validateDefineableTemplates(RedirectDefinable configDefinable) {
        List<URLRedirect> redirects = new ArrayList<>();
        TemplateMap[] urlMappings = configDefinable.defineURLMappings();
        if (urlMappings == null || urlMappings.length == 0) {
            // this is ok then, or is it?
            log.warn("RedirectDefinable: no templates defined for url redirect");
        } else {
            for (TemplateMap templateMap : urlMappings) {
                String incomingTemplate = templateMap.getIncomingTemplate();
                String outgoingTemplate = templateMap.getOutgoingTemplate();
                URLRedirect redirect;
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
                Set<String> incomingVars = new HashSet<>(redirect.preProcessedTemplate.variableNames);
                incomingVars.add(TemplateParseUtil.PREFIX);
                incomingVars.add(TemplateParseUtil.EXTENSION);
                incomingVars.add(TemplateParseUtil.DOT_EXTENSION);
                incomingVars.add(TemplateParseUtil.QUERY_STRING);
                incomingVars.add(TemplateParseUtil.QUESTION_QUERY_STRING);
                if (incomingVars.containsAll(redirect.outgoingPreProcessedTemplate.variableNames)) {
                    // all is ok
                    redirects.add(redirect);
                } else {
                    throw new IllegalArgumentException("Outgoing template (" + outgoingTemplate + ") has variables which do not occur in "
                            + "incoming template (" + incomingTemplate + ") and " + TemplateParseUtil.PREFIX
                            + ", please make sure your outgoing template only includes variables"
                            + " which can be found in the incoming template and " + TemplateParseUtil.PREFIX);
                }
            }
        }
        return redirects.toArray(new URLRedirect[0]);
    }

    /**
     * Execute this validate and get the templates so they can be registered
     * @param configControllable the entity provider
     * @return the array of URL redirects
     */
    public static URLRedirect[] validateControllableTemplates(RedirectControllable configControllable) {
        List<URLRedirect> redirects = new ArrayList<>();
        String[] templates = configControllable.defineHandledTemplatePatterns();
        if (templates == null || templates.length == 0) {
            throw new IllegalArgumentException("RedirectControllable: invalid defineHandledTemplatePatterns: " +
            "this should return a non-empty array of templates or the capability should not be used");
        } else {
            for (String template : templates) {
                URLRedirect redirect;
                try {
                    redirect = new URLRedirect(template);
                } catch (RuntimeException e) {
                    throw new IllegalArgumentException("Failed to validate redirect templates from handled template patterns for prefix ("
                            + configControllable.getEntityPrefix() + "): " + e.getMessage(), e);
                }
                redirects.add(redirect);
            }
        }
        return redirects.toArray(new URLRedirect[0]);
    }

}
