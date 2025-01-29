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
package org.sakaiproject.profile2.dao;

import java.util.List;

import org.sakaiproject.profile2.hbm.model.ProfileImageExternal;
import org.sakaiproject.profile2.hbm.model.ProfileImageOfficial;
import org.sakaiproject.profile2.hbm.model.ProfileImageUploaded;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.model.SocialNetworkingInfo;

/**
 * Internal DAO Interface for Profile2.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface ProfileDao {

	/**
	 * Get the current ProfileImage records from the database.
	 * There should only ever be one, but in case things get out of sync this returns all.
	 * This method is only used when we are adding a new image as we need to invalidate all of the others
	 * If you are just wanting to retrieve the latest image, see getCurrentProfileImageRecord()
	 *
	 * @param userId		userId of the user
	 */
	public List<ProfileImageUploaded> getCurrentProfileImageRecords(final String userId);
	
	/**
	 * Get the current ProfileImage record from the database.
	 * There should only ever be one, but if there are more this will return the latest. 
	 * This is called when retrieving a profile image for a user. When adding a new image, there is a call
	 * to a private method called getCurrentProfileImageRecords() which should invalidate any multiple current images
	 *
	 * @param userId		userId of the user
	 */
	public ProfileImageUploaded getCurrentProfileImageRecord(final String userId);
	
	/**
	 * Get old ProfileImage records from the database. 
	 * TODO: Used for displaying old the profile pictures album
	 *
	 * @param userId		userId of the user
	 */
	public List<ProfileImageUploaded> getOtherProfileImageRecords(final String userId);

	/**
	 * Get the ProfileImageOfficial record from the database for the given user
	 * @param userUuid		uuid of the user
	 * @return
	 */
	public ProfileImageOfficial getOfficialImageRecordForUser(final String userUuid);
	
	/**
	 * Save the ProfileImageOfficial record the database
	 * @param officialImage		ProfileImageOfficial object
	 * @return
	 */
	public boolean saveOfficialImageUrl(ProfileImageOfficial officialImage);
	
	/**
	 * Get a SocialNetworkingInfo record for a user
	 * @param userId		uuid of the user
	 * @return
	 */
	public SocialNetworkingInfo getSocialNetworkingInfo(final String userId);
	
	/**
	 * Save a SocialNetworkingInfo record
	 * @param socialNetworkingInfo	SocialNetworkingInfo object
	 * @return
	 */
	public boolean saveSocialNetworkingInfo(final SocialNetworkingInfo socialNetworkingInfo);
	
	/**
	 * Add a new profile image record to the database. Invalidates others before it adds itself.
	 *
	 * @param profileImage	ProfileImageUploaded obj	
	 */
	public boolean addNewProfileImage(final ProfileImageUploaded profileImage);
		
	/**
	 * Get a list of uuids for all users that have a SakaiPerson record
	 * @return list of uuids
	 */
	public List<String> getAllSakaiPersonIds();
	
	/**
	 * Get a total count of all users with SakaiPerson records
	 * @return count
	 */
	public int getAllSakaiPersonIdsCount();
	
	/**
	 * Get a ProfileImageExternal record for a user
	 * @param userId uuid of the user
	 * @return
	 */
	public ProfileImageExternal getExternalImageRecordForUser(final String userId);
	
	/**
	 * Save a ProfileImageExternal record
	 * @param externalImage		ProfileImageExternal record
	 * @return
	 */
	public boolean saveExternalImage(final ProfileImageExternal externalImage);
	
	/**
	 * Persist a new ProfilePreferences record and return it.
	 *
	 * @param prefs		complete ProfilePreferences record
	 */
	public ProfilePreferences addNewPreferencesRecord(ProfilePreferences prefs);

	/**
	 * Get a ProfilePreferences record for the user
	 * @param userId	uuid for the user
	 * @return
	 */
	public ProfilePreferences getPreferencesRecordForUser(final String userId);
	
	/**
	 * Save a ProfilePreferences record
	 * @param prefs		ProfilePreferences record
	 * @return
	 */
	public boolean savePreferencesRecord(ProfilePreferences prefs);
	
	/**
	 * Get the ExternalIntegrationInfo record for a user
	 * @param userUuid
	 * @return
	 */
	public ExternalIntegrationInfo getExternalIntegrationInfo(final String userUuid);
	
	/**
	 * Update a user's ExternalIntegrationInfo record
	 * @param info	ExternalIntegrationInfo for the user
	 * @return
	 */
	public boolean updateExternalIntegrationInfo(ExternalIntegrationInfo info);
	
	/**
	 * Invalidate the current profile image for a user.
	 *
	 * @param userUuid	the uuid for the user
	 */
	public boolean invalidateCurrentProfileImage(final String userUuid);
	
	// Hibernate query constants
	final String QUERY_GET_COMPANY_PROFILE = "getCompanyProfile";
	final String QUERY_GET_COMPANY_PROFILES = "getCompanyProfiles";
	
	final String QUERY_GET_CURRENT_PROFILE_IMAGE_RECORD = "getCurrentProfileImageRecord";
	final String QUERY_OTHER_PROFILE_IMAGE_RECORDS = "getOtherProfileImageRecords"; 

	//SakaiPersonMeta
	final String QUERY_FIND_SAKAI_PERSONS_BY_NAME_OR_EMAIL = "findSakaiPersonsByNameOrEmail"; 
	final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST = "findSakaiPersonsByInterest";
	final String QUERY_FIND_SAKAI_PERSONS_BY_INTEREST_AND_BUSINESS_BIO = "findSakaiPersonsByInterestAndBusinessBio";
	final String QUERY_GET_ALL_SAKAI_PERSON_IDS = "getAllSakaiPersonIds";
	final String QUERY_GET_ALL_SAKAI_PERSON_IDS_COUNT = "getAllSakaiPersonIdsCount";

	//ProfileImageOfficial
	final String QUERY_GET_OFFICIAL_IMAGE_RECORD = "getProfileImageOfficialRecord"; 
	
	// Hibernate object fields
	final String USER_UUID = "userUuid";
	final String UUID = "uuid";
	final String ID = "id";
	
	
}
