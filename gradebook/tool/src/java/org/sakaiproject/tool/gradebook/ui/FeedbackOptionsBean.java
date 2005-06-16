/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Provides support for the student feedback options page, which also controls
 * grade-to-percentage mappings for the gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class FeedbackOptionsBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(FeedbackOptionsBean.class);

	// View maintenance fields - serializable.

	/**
	 * This is the one page in which the user can change what's on the screen
	 * and have their current working inputs remembered without updating the
	 * database. In other words, this is currently our only "what if?" workflow.
	 * The following variable keeps bean initialization from overwriting stable
	 * input fields from the database.
	 */
	private boolean workInProgress;

	/** Holds "display assignment grades" checkbox's boolean value */
	private Boolean displayAssignmentGrades;

	/** Holds "display course grades" checkbox's boolean value */
	private Boolean displayCourseGrades;

	/** The currently selected grade mapping id, bound to the selectGradeType select box */
	private String selectedGradeMappingId;
	private List gradeMappingsSelectItems;

	/**
	 * According to the functional specification, "If the values are changed, then
	 * the instructor changes to a different scheme, then switches back, the GB
	 * remembers the instructors previously entered scheme."
	 * A comment by Oliver clarifies that this "remembering" doesn't involve
	 * persistent storage: Change Grade Type "is exclusively a page control and
	 * activating it does not save anything to the database."
	 * As a result, we need to carry the last user-entered values for all grade
	 * mappings, whether currently visible or not, across the request-thread.
	 * Only the currently visible mapping will actually be saved by the "Save"
	 * button, however.
	 */
	private Map gradeMappingRowsMap;	// Key = gradeMappingId, value = gradeMappingRows

	// Controller fields - transient.
	// (None needed at present.)

	public class GradeMappingRow implements Serializable {
		private String grade;
		private Double percentage;	// Minimum percentage needed for grade
		private boolean readOnly;	// The bottom grade always has a minimum of 0%

		public GradeMappingRow() {
		}
		public GradeMappingRow(String grade, Double percentage, boolean readOnly) {
			setGrade(grade);
			setPercentage(percentage);
			setReadOnly(readOnly);
		}

		public String getGrade() {
			return grade;
		}
		public void setGrade(String grade) {
			this.grade = grade;
		}
		public Double getPercentage() {
			return percentage;
		}
		public void setPercentage(Double percentage) {
			this.percentage = percentage;
		}
		public boolean isReadOnly() {
			return readOnly;
		}
		public void setReadOnly(boolean readOnly) {
			this.readOnly = readOnly;
		}
	}

	/**
	 * Initializes this backing bean.
	 */
	protected void init() {
		if (!workInProgress) {
			Gradebook gradebook = getGradebook();

			// Set the values for the checkboxed UI components
			displayAssignmentGrades = new Boolean(gradebook.isAssignmentsDisplayed());
			displayCourseGrades = new Boolean(gradebook.isCourseGradeDisplayed());

			// Set the grade mapping id (for the select box UI component) and the selected grade mapping
			selectedGradeMappingId = gradebook.getSelectedGradeMapping().getId().toString();
			if (logger.isDebugEnabled()) logger.debug("init: selectedGradeMappingId=" + selectedGradeMappingId);

			// Load the grade mappings, sorted by name.
			List gradeMappings = new ArrayList(gradebook.getGradeMappings());
			Collections.sort(gradeMappings);

			// Create the grade type drop-down menu, and initialize
			// the set of grade mapping tables.
			gradeMappingsSelectItems = new ArrayList(gradeMappings.size());
			gradeMappingRowsMap = new HashMap(gradeMappings.size());
			for (Iterator iter = gradeMappings.iterator(); iter.hasNext(); ) {
				GradeMapping gradeMapping = (GradeMapping)iter.next();
				gradeMappingsSelectItems.add(new SelectItem(gradeMapping.getId().toString(), gradeMapping.getName()));
				updateMappingRow(gradeMapping);
			}
		}

		// Set the view state.
		workInProgress = true;
	}

	/**
	 * Action listener to view a different grade type mapping.
	 * According to the specification, we do not update any changed values in the currently
	 * shown mapping, but we do remember them.
	 */
	public void changeGradeType(ActionEvent event) {
		// The selectedGradeMappingId was changed during the Update Model Values phase.
		// Not much else to do except re-render....
	}

	/**
	 * Action listener to reset the currently selected grade mapping to its default values.
	 * Other, not currently visible, changed unsaved grade mapping settings are left as they
	 * are.
	 */
	public void resetMappingValues(ActionEvent event) {
		Gradebook gradebook = getGradebook();
		GradeMapping gradeMapping = gradebook.getGradeMapping(Long.valueOf(selectedGradeMappingId));
		gradeMapping.setDefaultValues();
		updateMappingRow(gradeMapping);
	}

	private void updateMappingRow(GradeMapping gradeMapping) {
		List rows = new ArrayList();
		for (Iterator iter = gradeMapping.getGrades().iterator(); iter.hasNext(); ) {
			String grade = (String)iter.next();
			boolean readOnly = !(iter.hasNext());
			rows.add(new GradeMappingRow(grade, gradeMapping.getValue(grade), readOnly));
		}
		gradeMappingRowsMap.put(gradeMapping.getId().toString(), rows);
	}

	/**
	 * Updates the gradebook to reflect the currently selected grade type and mapping.
	 */
	public String save() {
		List mappingRows = (List)gradeMappingRowsMap.get(selectedGradeMappingId);
		if (!isMappingValid(mappingRows)) {
			return null;
		}

		Gradebook gradebook = getGradebook();
        String oldMappingName = gradebook.getSelectedGradeMapping().getName();
		gradebook.setAssignmentsDisplayed(displayAssignmentGrades.booleanValue());
		gradebook.setCourseGradeDisplayed(displayCourseGrades.booleanValue());

		GradeMapping gradeMapping = gradebook.getGradeMapping(Long.valueOf(selectedGradeMappingId));
		for (Iterator iter = mappingRows.iterator(); iter.hasNext(); ) {
			GradeMappingRow row = (GradeMappingRow)iter.next();
			gradeMapping.putValue(row.getGrade(), row.getPercentage());
		}
		gradebook.setSelectedGradeMapping(gradeMapping);

		try {
			getGradebookManager().updateGradebook(gradebook);
            FacesUtil.addRedirectSafeMessage(getLocalizedString("feedback_options_submit_success"));
        } catch (IllegalStateException ise) {
            FacesUtil.addErrorMessage(getLocalizedString("feedback_options_illegal_change", new String[] {oldMappingName}));
            return null;
		} catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("feedback_options_locking_failure"));
            return null;
		}

		return "overview";
	}

	private boolean isMappingValid(List mappingRows) {
		boolean valid = true;
		Double previousPercentage = null;
		for (Iterator iter = mappingRows.iterator(); iter.hasNext(); ) {
			GradeMappingRow row = (GradeMappingRow)iter.next();
			Double percentage = row.getPercentage();
			if (logger.isDebugEnabled()) logger.debug("checking percentage " + percentage + " for validity");

			if (percentage == null) {
				FacesUtil.addUniqueErrorMessage(getLocalizedString("feedback_options_require_all_values"));
				valid = false;
			} else if (percentage.doubleValue() < 0) {
				FacesUtil.addUniqueErrorMessage(getLocalizedString("feedback_options_require_positive"));
				valid = false;
			} else if ((previousPercentage != null) && (previousPercentage.doubleValue() < percentage.doubleValue())) {
				FacesUtil.addUniqueErrorMessage(getLocalizedString("feedback_options_require_descending_order"));
				valid = false;
			}
			previousPercentage = percentage;
		}
		return valid;
	}

	public String cancel() {
		// Just in case we change the navigation to stay on this page,
		// clear the work-in-progress indicator so that the user can
		// start fresh.
		workInProgress = false;

		return "overview";
	}

	public String getSelectedGradeMappingId() {
		if (logger.isDebugEnabled()) logger.debug("getSelectedGradeMappingId " + selectedGradeMappingId);
		return selectedGradeMappingId;
	}
	public void setSelectedGradeMappingId(String gradeMappingId) {
		if (logger.isDebugEnabled()) logger.debug("setSelectedGradeMappingId " + gradeMappingId);
		this.selectedGradeMappingId = gradeMappingId;
	}

	public Map getGradeMappingRowsMap() {

		return gradeMappingRowsMap;
	}
	public void setGradeMappingRowsMap(Map gradeMappingRowsMap) {
		this.gradeMappingRowsMap = gradeMappingRowsMap;
	}

	public List getGradeMappingsSelectItems() {
		return gradeMappingsSelectItems;
	}
	public void setGradeMappingsSelectItems(List gradeMappingsSelectItems) {
		this.gradeMappingsSelectItems = gradeMappingsSelectItems;
	}

	/**
	 * @return Returns the displayAssignmentGrades.
	 */
	public Boolean getDisplayAssignmentGrades() {
		return displayAssignmentGrades;
	}
	/**
	 * @param displayAssignmentGrades The displayAssignmentGrades to set.
	 */
	public void setDisplayAssignmentGrades(Boolean displayAssignmentGrades) {
		this.displayAssignmentGrades = displayAssignmentGrades;
	}
	/**
	 * @return Returns the displayCourseGrades.
	 */
	public Boolean getDisplayCourseGrades() {
		return displayCourseGrades;
	}
	/**
	 * @param displayCourseGrades The displayCourseGrades to set.
	 */
	public void setDisplayCourseGrades(Boolean displayCourseGrades) {
		this.displayCourseGrades = displayCourseGrades;
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
