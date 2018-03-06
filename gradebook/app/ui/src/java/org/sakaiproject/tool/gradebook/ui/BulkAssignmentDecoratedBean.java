/**
 * Copyright (c) 2003-2011 The Apereo Foundation
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
package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import org.sakaiproject.tool.gradebook.GradebookAssignment;

/**
 * Created to validate pointsPossible during bulk gradebook item
 * creation to catch non-numeric input into that field.
 * (NOTE: originally created to deal with 1 assignment at a time).
 * 
 * @author josephrodriguez
 */
public class BulkAssignmentDecoratedBean implements Serializable {
	private GradebookAssignment assignment;
	private String category;
	private String pointsPossible;
    public String bulkNoPointsError;
    public String bulkNoTitleError;
    public Boolean saveThisItem;
    public String selectedGradeEntryValue;

	public BulkAssignmentDecoratedBean(GradebookAssignment assignment, String category) {
		this.assignment = assignment;
		this.category = category;
		bulkNoPointsError = "OK";
		bulkNoTitleError = "OK";
		saveThisItem = Boolean.FALSE;
	}
	
	public GradebookAssignment getAssignment() {
		return assignment;
	}
	public void setAssignment(GradebookAssignment assignment) {
		this.assignment = assignment;
	}
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPointsPossible() {
		return pointsPossible;
	}
	public void setPointsPossible(String pointsPossible) {
		this.pointsPossible = pointsPossible;
	}

	public String getBulkNoPointsError() {
		return bulkNoPointsError;
	}

	public void setBulkNoPointsError(String bulkNoPointsError) {
		this.bulkNoPointsError = bulkNoPointsError;
	}

	public String getBulkNoTitleError() {
		return bulkNoTitleError;
	}

	public void setBulkNoTitleError(String bulkNoTitleError) {
		this.bulkNoTitleError = bulkNoTitleError;
	}

	public String getSaveThisItem() {
		return saveThisItem.toString();
	}

	public void setSaveThisItem(String saveThisItem) {
		this.saveThisItem = new Boolean(saveThisItem);
	}
	public boolean getBlnSaveThisItem() {
		return saveThisItem.booleanValue();
	}
	
	public String getSelectedGradeEntryValue() {
		return selectedGradeEntryValue;
	}

	public void setSelectedGradeEntryValue(String selectedGradeEntryValue) {
		this.selectedGradeEntryValue = selectedGradeEntryValue;
	}

}
