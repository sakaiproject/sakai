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
 * This the main object that represents a full person in Profile2. It is essentially a wrapper object around several other objects and data.
 * <p>See BasicPerson for the basic attributes like uuid, name, etc.
 * <p>All fields in Person will be set at instantiation time, however if any are null, this is a true error and should be handled by throwing the
 * appropriate exception.
 *  
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class Person extends BasicPerson implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	//private transient User user;
	private UserProfile profile;
	private ProfilePrivacy privacy;
	private ProfilePreferences preferences;
	
	/**
	 * No arg constructor
	 */
	public Person() {
		super();
	}
	
	public UserProfile getProfile() {
		return profile;
	}

	public void setProfile(UserProfile profile) {
		this.profile = profile;
	}

	public ProfilePrivacy getPrivacy() {
		return privacy;
	}

	public void setPrivacy(ProfilePrivacy privacy) {
		this.privacy = privacy;
	}

	public ProfilePreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(ProfilePreferences preferences) {
		this.preferences = preferences;
	}
	
	
}
