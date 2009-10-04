/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * A CourseGradeRecord is a grade record that can be associated with a CourseGrade.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseGradeRecord extends AbstractGradeRecord {
    private String enteredGrade;
    private Double autoCalculatedGrade;  // Not persisted
    private Double calculatedPointsEarned;	// Not persisted

    public static Comparator<CourseGradeRecord> calcComparator;

    static {
        calcComparator = new Comparator<CourseGradeRecord>() {
            public int compare(CourseGradeRecord cgr1, CourseGradeRecord cgr2) {
                if((cgr1 == null || cgr2 == null) || (cgr1.getGradeAsPercentage() == null && cgr2.getGradeAsPercentage() == null)) {
                    return 0;
                }
                if(cgr1 == null || cgr1.getGradeAsPercentage() == null) {
                    return -1;
                }
                if(cgr2 == null || cgr2.getGradeAsPercentage() == null) {
                    return 1;
                }
                //SAK-12017 - Commented out as getPointsEarned is no longer an accurate comparator
                // due to nulls no longer being calculated in to the Course Grade
                //return cgr1.getPointsEarned().compareTo(cgr2.getPointsEarned());
                //   Better to use getGradeAsPercentage
                return cgr1.getGradeAsPercentage().compareTo(cgr2.getGradeAsPercentage());
            }
        };
    }

    public static Comparator<CourseGradeRecord> getOverrideComparator(final GradeMapping mapping) {
        return new Comparator<CourseGradeRecord>() {
            public int compare(CourseGradeRecord cgr1, CourseGradeRecord cgr2) {

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
                
                // Grading scales are always defined in descending order.
                List<String> grades = mapping.getGradingScale().getGrades();
                int gradePos1 = -1;
                int gradePos2 = -1;
                for (int i = 0; (i < grades.size()) && ((gradePos1 == -1) || (gradePos2 == -1)); i++) {
                	String grade = grades.get(i);
                	if (grade.equals(enteredGrade1)) gradePos1 = i;
                	if (grade.equals(enteredGrade2)) gradePos2 = i;
                }
                return gradePos2 - gradePos1;
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
			percent = Double.valueOf(0);
		}
		return percent;
	}

	public void initNonpersistentFields(double totalPointsPossible, double totalPointsEarned) {
		Double percentageEarned;
		calculatedPointsEarned = totalPointsEarned;
		BigDecimal bdTotalPointsPossible = new BigDecimal(totalPointsPossible);
		BigDecimal bdTotalPointsEarned = new BigDecimal(totalPointsEarned);
		if (totalPointsPossible == 0.0) {
			percentageEarned = null;
		} else {
			percentageEarned = Double.valueOf(bdTotalPointsEarned.divide(bdTotalPointsPossible, GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100")).doubleValue());
		}
		autoCalculatedGrade = percentageEarned;
	}

	public void initNonpersistentFields(double totalPointsPossible, double totalPointsEarned, double literalTotalPointsEarned) {
		Double percentageEarned;
		//calculatedPointsEarned = totalPointsEarned;
		calculatedPointsEarned = literalTotalPointsEarned;
		BigDecimal bdTotalPointsPossible = new BigDecimal(totalPointsPossible);
		BigDecimal bdTotalPointsEarned = new BigDecimal(totalPointsEarned);

		if (totalPointsPossible <= 0.0) {
			percentageEarned = null;
		} else {
			percentageEarned = Double.valueOf(bdTotalPointsEarned.divide(bdTotalPointsPossible, GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100")).doubleValue());
		}
		autoCalculatedGrade = percentageEarned;
	}
}
