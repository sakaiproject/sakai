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

import org.sakaiproject.search.indexer.api.IndexJournalException;
import org.sakaiproject.search.indexer.api.LockTimeoutException;
import org.sakaiproject.search.transaction.api.IndexTransaction;

/**
 * The journal manage manages the state of the journal for the current node
 * 
 * @author ieb
 */
public interface JournalManager
{

	/**
	 * @param savePoint
	 * @return
	 * @throws JournalErrorException
	 *         if there was an error getting the next savePoint
	 */
	long getNextSavePoint(long savePoint) throws JournalErrorException;

	/**
	 * @param transactionId
	 * @return
	 * @throws IndexJournalException
	 */
	JournalManagerState prepareSave(long transactionId) throws IndexJournalException;

	/**
	 * @param jms
	 * @throws IndexJournalException
	 */
	void commitSave(JournalManagerState jms) throws IndexJournalException;

	/**
	 * @param jms
	 */
	void rollbackSave(JournalManagerState jms);

	/**
	 * Perfomes the open, giving the JournalManager implementation a chance to
	 * veto the transaction.
	 * 
	 * @param transaction
	 * @throws IndexJournalException
	 */
	void doOpenTransaction(IndexTransaction transaction) throws IndexJournalException;

}
