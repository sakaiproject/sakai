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

/**
 * @author rshastri <a href="mailto:rshastri@iupui.edu ">Rashmi Shastri</a>
 */
public interface Profile
{
	/**
	 * @return
	 */

	// public Long getId();
	/**
	 * @return
	 */
	public String getUserId();

	/**
	 * @param userID
	 */
	public void setUserID(String userID);

	/**
	 * @return
	 */

	// public Integer getVersion();
	/**
	 * @return
	 */
	public String getDepartment();

	/**
	 * @param department
	 */
	public void setDepartment(String department);

	/**
	 * @return
	 */
	public String getEmail();

	/**
	 * @param email
	 */
	public void setEmail(String email);

	/**
	 * @return
	 */
	public String getFirstName();

	/**
	 * @param firstName
	 */
	public void setFirstName(String firstName);

	/**
	 * @return
	 */
	public String getNickName();

	/**
	 * @param firstName
	 */
	public void setNickName(String nickName);

	/**
	 * @return
	 */
	public String getHomePhone();

	/**
	 * @param homePhone
	 */
	public void setHomePhone(String homePhone);

	/**
	 * @return
	 */
	public String getHomepage();

	/**
	 * @param homepage
	 */
	public void setHomepage(String homepage);

	/**
	 * @return
	 */
	public String getLastName();

	/**
	 * @param lastName
	 */
	public void setLastName(String lastName);

	/**
	 * @return
	 */
	public String getOtherInformation();

	/**
	 * @param otherInformation
	 */
	public void setOtherInformation(String otherInformation);

	/**
	 * @return
	 */
	public String getPictureUrl();

	/**
	 * @param pictureUrl
	 */
	public void setPictureUrl(String pictureUrl);

	/**
	 * @return
	 */
	public String getPosition();

	/**
	 * @param position
	 */
	public void setPosition(String position);

	/**
	 * @return
	 */
	public String getRoom();

	/**
	 * @param room
	 */
	public void setRoom(String room);

	/**
	 * @return
	 */
	public String getSchool();

	/**
	 * @param school
	 */
	public void setSchool(String school);

	/**
	 * @return
	 */
	public String getWorkPhone();

	/**
	 * @param workPhone
	 */
	public void setWorkPhone(String workPhone);

	/**
	 * @return
	 */
	public Boolean isInstitutionalPictureIdPreferred();

	/**
	 * @param institutionalPictureIdPreferred
	 */
	public void setInstitutionalPictureIdPreferred(Boolean institutionalPictureIdPreferred);

	/**
	 * @return
	 */
	public byte[] getInstitutionalPicture();

	/**
	 * @param institutionalPicture
	 */
	// Can not allow users to set institutiona picture.
	// public void setInstitutionalPicture(byte[] institutionalPicture);
	/**
	 * @return
	 */
	public SakaiPerson getSakaiPerson();

	/**
	 * @param sakaiPerson
	 */
	public void setSakaiPerson(SakaiPerson sakaiPerson);

	/**
	 * @return
	 */
	public Boolean getHidePrivateInfo();

	/**
	 * Person's preference to allow read access to private information
	 * 
	 * @param hidePrivateInfo
	 */
	public void setHidePrivateInfo(Boolean hidePrivateInfo);

	/**
	 * Person's preference to allow read access to public information
	 * 
	 * @return
	 */
	public Boolean getHidePublicInfo();

	/**
	 * Person's preference to allow read access to public information
	 * 
	 * @param hidePublicInfo
	 */
	public void setHidePublicInfo(Boolean hidePublicInfo);
	
	public Boolean getLocked();
}
