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

import java.util.Iterator;
import java.util.List;
import java.util.Timer;

/**
 * The ConcurrentIndexManager,  manages a single thread performs a number of tasks associated with index management.
 * @author ieb
 *
 */
public class ConcurrentIndexManager
{
	private Timer timer = new Timer(true);;
	private List<IndexManagementTimerTask> tasks;
	

	public void init() {
		for ( Iterator<IndexManagementTimerTask> i = tasks.iterator(); i.hasNext(); ) {
			IndexManagementTimerTask task = i.next();
			if ( task.isFixedRate() ) {
				timer.scheduleAtFixedRate(task, task.getDelay(), task.getPeriod());
			} else {
				timer.schedule(task, task.getDelay(), task.getPeriod());
				
			}
		}
	}


	/**
	 * @return the tasks
	 */
	public List<IndexManagementTimerTask> getTasks()
	{
		return tasks;
	}


	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(List<IndexManagementTimerTask> tasks)
	{
		this.tasks = tasks;
	}

}
