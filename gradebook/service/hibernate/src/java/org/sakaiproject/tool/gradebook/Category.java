package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

public class Category implements Serializable
{
	private Long id;
	private int version;
	private Gradebook gradebook;
	private String name;
	private Double weight;
	private int drop_lowest;
	private boolean removed;
	private Double averageTotalPoints; //average total points possible for this category
	private Double averageScore; //average scores that students got for this category
	private Double mean; //mean value of percentage for this category
	private List assignmentList;
	private int assignmentCount;
	
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

	public int getDrop_lowest()
	{
		return drop_lowest;
	}
	
	public void setDrop_lowest(int drop_lowest)
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
    int numScored = 0;
    int numOfAssignments = 0;
    double total = 0;
    double totalPossible = 0;

    for (Assignment assign : assignmentsWithStats) 
    {
    	Double score = assign.getAverageTotal();
//    	if(assign.isReleased())
//    	{
    	if(assign.isCounted() && assign.getPointsPossible().doubleValue() > 0.0)
    	{
    		if (score == null) 
    		{
    		} 
    		else 
    		{
    			total += score.doubleValue();
    			if(assign.getPointsPossible() != null)
    			{
    				totalPossible += assign.getPointsPossible().doubleValue();
    				numOfAssignments ++;
    			}
    			numScored++;
    		}
    	}
//    	}
    }

    if (numScored == 0 || numOfAssignments == 0) 
    {
    	averageScore = null;
    	averageTotalPoints = null;
    	mean = null;
    } 
    else 
    {
    	averageScore = new Double(total / numScored);
    	averageTotalPoints = new Double(totalPossible / numOfAssignments);
    	double value = total / numScored / averageTotalPoints.doubleValue() * 100;
    	mean = new Double(value) ;
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
	public boolean isCategory() {
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
}
