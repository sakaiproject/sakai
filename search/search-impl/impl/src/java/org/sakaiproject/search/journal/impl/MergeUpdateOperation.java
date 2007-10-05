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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.journal.api.IndexMergeTransaction;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.journal.api.JournalExhausetedException;
import org.sakaiproject.search.journal.api.JournaledObject;
import org.sakaiproject.search.journal.api.ManagementOperation;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * @author ieb TODO Unit test
 */
public class MergeUpdateOperation implements ManagementOperation
{

	private static final Log log = LogFactory.getLog(MergeUpdateOperation.class);

	private JournaledObject journaledObject;

	private MergeUpdateManager mergeUpdateManager;

	public void init()
	{

	}

	public void destroy()
	{

	}

	/**
	 * @see org.sakaiproject.search.journal.api.ManagementOperation#runOnce()
	 */
	public void runOnce()
	{

		// find the current journal ID,
		// get a list of later jourals
		// unpack them locally
		// merge with the curent index.
		// 
		log.debug("Last Journaled savePoint is " + journaledObject.getLastJournalEntry());

		if (journaledObject.aquireUpdateLock())
		{
			log.debug("Now Locked Journaled savePoint is "
					+ journaledObject.getLastJournalEntry());
			try
			{
				try
				{
					while (true)
					{
						IndexMergeTransaction mergeUpdateTransaction = null;
						try
						{
							Map<String, Object> m = new HashMap<String, Object>();
							mergeUpdateTransaction = (IndexMergeTransaction) mergeUpdateManager
									.openTransaction(m);
							mergeUpdateTransaction.prepare();
							mergeUpdateTransaction.commit();
							log.info("Merged Journal "
									+ mergeUpdateTransaction.getJournalEntry()
									+ " from the redolog");
						}
						catch (JournalErrorException jex)
						{
							if (mergeUpdateTransaction != null)
							{
								log.warn("Failed to compete merge of "
										+ mergeUpdateTransaction.getJournalEntry() + " ",
										jex);
								try
								{
									mergeUpdateTransaction.rollback();
								}
								catch (Exception ex)
								{
									log.warn("Failed to rollback transaction ", ex);
								}
							}
							else
							{
								log.warn("Failed to start merge operation ", jex);

							}
							break;
						}
						catch (IndexTransactionException iupex)
						{
							if (mergeUpdateTransaction != null)
							{
								log.warn("Failed to compete merge of "
										+ mergeUpdateTransaction.getJournalEntry() + "",
										iupex);
								try
								{
									mergeUpdateTransaction.rollback();
								}
								catch (Exception ex)
								{
									log.warn("Failed to rollback transaction ", ex);
								}
							}
							else
							{
								log.warn("Failed to start merge operation ", iupex);

							}
						}
						finally
						{
							try
							{
								mergeUpdateTransaction.close();
							}
							catch (Exception ex)
							{
							}

						}
					}
				}
				catch (JournalExhausetedException ex)
				{
					if (log.isDebugEnabled())
					{
						log.debug("No More Jounral Entries ", ex);
					}
				}
			}
			finally
			{
				journaledObject.releaseUpdateLock();
			}
		}
		else
		{
			log.warn("No Lock, index update abandoned");
			journaledObject.debugLock();
		}
	}

	/**
	 * @return the journaledObject
	 */
	public JournaledObject getJournaledObject()
	{
		return journaledObject;
	}

	/**
	 * @param journaledObject
	 *        the journaledObject to set
	 */
	public void setJournaledObject(JournaledObject journaledObject)
	{
		this.journaledObject = journaledObject;
	}

	/**
	 * @return the mergeUpdateManager
	 */
	public MergeUpdateManager getMergeUpdateManager()
	{
		return mergeUpdateManager;
	}

	/**
	 * @param mergeUpdateManager
	 *        the mergeUpdateManager to set
	 */
	public void setMergeUpdateManager(MergeUpdateManager mergeUpdateManager)
	{
		this.mergeUpdateManager = mergeUpdateManager;
	}

}
