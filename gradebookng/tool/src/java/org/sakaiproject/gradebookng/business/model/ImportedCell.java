package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a cell in the imported spreadsheet
 */
@ToString
public class ImportedCell implements Serializable {

	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	private String score;
	
	@Getter
	@Setter
	private String comment;

	public ImportedCell() {
	}
	

}
