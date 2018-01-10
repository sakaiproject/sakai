/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.common.edu.person;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.common.edu.person.InetOrgPerson;
import org.sakaiproject.api.common.edu.person.OrganizationalPerson;
import org.sakaiproject.api.common.edu.person.Person;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
@Slf4j
public class InetOrgPersonImpl extends OrganizationalPersonImpl implements Person, OrganizationalPerson, InetOrgPerson
{
	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getAudio()
	 */
	public BufferedInputStream getAudio()
	{
		// TODO implement audio
		return null;
	}

	/**
	 * @param audio
	 *        The audio to set.
	 */
	public void setAudio(BufferedOutputStream audio)
	{
		// TODO implement audio
		;
	}

	protected String givenName;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getGivenName()
	 */
	public String getGivenName()
	{
		return givenName;
	}

	/**
	 * @param givenName
	 *        The givenName to set.
	 */
	public void setGivenName(String givenName)
	{
		this.givenName = givenName;
	}

	protected String homePhone;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getHomePhone()
	 */
	public String getHomePhone()
	{
		return homePhone;
	}

	/**
	 * @param homePhone
	 *        The homePhone to set.
	 */
	public void setHomePhone(String homePhone)
	{
		this.homePhone = homePhone;
	}

	protected String homePostalAddress;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getHomePostalAddress()
	 */
	public String getHomePostalAddress()
	{
		return homePostalAddress;
	}

	/**
	 * @param homePostalAddress
	 *        The homePostalAddress to set.
	 */
	public void setHomePostalAddress(String homePostalAddress)
	{
		this.homePostalAddress = homePostalAddress;
	}

	protected String initials;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getInitials()
	 */
	public String getInitials()
	{
		return initials;
	}

	/**
	 * @param initials
	 *        The initials to set.
	 */
	public void setInitials(String initials)
	{
		this.initials = initials;
	}

	protected byte[] jpegPhoto;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getJpegPhoto()
	 */
	public byte[] getJpegPhoto()
	{
		return jpegPhoto;
	}

	/**
	 * @param jpegPhoto
	 *        The jpegPhoto to set.
	 */
	public void setJpegPhoto(byte[] jpegPhoto)
	{
		this.jpegPhoto = jpegPhoto;
	}

	protected String labeledURI;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getLabeledURI()
	 */
	public String getLabeledURI()
	{
		return labeledURI;
	}

	/**
	 * @param labeledURI
	 *        The labeledURI to set.
	 */
	public void setLabeledURI(String labeledURI)
	{
		this.labeledURI = labeledURI;
	}

	protected String mail;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getMail()
	 */
	public String getMail()
	{
		return mail;
	}

	/**
	 * @param mail
	 *        The mail to set.
	 */
	public void setMail(String mail)
	{
		this.mail = mail;
	}

	protected String manager;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getManager()
	 */
	public String getManager()
	{
		return manager;
	}

	/**
	 * @param manager
	 *        The manager to set.
	 */
	public void setManager(String manager)
	{
		this.manager = manager;
	}

	protected String mobile;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getMobile()
	 */
	public String getMobile()
	{
		return mobile;
	}

	/**
	 * @param mobile
	 *        The mobile to set.
	 */
	public void setMobile(String mobile)
	{
		this.mobile = mobile;
	}

	protected String organization;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getOrganization()
	 */
	public String getOrganization()
	{
		return organization;
	}

	/**
	 * @param organization
	 *        The organization to set.
	 */
	public void setOrganization(String organization)
	{
		this.organization = organization;
	}

	protected String pager;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getPager()
	 */
	public String getPager()
	{
		return pager;
	}

	/**
	 * @param pager
	 *        The pager to set.
	 */
	public void setPager(String pager)
	{
		this.pager = pager;
	}

	protected String preferredLanguage;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getPreferredLanguage()
	 */
	public String getPreferredLanguage()
	{
		return preferredLanguage;
	}

	/**
	 * @param preferredLanguage
	 *        The preferredLanguage to set.
	 */
	public void setPreferredLanguage(String preferredLanguage)
	{
		this.preferredLanguage = preferredLanguage;
	}

	protected String uid;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getUid()
	 */
	public String getUid()
	{
		return uid;
	}

	/**
	 * @param uid
	 *        The uid to set.
	 */
	public void setUid(String uid)
	{
		this.uid = uid;
	}

	protected byte[] userCertificate;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getUserCertificate()
	 */
	public byte[] getUserCertificate()
	{
		return userCertificate;
	}

	/**
	 * @param userCertificate
	 *        The userCertificate to set.
	 */
	public void setUserCertificate(byte[] userCertificate)
	{
		this.userCertificate = userCertificate;
	}

	protected byte[] userSMIMECertificate;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getUserSMIMECertificate()
	 */
	public byte[] getUserSMIMECertificate()
	{
		return userSMIMECertificate;
	}

	/**
	 * @param userSMIMECertificate
	 *        The userSMIMECertificate to set.
	 */
	public void setUserSMIMECertificate(byte[] userSMIMECertificate)
	{
		this.userSMIMECertificate = userSMIMECertificate;
	}

	protected String carLicense;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getCarLicense()
	 */
	public String getCarLicense()
	{
		return carLicense;
	}

	/**
	 * @param carLicense
	 *        The carLicense to set.
	 */
	public void setCarLicense(String carLicense)
	{
		this.carLicense = carLicense;
	}

	protected String displayName;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getDisplayName()
	 */
	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * @param displayName
	 *        The displayName to set.
	 */
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	protected String departmentNumber;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getDepartmentNumber()
	 */
	public String getDepartmentNumber()
	{
		return departmentNumber;
	}

	/**
	 * @param departmentNumber
	 *        The departmentNumber to set.
	 */
	public void setDepartmentNumber(String departmentNumber)
	{
		this.departmentNumber = departmentNumber;
	}

	protected String employeeNumber;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getEmployeeNumber()
	 */
	public String getEmployeeNumber()
	{
		return employeeNumber;
	}

	/**
	 * @param employeeNumber
	 *        The employeeNumber to set.
	 */
	public void setEmployeeNumber(String employeeNumber)
	{
		this.employeeNumber = employeeNumber;
	}

	protected String employeeType;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getEmployeeType()
	 */
	public String getEmployeeType()
	{
		return employeeType;
	}

	/**
	 * @param employeeType
	 *        The employeeType to set.
	 */
	public void setEmployeeType(String employeeType)
	{
		this.employeeType = employeeType;
	}

	protected byte[] userPKCS12;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getUserPKCS12()
	 */
	public byte[] getUserPKCS12()
	{
		return userPKCS12;
	}

	/**
	 * @param userPKCS12
	 *        The userPKCS12 to set.
	 */
	public void setUserPKCS12(byte[] userPKCS12)
	{
		this.userPKCS12 = userPKCS12;
	}

	protected String businessCategory;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getBusinessCategory()
	 */
	public String getBusinessCategory()
	{
		return businessCategory;
	}

	/**
	 * @param businessCategory
	 *        The businessCategory to set.
	 */
	public void setBusinessCategory(String businessCategory)
	{
		this.businessCategory = businessCategory;
	}

	protected String x500UniqueIdentifier;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getX500UniqueIdentifier()
	 */
	public String getX500UniqueIdentifier()
	{
		return x500UniqueIdentifier;
	}

	/**
	 * @param uniqueIdentifier
	 *        The x500UniqueIdentifier to set.
	 */
	public void setX500UniqueIdentifier(String uniqueIdentifier)
	{
		x500UniqueIdentifier = uniqueIdentifier;
	}

	protected String roomNumber;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getRoomNumber()
	 */
	public String getRoomNumber()
	{
		return roomNumber;
	}

	/**
	 * @param roomNumber
	 *        The roomNumber to set.
	 */
	public void setRoomNumber(String roomNumber)
	{
		this.roomNumber = roomNumber;
	}

	protected String secretary;

	/**
	 * @see org.sakaiproject.service.profile.InetOrgPerson#getSecretary()
	 */
	public String getSecretary()
	{
		return secretary;
	}

	/**
	 * @param secretary
	 *        The secretary to set.
	 */
	public void setSecretary(String secretary)
	{
		this.secretary = secretary;
	}

	public Blob getBlobImage()
	{
		if (this.jpegPhoto == null || jpegPhoto.length < 1 || ServerConfigurationService.getString("profile.photoRepositoryPath", null) != null)
		{
			return null;
		}
		try {
			return new SerialBlob(this.jpegPhoto);
		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
			return null;
		}
	}

	public void setBlobImage(Blob blobImage)
	{
		this.jpegPhoto = toByteArray(blobImage);
	}

	private byte[] toByteArray(Blob fromBlob)
	{
		if (log.isDebugEnabled())
		{
			log.debug("toByteArray(Blob " + fromBlob + ")");
		}

		try
		{
			if (fromBlob == null || fromBlob.length() < 1)
			{
				return null;
			}
		}
		catch (SQLException e1)
		{
			log.error(e1.getMessage(), e1);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			return toByteArray(fromBlob, baos);
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (baos != null)
			{
				try
				{
					baos.close();
				}
				catch (IOException ex)
				{
					log.error(ex.getMessage(), ex);
				}
			}
		}
	}

	private byte[] toByteArray(Blob fromBlob, ByteArrayOutputStream baos) throws SQLException, IOException
	{
		if (log.isDebugEnabled())
		{
			log.debug("toByteArray(Blob " + fromBlob + ", ByteArrayOutputStream " + baos + ")");
		}

		if (fromBlob == null || fromBlob.length() < 1 || ServerConfigurationService.getString("profile.photoRepositoryPath", null) != null)
		{
			return null;
		}
		byte[] buf = new byte[4000];
		InputStream is = fromBlob.getBinaryStream();
		try
		{
			for (;;)
			{
				int dataSize = is.read(buf);

				if (dataSize == -1) break;
				baos.write(buf, 0, dataSize);
			}
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException ex)
				{
					log.error(ex.getMessage(), ex);
				}
			}
		}
		return baos.toByteArray();
	}

}
