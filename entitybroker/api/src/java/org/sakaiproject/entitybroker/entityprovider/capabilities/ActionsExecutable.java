/**
 * $Id$
 * $URL$
 * ActionsExecutable.java - entity-broker - Jul 25, 2008 2:46:26 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.capabilities;

import java.io.OutputStream;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;


/**
 * This entity supports custom actions (as defined by RoR and REST microformat:
 * http://microformats.org/wiki/rest/urls)<br/>
 * This means that there are custom actions which can be invoked on entities or entity spaces,
 * custom actions can augment the current entity operation or they can completely
 * change the behavior and skip the current operation entirely<br/>
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface ActionsExecutable extends EntityProvider {

   /**
    * Defines the custom actions which are allowed to be performed on your entities
    * @return an array of the custom actions in the order they should be checked
    * for in incoming requests
    */
   CustomAction[] defineActions();

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
    * @throws IllegalArgumentException if there are required params that are missing or invalid
    * @throws IllegalStateException if the action cannot be performed for some reason
    */
   Object executeActions(EntityView entityView, String action, Map<String, Object> actionParams, OutputStream outputStream);

}
