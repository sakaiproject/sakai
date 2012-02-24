/**
 * $Id$
 * $URL$
 * EntityProviderMethodStore.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker.entityprovider;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.extension.CustomAction;
import org.sakaiproject.entitybroker.entityprovider.extension.URLRedirect;

/**
 * This is the interface for handling storage of methods (related to custom actions and the like),
 * this is for internal usage only and should not be accessed or used by other developers
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public interface EntityProviderMethodStore {

    // ACTIONS

    /**
     * Get all the custom actions that can be found
     * @param entityProvider the provider to search for custom actions
     * @param ignoreFailures if true then will not throw exceptions  if methods are not found
     * @return the array of CustomAction objects which are found
     */
    public CustomAction[] findCustomActions(EntityProvider entityProvider, boolean ignoreFailures);

    /**
     * Set the custom actions for this prefix
     * @param prefix an entity prefix
     * @param actions a map of action -> {@link CustomAction}
     */
    public void setCustomActions(String prefix, Map<String,CustomAction> actions);

    /**
     * Add a custom action for a prefix
     * @param prefix an entity prefix
     * @param customAction the custom action to add
     */
    public void addCustomAction(String prefix, CustomAction customAction);

    /**
     * Remove any custom actions that are set for this prefix
     * @param prefix an entity prefix
     */
    public void removeCustomActions(String prefix);

    /**
     * Gets the list of all custom actions for a prefix
     * @param prefix an entity prefix
     * @return a list of CustomActions for this prefix, empty if there are none
     */
    public List<CustomAction> getCustomActions(String prefix);

    /**
     * Get the {@link CustomAction} for a prefix and action if it exists
     * @param prefix an entity prefix
     * @param action an action key
     * @return the custom action OR null if none found
     */
    public CustomAction getCustomAction(String prefix, String action);


    // REDIRECT
    
    /**
     * Looks for redirect methods in the given entity provider
     * @param entityProvider an entity provider
     * @return an array of redirect objects
     * @throws IllegalArgumentException if the methods are setup incorrectly
     */
    public URLRedirect[] findURLRedirectMethods(EntityProvider entityProvider);

    /**
     * Add all URL redirects to the following prefix,
     * maintains any existing ones
     * @param prefix an entity prefix
     * @param redirects an array of redirects
     * @throws IllegalArgumentException if any of the URL redirects are invalid
     */
    public void addURLRedirects(String prefix, URLRedirect[] redirects);

    /**
     * Remove any and all redirects for this prefix
     * @param prefix an entity prefix
     */
    public void removeURLRedirects(String prefix);

    /**
     * Get the list of all redirects for this prefix
     * @param prefix the entity prefix
     * @return a list of url redirects, may be empty if there are none
     */
    public List<URLRedirect> getURLRedirects(String prefix);

}