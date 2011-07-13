package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.sakaiproject.service.gradebook.shared.GradebookService;

public class Category implements Serializable
{
	private Long id;
	private int version;
	private Gradebook gradebook;
	private String name;
	private Double weight;
	private Integer drop_lowest;
	private Integer dropHighest;
    private Integer keepHighest;
    private Double itemValue;

    private boolean removed;
	private Double averageTotalPoints; //average total points possible for this category
	private Double averageScore; //average scores that students got for this category
	private Double mean; //mean value of percentage for this category
	private Double totalPointsEarned; //scores that students got for this category
	private Double totalPointsPossible; //total points possible for this category
	private List assignmentList;
	private int assignmentCount;
	private Boolean extraCredit = false;
	private Boolean unweighted;
	private Boolean equalWeightAssignments;
	private Integer categoryOrder;
	private Boolean enforcePointWeighting;
	
	public static Comparator nameComparator;
	public static Comparator averageScoreComparator;
	public static Comparator weightComparator;
	
  public static String SORT_BY_NAME = "name";
  public static String SORT_BY_AVERAGE_SCORE = "averageScore";
  public static String SORT_BY_WEIGHT = "weight";
  
	static
	{
		nameComparator = new Comparator() 
		{
			public int compare(Object o1, Object o2) 
			{
				return ((Category)o1).getName().toLowerCase().compareTo(((Category)o2).getName().toLowerCase());
			}
		};
		averageScoreComparator = new Comparator() 
		{
			public int compare(Object o1, Object o2) 
			{
				Category one = (Category)o1;
				Category two = (Category)o2;

				if(one.getAverageScore() == null && two.getAverageScore() == null) 
				{
					return one.getName().compareTo(two.getName());
				}

				if(one.getAverageScore() == null) {
					return -1;
				}
				if(two.getAverageScore() == null) {
					return 1;
				}

				int comp = (one.getAverageScore().compareTo(two.getAverageScore()));
				if(comp == 0) 
				{
					return one.getName().compareTo(two.getName());
				} 
				else 
				{
					return comp;
				}
			}
		};
		weightComparator = new Comparator() 
		{
			public int compare(Object o1, Object o2) 
			{
				Category one = (Category)o1;
				Category two = (Category)o2;

				if(one.getWeight() == null && two.getWeight() == null) 
				{
					return one.getName().compareTo(two.getName());
				}

				if(one.getWeight() == null) {
					return -1;
				}
				if(two.getWeight() == null) {
					return 1;
				}

				int comp = (one.getWeight().compareTo(two.getWeight()));
				if(comp == 0) 
				{
					return one.getName().compareTo(two.getName());
				} 
				else 
				{
					return comp;
				}
			}
		};
	}

	public Integer getDropHighest() {
        return dropHighest == null ? 0 : dropHighest;
    }

    public void setDropHighest(Integer dropHighest) {
        this.dropHighest = dropHighest;
    }
	
	public Integer getKeepHighest() {
        return keepHighest == null ? 0 : keepHighest;
    }

    public void setKeepHighest(Integer keepHighest) {
        this.keepHighest = keepHighest;
    }
    
    /*
     * returns true if this category drops any scores
     */
    public boolean isDropScores() {
        return getDrop_lowest() > 0 || getDropHighest() > 0 || getKeepHighest() > 0;
    }

    public Double getItemValue() {
    	if(isAssignmentsEqual()){
    		Double returnVal = 0.0;
    		List assignments = getAssignmentList();
            if(assignments != null){
                for(Object obj : assignments) {
                    if(obj instanceof Assignment) {
                        Assignment assignment = (Assignment)obj;
                        if(!Assignment.item_type_adjustment.equals(assignment.getItemType())) {// ignore adjustment items
                        	returnVal = assignment.getPointsPossible();
                        	return returnVal;
                        }                        
                    }
                }
            }
            //didn't find any, so return 0.0
    		return returnVal;
    	}else{
    		return 0.0;
    	}
    }

    public Integer getDrop_lowest()
    {
        return drop_lowest != null ? drop_lowest : 0;
    }
    
    public void setDrop_lowest(Integer drop_lowest)
    {
        this.drop_lowest = drop_lowest;
    }

    public Gradebook getGradebook()
	{
		return gradebook;
	}
	
	public void setGradebook(Gradebook gradebook)
	{
		this.gradebook = gradebook;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
	    // SAK-20071 - names over 255 chars cause DB insert failure
	    if (name != null && name.length() > 250) {
	        // truncate the name to only 250 chars
	        name = name.substring(0, 249);
	    }
		this.name = name;
	}
	
	public int getVersion()
	{
		return version;
	}
	
	public void setVersion(int version)
	{
		this.version = version;
	}
	
	public Double getWeight()
	{
		return weight;
	}
	
	public void setWeight(Double weight)
	{
		this.weight = weight;
	}

	public boolean isRemoved()
	{
		return removed;
	}

	public void setRemoved(boolean removed)
	{
		this.removed = removed;
	}

	public Double getAverageTotalPoints()
	{
		return averageTotalPoints;
	}

	public void setAverageTotalPoints(Double averageTotalPoints)
	{
		this.averageTotalPoints = averageTotalPoints;
	}

	public Double getAverageScore()
	{
		return averageScore;
	}

	public void setAverageScore(Double averageScore)
	{
		this.averageScore = averageScore;
	}
	
	public void calculateStatistics(List<Assignment> assignmentsWithStats)
	{
		int gbGradeType = getGradebook().getGrade_type();
		int numScored = 0;
		int numOfAssignments = 0;
		BigDecimal total = new BigDecimal("0");
		BigDecimal totalPossible = new BigDecimal("0");
		BigDecimal adjustmentPoints = new BigDecimal("0");

		for (Assignment assign : assignmentsWithStats) 
		{
			Double score = assign.getAverageTotal();

			//    	if(assign.isReleased())
			//    	{
			boolean adjustmentItemWithNoPoints = false;

			if(assign.isCounted() && !assign.getUngraded() && assign.getPointsPossible().doubleValue() > 0.0)
			{
				if (score == null) 
				{
				} 
				else 
				{
					if (assign.isExtraCredit()!=null)
					{
						if (assign.isExtraCredit())
						{
							if (assign.getPointsPossible()==null || assign.getPointsPossible()==0)
								adjustmentItemWithNoPoints = true;
						}
					}
					if (!adjustmentItemWithNoPoints)
					{
						if(assign.getPointsPossible() != null)
						{
							if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
								total = total.add(new BigDecimal(score.toString()));
							else if(gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE)
								total = total.add(new BigDecimal(score.toString()).multiply(new BigDecimal(assign.getPointsPossible())).divide(new BigDecimal("100")));
							if((gbGradeType == GradebookService.GRADE_TYPE_POINTS || gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE) && ((assign.isExtraCredit()!=null && !assign.isExtraCredit()) || assign.isExtraCredit()==null))
								totalPossible = totalPossible.add(new BigDecimal(assign.getPointsPossible().toString()));
							numOfAssignments ++;
						}
						numScored++;
					}
					else
					{
						BigDecimal bdScore = new BigDecimal(score.toString());
						if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
							total = total.add(bdScore);
						else if(gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE)
						{
							if (assign.isExtraCredit()!=null && assign.isExtraCredit())
								adjustmentPoints = adjustmentPoints.add(bdScore.multiply(new BigDecimal(1)).divide(new BigDecimal("100")));
							else
								total = total.add(bdScore.multiply(new BigDecimal(1)).divide(new BigDecimal("100")));
						}
					}
				}
			}
			//    	}
		}

		if (numScored == 0 || numOfAssignments == 0) 
		{
			averageScore = null;
			averageTotalPoints = null;
			mean = null;
			totalPointsEarned = null;
			totalPointsPossible = null;
		} 
		else 
		{
			BigDecimal bdNumScored = new BigDecimal(numScored);
			BigDecimal bdNumAssign = new BigDecimal(numOfAssignments);
			totalPointsEarned = total.doubleValue();
			totalPointsPossible = totalPossible.doubleValue();
			averageScore = new Double(total.divide(bdNumScored, GradebookService.MATH_CONTEXT).doubleValue());
			averageTotalPoints = new Double(totalPossible.divide(bdNumAssign, GradebookService.MATH_CONTEXT).doubleValue());
			BigDecimal value = total.divide(bdNumScored, GradebookService.MATH_CONTEXT).divide(new BigDecimal(averageTotalPoints.doubleValue()), GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));
			if (gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE) 	 
			{
				averageScore += adjustmentPoints.doubleValue();
				value = value.add(adjustmentPoints.multiply(new BigDecimal("100"), GradebookService.MATH_CONTEXT), GradebookService.MATH_CONTEXT);
			}
			mean = new Double(value.doubleValue()) ;
		}
	}
	

	public void calculateStatisticsPerStudent(List<AssignmentGradeRecord> gradeRecords, String studentUid)
	{
		int gbGradeType = getGradebook().getGrade_type();
		int numScored = 0;
		int numOfAssignments = 0;
		BigDecimal total = new BigDecimal("0");
		BigDecimal totalPossible = new BigDecimal("0");
		BigDecimal adjustmentPoints = new BigDecimal("0");

		if (gradeRecords == null) 
		{
			setAverageScore(null);
			setAverageTotalPoints(null);
			setMean(null);
			setTotalPointsEarned(null);
			setTotalPointsPossible(null);
			return;
		}

		for (AssignmentGradeRecord gradeRecord : gradeRecords) 
		{
			if(gradeRecord != null && gradeRecord.getStudentId().equals(studentUid))
			{
				Assignment assignment = gradeRecord.getAssignment();
				boolean adjustmentItemWithNoPoints = false;
				if (assignment.isCounted() && !assignment.getUngraded() && assignment.getPointsPossible().doubleValue() > 0.0 && !gradeRecord.getDroppedFromGrade()) 
				{

					Category assignCategory = assignment.getCategory();
					if (assignCategory != null && assignCategory.getId()!=null && assignCategory.getId().equals(id))
					{
						Double score = gradeRecord.getPointsEarned();
						if (assignment.isExtraCredit()!=null)
						{
							if (assignment.isExtraCredit()!=null)
							{
								if (assignment.getPointsPossible()==null || assignment.getPointsPossible()==0)
									adjustmentItemWithNoPoints = true;
							}
						}
						if (!adjustmentItemWithNoPoints)
						{
							if (score != null) 
							{
								BigDecimal bdScore = new BigDecimal(score);
								if(assignment.getPointsPossible() != null)
								{
									if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
										total = total.add(bdScore);
									else if(gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE)
										total = total.add(bdScore.multiply(new BigDecimal(assignment.getPointsPossible())).divide(new BigDecimal("100")));
									if((gbGradeType == GradebookService.GRADE_TYPE_POINTS || gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE) && ((assignment.isExtraCredit()!=null && !assignment.isExtraCredit()) || assignment.isExtraCredit()==null))
									{
										BigDecimal bdPointsPossible = new BigDecimal(assignment.getPointsPossible().toString());
										totalPossible = totalPossible.add(bdPointsPossible);
									}
									numOfAssignments ++;
								}
								numScored++;
							}
						}
						else
						{
							if (score != null) 
							{
								BigDecimal bdScore = new BigDecimal(score);
								if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
									total = total.add(bdScore);
								else if(gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE)
								{
									if (assignment.isExtraCredit()!=null && assignment.isExtraCredit())
										adjustmentPoints = adjustmentPoints.add(bdScore.multiply(new BigDecimal(1)).divide(new BigDecimal("100")));
									else
										total = total.add(bdScore.multiply(new BigDecimal(1)).divide(new BigDecimal("100")));
								}
							}
						}
					}
				}
			}
		}


		// if totalPossible is 0, this prevents a division by zero scenario likely from
		// an adjustment item being the only thing graded.
		if (numScored == 0 || numOfAssignments == 0 || totalPossible.doubleValue() == 0) 
		{
			averageScore = null;
			averageTotalPoints = null;
			mean = null;
			totalPointsEarned = null;
			totalPointsPossible = null;
		} 
		else 
		{
                BigDecimal bdNumScored = new BigDecimal(numScored);
				BigDecimal bdNumAssign = new BigDecimal(numOfAssignments);
				totalPointsEarned = total.doubleValue();;
				totalPointsPossible = totalPossible.doubleValue();
				averageScore = new Double(total.divide(bdNumScored, GradebookService.MATH_CONTEXT).doubleValue());
				averageTotalPoints = new Double(totalPossible.divide(bdNumAssign, GradebookService.MATH_CONTEXT).doubleValue());
				BigDecimal value = total.divide(bdNumScored, GradebookService.MATH_CONTEXT).divide((totalPossible.divide(bdNumAssign, GradebookService.MATH_CONTEXT)), GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));
				if (gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE) 	 
                {
					averageScore += adjustmentPoints.doubleValue();
					value = value.add(adjustmentPoints.multiply(new BigDecimal("100"), GradebookService.MATH_CONTEXT), GradebookService.MATH_CONTEXT);
                }
				mean = new Double(value.doubleValue()) ;
		}
	}
	

	public List getAssignmentList()
	{
		return assignmentList;
	}

	public void setAssignmentList(List assignmentList)
	{
		this.assignmentList = assignmentList;
	}
	
	/*
	 * The methods below are used with the GradableObjects because all three
	 * are displayed in a dataTable together
	 */
	public boolean getIsCategory() {
		return true;
	}
	public boolean isCourseGrade() {
		return false;
	}
	public boolean isAssignment() {
		return false;
	}

	public Double getMean()
	{
		return mean;
	}

	public void setMean(Double mean)
	{
		this.mean = mean;
	}
	
	public int getAssignmentCount(){
		return assignmentCount;
	}
	
	public void setAssignmentCount(int assignmentCount){
		this.assignmentCount = assignmentCount;
	}

	public Boolean isExtraCredit() {
		return extraCredit;
	}

	public void setExtraCredit(Boolean isExtraCredit) {
		this.extraCredit = isExtraCredit;
	}

    public boolean isAssignmentsEqual() {
        boolean isEqual = true;
        Double pointsPossible = null;
        List assignments = getAssignmentList();
        if(assignments == null) {
            return isEqual;
        } else {
            for(Object obj : assignments) {
                if(obj instanceof Assignment) {
                    Assignment assignment = (Assignment)obj;
                    if(pointsPossible == null) {
                        if(!Assignment.item_type_adjustment.equals(assignment.getItemType())) {// ignore adjustment items
                            pointsPossible = assignment.getPointsPossible();
                        }
                    } else {
                        if(assignment.getPointsPossible() != null
                                && !Assignment.item_type_adjustment.equals(assignment.getItemType()) // ignore adjustment items that are not equal
                                && !pointsPossible.equals(assignment.getPointsPossible())) {
                            isEqual = false;
                            return isEqual;
                        }
                    }
                }
            }
        }
        return isEqual;
    }	

	public Boolean isUnweighted() {
		return unweighted;
	}

	public void setUnweighted(Boolean unweighted) {
		this.unweighted = unweighted;
	}

	public Boolean isEqualWeightAssignments() {
		return equalWeightAssignments;
	}

	public void setEqualWeightAssignments(Boolean equalWeightAssignments) {
		this.equalWeightAssignments = equalWeightAssignments;
	}

	public Integer getCategoryOrder() {
		return categoryOrder;
	}

	public void setCategoryOrder(Integer categoryOrder) {
		this.categoryOrder = categoryOrder;
	}

	public Boolean isEnforcePointWeighting() {
		return enforcePointWeighting;
	}

	public void setEnforcePointWeighting(Boolean enforcePointWeighting) {
		this.enforcePointWeighting = enforcePointWeighting;
	}

	public Double getTotalPointsEarned() {
		return totalPointsEarned;
	}

	public void setTotalPointsEarned(Double totalPointsEarned) {
		this.totalPointsEarned = totalPointsEarned;
	}

	public Double getTotalPointsPossible() {
		return totalPointsPossible;
	}

	public void setTotalPointsPossible(Double totalPointsPossible) {
		this.totalPointsPossible = totalPointsPossible;
	}
}
