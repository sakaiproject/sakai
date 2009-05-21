package uk.ac.lancs.e_science.profile2.impl;

import org.apache.log4j.Logger;

import uk.ac.lancs.e_science.profile2.api.Profile;
import uk.ac.lancs.e_science.profile2.api.ProfilePrivacyService;
import uk.ac.lancs.e_science.profile2.api.SakaiProxy;

/**
 * <p>This is the implementation of {@link ProfilePrivacyService}; see that interface for usage details.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ProfilePrivacyServiceImpl implements ProfilePrivacyService {

	private static final Logger log = Logger.getLogger(ProfilePrivacyServiceImpl.class);
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private Profile profile;
	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	
	

}
