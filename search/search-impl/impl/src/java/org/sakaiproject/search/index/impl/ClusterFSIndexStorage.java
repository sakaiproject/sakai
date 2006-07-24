/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.search.index.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.index.ClusterFilesystem;
import org.sakaiproject.search.index.IndexStorage;

/**
 * Inpmelemtation of IndexStorage using a Cluster File system.
 * This implementation perfoms all index write operations in a new
 * temporary segment. On completion of the index operation it is
 * merged with the current segment. If the current segment is larger
 * than the threshold, a new segment is created. Managing the segments
 * and how they relate to the cluster is delegateed to the ClusterFilesystem
 * @author ieb
 */
public class ClusterFSIndexStorage implements IndexStorage
{
	private static final Log log = LogFactory
			.getLog(ClusterFSIndexStorage.class);

	/**
	 * Location of the index store on local disk, passed to the underlying index store
	 */
	private String searchIndexDirectory = null;

	/**
	 * The token analyzer
	 */
	private AnalyzerFactory analyzerFactory = null;
	

	/**
	 * Maximum size of a segment
	 */
	private long segmentThreshold = 1024 * 1024 * 2; // Maximum Segment size

	// is 2M

	private ClusterFilesystem clusterFS = null;
	
	public void init() {
		clusterFS.setLocation(searchIndexDirectory);
	}

	public IndexReader getIndexReader() throws IOException
	{
		List segments = clusterFS.updateSegments();
		log.debug("Found " + segments.size() + " segments ");
		IndexReader[] readers = new IndexReader[segments.size()];
		int j = 0;
		for (Iterator i = segments.iterator(); i.hasNext();)
		{
			String segment = (String) i.next();
			String segmentName = clusterFS.getSegmentName(segment);
			try
			{
				if (false)
				{

					// this code will simulate a massive index failure, where
					// evey 5th segment is dammaged beyond repair.
					// only enable if you want to test the recovery mechanism
					if (j % 5 == 0)
					{
						File f = new File(segment);
						log.warn("Removing Segment for test " + f);
						File[] files = f.listFiles();
						for (int k = 0; k < files.length; k++)
						{
							files[k].delete();
						}
						f.delete();
					}
				}

				
				if ( !clusterFS.checkSegmentValidity(segmentName) ) {
					throw new Exception("checksum failed");
				}
				readers[j] = IndexReader.open(segment);
			}
			catch (Exception ex)
			{
				try
				{

					log.debug("Invalid segment  ",ex);
					log
							.warn("Found corrupted segment ("
									+ segmentName
									+ ") in Local store, attempting to recover from DB");
					clusterFS.recoverSegment(segmentName);
					readers[j] = IndexReader.open(segment);
					log
							.warn("Recovery complete, resuming normal operations having restored "
									+ segmentName);
				}
				catch (Exception e)
				{
					log
							.error("---Failed to recover corrupted segment from the DB,\n"
									+ "--- it is probably that there has been a local hardware\n"
									+ "--- failure on this node or that the backup in the DB is missing\n"
									+ "--- or corrupt. To recover, remove the segment from the db, and rebuild the index \n"
									+ "--- eg delete from search_segments where name_ = '"
									+ segmentName + "'; \n");

				}
			}
			j++;
		}
		IndexReader indexReader = new MultiReader(readers);
		return indexReader;
	}

	public IndexWriter getIndexWriter(boolean create) throws IOException
	{

		// to ensure that we dont dammage the index due to OutOfMemory, if it
		// should ever happen
		// we will open a temporary index, which will be merged on completion
		File currentSegment = null;
		IndexWriter indexWriter = null;
		if (false)
		{
			List segments = clusterFS.updateSegments();
			log.debug("Found " + segments.size() + " segments ");
			if (segments.size() > 0)
			{
				currentSegment = new File((String) segments
						.get(segments.size() - 1));
				if (!currentSegment.exists()
						|| clusterFS.getTotalSize(currentSegment) > segmentThreshold)
				{
					currentSegment = null;
				}

			}
			if (currentSegment == null)
			{
				currentSegment = clusterFS.newSegment();
				log.debug("Created new segment " + currentSegment.getName());
				indexWriter = new IndexWriter(currentSegment, getAnalyzer(),
						true);
				indexWriter.setUseCompoundFile(true);
				//indexWriter.setInfoStream(System.out);
				indexWriter.setMaxMergeDocs(50);
				indexWriter.setMergeFactor(50);
			}
			else
			{
				clusterFS.touchSegment(currentSegment);
				indexWriter = new IndexWriter(currentSegment, getAnalyzer(),
						false);
				indexWriter.setUseCompoundFile(true);
				//indexWriter.setInfoStream(System.out);
				indexWriter.setMaxMergeDocs(50);
				indexWriter.setMergeFactor(50);
			}
		}
		else
		{
			File tempIndex = clusterFS.getTemporarySegment(true);
			indexWriter = new IndexWriter(tempIndex, getAnalyzer(), true);
			indexWriter.setUseCompoundFile(true);
			//indexWriter.setInfoStream(System.out);
			indexWriter.setMaxMergeDocs(50);
			indexWriter.setMergeFactor(50);
		}
		return indexWriter;
	}

	public IndexSearcher getIndexSearcher() throws IOException
	{

		IndexSearcher indexSearcher = null;
		try
		{
			long reloadStart = System.currentTimeMillis();
			indexSearcher = new IndexSearcher(getIndexReader());
			if (indexSearcher == null)
			{
				log.warn("No search Index exists at this time");

			}
			long reloadEnd = System.currentTimeMillis();
			log.debug("Reload Complete " + indexSearcher.maxDoc() + " in "
					+ (reloadEnd - reloadStart));

		}
		catch (FileNotFoundException e)
		{
			log.error("There has been a major poblem with the"
					+ " Search Index which has become corrupted ", e);
		}
		catch (IOException e)
		{
			log.error("There has been a major poblem with the "
					+ "Search Index which has become corrupted", e);
		}
		return indexSearcher;
	}

	public boolean indexExists()
	{
		List segments = clusterFS.updateSegments();
		return (segments.size() > 0);
	}

	public Analyzer getAnalyzer()
	{
		return analyzerFactory.newAnalyzer();
	}

	public void setLocation(String location)
	{
		searchIndexDirectory = location;
		if ( clusterFS != null ) {
			clusterFS.setLocation(location);
		}

	}

	public void doPreIndexUpdate() throws IOException
	{
		log.debug("Start Index Cycle");
		// dont enable locks
		FSDirectory.setDisableLocks(true);

	}

	public void doPostIndexUpdate() throws IOException
	{
		FSDirectory.setDisableLocks(true);
		// get the tmp index
		File tmpSegment = clusterFS.getTemporarySegment(false);
		Directory[] tmpDirectory = new Directory[1];
		tmpDirectory[0] = FSDirectory.getDirectory(tmpSegment, false);

		List segments = clusterFS.updateSegments();

		// create a size sorted list
		long[] segmentSize = new long[segments.size()-1];
		File[] segmentName = new File[segments.size()-1];
		if ( segmentSize.length > 10 ) {
			for (int i = 0; i < segments.size()-1; i++ ) {
				segmentName[i] = new File((String)segments.get(i));
				segmentSize[i] = clusterFS.getTotalSize(segmentName[i]);
			}
			boolean moved = true;
			while (moved)
			{
				moved = false;
				for (int i = 1; i < segmentSize.length; i++)
				{
					if (segmentSize[i] > segmentSize[i - 1])
					{
						long size = segmentSize[i];
						File name = segmentName[i];
						segmentSize[i] = segmentSize[i - 1];
						segmentName[i] = segmentName[i - 1];
						segmentSize[i - 1] = size;
						segmentName[i - 1] = name;
						moved = true;
					}
				}
			}
			
			long sizeBlock = 0;
			int ninblock = 0;
			int mergegroupno = 1;
			int[] mergegroup = new int[segmentSize.length];
			int[] groupstomerge = new int[segmentSize.length];
			mergegroup[0] = mergegroupno;
			{
				int j = 0;
				for ( int i = 0; i < segmentSize.length; i++ ) {
					groupstomerge[i] = 0;
					if ( ninblock == 0 ) {
						sizeBlock = segmentSize[0];
						ninblock = 1;	
					}
					if ( segmentSize[i] > sizeBlock/10 ) {
						ninblock++;
					} else {
						ninblock = 1;
						mergegroupno++;
						sizeBlock = segmentSize[i];
					}
					mergegroup[i] = mergegroupno;
					if ( ninblock >= 10 ) {
						groupstomerge[j++] = mergegroupno;
						mergegroupno++;
						ninblock = 0;
						
					} 
				}
				if ( j > 0 ) {
					StringBuffer status = new StringBuffer();
					for ( int i = 0; i < segmentSize.length; i++ ) {
						status.append("Segment ").append(i).append(" n").append(segmentName[i]).append(" s").append(segmentSize[i]).append(" g").append(mergegroup[i]).append("\n");
					}
					for ( int i = 0; i < groupstomerge.length; i++ ) {
						status.append("Merge group ").append(i).append(" m").append(groupstomerge[i]).append("\n");
					}
					log.info("Search Merge \n"+status);
				}
					
			}
			// groups to merge contains a list of group numbers that need to be
			// merged.
			// mergegroup marks each segment with a group number.
			for ( int i = 0; i < groupstomerge.length; i++ ) {
				if ( groupstomerge[i] != 0 ) {
					StringBuffer status = new StringBuffer();
					status.append("Group ").append(i).append(" Merge ").append(groupstomerge[i]).append("\n");
					File mergeSegment = clusterFS.newSegment();
					
					IndexWriter mergeIndexWriter = null;
					boolean mergeOk = false;
					try {
						mergeIndexWriter = new IndexWriter(FSDirectory.getDirectory(mergeSegment,false), getAnalyzer(), true);
						mergeIndexWriter.setUseCompoundFile(true);
						//indexWriter.setInfoStream(System.out);
						mergeIndexWriter.setMaxMergeDocs(50);
						mergeIndexWriter.setMergeFactor(50);
						ArrayList indexes = new ArrayList();
						for ( int j = 0; j < mergegroup.length; j++  ) {
							if ( mergegroup[j] == groupstomerge[i]) {
								Directory d = FSDirectory.getDirectory(segmentName[j],false);
								if ( d.fileExists("segments") ) {
									status.append("   Merge ").append(segmentName[i].getName()).append(" >> ").append(mergeSegment.getName()).append("\n");
									indexes.add(d);
								}
							}
						}
						log.info("Merging \n"+status);
						mergeIndexWriter.addIndexes((Directory[]) indexes.toArray(new Directory[indexes.size()]));
						mergeIndexWriter.close();
						log.info("Done "+groupstomerge[i]);
						mergeIndexWriter = null;
						// remove old segments
						mergeOk = true;
					} catch ( Exception ex ) {
						log.error("Failed to merge search segments "+ex.getMessage());
						try { mergeIndexWriter.close(); } catch ( Exception ex2 ) {}
						try {
							clusterFS.removeLocalSegment(mergeSegment);
						} catch ( Exception ex2 ) {
							log.error("Failed to remove merge segment "+mergeSegment.getName()+" "+ex2.getMessage());
						}
						
					} finally {
						try {
							mergeIndexWriter.close();
						} catch ( Exception ex) {
						}
					}
					if ( mergeOk ) {
						for ( int j = 0; j < mergegroup.length; j++  ) {
							if ( mergegroup[j] == groupstomerge[i]) {
								clusterFS.removeLocalSegment(segmentName[j]);
							}
						}
					}
				}
			}
		}
		
		
		// merge it with the current index
		File currentSegment = null;
		IndexWriter indexWriter = null;

		log.debug("Found " + segments.size() + " segments ");
		if (segments.size() > 0)
		{
			currentSegment = new File((String) segments
					.get(segments.size() - 1));
			if (!currentSegment.exists()
					|| clusterFS.getTotalSize(currentSegment) > segmentThreshold)
			{
				currentSegment = null;
			}

		}
		if (currentSegment == null)
		{
			currentSegment = clusterFS.newSegment();
			log.debug("Created new segment " + currentSegment.getName());
			indexWriter = new IndexWriter(FSDirectory.getDirectory(currentSegment,false), getAnalyzer(), true);
			indexWriter.setUseCompoundFile(true);
			//indexWriter.setInfoStream(System.out);
			indexWriter.setMaxMergeDocs(50);
			indexWriter.setMergeFactor(50);
		}
		else
		{
			clusterFS.touchSegment(currentSegment);
			indexWriter = new IndexWriter(FSDirectory.getDirectory(currentSegment,false), getAnalyzer(), false);
			indexWriter.setUseCompoundFile(true);
			//indexWriter.setInfoStream(System.out);
			indexWriter.setMaxMergeDocs(50);
			indexWriter.setMergeFactor(50);
		}

		if ( tmpDirectory[0].fileExists("segments") ) {
			indexWriter.addIndexes(tmpDirectory);
		}
		indexWriter.close();

		clusterFS.removeTemporarySegment();
		
		clusterFS.saveSegments();
		
		log.debug("End Index Cycle");
	}
	
	

	/**
	 * @return Returns the analzyserFactory.
	 */
	public AnalyzerFactory getAnalyzerFactory()
	{
		return analyzerFactory;
	}

	/**
	 * @param analzyserFactory
	 *        The analzyserFactory to set.
	 */
	public void setAnalyzerFactory(AnalyzerFactory analzyserFactory)
	{
		this.analyzerFactory = analzyserFactory;
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


}
