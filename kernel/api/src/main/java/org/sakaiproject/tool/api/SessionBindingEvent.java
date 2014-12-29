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

/**
 * <p>
 * Events of this type are either sent to an object that implements {@link SessionBindingListener}when it is bound or unbound from a session.
 * </p>
 * <p>
 * (Based on HttpSessionBindingEvent from the Servlet API).
 * </p>
 */
public interface SessionBindingEvent
{
	/**
	 * Returns the name with which the attribute is bound to or unbound from the session.
	 * 
	 * @return a string specifying the name with which the object is bound to or unbound from the session
	 */
	String getName();

	/**
	 * Return the session that bound or unbound the attribute value.
	 * 
	 * @return The Session object that bound or unbound the attribute value.
	 */
	Session getSession();

	/**
	 * Returns the value of the attribute that has been added, removed or replaced. If the attribute was added (or bound), this is the value of the attribute. If the attribute was removed (or unbound), this is the value of the removed attribute. If the
	 * attribute was replaced, this is the old value of the attribute.
	 * 
	 * @return The value of the attribute being bound or unbound.
	 */
	Object getValue();
}
