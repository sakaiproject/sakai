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

package org.sakaiproject.api.common.edu.person;

/**
 * See ITU X.521 spec.
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface OrganizationalPerson extends Person
{
	/**
	 * A fax number for the directory entry. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public String getFacsimileTelephoneNumber();

	/**
	 * A fax number for the directory entry. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * 
	 */
	public void setFacsimileTelephoneNumber(String facsimileTelephoneNumber);

	/**
	 * According to RFC 2256, "This attribute contains the name of a locality, such as a city, county or other geographic region (localityName)."
	 * <p>
	 * X.520(2000) reads: "The Locality Name attribute type specifies a locality. When used as a component of a directory name, it identifies a geographical area or locality in which the named object is physically located or with which it is associated in
	 * some other important way.
	 * 
	 * @return
	 */
	public String getLocalityName();

	/**
	 * According to RFC 2256, "This attribute contains the name of a locality, such as a city, county or other geographic region (localityName)."
	 * <p>
	 * X.520(2000) reads: "The Locality Name attribute type specifies a locality. When used as a component of a directory name, it identifies a geographical area or locality in which the named object is physically located or with which it is associated in
	 * some other important way.
	 * 
	 * 
	 */
	public void setLocalityName(String localityName);

	/**
	 * Abbreviation for state or province name.
	 * <p>
	 * Format: The values should be coordinated on a national level and if well-known shortcuts exist - like the two-letter state abbreviations in the US these abbreviations are preferred over longer full names.
	 * <p>
	 * According to RFC 2256, "This attribute contains the full name of a state or province (stateOrProvinceName)."
	 * <p>
	 * Permissible values (if controlled)
	 * <p>
	 * For states in the United States, U.S. Postal Service set of two-letter state name abbreviations.
	 * 
	 * @return
	 */
	public String getStateOrProvinceName();

	/**
	 * Abbreviation for state or province name.
	 * <p>
	 * Format: The values should be coordinated on a national level and if well-known shortcuts exist - like the two-letter state abbreviations in the US  these abbreviations are preferred over longer full names.
	 * <p>
	 * According to RFC 2256, "This attribute contains the full name of a state or province (stateOrProvinceName)."
	 * <p>
	 * Permissible values (if controlled)
	 * <p>
	 * For states in the United States, U.S. Postal Service set of two-letter state name abbreviations.
	 * 
	 * 
	 */
	public void setStateOrProvinceName(String stateOrProvinceName);

	/**
	 * Follow X.500(2001): "The postal code attribute type specifies the postal code of the named object. If this attribute value is present, it will be part of the object's postal address." Zip code in USA, postal code for other countries.
	 * 
	 * @return
	 */
	public String getPostalCode();

	/**
	 * Follow X.500(2001): "The postal code attribute type specifies the postal code of the named object. If this attribute value is present, it will be part of the object's postal address." Zip code in USA, postal code for other countries.
	 * 
	 * 
	 */
	public void setPostalCode(String postalCode);

	/**
	 * Follow X.500(2001): "The Post Office Box attribute type specifies the Postal Office Box by which the object will receive physical postal delivery. If present, the attribute value is part of the object's postal address."
	 * 
	 * 
	 */
	public String getPostOfficeBox();

	/**
	 * Follow X.500(2001): "The Post Office Box attribute type specifies the Postal Office Box by which the object will receive physical postal delivery. If present, the attribute value is part of the object's postal address."
	 * 
	 * 
	 */
	public void setPostOfficeBox(String postOfficeBox);

	/**
	 * From X.521 spec: LocaleAttributeSet, PostalAttributeSet
	 * 
	 * @return
	 */
	public String getStreetAddress();

	/**
	 * From X.521 spec: LocaleAttributeSet, PostalAttributeSet
	 */
	public void setStreetAddress(String streetAddress);

	/**
	 * From X.521 spec: PostalAttributeSet
	 * 
	 * @return
	 */
	public String getPhysicalDeliveryOfficeName();

	/**
	 * From X.521 spec: PostalAttributeSet
	 */
	public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName);

	/**
	 * From X.521 spec: PostalAttributeSet
	 * <p>
	 * Campus or office address. inetOrgPerson has a homePostalAddress that complements this attribute. X.520(2000) reads: "The Postal Address attribute type specifies the address information required for the physical postal delivery to an object."
	 * 
	 * @return
	 */
	public String getPostalAddress();

	/**
	 * From X.521 spec: PostalAttributeSet
	 * <p>
	 * Campus or office address. inetOrgPerson has a homePostalAddress that complements this attribute. X.520(2000) reads: "The Postal Address attribute type specifies the address information required for the physical postal delivery to an object."
	 * 
	 * 
	 */
	public void setPostalAddress(String postalAddress);

	/**
	 * Follow X.520(2001): "The Title attribute type specifies the designated position or function of the object within an organization."
	 * 
	 * @return
	 */
	public String getTitle();

	/**
	 * Follow X.520(2001): "The Title attribute type specifies the designated position or function of the object within an organization."
	 * 
	 * 
	 */
	public void setTitle(String title);

	/**
	 * Organizational unit(s). According to X.520(2000), "The Organizational Unit Name attribute type specifies an organizational unit. When used as a component of a directory name it identifies an organizational unit with which the named object is
	 * affiliated.
	 * <p>
	 * The designated organizational unit is understood to be part of an organization designated by an OrganizationName [o] attribute. It follows that if an Organizational Unit Name attribute is used in a directory name, it must be associated with an
	 * OrganizationName [o] attribute.
	 * <p>
	 * An attribute value for Organizational Unit Name is a string chosen by the organization of which it is a part."
	 * 
	 * @return
	 */
	public String getOrganizationalUnit();

	/**
	 * Organizational unit(s). According to X.520(2000), "The Organizational Unit Name attribute type specifies an organizational unit. When used as a component of a directory name it identifies an organizational unit with which the named object is
	 * affiliated.
	 * <p>
	 * The designated organizational unit is understood to be part of an organization designated by an OrganizationName [o] attribute. It follows that if an Organizational Unit Name attribute is used in a directory name, it must be associated with an
	 * OrganizationName [o] attribute.
	 * <p>
	 * An attribute value for Organizational Unit Name is a string chosen by the organization of which it is a part."
	 * 
	 * 
	 */
	public void setOrganizationalUnit(String organizationalUnit);
}
