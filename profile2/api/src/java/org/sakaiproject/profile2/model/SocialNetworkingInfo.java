/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.profile2.model;

import java.io.Serializable;

/**
 * <code>SocialNetworkingInfo</code> is a model for storing a user's social
 * networking details.
 */
public class SocialNetworkingInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String userUuid;
	private String facebookUsername;
	private String linkedinUsername;
	private String myspaceUsername;
	private String skypeUsername;
	private String twitterUsername;
	
	public SocialNetworkingInfo() {
		
	}

	public SocialNetworkingInfo(String userUuid, String facebookUsername,
			String linkedinUsername, String myspaceUsername,
			String skypeUsername, String twitterUsername) {

		this.userUuid = userUuid;
		this.facebookUsername = facebookUsername;
		this.linkedinUsername = linkedinUsername;
		this.myspaceUsername = myspaceUsername;
		this.skypeUsername = skypeUsername;
		this.twitterUsername = twitterUsername;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getFacebookUsername() {
		return facebookUsername;
	}

	public void setFacebookUsername(String facebookUsername) {
		this.facebookUsername = facebookUsername;
	}

	public String getLinkedinUsername() {
		return linkedinUsername;
	}

	public void setLinkedinUsername(String linkedinUsername) {
		this.linkedinUsername = linkedinUsername;
	}

	public String getMyspaceUsername() {
		return myspaceUsername;
	}

	public void setMyspaceUsername(String myspaceUsername) {
		this.myspaceUsername = myspaceUsername;
	}

	public String getSkypeUsername() {
		return skypeUsername;
	}

	public void setSkypeUsername(String skypeUsername) {
		this.skypeUsername = skypeUsername;
	}

	public String getTwitterUsername() {
		return twitterUsername;
	}

	public void setTwitterUsername(String twitterUsername) {
		this.twitterUsername = twitterUsername;
	}
	
	
}
