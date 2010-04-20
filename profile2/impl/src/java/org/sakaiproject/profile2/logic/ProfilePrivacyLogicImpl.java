package org.sakaiproject.profile2.logic;

/**
 * Implementation of ProfileLinkLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfilePrivacyLogicImpl implements ProfilePrivacyLogic {

	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
}
