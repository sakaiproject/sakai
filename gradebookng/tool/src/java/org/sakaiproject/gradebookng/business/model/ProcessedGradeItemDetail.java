package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Data;

/**
 * Created by chmaurer on 1/27/15.
 */
@Data
public class ProcessedGradeItemDetail implements Serializable {

	private String studentEid;
	private String studentUuid;
	private String grade;
	private String comment;
}
