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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationManager;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction;
import org.sakaiproject.search.transaction.impl.IndexTransactionImpl;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;

/**
 * @author ieb
 */
public class JournalOptimizationTransactionImpl extends IndexTransactionImpl implements
		JournalOptimizationTransaction
{

	private List<Long> mergeList = new ArrayList<Long>();

	private List<File> mergeSegments = new ArrayList<File>();

	private long targetVersion;

	private List<File> mergeSegmentList = new ArrayList<File>();

	private File targetSegment;

	/***
	 * @param manager
	 * @param m
	 */
	public JournalOptimizationTransactionImpl(TransactionManagerImpl manager,
			Map<String, Object> m)
	{
		super(manager, m);
	}

	/***
	 * @param mergeList
	 */
	public void setMergeList(List<Long> mergeList)
	{
		this.mergeList = mergeList;

	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#getMergeList()
	 */
	public List<Long> getMergeList()
	{
		return mergeList;
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#addMergeSegment(java.io.File)
	 */
	public void addMergeSegment(File localJournalLocation)
	{
		mergeSegments.add(localJournalLocation);

	}

	/***
	 * @param long1
	 */
	public void setTargetVersion(long targetVersion)
	{
		this.targetVersion = targetVersion;
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#getMergeSegmentList()
	 */
	public List<File> getMergeSegmentList()
	{
		return mergeSegmentList;
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#getTargetSegment()
	 */
	public File getTargetSegment()
	{
		return targetSegment;
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#getTargetVersion()
	 */
	public long getTargetVersion()
	{
		return targetVersion;
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#setTargetSegment(java.io.File)
	 */
	public void setTargetSegment(File targetSegment)
	{
		this.targetSegment = targetSegment;
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#getAnalyzer()
	 */
	public Analyzer getAnalyzer()
	{
		return ((JournalOptimizationManager) manager).getAnalyzer();
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#getJournalManager()
	 */
	public JournalManager getJournalManager()
	{
		return ((JournalOptimizationManager) manager).getJournalManager();
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#getWorkingSpace()
	 */
	public String getWorkingSpace()
	{
		return ((JournalOptimizationManager) manager).getWorkingSpace();
	}

	/**
	 *
	 * 
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationTransaction#setTargetVersion(java.lang.Long)
	 */
	public void setTargetVersion(Long targetVersion)
	{
		this.targetVersion = targetVersion;

	}

}
