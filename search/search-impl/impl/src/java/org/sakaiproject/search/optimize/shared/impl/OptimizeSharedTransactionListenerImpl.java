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

package org.sakaiproject.search.optimize.shared.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.indexer.impl.SearchBuilderItemSerializer;
import org.sakaiproject.search.journal.api.JournaledIndex;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.optimize.api.OptimizeTransactionListener;
import org.sakaiproject.search.optimize.api.OptimizedFailedIndexTransactionException;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * An OptimizationTransactionListener that optimizes the index. It first
 * collects the segments that could be optimized. If ther are more than the
 * mergeSize then it will perform the merge into a temporary segment and if that
 * is successfull that index will be merged in the commit phase into the
 * permanent index
 * 
 * @author ieb
 */
public class OptimizeSharedTransactionListenerImpl implements OptimizeTransactionListener
{

	private static final Log log = LogFactory
			.getLog(OptimizeSharedTransactionListenerImpl.class);

	private SearchBuilderItemSerializer searchBuilderItemSerializer = new SearchBuilderItemSerializer();

	private JournaledIndex journaledIndex;

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#close(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void close(IndexTransaction transaction) throws IndexTransactionException
	{
		// nothing special to do, the transaction will close itself
	}

	/**
	 * commit closes the temporary segment, and merges it into the permanent
	 * segment. Both the temporary and permanent were opened in the prepare
	 * phase
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#commit(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void commit(IndexTransaction transaction) throws IndexTransactionException
	{
		try
		{
			journaledIndex.loadIndexReader();
		}
		catch (IOException e)
		{
			log.error("Failed to load index ",e);
		}
	}

	/**
	 * Open the index, but throw a NoOptimizationReqiredException if there are
	 * not enough segments to perfom a merge on
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#open(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void open(IndexTransaction transaction) throws IndexTransactionException
	{
	}

	/**
	 * Perform the merge operation on the segments into the target shared
	 * segment
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#prepare(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void prepare(IndexTransaction transaction) throws IndexTransactionException
	{
		MultiReader mr = null;
		IndexWriter indexWriter = null;
		try
		{
			JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
			File targetSegment = jtransaction.getTargetSegment();
			FSDirectory directory = FSDirectory.getDirectory(targetSegment, false);
			List<File> optimizableSegments = jtransaction.getMergeSegmentList();
			FSDirectory[] directories = new FSDirectory[optimizableSegments.size()];
			int i = 0;
			for (File f : optimizableSegments)
			{
				directories[i++] = FSDirectory.getDirectory(f, false);
			}
			// process the reads of all the elments, and then merge.
			IndexReader[] ir = new IndexReader[optimizableSegments.size() + 1];
			i = 0;
			for (File f : optimizableSegments)
			{
				ir[i] = IndexReader.open(directories[i]);
				i++;

			}
			ir[optimizableSegments.size()] = IndexReader.open(directory);

			mr = new MultiReader(ir);

			{
				List<SearchBuilderItem> deleteDocuments = searchBuilderItemSerializer
						.loadTransactionList(targetSegment);

				for (SearchBuilderItem sbi : deleteDocuments)
				{
					if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
					{
						mr.deleteDocuments(new Term(SearchService.FIELD_REFERENCE, sbi
								.getName()));
					}
				}
				searchBuilderItemSerializer.removeTransactionList(targetSegment);
			}
			
			// perform the deletes
			for (File f : optimizableSegments)
			{
				List<SearchBuilderItem> deleteDocuments = searchBuilderItemSerializer
						.loadTransactionList(f);

				for (SearchBuilderItem sbi : deleteDocuments)
				{
					if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
					{
						mr.deleteDocuments(new Term(SearchService.FIELD_REFERENCE, sbi
								.getName()));
					}
				}
				searchBuilderItemSerializer.removeTransactionList(f);
			}
			mr.close();

			// open the permanent writer to ensure it can be opened
			indexWriter = new IndexWriter(directory, jtransaction.getAnalyzer(), false);
			indexWriter.setUseCompoundFile(true);
			// indexWriter.setInfoStream(System.out);
			indexWriter.setMaxMergeDocs(50);
			indexWriter.setMergeFactor(50);

			indexWriter.addIndexes(directories);
			indexWriter.optimize();
			indexWriter.close();
			
			

		}
		catch (IOException e)
		{
			throw new OptimizedFailedIndexTransactionException(
					"Failed to Optimize indexes ", e);
		}
		finally
		{
			try
			{
				mr.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				indexWriter.close();
			}
			catch (Exception ex)
			{
			}
		}

	}

	/**
	 * Roll back the optimize operation
	 * 
	 * @see org.sakaiproject.search.transaction.api.TransactionListener#rollback(org.sakaiproject.search.transaction.api.IndexTransaction)
	 */
	public void rollback(IndexTransaction transaction) throws IndexTransactionException
	{
		// roll should be handled in the transaction, nothing special to do
		// here.

	}

	/**
	 * @return the journaledIndex
	 */
	public JournaledIndex getJournaledIndex()
	{
		return journaledIndex;
	}

	/**
	 * @param journaledIndex the journaledIndex to set
	 */
	public void setJournaledIndex(JournaledIndex journaledIndex)
	{
		this.journaledIndex = journaledIndex;
	}

}
