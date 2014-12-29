/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/StorageUser.java $
 * $Id: StorageUser.java 74692 2010-03-16 13:58:08Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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
package org.sakaiproject.util;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.w3c.dom.Element;

/**
 * <p>
 * SingleStorageUser is an interface that the Storage implementations use
 * to create new Entity instances. In all classes implementing this interface and not
 * {@link DoubleStorageUser} they should expect the container to always be <code>null</code>.
 * </p>
 * <p>
 * This class could have been refactored further so that it's calls didn't have the containing
 * entity and so didn't get passed <code>null</code>, but that would have broken more code.
 * </p>
 * %%% callbacks from the BaseXmlFileStorage and other Storage classes...
 * @see DoubleStorageUser
 *
 */
public interface SingleStorageUser {

	/**
	 * Construct a new resource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	Entity newResource(Entity container, String id, Object[] others);

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	Entity newResource(Entity container, Element element);

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	Entity newResource(Entity container, Entity other);

	/**
	 * Construct a new resource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	Edit newResourceEdit(Entity container, String id, Object[] others);

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	Edit newResourceEdit(Entity container, Element element);

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	Edit newResourceEdit(Entity container, Entity other);

	/**
	 * Collect the fields that need to be stored outside the XML (for the resource).
	 * 
	 * @return An array of field values to store in the record outside the XML (for the resource).
	 * <code>null</code> is accepted and means nothing needs storing as a field.
	 */
	Object[] storageFields(Entity r);

}
