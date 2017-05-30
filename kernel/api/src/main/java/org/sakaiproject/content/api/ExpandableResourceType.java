/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

/**
 * 
 * ExpandableResourceType is an interface used to define special behaviors of custom 
 * resource types that can be expanded.
 *
 */
public interface ExpandableResourceType extends ResourceType
{
	/**
	 * Retrieve a reference for the location of the icon for this type. This should  
	 * be relative to the image library in "/reference/library/src/webapp/image/".  
	 * For example, the plain-text image is "sakai/text.gif";
	 * If null, other info may be used to find an icon, which may be inappropriate.
	 * Implementations of this method should deal with four cases: The collection 
	 * is empty (in which case the icon will not have an expand action associated with it),
	 * the collection is too big to expand (i.e. memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT),
	 * the collection is expanded (in which case the "collapse" action is offered), and the 
	 * collection is collapsed (in which case the "expand" action is offered).
	 * The value returned by this method will be used except for the top-level folders 
	 * within a site.  Top-level folders are expanded by default and cannot be collapsed,
	 * so the single-parameter getIconLocation(ContentEntity) method is used in that
	 * case to get the icon location. 
	 * @param entity The entity for which the icon is needed, or null, especially in
	 * cases where a specific entity has not yet been created. 
	 * @return A path to the icon relative to the root of the image library in
	 * "/reference/library/src/webapp/image/", or null
	 */
	@Deprecated
	public String getIconLocation(ContentEntity entity, boolean expanded);
	
	/**
	 * Same as getIconLocation, but retrieves its font-awesome icon class
	 * 
	 * @param entity The entity for which the icon class is needed, or null, especially in
	 * cases where a specific entity has not yet been created. 
	 * @return A font-awesome icon class, or null
	 */
	public String getIconClass(ContentEntity entity, boolean expanded);
	
	/**
	 * Access a text string suitable for use as a very brief description of the expand/collapse option
	 * for a particular resource.  Implementations of this method should deal with four cases: The collection 
	 * is empty (in which case the icon will not have an expand action associated with it),
	 * the collection is too big to expand (i.e. memberCount > ResourceType.EXPANDABLE_FOLDER_SIZE_LIMIT),
	 * the collection is expanded (in which case the "collapse" action is offered), and the 
	 * collection is collapsed (in which case the "expand" action is offered).
	 * If the string is more than about 40 or 50 characters, it may be truncated at an arbitrary
	 * length.  The string may identify the type of this resource or more specific information. 
	 * If the entity parameter is null, the method should return a more general description of
	 * the the entity (suitable for a new entity during creation dialogs).  The string should be 
	 * localized. In addition, the standard getLocalizedHoverText method should be implemented. 
	 * The text supplied by that method will be used to label the "access" action for the entity.
	 * @param entity The resource that's being displayed, or null indicating that the entity might  
	 * not yet exist.
	 * @param expanded
	 * @return
	 */
	public String getLocalizedHoverText(ContentEntity entity, boolean expanded);
	
	/**
	 * Access an action used to expand a collection when the user clicks an "expand" icon
	 * in the Resources tool or FilePicker helper. 
	 * @return the expand action.
	 */
	public ServiceLevelAction getExpandAction();
	
	/**
	 * Access an action used to collapse a collection when the user clicks an "collapse" icon
	 * in the Resources tool or FilePicker helper. 
	 * @return the collapse action.
	 */
	public ServiceLevelAction getCollapseAction();

	/**
	 * Determine whether users should be able to perform a particular action to create new resources in 
	 * a particular collection.  This provides a way for the type registration to disallow creation of
	 * entities within ContentEntity instances that are of that ResourceType.
	 * @param action An action to create a ContentEntity of a particular ResourceType.
	 * @param entity An instance of a ContentEntity whose type is this ExpandableResourceType. 
	 * @return
	 */
	public boolean allowAddAction(ResourceToolAction action, ContentEntity entity);

}
