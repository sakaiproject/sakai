package org.sakaiproject.assignment.api.reminder;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface AssignmentDueReminderService extends ScheduledInvocationCommand {

    void scheduleDueDateReminder(String assignmentId);

    void removeScheduledReminder(String assignmentId);
}
