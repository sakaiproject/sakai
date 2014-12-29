package org.sakaiproject.api.app.messageforums;

import java.util.Date;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public interface ForumScheduleNotification extends ScheduledInvocationCommand{

	public void scheduleAvailability(Area area);
	
	public void scheduleAvailability(DiscussionForum forum);
	
	public void scheduleAvailability(DiscussionTopic topic);
	
	public boolean makeAvailableHelper(boolean availabilityRestricted, Date openDate, Date closeDate);
	
}
