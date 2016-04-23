package org.sakaiproject.component.app.scheduler;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.ScheduledInvocationRunner;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;

public class ScheduledInvocationManagerImpl implements ScheduledInvocationManager {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledInvocationManagerImpl.class);

	private static final String SCHEDULED_INVOCATION_RUNNER_DEFAULT_INTERVAL_PROPERTY = "jobscheduler.invocation.interval";

	private static final int SCHEDULED_INVOCATION_RUNNER_DEFAULT_INTERVAL = 120; //default: time in seconds, run every 2 mins

	
	/** Dependency: IdManager */
	protected IdManager m_idManager = null;

	public void setIdManager(IdManager service) {
		m_idManager = service;
	}

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	public void setSqlService(SqlService service) {
		m_sqlService = service;
	}
	

	/** Dependency: SchedulerManager */
	protected SchedulerManager m_schedulerManager = null;

	public void setSchedulerManager(SchedulerManager service) {
		m_schedulerManager = service;
	}
	
	
	/** Dependency: ServerConfigurationService */
	protected ServerConfigurationService m_serverConfigurationService = null;

	public void setServerConfigurationService(ServerConfigurationService service) {
		m_serverConfigurationService = service;
	}
	

	/** Configuration: Should we autocreate our database tables */
	protected boolean autoDdl;

	public void setAutoDdl(boolean autoDdl) {
		this.autoDdl = autoDdl;
	}
	
	

	public void init() {
		LOG.info("init()");
		if (autoDdl) {
			m_sqlService.ddl(getClass().getClassLoader(), "delayed");
			// Create the additional indexes as hibernate currently fails todo this.
			// Once hibernate is updated to 3.6 it should be able to manage in it's own.
			m_sqlService.ddl(getClass().getClassLoader(), "indexes2");
		}
	      try {
	          registerScheduledInvocationRunner();
	       } catch (SchedulerException e) {
	          LOG.error("failed to schedule ScheduledInvocationRunner job", e);
	       }
	    }

   public void destroy() {
      LOG.info("destroy()");
   }

   protected void registerScheduledInvocationRunner() throws SchedulerException {
	   
	   //trigger will not start immediately, wait until interval has passed before 1st run
	   long startTime = System.currentTimeMillis() + getScheduledInvocationRunnerInterval();
	   
       JobDetail detail = JobBuilder.newJob(ScheduledInvocationRunner.class)
               .withIdentity("org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner", "org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl")
               .build();
               
       Trigger trigger = TriggerBuilder.newTrigger()
               .withIdentity("org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner", "org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl")
               .startAt(new Date(startTime))
               .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                       .repeatForever()
                       .withIntervalInSeconds(getScheduledInvocationRunnerInterval()))
               .build();
               
       Scheduler scheduler = m_schedulerManager.getScheduler();
       // This removes the jobs if there are no trigger left on it.
       scheduler.unscheduleJob(trigger.getKey());
       scheduler.scheduleJob(detail, trigger);
    }
   
   protected int getScheduledInvocationRunnerInterval() {
	   return m_serverConfigurationService.getInt(SCHEDULED_INVOCATION_RUNNER_DEFAULT_INTERVAL_PROPERTY, SCHEDULED_INVOCATION_RUNNER_DEFAULT_INTERVAL);
   }
   
	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#createDelayedInvocation(org.sakaiproject.time.api.Time, java.lang.String, java.lang.String)
	 */
	public String createDelayedInvocation(Time time, String componentId, String opaqueContext) {

		String uuid = m_idManager.createUuid();

		LOG.debug("Creating new Delayed Invocation: " + uuid);
		String sql = "INSERT INTO SCHEDULER_DELAYED_INVOCATION VALUES(?,?,?,?)";

		Object[] fields = new Object[4];

		fields[0] = uuid;
		fields[1] = time;
		fields[2] = componentId;
		fields[3] = opaqueContext;

		LOG.debug("SQL: " + sql);
		if (m_sqlService.dbWrite(sql, fields)) {
			LOG.info("Created new Delayed Invocation: uuid=" + uuid);
			return uuid;
		} else {
			LOG.error("Failed to create new Delayed Invocation: componentId=" + componentId + 
					", opaqueContext=" + opaqueContext);
			return null;
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String)
	 */
	public void deleteDelayedInvocation(String uuid) {

		LOG.debug("Removing Delayed Invocation: " + uuid);
		String sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION WHERE INVOCATION_ID = ?";

		Object[] fields = new Object[1];
		fields[0] = uuid;

		LOG.debug("SQL: " + sql);
		m_sqlService.dbWrite(sql, fields);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String, java.lang.String)
	 */
	public void deleteDelayedInvocation(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		//String sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION WHERE COMPONENT = ? AND CONTEXT = ?";
		String sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION";

		Object[] fields = new Object[0];
		if (componentId.length() > 0 && opaqueContext.length() > 0) {
			// both non-blank
			sql += " WHERE COMPONENT = ? AND CONTEXT = ?";
			fields = new Object[2];
			fields[0] = componentId;
			fields[1] = opaqueContext;
		} else if (componentId.length() > 0) {
			// context blank
			sql += " WHERE COMPONENT = ?";
			fields = new Object[1];
			fields[0] = componentId;
		} else if (opaqueContext.length() > 0) {
			// component blank
			sql += " WHERE CONTEXT = ?";
			fields = new Object[1];
			fields[0] = opaqueContext;
		} else {
			// both blank
		}

		LOG.debug("SQL: " + sql);
		if ( m_sqlService.dbWrite(sql, fields) ) {
			LOG.info("Removed all scheduled invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		} else {
			LOG.error("Failure while attempting to remove invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#findDelayedInvocations(java.lang.String, java.lang.String)
	 */
	public DelayedInvocation[] findDelayedInvocations(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		//String sql = "SELECT * FROM SCHEDULER_DELAYED_INVOCATION WHERE COMPONENT = ? AND CONTEXT = ?";
		String sql = "SELECT * FROM SCHEDULER_DELAYED_INVOCATION";

		Object[] fields = new Object[0];
		if (componentId.length() > 0 && opaqueContext.length() > 0) {
			// both non-blank
			sql += " WHERE COMPONENT = ? AND CONTEXT = ?";
			fields = new Object[2];
			fields[0] = componentId;
			fields[1] = opaqueContext;
		} else if (componentId.length() > 0) {
			// context blank
			sql += " WHERE COMPONENT = ?";
			fields = new Object[1];
			fields[0] = componentId;
		} else if (opaqueContext.length() > 0) {
			// component blank
			sql += " WHERE CONTEXT = ?";
			fields = new Object[1];
			fields[0] = opaqueContext;
		} else {
			// both blank
		}

		List invocations = m_sqlService.dbRead(sql, fields, new DelayedInvocationReader());
		return (DelayedInvocation[]) invocations.toArray( new DelayedInvocation[] {} );
	}

}
