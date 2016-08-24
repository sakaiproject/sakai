package org.sakaiproject.gradebookng.business.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Holds the data about a grade item that is imported from the spreadsheet as well as any edits that happen through the wizard
 * TODO refactor this, it is far too busy
 */

@ToString
public class ProcessedGradeItem implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Indicate the type of column
	 */
	@Getter
	@Setter
	private Type type;

	public enum Type {
		GB_ITEM,
		COMMENT
	}

	@Getter
	@Setter
	private String itemTitle;

	@Getter
	@Setter
	private Long itemId;

	@Getter
	@Setter
	private String itemPointValue;

	@Getter
	@Setter
	private ProcessedGradeItemStatus status = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UNKNOWN);

	@Getter
	@Setter
	private List<ProcessedGradeItemDetail> processedGradeItemDetails = new ArrayList<ProcessedGradeItemDetail>();

	@Getter
	@Setter
	private ProcessedGradeItemStatus commentStatus = new ProcessedGradeItemStatus(ProcessedGradeItemStatus.STATUS_UNKNOWN);

	/**
	 * Collection of fields from the edited assignment. These may differ to the imported fields and need to be used on the confirmation screen.
	 */

	@Getter
	@Setter
	private String assignmentTitle;

	@Getter
	@Setter
	private Double assignmentPoints;

}
