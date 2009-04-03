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

import java.io.File;

/**
 * @author ieb
 */
public class JournalSettings
{
	private String localIndexBase;

	private String sharedJournalBase;

	private int minimumOptimizeSavePoints;

	private String sharedOptimizeWorkingSpace;

	private int optimizeMergeSize;

	private String optimizerWorkingDirectory;

	private String indexerWorkingDirectory;

	private boolean soakTest;

	private String localIndexWorkingSpace;

	private String searchIndexDirectory;

	private String journalLocation;

	private String sharedOptimizeCreateIndexWorkingSpace;

	private int sharedMaxMergeFactor;

	private int sharedMaxBufferedDocs;

	private int sharedMaxMergeDocs;

	private int localMaxBufferedDocs;

	private int localMaxMergeFactor;

	private int localMaxMergeDocs;

	private int createMaxMergeDocs;

	private int createMaxBufferedDocs;

	private int createMaxMergeFactor;

	private boolean compressShared = false;

	public void init()
	{

	}

	public void destroy()
	{

	}

	/**
	 * @return the localIndexBase
	 */
	public String getLocalIndexBase()
	{
		return localIndexBase;
	}

	/**
	 * @param localIndexBase
	 *        the localIndexBase to set
	 */
	public void setLocalIndexBase(String localIndexBase)
	{
		this.localIndexBase = localIndexBase;
		indexerWorkingDirectory = new File(localIndexBase, "indexer-work")
				.getAbsolutePath();
		localIndexWorkingSpace = new File(localIndexBase, "index-import")
				.getAbsolutePath();
		optimizerWorkingDirectory = new File(localIndexBase, "index-optimize")
				.getAbsolutePath();
		searchIndexDirectory = new File(localIndexBase, "index").getAbsolutePath();
		sharedOptimizeWorkingSpace = new File(localIndexBase, "journal-optimize-import")
				.getAbsolutePath();
	}

	/**
	 * @return the sharedJournalBase
	 */
	public String getSharedJournalBase()
	{
		return sharedJournalBase;
	}

	/**
	 * @param sharedJournalBase
	 *        the sharedJournalBase to set
	 */
	public void setSharedJournalBase(String sharedJournalBase)
	{
		this.sharedJournalBase = sharedJournalBase;
		journalLocation = new File(sharedJournalBase, "searchjournal").getAbsolutePath();
	}

	/**
	 * @return
	 */
	public String getJournalLocation()
	{
		return journalLocation;
	}

	/**
	 * @return
	 */
	public String getSearchIndexDirectory()
	{
		return searchIndexDirectory;
	}

	/**
	 * @return
	 */
	public String getLocalIndexWorkingSpace()
	{
		return localIndexWorkingSpace;
	}

	/**
	 * @return
	 */
	public boolean getSoakTest()
	{
		return soakTest;
	}

	/**
	 * @return
	 */
	public String getIndexerWorkingDirectory()
	{
		return indexerWorkingDirectory;
	}

	/**
	 * @return
	 */
	public String getOptimizerWorkingDirectory()
	{
		return optimizerWorkingDirectory;
	}

	/**
	 * @return
	 */
	public int getOptimizeMergeSize()
	{
		return optimizeMergeSize;
	}

	/**
	 * @return
	 */
	public String getSharedOptimizeWorkingSpace()
	{
		return sharedOptimizeWorkingSpace;
	}

	/**
	 * @return
	 */
	public int getMinimumOptimizeSavePoints()
	{
		return minimumOptimizeSavePoints;
	}

	/**
	 * @param minimumOptimizeSavePoints
	 *        the minimumOptimizeSavePoints to set
	 */
	public void setMinimumOptimizeSavePoints(int minimumOptimizeSavePoints)
	{
		this.minimumOptimizeSavePoints = minimumOptimizeSavePoints;
	}

	/**
	 * @param optimizMergeSize
	 *        the optimizMergeSize to set
	 */
	public void setOptimizeMergeSize(int optimizeMergeSize)
	{
		this.optimizeMergeSize = optimizeMergeSize;
	}

	/**
	 * @param soakTest
	 *        the soakTest to set
	 */
	public void setSoakTest(boolean soakTest)
	{
		this.soakTest = soakTest;
	}

	/**
	 * @return
	 */
	public int getLocalMaxMergeDocs()
	{
		return localMaxMergeDocs;
	}

	/**
	 * @return
	 */
	public int getLocalMaxMergeFactor()
	{
		return localMaxMergeFactor;
	}

	/**
	 * @return
	 */
	public int getLocalMaxBufferedDocs()
	{
		return localMaxBufferedDocs;
	}

	/**
	 * @return
	 */
	public int getSharedMaxMergeDocs()
	{
		return sharedMaxMergeDocs;
	}

	/**
	 * @return
	 */
	public int getSharedMaxBufferedDocs()
	{
		return sharedMaxBufferedDocs;
	}

	/**
	 * @return
	 */
	public int getSharedMaxMergeFactor()
	{
		return sharedMaxMergeFactor;
	}

	/**
	 * @param localMaxBufferedDocs the localMaxBufferedDocs to set
	 */
	public void setLocalMaxBufferedDocs(int localMaxBufferedDocs)
	{
		this.localMaxBufferedDocs = localMaxBufferedDocs;
	}

	/**
	 * @param localMaxMergeDocs the localMaxMergeDocs to set
	 */
	public void setLocalMaxMergeDocs(int localMaxMergeDocs)
	{
		this.localMaxMergeDocs = localMaxMergeDocs;
	}

	/**
	 * @param localMaxMergeFactor the localMaxMergeFactor to set
	 */
	public void setLocalMaxMergeFactor(int localMaxMergeFactor)
	{
		this.localMaxMergeFactor = localMaxMergeFactor;
	}

	/**
	 * @param sharedMaxBufferedDocs the sharedMaxBufferedDocs to set
	 */
	public void setSharedMaxBufferedDocs(int sharedMaxBufferedDocs)
	{
		this.sharedMaxBufferedDocs = sharedMaxBufferedDocs;
	}

	/**
	 * @param sharedMaxMergeDocs the sharedMaxMergeDocs to set
	 */
	public void setSharedMaxMergeDocs(int sharedMaxMergeDocs)
	{
		this.sharedMaxMergeDocs = sharedMaxMergeDocs;
	}

	/**
	 * @param sharedMaxMergeFactor the sharedMaxMergeFactor to set
	 */
	public void setSharedMaxMergeFactor(int sharedMaxMergeFactor)
	{
		this.sharedMaxMergeFactor = sharedMaxMergeFactor;
	}

	/**
	 * @return
	 */
	public int getCreateMaxMergeDocs()
	{
		return createMaxMergeDocs;
	}

	/**
	 * @return
	 */
	public int getCreateMaxBufferedDocs()
	{
		return createMaxBufferedDocs;
	}

	/**
	 * @return
	 */
	public int getCreateMaxMergeFactor()
	{
		return createMaxMergeFactor;
	}

	/**
	 * @param createMaxBufferedDocs the createMaxBufferedDocs to set
	 */
	public void setCreateMaxBufferedDocs(int createMaxBufferedDocs)
	{
		this.createMaxBufferedDocs = createMaxBufferedDocs;
	}

	/**
	 * @param createMaxMergeDocs the createMaxMergeDocs to set
	 */
	public void setCreateMaxMergeDocs(int createMaxMergeDocs)
	{
		this.createMaxMergeDocs = createMaxMergeDocs;
	}

	/**
	 * @param createMaxMergeFactor the createMaxMergeFactor to set
	 */
	public void setCreateMaxMergeFactor(int createMaxMergeFactor)
	{
		this.createMaxMergeFactor = createMaxMergeFactor;
	}

	/**
	 * @return
	 */
	public boolean getCompressShared()
	{
		return compressShared;
	}

	/**
	 * @param compressShared the compressShared to set
	 */
	public void setCompressShared(boolean compressShared)
	{
		this.compressShared = compressShared;
	}
	
	


}
