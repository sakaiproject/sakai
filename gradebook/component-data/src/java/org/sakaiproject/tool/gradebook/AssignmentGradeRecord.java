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

package org.sakaiproject.tool.gradebook;

import java.util.Date;

/**
 * An AssignmentGradeRecord is a grade record that can be associated with an
 * Assignment.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AssignmentGradeRecord extends AbstractGradeRecord {
    protected String displayGrade; // Not persisted

    public AssignmentGradeRecord() {
        super();
    }

    /**
	 * @param assignment The assignment this grade record is associated with
     * @param studentId The student id for whom this grade record belongs
     * @param graderId The id of the user submitting this grade record
	 * @param grade The grade, or points earned
	 */
	public AssignmentGradeRecord(Assignment assignment, String studentId, String graderId, Double grade) {
        super();
        this.gradableObject = assignment;
        this.studentId = studentId;
        this.graderId = graderId;
        this.pointsEarned = grade;
        this.dateRecorded = new Date();
	}

    /**
     * Returns null if the points earned is null.  Otherwise, returns earned / points possible * 100.
     * 
     * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#getGradeAsPercentage()
     */
    public Double getGradeAsPercentage() {
        if (pointsEarned == null) {
            return null;
        }
        double earned = pointsEarned.doubleValue();
        double possible = ((Assignment)getGradableObject()).getPointsPossible().doubleValue();
        return new Double(earned / possible * 100);
    }

	/**
	 * @return Returns the displayGrade.
	 */
	public String getDisplayGrade() {
		return displayGrade;
	}
	/**
	 * @param displayGrade The displayGrade to set.
	 */
	public void setDisplayGrade(String displayGrade) {
		this.displayGrade = displayGrade;
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#isCourseGradeRecord()
	 */
	public boolean isCourseGradeRecord() {
		return false;
	}

    public Assignment getAssignment() {
    	return (Assignment)getGradableObject();   
    }
}



