package org.sakaiproject.component.app.scheduler;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.listeners.TriggerListenerSupport;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.component.app.scheduler.jobs.ScheduledInvocationJob;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

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

	// The Quartz group name that contains all our Jobs and Triggers.
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

	private ContextMappingDAO dao;

	public void setDao(ContextMappingDAO dao) {
		this.dao = dao;
	}

	protected TriggerListener triggerListener;

	public void init() throws SchedulerException {
		LOG.info("init()");
		triggerListener = new ContextTriggerListener("ContextTriggerListener");
		ListenerManager listenerManager = schedulerFactory.getScheduler().getListenerManager();
		// Just filter on our group.
		listenerManager.addTriggerListener(triggerListener, GroupMatcher.triggerGroupEquals(GROUP_NAME));
	}

	public void destroy() throws SchedulerException {
		LOG.info("destroy()");
		ListenerManager listenerManager = schedulerFactory.getScheduler().getListenerManager();
		listenerManager.removeTriggerListener(triggerListener.getName());
	}

	@Override
	@Transactional(propagation = REQUIRES_NEW)
	public String createDelayedInvocation(Time  time, String componentId, String opaqueContext) {
		Instant instant = Instant.ofEpochMilli(time.getTime());
		return createDelayedInvocation(instant, componentId, opaqueContext);
	}

	@Override
	@Transactional(propagation = REQUIRES_NEW)
	public String createDelayedInvocation(Instant instant, String componentId, String opaqueContext) {
		String uuid = m_idManager.createUuid();
		createDelayedInvocation(instant, componentId, opaqueContext, uuid);
		return uuid;
	}

	/**
	 * Creates a new delated invocation. This exists so that the migration code can create a new delayed invocation
	 * and specify the UUID that should be used.
	 * @see org.sakaiproject.component.app.scheduler.jobs.SchedulerMigrationJob
	 */
	@Transactional(propagation = REQUIRES_NEW)
	public void createDelayedInvocation(Instant instant, String componentId, String opaqueContext, String uuid) {
		String oldUuid = dao.get(componentId, opaqueContext);
		// Delete the existing one.
		if (oldUuid != null) {
			deleteDelayedInvocation(componentId, opaqueContext);
		}
		dao.add(uuid, componentId, opaqueContext);
		try {
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
					.startAt(Date.from(instant))
					.forJob(key)
					.usingJobData(CONTEXT_ID, opaqueContext)
					.build();
			scheduler.scheduleJob(trigger);
			// This is so that we can do fast lookups.
			LOG.info("Created new Delayed Invocation: uuid=" + uuid);
		} catch (SchedulerException se) {
			dao.remove(uuid);
			LOG.error("Failed to create new Delayed Invocation: componentId=" + componentId +
					", opaqueContext=" + opaqueContext, se);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String)
	 */
	@Transactional(propagation = REQUIRES_NEW)
	public void deleteDelayedInvocation(String uuid) {

		LOG.debug("Removing Delayed Invocation: " + uuid);
		try {
			TriggerKey key = new TriggerKey(uuid, GROUP_NAME);
			schedulerFactory.getScheduler().unscheduleJob(key);
			dao.remove(uuid);
		} catch (SchedulerException e) {
			LOG.error("Failed to remove Delayed Invocation: uuid="+ uuid, e);
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String, java.lang.String)
	 */
	@Transactional(propagation = REQUIRES_NEW)
	public void deleteDelayedInvocation(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		Collection<String> uuids = dao.find(componentId, opaqueContext);
		for (String uuid: uuids) {
			deleteDelayedInvocation(uuid);
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#findDelayedInvocations(java.lang.String, java.lang.String)
	 */
	@Transactional(propagation = REQUIRES_NEW)
	public DelayedInvocation[] findDelayedInvocations(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		Collection<String> uuids = dao.find(componentId, opaqueContext);
		List<DelayedInvocation> invocations = new ArrayList<>();
		for (String uuid: uuids) {
			TriggerKey key = new TriggerKey(uuid, GROUP_NAME);
			try {
				Trigger trigger = schedulerFactory.getScheduler().getTrigger(key);
				if (trigger == null) {
					LOG.error("Failed to trigger with key: {}", key);
				} else {
					invocations.add(new DelayedInvocation(trigger.getKey().getName(), trigger.getNextFireTime(), key.getName(), opaqueContext));
				}
			} catch (SchedulerException e) {
				LOG.warn("Problem finding delayed invocations.", e);
				return null;
			}
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
					LOG.warn("One of our triggers ({}) didn't have a context ID", trigger.getKey());
				} else {
					dao.remove(trigger.getJobKey().getName(), contextId);
				}
			}
		}
	}
}
