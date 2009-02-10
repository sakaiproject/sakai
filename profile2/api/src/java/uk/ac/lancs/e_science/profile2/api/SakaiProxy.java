package uk.ac.lancs.e_science.profile2.api;

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
	
	/**
	 * Post an event to Sakai
	 * 
	 * @param event			name of event
	 * @param reference		reference
	 * @param modify		true if something changed, false if just access
	 * 
	 * NOTE: ideally, we could have the context in here which would be the person who performed the event
	 * rather than having to crossmatch it with a session.
	 * In most if not all of these events, the reference is the userId of the person who had the event performed on them
	 * since a user profile is not site specific.
	 */
	public void postEvent(String event,String reference,boolean modify);

	/**
	 * Send an email message
	 * 
	 * @param userId	userId to send the message to
	 * @param subject	subject of message
	 * @param message	contents of message
	 */
	public void sendEmail(String userId, String subject, String message);
	
	
	/**
	 * Get the name of this Sakai installation (ie Sakai@Lancs)
	 * @return
	 */
	public String getServiceName();
	
	/**
	 * Get the DNS name of this Sakai server (ie sakai.lancs.ac.uk)
	 * @return
	 */
	public String getServerName();
	
	/**
	 * Updates a user's email address
	 * If the user is a provided user (ie from LDAP) this will probably fail
	 * so only internal accounts can be updated. That's ok since LDAP accounts will always 
	 * have an email address associated with tehm (most likely ;)
	 * 
	 * @param userId	uuid of the user
	 * @param email	
	 */
	public void updateEmailForUser(String userId, String email);
	
	
	public String getPortalUrl();
	
	public String getCurrentPageId();
	
	public String getCurrentToolId();
	
	public String getDirectUrl(String toolString);
	
	/**
	 * Check if a user is allowed to update their email address in their Account
	 * ie could come from LDAP so updates not allowed
	 * 
	 * @param userId
	 * @return
	 */
	public boolean isEmailUpdateAllowed(String userId);

}
