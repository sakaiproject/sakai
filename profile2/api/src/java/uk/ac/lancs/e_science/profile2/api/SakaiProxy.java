package uk.ac.lancs.e_science.profile2.api;

import java.util.LinkedHashMap;

import org.sakaiproject.api.common.edu.person.SakaiPerson;

public interface SakaiProxy {
	
	//part or parth for where a profile image is uplaoded to depending on its type
	public static final int PROFILE_IMAGE_MAIN = 1;		
	public static final int PROFILE_IMAGE_THUMBNAIL = 2;
	
	
	public String getCurrentSiteId();
	
	public String getCurrentUserId();
	
	public String getUserEid(String userId);

	public String getUserDisplayName(String userId);
	
	public String getUserEmail(String userId);
	
	public boolean isUserAdmin(String userId);
	
	public SakaiPerson getSakaiPerson(String userId);
	
	public SakaiPerson getSakaiPersonPrototype();
	
	public SakaiPerson createSakaiPerson(String userId);

	public boolean updateSakaiPerson(SakaiPerson sakaiPerson);
			
	public int getMaxProfilePictureSize();
	
	public LinkedHashMap<String,String> getSiteListForUser(int limitSites);
	
	public String cleanString(String input);
		
	public String getProfileImageResourcePath(String userId, int type, String fileName);
	
	public boolean saveFile(String fullResourceId, String userId, String fileName, String mimeType, byte[] fileData);
		
	/**
	 * Retrieve a resource from ContentHosting
	 *
	 * @param resourceId	the full resourceId of the file
	 */
	public byte[] getResource(String resourceId);
	
}
