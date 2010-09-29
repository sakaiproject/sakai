package org.sakaiproject.signup.restful;

import org.sakaiproject.signup.model.SignupMeeting;

/**
 * <p>
 * This class holds the information of sign-up meeting object and the permission
 * information regarding to the targeted siteId.
 * </p>
 * 
 * @author Peter Liu
 * 
 */
public class SignupTargetSiteEventInfo {

	private SignupMeeting signupMeeting;

	/*
	 * the permission info in signupMeeting object will be related to this
	 * targetSiteId
	 */
	private String targetSiteId;

	public SignupTargetSiteEventInfo(SignupMeeting sMeeting, String targetSiteId) {
		this.signupMeeting = sMeeting;
		this.targetSiteId = targetSiteId;
	}

	public SignupMeeting getSignupMeeting() {
		return signupMeeting;
	}

	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

	public String getTargetSiteId() {
		return targetSiteId;
	}

	public void setTargetSiteId(String targetSiteId) {
		this.targetSiteId = targetSiteId;
	}

}
