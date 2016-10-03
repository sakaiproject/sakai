package org.sakaiproject.service.gradebook.shared;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PermissionDefinition)) {
            return false;
        }
		
		PermissionDefinition other  = (PermissionDefinition) o;
		
		return new EqualsBuilder()
				// id purposely not included so that we dont get duplicate permissions
				.append(userId, other.userId)
				.append(function, other.function)
				.append(categoryId, other.categoryId)
				.append(groupReference, other.groupReference)
				.isEquals();
	}
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder()
        		.append(userId)
        		.append(function)
        		.append(categoryId)
        		.append(groupReference)
        		.hashCode();
    }

	
}
