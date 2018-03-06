/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.gradebookng.business.importExport.UserIdentificationReport;
import org.sakaiproject.gradebookng.business.model.ImportedSpreadsheetWrapper;
import org.sakaiproject.gradebookng.business.model.ProcessedGradeItem;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * Model object used for the import wizard panels
 */
public class ImportWizardModel implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Representation of the spreadsheet
	 */
	@Getter
	@Setter
	private ImportedSpreadsheetWrapper spreadsheetWrapper;

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
	 * List of items from the spreadsheet that need to be created first
	 */
	@Getter
	@Setter
	private List<ProcessedGradeItem> itemsToCreate;

	/**
	 * List of items from the spreadsheet that just need their data updated.
	 */
	@Getter
	@Setter
	private List<ProcessedGradeItem> itemsToUpdate;

	/**
	 * List of items from the spreadsheet that need to have the assignment updated and their data updated
	 */
	@Getter
	@Setter
	private List<ProcessedGradeItem> itemsToModify;

	/**
	 * Maps items from the spreadsheet to the assignments that need to be created once the wizard has been completed
	 */
	@Getter
	@Setter
	private Map<ProcessedGradeItem, Assignment> assignmentsToCreate = new LinkedHashMap<>();

	/**
	 * The {@link UserIdentificationReport} generated during parsing of the raw import file
	 */
	@Getter
	@Setter
	private UserIdentificationReport userReport;
}
