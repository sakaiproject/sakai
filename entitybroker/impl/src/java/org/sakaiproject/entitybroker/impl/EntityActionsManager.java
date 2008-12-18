/**
 * $Id$
 * $URL$
 * EntityActionsManager.java - entity-broker - Jul 26, 2008 9:58:00 AM - azeckoski
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsDefineable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutionControllable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;
import org.sakaiproject.entitybroker.impl.util.RequestUtils;
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.http.LazyResponseOutputStream;
import org.azeckoski.reflectutils.ReflectUtils;


/**
 * Handles everything related to the custom actions registration and execution
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityActionsManager {

    private static Log log = LogFactory.getLog(EntityActionsManager.class);

    private HashSet<String> reservedActions = null;
    private Map<String, Map<String, CustomAction>> entityActions = new ConcurrentHashMap<String, Map<String,CustomAction>>();

    public EntityActionsManager() {
        reservedActions = new HashSet<String>(4);
        reservedActions.add("describe");
        reservedActions.add("new");
        reservedActions.add("edit");
        reservedActions.add("delete");
    }

    /**
     * Execute a custom action request
     * @param actionProvider
     * @param entityView
     * @param action
     * @param request
     * @param response
     * @return an action return (may be null)
     * @throws IllegalArgumentException if any args are invalid
     * @throws UnsupportedOperationException if the action is not valid for this prefix
     * @throws IllegalStateException if a failure occurs
     */
    public ActionReturn handleCustomActionRequest(ActionsExecutable actionProvider, EntityView entityView, String action,
            HttpServletRequest request, HttpServletResponse response, Map<String, Object> searchParams) {
        if (actionProvider == null || entityView == null || action == null || request == null || response == null) {
            throw new IllegalArgumentException("actionProvider and view and action and request and response must not be null");
        }
        // get the action params out of the request first
        Map<String, Object> actionParams = RequestStorageImpl.getRequestValues(request, true, true, true);
        EntityReference ref = entityView.getEntityReference();
        OutputStream outputStream = new LazyResponseOutputStream(response);
        ActionReturn actionReturn = handleCustomActionExecution(actionProvider, ref, action, actionParams, outputStream, entityView, searchParams);
        // now process the return into the request or response as needed
        if (actionReturn != null) {
            if (actionReturn.output != null || actionReturn.outputString != null) {
                if (actionReturn.output == null) {
                    // write the string into the response outputstream
                    try {
                        outputStream.write( actionReturn.outputString.getBytes() );
                    } catch (IOException e) {
                        throw new RuntimeException("Failed encoding for outputstring: " + actionReturn.outputString);
                    }
                    actionReturn.output = outputStream;
                }
                // now set the encoding, mimetype into the response
                actionReturn.format = entityView.getExtension();
                if (actionReturn.encoding == null || actionReturn.mimeType == null) {
                    // use default if not set
                    if (actionReturn.format == null) {
                        actionReturn.format = Formats.XML;
                    }
                    RequestUtils.setResponseEncoding(actionReturn.format, response);
                } else {
                    response.setCharacterEncoding(actionReturn.encoding);
                    response.setContentType(actionReturn.mimeType);
                }
            }
            // also sets the response code when handling the action
            if (actionReturn.responseCode > 0) {
                response.setStatus(actionReturn.responseCode);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
            }
            // other returns require no extra work here
        } else {
            // no failure so set the status code
            response.setStatus(HttpServletResponse.SC_OK);
        }
        return actionReturn;
    }

    /**
     * Handles the execution of custom actions based on a request for execution
     * @throws IllegalArgumentException if any args are invalid
     * @throws UnsupportedOperationException if the action is not valid for this prefix
     */
    public ActionReturn handleCustomActionExecution(ActionsExecutable actionProvider, EntityReference ref, String action, 
            Map<String, Object> actionParams, OutputStream outputStream, EntityView view, Map<String, Object> searchParams) {
        if (actionProvider == null || ref == null || action == null || "".equals(action)) {
            throw new IllegalArgumentException("actionProvider and ref and action must not be null");
        }
        if (outputStream == null) {
            // create an outputstream to hold the data
            outputStream = new ByteArrayOutputStream();
        }
        String prefix = ref.getPrefix();
        CustomAction customAction = getCustomAction(prefix, action);
        if (customAction == null) {
            throw new UnsupportedOperationException("Invalid action ("+action+"), this action is not a supported custom action for prefix ("+prefix+")");
        }
        ActionReturn actionReturn = null;
        Object result = null;
        if (ActionsExecutionControllable.class.isAssignableFrom(actionProvider.getClass())) {
            // execute the action
            result = ((ActionsExecutionControllable)actionProvider).executeActions(new EntityView(ref, null, null), action, actionParams, outputStream);
        } else {
            if (customAction.methodName == null) {
                throw new IllegalStateException("The custom action must have the method name set, null is not allowed: " + customAction);
            }
            Method method = customAction.getMethod();
            if (method == null) {
                try {
                    // Note: this is really expensive, need to cache the Method lookup
                    method = actionProvider.getClass().getMethod(customAction.methodName, customAction.methodArgTypes);
                } catch (SecurityException e1) {
                    throw new RuntimeException("Fatal error trying to get custom action method: " + customAction, e1);
                } catch (NoSuchMethodException e1) {
                    throw new RuntimeException("Fatal error trying to get custom action method: " + customAction, e1);
                }
                customAction.setMethod(method); // cache the method
            }
            Object[] args = new Object[customAction.methodArgTypes.length];
            for (int i = 0; i < customAction.methodArgTypes.length; i++) {
                Class<?> argType = customAction.methodArgTypes[i];
                if (EntityReference.class.equals(argType)) {
                    args[i] = ref;
                } else if (EntityView.class.equals(argType)) {
                    if (view == null) {
                        view = new EntityView(ref, customAction.viewKey, null);
                    }
                    args[i] = view;
                } else if (String.class.equals(argType)) {
                    args[i] = actionProvider.getEntityPrefix();
                } else if (Search.class.equals(argType)) {
                    Search search = null;
                    if (searchParams == null || searchParams.isEmpty()) {
                        search = new Search();
                    } else {
                        search = RequestUtils.makeSearchFromRequestParams(searchParams);
                    }
                    args[i] = search;
                } else if (OutputStream.class.equals(argType)) {
                    args[i] = outputStream;
                } else if (Map.class.equals(argType)) {
                    args[i] = actionParams;
                } else {
                    throw new IllegalStateException("custom action ("+customAction+") contains an invalid methodArgTypes, " +
                            "only valid types allowed: " + ReflectUtils.arrayToString(validParamTypes));
                }
            }
            try {
                result = method.invoke(actionProvider, args);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Fatal error trying to execute custom action method: " + customAction, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Fatal error trying to execute custom action method: " + customAction, e);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    if (e.getCause().getClass().isAssignableFrom(IllegalArgumentException.class)) {
                        throw new IllegalArgumentException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                    } else if (e.getCause().getClass().isAssignableFrom(EntityNotFoundException.class)) {
                        throw new EntityNotFoundException(e.getCause().getMessage() + " (rethrown)", ref+"", e.getCause());
                    } else if (e.getCause().getClass().isAssignableFrom(FormatUnsupportedException.class)) {
                        String format = ((FormatUnsupportedException)e.getCause()).format;
                        throw new FormatUnsupportedException(e.getCause().getMessage() + " (rethrown)", e.getCause(), ref+"", format);
                    } else if (e.getCause().getClass().isAssignableFrom(UnsupportedOperationException.class)) {
                        throw new UnsupportedOperationException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                    } else if (e.getCause().getClass().isAssignableFrom(EntityException.class)) {
                        int code = ((EntityException)e.getCause()).responseCode;
                        throw new EntityException(e.getCause().getMessage() + " (rethrown)", ref+"", code);
                    } else if (e.getCause().getClass().isAssignableFrom(IllegalStateException.class)) {
                        throw new IllegalStateException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                    } else if (e.getCause().getClass().isAssignableFrom(SecurityException.class)) {
                        throw new SecurityException(e.getCause().getMessage() + " (rethrown)", e.getCause());
                    }
                }
                throw new RuntimeException("Fatal error trying to execute custom action method: " + customAction, e);
            }
        }
        if (result != null) {
            Class<?> resultClass = result.getClass();
            // package up the result in the ActionResult
            if (Boolean.class.isAssignableFrom(resultClass)) {
                // handle booleans specially
                boolean bool = ((Boolean) result).booleanValue();
                if (bool) {
                    result = null;
                } else {
                    throw new EntityNotFoundException("Could not find data for ref ("+ref+") from custom action ("+action+"), (returned boolean false)", ref+"");
                }
            } else if (ActionReturn.class.isAssignableFrom(resultClass)) {
                actionReturn = (ActionReturn) result;
            } else if (OutputStream.class.isAssignableFrom(resultClass)) {
                actionReturn = new ActionReturn(outputStream);
            } else if (String.class.isAssignableFrom(resultClass)) {
                actionReturn = new ActionReturn((String) result);
            } else if (List.class.isAssignableFrom(resultClass)) {
                // convert the list to a list of EntityData
                List<EntityData> data = EntityDataUtils.convertToEntityData((List<?>) result, ref);
                actionReturn = new ActionReturn(data, (String) null);
            } else if (EntityData.class.isAssignableFrom(resultClass)) {
                actionReturn = new ActionReturn( (EntityData) result, (String) null);
            } else {
                // assume this is an entity object (not ED)
                EntityData ed = EntityDataUtils.makeEntityData(ref, result);
                actionReturn = new ActionReturn( ed, (String) null);
            }
        }
        return actionReturn;
    }

    protected static Class<?>[] validParamTypes = {
        EntityReference.class,
        EntityView.class,
        Search.class,
        String.class,
        OutputStream.class,
        Map.class
    };
    /**
     * Get all the custom actions that can be found
     * @param entityProvider the provider to search for custom actions
     * @param ignoreFailures if true then will not throw exceptions  if methods are not found
     * @return the array of CustomAction objects which are found
     */
    public CustomAction[] findCustomActions(EntityProvider entityProvider, boolean ignoreFailures) {
        ArrayList<CustomAction> actions = new ArrayList<CustomAction>();
        Method[] methods = entityProvider.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(EntityCustomAction.class)) {
                EntityCustomAction ecaAnnote = method.getAnnotation(EntityCustomAction.class);
                String action = ecaAnnote.action();
                if (null == action || "".equals(action)) {
                    action = method.getName();
                }
                String viewKey = ecaAnnote.viewKey();
                if (null == viewKey || "".equals(viewKey)) {
                    //viewKey = EntityView.VIEW_SHOW;
                    viewKey = null; // allow any type of request
                }
                CustomAction ca = new CustomAction(action, viewKey, method.getName());
                try {
                    ca.methodArgTypes = validateParamTypes(method.getParameterTypes());
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
                    ca.methodArgTypes = validateParamTypes(method.getParameterTypes());
                } catch (IllegalArgumentException e) {
                    log.warn("A method ("+method.getName()+") in the entity provider for prefix ("
                            + entityProvider.getEntityPrefix()+") appears to be a custom action method but"
                            + "does not have a valid set of parameter types, this may be ok but should be checked on: " + e.getMessage());
                    continue;
                }
                ca.setMethod(method); // store the method in the ca
                actions.add(ca);
            }
        }
        return actions.toArray(new CustomAction[actions.size()]);
    }

    /**
     * Takes a set of custom actions and validates them
     * @param actionsDefineable an entity provider which uses custom actions
     * @param customActions
     * @throws IllegalArgumentException if the custom actions are invalid
     */
    public static void validateCustomActionMethods(ActionsDefineable actionsDefineable) {
        CustomAction[] customActions = actionsDefineable.defineActions();
        Method[] methods = actionsDefineable.getClass().getMethods();
        for (int i = 0; i < customActions.length; i++) {
            CustomAction ca = customActions[i];
            if (ca == null) {
                throw new IllegalArgumentException("Custom actions cannot be null");
            }
            if (ca.methodName == null || "".equals(ca.methodName)) {
                throw new IllegalArgumentException("Method names must be set for all custom actions when using " + ActionsDefineable.class);
            }
            boolean found = false;
            for (Method method : methods) {
                String name = method.getName();
                if (name.equals(ca.methodName)) {
                    ca.methodArgTypes = validateParamTypes(method.getParameterTypes());
                    ca.setMethod(method); // store the method in the ca
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("No public method found called ("+ca.methodName
                        +") in the entity provider for prefix ("+actionsDefineable.getEntityPrefix()+"), "
                        + "the method was defined as a custom action by " + ActionsDefineable.class);
            }
        }
    }

    /**
     * Validates the parameter types on a method to make sure they are valid
     * @param paramTypes an array of parameter types
     * @return the new valid array of param types
     * @throws IllegalArgumentException is the param types are invalid
     */
    public static Class<?>[] validateParamTypes(Class<?>[] paramTypes) {
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
                throw new IllegalArgumentException("Invalid custom action method: param type is not allowed: " + paramType.getName() 
                        + " : valid types include: " + ReflectUtils.arrayToString(validParamTypes));
            }
        }
        return validParams;
    }

    /**
     * Set the custom actions for this prefix
     * @param prefix an entity prefix
     * @param actions a map of action -> {@link CustomAction}
     */
    public void setCustomActions(String prefix, Map<String,CustomAction> actions) {
        Map<String,CustomAction> cas = new HashMap<String, CustomAction>();
        StringBuilder sb = new StringBuilder();
        for (Entry<String, CustomAction> ca : actions.entrySet()) {
            CustomAction action = ca.getValue();
            if (action == null || ca.getKey() == null || "".equals(ca.getKey())) {
                throw new IllegalArgumentException("custom action object and action key must not be null");
            }
            if (reservedActions.contains(ca.getKey().toLowerCase())) {
                StringBuilder rsb = new StringBuilder();
                for (String reserved : reservedActions) {
                    if (rsb.length() > 0) {
                        rsb.append(", ");
                    }
                    rsb.append(reserved);
                }
                throw new IllegalArgumentException(ca.getKey() + " is a reserved word and cannot be used as a custom action key "
                        + ", reserved words include: " + rsb);
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(ca.getValue().toString());
            cas.put(ca.getKey(), action.copy()); // make a copy to avoid holding objects from another ClassLoader
        }
        entityActions.put(prefix, actions);
        log.info("Registered "+actions.size()+" custom actions for entity prefix ("+prefix+"): " + sb.toString());
    }

    /**
     * Add a custom action for a prefix
     * @param prefix an entity prefix
     * @param customAction the custom action to add
     */
    public void addCustomAction(String prefix, CustomAction customAction) {
        // NOTE: we are always creating a new map here to ensure there are no collisions
        Map<String,CustomAction> actions = new HashMap<String, CustomAction>();
        if (entityActions.containsKey(prefix)) {
            // add the existing ones first
            actions.putAll(entityActions.get(prefix));
        }
        // add the new one to the map
        actions.put(customAction.action, customAction);
        // put the new map into the store
        setCustomActions(prefix, actions);
    }

    /**
     * Get the {@link CustomAction} for a prefix and action if it exists
     * @param prefix an entity prefix
     * @param action an action key
     * @return the custom action OR null if none found
     */
    public CustomAction getCustomAction(String prefix, String action) {
        CustomAction ca = null;
        if (entityActions.containsKey(prefix)) {
            ca = entityActions.get(prefix).get(action);
        }
        return ca;
    }

    /**
     * Remove any custom actions that are set for this prefix
     * @param prefix an entity prefix
     */
    public void removeCustomActions(String prefix) {
        entityActions.remove(prefix);
    }

    /**
     * Gets the list of all custom actions for a prefix
     * @param prefix an entity prefix
     * @return a list of CustomActions for this prefix, empty if there are none
     */
    public List<CustomAction> getCustomActions(String prefix) {
        List<CustomAction> actions = new ArrayList<CustomAction>();
        Map<String, CustomAction> actionMap = entityActions.get(prefix);
        if (actionMap != null) {
            for (Entry<String, CustomAction> entry : actionMap.entrySet()) {
                actions.add(entry.getValue());
            }
        }
        return actions;
    }

}
