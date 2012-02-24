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

import org.sakaiproject.api.common.manager.Persistable;

/**
 * See ITU X.521 spec.
 * 
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface Person extends Persistable
{
	/**
	 * @return The UUID of the Agent for which this record describes.
	 */
	public String getAgentUuid();

	/**
	 * @param agentUuid
	 *        The UUID of the Agent for which this record describes.
	 */
	public void setAgentUuid(String agentUuid);

	/**
	 * @return
	 */
	public String getTypeUuid();

	/**
	 * @param typeUuid
	 */
	public void setTypeUuid(String typeUuid);

	/**
	 * Common name. According to RFC 2256, "This is the X.500 commonName attribute, which contains a name of an object. If the object corresponds to a person, it is typically the person's full name.
	 * 
	 * @return
	 */
	public String getCommonName();

	/**
	 * Common name. According to RFC 2256, "This is the X.500 commonName attribute, which contains a name of an object. If the object corresponds to a person, it is typically the person's full name.
	 */
	public void setCommonName(String commonName);

	/**
	 * Open-ended; whatever the person or the directory manager puts here. According to RFC 2256, "This attribute contains a human-readable description of the object."
	 * 
	 * @return
	 */
	public String getDescription();

	/**
	 * Open-ended; whatever the person or the directory manager puts here. According to RFC 2256, "This attribute contains a human-readable description of the object."
	 */
	public void setDescription(String description);

	/**
	 * Follow person object class definition: Identifies (by DN) another directory server entry that may contain information related to this entry.
	 * <p>
	 * According to X.520(2000), "The See Also attribute type specifies names of other Directory objects which may be other aspects (in some sense) of the same real world object."
	 * 
	 * @return
	 */
	public String getSeeAlso();

	/**
	 * Follow person object class definition: Identifies (by DN) another directory server entry that may contain information related to this entry.
	 * <p>
	 * According to X.520(2000), "The See Also attribute type specifies names of other Directory objects which may be other aspects (in some sense) of the same real world object."
	 * 
	 * @return
	 */
	public void setSeeAlso(String seeAlso);

	/**
	 * Surname or family name. According to RFC 2256, "This is the X.500 surname attribute, which contains the family name of a person."
	 * 
	 * @return
	 */
	public String getSurname();

	/**
	 * Surname or family name. According to RFC 2256, "This is the X.500 surname attribute, which contains the family name of a person."
	 */
	public void setSurname(String surname);

	/**
	 * According to RFC 2256, "This attribute contains the physical address of the object to which the entry corresponds, such as an address for package delivery (streetAddress)."
	 * 
	 * @return
	 */
	public String getStreet();

	/**
	 * According to RFC 2256, "This attribute contains the physical address of the object to which the entry corresponds, such as an address for package delivery (streetAddress)."
	 * 
	 * @return
	 */
	public void setStreet(String street);

	/**
	 * Office/campus phone number. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public String getTelephoneNumber();

	/**
	 * Office/campus phone number. Attribute values should follow the agreed format for international telephone numbers: i.e., "+44 71 123 4567."
	 * 
	 * @return
	 */
	public void setTelephoneNumber(String telephoneNumber);

	/*
	 * This seems too danegrous to expose. // public String getUserPassword();
	 */
}
