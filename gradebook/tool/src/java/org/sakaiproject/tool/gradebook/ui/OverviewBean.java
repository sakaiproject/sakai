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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.GradableObject;

/**
 * Backing bean for the visible list of assignments in the gradebook.
 */
public class OverviewBean extends GradebookDependentBean implements Serializable  {
	private static final Log logger = LogFactory.getLog(OverviewBean.class);

    private static final Map columnSortMap;

	// View maintenance fields - serializable.
	private List gradableObjectRows;

	// Controller fields - transient.
	private transient List gradableObjects;

    static {
        columnSortMap = new HashMap();
        columnSortMap.put(Assignment.SORT_BY_NAME, Assignment.nameComparator);
        columnSortMap.put(Assignment.SORT_BY_DATE, Assignment.dateComparator);
        columnSortMap.put(Assignment.SORT_BY_MEAN, Assignment.meanComparator);
        columnSortMap.put(Assignment.SORT_BY_POINTS, Assignment.pointsComparator);
    }

    
	public class GradableObjectRow implements Serializable {
		private Long id;
		private transient String name;
		private transient Date dueDate;
		private transient Double mean;
		private transient Double points;
        private boolean externallyMaintained;
        private String externalAppName;
        private String externalUrl;
        private boolean courseGrade;

		public GradableObjectRow() {
		}
		public GradableObjectRow(GradableObject gradableObject) {
			id = gradableObject.getId();
			name = gradableObject.getName();
			dueDate = gradableObject.getDateForDisplay();
			Double formattedMean = gradableObject.getFormattedMean();
            if(formattedMean != null) {
                mean = new Double(formattedMean.doubleValue() / 100);
            }
			points = gradableObject.getPointsForDisplay();
			courseGrade = gradableObject.isCourseGrade();
            if(!gradableObject.isCourseGrade() && ((Assignment)gradableObject).isExternallyMaintained()) {
                externallyMaintained = true;
                externalAppName = ((Assignment)gradableObject).getExternalAppName();
                externalUrl = ((Assignment)gradableObject).getExternalInstructorLink();
            }
		}

		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Date getDueDate() {
			return dueDate;
		}
		public void setDueDate(Date dueDate) {
			this.dueDate = dueDate;
		}
		public Double getMean() {
			return mean;
		}
		public void setMean(Double mean) {
			this.mean = mean;
		}
		public Double getPoints() {
			return points;
		}
		public void setPoints(Double points) {
			this.points = points;
		}
		public boolean isCourseGrade() {
			return courseGrade;
		}
		public void setCourseGrade(boolean courseGrade) {
			this.courseGrade = courseGrade;
		}
		public String getExternalAppName() {
			return externalAppName;
		}
		public void setExternalAppName(String externalAppName) {
			this.externalAppName = externalAppName;
		}
		public String getExternalUrl() {
			return externalUrl;
		}
		public void setExternalUrl(String externalUrl) {
			this.externalUrl = externalUrl;
		}
		public boolean isExternallyMaintained() {
			return externallyMaintained;
		}
		public void setExternallyMaintained(boolean externallyMaintained) {
			this.externallyMaintained = externallyMaintained;
		}
	}

	public List getGradableObjectRows() {
		return gradableObjectRows;
	}
	public void setGradableObjectRows(List gradableObjectRows) {
		this.gradableObjectRows = gradableObjectRows;
	}

	protected void init() {
		// Clear view state.
		gradableObjectRows = new ArrayList();
        
		// Get the list of assignments for this gradebook, sorted as defined in the overview page.
        gradableObjects = getGradableObjectManager().getAssignmentsWithStats(getGradebookId(),
        		getAssignmentSortColumn(), isAssignmentSortAscending());

        
		// Always put the course grade last.
		gradableObjects.add(getGradableObjectManager().getCourseGradeWithStats(getGradebookId()));

		// Set up assignment rows.
		for (Iterator iter = gradableObjects.iterator(); iter.hasNext(); ) {
			gradableObjectRows.add(new GradableObjectRow((GradableObject)iter.next()));
		}
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

/**************************************************************************************************************************************************************************************************************************************************************
 * $Id$
 *************************************************************************************************************************************************************************************************************************************************************/
