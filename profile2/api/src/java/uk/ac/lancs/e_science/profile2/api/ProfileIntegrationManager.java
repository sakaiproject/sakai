package uk.ac.lancs.e_science.profile2.api;

/**
 * Simple API for managing some integration stuff with Profile2
 * 
 * DO NOT IMPLEMENT THIS YOURSELF
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public interface ProfileIntegrationManager {

	/**
	 * the user's password needs to be decrypted and sent to Twitter for updates
	 * so we can't just one-way encrypt it. 
	 */
	public static final String BASIC_ENCRYPTION_KEY = "AbrA_ca-DabRa.123";
	
	
	/**
	 * setup the sakai.property and default value for
	 * profile2.integration.twitter.source=YOUR CUSTOM SOURCE
	 
	 * But you can't just set it like that.
	 
	 * See here:
	 * http://bugs.sakaiproject.org/confluence/display/PROFILE/Profile2
	 */
	public static final String TWITTER_UPDATE_SOURCE_PROPERTY = "profile2.integration.twitter.source";
	public static final String TWITTER_UPDATE_SOURCE_DEFAULT = "Sakai";
	
	
	/**
	 * Get value of profile2.integration.twitter.source from sakai.properties
	 * 
	 * @return
	 */
	public String getTwitterSource();
	
}
