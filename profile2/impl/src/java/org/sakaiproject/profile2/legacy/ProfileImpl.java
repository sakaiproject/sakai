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

package org.sakaiproject.profile2.legacy;

import lombok.Data;

import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

@Data
public class ProfileImpl implements Profile
{
	private String userId;
	private String department;
	private String email;
	private String firstName;
	private String lastName;
	private String nickName;
	private String homePhone;
	private String homepage;
	private String otherInformation;
	private String pictureUrl; //this needs to be populated with the ProfileImage.url field, if set
	private String position;
	private String room;
	private String school;
	private String workPhone;
	private Boolean institutionalPictureIdPreferred;
	private byte[] institutionalPicture; //never set this, dont even have a setter
	private Boolean hidePrivateInfo; //set to default of false because Profile2 Privacy is used
	private Boolean hidePublicInfo; //set to default of false because Profile2 Privacy is used
	
	
	/** getters/setters that have odd signatures so we explicity declare them **/
	public String getUserId() {
		return this.userId;
	}
	public void setUserID(String arg0) {
		this.userId = arg0;
	}
	public Boolean isInstitutionalPictureIdPreferred() {
		return institutionalPictureIdPreferred;
	}
	public void setInstitutionalPictureIdPreferred(Boolean institutionalPictureIdPreferred) {
		this.institutionalPictureIdPreferred = institutionalPictureIdPreferred;
	}
	
	
	/** additional methods from original Profile API but are just stubs as we don't use them */
	public Boolean getLocked() {
		return Boolean.FALSE;
	}
	public SakaiPerson getSakaiPerson() {
		return null;
	}
	public void setSakaiPerson(SakaiPerson arg0) {
	}


	
	
	
}
