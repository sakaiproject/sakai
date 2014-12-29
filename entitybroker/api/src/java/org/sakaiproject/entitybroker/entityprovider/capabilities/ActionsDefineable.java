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

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;


/**
 * This entity supports custom actions (as defined by RoR and REST microformat:
 * http://microformats.org/wiki/rest/urls)<br/>
 * This is the more controllable version, use {@link ActionsExecutable} if you
 * want to use the conventions and allow the system to detect your custom actions based
 * on method names and annotations<br/>
 * This means that there are custom actions which can be invoked on entities or entity spaces,
 * custom actions can augment the current entity operation or they can completely
 * change the behavior and skip the current operation entirely<br/>
 * You can create methods in your entity provider which either end with {@value #ACTION_METHOD_SUFFIX}
 * or use the {@link EntityCustomAction} suffix to define the custom actions<br/>
 * You can describe the actions using the {@link Describeable} key: <prefix>.action.<actionKey> = description<br/>
 * If you want more control then you can use {@link ActionsExecutionControllable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface ActionsDefineable extends ActionsExecutable {

   /**
    * Defines the custom actions which are allowed to be performed on your entities
    * @return an array of the custom actions in the order they should be checked
    * for in incoming requests<br/>
    * The action keys should match with public methods in your provider which end with
    * {@link ActionsExecutable#ACTION_METHOD_SUFFIX} or have the {@link EntityCustomAction}
    * annotation on them, use the fields in {@link CustomAction} to define the methodName
    * @see CustomAction for details about what to return
    * @see EntityCustomAction for more info on what your methods should look like
    */
   CustomAction[] defineActions();

}
