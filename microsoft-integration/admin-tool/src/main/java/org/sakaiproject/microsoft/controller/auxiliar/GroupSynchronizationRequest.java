package org.sakaiproject.microsoft.controller.auxiliar;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GroupSynchronizationRequest {
	private String selectedGroupId = null;
	private String selectedChannelId = null;
	private String newChannelName = "";
}
