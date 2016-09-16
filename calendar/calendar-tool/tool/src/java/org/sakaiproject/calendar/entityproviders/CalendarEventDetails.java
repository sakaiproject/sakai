package org.sakaiproject.calendar.entityproviders;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.time.api.Time;

@Data
public class CalendarEventDetails extends CalendarEventSummary {

	private String location;
	private Time lastTime;
	private String description;
	private String descriptionFormatted;
	private String eventReference;
	protected List<AttachmentSummary> attachments;

	public CalendarEventDetails(CalendarEvent event) {
		super(event);

		location = event.getLocation();
		lastTime = event.getRange().lastTime();
		description = event.getDescription();
		descriptionFormatted = event.getDescriptionFormatted();
		eventReference = event.getReference();

		attachments = new ArrayList<AttachmentSummary>();
		//for externally subscribed calendar events attachments can be null
		if(event.getAttachments() != null){
			for (Reference r : event.getAttachments()) {
				attachments.add(new AttachmentSummary(r));
			}
		}
	}

	@Data
	public static class AttachmentSummary implements
			Comparable<AttachmentSummary> {
		private String url;

		public AttachmentSummary(Reference r) {
			url = r.getUrl();
		}

		public int compareTo(AttachmentSummary other) {
			return url.compareTo(other.url);
		}
	}

}
