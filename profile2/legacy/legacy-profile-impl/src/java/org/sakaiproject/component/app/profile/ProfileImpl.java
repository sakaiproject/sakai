/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.profile;

import org.sakaiproject.api.app.profile.Profile;


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
	private String pictureUrl; //never set this, maybe set this from ResourceWrapper?
	private String position;
	private String room;
	private String school;
	private String workPhone;
	private Boolean institutionalPictureIdPreferred;
	private byte[] institutionalPicture; //never set this, dont even have a setter
	private Boolean hidePrivateInfo; //set to default of false because Profile2 Privacy is used
	private Boolean hidePublicInfo; //set to default of false because Profile2 Privacy is used
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getHomePhone() {
		return homePhone;
	}
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	public String getOtherInformation() {
		return otherInformation;
	}
	public void setOtherInformation(String otherInformation) {
		this.otherInformation = otherInformation;
	}
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getWorkPhone() {
		return workPhone;
	}
	public void setWorkPhone(String workPhone) {
		this.workPhone = workPhone;
	}
	public Boolean isInstitutionalPictureIdPreferred() {
		return institutionalPictureIdPreferred;
	}
	public void setInstitutionalPictureIdPreferred(
			Boolean institutionalPictureIdPreferred) {
		this.institutionalPictureIdPreferred = institutionalPictureIdPreferred;
	}
	public byte[] getInstitutionalPicture() {
		return institutionalPicture;
	}
	public void setInstitutionalPicture(byte[] institutionalPicture) {
		this.institutionalPicture = institutionalPicture;
	}
	public Boolean getHidePrivateInfo() {
		return hidePrivateInfo;
	}
	public void setHidePrivateInfo(Boolean hidePrivateInfo) {
		this.hidePrivateInfo = hidePrivateInfo;
	}
	public Boolean getHidePublicInfo() {
		return hidePublicInfo;
	}
	public void setHidePublicInfo(Boolean hidePublicInfo) {
		this.hidePublicInfo = hidePublicInfo;
	}
}
