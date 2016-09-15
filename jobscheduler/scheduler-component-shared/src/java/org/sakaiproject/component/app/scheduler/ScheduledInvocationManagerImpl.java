package org.sakaiproject.component.app.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.listeners.TriggerListenerSupport;
import org.sakaiproject.component.app.scheduler.jobs.ScheduledInvocationJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;

/**
 * componentId -> job key (name)
 * opaqueContent/contextId -> trigger key (name)
 *
 * jobs have groups and triggers have groups.
 * matching by quartz can be done on both of them and supports equals/startswith.
 * possiblity to have another table that does the opaqueID to UUID mapping?
 */
public class ScheduledInvocationManagerImpl implements ScheduledInvocationManager {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledInvocationManagerImpl.class);

	public static final String GROUP_NAME = "org.sakaiproject.component.app.scheduler.jobs.ScheduledInvocationJob";

	/**
	 * The key in the job data map that contains the opaque ID.
	 */
	public static final String CONTEXT_ID = "contextId";

	/** Dependency: IdManager */
	protected IdManager m_idManager = null;

	public void setIdManager(IdManager service) {
		m_idManager = service;
	}

	/** Dependency: SchedulerFactory */
	protected SchedulerFactory schedulerFactory = null;

	public void setSchedulerFactory(SchedulerFactory schedulerFactory) {
		this.schedulerFactory = schedulerFactory;
	}

	private SessionFactory sessionFactory;

	private ContextMapingDAO dao;

	public void setDao(ContextMapingDAO dao) {
		this.dao = dao;
	}

	protected TriggerListener triggerListener;

	public void init() {
		LOG.info("init()");
		triggerListener = new ContextTriggerListener("ContextTriggerListener");
//		schedulerFactory.getGlobalTriggerListeners().add(triggerListener);
	}

	public void destroy() {
		LOG.info("destroy()");
//		schedulerFactory.getGlobalTriggerListeners().remove(triggerListener);
}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#createDelayedInvocation(org.sakaiproject.time.api.Time, java.lang.String, java.lang.String)
	 */
	public String createDelayedInvocation(Time time, String componentId, String opaqueContext) {
		try {
			String uuid = m_idManager.createUuid();
			Scheduler scheduler = schedulerFactory.getScheduler();
			JobKey key = new JobKey(componentId, GROUP_NAME);
			JobDetail detail = scheduler.getJobDetail(key);
			if (detail == null) {
				try {
					detail = JobBuilder.newJob(ScheduledInvocationJob.class)
							.withIdentity(key)
							.storeDurably()
							.build();
					scheduler.addJob(detail, false);
				} catch (ObjectAlreadyExistsException se) {
					// We can ignore this one as it means the job is already present. This should only happen
					// due concurrent inserting of the job
					LOG.debug("Failed to add job {} as it already exists ", key, se);
				}
			}
			// Non-repeating trigger.
			Trigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(uuid, GROUP_NAME)
					.startAt(new Date(time.getTime()))
					.forJob(key)
					.usingJobData(CONTEXT_ID, opaqueContext)
					.build();
			scheduler.scheduleJob(trigger);
			LOG.info("Created new Delayed Invocation: uuid=" + uuid);
			return uuid;
		} catch (SchedulerException se) {
			LOG.error("Failed to create new Delayed Invocation: componentId=" + componentId +
					", opaqueContext=" + opaqueContext, se);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String)
	 */
	public void deleteDelayedInvocation(String uuid) {

		LOG.debug("Removing Delayed Invocation: " + uuid);
		try {
			TriggerKey key = new TriggerKey(uuid, GROUP_NAME);
			schedulerFactory.getScheduler().unscheduleJob(key);
		} catch (SchedulerException e) {
			LOG.error("Failed to remove Delayed Invocation: uuid="+ uuid, e);
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String, java.lang.String)
	 */
	public void deleteDelayedInvocation(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		try {
			Scheduler scheduler = schedulerFactory.getScheduler();
			Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(GROUP_NAME));
			for (JobKey jobKey : jobKeys) {
				if (componentId.length() > 0 && !(jobKey.getName().equals(componentId))) {
					// If we're filtering by component Id and it doesn't match skip.
					continue;
				}
				JobDetail detail = scheduler.getJobDetail(jobKey);
				if (detail != null) {
					List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
					for (Trigger trigger: triggers) {
						String contextId = trigger.getJobDataMap().getString(CONTEXT_ID);
						if (opaqueContext.length() > 0 && !(opaqueContext.equals(contextId))) {
							// If we're filtering by opaqueContent and it doesn't match skip.
							continue;
						}
						// Unscehdule the trigger.
						deleteDelayedInvocation(trigger.getKey().getName());
					}
				}
			}
		} catch (SchedulerException se) {
			LOG.error("Failure while attempting to delete invocations matching: componentId={}, opaqueContext={}",
					opaqueContext, componentId, se);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#findDelayedInvocations(java.lang.String, java.lang.String)
	 */
	public DelayedInvocation[] findDelayedInvocations(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		List<DelayedInvocation> invocations = new ArrayList<>();
		try {
			Scheduler scheduler = schedulerFactory.getScheduler();
			Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(GROUP_NAME));
			for (JobKey jobKey : jobKeys) {
				if (componentId.length() > 0 && !(jobKey.getName().equals(componentId))) {
					// If we're filtering by component Id and it doesn't match skip.
					continue;
				}
				JobDetail detail = scheduler.getJobDetail(jobKey);
				if (detail != null) {
					List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
					for (Trigger trigger: triggers) {
						String contextId = trigger.getJobDataMap().getString(CONTEXT_ID);
						if (opaqueContext.length() > 0 && !(opaqueContext.equals(contextId))) {
							// If we're filtering by opaqueContent and it doesn't match skip.
							continue;
						}
						// Add this one to the list.
						invocations.add(new DelayedInvocation(trigger.getKey().getName(), trigger.getNextFireTime(), jobKey.getName(), contextId));
					}
				}
			}
		} catch (SchedulerException se) {
			LOG.error("Failure while attempting to find invocations matching: componentId={}, opaqueContext={}",
					opaqueContext, componentId, se);
		}
		return invocations.toArray(new DelayedInvocation[]{});
	}

	/**
	 * This is used to cleanup the aditional data after the trigger has fired.
	 */
	private class ContextTriggerListener extends TriggerListenerSupport {

		ContextTriggerListener(String name) {
			this.name = name;
		}

		private String name;

		@Override
		public String getName() {
			return name;
		}

		public void triggerComplete(
				Trigger trigger,
				JobExecutionContext context,
				Trigger.CompletedExecutionInstruction triggerInstructionCode) {
			// Check it's one of ours
			if (GROUP_NAME.equals(trigger.getKey().getGroup())) {
				String contextId = trigger.getJobDataMap().getString(CONTEXT_ID);
				if (contextId == null) {
					LOG.warn("Once of our triggers ({}) didn't have a context ID", trigger.getKey());
				} else {
					// TODO Remove context ID to trigger mapping.
				}
			}
		}
	}
}
