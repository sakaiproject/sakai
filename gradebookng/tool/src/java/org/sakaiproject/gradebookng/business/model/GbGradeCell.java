package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * Tracks the coordinates of the cell (student and assignment) and the last edit on this cell
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GbGradeCell implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	private final String studentUuid;

	@Getter
	private final long assignmentId;

	@Getter
	@Setter
	private Date lastUpdated;

	@Getter
	private final String lastUpdatedBy;

	public GbGradeCell(final String studentUuid, final long assignmentId, final String lastUpdatedBy) {
		this.studentUuid = studentUuid;
		this.assignmentId = assignmentId;
		this.lastUpdated = new Date();
		this.lastUpdatedBy = lastUpdatedBy;
	}

}
