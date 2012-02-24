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

import java.util.EventListener;

/**
 * <p>
 * Causes an object to be notified when it is bound to or unbound from a session. The object is notified by an {@link SessionBindingEvent}object. This may be as a result of a programmer explicitly unbinding an attribute from a session, due to a session
 * being invalidated, or due to a session timing out.
 * </p>
 * <p>
 * (Based on HttpSessionBindingListener from the Servlet API).
 * </p>
 */
public interface SessionBindingListener extends EventListener
{
	/**
	 * Notifies the object that it is being bound to a session.
	 * 
	 * @param event
	 *        the event that identifies the session
	 */
	void valueBound(SessionBindingEvent event);

	/**
	 * Notifies the object that it is being unbound from a session.
	 * 
	 * @param event
	 *        the event that identifies the session
	 */
	void valueUnbound(SessionBindingEvent event);
}
