package org.sakaiproject.webapi.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembershipScheduledRestBean {
	private String siteId;
	private String userId;
	private String role;
	private String timestamp;
}
