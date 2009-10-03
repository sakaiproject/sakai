package org.sakaiproject.tool.gradebook;

import java.io.Serializable;

public class Permission implements Serializable
{
	private Long id;
	private int version;
	private Long gradebookId;
	private String userId;
	private String function;
	private Long categoryId;
	private String groupId;

	public Long getCategoryId()
	{
		return categoryId;
	}
	
	public void setCategoryId(Long categoryId)
	{
		this.categoryId = categoryId;
	}
	
	public Long getGradebookId()
	{
		return gradebookId;
	}
	
	public void setGradebookId(Long gradebookId)
	{
		this.gradebookId = gradebookId;
	}
	
	public String getGroupId()
	{
		return groupId;
	}
	
	public void setGroupId(String groupId)
	{
		this.groupId = groupId;
	}
	
	public Long getId()
	{
		return id;
	}
	
	public void setId(Long id)
	{
		this.id = id;
	}
	
	public String getFunction()
	{
		return function;
	}
	
	public void setFunction(String function)
	{
		this.function = function;
	}
	
	public String getUserId()
	{
		return userId;
	}
	
	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public int getVersion()
	{
		return version;
	}
	
	public void setVersion(int version)
	{
		this.version = version;
	}
	
}
