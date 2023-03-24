package org.sakaiproject.sitemanage.api;

import java.time.Instant;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface JoinableSetReminderScheduleService extends ScheduledInvocationCommand {

    void scheduleJSetReminder(Instant dateTime, String dataPair);

    void removeScheduledJSetReminder(String dataPair);

    void execute(String dataPair);
}
