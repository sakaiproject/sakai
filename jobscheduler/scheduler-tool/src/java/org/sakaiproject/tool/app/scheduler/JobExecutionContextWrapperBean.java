package org.sakaiproject.tool.app.scheduler;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
							parentTool.rb.getFormattedMessage("kill_message", 
									new String[] {jec.getJobDetail().getName()}), null));
		}
		
		return "runningJobs";
	}
	
}
