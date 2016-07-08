package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a cell in the imported spreadsheet
 */
@ToString
public class ImportedGradeItem implements Serializable {

	private static final long serialVersionUID = 1L;

	public ImportedGradeItem(final String gradeItemName) {
		this.gradeItemName = gradeItemName;
	}

	private final String gradeItemName;

	/**
	 * TODO: can these just be collapsied into a generic 'value' field?
	 */
	@Getter
	@Setter
	private String gradeItemComment;

	@Getter
	@Setter
	private String gradeItemScore;

}
