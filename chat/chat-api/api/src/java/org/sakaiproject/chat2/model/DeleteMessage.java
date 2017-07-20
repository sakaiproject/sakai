package org.sakaiproject.chat2.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeleteMessage{
	private String id;
	private String channelId;
}
