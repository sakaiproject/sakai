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

import java.util.List;

/**
 * 
 * 
 */
public interface ResourceType 
{
	public static final String TYPE_TEXT = "org.sakaiproject.content.types.TextDocumentType";
	public static final String TYPE_HTML = "org.sakaiproject.content.types.HtmlDocumentType";
	public static final String TYPE_URL = "org.sakaiproject.content.types.urlResource";
	public static final String TYPE_UPLOAD = "org.sakaiproject.content.types.fileUpload";
	public static final String TYPE_FOLDER = "org.sakaiproject.content.types.folder";
	public static final String TYPE_METAOBJ = "org.sakaiproject.metaobj.shared.FormHelper";

	public static final String MIME_TYPE_TEXT = "text/plain";
	public static final String MIME_TYPE_HTML = "text/html";
	public static final String MIME_TYPE_METAOBJ = "application/x-osp";
	public static final String MIME_TYPE_URL = "text/url";
	
	public static final int EXPANDABLE_FOLDER_SIZE_LIMIT = 256;
	
	public static final int MAX_LENGTH_SHORT_SIZE_LABEL = 18;
	public static final int MAX_LENGTH_LONG_SIZE_LABEL = 80;

	/**
	 * Access the action specified by the parameter.  If the action is defined for this resource type,
	 * it is returned.  Otherwise, the method returns null.
	 * @param type
	 * @return
	 */
	public List<ResourceToolAction> getActions(ResourceToolAction.ActionType type);

	/**
	 * Access an ordered list of actions specified by the parameter.  The actions are added to the list
	 * in the order specified by the parametric list.  If none of the action types specified in the list
	 * are defined for this resource type, the returned list will be empty.
	 * @param types
	 * @return
	 */
	public List<ResourceToolAction> getActions(List<ResourceToolAction.ActionType> types);

	/**
	 * @param actionId
	 * @return
	 */
	public ResourceToolAction getAction(String actionId);
	
	/**
	 * Retrieve a reference for the location of the icon for this type. This should  
	 * be relative to the image library in "/reference/library/src/webapp/image/".  
	 * For example, the plain-text image is "sakai/text.gif";
	 * If null, the mimetype of the resource or other info may be used to find an icon,
	 * which may be inappropriate for a particular resource type. In the case of folders
	 * and other expandable resources, the value returned by this method will be used 
	 * for top-level folders within a site because, those folders are expanded by default. 
	 * 
	 * @param entity The entity for which the icon is needed, or null, especially in
	 * cases where a specific entity has not yet been created. 
	 * @return A path to the icon relative to the root of the image library in
	 * "/reference/library/src/webapp/image/", or null
	 */
	@Deprecated
	public String getIconLocation(ContentEntity entity);
	
	/**
	 * Same as getIconLocation, but retrieves its font-awesome icon class
	 * 
	 * @param entity The entity for which the class is needed, or null, especially in
	 * cases where a specific entity has not yet been created. 
	 * @return A font-awesome icon class, or null
	 */
	public String getIconClass(ContentEntity entity);
	
	// TODO: types should be able to opt-out of some "properties"
	// "property" categories: title (always required), description, copyright/licensing, access (groups, public), email-notification, availability  
	/**
	 * Access the identifier for this type (which must be unique within the registry and must be limited to alphnumeric characters).
	 * @return
	 */
	public String getId();
	
	/**
	 * @return
	 */
	public String getLabel(); 
	
	/**
	 * Access a text string suitable for use as a very brief description of a particular resource.
	 * If the string is more than about 40 or 50 characters, it may be truncated at an arbitrary
	 * length.  The string may identify the type of this resource or more specific information. 
	 * If the entity parameter is null, the method should return a more general description of
	 * the the entity (suitable for a new entity during creation dialogs).  The string should be 
	 * localized. If no return value is supplied, a default hover-string will be used.
	 * @param entity The resource that's being displayed, or null indicating that the entity might  
	 * not yet exist.
	 * @return
	 */
	public String getLocalizedHoverText(ContentEntity entity);
	
	/**
	 * Access a short string that describes the "size" of an entity in some appropriate way.  For
	 * example, the "size" of a folder might be the number of items it contains, the "size" of a text
	 * document might be the number of bytes it contains, and the "size" of a audio file might be its
	 * playing time at a normal speed.  If the method returns null, the Resources tool will display 
	 * the number of bytes if the entity is a ContentResource or the number of members if the entity
	 * is a ContentCollection.  The string must be short.  If it's longer than about 18 characters, 
	 * it may be truncated arbitrarily, which may render it meaningless to users.
	 * @param entity The resource that's being displayed, or null indicating that the entity might  
	 * not yet exist.
	 * @return A short string describing the size of an entity, or null. 
	 * @see ResourceType#MAX_LENGTH_SHORT_SIZE_LABEL
	 */
	public String getSizeLabel(ContentEntity entity);
	
	/**
	 * Access a short string that describes the "size" of an entity in some appropriate way.  For
	 * example, the "size" of a folder might be the number of items it contains, the "size" of a text
	 * document might be the number of bytes it contains, and the "size" of a audio file might be its
	 * playing time at a normal speed.  If the method returns null, the Resources tool will display 
	 * the number of bytes if the entity is a ContentResource or the number of members if the entity
	 * is a ContentCollection.  The string must be short.  If it's longer than about 80 characters, 
	 * it may be truncated arbitrarily, which may render it meaningless to users.
	 * @param entity The resource that's being displayed, or null indicating that the entity might  
	 * not yet exist.
	 * @return A short string describing the size of an entity, or null. 
	 * @see ResourceType#MAX_LENGTH_LONG_SIZE_LABEL
	 */
	public String getLongSizeLabel(ContentEntity entity);

	/**
	 * Should the Resources tool support hiding and scheduled release and/or retraction for items of this type?
	 * @return true if availability is included among the resource properties in the UI, false otherwise.
	 */
	public boolean hasAvailabilityDialog();
	
	/**
	 * Should the Resources tool elicit a description for items of this type?
	 * @return true if a description is included among the resource properties in the UI, false otherwise.
	 */
	public boolean hasDescription();
	
	/**
	 * Should the Resources tool support access by groups for items of this type?
	 * @return true if access by groups is included among the resource properties in the UI, false otherwise.
	 */
	public boolean hasGroupsDialog();
	
	/**
	 * Should the Resources tool support optional email notification for items of this type?
	 * @return true if email-notification is included among the resource properties in the UI, false otherwise.
	 */
	public boolean hasNotificationDialog();
	
	/**
	 * Should the Resources tool allow specification of "optional properties" (usually Dublin Core tags) for items of this type?
	 * @return true if optional properties form is included among the resource properties in the UI, false otherwise.
	 */
	public boolean hasOptionalPropertiesDialog();
	
	/**
	 * Should the Resources tool support making items of this type public?
	 * @return true if public access is included among the resource properties in the UI, false otherwise.
	 */
	public boolean hasPublicDialog();
	
	/**
	 * Should the Resources tool elicit copyright/licensing for items of this type?
	 * @return true if the copyright/licensing dialog is included among the resource properties in the UI, false otherwise.
	 */
	public boolean hasRightsDialog();
	
	/**
	 * Indicates whether this resource type is one for which an "expand" action is available.
	 * If this method returns true, the ResourceType registration must implement the ExpandableResourceType
	 * interface and provide action definitions of type "expand" and "collapse".
	 * @return
	 */
	public boolean isExpandable();

}
