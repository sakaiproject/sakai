package org.sakaiproject.profile2.logic;

import org.sakaiproject.profile2.model.ExternalIntegrationInfo;

/**
 * An interface for dealing with external integrations in Profile2
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
public interface ProfileExternalIntegrationLogic {

	/**
	 * Get the ExternalIntegrationInfo record for a user or an empty record if none.
	 * @param userUuid
	 * @return
	 */
	public ExternalIntegrationInfo getExternalIntegrationInfo(final String userUuid);
	
	/**
	 * Update a user's ExternalIntegrationInfo
	 * @param info ExternalIntegrationInfo object for the user
	 * @return
	 */
	public boolean updateExternalIntegrationInfo(ExternalIntegrationInfo info);
	
	
	/**
	 * Get a Twitter RequestToken for linking user accounts - used for linking accounts.
	 * @return RequestToken
	 *
	 */
	//public RequestToken getTwitterRequestToken();
	
	/**
	 * Get a Twitter OAuth AccessToken for the supplied accessCode - used for linking accounts.
	 * @param requestToken	the RequestToken that was used for getting the accessCode
	 * @param accessCode	the accessCode
	 * @return
	 */
	//public AccessToken getTwitterAccessToken(RequestToken requestToken, String accessCode);
}
