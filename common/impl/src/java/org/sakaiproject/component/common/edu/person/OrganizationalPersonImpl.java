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

import org.sakaiproject.api.common.edu.person.OrganizationalPerson;
import org.sakaiproject.api.common.edu.person.Person;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public class OrganizationalPersonImpl extends PersonImpl implements Person, OrganizationalPerson
{
	protected String facsimileTelephoneNumber;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getFacsimileTelephoneNumber()
	 */
	public String getFacsimileTelephoneNumber()
	{
		return facsimileTelephoneNumber;
	}

	/**
	 * @param facsimileTelephoneNumber
	 *        The facsimileTelephoneNumber to set.
	 */
	public void setFacsimileTelephoneNumber(String facsimileTelephoneNumber)
	{
		this.facsimileTelephoneNumber = facsimileTelephoneNumber;
	}

	protected String localityName;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getLocalityName()
	 */
	public String getLocalityName()
	{
		return localityName;
	}

	/**
	 * @param localityName
	 *        The localityName to set.
	 */
	public void setLocalityName(String localityName)
	{
		this.localityName = localityName;
	}

	protected String stateOrProvinceName;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getStateOrProvinceName()
	 */
	public String getStateOrProvinceName()
	{
		return stateOrProvinceName;
	}

	/**
	 * @param stateOrProvinceName
	 *        The stateOrProvinceName to set.
	 */
	public void setStateOrProvinceName(String stateOrProvinceName)
	{
		this.stateOrProvinceName = stateOrProvinceName;
	}

	protected String postalCode;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getPostalCode()
	 */
	public String getPostalCode()
	{
		return postalCode;
	}

	/**
	 * @param postalCode
	 *        The postalCode to set.
	 */
	public void setPostalCode(String postalCode)
	{
		this.postalCode = postalCode;
	}

	protected String postOfficeBox;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getPostOfficeBox()
	 */
	public String getPostOfficeBox()
	{
		return postOfficeBox;
	}

	/**
	 * @param postOfficeBox
	 *        The postOfficeBox to set.
	 */
	public void setPostOfficeBox(String postOfficeBox)
	{
		this.postOfficeBox = postOfficeBox;
	}

	protected String streetAddress;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getStreetAddress()
	 */
	public String getStreetAddress()
	{
		return streetAddress;
	}

	/**
	 * @param streetAddress
	 *        The streetAddress to set.
	 */
	public void setStreetAddress(String streetAddress)
	{
		this.streetAddress = streetAddress;
	}

	protected String physicalDeliveryOfficeName;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getPhysicalDeliveryOfficeName()
	 */
	public String getPhysicalDeliveryOfficeName()
	{
		return physicalDeliveryOfficeName;
	}

	/**
	 * @param physicalDeliveryOfficeName
	 *        The physicalDeliveryOfficeName to set.
	 */
	public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName)
	{
		this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
	}

	protected String postalAddress;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getPostalAddress()
	 */
	public String getPostalAddress()
	{
		return postalAddress;
	}

	/**
	 * @param postalAddress
	 *        The postalAddress to set.
	 */
	public void setPostalAddress(String postalAddress)
	{
		this.postalAddress = postalAddress;
	}

	protected String title;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getTitle()
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title
	 *        The title to set.
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	protected String organizationalUnit;

	/**
	 * @see org.sakaiproject.service.profile.OrganizationalPerson#getOrganizationalUnit()
	 */
	public String getOrganizationalUnit()
	{
		return organizationalUnit;
	}

	/**
	 * @param organizationalUnit
	 *        The organizationalUnit to set.
	 */
	public void setOrganizationalUnit(String organizationalUnit)
	{
		this.organizationalUnit = organizationalUnit;
	}
}
