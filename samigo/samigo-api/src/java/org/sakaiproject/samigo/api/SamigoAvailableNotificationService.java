package org.sakaiproject.samigo.api;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface SamigoAvailableNotificationService extends ScheduledInvocationCommand {

    void rescheduleAssessmentAvailableNotification(String publishedId);

    void scheduleAssessmentAvailableNotification(String publishedId);

    void removeScheduledAssessmentNotification(String publishedId);
}
