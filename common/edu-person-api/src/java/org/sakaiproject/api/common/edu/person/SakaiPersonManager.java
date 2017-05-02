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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.api.common.type.Type;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface SakaiPersonManager
{
	public static final String PROFILE_UPDATE = "profile.update";
	public static final String PROFILE_DELETE = "profile.delete";

	/**
	 * Creates a persistent SakaiPerson record.
	 * 
	 * @param agentUuid
	 * @param recordType
	 *        {@link #getSystemMutableType()} or {@link #getUserMutableType()}
	 * @return
	 */
	public SakaiPerson create(String agentUuid, Type recordType);

	/**
	 * Get a new instantiation of an empty SakaiPerson object (has no persistent state). For example, useful if you query-by-example finder method.
	 * 
	 * @return
	 */
	public SakaiPerson getPrototype();

	/**
	 * Retrieve SakaiPerson by uid (username).
	 * 
	 * @param uid
	 *        username
	 * @return List of SakaiPerson objects incuding both system and user mutable Types.
	 */
	public List<SakaiPerson> findSakaiPersonByUid(String uid);

	/**
	 * Query-by-Example finder signature.
	 * 
	 * @param queryByExample
	 *        A SakaiPerson protoype. All non-null preoperties will be searched using a logical AND.
	 * @return
	 */
	public List<SakaiPerson> findSakaiPerson(SakaiPerson queryByExample);

	/**
	 * Assumes current user. If you would like to specify the user, see {@link #findSakaiPerson(String, Type)}.
	 * 
	 * @param recordType
	 *        See {@link #getSystemMutableType()} or {@link #getUserMutableType()}.
	 * @return
	 */
	public SakaiPerson getSakaiPerson(Type recordType);

	/**
	 * Find all SakaiPerson objects with specified type. Types should be obtained through the Type constant getter methods.
	 * 
	 * @param agent
	 * @param recordType
	 *        See {@link #getSystemMutableType()} or {@link #getUserMutableType()}.
	 * @return
	 */
	public SakaiPerson getSakaiPerson(String agentUuid, Type recordType);

	/**
	 * Finds all SakaiPerson objects with the specified type, whos IDs are contained
	 * in the userIds collection.
	 * 
	 * @param userIds
	 * @param userMutableType
	 * @return
	 */
	public Map<String, SakaiPerson> getSakaiPersons(Set<String> userIds, Type userMutableType);

	/**
	 * Returns the userMutableType constant. SakaiPerson's of this Type allow the user to modify all attributes.
	 * 
	 * @return
	 */
	public Type getUserMutableType();

	/**
	 * Returns the systemMutableType constant. SakaiPerson's of this Type can only be modified by the "system", i.e. not the end user, and would normally consist of enterprise data (e.g. LDAP, etc).
	 * 
	 * @return
	 */
	public Type getSystemMutableType();

	/**
	 * Save or update the SakaiPerson bean.
	 * 
	 * @param sakaiPerson
	 */
	public void save(SakaiPerson sakaiPerson);

	/**
	 * Removes SakaiPerson from persistent state.
	 * 
	 * @param sakaiPerson
	 */
	public void delete(SakaiPerson sakaiPerson);

	/**
	 * Search the "common" SakaiPerson fields for a given String.
	 * 
	 * @param simpleSearchCriteria
	 *        String used to search for SakaiPerson objects where the following properties are like this String: uid, givenName, surname.
	 * @return
	 */
	public List<SakaiPerson> findSakaiPerson(String simpleSearchCriteria);

	/**
	 * Composite call to determine if a Set of Agents have the FERPA flag enabled.
	 * 
	 * @param agentUuids
	 * @return A List of agentUuid Strings where FERPA is enabled.
	 */
	public List<String> isFerpaEnabled(Collection<String> agentUuids);

	/**
	 * Find all SakaiPerson objects where ferpaEnabled == TRUE.
	 * 
	 * @return A List of SakaiPerson objects where ferpaEnabled == TRUE.
	 */
	public List<SakaiPerson> findAllFerpaEnabled();
	
	
}
