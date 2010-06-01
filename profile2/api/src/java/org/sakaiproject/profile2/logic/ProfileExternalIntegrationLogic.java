package org.sakaiproject.profile2.logic;

import java.util.Map;

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
	 * Returns a map of the Twitter OAuth consumer 'key' and 'secret'
	 * @return
	 */
	public Map<String,String> getTwitterOAuthConsumerDetails();

	/**
	 * Gets the Twitter name associated with the stored details, if any.
	 * @param info  ExternalIntegrationInfo object for the user
	 * @return name or null if none/error
	 */
	public String getTwitterName(ExternalIntegrationInfo info);

	/**
	 * Check if the stored Twitter credentials are valid.
	 * @param info   ExternalIntegrationInfo object for the user
	 * @return true if valid, false if not/error
	 */
	public boolean validateTwitterCredentials(ExternalIntegrationInfo info);

	
}
