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
import java.util.Comparator;


/** 
 * This the main object that represents a person in Profile2. It is essentially a wrapper object around several other objects.
 * <p>There are standard fields that will always be set.<br />
 * There are also extended objects that you can get access to.</p>
 *  
 * <p>Depending on how you request this object, these extended objects are not guaranteed to be set so
 * you should check for their existence before using. If not set, and you need it, you can get it for a person
 * from the appropriate method in ProfileLogic. If you need it for a whole list, check your method that creates the List
 * of persons, you might be able to get all of the info you need from a different method.</p>
 * 
 * <p>Note about serialisation. The User object is not serialisable and does not contain a no-arg constructor so cannot be manually serialised via
 * the serializable methods (readObject, writeObject). Hence why it is not just included.
 * So the most useful values it provides are extracted and set into this object.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class Person implements Serializable, Comparable {
	
	private static final long serialVersionUID = 1L;
	
	private String uuid;
	private String displayName;
	private String type;
	
	
	//private transient User user;
	private UserProfile profile;
	private ProfilePrivacy privacy;
	private ProfilePreferences preferences;
	
	/**
	 * No-arg constructor
	 */
	public Person() {
	}
	
	/**
	 * Basic constructor
	 * 
	 * @param uuid
	 * @param displayName
	 */
	public Person(String uuid, String displayName) {
		this.uuid = uuid;
		this.displayName=displayName;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/*
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	*/

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
	
	//default sort
	public int compareTo(Object o) {
		String field = ((Person)o).getDisplayName();
        int lastCmp = displayName.compareTo(field);
        return (lastCmp != 0 ? lastCmp : displayName.compareTo(field));
	}
	
	
}
