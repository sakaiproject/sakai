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

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;
import org.sakaiproject.profile2.util.ProfileConstants;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Implementation of ProfileExternalIntegrationLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ProfileExternalIntegrationLogicImpl implements ProfileExternalIntegrationLogic {

	/**
	 * OAuth Consumer registration details for Profile2.
	 */
	@Deprecated private final String TWITTER_OAUTH_CONSUMER_KEY="XzSPZIj0LxNaaoBz8XrgZQ";
	@Deprecated private final String TWITTER_OAUTH_CONSUMER_SECRET="FSChsnmTufYi3X9H25YdFRxBhPXgnh2H0lMnLh7ZVG4";
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
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
	@Override
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
	@Override
	public Map<String,String> getTwitterOAuthConsumerDetails() {
		
		Map<String,String> map = new HashMap<String,String>();
		map.put("key", sakaiProxy.getServerConfigurationParameter("profile2.twitter.oauth.key", TWITTER_OAUTH_CONSUMER_KEY));
		map.put("secret", sakaiProxy.getServerConfigurationParameter("profile2.twitter.oauth.secret", TWITTER_OAUTH_CONSUMER_SECRET));
		return map;
	}
	
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
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
			TwitterFactory factory = new TwitterFactory();
			Twitter twitter = factory.getInstance();
			twitter.setOAuthConsumer(config.get("key"), config.get("secret"));
			twitter.setOAuthAccessToken(accessToken);
			
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
	@Override
	public boolean validateTwitterCredentials(ExternalIntegrationInfo info) {
		return StringUtils.isNotBlank(getTwitterName(info));
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public void sendMessageToTwitter(final String userUuid, String message){
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
			@Override
			public synchronized void run() {
				
				//global config
				Map<String,String> config = getTwitterOAuthConsumerDetails();

				//token for user
				AccessToken accessToken = new AccessToken(userToken, userSecret);
				
				//setup
				TwitterFactory factory = new TwitterFactory();
				Twitter twitter = factory.getInstance();
				twitter.setOAuthConsumer(config.get("key"), config.get("secret"));
				twitter.setOAuthAccessToken(accessToken);
				
				try {
					twitter.updateStatus(message);
					log.info("Twitter status updated for: " + userUuid); 
					
					//post update event
					sakaiProxy.postEvent(ProfileConstants.EVENT_TWITTER_UPDATE, "/profile/"+userUuid, true);
				}
				catch (TwitterException e) {
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
		String token = info.getTwitterToken();
		String secret = info.getTwitterSecret();
		if(StringUtils.isBlank(token) || StringUtils.isBlank(secret)) {
			return;
		}
		
		//PRFL-423 limit to 140 chars
		//Hardcoded limit because 140 is the Twitter requirement so no need to make configurable
		message = ProfileUtils.truncate(message, 140, false); 
		
		//instantiate class to send the data
		new TwitterUpdater(userUuid, token, secret, message);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	@Override
	public String getGoogleAuthenticationUrl() {
		
		String clientId = sakaiProxy.getServerConfigurationParameter("profile2.integration.google.client-id", null);
	
		if(StringUtils.isBlank(clientId)){
			log.error("Google integration not properly configured. Please set the client id");
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("https://accounts.google.com/o/oauth2/auth?");
		sb.append("client_id=");
		sb.append(clientId);
		sb.append("&redirect_uri=");
		sb.append(ProfileConstants.GOOGLE_REDIRECT_URI);
		sb.append("&response_type=code");
		sb.append("&scope=");
		sb.append(ProfileConstants.GOOGLE_DOCS_SCOPE);
		
		return sb.toString();
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
	
	@Setter
	private ProfileDao dao;
	
	@Setter
	private SakaiProxy sakaiProxy;
	
	
}
