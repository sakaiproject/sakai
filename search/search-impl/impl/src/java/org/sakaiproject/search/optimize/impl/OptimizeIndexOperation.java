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

package org.sakaiproject.search.optimize.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.journal.api.JournalErrorException;
import org.sakaiproject.search.journal.api.JournalExhausetedException;
import org.sakaiproject.search.journal.api.JournaledObject;
import org.sakaiproject.search.journal.api.ManagementOperation;
import org.sakaiproject.search.optimize.api.NoOptimizationRequiredException;
import org.sakaiproject.search.transaction.api.IndexTransactionException;

/**
 * Performs an optimize operation using an OptimizIndexManager to manage the
 * 2PC.
 * 
 * @author ieb
 */
public class OptimizeIndexOperation implements ManagementOperation
{

	private static final Log log = LogFactory.getLog(OptimizeIndexOperation.class);

	/**
	 * The journaled object which is being optimized
	 */
	private JournaledObject journaledObject;

	/**
	 * The manager that performs the optimization
	 */
	private OptimizeIndexManager optimizeUpdateManager;

	public void destroy()
	{

	}

	public void init()
	{
	}

	/**
	 * @see org.sakaiproject.search.journal.api.ManagementOperation#runOnce()
	 */
	public void runOnce()
	{

		/*
		 * Run the optimizer transaction once
		 */

		if (journaledObject.aquireUpdateLock())
		{
			log.debug("Now Locked Journaled savePoint is "
					+ journaledObject.getLastJournalEntry());
			try
			{
				try
				{
					IndexOptimizeTransactionImpl optimizeUpdateTransaction = null;
					try
					{
						Map<String, Object> m = new HashMap<String, Object>();
						optimizeUpdateTransaction = (IndexOptimizeTransactionImpl) optimizeUpdateManager
								.openTransaction(m);
						optimizeUpdateTransaction.prepare();
						optimizeUpdateTransaction.commit();
						log.debug("Optimize complete ");
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
							if (optimizeUpdateTransaction != null)
								optimizeUpdateTransaction.close();
						}
						catch (Exception ex)
						{
							log.debug(ex);
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
