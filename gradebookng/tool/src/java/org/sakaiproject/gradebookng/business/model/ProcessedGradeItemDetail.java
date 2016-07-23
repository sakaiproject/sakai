package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by chmaurer on 1/27/15.
 */

public class ProcessedGradeItemDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String studentEid;

	@Getter
	@Setter
	private String studentUuid;

	@Getter
	@Setter
	private String grade;

	@Getter
	@Setter
	private String comment;
}
