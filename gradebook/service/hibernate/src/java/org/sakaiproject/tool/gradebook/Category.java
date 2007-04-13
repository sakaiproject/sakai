package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
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
	private Double averageTotalPoints;
	private Double averageScore;
	private List assignmentList;

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
    	if(assign.getPointsPossible() != null)
    	{
    		totalPossible += assign.getPointsPossible().doubleValue();
    		numOfAssignments ++;
    	}
    	if (score == null) 
    	{
    		numScored++;
    	} 
    	else 
    	{
    		total += score.doubleValue();
    		numScored++;
    	}
    }

    if (numScored == 0 || numOfAssignments == 0) 
    {
    	averageScore = null;
    	averageTotalPoints = null;
    } 
    else 
    {
    	averageScore = new Double(total / numScored);
    	averageTotalPoints = new Double(totalPossible / numOfAssignments);
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
}
