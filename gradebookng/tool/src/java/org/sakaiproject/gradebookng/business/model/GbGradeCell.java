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
	private String studentUuid;
	
	@Getter
	private long assignmentId;
	
	@Getter @Setter
	private Date lastUpdated;

	@Getter
	private String lastUpdatedBy;


	public GbGradeCell(String studentUuid, long assignmentId, String lastUpdatedBy){
		this.studentUuid = studentUuid;
		this.assignmentId = assignmentId;
		this.lastUpdated = new Date();
		this.lastUpdatedBy = lastUpdatedBy;
	}
	
}
