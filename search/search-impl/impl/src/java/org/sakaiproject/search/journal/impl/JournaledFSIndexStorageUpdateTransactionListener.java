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

package org.sakaiproject.search.journal.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.indexer.impl.SearchBuilderItemSerializer;
import org.sakaiproject.search.journal.api.IndexMergeTransaction;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournalStorage;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.journal.api.JournaledObject;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * Listens for Transaction changes in the 2PC associated with an index update
 * 
 * @author ieb TODO Unit test
 */
public class JournaledFSIndexStorageUpdateTransactionListener implements
		MergeTransactionListener
{

	private JournaledIndex journaledIndex;

	private JournalManager journalManager;

	private SearchBuilderItemSerializer searchBuilderItemSerializer = new SearchBuilderItemSerializer();

	private JournalStorage journalStorage;

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#open(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void open(IndexTransaction transaction) throws IndexTransactionException
	{
		long lastJournalEntry = journaledIndex.getLastJournalEntry();
		long thisJournalEntry = journaledIndex.getJournalVersion();
		long nextJournalEntry = journalManager.getNextVersion(thisJournalEntry);
		if (nextJournalEntry == lastJournalEntry)
		{
			throw new JournalErrorException("Journal is stalled at ID "
					+ lastJournalEntry);
		}
		journaledIndex.setLastJournalEntry(nextJournalEntry);
		((IndexMergeTransaction) transaction).setJournalEntry(nextJournalEntry);
		transaction.put(JournaledObject.class.getName() + ".thisJournalEntry",
				thisJournalEntry);
	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#prepare(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void prepare(IndexTransaction transaction) throws IndexTransactionException
	{
		long journalEntry = ((IndexMergeTransaction) transaction).getJournalEntry();
		if (journalEntry == -1)
		{
			throw new JournalErrorException("No target journal entry ");
		}

		try
		{
			// update should perform the delete operations and add the segments
			// into the current reader.

			String workingSpace = journaledIndex.getWorkingSpace();
			journalStorage.retrieveVersion(journalEntry, workingSpace);
			File f = new File(workingSpace, String.valueOf(journalEntry));
			File segments = new File(f, "segments");
			if (segments.exists())
			{
				journaledIndex.addSegment(f);
				List<SearchBuilderItem> deleteDocuments = searchBuilderItemSerializer
						.loadTransactionList(f);
				if (deleteDocuments.size() > 0)
				{
					IndexReader deleteIndexReader = journaledIndex
							.getDeletionIndexReader();
					transaction.put(
							JournaledFSIndexStorageUpdateTransactionListener.class
									.getName()
									+ ".deleteIndexReader", deleteIndexReader);

					for (SearchBuilderItem sbi : deleteDocuments)
					{
						if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
						{
							deleteIndexReader.deleteDocuments(new Term(
									SearchService.FIELD_REFERENCE, sbi.getName()));
						}
					}
				}
			}
		}
		catch (IOException ioex)
		{
			throw new IndexTransactionException("Failed to delete documents ", ioex);
		}

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#rollback(org.sakaiproject.search.indexer.api.IndexUpdateTransaction)
	 */
	public void rollback(IndexTransaction transaction) throws IndexTransactionException
	{
		IndexReader deleteIndexReader = (IndexReader) transaction
				.get(JournaledFSIndexStorageUpdateTransactionListener.class.getName()
						+ ".deleteIndexReader");
		transaction.clear(JournaledFSIndexStorageUpdateTransactionListener.class
				.getName()
				+ ".deleteIndexReader");

		try ///TODO: make this work correctly, roll back the index
		{
			if (deleteIndexReader != null)
			{
				deleteIndexReader.close();
			}
		}
		catch (IOException e)
		{
			throw new IndexTransactionException("Failed to close index with deletions");
		}
		// how do we perform a roll back ?
		// undo the delete operations and remove the index from the reader
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#commit(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void commit(IndexTransaction transaction) throws IndexTransactionException
	{
		IndexReader deleteIndexReader = (IndexReader) transaction
				.get(JournaledFSIndexStorageUpdateTransactionListener.class.getName()
						+ ".deleteIndexReader");
		long journalEntry = ((IndexMergeTransaction) transaction).getJournalEntry();
		if (journalEntry == -1)
		{
			throw new JournalErrorException("No target journal entry ");
		}

		try
		{

			if (deleteIndexReader != null)
			{
				deleteIndexReader.close();
			}
			journaledIndex.setJournalIndexEntry(journalEntry);
		}
		catch (IOException e)
		{
			throw new IndexTransactionException("Failed to close index with deletions");
		}
		transaction.clear(JournaledFSIndexStorageUpdateTransactionListener.class
				.getName()
				+ ".deleteIndexReader");

	}

	/**
	 * @return the journaledIndex
	 */
	public JournaledIndex getJournaledIndex()
	{
		return journaledIndex;
	}

	/**
	 * @param journaledIndex
	 *        the journaledIndex to set
	 */
	public void setJournaledIndex(JournaledIndex journaledIndex)
	{
		this.journaledIndex = journaledIndex;
	}

	/**
	 * @return the journalManager
	 */
	public JournalManager getJournalManager()
	{
		return journalManager;
	}

	/**
	 * @param journalManager
	 *        the journalManager to set
	 */
	public void setJournalManager(JournalManager journalManager)
	{
		this.journalManager = journalManager;
	}

	/**
	 * @return the journalStorage
	 */
	public JournalStorage getJournalStorage()
	{
		return journalStorage;
	}

	/**
	 * @param journalStorage
	 *        the journalStorage to set
	 */
	public void setJournalStorage(JournalStorage journalStorage)
	{
		this.journalStorage = journalStorage;
	}

}
