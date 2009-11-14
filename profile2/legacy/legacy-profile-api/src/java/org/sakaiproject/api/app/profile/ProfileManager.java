/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.api.app.profile;

import java.util.Map;
import java.util.Set;

/**
 * @author rshastri
 */
public interface ProfileManager
{
	public void init();

	public void destroy();

	/**
	 * Returns a map of user IDs to Profiles.
	 * 
	 * @param userIds A collection of user IDs
	 * @return
	 */
	public Map<String, Profile> getProfiles(Set<String> userIds);
	
	/**
	 * Allow user with update access to site view id photo of member users
	 * 
	 * @param uid
	 * @param siteMaintainer
	 * @return
	 */
	public byte[] getInstitutionalPhotoByUserId(String uid, boolean siteMaintainer);

	// Helper methods
	/**
	 * @param profile
	 * @return
	 */
	public boolean displayCompleteProfile(Profile profile);

	/**
	 * @param id
	 * @return
	 */
	public Profile getUserProfileById(String id);

}
