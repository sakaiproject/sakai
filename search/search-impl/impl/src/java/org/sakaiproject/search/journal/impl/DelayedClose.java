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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author ieb
 */
public abstract class DelayedClose implements Delayed
{

	private static final Log log = LogFactory.getLog(DelayedClose.class);

	private long end;

	/**
	 * @param delay2
	 * @param inclose
	 */
	public DelayedClose(long delay)
	{
		this.end = System.currentTimeMillis() + delay;
		log.debug("Delayed close will trigger at "+end);
	}

	public long getDelay(TimeUnit unit)
	{
		long tnow = System.currentTimeMillis();
		long milisdiff = end - tnow;
		long t = unit.convert(milisdiff, TimeUnit.MILLISECONDS);
		return t;
	}

	public int compareTo(Delayed del)
	{

		if (end < ((DelayedClose) del).end)
		{
			return -1;
		}
		else if (end > ((DelayedClose) del).end)
		{
			return 1;
		}
		return 0;
	}

	protected abstract void close();

}
