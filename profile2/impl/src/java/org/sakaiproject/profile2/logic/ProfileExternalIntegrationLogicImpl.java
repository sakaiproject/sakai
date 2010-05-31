package org.sakaiproject.profile2.logic;

import org.sakaiproject.profile2.dao.ProfileDao;
import org.sakaiproject.profile2.model.ExternalIntegrationInfo;

/**
 * Implementation of ProfileExternalIntegrationLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileExternalIntegrationLogicImpl implements ProfileExternalIntegrationLogic {

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
		return dao.updateExternalIntegrationInfo(info);
	}
	
	/**
 	 * {@inheritDoc}
 	 */
	/*
	public RequestToken getTwitterRequestToken() {
		
		Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer(TWITTER_OAUTH_CONSUMER_KEY, TWITTER_OAUTH_CONSUMER_SECRET);
	    
	    try {
			return twitter.getOAuthRequestToken();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	    
	    return null;
	}
	
	public AccessToken getTwitterAccessToken(RequestToken requestToken, String accessCode) {
		
		Twitter twitter = new TwitterFactory().getInstance();
	    twitter.setOAuthConsumer(TWITTER_OAUTH_CONSUMER_KEY, TWITTER_OAUTH_CONSUMER_SECRET);
		
	    try {
			return twitter.getOAuthAccessToken(requestToken, accessCode);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
	}
	*/

	
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
	
	
}
