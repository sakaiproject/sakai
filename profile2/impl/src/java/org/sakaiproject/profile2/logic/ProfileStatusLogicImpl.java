package org.sakaiproject.profile2.logic;

/**
 * Implementation of ProfileStatusLogic API
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileStatusLogicImpl implements ProfileStatusLogic {

	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
}
