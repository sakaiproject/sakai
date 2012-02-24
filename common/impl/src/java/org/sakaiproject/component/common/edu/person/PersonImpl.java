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

import org.sakaiproject.api.common.edu.person.Person;
import org.sakaiproject.component.common.manager.PersistableImpl;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public class PersonImpl extends PersistableImpl implements Person
{
	protected String businessKey()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(agentUuid);
		sb.append(typeUuid);
		return sb.toString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (!(obj instanceof PersonImpl)) return false;
		PersonImpl other = (PersonImpl) obj;
		return this.businessKey().equals(other.businessKey());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return businessKey().hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{id=");
		sb.append(id);
		sb.append(", uuid=");
		sb.append(uuid);
		sb.append(", typeUuid=");
		sb.append(typeUuid);
		sb.append("}");
		return sb.toString();
	}

	protected String agentUuid;

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPerson#getAgentUuid()
	 */
	public String getAgentUuid()
	{
		return agentUuid;
	}

	/**
	 * @see org.sakaiproject.api.common.edu.person.SakaiPerson#setAgentUuid(java.lang.String)
	 */
	public void setAgentUuid(String agentUuid)
	{
		this.agentUuid = agentUuid;
	}

	protected String typeUuid;

	/**
	 * @return Returns the typeUuid.
	 */
	public String getTypeUuid()
	{
		return typeUuid;
	}

	/**
	 * @param typeUuid
	 *        The typeUuid to set.
	 */
	public void setTypeUuid(String typeUuid)
	{
		this.typeUuid = typeUuid;
	}

	protected String commonName;

	/**
	 * @see org.sakaiproject.service.profile.Person#getCommonName()
	 */
	public String getCommonName()
	{
		return commonName;
	}

	/**
	 * @param commonName
	 *        The commonName to set.
	 */
	public void setCommonName(String commonName)
	{
		this.commonName = commonName;
	}

	protected String description;

	/**
	 * @see org.sakaiproject.service.profile.Person#getDescription()
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * @param description
	 *        The description to set.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	protected String seeAlso;

	/**
	 * @see org.sakaiproject.service.profile.Person#getSeeAlso()
	 */
	public String getSeeAlso()
	{
		return seeAlso;
	}

	/**
	 * @param seeAlso
	 *        The seeAlso to set.
	 */
	public void setSeeAlso(String seeAlso)
	{
		this.seeAlso = seeAlso;
	}

	protected String surname;

	/**
	 * @see org.sakaiproject.service.profile.Person#getSurname()
	 */
	public String getSurname()
	{
		return surname;
	}

	/**
	 * @param surname
	 *        The surname to set.
	 */
	public void setSurname(String surname)
	{
		this.surname = surname;
	}

	protected String street;

	/**
	 * @see org.sakaiproject.service.profile.Person#getStreet()
	 */
	public String getStreet()
	{
		return street;
	}

	/**
	 * @param street
	 *        The street to set.
	 */
	public void setStreet(String street)
	{
		this.street = street;
	}

	protected String telephoneNumber;

	/**
	 * @see org.sakaiproject.service.profile.Person#getTelephoneNumber()
	 */
	public String getTelephoneNumber()
	{
		return telephoneNumber;
	}

	/**
	 * @param telephoneNumber
	 *        The telephoneNumber to set.
	 */
	public void setTelephoneNumber(String telephoneNumber)
	{
		this.telephoneNumber = telephoneNumber;
	}

}
