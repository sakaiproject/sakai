/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.search.indexer.api;

import org.sakaiproject.search.cluster.impl.IndexJournalException;

/**
 * This listener is notified of changes in the transaction state.
 * @author ieb
 *
 */
public interface TransactionListener
{
	/**
	 * Prepare to commit the transaction
	 * @param transaction
	 * @throws IndexJournalException 
	 */
	void prepare(IndexUpdateTransaction transaction) throws IndexJournalException;

	/**
	 * @param transaction
	 * @throws IndexTransactionException 
	 */
	void commit(IndexUpdateTransaction transaction) throws IndexTransactionException;

	/**
	 * @param transaction
	 */
	void rollback(IndexUpdateTransaction transaction) throws IndexTransactionException;

	/**
	 * @param transaction
	 */
	void open(IndexUpdateTransaction transaction) throws IndexTransactionException;


}
