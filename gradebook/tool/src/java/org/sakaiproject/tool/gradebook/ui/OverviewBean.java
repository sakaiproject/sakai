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
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.business.FacadeUtils;

/**
 * Backing bean for the visible list of assignments in the gradebook.
 */
public class OverviewBean extends GradebookDependentBean implements Serializable  {
	private static final Log logger = LogFactory.getLog(OverviewBean.class);

    private static final Map columnSortMap;

	private List gradableObjects;

    static {
        columnSortMap = new HashMap();
        columnSortMap.put(Assignment.SORT_BY_NAME, Assignment.nameComparator);
        columnSortMap.put(Assignment.SORT_BY_DATE, Assignment.dateComparator);
        columnSortMap.put(Assignment.SORT_BY_MEAN, Assignment.meanComparator);
        columnSortMap.put(Assignment.SORT_BY_POINTS, Assignment.pointsComparator);
    }

	public List getGradableObjects() {
		return gradableObjects;
	}
	public void setGradableObjects(List gradableObjects) {
		this.gradableObjects = gradableObjects;
	}

	protected void init() {
		Set enrollmentUids = FacadeUtils.getStudentUids(getCourseManagementService().getEnrollments(getGradebookUid()));

		// Get the list of assignments for this gradebook, sorted as defined in the overview page.
        gradableObjects = getGradeManager().getAssignmentsWithStats(getGradebookId(), enrollmentUids,
        		getAssignmentSortColumn(), isAssignmentSortAscending());

		// Always put the course grade last.
		gradableObjects.add(getGradeManager().getCourseGradeWithStats(getGradebookId(), enrollmentUids));
	}

    // Delegated sort methods
	public String getAssignmentSortColumn() {
        return getPreferencesBean().getAssignmentSortColumn();
	}
	public void setAssignmentSortColumn(String assignmentSortColumn) {
        getPreferencesBean().setAssignmentSortColumn(assignmentSortColumn);
    }
    public boolean isAssignmentSortAscending() {
        return getPreferencesBean().isAssignmentSortAscending();
	}
    public void setAssignmentSortAscending(boolean sortAscending) {
        getPreferencesBean().setAssignmentSortAscending(sortAscending);
    }

	/**
     * @return The comma-separated list of css styles to use in displaying the rows
     */
    public String getRowStyles() {
        StringBuffer sb = new StringBuffer();
        for(Iterator iter = gradableObjects.iterator(); iter.hasNext();) {
            GradableObject go = (GradableObject)iter.next();
            if(go.isCourseGrade()) {
               sb.append("internal");
               break;
            } else {
                Assignment asn = (Assignment)go;
                if(asn.isExternallyMaintained()) {
                    sb.append("external,");
                } else {
                    sb.append("internal,");
                }
            }
        }
        return sb.toString();
    }
}



