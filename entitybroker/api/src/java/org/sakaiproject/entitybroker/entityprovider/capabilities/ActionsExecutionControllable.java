/**
 * $Id$
 * $URL$
 * ActionsExecutable.java - entity-broker - Jul 25, 2008 2:46:26 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.io.OutputStream;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.exception.FormatUnsupportedException;

/**
 * This entity supports custom actions (as defined by RoR and REST microformat:
 * http://microformats.org/wiki/rest/urls)<br/>
 * This is the most complex interface for implementing custom actions but allows the most control also,
 * use {@link ActionsExecutable} or {@link ActionsDefineable} in most circumstance<br/>
 * This means that there are custom actions which can be invoked on entities or entity spaces,
 * custom actions can augment the current entity operation or they can completely
 * change the behavior and skip the current operation entirely<br/>
 * You can describe the actions using the {@link Describeable} key: <prefix>.action.<actionKey> = description<br/>
 * You can create methods in your entity provider which either end with {@value #ACTION_METHOD_SUFFIX}
 * or use the {@link EntityCustomAction} suffix to define the custom actions<br/>
 * @see EntityCustomAction for more details
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface ActionsExecutionControllable extends ActionsDefineable {

    /**
     * This allows the developer to define how to execute custom actions on entities,
     * this method will be called every time a custom action execution is requested,
     * the incoming data provides the context for the action to be executed<br/>
     * NOTE: The return data can be complex so please read carefully,
     * entity data is returned as the default for the request if no format is specified
     * @param entityView an entity view, should contain all the information related to the incoming entity request or URL,
     * includes the entity reference and the requested format information
     * @param action key which will be used to trigger the action (e.g. promote),
     * will be triggered by a URL like so: /user/aaronz/promote
     * @param requestValues this is an array which contains passed in action params,
     * if this is running as a result of an http request this will include all the request variables,
     * otherwise this will just return any custom values needed to execute this action
     * @param outputStream an OutputStream to place binary or long text data into,
     * if this is used for binary data then the {@link ActionReturn} should be returned with the correct encoding information
     * and the output variable set to the OutputStream
     * @return this should return one of the following: <br/>
     * 1) null (this is ok in most circumstances to indicate the method is done, use an exception to indicate failure) <br/>
     * 2) an {@link ActionReturn} (this is a special object used to indicate return states and handle binary data) <br/>
     * 3) a UTF-8 encoded OutputStream or String <br/>
     * 4) a List of entity objects <br/>
     * 5) an entity object <br/>
     * 6) a boolean value (true indicates success and is the same as returning null, false indicates failure and causes an {@link EntityNotFoundException} <br/>
     * <br/>
     * Note: Can throw the indicated exceptions and have them translated and handled, all others will pass through
     * Can throw the following exceptions and have them translated and handled, all others will pass through:<br/>
     * {@link EntityNotFoundException} to indicate the entity request could not find the data that was requested <br/>
     * {@link IllegalArgumentException} to indicate that the incoming params or the request was invalid <br/>
     * {@link FormatUnsupportedException} to indicate that the requested format is not supported for this entity request <br/>
     * {@link EntityException} to indicate a specific entity failure occurred, can include a response code and error message <br/>
     * {@link SecurityException} to indicate that the the current user is no allowed to perform this action <br/>
     * {@link IllegalStateException} to indicate a general failure has occurred <br/>
     */
    Object executeActions(EntityView entityView, String action, Map<String, Object> actionParams, OutputStream outputStream);

}
