/**
 * $Id$
 * $URL$
 * CustomAction.java - entity-broker - Jul 25, 2008 2:49:39 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;

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
    final public String action;
    /**
     * (optional) Must match one of the VIEW constants from {@link EntityView}<br/>
     * The view type which this action goes with, this
     * roughly translates to the GET/POST/PUT/DELETE in http<br/>
     * e.g. GET /user/action would be {@link EntityView#VIEW_LIST}
     * while POST /user/aaronz/action would be {@link EntityView#VIEW_NEW},
     * can be null to match all viewkeys (i.e. to allow this action
     * from any http method type and on collections and entities)
     */
    final public String viewKey;
    /**
     * This will be non-null if there is a custom action method which was found or identified
     * by the annotation {@link EntityCustomAction} or if the developer has defined this
     * explicitly
     */
    final public String methodName;
    /**
     * These are the argument types found in the custom action method in order,
     * this should not be populated manually as any value in this will be overwritten
     */
    public Class<?>[] methodArgTypes;

    // NOTE: we are holding onto the method here so the reflection is less costly
    private SoftReference<Method> method;
    /**
     * INTERNAL USE ONLY
     */
    public Method getMethod() {
        Method m = null;
        if (method != null) {
            m = method.get(); 
        }
        return m;
    }
    /**
     * INTERNAL USE ONLY
     */
    public void setMethod(Method m) {
        if (m != null) {
            method = new SoftReference<Method>(m);
        } else {
            method = null;
        }
    }

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
     * @see EntityCustomAction for details about what the method should return, what arguments are allowed, and what exceptions mean
     */
    public CustomAction(String action, String viewKey) {
        this(action, viewKey, null);
    }

    /**
     * Adds the methodName arg,
     * use this version when using this with {@link ActionsDefineable}
     * @param action the action key
     * @param viewKey the view key OR null
     * @param methodName the name of the method in your entity provider to 
     * execute when the custom action is requested
     * @see EntityCustomAction for details about what the method should return, what arguments are allowed, and what exceptions mean
     * @see CustomAction#CustomAction(String, String) for more details
     */
    public CustomAction(String action, String viewKey, String methodName) {
        if (action == null || "".equals(action)) {
            throw new IllegalArgumentException("action must not be null or empty string");
        }
        this.action = action;
        if ("".equals(viewKey)) { viewKey = null; }
        if (viewKey != null) {
            TemplateParseUtil.validateTemplateKey(viewKey);
        }
        this.viewKey = viewKey;
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
