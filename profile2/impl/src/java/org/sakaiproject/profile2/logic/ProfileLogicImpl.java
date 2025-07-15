/**
 * Copyright (c) 2008-2012 The Sakai Foundation
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
package org.sakaiproject.profile2.logic;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.BasicPerson;
import org.sakaiproject.profile2.model.MimeTypeByteArray;
import org.sakaiproject.profile2.model.Person;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.user.api.User;

/**
 * Implementation of ProfileLogic for Profile2.
 *
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
@Slf4j
public class ProfileLogicImpl implements ProfileLogic {

	@Override
	public UserProfile getUserProfile(final String userUuid) {
	    return getUserProfile(userUuid, null);
    }

	@Override
	public UserProfile getUserProfile(final String userUuid, final String siteId) {

		//check auth and get currentUserUuid
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in to get a UserProfile.");
		}

		//get User
		User u = sakaiProxy.getUserById(userUuid);
		if(u == null) {
			log.error("User " + userUuid + " does not exist.");
			return null;
		}

		//setup obj
		UserProfile p = new UserProfile();
		p.setUserUuid(userUuid);
		p.setDisplayName(u.getDisplayName());
		p.setImageUrl(imageLogic.getProfileImageEntityUrl(userUuid, ProfileConstants.PROFILE_IMAGE_MAIN));
		p.setImageThumbUrl(imageLogic.getProfileImageEntityUrl(userUuid, ProfileConstants.PROFILE_IMAGE_THUMBNAIL));

		SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userUuid);
		if (sakaiPerson == null) {
			sakaiPerson = sakaiProxy.createSakaiPerson(userUuid);
			if (sakaiPerson == null) {
				return p;
			}
		}

		//transform
		p = transformSakaiPersonToUserProfile(p, sakaiPerson);

		if(StringUtils.equals(userUuid, currentUserUuid) || sakaiProxy.isSuperUser()) {
			p.setEmail(u.getEmail());
			p.setSocialInfo(getSocialNetworkingInfo(userUuid));
			return p;
		}

		//ADD email if allowed, REMOVE contact info if not
        if(siteId != null && sakaiProxy.isUserAllowedInSite(currentUserUuid, ProfileConstants.ROSTER_VIEW_EMAIL, siteId)) {
			p.setEmail(u.getEmail());
        }

        p.setSocialInfo(getSocialNetworkingInfo(userUuid));

		return p;
	}

	@Override
	public boolean saveUserProfile(SakaiPerson sp) {

		if(sakaiProxy.updateSakaiPerson(sp)) {
			sendProfileChangeEmailNotification(sp.getAgentUuid());

			return true;
		}

		return false;
	}

	@Override
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId) {

		if(userId == null){
	  		throw new IllegalArgumentException("Null argument in ProfileLogic.getSocialNetworkingInfo");
	  	}

		SocialNetworkingInfo socialNetworkingInfo = dao.getSocialNetworkingInfo(userId);
		if (null == socialNetworkingInfo) {
			socialNetworkingInfo = new SocialNetworkingInfo(userId);
		}

		return socialNetworkingInfo;
	}

	@Override
	public boolean saveSocialNetworkingInfo(SocialNetworkingInfo socialNetworkingInfo) {

		if(dao.saveSocialNetworkingInfo(socialNetworkingInfo)) {
			log.info("Updated social networking info for user: " + socialNetworkingInfo.getUserUuid());
			return true;
		}

		return false;
	}

	@Override
	public BasicPerson getBasicPerson(String userUuid) {
		return getBasicPerson(sakaiProxy.getUserById(userUuid));
	}

	@Override
	public BasicPerson getBasicPerson(User user) {
		BasicPerson p = new BasicPerson();
		p.setUuid(user.getId());
		p.setDisplayName(user.getDisplayName());
		p.setType(user.getType());
		return p;
	}

	@Override
	public List<BasicPerson> getBasicPersons(List<User> users) {
		List<BasicPerson> list = new ArrayList<BasicPerson>();
		for(User u:users){
			list.add(getBasicPerson(u));
		}
		return list;
	}

	@Override
	public Person getPerson(String userUuid) {
		return getPerson(sakaiProxy.getUserById(userUuid));
	}

	@Override
	public Person getPerson(User user) {
		//catch for non existent user
		if(user == null){
			return null;
		}
		Person p = new Person();
		String userUuid = user.getId();
		p.setUuid(userUuid);
		p.setDisplayName(user.getDisplayName());
		p.setType(user.getType());
		p.setProfile(getUserProfile(userUuid));

		return p;
	}

	@Override
	public List<Person> getPersons(List<User> users) {
		List<Person> list = new ArrayList<>();
		for (User u : users) {
			list.add(getPerson(u));
		}
		return list;
	}

	@Override
	public List<String> getAllSakaiPersonIds() {
		return dao.getAllSakaiPersonIds();
	}

	@Override
	public int getAllSakaiPersonIdsCount() {
		return dao.getAllSakaiPersonIdsCount();
	}

	//service init
	public void init() {
		log.info("Profile2: init()");
	}

	/**
	 * Convenience method to map a SakaiPerson object onto a UserProfile object
	 *
	 * @param sp 		input SakaiPerson
	 * @return			returns a UserProfile representation of the SakaiPerson object
	 */
	private UserProfile transformSakaiPersonToUserProfile(UserProfile p, SakaiPerson sp) {

		//map fields from SakaiPerson to UserProfile

		//basic info
		p.setNickname(sp.getNickname());
		p.setPhoneticPronunciation(sp.getPhoneticPronunciation());

		//contact info
		p.setMobilephone(sp.getMobile());

		return p;
	}


	/**
	 * Sends an email notification when a user changes their profile, if enabled.
	 * @param toUuid		the uuid of the user who changed their profile
	 */
	private void sendProfileChangeEmailNotification(final String userUuid) {

		//check if option is enabled
		boolean enabled = Boolean.valueOf(sakaiProxy.getServerConfigurationParameter("profile2.profile.change.email.enabled", "false"));
		if(!enabled) {
			return;
		}

		//get the user to send to. THis will be translated into an internal ID. Since SakaiProxy.sendEmail takes a userId as a param
		//it was easier to require an eid here (and thus the person needs to have an account) rather than create a new method that takes an email address.
		String eidTo = sakaiProxy.getServerConfigurationParameter("profile2.profile.change.email.eid", null);
		if(StringUtils.isBlank(eidTo)){
			log.error("Profile change email notification is enabled but no user eid to send it to is set. Please set 'profile2.profile.change.email.eid' in sakai.properties");
			return;
		}

		//get internal id for this user
		String userUuidTo = sakaiProxy.getUserIdForEid(eidTo);
		if(StringUtils.isBlank(userUuidTo)) {
			log.error("Profile change email notification is setup with an invalid eid. Please adjust 'profile2.profile.change.email.eid' in sakai.properties");
			return;
		}

		return;
	}

	@Override
	public String getUserNamePronunciationResourceId(String uuid) {
		final String slash = Entity.SEPARATOR;
		final StringBuilder path = new StringBuilder();
		path.append(slash);
		path.append("private");
		path.append(slash);
		path.append("namePronunciation");
		path.append(slash);
		path.append(uuid);
		path.append(".wav");
		return path.toString();
	}

	@Override
	public MimeTypeByteArray getUserNamePronunciation(String uuid) {
		String resourceId = this.getUserNamePronunciationResourceId(uuid);
		return sakaiProxy.getResource(resourceId);
	}

	@Setter
	private SakaiProxy sakaiProxy;

	@Setter
	private ProfileDao dao;

	@Setter
	private ProfileImageLogic imageLogic;
}
