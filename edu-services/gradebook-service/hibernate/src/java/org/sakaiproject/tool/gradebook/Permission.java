/**
 * Copyright (c) 2003-2009 The Apereo Foundation
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
