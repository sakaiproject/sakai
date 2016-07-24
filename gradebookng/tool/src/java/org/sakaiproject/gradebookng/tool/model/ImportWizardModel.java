package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.service.gradebook.shared.Assignment;

import lombok.Getter;
import lombok.Setter;

/**
 * Model object used for the import wizard panels
 */
public class ImportWizardModel implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * List of items that have been uploaded
	 */
	@Getter
	@Setter
	private List<ProcessedGradeItem> processedGradeItems;

	/**
	 * List of items that have been selected to import
	 */
	@Getter
	@Setter
	private List<ProcessedGradeItem> selectedGradeItems;

	/**
	 * Which step is the new gb item creation currently on
	 */
	@Getter
	@Setter
	private int step;

	/**
	 * How many total steps are in the new gb item creation portion of the wizard
	 */
	@Getter
	@Setter
	private int totalSteps;

	/**
	 * List of items from the spreadsheet
	 */
	@Getter
	@Setter
	private List<ProcessedGradeItem> itemsToCreate;

	/**
	 * List of items from the spreadsheet
	 */
	@Getter
	@Setter
	private List<ProcessedGradeItem> itemsToUpdate;

	/**
	 * The list of assignments to be created once the form has been filled in
	 */
	@Getter
	@Setter
	private List<Assignment> assignmentsToCreate = new ArrayList<Assignment>();

}
