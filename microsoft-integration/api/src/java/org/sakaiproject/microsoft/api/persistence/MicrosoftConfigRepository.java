/**
 * Copyright (c) 2024 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.microsoft.api.persistence;

import java.util.Map;
import java.util.Optional;

import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;
import org.sakaiproject.microsoft.api.model.MicrosoftConfigItem;
import org.sakaiproject.serialization.SerializableRepository;

public interface MicrosoftConfigRepository extends SerializableRepository<MicrosoftConfigItem, String> {
	public static final String PREFIX_SYNCH = "SYNCH:";
	public static final String CREATE_TEAM = PREFIX_SYNCH + "CREATE_TEAM";
	public static final String DELETE_SYNCH = PREFIX_SYNCH + "DELETE_SYNCH";
	public static final String DELETE_TEAM = PREFIX_SYNCH + "DELETE_TEAM";
	public static final String ADD_USER_TO_TEAM = PREFIX_SYNCH + "ADD_USER_TO_TEAM";
	public static final String REMOVE_USER_FROM_TEAM = PREFIX_SYNCH + "REMOVE_USER_FROM_TEAM";
	public static final String CREATE_CHANNEL = PREFIX_SYNCH + "CREATE_CHANNEL";
	public static final String DELETE_CHANNEL = PREFIX_SYNCH + "DELETE_CHANNEL";
	public static final String ADD_USER_TO_CHANNEL = PREFIX_SYNCH + "ADD_USER_TO_CHANNEL";
	public static final String REMOVE_USER_FROM_CHANNEL = PREFIX_SYNCH + "REMOVE_USER_FROM_CHANNEL";
	public static final String REMOVE_USERS_WHEN_UNPUBLISH = PREFIX_SYNCH + "REMOVE_USERS_WHEN_UNPUBLISH";
	public static final String CREATE_INVITATION = PREFIX_SYNCH + "CREATE_INVITATION";
	
	public static final String PREFIX_NEWSITE = "NEWSITE:";
	public static final String NEW_SITE_TYPE = PREFIX_NEWSITE + "TYPE";
	public static final String NEW_SITE_PUBLISHED = PREFIX_NEWSITE + "PUBLISHED";
	public static final String NEW_SITE_PROPERTY = PREFIX_NEWSITE + "PROPERTY";
	public static final String NEW_SITE_SYNC_DURATION = PREFIX_NEWSITE + "SYNC_DURATION";
	
	public static final String PREFIX_JOB = "JOB:";
	public static final String PREFIX_SITE = "SITE:";
	public static final String JOB_SITE_TYPE = PREFIX_JOB + PREFIX_SITE + "TYPE";
	public static final String JOB_SITE_PUBLISHED = PREFIX_JOB + PREFIX_SITE + "PUBLISHED";
	public static final String JOB_SITE_PROPERTY = PREFIX_JOB + PREFIX_SITE + "PROPERTY";
	
	public static final String PREFIX_ONEDRIVE = "ONEDRIVE:";
	public static final String ONEDRIVE_ENABLED = PREFIX_ONEDRIVE + "ENABLED";
	
	public static final String PREFIX_COLLABORATIVE_DOCUMENTS = "COLLABDOCS:";
	public static final String MAX_UPLOAD_SIZE = PREFIX_COLLABORATIVE_DOCUMENTS + "MAX_UPLOAD_SIZE";
	
	Optional<MicrosoftConfigItem> getConfigItemByKey(String key);
	String getConfigItemValueByKey(String key);
	//------------------------------ CREDENTIALS -------------------------------------------------------
	MicrosoftCredentials getCredentials();

	//------------------------------- SAKAI - MICROSOFT USER MAPPING ------------------------------------
	SakaiUserIdentifier getMappedSakaiUserId();
	MicrosoftUserIdentifier getMappedMicrosoftUserId();
	
	//------------------------------- MICROSOFT SYNCHRONIZATION ------------------------------------
	Map<String, MicrosoftConfigItem> getDefaultSynchronizationConfigItems();
	Map<String, MicrosoftConfigItem> getAllSynchronizationConfigItems();
	
	Boolean isAllowedCreateTeam();
	Boolean isAllowedDeleteSynch();
	Boolean isAllowedDeleteTeam();
	Boolean isAllowedAddUserToTeam();
	Boolean isAllowedRemoveUserFromTeam();
	Boolean isAllowedCreateChannel();
	Boolean isAllowedDeleteChannel();
	Boolean isAllowedAddUserToChannel();
	Boolean isAllowedRemoveUserFromChannel();
	Boolean isAllowedRemoveUsersWhenUnpublish();
	Boolean isAllowedCreateInvitation();
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - NEW SITE ------------------------------------
	SakaiSiteFilter getNewSiteFilter();
	long getSyncDuration();
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - JOB ------------------------------------
	SakaiSiteFilter getJobSiteFilter();
}