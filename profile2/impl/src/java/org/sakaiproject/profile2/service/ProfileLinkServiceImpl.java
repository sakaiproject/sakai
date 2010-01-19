package org.sakaiproject.profile2.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.util.ProfileConstants;

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
	public String getInternalDirectUrlToUserProfile(final String userUuid) {
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//link direct to ViewProfile page and add in the user param
		String extraParams = null;
		Map<String,String> vars = new HashMap<String,String>();
		vars.put(ProfileConstants.WICKET_PARAM_USERID, userUuid);
		extraParams = getFormattedStateParamForWicketTool(ProfileConstants.WICKET_PAGE_PROFILE_VIEW, vars);
		
		return sakaiProxy.getDirectUrlToUserProfile(currentUserUuid, extraParams);
	}

	
	
	/**
 	* {@inheritDoc}
 	*/
	public String getInternalDirectUrlToUserMessages(final String threadId) {
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//link direct to messages page, if we have a threadId, add the appropriate params in
		String extraParams = null;
		if(StringUtils.isNotBlank(threadId)) {
			Map<String,String> vars = new HashMap<String,String>();
			vars.put(ProfileConstants.WICKET_PARAM_THREAD, threadId);
			extraParams = getFormattedStateParamForWicketTool(ProfileConstants.WICKET_PAGE_MESSAGE_VIEW, vars);
		} else {
			extraParams = getFormattedStateParamForWicketTool(ProfileConstants.WICKET_PAGE_MESSAGE_LIST, null);
		}
		
		return sakaiProxy.getDirectUrlToUserProfile(currentUserUuid, extraParams);
	}

	/**
 	* {@inheritDoc}
 	*/
	public String getInternalDirectUrlToUserConnections() {
		String currentUserUuid = sakaiProxy.getCurrentUserId();
		if(currentUserUuid == null) {
			throw new SecurityException("Must be logged in.");
		}
		
		//link direct to connections page, no extra params needed
		String extraParams = getFormattedStateParamForWicketTool(ProfileConstants.WICKET_PAGE_CONNECTIONS, null);
		return sakaiProxy.getDirectUrlToUserProfile(currentUserUuid, extraParams);
	}


	

	

	/**
 	* {@inheritDoc}
 	*/
	public String getUrlToUserProfile(final String userUuid) {
		return profileLogic.getEntityLinkToProfileHome(userUuid);
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

/**
 * Special method that mimics the urlFor method from Wicket for a class.
 * Since we can't use that outside of Wicket, we need to copy it's behaviour here. This must be tested in Wicket upgrades 
 * but should be stable since the idea is that they are bookmarkable links and shouldn't change.
 * 
 * <p>The return is not URL encoded as it is encoded in SakaiProxy</p>
 * 
 * @param pageClass	page class to be used, see ProfileConstants.WICKET_PAGE_PROFILE for example
 * @param params	key,value pair of any additional params required for the URL
 * @return
 */
	private String getFormattedStateParamForWicketTool(final String pageClass, final Map<String,String> vars) {
		

		//%3Fwicket%3AbookmarkablePage%3D%3Aorg.sakaiproject.profile2.tool.pages.MyFriends
		//?wicket:bookmarkablePage=:org.sakaiproject.profile2.tool.pages.MyFriends
		//%3Fwicket%3AbookmarkablePage%3D%3Aorg.sakaiproject.profile2.tool.pages.MyMessageView%26thread%3D99eb8904-e4a5-4569-bbda-bef4be6803aa
		//&thread=99eb8904-e4a5-4569-bbda-bef4be6803aa
		
		StringBuilder params = new StringBuilder();
		params.append("?wicket:bookmarkablePage=:");
		params.append(pageClass);
		
		if(vars != null) {
			for(Map.Entry<String,String> var : vars.entrySet()) {
				params.append("&");
				params.append(var.getKey());
				params.append("=");
				params.append(var.getValue());
			}
		}

		return params.toString();
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
