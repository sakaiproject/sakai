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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.CourseManagement;
import org.sakaiproject.tool.gradebook.facades.Enrollment;

/**
 * @author josh
 *
 */
public class TestGradebookTool {
    // UI State
    Gradebook selectedGradebook;

    // Services
    private GradebookManager gradebookManager;
    private GradeManager gradeManager;
    private CourseManagement courseManagementService;

    /**
     * @return A List of all assignments in the currently selected gradebook
     */
    public List getAssignments() {
        List gradableObjects = gradeManager.getAssignmentsWithStats(selectedGradebook.getId());
        gradableObjects.add(gradeManager.getCourseGradeWithStats(selectedGradebook.getId()));
        return gradableObjects;
    }

    /**
     * @return A Set of all Users enrolled in the currently selected gradebook
     */
    public Set getStudents() {
        Set students = new HashSet();
        Set enrollments = courseManagementService.getEnrollments(selectedGradebook.getUid());
        for(Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
            students.add(((Enrollment)enrIter.next()).getUser());
        }
        return students;
    }

    //// JSF Action Events ////
    public void selectGradebook(ActionEvent ae) {
        selectedGradebook = (Gradebook)FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("gb");
    }

    //// Tool state getters and setters

    /**
     * @return Returns the selectedGradebook.
     */
    public Gradebook getSelectedGradebook() {
        return selectedGradebook;
    }
    /**
     * @param selectedGradebook The selectedGradebook to set.
     */
    public void setSelectedGradebook(Gradebook selectedGradebook) {
        this.selectedGradebook = selectedGradebook;
    }

    //// Service Dependencies ////

	/**
	 * @return Returns the gradeManager.
	 */
	public GradeManager getGradeManager() {
		return gradeManager;
	}
	/**
	 * @param gradeManager The gradeManager to set.
	 */
	public void setGradeManager(GradeManager gradeManager) {
		this.gradeManager = gradeManager;
	}
	/**
	 * @return Returns the courseManagementService.
	 */
	public CourseManagement getCourseManagementService() {
		return courseManagementService;
	}
	/**
	 * @param courseManagementService The courseManagementService to set.
	 */
	public void setCourseManagementService(
			CourseManagement courseManagementService) {
		this.courseManagementService = courseManagementService;
	}
	/**
	 * @return Returns the gradebookManager.
	 */
	public GradebookManager getGradebookManager() {
		return gradebookManager;
	}
	/**
	 * @param gradebookManager The gradebookManager to set.
	 */
	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}
}

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
