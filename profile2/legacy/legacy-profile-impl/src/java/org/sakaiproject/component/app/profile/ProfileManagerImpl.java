/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.component.app.profile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.profile.ProfileImpl;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.ProfilePrivacy;
import org.sakaiproject.profile2.model.ResourceWrapper;
import org.sakaiproject.profile2.service.ProfileImageService;
import org.sakaiproject.profile2.service.ProfilePrivacyService;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


public class ProfileManagerImpl implements ProfileManager {
	
	private static final Log log = LogFactory.getLog(ProfileManagerImpl.class);

	/**
 	* {@inheritDoc}
 	*/
	public Profile getUserProfileById(String id){
		
		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(id);
		if (sakaiPerson == null){
			return null;
		}
		
		//transform the profile
		Profile profile = transformSakaiPersontoProfile(sakaiPerson);
		
		//get the privacy object for the user
		//ProfilePrivacy privacy = profilePrivacyService.getProfilePrivacyRecord(id); 
		
		//run the privacy check to blank fields
		
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
		
		//the profile will already be cleaned.
		return true;
	}

	

	


	
   
	/**
 	* {@inheritDoc}
 	*/
	public byte[] getInstitutionalPhotoByUserId(String userId, boolean viewerHasPermission)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getInstitutionalPhoto(" + userId + ")");
		}
		if (userId == null || userId.length() < 1) throw new IllegalArgumentException("Illegal userId argument passed!");

		/* Profile2 modifications */
		ResourceWrapper resource = new ResourceWrapper();
		resource = profileImageService.getProfileImage(userId, ProfileConstants.PROFILE_IMAGE_MAIN);
		return resource.getBytes();
		
		/* Legacy Profile method
		
		SakaiPerson sakaiSystemPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getSystemMutableType());
		SakaiPerson sakaiPerson = sakaiPersonManager.getSakaiPerson(userId, sakaiPersonManager.getUserMutableType());
		Profile profile = null;

		if ((sakaiSystemPerson == null))
		{
			try
			{
				userDirectoryService.getUser(userId);
			}
			catch (UserNotDefinedException unde)
			{
				log.warn("User " + userId + " does not exist. ", unde);
				return null;
			}
			sakaiSystemPerson = sakaiPersonManager.create(userId, sakaiPersonManager.getSystemMutableType());
		}
		Profile systemProfile = new ProfileImpl(sakaiSystemPerson);
		
		// Fetch current users institutional photo for either the user or super user
		if (getCurrentUserId().equals(userId) || SecurityService.isSuperUser() || viewerHasPermission)
		{
			if(log.isDebugEnabled()) log.debug("Official Photo fetched for userId " + userId);
			return systemProfile.getInstitutionalPicture();	
		}

		// if the public information && private information is viewable and user uses to display institutional picture id.
		if (sakaiPerson != null)
		{
			profile = new ProfileImpl(sakaiPerson);
			if (sakaiPerson != null && (profile.getHidePublicInfo() != null)
					&& (profile.getHidePublicInfo().booleanValue() == false) && profile.getHidePrivateInfo() != null
					&& profile.getHidePrivateInfo().booleanValue() == false
					&& profile.isInstitutionalPictureIdPreferred() != null
					&& profile.isInstitutionalPictureIdPreferred().booleanValue() == true)
			{
				if(log.isDebugEnabled()) log.debug("Official Photo fetched for userId " + userId);			
				return systemProfile.getInstitutionalPicture();				
			}

		}
		return null;
		*/
		
	}

	
	
	
	public Map<String, Profile> getProfiles(Set<String> userIds) {
		Map<String, Profile> profiles = new HashMap<String, Profile>();
		if(userIds == null || userIds.isEmpty()) {
			return profiles;
		}

		for(Iterator<String>iter = userIds.iterator(); iter.hasNext();){
			String userId = iter.next();
			profiles.put(userId, getUserProfileById(userId));
		}
		
		if(log.isDebugEnabled()) log.debug("Returning profiles for " + profiles.keySet().size() + " users");
		return profiles;
	}
	
	
	/**
	 * Convenience method to map a SakaiPerson object onto a Profile
	 * 
	 * @param sp 		input SakaiPerson
	 * @return			returns a Profile representation of the SakaiPerson object
	 */
	private Profile transformSakaiPersontoProfile(SakaiPerson sp) {
	
		String userUuid = sp.getAgentUuid();
		
		Profile p = new ProfileImpl();
		
		//transform the fields
		p.setUserId(sp.getAgentUuid());
		p.setNickName(sp.getNickname());
		p.setFirstName(sakaiProxy.getUserFirstName(userUuid));
		p.setLastName(sakaiProxy.getUserLastName(userUuid));
		p.setEmail(sakaiProxy.getUserEmail(userUuid));
		p.setHomepage(sp.getLabeledURI());
		p.setHomePhone(sp.getHomePhone());
		p.setWorkPhone(sp.getTelephoneNumber());
		p.setDepartment(sp.getOrganizationalUnit());
		p.setPosition(sp.getTitle());
		p.setSchool(sp.getCampus());
		p.setRoom(sp.getRoomNumber());
		p.setOtherInformation(sp.getNotes());
		
		//Set the defaults for public and private info. These are not used, stricter privacy settings from Profile2 are instead.
		//However these need to be set so Roster doesn't barf.
		p.setHidePrivateInfo(Boolean.valueOf(false));
		p.setHidePublicInfo(Boolean.valueOf(false));
		
		//set default for this so it uses the value from the byte[] it will look up
		p.setInstitutionalPictureIdPreferred(Boolean.valueOf(false));
		
		return p;
	}
	
	
	/**
	 * Is this profile the current user's profile?
	 * @param profile
	 * @return
	 */
	private boolean isCurrentUserProfile(Profile profile){
		return((profile != null) && StringUtils.equals(profile.getUserId(), sakaiProxy.getCurrentUserId()));
	}
	
	
	
	
	public void init(){
		log.info("init()");
	}

	public void destroy(){
		log.debug("destroy()");
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private SakaiPersonManager sakaiPersonManager;
	public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager) {
		this.sakaiPersonManager = sakaiPersonManager;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	private ProfileImageService profileImageService;
	public void setProfileImageService(ProfileImageService profileImageService) {
		this.profileImageService = profileImageService;
	}
	
	private ProfilePrivacyService profilePrivacyService;
	public void setProfilePrivacyService(ProfilePrivacyService profilePrivacyService) {
		this.profilePrivacyService = profilePrivacyService;
	}
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	
	

	

}
