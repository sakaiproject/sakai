/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
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
package org.sakaiproject.tool.api;

/**
 * SessionStore it a mix-in interface for use most commonly with SessionManager.
 * This interface is to represent the ability to manipulate the underlying storage
 * of Sessions that the SessionManager is managing. 
 * 
 * @author holdorph
 */
public interface SessionStore {

	/**
	 * Remove the Session corresponding to this id from the
	 * Session storage.
	 * 
	 * @param id the session identifier
	 */
	public void remove(String id);

	/**
	 * Checks the current Tool ID to determine if this tool is marked for clustering.
	 * 
	 * @return true if the tool is marked for clustering, false otherwise.
	 */
	public boolean isCurrentToolClusterable();
}
