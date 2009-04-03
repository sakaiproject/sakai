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

package org.sakaiproject.search.journal.impl;

import java.util.Map;

import org.sakaiproject.search.journal.api.IndexMergeTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.impl.IndexItemsTransactionImpl;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;

/**
 * A merge transaction that control the merging of the journal redo log with the
 * local search index
 * 
 * @author ieb TODO Unit test
 */
public class IndexMergeTransactionImpl extends IndexItemsTransactionImpl implements
		IndexMergeTransaction
{
	private long journalEntry;

	/**
	 * 
	 */
	public IndexMergeTransactionImpl(TransactionManagerImpl manager, Map<String, Object> m)
			throws IndexTransactionException
	{
		super(manager, m);
	}

	/**
	 * @see org.sakaiproject.search.journal.api.IndexMergeTransaction#getJournalEntry()
	 */
	public long getJournalEntry()
	{
		return journalEntry;
	}

	/**
	 * @see org.sakaiproject.search.journal.api.IndexMergeTransaction#setJournalEntry(long)
	 */
	public void setJournalEntry(long journalEntry)
	{
		this.journalEntry = journalEntry;
	}

}
