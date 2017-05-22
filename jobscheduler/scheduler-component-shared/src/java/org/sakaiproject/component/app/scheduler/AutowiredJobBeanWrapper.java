package org.sakaiproject.component.app.scheduler;

import org.quartz.Job;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;

/**
 * This is a JobBeanWrapper that will just be registered with the Job Scheduler.
 * This class isn't actually used when running the job as quartz will directly create an instance of the class.
 */
public class AutowiredJobBeanWrapper  implements JobBeanWrapper {

    private String jobType;
    private Class<? extends Job> aClass;

    public AutowiredJobBeanWrapper(Class<? extends Job> aClass, String jobType) {
        this.aClass = aClass;
        this.jobType = jobType;
    }

    @Override
    public String getBeanId() {
        // We don't need a bean ID as a new instance will be created and will get autowired.
        return null;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return aClass;
    }

    @Override
    public String getJobType() {
        return jobType;
    }
}
