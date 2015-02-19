package org.sakaiproject.component.app.scheduler.jobs.logmessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionException;
import org.sakaiproject.component.app.scheduler.jobs.AbstractConfigurableJob;

/**
 * This is a simple Job that allows a message to be logged.
 * This is useful for testing and time stamping the log files.
 */
public class LogMessageJob extends AbstractConfigurableJob {

    @Override
    public void runJob() throws JobExecutionException {
        String level = getConfiguredProperty("level");
        String message = getConfiguredProperty("message");
        String logger = getConfiguredProperty("logger");
        Log log = LogFactory.getLog(logger);
        if ("trace".equalsIgnoreCase(level)) {
            log.trace(message);
        } else if ("debug".equalsIgnoreCase(level)) {
            log.debug(message);
        } else if ("info".equalsIgnoreCase(level)) {
            log.info(message);
        } else if ("warn".equalsIgnoreCase(level)) {
            log.warn(message);
        } else if ("error".equalsIgnoreCase(level)) {
            log.error(message);
        } else if ("fatal".equalsIgnoreCase(level)) {
            log.fatal(message);
        }
    }
}
