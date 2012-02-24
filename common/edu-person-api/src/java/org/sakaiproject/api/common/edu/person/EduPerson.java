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
 * See Internet2 EduPerson specification.
 * <p>
 * EduPerson is an auxiliary object class for campus directories designed to facilitate communication among higher education institutions. It consists of a set of data elements or attributes about individuals within higher education, along with
 * recommendations on the syntax and semantics of the data that may be assigned to those attributes.
 * </p>
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface EduPerson extends InetOrgPerson
{
	/**
	 * Specifies the person's relationship(s) to the institution in broad categories such as student, faculty, staff, alum, etc. (See controlled vocabulary). Permissible values (if controlled) faculty, student, staff, alum, member, affiliate, employee.
	 * 
	 * @return
	 */
	public String getAffiliation();

	/**
	 * Specifies the person's relationship(s) to the institution in broad categories such as student, faculty, staff, alum, etc. (See controlled vocabulary). Permissible values (if controlled) faculty, student, staff, alum, member, affiliate, employee.
	 * 
	 * @return
	 */
	public void setAffiliation(String affiliation);

	/**
	 * URI (either URN or URL) that indicates a set of rights to specific resources.
	 * 
	 * @return
	 */
	public String getEntitlement();

	/**
	 * URI (either URN or URL) that indicates a set of rights to specific resources.
	 */
	public void setEntitlement(String entitlement);

	/**
	 * Person's nickname, or the informal name by which they are accustomed to be hailed.
	 * 
	 * @return
	 */
	public String getNickname();

	/**
	 * Person's nickname, or the informal name by which they are accustomed to be hailed.
	 */
	public void setNickname(String nickname);

	/**
	 * The distinguished name (DN) of the of the directory entry representing the institution with which the person is associated.
	 * 
	 * @return
	 */
	public String getOrgDn();

	/**
	 * The distinguished name (DN) of the of the directory entry representing the institution with which the person is associated.
	 */
	public void setOrgDn(String orgDn);

	/**
	 * The distinguished name(s) (DN) of the directory entries representing the person's Organizational Unit(s). May be multivalued, as for example, in the case of a faculty member with appointments in multiple departments or a person who is a student in
	 * one department and an employee in another.
	 * 
	 * @return
	 */
	public String getOrgUnitDn();

	/**
	 * The distinguished name(s) (DN) of the directory entries representing the person's Organizational Unit(s). May be multivalued, as for example, in the case of a faculty member with appointments in multiple departments or a person who is a student in
	 * one department and an employee in another.
	 * 
	 * @return
	 */
	public void setOrgUnitDn(String orgUnitDn);

	/**
	 * Specifies the person's PRIMARY relationship to the institution in broad categories such as student, faculty, staff, alum, etc. (See controlled vocabulary). Permissible values (if controlled) faculty, student, staff, alum, member, affiliate,
	 * employee.
	 * 
	 * @return
	 */
	public String getPrimaryAffiliation();

	/**
	 * Specifies the person's PRIMARY relationship to the institution in broad categories such as student, faculty, staff, alum, etc. (See controlled vocabulary). Permissible values (if controlled) faculty, student, staff, alum, member, affiliate,
	 * employee.
	 * 
	 * @return
	 */
	public void setPrimaryAffiliation(String primaryAffiliation);

	/**
	 * The distinguished name (DN) of the directory entry representing the person's primary Organizational Unit(s).
	 * 
	 * @return
	 */
	public String getPrimaryOrgUnitDn();

	/**
	 * The distinguished name (DN) of the directory entry representing the person's primary Organizational Unit(s).
	 */
	public void setPrimaryOrgUnitDn(String primaryOrgUnitDn);

	/**
	 * The "NetID" of the person for the purposes of inter-institutional authentication. Should be stored in the form of user_at_univ.edu, where univ.edu is the name of the local security domain.
	 * 
	 * @return
	 */
	public String getPrincipalName();

	/**
	 * The "NetID" of the person for the purposes of inter-institutional authentication. Should be stored in the form of user_at_univ.edu, where univ.edu is the name of the local security domain.
	 * 
	 * @return
	 */
	public void setPrincipalName(String principalName);
}
