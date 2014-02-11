package org.sakaiproject.webservices;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.tool.api.Session;



/**
 * SakaiJob.jws
 * <p/>
 * A set of administrative web services for managing quartz jobs in Sakai
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class SakaiJob extends AbstractWebService {

    private static final Log LOG = LogFactory.getLog(SakaiJob.class);

    /**
     * Delete a job from the Job Scheduler
     * @param sessionId	valid admin session id
     * @param name		job name
     * @return
     * @
     */
    public String deleteJob(String sessionId, String name)  {
        Session session = establishSession(sessionId);
        if (!securityService.isSuperUser()) {
            LOG.warn("NonSuperUser trying to collect configuration: " + session.getUserId());
            return "error: NonSuperUser trying to collect configuration: " + session.getUserId();
        }
        try {
            schedulerManager.getScheduler().deleteJob(name, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            LOG.warn(e.getMessage(), e);
            return "error:" + e.getMessage();
        }
        return "success";
    }

    /**
     * Create a job in the Job Scheduler
     * @param sessionId 	valid admin session id
     * @param name			name of job
     * @param type			type of job
     * @return
     * @
     */
    public String createJob(String sessionId, String name, String type)  {

        Session session = establishSession(sessionId);
        if (!securityService.isSuperUser()) {
            LOG.warn("NonSuperUser trying to collect configuration: " + session.getUserId());
            return "error: NonSuperUser trying to collect configuration: " + session.getUserId();
        }

        Scheduler scheduler = schedulerManager.getScheduler();
        JobBeanWrapper job = schedulerManager.getJobBeanWrapper(type);
        JobDetail jd = null;
        
        if (job != null) {
            // create a new JobDetail object for this job
            jd = createJobDetail(job, name);
            try {
                scheduler.addJob(jd, false);
            } catch (SchedulerException e) {
                LOG.warn(e.getMessage(), e);
                return "error: " + e.getMessage();
            }

            return "success";
        }

        return "error: can't find job of type: " + type;
    }

    /**
     * Run a job from the Job Scheduler
     * @param sessionId 	valid admin session id
     * @param jobName		name of job to run
     * @return
     * @
     */
    public String runJob(String sessionId, String jobName)  {
        Session session = establishSession(sessionId);
        if (!securityService.isSuperUser()) {
            LOG.warn("NonSuperUser trying to collect configuration: " + session.getUserId());
            return "error: NonSuperUser trying to collect configuration: " + session.getUserId();
        }

        JobDetail jobDetail = null;
        Scheduler scheduler = schedulerManager.getScheduler();

        try {
            jobDetail = scheduler.getJobDetail(jobName, Scheduler.DEFAULT_GROUP);
        } catch (SchedulerException e) {
            LOG.warn(e.getMessage(), e);
            return "error: " + e.getMessage();
        }
        
        if (jobDetail != null) {
            try {
                scheduler.triggerJob(jobDetail.getName(), jobDetail.getGroup());
            } catch (SchedulerException e) {
                LOG.warn(e.getMessage(), e);
                return "error: " + e.getMessage();
            }
        } else {
            return "error: can't find a job with name: " + jobName;
        }

        return "success";
    }

    private JobDetail createJobDetail(JobBeanWrapper job, String jobName) {
        JobDetail jd = new JobDetail(jobName, Scheduler.DEFAULT_GROUP, job.getJobClass(), false, true, true);
        JobDataMap map = jd.getJobDataMap();

        map.put(JobBeanWrapper.SPRING_BEAN_NAME, job.getBeanId());
        map.put(JobBeanWrapper.JOB_TYPE, job.getJobType());

        return jd;
    }
}
