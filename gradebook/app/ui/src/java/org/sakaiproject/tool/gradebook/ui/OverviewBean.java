/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

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
		// Get the list of assignments for this gradebook, sorted as defined in the overview page.
        gradableObjects = getGradebookManager().getAssignmentsAndCourseGradeWithStats(getGradebookId(),
        		getAssignmentSortColumn(), isAssignmentSortAscending());
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

    public String getGradeOptionSummary() {
    	String gradeOptionSummary;
    	Gradebook gradebook = getGradebook();
    	String gradeMappingName = gradebook.getSelectedGradeMapping().getName();
    	if (gradebook.isAssignmentsDisplayed()) {
    		if (gradebook.isCourseGradeDisplayed()) {
    			gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_all_viewable", new String[] {gradeMappingName});
    		} else {
    			gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_assignments_viewable");
    		}
    	} else if (gradebook.isCourseGradeDisplayed()) {
    		gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_course_grade_viewable", new String[] {gradeMappingName});
    	} else {
    		gradeOptionSummary = FacesUtil.getLocalizedString("overview_grade_option_none_viewable");
    	}
    	return gradeOptionSummary;
    }

}
