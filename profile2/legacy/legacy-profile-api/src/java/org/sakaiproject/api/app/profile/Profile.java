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

package org.sakaiproject.api.app.profile;

import org.sakaiproject.api.common.edu.person.SakaiPerson;


public interface Profile
{
	
	public String getUserId();
	public void setUserId(String userId);
	public String getDepartment();
	public void setDepartment(String department);
	public String getEmail();
	public void setEmail(String email);
	public String getFirstName();
	public void setFirstName(String firstName);
	public String getLastName();
	public void setLastName(String lastName);
	public String getNickName();
	public void setNickName(String nickName);
	public String getHomePhone();
	public void setHomePhone(String homePhone);
	public String getHomePage();
	public void setHomePage(String homePage);
	public String getOtherInformation();
	public void setOtherInformation(String otherInformation);
	public String getPictureUrl();
	public void setPictureUrl(String pictureUrl);
	public String getPosition();
	public void setPosition(String position);
	public String getRoom();
	public void setRoom(String room);
	public String getSchool();
	public void setSchool(String school);
	public String getWorkPhone();
	public void setWorkPhone(String workPhone);
	public Boolean isInstitutionalPictureIdPreferred();
	public void setInstitutionalPictureIdPreferred(Boolean institutionalPictureIdPreferred);
	public byte[] getInstitutionalPicture();
	public void setInstitutionalPicture(byte[] institutionalPicture);
	public Boolean getHidePrivateInfo();
	public void setHidePrivateInfo(Boolean hidePrivateInfo);
	public Boolean getHidePublicInfo();
	public void setHidePublicInfo(Boolean hidePublicInfo);
}
