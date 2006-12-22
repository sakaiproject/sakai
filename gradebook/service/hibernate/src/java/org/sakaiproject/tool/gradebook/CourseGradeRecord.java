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

package org.sakaiproject.tool.gradebook;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

/**
 * A CourseGradeRecord is a grade record that can be associated with a CourseGrade.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseGradeRecord extends AbstractGradeRecord {
    private String enteredGrade;
    private Double autoCalculatedGrade;  // Not persisted
    private Double calculatedPointsEarned;	// Not persisted

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
	 */
	public CourseGradeRecord(CourseGrade courseGrade, String studentId) {
        this.gradableObject = courseGrade;
        this.studentId = studentId;
	}

    /**
     * Default no-arg constructor
     */
    public CourseGradeRecord() {
        super();
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
            return getCourseGrade().getGradebook().getSelectedGradeMapping().getValue(enteredGrade);
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

	public Double getPointsEarned() {
		return calculatedPointsEarned;
	}

    /**
	 * @return Returns the displayGrade.
	 */
	public String getDisplayGrade() {
        if(enteredGrade != null) {
            return enteredGrade;
        } else {
            return getCourseGrade().getGradebook().getSelectedGradeMapping().getGrade(autoCalculatedGrade);
        }
	}

	/**
	 * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#isCourseGradeRecord()
	 */
	public boolean isCourseGradeRecord() {
		return true;
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

	public void initNonpersistentFields(double totalPointsPossible, double totalPointsEarned) {
		Double percentageEarned;
		calculatedPointsEarned = totalPointsEarned;
		if (totalPointsPossible == 0.0) {
			percentageEarned = null;
		} else {
			percentageEarned = new Double(totalPointsEarned / totalPointsPossible * 100);
		}
		autoCalculatedGrade = percentageEarned;
	}
}
