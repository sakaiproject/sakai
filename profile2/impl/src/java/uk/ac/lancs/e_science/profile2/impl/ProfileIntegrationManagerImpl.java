package uk.ac.lancs.e_science.profile2.impl;

import org.sakaiproject.component.cover.ServerConfigurationService;

import uk.ac.lancs.e_science.profile2.api.ProfileIntegrationManager;

public class ProfileIntegrationManagerImpl implements ProfileIntegrationManager {

	/**
	 * @see uk.ac.lancs.e_science.profile2.api.ProfileIntegrationManager#getTwitterSource()
	 */
	public String getTwitterSource() {
		return(ServerConfigurationService.getString(TWITTER_UPDATE_SOURCE_PROPERTY, TWITTER_UPDATE_SOURCE_DEFAULT));
	}
}
