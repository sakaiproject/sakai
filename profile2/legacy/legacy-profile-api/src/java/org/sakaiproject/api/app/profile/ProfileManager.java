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
 * This is the legacy ProfileManager API, adapted to work in Profile2.
 * <p>DO NOT USE THIS FOR NEW TOOLS. This is for legacy tool support only, 
 * ie tools that already bind to the original Profile API and need to also work in Profile2.</p>
 * <p>
 * Notes:<br />
 * ------<br />
 * Official photo used to be loaded into the JPEG_PHOTO field in SakaiPerson, and thus available as a byte[].<br />
 * You could only ever set a URL to your personal profile picture. Thus for Roster, the Profile pictures are a URL only.
 * </p>
 * <p>
 * How this is adapted for Profile2.<br />
 * --------------------------------<br />
 * In profile2, you can have only one active profile image at a time. Uploaded images are stored in ContentHosting and URLs are stored as a record
 * in PROFILE_IMAGES_EXTERNAL_T. If an institution needs to control this, the best and easiest way is to
 * add a record to PROFILE_IMAGES_EXTERNAL_T and lock down the ability for user's change the image via the Profile2 sakai.properties
 * </p>
 * <p>
 * So, Roster will be looking at the byte[] field for an official photo and the url field for a profile image.<br />
 * Profile2 has a ResourceWrapper class which wraps a binary resource. The byte[] is there and the URL is created via EntityBroker.<br />
 * These values are returned in the methods below. So Roster always gets the same value for Official and profile pictures.
 * </p>
 * 
 */
public interface ProfileManager
{
	
	/**
	 * This is the main method that is used to get a user's profile. It simply calls {@link org.sakaiproject.profile2.service.ProfileService.getLegacyUserProfile(String userId)}.
	 * That returns a UserProfile object, so it is then transformed into a compatible Profile object for use here.
	 * @param userId
	 * @return
	 */
	public Profile getUserProfileById(String userId);
	

	/**
	 * Returns a map of user IDs to Profiles. Simply calls getUserProfileById() for each userId
	 * 
	 * @param userIds
	 * @return
	 */
	public Map<String, Profile> getProfiles(Set<String> userIds);
	
	/**
	 * Gets the photo as a byte[] for a user. Ignores siteMaintainer field as the stricter ProfilePrivacy methods are used instead
	 * 
	 * @param userId
	 * @param siteMaintainer
	 * @return
	 */
	public byte[] getInstitutionalPhotoByUserId(String userId, boolean siteMaintainer);

	/**
	 * Always returns true, the profile is cleaned before it is returned.
	 * @param profile
	 * @return
	 */
	public boolean displayCompleteProfile(Profile profile);

	
	public void init();

	public void destroy();
	

}
