/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.content.api;

import java.util.Collection;
import java.util.Map;

import org.sakaiproject.javax.Filter;

/**
 * <p>
 * ResourceTypeRegistry is the API for managing definitions types of resources for the Resources tool.
 * </p>
 */
public interface ResourceTypeRegistry 
{
	/**
	 * Access the definition of an action based on the id of the type in which it's defined and
	 * the id of the action within that type.
	 * @param typeId
	 * @param actionId
	 * @return
	 */
	public ResourceToolAction getAction(String typeId, String actionId);
	
	/**
	 * Access a registered multi-item action.
	 * @param listActionId
	 * @return the action, or null if no multi-item action registered with that id.
	 */
	public ServiceLevelAction getMultiItemAction(String listActionId);
	
	/**
	 * Access all multi-item actions that have been registered.
	 * @return
	 */
	public Collection<ServiceLevelAction> getMultiItemActions();
	
	/**
	 *  Access the definition of a particular resource type.
	 * @param typeId The id of the resource type.
	 * @return The ResourceType object which defines the requested type, or null if the type is not defined.
	 */
	public ResourceType getType(String typeId);
	
	/**
	 * Access a collection (possibly empty) of all resource types that have been defined.
	 * @return
	 */
	public Collection getTypes();
	
	/**
	 * Access a subset of the resource types that have been defined where membership in the subset is 
	 * determined by whether the filter indicates that the ResourceType is accepted.  The filter can 
	 * accept a ResourceType object based on any attribute of the type that can be determined from the
	 * ResourceType object itself.
	 * @param filter
	 * @return
	 */
	public Collection getTypes(Filter filter);
	
	/**
	 * In converting to the type registry, we need to use mimetype in some cases as a way to identify
	 * the resource-type of some existing resources.  This method handles the mapping from mimetype
	 * to resource-type.
	 * @param contentType A mime type
	 * @return The "resource-type" for items with the given mime-type.
	 */
	public String mimetype2resourcetype(String contentType);
	
	/**
	 * Create a new "pipe" that can be used as a conduit of information between the Resources tool 
	 * and a helper that handles some part of an action. 
	 * @param initializationId
	 * @param action
	 * @return
	 */
	public ResourceToolActionPipe newPipe(String initializationId, ResourceToolAction action);
		
	/**
	 * Register a ResourceType object to indicate that resources of that type can be defined in  
	 * the Resources tool.  If the InteractionAction object is null or if the type object's getId()  
	 * method returns a null value, no type is registered. 
	 * @param type
	 */
	public void register(ResourceType type);
	/**
	 * Register a ResourceType object to indicate that resources of that type can be defined in
	 * the Resources tool ; and register a ContentChangeHandler with the ResourceType to deal with
	 * changing content for that type.  If the InteractionAction object is null or if the type object's getId()
	 * method returns a null value, no type is registered.
	 * @param type
	 */
	public void register(ResourceType type, ContentChangeHandler cch);
	/**
	 *  Returns the ContentChangeHandler of a particular resource type.
	 * @param resourceType The resource type.
	 * @return The ContentChangeHandler object associated with that type or null if the type is not defined.
	 */
	public ContentChangeHandler getContentChangeHandler(String resourceType);
	/**
	 * @param context
	 * @param enabled
	 */
	public void setMapOfResourceTypesForContext(String context, Map<String,Boolean> enabled);
	
	/**
	 * @param context
	 * @return
	 */
	public Map<String,Boolean> getMapOfResourceTypesForContext(String context);
	
	/**
	 * Access a collection (possibly empty) of all resource types that have been defined and 
	 * enabled in the context (site).
	 * @return
	 */
	public Collection<ResourceType> getTypes(String context);

}
