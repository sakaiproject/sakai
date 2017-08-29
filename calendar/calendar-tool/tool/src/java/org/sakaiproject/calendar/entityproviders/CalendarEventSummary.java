package org.sakaiproject.calendar.entityproviders;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.CalendarUtil;

import lombok.Data;

@Data
public class CalendarEventSummary {
	private String reference;
	private String siteName;
	private String eventId;
	private String title;
	private String type;
	private String creator;
	private Time firstTime;
	private long duration;
	private String description;
	private RecurrenceRule recurrenceRule;

	/**
	 * This field will only be set if the event is an assignment and can be used to reconstrut the deepLink
	 */
	private String assignmentId;

	/**
	 * Set externally after object creation, signals the site the event came from (not part of CalendarEvent)
	 */
	private String siteId;
	//icon used for specific eventType
	private String eventIcon;

	public CalendarEventSummary() {
	}

	public CalendarEventSummary(final CalendarEvent event) {
		this.reference = event.getCalendarReference();
		this.siteName = event.getSiteName();
		this.eventId = event.getId();
		this.title = event.getDisplayName();
		this.type = event.getType();
		this.creator = event.getCreator();
		this.firstTime = event.getRange().firstTime();
		this.duration = event.getRange().duration();
		this.recurrenceRule = event.getRecurrenceRule();
		this.duration = event.getRange().duration();
		this.recurrenceRule = event.getRecurrenceRule();
		this.description = event.getDescriptionFormatted();
		this.assignmentId = event.getField(CalendarUtil.NEW_ASSIGNMENT_DUEDATE_CALENDAR_ASSIGNMENT_ID);
	}

}
