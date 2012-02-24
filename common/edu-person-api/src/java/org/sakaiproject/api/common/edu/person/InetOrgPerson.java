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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

/**
 * See RFC 2798.
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface InetOrgPerson extends OrganizationalPerson
{
	/**
	 * RFC 1274 notes that the proprietary format they recommend is "interim" only.
	 * 
	 * @return
	 */
	public BufferedInputStream getAudio();

	/**
	 * RFC 1274 notes that the proprietary format they recommend is "interim" only.
	 */
	public void setAudio(BufferedOutputStream audio);

	/**
	 * The name(s) that should appear in white-pages-like applications for this person. From RFC 2798 description: "preferred name of a person to be used when displaying entries."
	 * 
	 * @return
	 */
	public String getDisplayName();

	/**
	 * The name(s) that should appear in white-pages-like applications for this person. From RFC 2798 description: "preferred name of a person to be used when displaying entries."
	 * 
	 * @return
	 */
	public void setDisplayName(String displayName);

	/**
	 * From RFC 2256 description:" The givenName attribute is used to hold the part of a person's name which is not their surname nor middle name."
	 * 
	 * @return
	 */
	public String getGivenName();

	/**
	 * From RFC 2256 description:" The givenName attribute is used to hold the part of a person's name which is not their surname nor middle name."
	 * 
	 * @return
	 */
	public void setGivenName(String givenName);

	/**
	 * From RFC 1274 description: "The [homePhone] attribute type specifies a home telephone number associated with a person.� Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public String getHomePhone();

	/**
	 * From RFC 1274 description: "The [homePhone] attribute type specifies a home telephone number associated with a person.� Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public void setHomePhone(String homePhone);

	/**
	 * From RFC 1274 description: "The Home postal address attribute type specifies a home postal address for an object. This should be limited to up to 6 lines of 30 characters each."
	 * 
	 * @return
	 */
	public String getHomePostalAddress();

	/**
	 * From RFC 1274 description: "The Home postal address attribute type specifies a home postal address for an object. This should be limited to up to 6 lines of 30 characters each."
	 * 
	 * @return
	 */
	public void setHomePostalAddress(String homePostalAddress);

	/**
	 * From RFC 2256 description: "The initials attribute contains the initials of some or all of an individuals names, but not the surname(s)."
	 * 
	 * @return
	 */
	public String getInitials();

	/**
	 * From RFC 2256 description: "The initials attribute contains the initials of some or all of an individuals names, but not the surname(s)."
	 * 
	 * @return
	 */
	public void setInitials(String initials);

	/**
	 * Follow inetOrgPerson definition of RFC 2798: "Used to store one or more images of a person using the JPEG File Interchange Format [JFIF]."
	 * 
	 * @return
	 */
	public byte[] getJpegPhoto();

	/**
	 * Follow inetOrgPerson definition of RFC 2798: "Used to store one or more images of a person using the JPEG File Interchange Format [JFIF]."
	 * 
	 * @return
	 */
	public void setJpegPhoto(byte[] jpegPhoto);

	/**
	 * Follow inetOrgPerson definition of RFC 2079: "Uniform Resource Identifier with optional label."
	 * <p>
	 * Most commonly a URL for a web site associated with this person.
	 * 
	 * @return
	 */
	public String getLabeledURI();

	/**
	 * Follow inetOrgPerson definition of RFC 2079: "Uniform Resource Identifier with optional label."
	 * <p>
	 * Most commonly a URL for a web site associated with this person.
	 */
	public void setLabeledURI(String labeledURI);

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [mail] attribute type specifies an electronic mailbox attribute following the syntax specified in RFC 822. Note that this attribute should not be used for greybook or other non-Internet order
	 * mailboxes."
	 * 
	 * @return
	 */
	public String getMail();

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [mail] attribute type specifies an electronic mailbox attribute following the syntax specified in RFC 822. Note that this attribute should not be used for greybook or other non-Internet order
	 * mailboxes."
	 * 
	 * @return
	 */
	public void setMail(String mail);

	/**
	 * Follow inetOrgPerson definition which refers to RFC 1274: "The manager attribute type specifies the manager of an object represented by an entry." The value is a DN.
	 * 
	 * @return
	 */
	public String getManager();

	/**
	 * Follow inetOrgPerson definition which refers to RFC 1274: "The manager attribute type specifies the manager of an object represented by an entry." The value is a DN.
	 * 
	 * @return
	 */
	public void setManager(String manager);

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [mobile] attribute type specifies a mobile telephone number associated with a person. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public String getMobile();

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [mobile] attribute type specifies a mobile telephone number associated with a person. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public void setMobile(String mobile);

	/**
	 * Standard name of the top-level organization (institution) with which this person is associated.
	 * 
	 * @return
	 */
	public String getOrganization();

	/**
	 * Standard name of the top-level organization (institution) with which this person is associated.
	 */
	public void setOrganization(String organization);

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [pager] attribute type specifies a pager telephone number for an object. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public String getPager();

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [pager] attribute type specifies a pager telephone number for an object. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public void setPager(String pager);

	/**
	 * Follow inetOrgPerson definition of RFC 2798: "preferred written or spoken language for a person.
	 * <p>
	 * See RFC2068 and ISO 639 for allowable values in this field. Esperanto, for example is EO in ISO 639, and RFC2068 would allow a value of en-US for US English.
	 * 
	 * @return
	 */
	public String getPreferredLanguage();

	/**
	 * Follow inetOrgPerson definition of RFC 2798: "preferred written or spoken language for a person.�
	 * <p>
	 * See RFC2068 and ISO 639 for allowable values in this field. Esperanto, for example is EO in ISO 639, and RFC2068 would allow a value of en-US for US English.
	 * 
	 * @return
	 */
	public void setPreferredLanguage(String preferredLanguage);

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [uid] attribute type specifies a computer system login name."
	 * 
	 * @return
	 */
	public String getUid();

	/**
	 * Follow inetOrgPerson definition of RFC 1274: "The [uid] attribute type specifies a computer system login name."
	 * 
	 * @return
	 */
	public void setUid(String uid);

	/**
	 * A user's X.509 certificate
	 * 
	 * @return
	 */
	public byte[] getUserCertificate();

	/**
	 * A user's X.509 certificate
	 */
	public void setUserCertificate(byte[] userCertificate);

	/**
	 * An X.509 certificate specifically for use in S/MIME applications (see RFCs 2632, 2633 and 2634).
	 * 
	 * @return
	 */
	public byte[] getUserSMIMECertificate();

	/**
	 * An X.509 certificate specifically for use in S/MIME applications (see RFCs 2632, 2633 and 2634).
	 */
	public void setUserSMIMECertificate(byte[] userSMIMECertificate);

	/**
	 * RFC 2798
	 * <p>
	 * This multivalued field is used to record the values of the license or registration plate associated with an individual.
	 * 
	 * @return
	 */
	public String getCarLicense();

	/**
	 * RFC 2798
	 * <p>
	 * This multivalued field is used to record the values of the license or registration plate associated with an individual.
	 * 
	 * @return
	 */
	public void setCarLicense(String carLicense);

	/**
	 * RFC 2798
	 * <p>
	 * Code for department to which a person belongs. This can also be strictly numeric (e.g., 1234) or alphanumeric (e.g., ABC/123).
	 * 
	 * @return
	 */
	public String getDepartmentNumber();

	/**
	 * RFC 2798
	 * <p>
	 * Code for department to which a person belongs. This can also be strictly numeric (e.g., 1234) or alphanumeric (e.g., ABC/123).
	 * 
	 * @return
	 */
	public void setDepartmentNumber(String departmentNumber);

	/**
	 * RFC 2798
	 * <p>
	 * Numeric or alphanumeric identifier assigned to a person, typically based on order of hire or association with an organization. Single valued.
	 * 
	 * @return
	 */
	public String getEmployeeNumber();

	/**
	 * RFC 2798
	 * <p>
	 * Numeric or alphanumeric identifier assigned to a person, typically based on order of hire or association with an organization. Single valued.
	 * 
	 * @return
	 */
	public void setEmployeeNumber(String employeeNumber);

	/**
	 * RFC 2798
	 * <p>
	 * Used to identify the employer to employee relationship. Typical values used will be "Contractor", "Employee", "Intern", "Temp", "External", and "Unknown" but any value may be used.
	 * 
	 * @return
	 */
	public String getEmployeeType();

	/**
	 * RFC 2798
	 * <p>
	 * Used to identify the employer to employee relationship. Typical values used will be "Contractor", "Employee", "Intern", "Temp", "External", and "Unknown" but any value may be used.
	 * 
	 * @return
	 */
	public void setEmployeeType(String employeeType);

	/**
	 * PKCS #12 [PKCS12] provides a format for exchange of personal identity information. When such information is stored in a directory service, the userPKCS12 attribute should be used. This attribute is to be stored and requested in binary form, as
	 * 'userPKCS12;binary'. The attribute values are PFX PDUs stored as binary data.
	 * 
	 * @return
	 */
	public byte[] getUserPKCS12();

	/**
	 * PKCS #12 [PKCS12] provides a format for exchange of personal identity information. When such information is stored in a directory service, the userPKCS12 attribute should be used. This attribute is to be stored and requested in binary form, as
	 * 'userPKCS12;binary'. The attribute values are PFX PDUs stored as binary data.
	 * 
	 * @return
	 */
	public void setUserPKCS12(byte[] userPKCS12);

	/**
	 * RFC 2798
	 * 
	 * @return
	 */
	public String getBusinessCategory();

	/**
	 * RFC 2798
	 */
	public void setBusinessCategory(String businessCategory);

	/**
	 * RFC 2798 - Likely to change if directory is reloaded with data.
	 * 
	 * @return
	 */
	public String getX500UniqueIdentifier();

	/**
	 * RFC 2798 - Likely to change if directory is reloaded with data.
	 */
	public void setX500UniqueIdentifier(String uniqueIdentifier);

	/**
	 * RFC 2798
	 * 
	 * @return
	 */
	public String getRoomNumber();

	/**
	 * RFC 2798
	 */
	public void setRoomNumber(String roomNumber);

	/**
	 * RFC 2798
	 * 
	 * @return
	 */
	public String getSecretary();

	/**
	 * RFC 2798
	 */
	public void setSecretary(String secretary);
}
