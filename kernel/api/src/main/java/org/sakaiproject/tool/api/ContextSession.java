/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.tool.api;

import java.util.Enumeration;

/**
 * <p>
 * ContextSession models an end user's HttpSession for a context (as in ServletContext).
 * </p>
 */
public interface ContextSession
{

	/**
	 * Remove and unbind all attributes in the session.
	 * 
	 * @exception IllegalStateException
	 *            if this method is called on an already invalidated session
	 */
	void clearAttributes();

	/**
	 * Returns the object bound with the specified name in this session, or <code>null</code> if no object is bound under the name.
	 * 
	 * @param name
	 *        a string specifying the name of the object
	 * @return the object with the specified name
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	Object getAttribute(String name);

	/**
	 * Returns an <code>Enumeration</code> of <code>String</code> objects containing the names of all the objects bound to this session.
	 * 
	 * @return an <code>Enumeration</code> of <code>String</code> objects specifying the names of all the objects bound to this session
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	Enumeration<String> getAttributeNames();

	/**
	 * Returns the time when this session was created, measured in milliseconds since midnight January 1, 1970 GMT.
	 * 
	 * @return a <code>long</code> specifying when this session was created, expressed in milliseconds since 1/1/1970 GMT
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	long getCreationTime();

	/**
	 * Returns a string containing the unique identifier assigned to this session.
	 * 
	 * @return a string specifying the identifier assigned to this session
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	String getId();

	/**
	 * Returns the last time the client sent a request associated with this tool session, as the number of milliseconds since midnight January 1, 1970 GMT.
	 * <p>
	 * Actions that your application takes, such as getting or setting a value associated with the session, do not affect the access time.
	 * 
	 * @return a <code>long</code> representing the last time the client sent a request associated with this session, expressed in milliseconds since 1/1/1970 GMT
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	long getLastAccessedTime();

	/**
	 * Returns a string containing the context identifier for this session.
	 * 
	 * @return a string specifying the context identifier for this session.
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	String getContextId();

	/**
	 * Return the enterprise id of the user associated with this session.
	 * 
	 * @return The enterprise id of the user associated with this session.
	 */
	String getUserEid();

	/**
	 * Return the authenticated user id associated with this session.
	 * 
	 * @return The authenticated user id associated with this session.
	 */
	String getUserId();

	/**
	 * Removes the object bound with the specified name from this session. If the session does not have an object bound with the specified name, this method does nothing.
	 * <p>
	 * After this method executes, and if the object implements <code>SessionBindingListener</code>, Sakai calls <code>SessionBindingListener.valueUnbound</code>.
	 * 
	 * @param name
	 *        the name of the object to remove from this session
	 * @exception IllegalStateException
	 *            if this method is called on an invalidated session
	 */
	void removeAttribute(String name);

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
	 *
	 * @exception IllegalStateException	if this method is called on an
	 *					invalidated session
	 */
	void setAttribute(String name, Object value);
}
