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

package org.sakaiproject.tool.gradebook;

import java.math.BigDecimal;
import java.util.Comparator;

import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * An AssignmentGradeRecord is a grade record that can be associated with an
 * GradebookAssignment.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AssignmentGradeRecord extends AbstractGradeRecord implements Cloneable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8259092798479256962L;
	
	private Double pointsEarned;
    private String letterEarned;
    private Double percentEarned;
    private boolean userAbleToView;
    private Boolean excludedFromGrade;
    private transient BigDecimal earnedWeightedPercentage;
    private transient BigDecimal overallWeight;
    private transient Boolean isDropped;
    // used for drop highest/lowest score functionality
    private Boolean droppedFromGrade;

    public static Comparator<AssignmentGradeRecord> numericComparator;

    static{
    	numericComparator = new Comparator<AssignmentGradeRecord>() {
            @Override
			public int compare(AssignmentGradeRecord agr1, AssignmentGradeRecord agr2) {
                if(agr1 == null && agr2 == null) {
                    return 0;
                }
                if(agr1 == null) {
                    return -1;
                }
                if(agr2 == null) {
                    return 1;
                }
                Double agr1Points = agr1.getPointsEarned();
                Double agr2Points = agr2.getPointsEarned();
                
                if (agr1Points == null && agr2Points == null) {
                    return 0;
                }
                if (agr1Points == null && agr2Points != null) {
                    return -1;
                }
                if (agr1Points != null && agr2Points == null) {
                    return 1;
                }
                try {
                    return agr1Points.compareTo(agr2Points);
                } catch(NumberFormatException e) {
                    return agr1Points.compareTo(agr2Points); // if not number, default to calcComparator functionality
                }
            }
        };
    }

    public AssignmentGradeRecord() {
        super();
    }

    /**
     * The graderId and dateRecorded properties will be set explicitly by the
     * grade manager before the database is updated.
	 * @param assignment The assignment this grade record is associated with
     * @param studentId The student id for whom this grade record belongs
	 * @param grade The grade, or points earned
	 */
	public AssignmentGradeRecord(GradebookAssignment assignment, String studentId, Double grade) {
        super();
        this.gradableObject = assignment;
        this.studentId = studentId;
        this.pointsEarned = grade;
	}
	
	public static Comparator<AssignmentGradeRecord> calcComparator;

    static {
        calcComparator = new Comparator<AssignmentGradeRecord>() {
            @Override
			public int compare(AssignmentGradeRecord agr1, AssignmentGradeRecord agr2) {
                if(agr1 == null && agr2 == null) {
                    return 0;
                }
                if(agr1 == null) {
                    return -1;
                }
                if(agr2 == null) {
                    return 1;
                }
                Double agr1Points = agr1.getPointsEarned();
                Double agr2Points = agr2.getPointsEarned();
                
                if (agr1Points == null && agr2Points == null) {
                	return 0;
                }
                if (agr1Points == null && agr2Points != null) {
                	return -1;
                }
                if (agr1Points != null && agr2Points == null) {
                	return 1;
                }
                return agr1Points.compareTo(agr2Points);
            }
        };
    }

    /**
     * @return Returns the pointsEarned
     */
    @Override
	public Double getPointsEarned() {
        return pointsEarned;
    }

	/**
	 * @param pointsEarned The pointsEarned to set.
	 */
	public void setPointsEarned(Double pointsEarned) {
		this.pointsEarned = pointsEarned;
	}

    /**
     * Returns null if the points earned is null.  Otherwise, returns earned / points possible * 100.
     *
     * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#getGradeAsPercentage()
     */
    @Override
	public Double getGradeAsPercentage() {
        if (pointsEarned == null) {
            return null;
        }
        BigDecimal bdPointsEarned = new BigDecimal(pointsEarned.toString());
        BigDecimal bdPossible = new BigDecimal(((GradebookAssignment)getGradableObject()).getPointsPossible().toString());
        BigDecimal bdPercent = bdPointsEarned.divide(bdPossible, GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));
        return Double.valueOf(bdPercent.doubleValue());
    }

	/**
	 * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#isCourseGradeRecord()
	 */
	@Override
	public boolean isCourseGradeRecord() {
		return false;
	}

    public GradebookAssignment getAssignment() {
    	return (GradebookAssignment)getGradableObject();
    }
    
    public Double getPercentEarned() {
    	return percentEarned;
    }
    
    public void setPercentEarned(Double percentEarned) {
    	this.percentEarned = percentEarned;
    }

    public String getLetterEarned()
    {
    	return letterEarned;
    }

    public void setLetterEarned(String letterEarned)
    {
    	this.letterEarned = letterEarned;
    }
    
    public boolean isUserAbleToView() {
    	return userAbleToView;
    }
    public void setUserAbleToView(boolean userAbleToView) {
    	this.userAbleToView = userAbleToView;
    }

    @Override
	public AssignmentGradeRecord clone()
    {
    	AssignmentGradeRecord agr = new AssignmentGradeRecord();
    	agr.setDateRecorded(dateRecorded);
    	agr.setGradableObject(gradableObject);
    	agr.setGraderId(graderId);
    	agr.setLetterEarned(letterEarned);
    	agr.setPointsEarned(pointsEarned);
    	agr.setPercentEarned(percentEarned);
    	agr.setStudentId(studentId);
    	return agr;
    }

	public Boolean isExcludedFromGrade() {
		return excludedFromGrade;
	}

	public void setExcludedFromGrade(Boolean isExcludedFromGrade) {
		this.excludedFromGrade = isExcludedFromGrade;
	}
	
	public BigDecimal getEarnedWeightedPercentage() {
		return earnedWeightedPercentage;
	}
	
	public void setEarnedWeightedPercentage(BigDecimal earnedWeightedPercentage) {
		this.earnedWeightedPercentage = earnedWeightedPercentage;
	}
	
	public Boolean isDropped() {
		return isDropped;
	}
	
	public void setDropped(Boolean isDropped) {
		this.isDropped = isDropped;
	}
	
	public BigDecimal getOverallWeight() {
		return overallWeight;
	}
	
	public void setOverallWeight(BigDecimal overallWeight) {
		this.overallWeight = overallWeight;
	}
	
	public Boolean getDroppedFromGrade() {
        return this.droppedFromGrade == null ? false : this.droppedFromGrade;
    }

    public void setDroppedFromGrade(Boolean droppedFromGrade) {
        this.droppedFromGrade = droppedFromGrade;
    }
}



