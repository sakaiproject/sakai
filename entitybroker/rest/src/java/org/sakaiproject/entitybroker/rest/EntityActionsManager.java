/**
 * $Id$
 * $URL$
 * EntityActionsManager.java - entity-broker - Jul 26, 2008 9:58:00 AM - azeckoski
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderMethodStore;
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
import org.sakaiproject.entitybroker.util.EntityDataUtils;
import org.sakaiproject.entitybroker.util.http.LazyResponseOutputStream;
import org.sakaiproject.entitybroker.util.request.RequestStorageImpl;
import org.sakaiproject.entitybroker.util.request.RequestUtils;


/**
 * Handles everything related to the custom actions registration and execution
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityActionsManager {

    protected EntityActionsManager() { }

    /**
     * Full constructor
     * @param entityProviderMethodStore the provider method store service
     */
    public EntityActionsManager(EntityProviderMethodStore entityProviderMethodStore) {
        this.entityProviderMethodStore = entityProviderMethodStore;
    }

    private EntityProviderMethodStore entityProviderMethodStore;
    public void setEntityProviderMethodStore(EntityProviderMethodStore entityProviderMethodStore) {
        this.entityProviderMethodStore = entityProviderMethodStore;
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
                        actionReturn.format = Formats.TXT;
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
        CustomAction customAction = entityProviderMethodStore.getCustomAction(prefix, action);
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
                            "only valid types allowed: EntityReference, EntityView, Search, String, OutputStream, Map");
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

    /**
     * Get the {@link CustomAction} for a prefix and action if it exists
     * @param prefix an entity prefix
     * @param action an action key
     * @return the custom action OR null if none found
     */
    public CustomAction getCustomAction(String prefix, String action) {
        return entityProviderMethodStore.getCustomAction(prefix, action);
    }

}
