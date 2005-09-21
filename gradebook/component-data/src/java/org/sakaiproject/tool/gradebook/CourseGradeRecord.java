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

package org.sakaiproject.tool.gradebook;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A CourseGradeRecord is a grade record that can be associated with a CourseGrade.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseGradeRecord extends AbstractGradeRecord {
    private String enteredGrade;
    private Double sortGrade; // Persisted for sorting purposes
    private Double autoCalculatedGrade;  // Not persisted
    private String displayGrade;  // Not persisted

    private static final Log logger = LogFactory.getLog(CourseGradeRecord.class);
    public static Comparator calcComparator;

    static {
        calcComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }
                return cgr1.getPointsEarned().compareTo(cgr2.getPointsEarned());
            }
        };
    }

    public static Comparator getOverrideComparator(final GradeMapping mapping) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }

                String enteredGrade1 = StringUtils.trimToEmpty(cgr1.getEnteredGrade());
                String enteredGrade2 = StringUtils.trimToEmpty(cgr2.getEnteredGrade());

                if(!mapping.getGrades().contains(enteredGrade1) && !mapping.getGrades().contains(enteredGrade2)) {
                    return 0; // neither of these are valid grades (they are probably empty strings)
                }
                if(!mapping.getGrades().contains(enteredGrade1)) {
                    return -1;
                }
                if(!mapping.getGrades().contains(enteredGrade2)) {
                    return 1;
                }
                return mapping.getValue(enteredGrade1).compareTo(mapping.getValue(enteredGrade2));
            }
        };

    }
    /**
     * The graderId and dateRecorded properties will be set explicitly by the
     * grade manager before the database is updated.
	 * @param courseGrade
	 * @param studentId
	 * @param grade
	 */
	public CourseGradeRecord(CourseGrade courseGrade, String studentId, String grade) {
        this.gradableObject = courseGrade;
        this.studentId = studentId;
        this.enteredGrade = grade;
        this.sortGrade = courseGrade.getGradebook().getSelectedGradeMapping().getValue(grade);
	}

    /**
     * Default no-arg constructor
     */
    public CourseGradeRecord() {
        super();
    }

    /**
     * Calculates the total points earned for a set of grade records.
     *
     * @param gradeRecords The collection of all grade records for a student
     */
    public void calculateTotalPointsEarned(Collection gradeRecords) {
        double total = 0;
        for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
            AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
            // Skip this if it doesn't have any points earned
            if(agr.getPointsEarned() == null) {
                continue;
            }
            total += agr.getPointsEarned().doubleValue();
        }
        pointsEarned = new Double(total);
    }


	/**
     * This method will fail unless this course grade was fetched "with statistics",
     * since it relies on having the total number of points possible available to
     * calculate the percentage.
     *
     * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#getGradeAsPercentage()
     */
    public Double getGradeAsPercentage() {
        if(enteredGrade == null) {
            return autoCalculatedGrade;
        } else {
            return getGradableObject().getGradebook().getSelectedGradeMapping().getValue(enteredGrade);
        }
    }

    /**
     * Convenience method to get the correctly cast CourseGrade that this
     * CourseGradeRecord references.
     *
     * @return CourseGrade referenced by this GradableObject
     */
    public CourseGrade getCourseGrade() {
    	return (CourseGrade)super.getGradableObject();
    }
    /**
	 * @return Returns the enteredGrade.
	 */
	public String getEnteredGrade() {
		return enteredGrade;
	}
	/**
	 * @param enteredGrade The enteredGrade to set.
	 */
	public void setEnteredGrade(String enteredGrade) {
		this.enteredGrade = enteredGrade;
	}
	/**
	 * @return Returns the autoCalculatedGrade.
	 */
	public Double getAutoCalculatedGrade() {
		return autoCalculatedGrade;
	}
	/**
	 * @param autoCalculatedGrade The autoCalculatedGrade to set.
	 */
	public void setAutoCalculatedGrade(Double autoCalculatedGrade) {
		this.autoCalculatedGrade = autoCalculatedGrade;
	}
	/**
	 * @return Returns the sortGrade.
	 */
	public Double getSortGrade() {
		return sortGrade;
	}
	/**
	 * @param sortGrade The sortGrade to set.
	 */
	public void setSortGrade(Double sortGrade) {
		this.sortGrade = sortGrade;
	}

    /**
	 * @return Returns the displayGrade.
	 */
	public String getDisplayGrade() {
        if(enteredGrade != null) {
            return enteredGrade;
        } else {
            CourseGrade cg = (CourseGrade)getGradableObject();
            return cg.getGradebook().getSelectedGradeMapping().getGrade(sortGrade);

        }
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#isCourseGradeRecord()
	 */
	public boolean isCourseGradeRecord() {
		return true;
	}


    /**
     * Calculates the grade as a percentage for a course grade record.
     */
    public Double calculatePercent(double totalPointsPossible) {
        Double pointsEarned = getPointsEarned();
        if(pointsEarned == null) {
            return null;
        } else {
            return new Double(pointsEarned.doubleValue() / totalPointsPossible * 100);
        }
    }

	/**
	 * For use by the Course Grade UI.
	 */
	public Double getNonNullAutoCalculatedGrade() {
		Double percent = getAutoCalculatedGrade();
		if (percent == null) {
			percent = new Double(0);
		}
		return percent;
	}
}
