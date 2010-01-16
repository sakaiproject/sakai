package org.sakaiproject.profile2.service;

import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;

public class ProfileLinkServiceImpl implements ProfileLinkService {


	/**
 	* {@inheritDoc}
 	*/
	public String getInternalDirectUrlToUserProfile() {
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		return sakaiProxy.getDirectUrlToUserProfile(currentUserUuid, null);
	}


	/**
 	* {@inheritDoc}
 	*/
	public String getInternalDirectUrlToUserProfile(String userUuid) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
 	* {@inheritDoc}
 	*/
	public String getUrlToUserProfile() {
		return profileLogic.getEntityLinkToProfileHome();
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUrlToUserMessages(final String threadId) {
		return profileLogic.getEntityLinkToProfileMessages(threadId);
	}
	
	/**
 	* {@inheritDoc}
 	*/
	public String getUrlToUserConnections() {
		return profileLogic.getEntityLinkToProfileConnections();
	}


	
	
	
	private SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	private ProfileLogic profileLogic;
	public void setProfileLogic(ProfileLogic profileLogic) {
		this.profileLogic = profileLogic;
	}


	

	
}
