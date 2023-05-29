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
	public static final String DELETE_TEAM = PREFIX_SYNCH + "DELETE_TEAM";
	public static final String ADD_USER_TO_TEAM = PREFIX_SYNCH + "ADD_USER_TO_TEAM";
	public static final String REMOVE_USER_FROM_TEAM = PREFIX_SYNCH + "REMOVE_USER_FROM_TEAM";
	public static final String CREATE_CHANNEL = PREFIX_SYNCH + "CREATE_CHANNEL";
	public static final String DELETE_CHANNEL = PREFIX_SYNCH + "DELETE_CHANNEL";
	public static final String ADD_USER_TO_CHANNEL = PREFIX_SYNCH + "ADD_USER_TO_CHANNEL";
	public static final String REMOVE_USER_FROM_CHANNEL = PREFIX_SYNCH + "REMOVE_USER_FROM_CHANNEL";
	public static final String CREATE_INVITATION = PREFIX_SYNCH + "CREATE_INVITATION";

	public static final String PREFIX_NEWSITE = "NEWSITE:";
	public static final String SITE_TYPE = PREFIX_NEWSITE + "TYPE";
	public static final String SITE_PUBLISHED = PREFIX_NEWSITE + "PUBLISHED";
	
	public static final String PREFIX_ONEDRIVE = "ONEDRIVE:";
	public static final String ONEDRIVE_ENABLED = PREFIX_ONEDRIVE + "ENABLED";
	
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
	Boolean isAllowedDeleteTeam();
	Boolean isAllowedAddUserToTeam();
	Boolean isAllowedRemoveUserFromTeam();
	Boolean isAllowedCreateChannel();
	Boolean isAllowedDeleteChannel();
	Boolean isAllowedAddUserToChannel();
	Boolean isAllowedRemoveUserFromChannel();
	Boolean isAllowedCreateInvitation();
	
	//------------------------------- MICROSOFT SYNCHRONIZATION - NEW SITE ------------------------------------
	public SakaiSiteFilter getNewSiteFilter();
}