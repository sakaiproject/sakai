/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.alias.api;

import java.util.List;

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;

/**
 * <p>
 * AliasService provides a single unque namespace where Sakai entities can have alternate case-insenstive names.
 * </p>
 */
public interface AliasService extends EntityProducer
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:alias";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/alias";

	/** Name for the event of adding an alias. */
	static final String SECURE_ADD_ALIAS = "alias.add";

	/** Name for the event of updating an alias. */
	static final String SECURE_UPDATE_ALIAS = "alias.upd";

	/** Name for the event of removing an alias. */
	static final String SECURE_REMOVE_ALIAS = "alias.del";

	/**
	 * Check if the current user has permission to set this alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @param target
	 *        The resource reference string alias target.
	 * @return true if the current user has permission to set this alias, false if not.
	 */
	boolean allowSetAlias(String alias, String target);

	/**
	 * Allocate an alias for a resource
	 * 
	 * @param alias
	 *        The alias.
	 * @param target
	 *        The resource reference string alias target.
	 * @throws IdUsedException
	 *         if the alias is already used.
	 * @throws IdInvalidException
	 *         if the alias id is invalid.
	 * @throws PermissionException
	 *         if the current user does not have permission to set this alias.
	 */
	void setAlias(String alias, String target) throws IdUsedException, IdInvalidException, PermissionException;

	/**
	 * Check to see if the current user can remove this alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @return true if the current user can remove this alias, false if not.
	 */
	boolean allowRemoveAlias(String alias);

	/**
	 * Remove an alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @exception IdUnusedException
	 *            if not found.
	 * @exception PermissionException
	 *            if the current user does not have permission to remove this alias.
	 * @exception InUseException
	 *            if the Alias object is locked by someone else.
	 */
	void removeAlias(String alias) throws IdUnusedException, PermissionException, InUseException;

	/**
	 * Check to see if the current user can remove these aliasese for this target resource reference.
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @return true if the current user can remove these aliasese for this target resource reference, false if not.
	 */
	boolean allowRemoveTargetAliases(String target);

	/**
	 * Remove all aliases for this target resource reference, if any.
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @throws PermissionException
	 *         if the current user does not have permission to remove these aliases.
	 */
	void removeTargetAliases(String target) throws PermissionException;

	/**
	 * Find the target resource reference string associated with this alias.
	 * 
	 * @param alias
	 *        The alias.
	 * @return The target resource reference string associated with this alias.
	 * @throws IdUnusedException
	 *         if the alias is not defined.
	 */
	String getTarget(String alias) throws IdUnusedException;

	/**
	 * Find all the aliases defined for this target.
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @return A list (Alias) of all the aliases defined for this target.
	 */
	List<Alias> getAliases(String target);

	/**
	 * Find all the aliases defined for this target, within the record range given (sorted by id).
	 * 
	 * @param target
	 *        The target resource reference string.
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (Alias) of all the aliases defined for this target, within the record range given (sorted by id).
	 */
	List<Alias> getAliases(String target, int first, int last);

	/**
	 * Find all the aliases within the record range given (sorted by id).
	 * 
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (Alias) of all the aliases within the record range given (sorted by id).
	 */
	List<Alias> getAliases(int first, int last);

	/**
	 * Count all the aliases.
	 * 
	 * @return The count of all aliases.
	 */
	int countAliases();

	/**
	 * Search all the aliases that match this criteria in id or target, returning a subset of records within the record range given (sorted by id).
	 * 
	 * @param criteria
	 *        The search criteria.
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (Alias) of all the aliases matching the criteria, within the record range given (sorted by id).
	 */
	List<Alias> searchAliases(String criteria, int first, int last);

	/**
	 * Count all the aliases that match this criteria in id or target.
	 * 
	 * @return The count of all aliases matching the criteria.
	 */
	int countSearchAliases(String criteria);

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The alias id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	String aliasReference(String id);

	/**
	 * Check to see if the current user can add an alias.
	 * 
	 * @return true if the current user can add an alias, false if not.
	 */
	boolean allowAdd();

	/**
	 * Add a new alias. Must commit() to make official, or cancel() when done!
	 * 
	 * @param id
	 *        The alias id.
	 * @return A locked AliasEdit object (reserving the id).
	 * @exception IdInvalidException
	 *            if the alias id is invalid.
	 * @exception IdUsedException
	 *            if the alias id is already used.
	 * @exception PermissionException
	 *            if the current user does not have permission to add an alias.
	 */
	AliasEdit add(String id) throws IdInvalidException, IdUsedException, PermissionException;

	/**
	 * Check to see if the current user can edit this alias.
	 * 
	 * @param id
	 *        The alias id string.
	 * @return true if the current user can edit this alias, false if not.
	 */
	boolean allowEdit(String id);

	/**
	 * Get a locked alias object for editing. Must commit() to make official, or cancel() (or remove()) when done!
	 * 
	 * @param id
	 *        The alias id string.
	 * @return An AliasEdit object for editing.
	 * @exception IdUnusedException
	 *            if not found.
	 * @exception PermissionException
	 *            if the current user does not have permission to mess with this alias.
	 * @exception InUseException
	 *            if the Alias object is locked by someone else.
	 */
	AliasEdit edit(String id) throws IdUnusedException, PermissionException, InUseException;

	/**
	 * Commit the changes made to a AliasEdit object, and release the lock. The AliasEdit is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The AliasEdit object to commit.
	 */
	void commit(AliasEdit edit);

	/**
	 * Cancel the changes made to a AliasEdit object, and release the lock. The AliasEdit is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The AliasEdit object to commit.
	 */
	void cancel(AliasEdit edit);

	/**
	 * Remove this alias information - it must be a user with a lock from edit(). The AliasEdit is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The locked AliasEdit object to remove.
	 * @exception PermissionException
	 *            if the current user does not have permission to remove this alias.
	 */
	void remove(AliasEdit edit) throws PermissionException;
}
