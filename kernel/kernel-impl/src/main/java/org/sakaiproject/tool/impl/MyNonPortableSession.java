/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
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

package org.sakaiproject.tool.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.tool.api.NonPortableSession;

/**
 * NonPortableSession stores a users session data that can not be 'shared'
 * in a Terracotta (or similar) cluster.
 */
public class MyNonPortableSession implements NonPortableSession {

	/** Hold attributes in a Map. TODO: ConcurrentHashMap may be better for multiple writers */
	protected Map<String,Object> m_attributes = new ConcurrentHashMap<String,Object>();

	/**
	 * Returns the object bound with the specified name in this session, or <code>null</code> if no object is bound under the name.
	 * 
	 * @param name
	 *        a string specifying the name of the object
	 * @return the object with the specified name
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	public Object getAttribute(String name) {
		return m_attributes.get(name);
	}

	/**
	 * Removes the object bound with the specified name from this session. If the session does not have an object bound with the specified name, this method does nothing.
	 * <p>
	 * After this method executes, and if the object implements <code>SessionBindingListener</code>, Sakai calls <code>SessionBindingListener.valueUnbound</code>.
	 * 
	 * @param name
	 *        the name of the object to remove from this session
	 * @return the attribute with the specified name if it existed
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	public Object removeAttribute(String name) {
		// remove
		return m_attributes.remove(name);
	}

	/**
	 * Binds an object to this session, using the name specified. If an object of the same name is already bound to the session, the object is replaced.
	 * <p>
	 * After this method executes, and if the new object implements <code>SessionBindingListener</code>, Sakai calls <code>SessionBindingListener.valueBound</code>.
	 * <p>
	 * If an object was already bound to this session of this name that implements <code>SessionBindingListener</code>, its <code>SessionBindingListener.valueUnbound</code> method is called.
	 * <p>
	 * If the value passed in is null, this has the same effect as calling <code>removeAttribute()<code>.
	 *
	 * @param name			the name to which the object is bound;
	 *					cannot be null
	 *
	 * @param value			the object to be bound
	 * @return the old attribute with the specified name if it existed
	 * @exception IllegalStateException	if this method is called on an
	 *					invalidated session
	 */
	public Object setAttribute(String name, Object value) {
		return m_attributes.put(name, value);
	}

	/**
	 * Get all the attributes in this Session.  The returned data structure is a copy of all the attributes in this Session and does not
	 * represent the backing data structure of the Session itself.
	 * @return a new Map object representing the key/value pair of all attributes in this session
	 */
	public Map<String,Object> getAllAttributes() {
		return new HashMap<String,Object>(m_attributes);
	}

	/**
	 * Remove all attributes from this session
	 */
	public void clear() {
		m_attributes.clear();
	}

}
