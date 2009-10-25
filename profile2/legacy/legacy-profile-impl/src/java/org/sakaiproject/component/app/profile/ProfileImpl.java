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
import org.sakaiproject.api.common.edu.person.SakaiPerson;

/**
 * @author rshastri
 */
public class ProfileImpl implements Profile
{
	private SakaiPerson sakaiPerson;

	public ProfileImpl()
	{
	}

	/**
	 * @param eduPerson
	 */
	public ProfileImpl(SakaiPerson sakaiPerson)
	{
		this.sakaiPerson = sakaiPerson;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getDepartment()
	 */
	public String getDepartment()
	{
		return sakaiPerson.getOrganizationalUnit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setDepartment(java.lang.String)
	 */
	public void setDepartment(String department)
	{
		sakaiPerson.setOrganizationalUnit(department);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getEmail()
	 */
	public String getEmail()
	{
		return sakaiPerson.getMail();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setEmail(java.lang.String)
	 */
	public void setEmail(String email)
	{
		sakaiPerson.setMail(email);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getFirstName()
	 */
	public String getFirstName()
	{
		return sakaiPerson.getGivenName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setFirstName(java.lang.String)
	 */
	public void setFirstName(String firstName)
	{
		sakaiPerson.setGivenName(firstName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getNickName()
	 */
	public String getNickName()
	{
		return sakaiPerson.getNickname();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setNickName(java.lang.String)
	 */
	public void setNickName(String nickName)
	{
		sakaiPerson.setNickname(nickName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getHomePhone()
	 */
	public String getHomePhone()
	{
		return sakaiPerson.getHomePhone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setHomePhone(java.lang.String)
	 */
	public void setHomePhone(String homePhone)
	{
		sakaiPerson.setHomePhone(homePhone);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getHomepage()
	 */
	public String getHomepage()
	{
		// to account for the time when we weren't checking for valid urls
		String homepage =  sakaiPerson.getLabeledURI();
		if (homepage == null || homepage.equals (""))
		{
			// ignore the empty url field
		}
		else if (homepage.indexOf ("://") == -1)
		{
			// if it's missing the transport, add http://
			homepage = "http://" + homepage;
		}
		
		return homepage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setHomepage(java.lang.String)
	 */
	public void setHomepage(String homepage)
	{
		sakaiPerson.setLabeledURI(homepage);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getLastName()
	 */
	public String getLastName()
	{
		return sakaiPerson.getSurname();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setLastName(java.lang.String)
	 */
	public void setLastName(String lastName)
	{
		sakaiPerson.setSurname(lastName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getOtherInformation()
	 */
	public String getOtherInformation()
	{
		return sakaiPerson.getNotes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setOtherInformation(java.lang.String)
	 */
	public void setOtherInformation(String otherInformation)
	{
		sakaiPerson.setNotes(otherInformation);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getPictureURL()
	 */
	public String getPictureUrl()
	{
		return sakaiPerson.getPictureUrl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setPictureURL(java.lang.String)
	 */
	public void setPictureUrl(String pictureUrl)
	{
		sakaiPerson.setPictureUrl(pictureUrl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getPosition()
	 */
	public String getPosition()
	{
		return sakaiPerson.getTitle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setPosition(java.lang.String)
	 */
	public void setPosition(String position)
	{
		sakaiPerson.setTitle(position);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getRoom()
	 */
	public String getRoom()
	{
		return sakaiPerson.getRoomNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setRoom(java.lang.String)
	 */
	public void setRoom(String room)
	{
		sakaiPerson.setRoomNumber(room);
	}

	public String getSchool()
	{
		return sakaiPerson.getCampus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setSchool(java.lang.String)
	 */
	public void setSchool(String school)
	{
		sakaiPerson.setCampus(school);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getWorkPhone()
	 */
	public String getWorkPhone()
	{
		return sakaiPerson.getTelephoneNumber();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setWorkPhone(java.lang.String)
	 */
	public void setWorkPhone(String workPhone)
	{
		sakaiPerson.setTelephoneNumber(workPhone);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getNetworkID()
	 */
	public String getUserId()
	{
		return sakaiPerson.getUid();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setNetworkID(java.lang.String)
	 */
	public void setUserID(String userID)
	{
		sakaiPerson.setUid(userID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#isInstitutionalPictureIDSelected()
	 */
	public Boolean isInstitutionalPictureIdPreferred()
	{
		return sakaiPerson.isSystemPicturePreferred();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setInstitutionalPictureIDSelected(boolean)
	 */
	public void setInstitutionalPictureIdPreferred(Boolean institutionalPictureIdPreferred)
	{
		sakaiPerson.setSystemPicturePreferred(institutionalPictureIdPreferred);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getInstitutionalPicture()
	 */

	public byte[] getInstitutionalPicture()
	{
		return sakaiPerson.getJpegPhoto();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#isPrivateInfoViewable()
	 */
	public Boolean getHidePrivateInfo()
	{
		return sakaiPerson.getHidePrivateInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setPrivateInfoViewable(java.lang.Boolean)
	 */
	public void setHidePrivateInfo(Boolean hidePrivateInfo)
	{
		sakaiPerson.setHidePrivateInfo(hidePrivateInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#isPublicInfoViewable()
	 */
	public Boolean getHidePublicInfo()
	{
		return sakaiPerson.getHidePublicInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setPublicInfoViewable(java.lang.Boolean)
	 */
	public void setHidePublicInfo(Boolean hidePublicInfo)
	{
		sakaiPerson.setHidePublicInfo(hidePublicInfo);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#getSakaiPerson()
	 */
	public SakaiPerson getSakaiPerson()
	{
		return sakaiPerson;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.profile.Profile#setSakaiPerson(org.sakaiproject.api.app.profile.SakaiPerson)
	 */
	public void setSakaiPerson(SakaiPerson sakaiPerson)
	{
		this.sakaiPerson = sakaiPerson;
	}

	
	  public String getMobile()
	  {
		  return sakaiPerson.getMobile();
	  }
	  
	  public void setMobile(String mobile)
	  {
		  sakaiPerson.setMobile(mobile);
		  
	  }
	
	// public String toString()
	// {
	// return sakaiPerson.toString();
	// }
	  public Boolean getLocked() {
		  return sakaiPerson.getLocked();
	  }
}
