package org.sakaiproject.component.app.scheduler.jobs;

import org.apache.commons.lang.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.sakaiproject.component.app.scheduler.ContextMappingDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.CONTEXT_ID;
import static org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.GROUP_NAME;

/**
 * This checks that the data stored in quartz and in the lookup tables is in sync.
 */
public class ValidateScheduledInvocations implements Job {

    private Logger log = LoggerFactory.getLogger(ValidateScheduledInvocations.class);

    @Inject
    private SchedulerFactory factory;

    @Inject
    private ContextMappingDAO dao;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            // Number that have and entry in quartz but don't in the DAO tables.
            int missingDao = 0;
            // Number missing a context ID.
            int missingContextId = 0;
            // The total number in the DAO table.
            int totalDao = dao.find(null, null).size();
            // The total number in quartz.
            int totalQuartz = 0;
            // Mapped correctly from quartz to DAO.
            int totalMapped = 0;

            Scheduler scheduler = factory.getScheduler();
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(GROUP_NAME));
            for (JobKey jobKey : jobKeys) {
                JobDetail detail = scheduler.getJobDetail(jobKey);
                if (detail != null) {
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    for (Trigger trigger : triggers) {
                        totalQuartz++;
                        String contextId = trigger.getJobDataMap().getString(CONTEXT_ID);
                        if (StringUtils.isBlank(contextId)) {
                            log.error("No contextId found on trigger "+ trigger.getKey().getName());
                            missingContextId++;
                            continue;
                        }
                        Collection<String> uuids = dao.find(jobKey.getName(), contextId);
                        if (uuids.isEmpty()) {
                            log.error("Failed to find DAO for componentId "+ jobKey.getName() +
                            " and contextId "+ contextId);
                            missingDao++;
                        } else {
                            for (String uuid : uuids) {
                                log.debug("Found DAO that matches componentId "+ jobKey.getName()+
                                        " and contextId "+ contextId+ " with uuid "+ uuid);
                                totalMapped++;
                            }
                        }
                    }
                }
            }
            log.info("Summary[ Quartz Jobs:{}, Scheduler Jobs:{}, Mapped Jobs:{}, Missing Scheduler:{}, Missing ID:{}",
                    totalQuartz, totalDao, totalMapped, missingDao, missingContextId);
        } catch (SchedulerException se) {
            log.error("Failed to validate scheduled invocations.", se);
        }
    }
}
