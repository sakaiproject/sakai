package org.sakaiproject.webapi.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentScheduledRestBean {
	private String siteId;
	private String assessmentId;
	private String title;
	private String startDate;
	private String dueDate;
}
