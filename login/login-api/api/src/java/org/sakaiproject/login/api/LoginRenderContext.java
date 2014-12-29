/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation.
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
package org.sakaiproject.login.api;


/**
 * Knock off on {@link org.sakaiproject.portal.api.PortalRenderContext}
 * 
 * @author jrenfro
 *
 */
public interface LoginRenderContext {

	/**
	 * Set a value against a Key, normally a value might be a String,
	 * Collection or a Map, but depending on the render engine technology other
	 * objects may be acceptable.
	 * 
	 * @param string
	 * @param value
	 */
	void put(String string, Object value);

	/**
	 * Convert the render context to a string suitable for dumping to a log file
	 * or console.
	 * 
	 * @return
	 */
	String dump();

	/**
	 * Return true if the context needs this part of the portal
	 * 
	 * @param includeOption
	 * @return
	 */
	boolean uses(String includeOption);

	/**
	 * Get the render engine associated with this context.
	 * 
	 * @return
	 */
	LoginRenderEngine getRenderEngine();
	
}
