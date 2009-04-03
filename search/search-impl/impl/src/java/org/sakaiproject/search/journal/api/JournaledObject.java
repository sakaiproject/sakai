/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.journal.api;

/**
 * A journal object is an object that is managed by a jornal redo log
 * 
 * @author ieb
 */
public interface JournaledObject
{

	/**
	 * Get the current Journaled SavePoint
	 * 
	 * @return
	 */
	long getJournalSavePoint();

	/**
	 * get a lock on on the object for update
	 * 
	 * @return true is lock was granted, false if not
	 */
	boolean aquireUpdateLock();

	/**
	 * release the update lock
	 */
	void releaseUpdateLock();

	/**
	 * Aquires a lock to read the object
	 * 
	 * @return true if lock was granted, false if not
	 */
	boolean aquireReadLock();

	/**
	 * releases a lock to read object
	 */
	void releaseReadLock();

	/**
	 * get the last journal entry we tried to migrate to
	 * 
	 * @return
	 */
	long getLastJournalEntry();

	/**
	 * Set the last journal entry we tried to migrate to
	 * 
	 * @param nextJournalEntry
	 */
	void setLastJournalEntry(long nextJournalEntry);

	/**
	 * Set the current index entry for the journal
	 * 
	 * @param journalEntry
	 */
	void setJournalIndexEntry(long journalEntry);

	/**
	 * 
	 */
	void debugLock();

}
