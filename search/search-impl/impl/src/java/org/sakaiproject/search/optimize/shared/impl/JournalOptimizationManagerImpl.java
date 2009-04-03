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

package org.sakaiproject.search.optimize.shared.impl;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.sakaiproject.search.index.AnalyzerFactory;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.impl.JournalSettings;
import org.sakaiproject.search.optimize.shared.api.JournalOptimizationManager;
import org.sakaiproject.search.transaction.api.IndexTransaction;
import org.sakaiproject.search.transaction.api.IndexTransactionException;
import org.sakaiproject.search.transaction.impl.TransactionManagerImpl;

/**
 * @author ieb
 */
public class JournalOptimizationManagerImpl extends TransactionManagerImpl implements
		JournalOptimizationManager
{

	private JournalManager journalManager;

	private AnalyzerFactory analyzerFactory;

	private JournalSettings journalSettings;

	public void init()
	{

	}

	public void destroy()
	{

	}

	/**
	 * @see org.sakaiproject.search.transaction.api.TransactionIndexManager#openTransaction(java.util.Map)
	 */
	public IndexTransaction openTransaction(Map<String, Object> m)
			throws IndexTransactionException
	{
		JournalOptimizationTransactionImpl it = new JournalOptimizationTransactionImpl(
				this, m);
		it.open();
		return it;
	}

	/**
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationManager#getAnalyzer()
	 */
	public Analyzer getAnalyzer()
	{
		return analyzerFactory.newAnalyzer();
	}

	/**
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationManager#getJournalManager()
	 */
	public JournalManager getJournalManager()
	{
		return journalManager;
	}

	/**
	 * @see org.sakaiproject.search.optimize.shared.api.JournalOptimizationManager#getWorkingSpace()
	 */
	public String getWorkingSpace()
	{
		return journalSettings.getSharedOptimizeWorkingSpace();
	}

	/**
	 * @return the analyzerFactory
	 */
	public AnalyzerFactory getAnalyzerFactory()
	{
		return analyzerFactory;
	}

	/**
	 * @param analyzerFactory
	 *        the analyzerFactory to set
	 */
	public void setAnalyzerFactory(AnalyzerFactory analyzerFactory)
	{
		this.analyzerFactory = analyzerFactory;
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
