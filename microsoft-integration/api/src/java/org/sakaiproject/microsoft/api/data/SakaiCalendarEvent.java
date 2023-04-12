package org.sakaiproject.microsoft.api.data;

import java.util.List;

import org.sakaiproject.site.api.Group;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SakaiCalendarEvent {

	private String eventId;
	private String calendarReference;
	private String title;
	private String description;
	private String type;
	private long init;
	private long duration;
	private List<Group> groups;
	
}
