/**
 * Copyright (c) 2003-2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.app.scheduler;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import lombok.extern.slf4j.Slf4j;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;

@Slf4j
public class JobExecutionContextWrapperBean {

	private JobExecutionContext jec;
	private boolean isKillable = false;
	private SchedulerTool parentTool;
	
	public JobExecutionContextWrapperBean () {
	}
	
	public JobExecutionContextWrapperBean (SchedulerTool parentTool, JobExecutionContext jec) {
		this.jec = jec;
		this.parentTool = parentTool;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.app.scheduler.JobExecutionContextWrapper#getJec()
	 */
	public JobExecutionContext getJec() {
		return jec;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.app.scheduler.JobExecutionContextWrapper#setJec(org.quartz.JobExecutionContext)
	 */
	public void setJec(JobExecutionContext jec) {
		this.jec = jec;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.app.scheduler.JobExecutionContextWrapper#isKillable()
	 */
	public boolean getIsKillable() {
		return isKillable;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.app.scheduler.JobExecutionContextWrapper#setKillable(boolean)
	 */
	public void setIsKillable(boolean isKillable) {
		this.isKillable = isKillable;
	}

	public String processActionKill() {
		if (getIsKillable()) {
			InterruptableJob job = (InterruptableJob)jec.getJobInstance();
			try {
				job.interrupt();
			} catch (UnableToInterruptJobException e) {
				log.error(e.getMessage(), e);
			}
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							parentTool.rb.getFormattedMessage("kill_message", 
									new String[] {jec.getJobDetail().getKey().getName()}), null));
		}
		
		return "runningJobs";
	}
	
}
