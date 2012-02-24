/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.section.api.coursemanagement;

/**
 * Models a User for use in the Section Awareness API and the Section Manager Tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface User {
	/**
	 * @return Returns the userUid, the unique ID returned by the authentication facade.
	 */
	public String getUserUid();

	/**
	 * @return Returns the sortName, displayed when users are listed in order (for example,
	 * "Paine, Thomas" or "Wong Kar-Wai")
	 */
	public String getSortName();

    /**
	 * @return Returns the displayId, AKA "campus ID", a human-meaningful UID for the user (for
	 * example, a student ID number or an institutional email address)
	 */
	public String getDisplayId();

	/**
	 * @return Returns the displayName, displayed when only this user is being referred to
	 * (for example, "Thomas Paine" or "Wong Kar-Wai")
	 */
	public String getDisplayName();

}
