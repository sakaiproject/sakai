package org.sakaiproject.tool.assessment.services;



import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.StatefulJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;

import org.sakaiproject.site.cover.SiteService;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.authz.cover.AuthzGroupService;


public class AutoSubmitAssessmentsJob implements StatefulJob {
	
	private static final Log LOG = LogFactory.getLog(AutoSubmitAssessmentsJob.class);	
	protected String serverName = "unknown";
	
	public void init() {
		LOG.debug("AutoSubmitAssessmentsJob init()  ");
	}

	public void destroy() {
		LOG.debug("AutoSubmitAssessmentsJob destroy()");
	}

	
	public AutoSubmitAssessmentsJob() {
		super();
	}
 
	/*
	 * This job expects to find a nexusId in its trigger name or "where clause fragments"
	 * in its property file, e.g "and termid = 1064". 
	 * It runs the CourseAnchorImport first, then the CourseMaterialsImport, the AssignmentMigration 
	 * and the SyllabusImport. If it runs into an exception during any of the imports, it stops.
	 * 
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext jobInfo) throws JobExecutionException {
		loginToSakai("admin");
 
 		String jobName = jobInfo.getJobDetail().getName(); 
 		String triggerName = jobInfo.getTrigger().getName();
 		Date requestedFire = jobInfo.getScheduledFireTime();
		Date actualfire = jobInfo.getFireTime();

		StringBuffer whoAmI = new StringBuffer("AutoSubmitAssessmentsJob $");
		whoAmI.append(" Job: ");
		whoAmI.append(jobName);
		whoAmI.append(" Trigger: ");
		whoAmI.append(triggerName);
		
		if (requestedFire != null) {
			whoAmI.append(" Fire scheduled: ");
			whoAmI.append(requestedFire.toString());
		}
		
		if (actualfire != null) {
			whoAmI.append(" Fire actual: ");
			whoAmI.append(actualfire.toString());
		}
		
		EventTrackingService.post(EventTrackingService.newEvent("sam.auto-submit.job", 
				safeEventLength(whoAmI.toString()), true));			

		LOG.info("Start Job: " + whoAmI.toString());
		
		GradingService gradingService = new GradingService();
		gradingService.autoSubmitAssessments();
		
		logoutFromSakai();
	}
	
	/**
	 * <p>Login to sakai and start a user session. This users is intended
	 * to be one of the 'hard wired' users; admin, postmaster, or synchrobot.</p>
	 * <p>( this list of users can be extended; add the user via UI, update
	 * the sakai_users table so their EID matches ID, add them to the
	 * admin realm, restart )</p>
	 * @param whoAs - who to log in as
	 */
	protected void loginToSakai(String whoAs) {
		
		serverName = ServerConfigurationService.getServerName();
		LOG.debug(" AutoSubmitAssessmentsJob Logging into Sakai on " + serverName + " as " + whoAs);

		UsageSession session = UsageSessionService.startSession(whoAs, serverName, "AutoSubmitAssessmentsJob");
        if (session == null)
        {
    		EventTrackingService.post(EventTrackingService.newEvent("sam.auto-submit.job.error", whoAs + " unable to log into " + serverName, true));
    		return;
        }
		
		Session sakaiSession = SessionManager.getCurrentSession();
		sakaiSession.setUserId(whoAs);
		sakaiSession.setUserEid(whoAs);

		// update the user's externally provided realm definitions
		AuthzGroupService.refreshUser(whoAs);

		// post the login events
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGIN, whoAs + " running " + serverName, true));

	}


	protected void logoutFromSakai() {
		String serverName = ServerConfigurationService.getServerName();
		LOG.debug(" AutoSubmitAssessmentsJob Logging out of Sakai on " + serverName);
		EventTrackingService.post(EventTrackingService.newEvent(UsageSessionService.EVENT_LOGOUT, null, true));
		UsageSessionService.logout(); // safe to logout? what if other jobs are running?
	}
	
	/**
	 * Sometimes when logging to the sakai_events table it's possible to be logging
	 * with a string you don't know the size of. (An exception message is a good
	 * example)
	 * 
	 * This method is supplied to keep the lengh of logged messages w/in the limits
	 * of the sakai_event.ref column size.
	 * 
	 * The sakai_event.ref column size is currently 256
	 * 
	 * @param target
	 * @return
	 */
	static final public String safeEventLength(final String target) 
	{
		return (target.length() > 255 ? target.substring(0, 255) : target);
	}	
}