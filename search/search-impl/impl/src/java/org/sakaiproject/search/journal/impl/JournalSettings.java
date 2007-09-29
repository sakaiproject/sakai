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

/**
 * @author ieb
 */
public class JournalSettings
{
	private String localIndexBase;

	private String sharedJournalBase;

	private int minimumOptimizeSavePoints;

	private String sharedOptimizeWorkingSpace;

	private int optimizMergeSize;

	private String optimizerWorkingDirectory;

	private String indexerWorkingDirectory;

	private boolean soakTest;

	private String localIndexWorkingSpace;

	private String searchIndexDirectory;

	private String journalLocation;

	public void init()
	{

	}

	public void destory()
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
		sharedJournalBase = new File(localIndexBase, "journal-optimize")
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
	public int getOptimizMergeSize()
	{
		return optimizMergeSize;
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
	public void setOptimizMergeSize(int optimizMergeSize)
	{
		this.optimizMergeSize = optimizMergeSize;
	}

	/**
	 * @param soakTest
	 *        the soakTest to set
	 */
	public void setSoakTest(boolean soakTest)
	{
		this.soakTest = soakTest;
	}

}
