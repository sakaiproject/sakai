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
import org.sakaiproject.search.optimize.api.NoOptimizationRequiredException;
import org.sakaiproject.search.optimize.impl.IndexOptimizeTransactionImpl;
import org.sakaiproject.search.optimize.impl.OptimizeIndexManager;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * @author ieb TODO Unit test
 */
public class MergeUpdateOperation implements ManagementOperation
{

	private static final Log log = LogFactory.getLog(MergeUpdateOperation.class);

	private JournaledObject journaledObject;

	private MergeUpdateManager mergeUpdateManager;

	private OptimizeIndexManager optimizeUpdateManager;

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
		int count = 0;
		if (journaledObject.aquireUpdateLock())
		{
			log.debug("Now Locked Journaled savePoint is "
					+ journaledObject.getLastJournalEntry());
			try
			{
				StringBuilder sb = new StringBuilder();
				try
				{
					while (true)
					{
						IndexMergeTransaction mergeUpdateTransaction = null;
						try
						{
							long start = System.currentTimeMillis();
							Map<String, Object> m = new HashMap<String, Object>();
							mergeUpdateTransaction = (IndexMergeTransaction) mergeUpdateManager
									.openTransaction(m);
							mergeUpdateTransaction.prepare();
							mergeUpdateTransaction.commit();
							long timeTaken = System.currentTimeMillis() - start;
							sb.append("\n\tMerged Journal ").append(
									mergeUpdateTransaction.getJournalEntry()).append(
									" from the redolog in " + timeTaken + " ms");
							count++;
							if ((count > 10 && timeTaken > 500) || count > 50)
							{
								count = 0;
								IndexOptimizeTransactionImpl optimizeUpdateTransaction = null;
								try
								{
									start = System.currentTimeMillis();
									m = new HashMap<String, Object>();
									optimizeUpdateTransaction = (IndexOptimizeTransactionImpl) optimizeUpdateManager
											.openTransaction(m);
									optimizeUpdateTransaction.prepare();
									optimizeUpdateTransaction.commit();
									timeTaken = System.currentTimeMillis() - start;
									sb.append("Optimize complete in  ").append(timeTaken).append("ms");
								}
								catch (JournalErrorException jex)
								{
									if (optimizeUpdateTransaction != null)
									{
										log.warn("Failed to compete Optimize ", jex);
									}
									else
									{
										log.warn("Failed to start merge operation ", jex);

									}
									try
									{
										optimizeUpdateTransaction.rollback();
									}
									catch (Exception ex)
									{
										log.warn("Failed to rollback transaction ", ex);
									}
								}
								catch (NoOptimizationRequiredException nop)
								{

									log.debug("No Merge Performed " + nop.getMessage());
								}
								catch (IndexTransactionException iupex)
								{

									log.warn("Failed to compete optimize ", iupex);
									try
									{
										optimizeUpdateTransaction.rollback();
									}
									catch (Exception ex)
									{
										log.warn("Failed to rollback transaction ", ex);
									}
								}
								finally
								{
									try
									{
										optimizeUpdateTransaction.close();
									}
									catch (Exception ex)
									{
									}

								}
							}

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
								if (mergeUpdateTransaction != null) 
									mergeUpdateTransaction.close();
							}
							catch (Exception ex)
							{
								log.debug(ex);							}

						}
					}
				}
				catch (JournalExhausetedException ex)
				{
					if (log.isDebugEnabled())
					{
						log.debug("No More Journal Entries ");
					}
				}
				log.info("Local Merge Operation "+sb.toString()+"\n");
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

	/**
	 * @return the optimizeUpdateManager
	 */
	public OptimizeIndexManager getOptimizeUpdateManager()
	{
		return optimizeUpdateManager;
	}

	/**
	 * @param optimizeUpdateManager
	 *        the optimizeUpdateManager to set
	 */
	public void setOptimizeUpdateManager(OptimizeIndexManager optimizeUpdateManager)
	{
		this.optimizeUpdateManager = optimizeUpdateManager;
	}

}
