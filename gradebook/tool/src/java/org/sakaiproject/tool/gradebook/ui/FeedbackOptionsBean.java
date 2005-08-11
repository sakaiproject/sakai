/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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
import java.util.Iterator;
import java.util.List;

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

	/** Cache a copy of the gradebook object in the request thread, to keep track of all grade mapping changes */
    private Gradebook localGradebook;

    private Long selectedGradeMappingId;

    /** The list of select box items */
    private List gradeMappingsSelectItems;

    public Gradebook getLocalGradebook() {
        return localGradebook;
    }
	/**
	 * Initializes this backing bean.
	 */
	protected void init() {
		if (!workInProgress) {
			localGradebook = getGradebook();

			// Load the grade mappings, sorted by name.
			List gradeMappings = new ArrayList(localGradebook.getGradeMappings());
			Collections.sort(gradeMappings);

			// Create the grade type drop-down menu
			gradeMappingsSelectItems = new ArrayList(gradeMappings.size());
			for (Iterator iter = gradeMappings.iterator(); iter.hasNext(); ) {
				GradeMapping gradeMapping = (GradeMapping)iter.next();
				gradeMappingsSelectItems.add(new SelectItem(gradeMapping.getId().toString(), gradeMapping.getName()));
			}
            // set the selected grade mapping
            selectedGradeMappingId = localGradebook.getSelectedGradeMapping().getId();
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
        for(Iterator iter = localGradebook.getGradeMappings().iterator(); iter.hasNext();) {
            GradeMapping mapping = (GradeMapping)iter.next();
            if(mapping.getId().equals(selectedGradeMappingId)) {
                localGradebook.setSelectedGradeMapping(mapping);
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

//	private void updateMappingRow(GradeMapping gradeMapping) {
//		List rows = new ArrayList();
//		for (Iterator iter = gradeMapping.getGrades().iterator(); iter.hasNext(); ) {
//			String grade = (String)iter.next();
//			boolean readOnly = !(iter.hasNext());
//			rows.add(new GradeMappingRow(grade, gradeMapping.getValue(grade), readOnly));
//		}
//		gradeMappingRowsMap.put(gradeMapping.getId().toString(), rows);
//	}

	/**
	 * Updates the gradebook to reflect the currently selected grade type and mapping.
	 */
	public String save() {
        if (!isMappingValid(localGradebook.getSelectedGradeMapping())) {
            return null;
        }

		try {
			getGradebookManager().updateGradebook(localGradebook);
            FacesUtil.addRedirectSafeMessage(getLocalizedString("feedback_options_submit_success"));
        } catch (IllegalStateException ise) {
            FacesUtil.addErrorMessage(getLocalizedString("feedback_options_illegal_change", new String[] {localGradebook.getSelectedGradeMapping().getName()}));
            return null;
		} catch (StaleObjectModificationException e) {
            logger.error(e);
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
}



