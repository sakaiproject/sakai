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

package org.sakaiproject.user.api;

import java.util.Locale;

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;

/**
 * <p>
 * The PreferencesService keeps sets of preferences for each user (id)
 * </p>
 */
public interface PreferencesService extends EntityProducer
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:preferences";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/prefs";

	/** Securiy / Event for adding a preferences. */
	static final String SECURE_ADD_PREFS = "prefs.add";

	/** Securiy / Event for updating a preferences. */
	static final String SECURE_EDIT_PREFS = "prefs.upd";

	/** Securiy / Event for removing a preferences. */
	static final String SECURE_REMOVE_PREFS = "prefs.del";

	/** Prefs key under which stuff like the site tab order and hiding is stored. */
	static final String SITENAV_PREFS_KEY = "sakai:portal:sitenav";
	
	/** Prefs key under which stuff like the editor preferences stored. */
	static final String EDITOR_PREFS_KEY = "sakai:portal:editor";

	/** Prefs key under which stuff like the editor typepreferences stored. */
	static final String EDITOR_PREFS_TYPE = "editor:type";
	

	/**
	 * Access a set of preferences associated with this id.
	 * 
	 * @param id
	 *        The preferences id.
	 * @return The Preferences object.
	 */
	Preferences getPreferences(String id);

	/**
	 * Check to see if the current user can add or modify permissions with this id.
	 * 
	 * @param id
	 *        The preferences id.
	 * @return true if the user is allowed to update or add these preferences, false if not.
	 */
	boolean allowUpdate(String id);

	/**
	 * Add a new set of preferences with this id. Must commit(), remove() or cancel() when done.
	 * 
	 * @param id
	 *        The preferences id.
	 * @return A PreferencesEdit object for editing, possibly new.
	 * @exception PermissionException
	 *            if the current user does not have permission add preferences for this id.
	 * @exception IdUsedException
	 *            if these preferences already exist.
	 */
	PreferencesEdit add(String id) throws PermissionException, IdUsedException;

	/**
	 * Get a locked Preferences object for editing. Must commit(), cancel() or remove() when done.
	 * 
	 * @param id
	 *        The preferences id.
	 * @return A PreferencesEdit object for editing, possibly new.
	 * @exception PermissionException
	 *            if the current user does not have permission to edit these preferences.
	 * @exception InUseException
	 *            if the preferences object is locked by someone else.
	 * @exception IdUnusedException
	 *            if there is not preferences object with this id.
	 */
	PreferencesEdit edit(String id) throws PermissionException, InUseException, IdUnusedException;

	/**
	 * Commit the changes made to a PreferencesEdit object, and release the lock. The PreferencesEdit is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The PreferencesEdit object to commit.
	 */
	void commit(PreferencesEdit edit);

	/**
	 * Cancel the changes made to a PreferencesEdit object, and release the lock. The PreferencesEdit is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The PreferencesEdit object to commit.
	 */
	void cancel(PreferencesEdit edit);

	/**
	 * Remove this PreferencesEdit - it must be locked from edit(). The PreferencesEdit is disabled, and not to be used after this call.
	 * 
	 * @param edit
	 *        The PreferencesEdit object to remove.
	 */
	void remove(PreferencesEdit edit);
	
	
	/**
	 *  Get user's preferred locale (or null if not set)
	 * @param userId
	 * @return
	 */
	public Locale getLocale( String userId );
}
