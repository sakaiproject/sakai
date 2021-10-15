package org.sakaiproject.sitemanage.api;

import java.time.Instant;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface PublishingSiteScheduleService extends ScheduledInvocationCommand {

    void schedulePublishing(Instant when, String siteId);

    void removeScheduledPublish(String siteId);

    void execute(String siteId);
}
