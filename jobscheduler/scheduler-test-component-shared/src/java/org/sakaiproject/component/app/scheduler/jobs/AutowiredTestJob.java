package org.sakaiproject.component.app.scheduler.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.id.api.IdManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This is to test autowiring of Jobs.
 */
public class AutowiredTestJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(AutowiredTestJob.class);

    @Inject
    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    private IdManager idManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (idManager != null) {
            logger.info("Autowiring worked, generated ID {}", idManager.createUuid());
        } else {
            logger.info("IdManager was not autowired :-(");
        }
    }
}
