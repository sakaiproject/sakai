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

package org.sakaiproject.search.optimize.shared.api;

import java.io.File;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.sakaiproject.search.journal.api.JournalManager;
import org.sakaiproject.search.journal.api.JournalManagerState;
import org.sakaiproject.search.transaction.api.IndexTransaction;

/**
 * @author ieb
 */
public interface JournalOptimizationTransaction extends IndexTransaction
{

	/**
	 * @param mergeList
	 */
	void setMergeList(List<Long> mergeList);

	/**
	 * @return
	 */
	List<Long> getMergeList();

	/**
	 * @param localJournalLocation
	 */
	void addMergeSegment(File localJournalLocation);

	/**
	 * @return
	 */
	List<File> getMergeSegmentList();

	/**
	 * @return
	 */
	long getTargetSavePoint();

	/**
	 * @param long1
	 */
	void setTargetSavePoint(Long long1);

	/**
	 * @param localJournalLocation
	 */
	void setTargetSegment(File localJournalLocation);

	/**
	 * @return
	 */
	File getTargetSegment();

	/**
	 * @return
	 */
	Analyzer getAnalyzer();

	/**
	 * @return
	 */
	String getWorkingSpace();

	/**
	 * @return
	 */
	JournalManager getJournalManager();


	/**
	 * @param workingSegment
	 */
	void setWorkingSegment(File workingSegment);

	/**
	 * @return
	 */
	File getWorkingSegment();

	/**
	 * @return
	 */
	JournalManagerState getState();

	/**
	 * 
	 */
	void clearState();

	/**
	 * @param jms
	 */
	void setState(JournalManagerState jms);

}
