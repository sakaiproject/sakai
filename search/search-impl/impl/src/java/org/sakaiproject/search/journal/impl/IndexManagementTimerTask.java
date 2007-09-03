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

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.search.journal.api.ManagementOperation;

/**
 * @author ieb
 */
public class IndexManagementTimerTask extends TimerTask
{

	private static final Log log = LogFactory.getLog(IndexManagementTimerTask.class);

	private long delay = 60000L;

	private long period = 60000L;

	private boolean fixedRate = false;

	private ManagementOperation managementOperation;

	/**
	 * 
	 */
	public IndexManagementTimerTask()
	{
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
		try
		{
			managementOperation.runOnce();
		}
		catch (Throwable t)
		{
			log.error("Management Operation failed ", t);
		}
	}

}
