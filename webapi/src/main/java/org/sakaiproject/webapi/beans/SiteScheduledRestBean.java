package org.sakaiproject.webapi.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SiteScheduledRestBean {
	private String id;
	private String title;
	private String createdOn;
	private String modifiedOn;
	private String softlyDeletedDate;
}
