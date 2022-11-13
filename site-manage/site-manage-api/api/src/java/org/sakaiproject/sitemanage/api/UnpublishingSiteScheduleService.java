package org.sakaiproject.sitemanage.api;

import java.time.Instant;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface UnpublishingSiteScheduleService extends ScheduledInvocationCommand {

    void scheduleUnpublishing(Instant when, String siteId);

    void removeScheduledUnpublish(String siteId);

    void execute(String siteId);
}
