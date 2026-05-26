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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.sakaiproject.api.common.type.Type;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public interface SakaiPersonManager
{
	String PROFILE_UPDATE = "profile.update";
	String PROFILE_DELETE = "profile.delete";

    /**
     * Creates a persistent SakaiPerson record.
     *
     * @param agentUuid The user ID (must not be blank)
     * @param recordType The type of record to create - must be either
     *        {@link #getSystemMutableType()} for system-managed records or
     *        {@link #getUserMutableType()} for user-managed records
     * @return Optional containing the newly created and persisted SakaiPerson instance, or empty Optional when:
     *         <p>- agentUuid is blank
     *         <p>- recordType is null or not supported
     *         <p>- the user does not exist in the system
     *         <p>- any error occurs during creation
     *         <p>The returned object will have its locked status set to false,
     *         and if updateUserProfile is enabled and recordType is userMutable,
     *         will be populated with the user's first name, last name, and email
     */
    Optional<SakaiPerson> create(String agentUuid, Type recordType);

	/**
	 * Get a new instantiation of an empty SakaiPerson object (has no persistent state). For example, useful if you query-by-example finder method.
	 * 
	 * @return
	 */
	SakaiPerson getPrototype();

	/**
	 * Retrieve SakaiPerson by uid (username).
	 * 
	 * @param uid
	 *        username
	 * @return List of SakaiPerson objects incuding both system and user mutable Types.
	 */
	List<SakaiPerson> findSakaiPersonByUid(String uid);

	/**
	 * Query-by-Example finder signature.
	 * 
	 * @param queryByExample
	 *        A SakaiPerson protoype. All non-null preoperties will be searched using a logical AND.
	 * @return
	 */
	List<SakaiPerson> findSakaiPerson(SakaiPerson queryByExample);

    /**
     * Gets the SakaiPerson record for the current user with the specified record type.
     * This method uses the currently logged-in user's ID. For retrieving records for
     * a specific user, use {@link #getSakaiPerson(String, Type)} instead.
     *
     * @param recordType The type of record to retrieve - must be either 
     *                  {@link #getSystemMutableType()} for system-managed records or
     *                  {@link #getUserMutableType()} for user-managed records
     * @return Optional containing the matching SakaiPerson record if found, or empty Optional
     *         if no matching record exists or if the record type is invalid
	 */
    Optional<SakaiPerson> getSakaiPerson(Type recordType);

    /**
     * Find all SakaiPerson objects with specified type. Types should be obtained through the Type constant getter methods.
     *
     * @param agentUuid The unique identifier for the user/agent
     * @param recordType The type of SakaiPerson record to retrieve - must be either {@link #getSystemMutableType()} or {@link #getUserMutableType()}
     * @return Optional containing the matching SakaiPerson record if found, or empty Optional if no matching record exists or if the record type is invalid
	 */
	Optional<SakaiPerson> getSakaiPerson(String agentUuid, Type recordType);

	/**
	 * Finds all SakaiPerson objects with the specified type, whos IDs are contained
	 * in the userIds collection.
	 * 
	 * @param userIds
	 * @param userMutableType
	 * @return
	 */
	Map<String, SakaiPerson> getSakaiPersons(Set<String> userIds, Type userMutableType);

	/**
	 * Returns the userMutableType constant. SakaiPerson's of this Type allow the user to modify all attributes.
	 * 
	 * @return
	 */
	Type getUserMutableType();

	/**
	 * Returns the systemMutableType constant. SakaiPerson's of this Type can only be modified by the "system", i.e. not the end user, and would normally consist of enterprise data (e.g. LDAP, etc).
	 * 
	 * @return
	 */
	Type getSystemMutableType();

	/**
	 * Save or update the SakaiPerson bean.
	 * 
	 * @param sakaiPerson
	 */
	void save(SakaiPerson sakaiPerson);

	/**
	 * Removes SakaiPerson from persistent state.
	 * 
	 * @param sakaiPerson
	 */
	default void delete(SakaiPerson sakaiPerson) {
		Objects.requireNonNull(sakaiPerson, "SakaiPerson cannot be null");
		delete(sakaiPerson.getAgentUuid());
	}

	/**
	 * Removes SakaiPerson records from persistent state for the specified user.
	 * This will delete all SakaiPerson records (both system-managed and user-managed types)
	 * associated with the given user ID. A PROFILE_DELETE event will be posted for the deletion.
	 *
	 * @param userId The unique identifier (agentUuid) of the user whose SakaiPerson records should be deleted
	 */
	void delete(String userId);

	/**
	 * Search the "common" SakaiPerson fields for a given String.
	 * 
	 * @param simpleSearchCriteria
	 *        String used to search for SakaiPerson objects where the following properties are like this String: uid, givenName, surname.
	 * @return
	 */
	List<SakaiPerson> findSakaiPerson(String simpleSearchCriteria);

	/**
	 * Composite call to determine if a Set of Agents have the FERPA flag enabled.
	 * 
	 * @param agentUuids
	 * @return A List of agentUuid Strings where FERPA is enabled.
	 */
	List<String> isFerpaEnabled(Collection<String> agentUuids);

	/**
	 * Find all SakaiPerson objects where ferpaEnabled == TRUE.
	 * 
	 * @return A List of SakaiPerson objects where ferpaEnabled == TRUE.
	 */
	List<SakaiPerson> findAllFerpaEnabled();
	
	
}
