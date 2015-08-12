package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;

/**
 *  DTO for the {@link org.sakaiproject.gradebook.tool.Permission} to pass to external services. Not persisted.
 */
public class PermissionDefinition implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String userId;
	private String function;
	private Long categoryId;
	private String groupId;

	public Long getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getFunction() {
		return function;
	}
	
	public void setFunction(String function) {
		this.function = function;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}

	
}
