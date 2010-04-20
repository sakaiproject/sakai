package org.sakaiproject.profile2.logic;

/**
 * Implementation of ProfilePreferencesLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfilePreferencesLogicImpl implements ProfilePreferencesLogic {

	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
}
