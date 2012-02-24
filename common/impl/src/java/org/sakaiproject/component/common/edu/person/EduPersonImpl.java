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

import org.sakaiproject.api.common.edu.person.EduPerson;
import org.sakaiproject.api.common.edu.person.InetOrgPerson;
import org.sakaiproject.api.common.edu.person.OrganizationalPerson;
import org.sakaiproject.api.common.edu.person.Person;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public class EduPersonImpl extends InetOrgPersonImpl implements Person, OrganizationalPerson, InetOrgPerson, EduPerson
{

	protected String affiliation;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getAffiliation()
	 */
	public String getAffiliation()
	{
		return affiliation;
	}

	/**
	 * @param affiliation
	 *        The affiliation to set.
	 */
	public void setAffiliation(String affiliation)
	{
		this.affiliation = affiliation;
	}

	protected String entitlement;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getEntitlement()
	 */
	public String getEntitlement()
	{
		return entitlement;
	}

	/**
	 * @param entitlement
	 *        The entitlement to set.
	 */
	public void setEntitlement(String entitlement)
	{
		this.entitlement = entitlement;
	}

	protected String nickname;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getNickname()
	 */
	public String getNickname()
	{
		return nickname;
	}

	/**
	 * @param nickname
	 *        The nickname to set.
	 */
	public void setNickname(String nickname)
	{
		this.nickname = nickname;
	}

	protected String orgDn;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getOrgDn()
	 */
	public String getOrgDn()
	{
		return orgDn;
	}

	/**
	 * @param orgDn
	 *        The orgDn to set.
	 */
	public void setOrgDn(String orgDn)
	{
		this.orgDn = orgDn;
	}

	protected String orgUnitDn;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getOrgUnitDn()
	 */
	public String getOrgUnitDn()
	{
		return orgUnitDn;
	}

	/**
	 * @param orgUnitDn
	 *        The orgUnitDn to set.
	 */
	public void setOrgUnitDn(String orgUnitDn)
	{
		this.orgUnitDn = orgUnitDn;
	}

	protected String primaryAffiliation;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getPrimaryAffiliation()
	 */
	public String getPrimaryAffiliation()
	{
		return primaryAffiliation;
	}

	/**
	 * @param primaryAffiliation
	 *        The primaryAffiliation to set.
	 */
	public void setPrimaryAffiliation(String primaryAffiliation)
	{
		this.primaryAffiliation = primaryAffiliation;
	}

	protected String primaryOrgUnitDn;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getPrimaryOrgUnitDn()
	 */
	public String getPrimaryOrgUnitDn()
	{
		return primaryOrgUnitDn;
	}

	/**
	 * @param primaryOrgUnitDn
	 *        The primaryOrgUnitDn to set.
	 */
	public void setPrimaryOrgUnitDn(String primaryOrgUnitDn)
	{
		this.primaryOrgUnitDn = primaryOrgUnitDn;
	}

	protected String principalName;

	/**
	 * @see org.sakaiproject.service.profile.EduPerson#getPrincipalName()
	 */
	public String getPrincipalName()
	{
		return principalName;
	}

	/**
	 * @param principalName
	 *        The principalName to set.
	 */
	public void setPrincipalName(String principalName)
	{
		this.principalName = principalName;
	}

}
