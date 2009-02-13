package uk.ac.lancs.e_science.profile2.impl;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfileService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;
import uk.ac.lancs.e_science.profile2.api.entity.model.ProfileEntity;
/**
 * This ProfileService to be implemented by external tools
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class ProfileServiceImpl implements ProfileService {

	
	//setup SakaiProxy API
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	//setup Profile API
	private Profile profile;
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

}
