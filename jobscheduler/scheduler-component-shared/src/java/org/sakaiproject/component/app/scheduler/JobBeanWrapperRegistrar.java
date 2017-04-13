package org.sakaiproject.component.app.scheduler;

import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.api.app.scheduler.SchedulerManager;

import java.util.List;

/**
 * Just registers JobBeanWrappers with the Schedule Manager, this is needed because the autowired
 * jobs don't have to register themselves.
 */
public class JobBeanWrapperRegistrar {

    private SchedulerManager schedulerManager;
    private List<JobBeanWrapper> jobBeans;

    public void setSchedulerManager(SchedulerManager schedulerManager) {
        this.schedulerManager = schedulerManager;
    }

    public void setJobBeans(List<JobBeanWrapper> jobBeans) {
        this.jobBeans = jobBeans;
    }

    public void init() {
        jobBeans.stream().forEach(wrapper -> schedulerManager.registerBeanJob(wrapper.getJobType(), wrapper));
    }
}
