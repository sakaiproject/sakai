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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * An Assignment is the basic unit that composes a gradebook.  It represents a
 * single unit that, when aggregated in a gradebook, can be used as the
 * denomenator in calculating a CourseGradeRecord.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class Assignment extends GradableObject {

	/**
	 * use the SortType enum instead. This remains only for the gradebook tool
	 */
	@Deprecated public static String SORT_BY_DATE = "dueDate";
	@Deprecated public static String SORT_BY_NAME = "name";
	@Deprecated public static String SORT_BY_MEAN = "mean";
	@Deprecated public static String SORT_BY_POINTS = "pointsPossible";
	@Deprecated public static String SORT_BY_RELEASED ="released";
	@Deprecated public static String SORT_BY_COUNTED = "counted";
	@Deprecated public static String SORT_BY_EDITOR = "gradeEditor";
	@Deprecated public static String SORT_BY_SORTING = "sorting";
	@Deprecated public static String DEFAULT_SORT = SORT_BY_SORTING;
    
    public static String item_type_points = "Points";
    public static String item_type_percentage = "Percentage";
    public static String item_type_letter = "Letter Grade";
    public static String item_type_nonCalc = "Non-calculating";
    public static String item_type_adjustment = "Adjustment";

    public static Comparator dateComparator;
    public static Comparator nameComparator;
    public static Comparator pointsComparator;
    public static Comparator meanComparator;
    public static Comparator releasedComparator;
    public static Comparator countedComparator;
    public static Comparator gradeEditorComparator;
    public static Comparator categoryComparator;

    private Double pointsPossible;
    private Date dueDate;
    private boolean notCounted;
    private boolean externallyMaintained;
    private String externalStudentLink;
    private String externalInstructorLink;
    private String externalId;
    private String externalAppName;
    private boolean released;
    private Category category;
    private Double averageTotal;
    private boolean ungraded;
    private Boolean extraCredit = Boolean.FALSE;
	private Double assignmentWeighting;
	private Boolean countNullsAsZeros;
	private String itemType;
	public String selectedGradeEntryValue;
	private boolean hideInAllGradesTable = false;

	static {
        dateComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                if(log.isDebugEnabled()) log.debug("Comparing assignment + " + o1 + " to " + o2 + " by date");
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;

                // Sort by name if no date on either
                if(one.getDueDate() == null && two.getDueDate() == null) {
                    return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
                }
                // Null dates are last
                if(one.getDueDate() == null) {
                    return 1;
                }
                if(two.getDueDate() == null) {
                    return -1;
                }
                // Sort by name if both assignments have the same date
                int comp = (one.getDueDate().compareTo(two.getDueDate()));
                if(comp == 0) {
                    return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
                } else {
                    return comp;
                }
            }
            @Override
            public String toString() {
                return "Assignment.dateComparator";
            }
        };
        nameComparator = new Comparator() {
			public int compare(Object o1, Object o2) {
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;
                return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
			}
            @Override
            public String toString() {
                return "Assignment.nameComparator";
            }
        };
        pointsComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                if(log.isDebugEnabled()) log.debug("Comparing assignment + " + o1 + " to " + o2 + " by points");
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;

                int comp = one.getPointsPossible().compareTo(two.getPointsPossible());
                if(comp == 0) {
                    return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
                } else {
                    return comp;
                }
            }
            @Override
            public String toString() {
                return "Assignment.pointsComparator";
            }
        };
        meanComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                if(log.isDebugEnabled()) log.debug("Comparing assignment + " + o1 + " to " + o2 + " by mean");
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;

                Double mean1 = one.getMean();
                Double mean2 = two.getMean();
                if(mean1 == null && mean2 == null) {
                    return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
                }
                if(mean1 != null && mean2 == null) {
                    return 1;
                }
                if(mean1 == null && mean2 != null) {
                    return -1;
                }
                int comp = mean1.compareTo(mean2);
                if(comp == 0) {
                    return one.getName().toLowerCase().compareTo(two.getName().toLowerCase());
                } else {
                    return comp;
                }
            }
            @Override
            public String toString() {
                return "Assignment.meanComparator";
            }
        };

        releasedComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                if(log.isDebugEnabled()) log.debug("Comparing assignment + " + o1 + " to " + o2 + " by release");
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;

                int comp = String.valueOf(one.isReleased()).compareTo(String.valueOf(two.isReleased()));
                if(comp == 0) {
                    return one.getName().compareTo(two.getName());
                } else {
                    return comp;
                }
            }
            @Override
            public String toString() {
                return "Assignment.releasedComparator";
            }
        };
        
        countedComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                if(log.isDebugEnabled()) log.debug("Comparing assignment + " + o1 + " to " + o2 + " by counted");
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;

                int comp = String.valueOf(one.isCounted()).compareTo(String.valueOf(two.isCounted()));
                if(comp == 0) {
                    return one.getName().compareTo(two.getName());
                } else {
                    return comp;
                }
            }
            @Override
            public String toString() {
                return "Assignment.countedComparator";
            }
        };
        
        gradeEditorComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                if(log.isDebugEnabled()) log.debug("Comparing assignment + " + o1 + " to " + o2 + " by grade editor");
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;

                int comp = String.valueOf(one.getExternalAppName()).compareTo(String.valueOf(two.getExternalAppName()));
                if(comp == 0) {
                    return one.getName().compareTo(two.getName());
                } else {
                    return comp;
                }
            }
            @Override
            public String toString() {
                return "Assignment.gradeEditorComparator";
            }
        };

        categoryComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                if(log.isDebugEnabled()) log.debug("Comparing assignment + " + o1 + " to " + o2 + " by category ordering");
                Assignment one = (Assignment)o1;
                Assignment two = (Assignment)o2;

                // if categories are null
                if (one.getCategory() == null && two.getCategory() == null) {
                    // sort by assignment sort order
                    return one.getSortOrder().compareTo(two.getSortOrder());
                } else if (one.getCategory() == null) {
                    return 1;
                } else if (two.getCategory() == null) {
                    return -1;
                }

                // if in the same category, sort by their categorized sort order
                if (one.getCategory().equals(two.getCategory())) {
                    // handles null orders by putting them at the end of the list
                    if (one.getCategorizedSortOrder() == null) {
                        return 1;
                    } else if (two.getCategorizedSortOrder() == null) {
                        return -1;
                    }
                    return Integer.compare(one.getCategorizedSortOrder(), two.getCategorizedSortOrder());

                // otherwise, sort by their category order
                } else {
                    // check if category has a order (not required)
                    if (one.getCategory().getCategoryOrder() == null && two.getCategory().getCategoryOrder() == null) {
                        // both orders are null.. so order by A-Z
                        if (one.getCategory().getName() == null && two.getCategory().getName() == null) {
                            // both names are null so order by id
                            return one.getCategory().getId().compareTo(two.getCategory().getId());
                        } else if (one.getCategory().getName() == null) {
                            return 1;
                        } else if (two.getCategory().getName() == null) {
                            return -1;
                        } else {
                            return one.getCategory().getName().compareTo(two.getCategory().getName());
                        }
                    } else if (one.getCategory().getCategoryOrder() == null) {
                        return 1;
                    } else if (two.getCategory().getCategoryOrder() == null) {
                        return -1;
                    } else {
                        return one.getCategory().getCategoryOrder().compareTo(two.getCategory().getCategoryOrder());
                    }
                }
            }

            @Override
            public String toString() {
                return "Assignment.categoryComparator";
            }
        };
    }

    public Assignment(Gradebook gradebook, String name, Double pointsPossible, Date dueDate) {
        this.gradebook = gradebook;
        this.name = name;
        this.pointsPossible = pointsPossible;
        this.dueDate = dueDate;
        this.released = true;
        this.extraCredit = Boolean.FALSE;
        this.hideInAllGradesTable = false;
    }


    /**
     * constructor to support selective release
     * @param gradebook
     * @param name
     * @param pointsPossible
     * @param dueDate
     * @param released
     */
    public Assignment(Gradebook gradebook, String name, Double pointsPossible, Date dueDate, boolean released) {
        this.gradebook = gradebook;
        this.name = name;
        this.pointsPossible = pointsPossible;
        this.dueDate = dueDate;
        this.released = released;
        this.extraCredit = Boolean.FALSE;
        this.hideInAllGradesTable = false;
    }

    public Assignment() {
    	super();
    	this.extraCredit = Boolean.FALSE;
        this.hideInAllGradesTable = false;
    }

	/**
     */
    public boolean isCourseGrade() {
        return false;
    }
    /**
     * @see org.sakaiproject.tool.gradebook.GradableObject#isAssignment()
     */
    public boolean isAssignment() {
        return true;
    }
    /**
     * @see org.sakaiproject.tool.gradebook.GradableObject#isCategory()
     */
    public boolean getIsCategory() {
        return false;
    }

	/**
	 */
	public Date getDateForDisplay() {
        return dueDate;
	}

	/**
	 * @return Returns the dueDate.
	 */
	public Date getDueDate() {
		return dueDate;
	}
	/**
	 * @param dueDate The dueDate to set.
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	/**
	 */
	public boolean isNotCounted() {
		return notCounted;
	}
	/**
	 */
	public void setNotCounted(boolean notCounted) {
		this.notCounted = notCounted;
	}
	/**
	 */
	public boolean isCounted() {
		return !isNotCounted();
	}
	/**
	 * This cover is for the benefit of JSF checkboxes.
	 */
	public void setCounted(boolean counted) {
		setNotCounted(!counted);
	}
	/**
	 * @return Returns the externalInstructorLink.
	 */
	public String getExternalInstructorLink() {
		return externalInstructorLink;
	}
	/**
	 * @param externalInstructorLink The externalInstructorLink to set.
	 */
	public void setExternalInstructorLink(String externalInstructorLink) {
		this.externalInstructorLink = externalInstructorLink;
	}
	/**
	 * @return Returns the externallyMaintained.
	 */
	public boolean isExternallyMaintained() {
		return externallyMaintained;
	}
	/**
	 * @param externallyMaintained The externallyMaintained to set.
	 */
	public void setExternallyMaintained(boolean externallyMaintained) {
		this.externallyMaintained = externallyMaintained;
	}
	/**
	 * @return Returns the externalStudentLink.
	 */
	public String getExternalStudentLink() {
		return externalStudentLink;
	}
	/**
	 * @param externalStudentLink The externalStudentLink to set.
	 */
	public void setExternalStudentLink(String externalStudentLink) {
		this.externalStudentLink = externalStudentLink;
	}
	/**
	 * @return Returns the pointsPossible.
	 */
	public Double getPointsPossible() {
		return pointsPossible;
	}
	/**
	 * @param pointsPossible The pointsPossible to set.
	 */
	public void setPointsPossible(Double pointsPossible) {
		this.pointsPossible = pointsPossible;
	}
	/**
	 * @return Returns the externalId.
	 */
	public String getExternalId() {
		return externalId;
	}
	/**
	 * @param externalId The externalId to set.
	 */
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	/**
	 * @return Returns the externalAppName.
	 */
	public String getExternalAppName() {
		return externalAppName;
	}
	/**
	 * @param externalAppName The externalAppName to set.
	 */
	public void setExternalAppName(String externalAppName) {
		this.externalAppName = externalAppName;
	}


    /**
     *
     * @return selective release true or false
     */

    public boolean isReleased() {
        return released;
    }

    /**
     *
     * @param released returns wther the assignment has been released to users
     */
    public void setReleased(boolean released) {
        this.released = released;
    }

    /**
     * Calculate the mean score for students with entered grades.
     */
    public void calculateStatistics(Collection<AssignmentGradeRecord> gradeRecords) {
        int numScored = 0;
        BigDecimal total = new BigDecimal("0");
        BigDecimal pointsTotal = new BigDecimal("0");
        for (AssignmentGradeRecord record : gradeRecords) {
            // Skip grade records that don't apply to this gradable object
            if(!record.getGradableObject().equals(this)) {
                continue;
            }

            if(record.getDroppedFromGrade() == null) {
                throw new RuntimeException("record.droppedFromGrade cannot be null");
            }

            Double score = null;
            if(!ungraded && pointsPossible > 0)
            	score = record.getGradeAsPercentage();
            Double points = record.getPointsEarned();
            if (score == null && points == null || record.getDroppedFromGrade()) {
            	continue;
            }
            else if (score == null)
            {
            	pointsTotal = pointsTotal.add(new BigDecimal(points.toString()));
            	numScored++;
            }
            else 
            {
            	total = total.add(new BigDecimal(score.toString()));
            	pointsTotal = pointsTotal.add(new BigDecimal(points.toString()));
            	numScored++;
            }
        }
        if (numScored == 0) {
        	mean = null;
        	averageTotal = null;
        } else {
        	BigDecimal bdNumScored = new BigDecimal(numScored);
        	if(!ungraded && pointsPossible > 0)
        	{
        		mean = Double.valueOf(total.divide(bdNumScored, GradebookService.MATH_CONTEXT).doubleValue());
        	}
        	else
        	{
        		mean = null;
        	}
        	averageTotal = Double.valueOf(pointsTotal.divide(bdNumScored, GradebookService.MATH_CONTEXT).doubleValue());
        }
    }

		public Category getCategory()
		{
			return category;
		}

		public void setCategory(Category category)
		{
			this.category = category;
		}


		public Double getAverageTotal()
		{
			return averageTotal;
		}

		public void setAverageTotal(Double averageTotal)
		{
			this.averageTotal = averageTotal;
		}


		public boolean getUngraded()
		{
			return ungraded;
		}

		public void setUngraded(boolean ungraded)
		{
			this.ungraded = ungraded;
		}
		
		//these two functions are needed to keep the old API and help JSF and RSF play nicely together.  Since isExtraCredit already exists and we can't remove it
		//and JSF expects Boolean values to be "getExtraCredit", this had to be added for JSF.  Also, since the external GB create item page is in
		//RSF, you can't name it getExtraCredit and keep isExtraCredit b/c of SAK-14589
		public Boolean getIsExtraCredit(){
			return isExtraCredit();
		}
		
		public void setIsExtraCredit(Boolean isExtraCredit){
			this.setExtraCredit(isExtraCredit);
		}
		
		public Boolean isExtraCredit() {
			if(extraCredit == null){
				return Boolean.FALSE;
			}
			return extraCredit;
		}
		
		public void setExtraCredit(Boolean isExtraCredit) {
			this.extraCredit = isExtraCredit;
		}
		
		public Double getAssignmentWeighting() {
			return assignmentWeighting;
		}


		public void setAssignmentWeighting(Double assignmentWeighting) {
			this.assignmentWeighting = assignmentWeighting;
		}
		
		public String getItemType() {
			Gradebook gb = getGradebook();
			if (gb!=null)
			{
				if (isExtraCredit()!=null)
				{
					if (isExtraCredit())
					{
						// if we made it in here, go ahead and return since adjustment item takes priority over the rest
						itemType = item_type_adjustment;
						return itemType;
					}
				}
				
				if (getUngraded())
				{
					// if we made it in here, go ahead and return since non-calc item takes priority over the rest
					itemType = item_type_nonCalc;
					return itemType;
				}
				
				if(gb.getGrade_type() == GradebookService.GRADE_TYPE_POINTS)
				{
					itemType = item_type_points;
				}
				else if(gb.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE)
				{
					itemType = item_type_percentage;
				}
				else if(gb.getGrade_type() == GradebookService.GRADE_TYPE_LETTER)
				{
					itemType = item_type_letter;
				}
			}
			return itemType;
		}


		public void setItemType(String itemType) {
			this.itemType = itemType;
		}

		public Boolean getCountNullsAsZeros() {
			return countNullsAsZeros;
		}


		public void setCountNullsAsZeros(Boolean countNullsAsZeros) {
			this.countNullsAsZeros = countNullsAsZeros;
		}
		
		public String getSelectedGradeEntryValue() {
			return selectedGradeEntryValue;
		}

		public void setSelectedGradeEntryValue(String selectedGradeEntryValue) {
			this.selectedGradeEntryValue = selectedGradeEntryValue;
		}
		
		/**
		 * Convenience method for checking if the grade for the assignment should be included in calculations.
		 * This is different from just the {@link #isCounted()} method for an assignment.  This method does a more thorough check
		 * using other values, such as if removed, isExtraCredit, ungraded, etc in addition to the assignment's notCounted property.
		 * @return true if grades for this assignment should be included in various calculations.
		 */
		public boolean isIncludedInCalculations() {
			boolean isIncludedInCalculations = false;
			boolean extraCredit = isExtraCredit()!=null && isExtraCredit();
    		if (!removed && !ungraded && !notCounted && (extraCredit || (pointsPossible != null && pointsPossible>0)))
    		{
    			isIncludedInCalculations = true;
    		}
			return isIncludedInCalculations;
		}

	public boolean isHideInAllGradesTable() {
		return hideInAllGradesTable;
	}

	public void setHideInAllGradesTable(boolean hideInAllGradesTable) {
		this.hideInAllGradesTable = hideInAllGradesTable;
	}
}
