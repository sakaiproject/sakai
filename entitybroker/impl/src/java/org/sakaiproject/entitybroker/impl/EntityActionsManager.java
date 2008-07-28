/**
 * $Id$
 * $URL$
 * EntityActionsManager.java - entity-broker - Jul 26, 2008 9:58:00 AM - azeckoski
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.impl.entityprovider.extension.RequestStorageImpl;
import org.sakaiproject.entitybroker.impl.util.RequestUtils;


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

   public ActionReturn handleCustomActionRequest(ActionsExecutable actionProvider, EntityView entityView, String action,
         HttpServletRequest request, HttpServletResponse response) {
      if (actionProvider == null || entityView == null || action == null || request == null || response == null) {
         throw new IllegalArgumentException("actionProvider and view and action and request and response must not be null");
      }
      // get the action params out of the request first
      Map<String, Object> actionParams = RequestStorageImpl.getRequestValues(request);
      EntityReference ref = entityView.getEntityReference();
      OutputStream outputStream = null;
      try {
         outputStream = response.getOutputStream();
      } catch (IOException e1) {
         throw new RuntimeException("Failed getting response output stream");
      }
      ActionReturn actionReturn = handleCustomActionExecution(actionProvider, ref, action, actionParams, outputStream);
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
         // other returns require no extra work here
      }
      return actionReturn;
   }

   /**
    * Handles the execution of custom actions based on a request for execution
    * @throws IllegalArgumentException if any args are invalid
    * @throws UnsupportedOperationException if the action is not valid for this prefix
    */
   public ActionReturn handleCustomActionExecution(ActionsExecutable actionProvider, EntityReference ref, String action, 
         Map<String, Object> actionParams, OutputStream outputStream) {
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
      // TODO handle action methods?
      // execute the action
      Object result = actionProvider.executeActions(new EntityView(ref, null, null), action, actionParams, outputStream);
      if (result != null) {
         // package up the result in the ActionResult
         Class<?> resultClass = result.getClass();
         if (ActionReturn.class.isAssignableFrom(resultClass)) {
            actionReturn = (ActionReturn) result;
         } else if (OutputStream.class.isAssignableFrom(resultClass)) {
            actionReturn = new ActionReturn(outputStream);
         } else if (String.class.isAssignableFrom(resultClass)) {
            actionReturn = new ActionReturn((String) result);
         } else if (List.class.isAssignableFrom(resultClass)) {
            actionReturn = new ActionReturn((List<?>) result, null);
         } else {
            // assume this is an entity object
            actionReturn = new ActionReturn(result, null);
         }
      }
      return actionReturn;
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

}
