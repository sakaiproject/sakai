/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.legacy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.profile2.logic.ProfileImageLogic;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfileImage;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

/**
 * This is the implementation of the original ProfileManager, but modified to get its data from Profile2.
 * 
 * You can activate this in sakai.properties via:
 * profile.manager.integration.bean=org.sakaiproject.profile.legacy.ProfileManager 
 * 
 * Note it will not allow you to write any data, only retrieve, so is useful for integrations
 * that use the existing ProfileManager, eg Roster, YAFT, etc and you want to be compatible with both Profile and Profile2.
 *
 */
public class ProfileManagerImpl implements ProfileManager {
	
	private static final Log log = LogFactory.getLog(ProfileManagerImpl.class);

	/**
 	* {@inheritDoc}
 	*/
	public Profile getUserProfileById(String userId){
		
		UserProfile userProfile = profileLogic.getUserProfile(userId);
		
		//PRFL-475 Roster may send the userId of a non existent user
		if(userProfile == null) {
			return null;
		}
		
		//transform the profile
		Profile profile = transformUserProfiletoLegacyProfile(userProfile);
		
		//get the image record. If we have a url, add it in to the Profile.pictureUrl field
		ProfileImage image = getProfileImage(userId);
		if(StringUtils.isNotBlank(image.getUrl())){
			profile.setPictureUrl(image.getUrl());
		}
		
		//return
		return profile;
		
	}

	/**
 	* {@inheritDoc}
 	*/
	public boolean displayCompleteProfile(Profile profile){
		
		if (profile == null){
			return false;
		}
    	
    	//deny if request is coming from a site with roster (and) WITHOUT "roster.viewprofile" permission (PRFL-394)
		String currentSiteId = sakaiProxy.getCurrentSiteId();
		if(!sakaiProxy.isUserMyWorkspace(currentSiteId)
    			&& !sakaiProxy.isUserAllowedInSite(sakaiProxy.getCurrentUserId(), "roster.viewprofile", currentSiteId)) {
    		return false;
    	}
		
		//the profile will already be cleaned by the Profile2 Privacy methods.
		return true;
	}

	


	/**
 	* {@inheritDoc}
 	*/
	public byte[] getInstitutionalPhotoByUserId(String userId) {
		
		if (StringUtils.isBlank(userId)) {
			throw new IllegalArgumentException("Illegal userId argument passed!");
		}

		ProfileImage image = getProfileImage(userId);
		return image.getBinary();
	}


	
   
	/**
 	* {@inheritDoc}
 	*/
	public byte[] getInstitutionalPhotoByUserId(String userId, boolean viewerHasPermission) {
		return getInstitutionalPhotoByUserId(userId);
	}

	
	
	/**
 	* {@inheritDoc}
 	*/
	public Map<String, Profile> getProfiles(Set<String> userIds) {
		Map<String, Profile> profiles = new HashMap<String, Profile>();
		if(userIds == null || userIds.isEmpty()) {
			return profiles;
		}

		for(Iterator<String>iter = userIds.iterator(); iter.hasNext();){
			String userId = iter.next();
			
			//PRFL-475 skip nulls
			Profile profile = getUserProfileById(userId);
			if(profile != null) {
				profiles.put(userId, profile);
			}
		}
		
		return profiles;
	}
	
	/**
	 * Convenience method to get the ProfileImage record for us.
	 * @param userUuid	uuid of the user.
	 * @return
	 */
	private ProfileImage getProfileImage(String userUuid) {
		
		//PRFL-428 pass siteId so we check perms of user in site as well
		String currentSiteId = sakaiProxy.getCurrentSiteId();
		
		return imageLogic.getProfileImage(userUuid, null, null, ProfileConstants.PROFILE_IMAGE_MAIN, currentSiteId);
	}
	
	/**
	 * Convenience method to map a UserProfile object onto a legacy Profile object
	 * 
	 * @param sp 		input SakaiPerson
	 * @return			returns a Profile representation of the SakaiPerson object
	 */
	private Profile transformUserProfiletoLegacyProfile(UserProfile up) {
	
		String userUuid = up.getUserUuid();
		
		Profile p = new ProfileImpl();
		
		//transform the fields
		p.setUserID(userUuid);
		p.setNickName(up.getNickname());
		p.setFirstName(sakaiProxy.getUserFirstName(userUuid));
		p.setLastName(sakaiProxy.getUserLastName(userUuid));
		p.setEmail(sakaiProxy.getUserEmail(userUuid));
		p.setHomepage(up.getHomepage());
		p.setHomePhone(up.getHomephone());
		p.setWorkPhone(up.getWorkphone());
		p.setDepartment(up.getDepartment());
		p.setPosition(up.getPosition());
		p.setSchool(up.getSchool());
		p.setRoom(up.getRoom());
		p.setOtherInformation(up.getPersonalSummary());
		
		//Set the defaults for public and private info. These are not used, stricter privacy settings from Profile2 are instead.
		//However these need to be set so Roster doesn't barf.
		p.setHidePrivateInfo(Boolean.valueOf(false));
		p.setHidePublicInfo(Boolean.valueOf(false));
		
		//set to true so we can get the uploaded image from Profile2
		//this won't work too well if an institution uses official image urls or allows a user to specify a url.
		//This inter-tool dependency really needs to be replaced with EB, which will fix this.
		p.setInstitutionalPictureIdPreferred(Boolean.valueOf(true));
		
		return p;
	}
	
	
	/**
	 * Is this profile the current user's profile?
	 * @param profile
	 * @return
	 */
	public boolean isCurrentUserProfile(Profile profile){
		return((profile != null) && StringUtils.equals(profile.getUserId(), sakaiProxy.getCurrentUserId()));
	}
	
	
	
	
	public void init(){
		log.info("Profile2's LegacyProfileManager: init()");
	}

	public void destroy(){
		log.debug("Profile2's LegacyProfileManager: destroy()");
	}
	
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}
	
	private ProfileImageLogic imageLogic;
	public void setImageLogic(ProfileImageLogic imageLogic) {
		this.imageLogic = imageLogic;
	}


	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}

	
	/** additional methods from original profile API which we don't need real implementations of as they are specific to the Profile tool */
	public List findProfiles(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Profile getProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDisplayNoPhoto(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDisplayPictureURL(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDisplayUniversityPhoto(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDisplayUniversityPhotoUnavailable(Profile arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShowSearch() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShowTool() {
		// TODO Auto-generated method stub
		return false;
	}

	public void save(Profile arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	

	

}
