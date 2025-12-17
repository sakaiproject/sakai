/**
 * $Id$
 * $URL$
 * EntityCustomAction.java - entity-broker - Jul 28, 2008 11:09:39 AM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.annotations;

import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;


/**
 * This annotation indicates that this method is a custom action from {@link ActionsExecutable},
 * this should not be placed on any methods defined by a capability but should be placed on methods
 * which you want to be exposed as custom actions<br/>
 * By default the name of the method is used as the action key and this will work for read requests 
 * (viewKey is set to {@link EntityView#VIEW_SHOW}), 
 * you can add in action and viewKey annotation params to change those settings<br/>
 * You can describe this action using the key: <prefix>.action.<actionKey> = description<br/>
 * The methods annotated by this can have the following argument (parameter) types: <br/>
 * (type => data which will be given to the method) <br/>
 * {@link EntityView} : the current entity view for this request (contains extension, url, segments) <br/>
 * {@link EntityReference} : the current entity reference (prefix and optional id) <br/>
 * {@link String} : entity prefix <br/>
 * {@link Search} : the search object based on the incoming params <br/>
 * {@link OutputStream} : stream to place outbound data (probably binary) into for transmission <br/>
 * {@link Map} ({@link String} => {@link Object}) : a map of the actions parameters (params from the action request) <br/>
 * <br/>
 * These methods should return one of the following: <br/>
 * 1) null (this is ok in most circumstances to indicate the method is done, use an exception to indicate failure) <br/>
 * 2) an {@link ActionReturn} (this is a special object used to indicate return states and handle binary data) <br/>
 * 3) an {@link EntityData} (this is a special object used to wrap the object and provide meta data) <br/>
 * 4) a UTF-8 encoded OutputStream or String <br/>
 * 5) a List of entity objects <br/>
 * 6) an entity object <br/>
 * 7) a boolean value (true indicates success and is the same as returning null, false indicates failure and causes an {@link EntityNotFoundException} <br/>
 * <br/>
 * Can throw the following exceptions and have them translated and handled, all others will pass through:<br/>
 * {@link EntityNotFoundException} to indicate the entity request could not find the data that was requested <br/>
 * {@link IllegalArgumentException} to indicate that the incoming params or the request was invalid <br/>
 * {@link FormatUnsupportedException} to indicate that the requested format is not supported for this entity request <br/>
 * {@link EntityException} to indicate a specific entity failure occurred, can include a response code and error message <br/>
 * {@link SecurityException} to indicate that the the current user is no allowed to perform this action <br/>
 * {@link UnsupportedOperationException} to indicate that the current action being requested is invalid (typically indicates bad combination of viewKey and action) <br/>
 * {@link IllegalStateException} to indicate a general failure has occurred <br/>
 * <br/>
 * @see CustomAction
 * @see ActionsExecutable
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface EntityCustomAction {
    /**
     * (optional) The action key which will be used to trigger the action (e.g. promote) <br/>
     * By default the name of the method is used as the action key if this is not set,
     * trigger this action using a URL like: /direct/{prefix}/{name}
     * @return the name to use for this custom action
     */
    String action() default "";
    /**
     * (optional) Must match one of the VIEW constants from {@link EntityView},
     * if not set this will default to {@link EntityView#VIEW_SHOW} <br/>
     * The view type which this action works with (i.e. allowed to trigger the action), this
     * roughly translates to the GET/POST/PUT/DELETE in http<br/>
     * e.g. GET /user/action would be {@link EntityView#VIEW_LIST}
     * while POST /user/aaronz/action would be {@link EntityView#VIEW_NEW},
     * can be null or "" (empty string, for annotations you must use this since null is not a constant) 
     * to match all viewkeys (i.e. to allow this action
     * from any http method type and on collections and entities) <br/>
     * @return the view key constant from {@link EntityView}
     */
    String viewKey() default EntityView.VIEW_SHOW;
}
