package org.sakaiproject.tool.gradebook;

import java.io.Serializable;

public class Category implements Serializable
{
	private Long id;
	private int version;
	private Gradebook gradebook;
	private String name;
	private Double weight;
	private int drop_lowest;
	private boolean removed;

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
}
