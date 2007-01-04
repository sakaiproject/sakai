/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.Set;

import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.user.api.User;

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
	
	public static final String MIME_TYPE_TEXT = "text/plain";
	public static final String MIME_TYPE_HTML = "text/html";
	
	public enum ActionType { CREATE, MODIFY_METADATA, MODIFY_CONTENT, DELETE, COPY, DUPLICATE, MOVE, CUSTOM };  


	/**
	 * @param actionId
	 * @return
	 */
	public ResourceToolAction getAction(String actionId);
	
	/**
	 * Access an ordered list of actions (objects of type ResourceToolAction) 
	 * that can be offered to the current user in the Resources tool's list view.
	 * @param entityRef The Reference object for the entity whose actions are requested.
	 * @param permissions The names of the content permissions held by the current user in the resource's containing collection. 
	 * @return
	 */
	public List getActions(Reference entityRef, Set permissions);
	
	/**
	 * Access an ordered list of actions (objects of type ResourceToolAction) 
	 * that can be offered to a particular user in the Resources tool's list view 
	 * with respect to a particular resource of thetype described by this ResourceType object.
	 * @param entityRef The Reference object for the entity whose actions are requested.
	 * @param user The user for which the question is being asked.
	 * @param permissions The names of the content permissions held by the current user in the resource's containing collection. 
	 * @return
	 */
	public List getActions(Reference entityRef, User user, Set permissions);
	
	/**
	 * If the specified user is allowed to create new resources of this type 
	 * in the specified collection, return the ResourceToolAction describing 
	 * the part of the create action that handles the "content" of the resource. 
	 * Throw an UnsupportedOperationException if no create action is defined 
	 * for this type. 
	 * @param collectionRef The Reference object for the collection in which the resource would be created.
	 * @param user The user for which the question is being asked.
	 * @param permissions The names of the content permissions held by the current user in the resource's containing collection. 
	 * @return
	 */
	public ResourceToolAction getCreateAction(Reference collectionRef, User user, Set permissions);
	
	/**
	 * Retrieve a reference for the location of the icon for this type.
	 * If null, the mimetype of the resource will be used to find an icon.
	 * The reference should refer to an icon in the l
	 * @return
	 */
	public String getIconLocation();
	
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
	 * The string should be localized.  If no value is supplied, a default hover-string will be 
	 * used.
	 * @param reference A reference to the resource that's being displayed
	 * @return
	 */
	public String getLocalizedHoverText(Reference reference);
	
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
	 * Determine whether a particular action is enabled for this resource type 
	 * on a particular resource for a particular user.
	 * @param collectionRef The Reference object for the collection in which the resource would be created.
	 * @param user The user for which the question is being asked.
	 * @return true if a create action is allowed, false otherwise.
	 */
	public boolean isActionAllowed(String actionId, Reference entityRef, User user);
	
	/**
	 * Determine whether a create action is enabled for this type on a 
	 * particular collection for the current user. Throw an UnsupportedOperationException 
	 * if no create action is defined for this type.
	 * @param collectionRef The Reference object for the collection in which the resource would be created.
	 * @return true if a create action is allowed, false otherwise.
	 */
	public boolean isCreateActionAllowed(Reference collectionRef);
	
	/**
	 * Determine whether a create action is enabled for this type on a particular 
	 * collection for a particular user. Throw an UnsupportedOperationException 
	 * if no create action is defined for this type.
	 * @param collectionRef The Reference object for the collection in which the resource would be created.
	 * @param user The user for which the question is being asked.
	 * @return true if a create action is allowed, false otherwise.
	 */
	public boolean isCreateActionAllowed(Reference collectionRef, User user);
	
}
