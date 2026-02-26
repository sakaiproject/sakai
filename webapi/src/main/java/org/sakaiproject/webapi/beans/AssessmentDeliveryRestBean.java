package org.sakaiproject.webapi.beans;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentDeliveryRestBean {
	private String assessmentId;
	private String assessmentTitle;
	private boolean assessmentUpdatedNeedResubmit;
	private boolean assessmentUpdated;
	private Date dueDate;
	private boolean pastDue;
	private boolean timeRunning;
	private int timeLimit_hour;
	private int timeLimit_minute;
	private String alternativeDeliveryUrl;
}
