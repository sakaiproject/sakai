/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.search.optimize.shared.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.search.indexer.impl.SearchBuilderItemSerializer;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.model.SearchBuilderItem;
import org.sakaiproject.search.optimize.api.OptimizeTransactionListener;
import org.sakaiproject.search.optimize.api.OptimizedFailedIndexTransactionException;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.util.FileUtils;

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

	private JournalSettings journalSettings;

	public void init()
	{

	}

	public void destroy()
	{

	}

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
			JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
			File workingSegment = jtransaction.getWorkingSegment();
			if (workingSegment != null)
			{
				FileUtils.deleteAll(workingSegment);
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to rollback ", ex);
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
		/**
		 * <pre>
		 *   Merge Method 1
		 *   The merge operation takes a list of segments and merges from the oldest one to the newest one
		 *   into a temporary segment. Sweeping up all deleted documents allong the way.
		 *   
		 *    SAK-12668, discovered that this was inefficient, since the oldest segment is the largest segment
		 *    It will be more efficient to start at the target and sweep backwards collecting the deleted 
		 *    elements as we go, that way only the final merge will be expensive and should scale a little 
		 *    better.
		 *    
		 *    The fix for SAK-12668 is to reverse the order of merge so we have to deal with a number of smaller
		 *    segments and finally a single large merge operation.
		 *    
		 *    We may consider not merging if the segments are too big. 
		 *    All we are trying to do is reduce the restart load, and not necessarilly the number of big 
		 *    segments in the journal.
		 *   
		 *    I have checked the local index optimize which performs the operaiton in this way, except it
		 *    does not need to perform the delete operations which have already been performed during the merge.
		 * </pre>
		 */
		IndexReader reader = null;
		IndexWriter indexWriter = null;
		try
		{
			
			JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;

			File targetSegment = jtransaction.getTargetSegment();
			String indexWorkingSpace = jtransaction.getWorkingSpace();
			long targetSavePoint = jtransaction.getTargetSavePoint();
			List<File> optimizableSegments = jtransaction.getMergeSegmentList();

			File workingSegment = new File(indexWorkingSpace, targetSavePoint + "."
					+ System.currentTimeMillis());
			jtransaction.setWorkingSegment(workingSegment);
			Directory workingDirectory = FSDirectory.getDirectory(workingSegment, true);
			/*
			 * For merge to work correctly we must create a target segment, and
			 * then merge into that target, perforing deletes first and then
			 * merging. The target is a clean new segment
			 */
			long mergeStart = System.currentTimeMillis();

			indexWriter = new IndexWriter(workingDirectory, jtransaction.getAnalyzer(),
					true);
			indexWriter.setUseCompoundFile(true);
			// indexWriter.setInfoStream(System.out);
			indexWriter.setMaxMergeDocs(journalSettings.getSharedMaxMergeDocs());
			indexWriter.setMaxBufferedDocs(journalSettings.getSharedMaxBufferedDocs());
			indexWriter.setMergeFactor(journalSettings.getSharedMaxMergeFactor());
			

			boolean reverseMerge = true;
			StringBuilder timings = new StringBuilder();
			if (reverseMerge )
			{
				Map<String, String> deletedReferences = new HashMap<String, String>();
				for (int i = optimizableSegments.size() - 1; i >= 0; i--)
				{
					File f = optimizableSegments.get(i);
					Directory d = FSDirectory.getDirectory(f, false);

					long start = System.currentTimeMillis();
					
					reader = IndexReader.open(d);
					// apply later deletes to this segment
					for (String toDelete : deletedReferences.values())
					{
						reader.deleteDocuments(new Term(
									SearchService.FIELD_REFERENCE, toDelete));
					}
					reader.close();

					// merge the next index into temporary space

					indexWriter.addIndexes(new Directory[] { d });


					// collect additional delete references to be applied to earlier segments
					List<SearchBuilderItem> deleteDocuments = searchBuilderItemSerializer
							.loadTransactionList(f);
					for (SearchBuilderItem sbi : deleteDocuments)
					{
						if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
						{
							deletedReferences.put(sbi.getName(),sbi.getName());
						}
					}
					
					// Finish
					searchBuilderItemSerializer.removeTransactionList(f);
					long end = System.currentTimeMillis();
					log.info("Merged SavePoint " + f + " in " + (end - start) + " ms "
							+ f.getPath());
					timings.append("\n\tMerged SavePoint ").append(f.getName()).append(" in ").append((end - start)).append(" ms ");
					

				}

			}
			else
			{

				for (File f : optimizableSegments)
				{
					Directory d = FSDirectory.getDirectory(f, false);

					long start = System.currentTimeMillis();

					reader = IndexReader.open(d);
					// collect additional delete references
					List<SearchBuilderItem> deleteDocuments = searchBuilderItemSerializer
							.loadTransactionList(f);
					for (SearchBuilderItem sbi : deleteDocuments)
					{
						if (SearchBuilderItem.ACTION_DELETE.equals(sbi.getSearchaction()))
						{
							reader.deleteDocuments(new Term(
									SearchService.FIELD_REFERENCE, sbi.getName()));
						}
					}
					reader.close();

					// merge the next index into temporary space

					indexWriter.addIndexes(new Directory[] { d });

					searchBuilderItemSerializer.removeTransactionList(f);
					long end = System.currentTimeMillis();
					log.info("Merged SavePoint " + f + " in " + (end - start) + " ms "
							+ f.getPath());
					timings.append("\n\tMerged SavePoint ").append(f.getName()).append(" in ").append((end - start)).append(" ms ");
				}
			}
			long start = System.currentTimeMillis();
			indexWriter.optimize();
			indexWriter.close();
			long end = System.currentTimeMillis();
			log.info("Optimized Working SavePoint in " + (end - start) + " ms ");
			timings.append("\n\tOptimized Working SavePoint in ").append((end - start)).append(" ms ");
			timings.append("\n\tTotal Shared Optimize Merge Time (no transfer) ").append((end - mergeStart)).append(" ms ");
			log.info("Shared Optimize Timings "+timings.toString()+"\n");
			/*
			 * // merge into the target segment. log.info("=================
			 * Merging into "+targetSegment); indexWriter = new
			 * IndexWriter(targetSegment, jtransaction.getAnalyzer(), false);
			 * indexWriter.setUseCompoundFile(true); //
			 * indexWriter.setInfoStream(System.out);
			 * indexWriter.setMaxMergeDocs(journalSettings.getSharedMaxMergeDocs());
			 * indexWriter.setMaxBufferedDocs(journalSettings.getSharedMaxBufferedDocs());
			 * indexWriter.setMergeFactor(journalSettings.getSharedMaxMergeFactor());
			 * Directory d = FSDirectory.getDirectory(workingSegment, false);
			 * indexWriter.addIndexes(new Directory[] { d }); start =
			 * System.currentTimeMillis(); indexWriter.optimize();
			 * indexWriter.close(); end = System.currentTimeMillis();
			 * log.info("Optimized Target SavePoint in " + (end - start) + " ms
			 * ");
			 */

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
				reader.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
			}
			try
			{
				indexWriter.close();
			}
			catch (Exception ex)
			{
				log.debug(ex);
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
		try
		{
			JournalOptimizationTransaction jtransaction = (JournalOptimizationTransaction) transaction;
			File workingSegment = jtransaction.getWorkingSegment();
			if (workingSegment != null)
			{
				FileUtils.deleteAll(workingSegment);
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to rollback ", ex);
		}

	}

	/**
	 * @return the journalSettings
	 */
	public JournalSettings getJournalSettings()
	{
		return journalSettings;
	}

	/**
	 * @param journalSettings
	 *        the journalSettings to set
	 */
	public void setJournalSettings(JournalSettings journalSettings)
	{
		this.journalSettings = journalSettings;
	}

}
