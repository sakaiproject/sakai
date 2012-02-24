/**********************************************************************************
 * $URL$
 * $Id$
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

package org.sakaiproject.thread_local.api;

/**
 * <p>
 * ThreadLocalManager provides registration and discovery of objects bound to the "current" request processing or thread.
 * </p>
 * <p>
 * Clients who have objects to bind can set/get them at will - the Sakai Framework assures that the entire set of objects bound to current are cleared when the request processing is complete.
 * </p>
 */
public interface ThreadLocalManager
{
	/**
	 * Bind this object under this name with the current thread, or remove if the value is null.
	 * 
	 * @param name
	 *        The binding name.
	 * @param value
	 *        The object to bind, or null to un-bind this name.
	 */
	void set(String name, Object value);

	/**
	 * Remove all objects bound to the current thread.
	 */
	void clear();

	/**
	 * Find the named object bound to the current thread.
	 * 
	 * @param name
	 *        The binding name.
	 * @return The object bound by this name, or null if not found.
	 */
	Object get(String name);
}
