package org.sakaiproject.api.app.messageforums.scheduler;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface PrivateMessageSchedulerService extends ScheduledInvocationCommand {

    void scheduleDueDateReminder(Long messageId);

    void removeScheduledReminder(Long messageId);
}
