package org.sakaiproject.profile2.logic;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.model.ProfilePreferences;
import org.sakaiproject.profile2.util.ProfileConstants;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;

/**
 * Implementation of ProfileExternalIntegrationLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileExternalIntegrationLogicImpl implements ProfileExternalIntegrationLogic {

	private static final Logger log = Logger.getLogger(ProfileExternalIntegrationLogicImpl.class);

	
	private final String TWITTER_OAUTH_CONSUMER_KEY="XzSPZIj0LxNaaoBz8XrgZQ";
	private final String TWITTER_OAUTH_CONSUMER_SECRET="FSChsnmTufYi3X9H25YdFRxBhPXgnh2H0lMnLh7ZVG4";
	
	/**
 	 * {@inheritDoc}
 	 */
	public ExternalIntegrationInfo getExternalIntegrationInfo(String userUuid) {
		ExternalIntegrationInfo info = dao.getExternalIntegrationInfo(userUuid);
		if(info != null) {
			return info;
		}
		return getDefaultExternalIntegrationInfo(userUuid);
	}

	/**
 	 * {@inheritDoc}
 	 */
	public boolean updateExternalIntegrationInfo(ExternalIntegrationInfo info) {
		if(dao.updateExternalIntegrationInfo(info)){
			log.info("ExternalIntegrationInfo updated for user: " + info.getUserUuid());
			return true;
		}
		return false;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public Map<String,String> getTwitterOAuthConsumerDetails() {
		
		Map<String,String> map = new HashMap<String,String>();
		map.put("key", sakaiProxy.getServerConfigurationParameter("profile2.twitter.oauth.key", TWITTER_OAUTH_CONSUMER_KEY));
		map.put("secret", sakaiProxy.getServerConfigurationParameter("profile2.twitter.oauth.secret", TWITTER_OAUTH_CONSUMER_SECRET));
		return map;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	public String getTwitterName(ExternalIntegrationInfo info) {
		
		if(info == null){
			return null;
		}
		
		//get values
		String token = info.getTwitterToken();
		String secret = info.getTwitterSecret();
		
		if(StringUtils.isNotBlank(token) && StringUtils.isNotBlank(secret)) {

			//global config
			Map<String,String> config = getTwitterOAuthConsumerDetails();

			//token for user
			AccessToken accessToken = new AccessToken(token, secret);
			
			//setup
			Twitter twitter = new TwitterFactory().getOAuthAuthorizedInstance(config.get("key"), config.get("secret"), accessToken);
			
			//check
			try {
				return twitter.verifyCredentials().getScreenName();
			} catch (TwitterException e) {
				log.error("Error retrieving Twitter credentials: " + e.getClass() + ": " + e.getMessage());
			}
		}
		return null;
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public boolean validateTwitterCredentials(ExternalIntegrationInfo info) {
		return StringUtils.isNotBlank(getTwitterName(info));
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	public void sendMessageToTwitter(final String userUuid, final String message){
		//setup class thread to call later
		class TwitterUpdater implements Runnable{
			private Thread runner;
			private String userUuid;
			private String userToken;
			private String userSecret;
			private String message;

			public TwitterUpdater(String userUuid, String userToken, String userSecret, String message) {
				this.userUuid=userUuid;
				this.userToken=userToken;
				this.userSecret=userSecret;
				this.message=message;
				
				runner = new Thread(this,"Profile2 TwitterUpdater thread"); 
				runner.start();
			}
			

			//do it!
			public synchronized void run() {
				
				//global config
				Map<String,String> config = getTwitterOAuthConsumerDetails();

				//token for user
				AccessToken accessToken = new AccessToken(userToken, userSecret);
				
				//setup
				Twitter twitter = new TwitterFactory().getOAuthAuthorizedInstance(config.get("key"), config.get("secret"), accessToken);
				
				try {
					twitter.updateStatus(message);
					log.info("Twitter status updated for: " + userUuid); 
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_TWITTER_UPDATE, "/profile/"+userUuid, true);
				}
				catch (Exception e) {
					log.error("ProfileLogic.sendMessageToTwitter() failed. " + e.getClass() + ": " + e.getMessage());  
				}
			}
		}
		
		//is twitter enabled
		if(!sakaiProxy.isTwitterIntegrationEnabledGlobally()){
			return;
		}
			
		//get user info
		ExternalIntegrationInfo info = getExternalIntegrationInfo(userUuid);
		if(info == null){
			return;
		}
		String token = info.getTwitterToken();
		String secret = info.getTwitterSecret();
		if(StringUtils.isBlank(token) || StringUtils.isBlank(secret)) {
			return;
		}
		
		//instantiate class to send the data
		new TwitterUpdater(userUuid, token, secret, message);
	}
	
	
	/**
	 * Get a default record, will only contain the userUuid
	 * @param userUuid
	 * @return
	 */
	private ExternalIntegrationInfo getDefaultExternalIntegrationInfo(String userUuid) {
		ExternalIntegrationInfo info = new ExternalIntegrationInfo();
		info.setUserUuid(userUuid);
		return info;
		
	}
	
	
	private ProfileDao dao;
	public void setDao(ProfileDao dao) {
		this.dao = dao;
	}
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	
}
