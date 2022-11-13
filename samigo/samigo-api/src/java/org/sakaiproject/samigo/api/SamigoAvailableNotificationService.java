package org.sakaiproject.samigo.api;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface SamigoAvailableNotificationService extends ScheduledInvocationCommand {

    void scheduleAssessmentAvailableNotification(String publishedId);

    void removeScheduledAssessmentNotification(String publishedId);
}
