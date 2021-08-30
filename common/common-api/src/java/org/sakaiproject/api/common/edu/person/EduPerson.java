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
 * @see <a href="https://software.internet2.edu/eduperson/internet2-mace-dir-eduperson-201602.html">Internet2 EduPerson specification</a>. 
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
	
	/**
	 * Each value of this multi-valued attribute represents an ePPN (eduPersonPrincipalName) 
	 * value that was previously associated with the entry. The values MUST NOT 
	 * include the currently valid ePPN value. There is no implied or assumed order 
	 * to the values. This attribute MUST NOT be populated if ePPN values are ever 
	 * reassigned to a different entry (after, for example, a period of dormancy). 
	 * That is, they MUST be unique in space and over time.
	 * @return
	 */
	public String getPrincipalNamePrior();
	
	/**
	 * Each value of this multi-valued attribute represents an ePPN (eduPersonPrincipalName) 
	 * value that was previously associated with the entry. The values MUST NOT 
	 * include the currently valid ePPN value. There is no implied or assumed order 
	 * to the values. This attribute MUST NOT be populated if ePPN values are ever 
	 * reassigned to a different entry (after, for example, a period of dormancy). 
	 * That is, they MUST be unique in space and over time.
	 * @param PrincipalNamePrior
	 */
	public void setPrincipalNamePrior(String principalNamePrior);
	
	/**
	 * Specifies the person's affiliation within a particular security domain in 
	 * broad categories such as student, faculty, staff, alum, etc. The values consist 
	 * of a left and right component separated by an "@" sign. The left component 
	 * is one of the values from the eduPersonAffiliation controlled vocabulary.
	 * This right-hand side syntax of eduPersonScopedAffiliation intentionally 
	 * matches that used for the right-hand side values for eduPersonPrincipalName. 
	 * The "scope" portion MUST be the administrative domain to which the affiliation 
	 * applies. Multiple "@" signs are not recommended, but in any case, the first 
	 * occurrence of the "@" sign starting from the left is to be taken as the 
	 * delimiter between components. Thus, user identifier is to the left, security 
	 * domain to the right of the first "@". This parsing rule conforms to the POSIX 
	 * "greedy" disambiguation method in regular expression processing.
	 * @return
	 */
	public String getScopedAffiliation();
	
	/**
	 * Specifies the person's affiliation within a particular security domain in 
	 * broad categories such as student, faculty, staff, alum, etc. The values consist 
	 * of a left and right component separated by an "@" sign. The left component 
	 * is one of the values from the eduPersonAffiliation controlled vocabulary.
	 * This right-hand side syntax of eduPersonScopedAffiliation intentionally 
	 * matches that used for the right-hand side values for eduPersonPrincipalName. 
	 * The "scope" portion MUST be the administrative domain to which the affiliation 
	 * applies. Multiple "@" signs are not recommended, but in any case, the first 
	 * occurrence of the "@" sign starting from the left is to be taken as the 
	 * delimiter between components. Thus, user identifier is to the left, security 
	 * domain to the right of the first "@". This parsing rule conforms to the POSIX 
	 * "greedy" disambiguation method in regular expression processing.	 
	 * * @param scopedAffiliation
	 */
	public void  setScopedAffiliation(String scopedAffiliation);
	
	/**
	 * A persistent, non-reassigned, opaque identifier for a principal.
	 * @return
	 */
	public String getTargetedID();
	
	/**
	 * A persistent, non-reassigned, opaque identifier for a principal.
	 * @param targetedID
	 */
	public void setTargetedID(String targetedID);
	
	/**
	 * Set of URIs that assert compliance with specific standards for identity assurance.
	 * @return
	 */
	public String getAssurance();
	
	/**
	 * Set of URIs that assert compliance with specific standards for identity assurance.
	 * @param assurance
	 */
	public void setAssurance(String assurance);
	
	/**
	 * 	 * A long-lived, non re-assignable, omnidirectional identifier suitable for 
	 * use as a principal identifier by authentication providers or as a unique 
	 * external key by applications.
	 * @return
	 */
	public String getUniqueId();
	
	/**
	 * A long-lived, non re-assignable, omnidirectional identifier suitable for 
	 * use as a principal identifier by authentication providers or as a unique 
	 * external key by applications.
	 * @param uniqueId
	 */
	public void setUniqueId(String uniqueId);
	
	/**
	 * @return
	 */
	public String getOrcid();
	
	/**
	 * @param orcid
	 */
	public void setOrcid(String orcid);
}
