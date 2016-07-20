package org.sakaiproject.contentreview.dao;

public class ContentReviewActivityConfigEntry
{
	private Long id = -1L;
	private String activityId;
	private String toolId;
	private int providerId;
	private String name;
	private String value;
	
	public ContentReviewActivityConfigEntry()
	{
		this("", "", "", "", -1);
	}
	
	public ContentReviewActivityConfigEntry(String name, String value, String activityId, String toolId, int providerId)
	{
		this.name = name;
		this.value = value;
		this.toolId = toolId;
		this.activityId = activityId;
		this.providerId = providerId;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getToolId()
	{
		return toolId;
	}

	public void setToolId(String toolId)
	{
		this.toolId = toolId;
	}

	public String getActivityId()
	{
		return activityId;
	}

	public void setActivityId(String activityId)
	{
		this.activityId = activityId;
	}

	public int getProviderId()
	{
		return providerId;
	}

	public void setProviderId(int providerId)
	{
		this.providerId = providerId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
	
}

