package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;

import javax.faces.event.ValueChangeEvent;

import org.sakaiproject.tool.gradebook.Assignment;

/**
 * Created to validate pointsPossible during bulk gradebook item
 * creation to catch non-numeric input into that field.
 * (NOTE: originally created to deal with 1 assignment at a time).
 * 
 * @author josephrodriguez
 */
public class BulkAssignmentDecoratedBean implements Serializable {
	private Assignment assignment;
	private String category;
	private String pointsPossible;
	private String unGraded = "normal";
    public String bulkNoPointsError;
    public String bulkNoTitleError;
    public Boolean saveThisItem;
    
    private static final String UN_GRADED_NORMAL = "normal";
    private static final String UN_GRADED_NO_GRADED = "ungraded";

	public BulkAssignmentDecoratedBean(Assignment assignment, String category) {
		this.assignment = assignment;
		this.category = category;
		bulkNoPointsError = "OK";
		bulkNoTitleError = "OK";
		saveThisItem = Boolean.FALSE;
	}
	
	public Assignment getAssignment() {
		return assignment;
	}
	public void setAssignment(Assignment assignment) {
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
	
	public String getUngraded()
	{
		return unGraded;
	}

	public void setUngraded(String unGraded)
	{
		this.unGraded = unGraded;
	}
	
	public String processUngradedSettingChange(ValueChangeEvent vce)
	{
		String value = (String) vce.getNewValue(); 
		if (value != null && value.equals(UN_GRADED_NO_GRADED))
		{
			unGraded = UN_GRADED_NO_GRADED;
			assignment.setUngraded(true);
			assignment.setCounted(false);
		}
		else
		{
			unGraded = UN_GRADED_NORMAL;
			assignment.setUngraded(false);
			assignment.setCounted(true);
		}

		return null;
	}

}
