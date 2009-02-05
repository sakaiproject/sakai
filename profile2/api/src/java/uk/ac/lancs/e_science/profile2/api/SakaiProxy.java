package uk.ac.lancs.e_science.profile2.api;

import java.util.LinkedHashMap;
import java.util.List;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
/**
 * This is a helper API used by the Profile2 tool only. 
 * 
 * DO NOT IMPLEMENT THIS YOURSELF
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public interface SakaiProxy {
	
	public static final int FIRST_RECORD = 0;		
	public static final int MAX_RECORDS = 99;
	
	
	
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
	
	public String getSakaiConfigurationParameterAsString(String parameter, String defaultValue);
	public int getSakaiConfigurationParameterAsInt(String parameter, int defaultValue);
	public boolean getSakaiConfigurationParameterAsBoolean(String parameter, boolean defaultValue);
	
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
	
	/**
	 * Search UserDirectoryService for a user that matches in name or email
	 *
	 * @param search	search string. Return's List of Sakai userId's 
	 */
	public List<String> searchUsers(String search);
	
	
}
