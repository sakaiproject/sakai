/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.search.index.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.index.ClusterFilesystem;
import org.sakaiproject.search.index.SegmentInfo;

/**
 * Inpmelemtation of IndexStorage using a Cluster File system. This
 * implementation perfoms all index write operations in a new temporary segment.
 * On completion of the index operation it is merged with the current segment.
 * If the current segment is larger than the threshold, a new segment is
 * created. Managing the segments and how they relate to the cluster is
 * delegateed to the ClusterFilesystem
 * 
 * @author ieb
 */
public class ClusterFSIndexStorage extends BaseIndexStorage
{
	private static final Log log = LogFactory.getLog(ClusterFSIndexStorage.class);

	/**
	 * Maximum size of a segment on write
	 */
	private long segmentThreshold = 1024 * 1024 * 20; // Maximum Segment size

	// is 20M

	private ClusterFilesystem clusterFS = null;

	// maximum size of a segment during merge

	private long maxSegmentSize = 1024L * 1024L * 1500L; // just short of

	// 1.5G

	// maximum size of a segment considered for merge operations
	private long maxMegeSegmentSize = 1024L * 1024L * 1200L; // 1.2G

	public void init()
	{
	}

	public IndexReader getIndexReader() throws IOException
	{
		return getIndexReader(true);
	}

	private IndexReader getIndexReader(boolean withLock) throws IOException
	{
		if (withLock)
		{
			clusterFS.getLock();
		}
		List<SegmentInfo> segments = clusterFS.updateSegments();
		if (log.isDebugEnabled()) log.debug("Found " + segments.size() + " segments ");
		IndexReader[] readers = new IndexReader[segments.size()];
		int j = 0;
		for (Iterator<SegmentInfo> i = segments.iterator(); i.hasNext();)
		{
			SegmentInfo segment = i.next();
			try
			{
				if (false)
				{

					// this code will simulate a massive index failure, where
					// evey 5th segment is dammaged beyond repair.
					// only enable if you want to test the recovery mechanism
					if (j % 5 == 0)
					{
						File f = segment.getSegmentLocation();
						log.warn("Removing Segment for test " + f);
						File[] files = f.listFiles();
						for (int k = 0; k < files.length; k++)
						{
							files[k].delete();
						}
						f.delete();
					}
				}

				if (!segment.checkSegmentValidity(diagnostics, "getIndexReader "))
				{
					log.warn("Checksum Failed on  " + segment);
					segment.checkSegmentValidity(true, "getIndexReader Failed");
				}
				readers[j] = IndexReader.open(segment.getSegmentLocation());
			}
			catch (Exception ex)
			{
				try
				{
					if (readers[j] != null)
					{
						try
						{
							readers[j].close();
							readers[j] = null;
						}
						catch (Exception e)
						{
							log.debug(e);
						}
					}

					if (log.isDebugEnabled()) log.debug("Invalid segment  ", ex);
					log
							.warn(
									"Found corrupted segment ("
											+ segment.getName()
											+ ") in Local store, attempting to recover from DB.  Reason: "
											+ ex.getClass().getName() + ":"
											+ ex.getMessage(), ex);
					clusterFS.recoverSegment(segment);
					readers[j] = IndexReader.open(segment.getSegmentLocation());
					log
							.warn("Recovery complete, resuming normal operations having restored, ignore previous problems with this segment "
									+ segment.getName());
				}
				catch (Exception e)
				{
					if (readers[j] != null)
					{
						try
						{
							readers[j].close();
							readers[j] = null;
						}
						catch (Exception ex2)
						{
							log.debug(e);
						}
					}
					log
							.error(
									"---Problem recovering corrupted segment from the DB,\n"
											+ "--- it is probably that there has been a local hardware\n"
											+ "--- failure on this node or that the backup in the DB is missing\n"
											+ "--- or corrupt. To recover, remove the segment from the db, and rebuild the index \n"
											+ "--- eg delete from search_segments where name_ = '"
											+ segment.getName() + "'; \n", ex);

				}
			}
			j++;
		}
		List<IndexReader> l = new ArrayList<IndexReader>();
		for (int i = 0; i < readers.length; i++)
		{
			if (readers[i] != null)
			{
				l.add(readers[i]);
			}
		}
		if (l.size() != readers.length)
		{
			log
					.warn(" Opening index reader with a partial index set, this may result in a smallere search set than otherwise expected");
		}
		readers = l.toArray(new IndexReader[0]);
		if (readers.length > 0)
		{
			IndexReader indexReader = new MultiReader(readers);
			return indexReader;
		}
		throw new IOException("No Index available to open ");
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{

		if (log.isDebugEnabled())
			log.debug("+++++++++++++++++Start Index Writer Cycle   ");
		// to ensure that we dont dammage the index due to OutOfMemory, if it
		// should ever happen
		// we will open a temporary index, which will be merged on completion
		SegmentInfo currentSegment = null;
		IndexWriter indexWriter = null;
		if (false)
		{
			List<SegmentInfo> segments = clusterFS.updateSegments();
			if (log.isDebugEnabled())
				log.debug("Found " + segments.size() + " segments ");
			if (segments.size() > 0)
			{
				currentSegment = segments.get(segments.size() - 1);
				if (!currentSegment.isClusterSegment()
						|| currentSegment.getTotalSize() > segmentThreshold)
				{
					currentSegment = null;
				}

			}
			if (currentSegment == null)
			{
				currentSegment = clusterFS.newSegment();
				if (log.isDebugEnabled())
					log.debug("Created new segment " + currentSegment.getName());
				currentSegment.touchSegment();
				indexWriter = new IndexWriter(currentSegment.getSegmentLocation(),
						getAnalyzer(), true);
				indexWriter.setUseCompoundFile(true);
				// indexWriter.setInfoStream(System.out);
				indexWriter.setMaxMergeDocs(50);
				indexWriter.setMergeFactor(50);
			}
			else
			{
				currentSegment.touchSegment();
				indexWriter = new IndexWriter(currentSegment.getSegmentLocation(),
						getAnalyzer(), false);
				indexWriter.setUseCompoundFile(true);
				// indexWriter.setInfoStream(System.out);
				indexWriter.setMaxMergeDocs(50);
				indexWriter.setMergeFactor(50);
			}
			if (log.isDebugEnabled())
				log.debug("Using Current Index Writer "
						+ currentSegment.getSegmentLocation().getPath());
		}
		else
		{
			File tempIndex = clusterFS.getTemporarySegment(true);
			indexWriter = new IndexWriter(tempIndex, getAnalyzer(), true);
			indexWriter.setUseCompoundFile(true);
			// indexWriter.setInfoStream(System.out);
			indexWriter.setMaxMergeDocs(50);
			indexWriter.setMergeFactor(50);
			if (log.isDebugEnabled())
				log.debug("Using Temp Index Writer " + tempIndex.getPath());
		}
		return indexWriter;
	}

	protected IndexSearcher getIndexSearcher() throws IOException
	{

		IndexSearcher indexSearcher = null;
		try
		{
			long reloadStart = System.currentTimeMillis();
			log.debug("Open Search");
			indexSearcher = new IndexSearcher(getIndexReader(false));
			if (indexSearcher == null)
			{
				log.warn("No search Index exists at this time");

			}
			long reloadEnd = System.currentTimeMillis();
			if (log.isDebugEnabled())
				log.debug("Reload Complete " + indexSearcher.maxDoc() + " in "
						+ (reloadEnd - reloadStart));

		}
		catch (FileNotFoundException e)
		{
			try
			{
				indexSearcher.close();
			}
			catch (Exception ex)
			{
				log.debug(e);
			}
			indexSearcher = null;
			log.error("There has been a major poblem with the"
					+ " Search Index which has become corrupted ", e);
		}
		catch (IOException e)
		{
			try
			{
				indexSearcher.close();
			}
			catch (Exception ex)
			{
				log.debug(e);
			}
			indexSearcher = null;
			log.error("There has been a major poblem with the "
					+ "Search Index which has become corrupted", e);
		}
		return indexSearcher;
	}

	public boolean indexExists()
	{
		List<SegmentInfo> segments = clusterFS.updateSegments();
		return (segments.size() > 0);
	}

	public void doPreIndexUpdate() throws IOException
	{
		if (log.isDebugEnabled()) log.debug("Start Index Cycle");
		// dont enable locks
		FSDirectory.setDisableLocks(true);

	}

	public void doPostIndexUpdate() throws IOException
	{
	}

	private void mergeAndUpdate(boolean merge) throws IOException
	{
		if (merge)
		{
			FSDirectory.setDisableLocks(true);
			// get the tmp index
			File tmpSegment = clusterFS.getTemporarySegment(false);
			Directory[] tmpDirectory = new Directory[1];
			tmpDirectory[0] = FSDirectory.getDirectory(tmpSegment, false);

			// Need to fix checksums before merging.... is that really true,
			// 

			List<SegmentInfo> segments = clusterFS.updateSegments();

			if (log.isDebugEnabled())
				log.debug("Merge Phase 1: Starting on " + segments.size() + " segments ");

			// merge it with the current index
			SegmentInfo currentSegment = null;

			if (log.isDebugEnabled())
				log.debug("Found " + segments.size() + " segments ");
			if (segments.size() > 0)
			{
				currentSegment = segments.get(segments.size() - 1);
				if (currentSegment != null)
				{
					if (!currentSegment.isClusterSegment()
							|| (currentSegment.getTotalSize() > segmentThreshold)
							|| currentSegment.isDeleted())
					{
						if (diagnostics)
						{
							log
									.info("Current Segment not suitable, generating new segment "
											+ (currentSegment.isDeleted() ? "deleted,"
													: "")
											+ (!currentSegment.isClusterSegment() ? "non-cluster,"
													: "")
											+ ((currentSegment.getTotalSize() > segmentThreshold) ? "toobig,"
													: ""));
						}
						currentSegment = null;
					}
				}

			}
			if (currentSegment == null)
			{
				if (tmpDirectory[0].fileExists("segments.gen"))
				{
					currentSegment = clusterFS.saveTemporarySegment();
					/*
					 * We must add the new current segment to the list of
					 * segments so if it gets merged in the next step is is not
					 * left out
					 */
					segments.add(currentSegment);
					/*
					 * We should touch the segment to notify that it has been
					 * updated
					 */
					currentSegment.touchSegment();
				}
				else
				{
					log
							.warn("No Segment Created during indexing process, this should not happen, although it is possible tha the indexing operation did not find any files to index.");
				}
			}
			else
			{
				IndexWriter indexWriter = null;
				try
				{
					if (log.isDebugEnabled())
						log.debug("Using Existing Segment " + currentSegment.getName());
					currentSegment.touchSegment();
					indexWriter = new IndexWriter(FSDirectory.getDirectory(currentSegment
							.getSegmentLocation(), false), getAnalyzer(), false);
					indexWriter.setUseCompoundFile(true);
					// indexWriter.setInfoStream(System.out);
					indexWriter.setMaxMergeDocs(50);
					indexWriter.setMergeFactor(50);

					if (tmpDirectory[0].fileExists("segments.gen"))
					{
						if (log.isDebugEnabled())
							log.debug("Merging Temp segment " + tmpSegment.getPath()
									+ " with current segment "
									+ currentSegment.getSegmentLocation().getPath());
						indexWriter.addIndexes(tmpDirectory);
						indexWriter.optimize();
					}
					else
					{
						log.warn("No Merge performed, no tmp segment");
					}
				}
				finally
				{
					try
					{
						indexWriter.close();
						currentSegment.touchSegment();
					}
					catch (Exception ex)
					{
						// dotn care if this fails
						log.debug(ex);
					}
				}
			}

			/*
			 * segments in now a list of all segments including the current
			 * segment
			 */

			// create a size sorted list
			if (segments.size() > 10)
			{
				if (log.isDebugEnabled()) log.debug("Merge Phase 0 : Stating");
				// long[] segmentSize = new long[segments.size() - 1];
				// File[] segmentName = new File[segments.size() - 1];
				for (Iterator<SegmentInfo> i = segments.iterator(); i.hasNext();)
				{
					i.next().loadSize();
				}

				Collections.sort(segments, new Comparator<SegmentInfo>()
				{

					public int compare(SegmentInfo o1, SegmentInfo o2)
					{
						long l = o1.getSize() - o2.getSize();
						if (l == 0)
						{
							return 0;
						}
						else if (l < 0)
						{
							return -1;
						}
						else
						{
							return 1;
						}
					}

				});

				long sizeBlock = 0;
				int ninblock = 0;
				int mergegroupno = 1;
				int[] mergegroup = new int[segments.size()];
				int[] groupstomerge = new int[segments.size()];
				mergegroup[0] = mergegroupno;
				{
					int j = 0;
					for (int i = 0; i < mergegroup.length; i++)
					{
						if (segments.get(i).getSize() < maxMegeSegmentSize)
						{
							groupstomerge[i] = 0;
							if (ninblock == 0)
							{
								sizeBlock = segments.get(0).getSize();
								ninblock = 1;
								if (log.isDebugEnabled())
									log.debug("Order Size = " + sizeBlock);
							}

							if (segments.get(i).getSize() > sizeBlock / 10)
							{
								ninblock++;
								// count up blocks that have the same order of
								// size
							}
							else
							{
								// if there are more than 2 in the block force a
								// merge
								if (ninblock >= 2)
								{
									groupstomerge[j++] = mergegroupno;
								}

								// reset for the next order of magnitude down
								ninblock = 1;
								mergegroupno++;
								sizeBlock = segments.get(i).getSize();
							}
							mergegroup[i] = mergegroupno;
						}
					}
					// catch the merge all case
					if (ninblock >= 2)
					{
						groupstomerge[j++] = mergegroupno;
					}
					if (j > 0)
					{
						StringBuilder status = new StringBuilder();
						for (int i = 0; i < segments.size(); i++)
						{
							SegmentInfo si = segments.get(i);
							status.append("Segment ").append(i).append(" n").append(
									si.getName()).append(" s").append(si.getSize())
									.append(" g").append(mergegroup[i]).append("\n");
						}
						for (int i = 0; i < groupstomerge.length; i++)
						{
							status.append("Merge group ").append(i).append(" m").append(
									groupstomerge[i]).append("\n");
						}
						if (log.isDebugEnabled()) log.debug("Search Merge \n" + status);
					}

				}
				// groups to merge contains a list of group numbers that need to
				// be
				// merged.
				// mergegroup marks each segment with a group number.
				for (int i = 0; i < groupstomerge.length; i++)
				{
					if (groupstomerge[i] != 0)
					{
						StringBuilder status = new StringBuilder();
						status.append("Group ").append(i).append(" Merge ").append(
								groupstomerge[i]).append("\n");

						// merge the old segments into a new segment.

						SegmentInfo mergeSegment = clusterFS.newSegment();

						IndexWriter mergeIndexWriter = null;
						boolean mergeOk = false;
						try
						{
							mergeIndexWriter = new IndexWriter(FSDirectory.getDirectory(
									mergeSegment.getSegmentLocation(), false),
									getAnalyzer(), true);
							mergeIndexWriter.setUseCompoundFile(true);
							// indexWriter.setInfoStream(System.out);
							mergeIndexWriter.setMaxMergeDocs(50);
							mergeIndexWriter.setMergeFactor(50);
							List<Directory> indexes = new ArrayList<Directory>();
							long currentSize = 0L;
							for (int j = 0; j < mergegroup.length; j++)
							{
								// find if this segment is in the current merge
								// group
								SegmentInfo si = segments.get(j);
								if (mergegroup[j] == groupstomerge[i])
								{
									// if we merge this segment will the result
									// probably remain small enough
									if (si.isDeleted())
									{
										status
												.append(
														"   Skipped, Segment is already deleted  ")
												.append(" ").append(si.getName()).append(
														" || ").append(
														mergeSegment.getName()).append(
														"\n");
									}
									else if ((currentSize + si.getSize()) < maxSegmentSize)
									{
										currentSize += si.getSize();

										Directory d = FSDirectory.getDirectory(si
												.getSegmentLocation(), false);
										if (d.fileExists("segments.gen"))
										{
											status.append("   Merge ").append(
													si.getName()).append(" >> ").append(
													mergeSegment.getName()).append("\n");
											indexes.add(d);
										}
										else
										{
											status
													.append(
															"   Ignored segment as it does not exist ")
													.append(mergeSegment.getName())
													.append("\n");

										}
									}
									else
									{
										status.append("   Skipped, size >  ").append(
												maxSegmentSize).append(" ").append(
												si.getName()).append(" || ").append(
												mergeSegment.getName()).append("\n");
										// Dont merge this segment this time
										mergegroup[j] = -10;
									}

								}
							}
							// merge in the list of segments that we have
							// waiting to be merged
							if (diagnostics)
							{
								log.info("Merging \n" + status);
							}
							mergeIndexWriter.addIndexes((Directory[]) indexes
									.toArray(new Directory[indexes.size()]));
							mergeIndexWriter.optimize();
							if (diagnostics)
							{
								log.info("Merged Segment contians "
										+ mergeIndexWriter.docCount() + " documents ");
							}

							mergeIndexWriter.close();
							// mark the segment as create and ready of upload
							mergeSegment.setCreated();
							mergeSegment.touchSegment();

							if (log.isDebugEnabled())
								log.debug("Done " + groupstomerge[i]);
							mergeIndexWriter = null;
							// remove old segments
							mergeOk = true;
						}
						catch (Exception ex)
						{
							log.error("Failed to merge search segments "
									+ ex.getMessage());
							try
							{
								mergeIndexWriter.close();
							}
							catch (Exception ex2)
							{
								log.debug(ex2);
							}
							try
							{
								clusterFS.removeLocalSegment(mergeSegment);
							}
							catch (Exception ex2)
							{
								log
										.error("Failed to remove merge segment "
												+ mergeSegment.getName() + " "
												+ ex2.getMessage());
							}

						}
						finally
						{
							try
							{
								mergeIndexWriter.close();
							}
							catch (Exception ex)
							{
							}
						}
						if (mergeOk)
						{
							for (int j = 0; j < mergegroup.length; j++)
							{
								if (mergegroup[j] == groupstomerge[i])
								{
									clusterFS.removeLocalSegment(segments.get(j));
								}
							}
						}
					}
				}
			}
		}
		else
		{
			log.debug("Merge Not requested ");
		}
		clusterFS.removeTemporarySegment();

		clusterFS.saveSegments();
		if (log.isDebugEnabled())
			log.debug("+++++++++++++++++++++++++++++++++++++End Index Cycle");
	}

	public void setRecoverCorruptedIndex(boolean recover)
	{
	}

	/**
	 * @return Returns the clusterFS.
	 */
	public ClusterFilesystem getClusterFS()
	{
		return clusterFS;
	}

	/**
	 * @param clusterFS
	 *        The clusterFS to set.
	 */
	public void setClusterFS(ClusterFilesystem clusterFS)
	{
		this.clusterFS = clusterFS;
	}

	public long getLastUpdate()
	{
		return clusterFS.getLastUpdate();
	}

	public List getSegmentInfoList()
	{
		return clusterFS.getSegmentInfoList();
	}

	public void closeIndexReader(IndexReader indexReader) throws IOException
	{
		if (indexReader != null)
		{
			indexReader.close();
		}

		// only update required, no merge
		clusterFS.getLock();
		mergeAndUpdate(false);
		clusterFS.releaseLock();
		// if a lock was aquired, the lock should be released and the indx
		// synchronised

	}

	public void closeIndexWriter(IndexWriter indexWrite) throws IOException
	{
		if (indexWrite != null)
		{
			indexWrite.close();
		}
		clusterFS.getLock();
		mergeAndUpdate(true);
		clusterFS.releaseLock();
		// we should aquire a lock, merge in the index and sync
	}

	public boolean isMultipleIndexers()
	{
		return clusterFS.isMultipleIndexers();
	}

	public void closeIndexSearcher(IndexSearcher indexSearcher)
	{
		IndexReader indexReader = indexSearcher.getIndexReader();
		boolean closedAlready = false;
		try
		{
			if (indexReader != null)
			{
				indexReader.close();
				closedAlready = true;
			}
		}
		catch (Exception ex)
		{
			log.error("Failed to close Index Reader " + ex.getMessage());
		}
		try
		{
			indexSearcher.close();
		}
		catch (Exception ex)
		{
			if (closedAlready)
			{
				log.debug("Failed to close Index Searcher " + ex.getMessage());
			}
			else
			{
				log.error("Failed to close Index Searcher " + ex.getMessage());
			}

		}
	}

	/**
	 * @return the maxMegeSegmentSize
	 */
	public long getMaxMegeSegmentSize()
	{
		return maxMegeSegmentSize;
	}

	/**
	 * @param maxMegeSegmentSize
	 *        the maxMegeSegmentSize to set
	 */
	public void setMaxMegeSegmentSize(long maxMegeSegmentSize)
	{
		log.info("Max Segment Merge Size set to " + maxMegeSegmentSize);
		this.maxMegeSegmentSize = maxMegeSegmentSize;
	}

	/**
	 * @return the maxSegmentSize
	 */
	public long getMaxSegmentSize()
	{
		return maxSegmentSize;
	}

	/**
	 * @param maxSegmentSize
	 *        the maxSegmentSize to set
	 */
	public void setMaxSegmentSize(long maxSegmentSize)
	{
		log.info("Max Segment Size set to " + maxSegmentSize);
		this.maxSegmentSize = maxSegmentSize;
	}

	/**
	 * @return the segmentThreshold
	 */
	public long getSegmentThreshold()
	{
		return segmentThreshold;
	}

	/**
	 * @param segmentThreshold
	 *        the segmentThreshold to set
	 */
	public void setSegmentThreshold(long segmentThreshold)
	{
		log.info("New Segment Size threshold set to " + segmentThreshold);
		this.segmentThreshold = segmentThreshold;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.index.IndexStorage#centralIndexExists()
	 */
	public boolean centralIndexExists()
	{
		return clusterFS.centralIndexExists();
	}
}
