/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

/**
 * Provides support for the student feedback options page, which also controls
 * grade-to-percentage mappings for the gradebook.
 */
@Slf4j
public class FeedbackOptionsBean extends GradebookDependentBean implements Serializable {
	// View maintenance fields - serializable.

	/**
	 * This is the one page in which the user can change what's on the screen
	 * and have their current working inputs remembered without updating the
	 * database. In other words, this is currently our only "what if?" workflow.
	 * The following variable keeps bean initialization from overwriting stable
	 * input fields from the database.
	 */
	private boolean workInProgress;

	/** Cache a copy of the gradebook object in the request thread, to keep track of all grade mapping changes */
	private Gradebook localGradebook;

	private Long selectedGradeMappingId;

	/** The list of select box items */
	private List gradeMappingsSelectItems;

	// View into row-specific data.
	private List gradeRows;
    
	private boolean isValidWithLetterGrade = true;

	public class GradeRow implements Serializable {
    		private GradeMapping gradeMapping;
    		private String grade;
    		private boolean editable;
    		public GradeRow() {
    		}
    		public GradeRow(GradeMapping gradeMapping, String grade, boolean editable) {
    			this.gradeMapping = gradeMapping;
    			this.grade = grade;
    			this.editable = editable;
    		}

    		public String getGrade() {
    			return grade;
    		}

    		public Double getMappingValue() {
    			return (Double)gradeMapping.getGradeMap().get(grade);
    		}
    		public void setMappingValue(Double value) {
    			gradeMapping.getGradeMap().put(grade, value);
    		}

    		public boolean isGradeEditable() {
    			return editable;
		}
	}

	public Gradebook getLocalGradebook() {
		return localGradebook;
	}
	/**
	 * Initializes this backing bean.
	 */
	protected void init() {
		if (!workInProgress) {
			localGradebook = getGradebookManager().getGradebookWithGradeMappings(getGradebookId());

			// Load the grade mappings, sorted by name.
			List gradeMappings = new ArrayList(localGradebook.getGradeMappings());
			Collections.sort(gradeMappings);

			// Create the grade type drop-down menu
			gradeMappingsSelectItems = new ArrayList(gradeMappings.size());
			for (Iterator iter = gradeMappings.iterator(); iter.hasNext(); ) {
				GradeMapping gradeMapping = (GradeMapping)iter.next();
				gradeMappingsSelectItems.add(new SelectItem(gradeMapping.getId(), gradeMapping.getName()));
			}
            // set the selected grade mapping
            GradeMapping selectedGradeMapping = localGradebook.getSelectedGradeMapping();
            selectedGradeMappingId = selectedGradeMapping.getId();
			initGradeRows();
		}

		// Set the view state.
		workInProgress = true;
	}

	private void initGradeRows() {
		// Set up UI table view.
		gradeRows = new ArrayList();
		GradeMapping selectedGradeMapping = localGradebook.getSelectedGradeMapping();
		for (Iterator iter = selectedGradeMapping.getGrades().iterator(); iter.hasNext(); ) {
			String grade = (String)iter.next();
			
			// Bottom grades (with a lower bound of 0%) and manual-only
			// grades (which have no percentage equivalent) are not
			// editable.
			Double d = selectedGradeMapping.getDefaultBottomPercents().get(grade);
			boolean editable = ((d != null) && (d.doubleValue() > 0.0));
			gradeRows.add(new GradeRow(selectedGradeMapping, grade, editable));
		}
	}

	public List getGradeRows() {
		return gradeRows;
	}
	public void setGradeRows(List gradeRows) {
		this.gradeRows = gradeRows;
	}

	/**
	 * Action listener to view a different grade type mapping.
	 * According to the specification, we do not update any changed values in the currently
	 * shown mapping, but we do remember them.
	 */
	public void changeGradeType(ActionEvent event) {
		isValidWithLetterGrade = true;
		for(Iterator iter = localGradebook.getGradeMappings().iterator(); iter.hasNext();) {
            GradeMapping mapping = (GradeMapping)iter.next();
            if(mapping.getId().equals(selectedGradeMappingId)) {
            		if(localGradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER && mapping.getGradingScale().getUid().equals("LetterGradeMapping"))
            		{
            			isValidWithLetterGrade = false;
            			return;
            		}
                localGradebook.setSelectedGradeMapping(mapping);
                initGradeRows();
            }
        }
	}

	/**
	 * Action listener to reset the currently selected grade mapping to its default values.
	 * Other, not currently visible, changed unsaved grade mapping settings are left as they
	 * are.
	 */
	public void resetMappingValues(ActionEvent event) {
		localGradebook.getSelectedGradeMapping().setDefaultValues();
	}

	/**
	 * Updates the gradebook to reflect the currently selected grade type and mapping.
	 */
	public String save() {
        if (!isMappingValid(localGradebook.getSelectedGradeMapping())) {
            return null;
        }
        if(!isConflictWithLetterGrade(localGradebook.getSelectedGradeMapping()))
        {
        	isValidWithLetterGrade = false;
        	return null;
        }
        else
        	isValidWithLetterGrade = true;

		try {
			getGradebookManager().updateGradebook(localGradebook);
            FacesUtil.addRedirectSafeMessage(getLocalizedString("feedback_options_submit_success"));
        } catch (IllegalStateException ise) {
            FacesUtil.addErrorMessage(getLocalizedString("feedback_options_illegal_change", new String[] {localGradebook.getSelectedGradeMapping().getName()}));
            return null;
		} catch (StaleObjectModificationException e) {
            log.error(e.getMessage());
            FacesUtil.addErrorMessage(getLocalizedString("feedback_options_locking_failure"));
            return null;
		}

		return "overview";
	}

	private boolean isMappingValid(GradeMapping gradeMapping) {
		boolean valid = true;
		Double previousPercentage = null;
		for (Iterator iter = gradeMapping.getGrades().iterator(); iter.hasNext(); ) {
			String grade = (String)iter.next();
			Double percentage = (Double)gradeMapping.getValue(grade);
			if (log.isDebugEnabled()) log.debug("checking percentage " + percentage + " for validity");

			// Grades that are percentage-based need to remain percentage-based,
			// be in descending order, and end with 0.
			// Manual-only grades (which aren't associated with a percentage) always
			// follow the lowest percentage-based grade, and must stay manual-only.
			boolean manualOnly = (gradeMapping.getDefaultBottomPercents().get(grade) == null);

			if (manualOnly) {
				if (percentage != null) {
					// This shouldn't happen, given the UI.
					if (log.isErrorEnabled()) log.error("User " + getUserUid() + " attempted to set manual-only grade '" + grade + "' to be worth " + percentage + " percent");
					percentage = null;
					valid = false;
				}
			} else {
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
			}
			previousPercentage = percentage;
		}
		return valid;
	}
	
	private boolean isConflictWithLetterGrade(GradeMapping gradeMapping) {
		boolean valid = true;
		if(localGradebook.getGrade_type() != GradebookService.GRADE_TYPE_LETTER)
		{
			return valid;
		}
		if((gradeMapping.getGradingScale() != null && (!gradeMapping.getGradingScale().getUid().equals("LetterGradeMapping") && !gradeMapping.getGradingScale().getUid().equals("LetterGradePlusMinusMapping")))
				|| (gradeMapping.getGradingScale() == null && (!gradeMapping.getName().equals("Letter Grades") && !gradeMapping.getName().equals("Letter Grades with +/-"))))
		{
			return valid;
		}
		Map defaultMapping = gradeMapping.getDefaultBottomPercents();
		for (Iterator iter = gradeMapping.getGrades().iterator(); iter.hasNext(); ) 
		{
			String grade = (String)iter.next();
			Double percentage = (Double)gradeMapping.getValue(grade);
			Double defautPercentage = (Double)defaultMapping.get(grade);
			if (log.isDebugEnabled()) log.debug("checking if percentage is being changed for letter grade type gradebook: " + percentage + " for validity");
			if (percentage == null) 
			{
				FacesUtil.addUniqueErrorMessage(getLocalizedString("feedback_options_require_all_values"));
				valid = false;
			}
			else
			{
				if(!percentage.equals(defautPercentage))
				{
					valid = false;
				}
			}
		}
		return valid;
	}

	public String cancel() {
		// Just in case we change the navigation to stay on this page,
		// clear the work-in-progress indicator so that the user can
		// start fresh.
		workInProgress = false;
		isValidWithLetterGrade = true;

		return "overview";
	}

	public List getGradeMappingsSelectItems() {
		return gradeMappingsSelectItems;
	}
	public void setGradeMappingsSelectItems(List gradeMappingsSelectItems) {
		this.gradeMappingsSelectItems = gradeMappingsSelectItems;
	}

	public Long getSelectedGradeMappingId() {
		return selectedGradeMappingId;
	}
	public void setSelectedGradeMappingId(Long selectedGradeMappingId) {
		this.selectedGradeMappingId = selectedGradeMappingId;
	}
	public boolean getIsValidWithLetterGrade()
	{
		return isValidWithLetterGrade;
	}
	public void setIsValidWithLetterGrade(boolean isValidWithLetterGrade)
	{
		this.isValidWithLetterGrade = isValidWithLetterGrade;
	}
}
