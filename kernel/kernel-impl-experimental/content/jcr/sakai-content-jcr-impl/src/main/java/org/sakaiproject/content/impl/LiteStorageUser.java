/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.util.StorageUser;

/**
 * This is a greatly simplified storage user to act as a call back to a storage
 * user It is bound to the Entity model, but does not bind to the serialization
 * mechanism as when the storage mechanism is intialised by constructure an
 * implementation suitable for use with the storage layer will be passed in
 * 
 * @author ieb
 */
public interface LiteStorageUser extends StorageUser
{

	/**
	 * Create a new Resource based on the object passed in
	 * 
	 * @param storageUser
	 * @param source
	 * @return
	 */
	Entity newResource(Object source);

	/**
	 * Create a new Resource suitable to editing
	 * 
	 * @param source
	 * @return
	 */
	Edit newResourceEdit(Object source);

	/**
	 * Convert the Sakai ID to an absolute ID suitable for the internal storage
	 * 
	 * @param id
	 * @return
	 */
	String convertId2Storage(String id);

	/**
	 * Commit the edit to the object passed in
	 * 
	 * @param edit
	 * @param n
	 */
	void commit(Edit edit, Object n);

	/**
	 * @param path
	 * @return
	 */
	String convertStorage2Id(String path);

	/**
	 * @return
	 */
	Iterator<String> startupNodes();

	/**
	 * @param id
	 * @return
	 */
	Entity newContainerEditById(String id);

	/**
	 * @param id
	 * @return
	 */
	Entity newContainerById(String id);

	/**
	 * @param namespaces
	 */
	void setNamespaces(Map<String, String> namespaces);


}
