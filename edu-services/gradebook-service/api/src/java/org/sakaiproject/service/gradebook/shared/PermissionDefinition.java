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
	private String groupReference;

	public Long getCategoryId() {
		return categoryId;
	}
	
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	
	public String getGroupReference() {
		return groupReference;
	}
	
	public void setGroupReference(String groupReference) {
		this.groupReference = groupReference;
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
