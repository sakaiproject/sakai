package org.sakaiproject.assignment.api.model;

/**
 * the access string(role id or user id) for AssignmentAllPurposeItem
 * @author zqian
 *
 */
public class AssignmentAllPurposeItemAccess {
	private Long id;
	private String access;
	private AssignmentAllPurposeItem assignmentAllPurposeItem;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAccess() {
		return access;
	}
	public void setAccess(String access) {
		this.access = access;
	}
	public AssignmentAllPurposeItem getAssignmentAllPurposeItem()
	{
		return this.assignmentAllPurposeItem;
	}
	public void setAssignmentAllPurposeItem(AssignmentAllPurposeItem assignmentAllPurposeItem)
	{
		this.assignmentAllPurposeItem = assignmentAllPurposeItem;
	}
	
	public AssignmentAllPurposeItemAccess() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
