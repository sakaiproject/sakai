package org.sakaiproject.calendar.entityproviders;

import lombok.Data;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.time.api.Time;

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
	private RecurrenceRule recurrenceRule;

	public CalendarEventSummary() {
	}

	public CalendarEventSummary(CalendarEvent event) {
		reference = event.getCalendarReference();
		siteName = event.getSiteName();
		eventId = event.getId();
		title = event.getDisplayName();
		type = event.getType();
		creator = event.getCreator();
		firstTime = event.getRange().firstTime();
		duration = event.getRange().duration();
		recurrenceRule = event.getRecurrenceRule();
	}

}
