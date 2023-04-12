package org.sakaiproject.microsoft.api.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeetingRecordingData {
	private String id;
	private String name;
	private String url;
	private String organizerId;
}
