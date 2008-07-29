/**
 * $Id$
 * $URL$
 * CustomAction.java - entity-broker - Jul 25, 2008 2:49:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsDefineable;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;


/**
 * This defines a custom entity action,
 * this will be used to define which custom actions are allowed to be performed on
 * entities or collections of entities
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class CustomAction {

   /**
    * The action key which will be used to trigger the action (e.g. promote),
    * will be triggered by a URL like so: /user/aaronz/promote
    */
   public String action;
   /**
    * (optional) Must match one of the VIEW constants from {@link EntityView}<br/>
    * The view type which this action goes with, this
    * roughly translates to the GET/POST/PUT/DELETE in http<br/>
    * e.g. GET /user/action would be {@link EntityView#VIEW_LIST}
    * while POST /user/aaronz/action would be {@link EntityView#VIEW_SHOW},
    * can be null to match all viewkeys (i.e. to allow this action
    * from any http method type and on collections and entities)
    */
   public String viewKey = EntityView.VIEW_SHOW;
   /**
    * This will be non-null if there is a custom action method which was found or identified
    * by the annotation {@link EntityCustomAction} or if the developer has defined this
    * explicitly
    */
   public String methodName;
   /**
    * These are the argument types found in the custom action method in order,
    * this should not be populated manually as any value in this will be overwritten
    */
   public Class<?>[] methodArgTypes;

   /**
    * Construct a custom action for entities
    * @param action key which will be used to trigger the action (e.g. promote),
    * will be triggered by a URL like so: /user/aaronz/promote, <br/>
    * this cannot be null or empty string
    * @param viewKey (optional) this is the view type which this action goes with, this
    * roughly translates to the GET/POST/PUT/DELETE in http<br/>
    * e.g. GET /user/action would be {@link EntityView#VIEW_LIST}
    * while POST /user/aaronz/action would be {@link EntityView#VIEW_SHOW}<br/>
    * this must match one of the VIEW constants from {@link EntityView},
    * null indicates that this action can be run on entities and collections
    * using any http method (POST, GET)
    */
   public CustomAction(String action, String viewKey) {
      if (action == null || "".equals(action)) {
         throw new IllegalArgumentException("action must not be null or empty string");
      }
      this.action = action;
      if ("".equals(viewKey)) { viewKey = null; }
      if (viewKey != null) {
         TemplateParseUtil.validateTemplateKey(viewKey);
      }
      this.viewKey = viewKey;
   }

   /**
    * Adds the methodName arg,
    * use this version when using this with {@link ActionsDefineable}
    * @param action the action key
    * @param viewKey the view key OR null
    * @param methodName the name of the method in your entity provider to 
    * execute when the custom action is requested
    * @see CustomAction#CustomAction(String, String)
    */
   public CustomAction(String action, String viewKey, String methodName) {
      this(action, viewKey);
      this.methodName = methodName;
   }

   /**
    * @return a copy of this object
    */
   public CustomAction copy() {
      return copy(this);
   }

   /**
    * @param ca
    * @return a copy of the supplied object
    */
   public static CustomAction copy(CustomAction ca) {
      if (ca == null) {
         throw new IllegalArgumentException("action to copy must not be null");
      }
      CustomAction togo = new CustomAction(ca.action, ca.viewKey, ca.methodName);
      togo.methodArgTypes = ca.methodArgTypes;
      return togo;
   }

   @Override
   public String toString() {
      return this.action + ":" + this.viewKey + ":" + this.methodName;
   }

}
