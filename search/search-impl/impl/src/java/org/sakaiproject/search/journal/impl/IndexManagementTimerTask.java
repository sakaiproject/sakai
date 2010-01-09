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

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.journal.api.ManagementOperation;
import org.sakaiproject.thread_local.api.ThreadLocalManager;

/**
 * A timer Task for a management operation in the search code
 * 
 * @author ieb
 */
public class IndexManagementTimerTask extends TimerTask
{

	private static final Log log = LogFactory.getLog(IndexManagementTimerTask.class);

	private long delay = 60000L;

	private long period = 60000L;

	private boolean fixedRate = false;

	private ManagementOperation managementOperation;

	private boolean closed = false;

	private ThreadLocalManager threadLocalManager;

	/**
	 * 
	 */
	public IndexManagementTimerTask()
	{
	}

	public void init()
	{

	}

	public void destroy()
	{
		closed = true;
	}

	/**
	 * @return
	 */
	public long getDelay()
	{
		return delay;
	}

	/**
	 * @return
	 */
	public long getPeriod()
	{
		return period;
	}

	/**
	 * @param delay
	 *        the delay to set
	 */
	public void setDelay(long delay)
	{
		this.delay = delay;
	}

	/**
	 * @param period
	 *        the period to set
	 */
	public void setPeriod(long period)
	{
		this.period = period;
	}

	/**
	 * @return
	 */
	public boolean isFixedRate()
	{
		return fixedRate;
	}

	/**
	 * @param fixedRate
	 *        the fixedRate to set
	 */
	public void setFixedRate(boolean fixedRate)
	{
		this.fixedRate = fixedRate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run()
	{
		if (closed)
		{
			return;
		}
		try
		{
			ConcurrentIndexManager.setRunning(managementOperation);
			threadLocalManager.clear();
			managementOperation.runOnce();
			threadLocalManager.clear();
		
		}
		catch (Throwable t)
		{
			log.error("Management Operation failed ", t);
		} finally {
			ConcurrentIndexManager.setRunning(null);
		}
	}

	/**
	 * @return the managementOperation
	 */
	public ManagementOperation getManagementOperation()
	{
		return managementOperation;
	}

	/**
	 * @param managementOperation
	 *        the managementOperation to set
	 */
	public void setManagementOperation(ManagementOperation managementOperation)
	{
		this.managementOperation = managementOperation;
	}

	/**
	 * @return the closed
	 */
	public boolean isClosed()
	{
		return closed;
	}

	/**
	 * @param closed
	 *        the closed to set
	 */
	public void setClosed(boolean closed)
	{
		this.closed = closed;
	}

	public ThreadLocalManager getThreadLocalManager() {
		return threadLocalManager;
	}

	public void setThreadLocalManager(ThreadLocalManager threadLocalManager) {
		this.threadLocalManager = threadLocalManager;
	}


}
