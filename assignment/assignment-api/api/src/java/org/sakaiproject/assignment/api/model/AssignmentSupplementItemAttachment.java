package org.sakaiproject.assignment.api.model;

/**
 * the attachment for the AssigmentSupplementItem object
 * @author zqian
 *
 */
public class AssignmentSupplementItemAttachment {
	private Long id;
	private String attachmentId;
	private AssignmentSupplementItemWithAttachment assignmentSupplementItemWithAttachment;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAttachmentId() {
		return attachmentId;
	}
	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}
	public AssignmentSupplementItemWithAttachment getAssignmentSupplementItemWithAttachment()
	{
		return this.assignmentSupplementItemWithAttachment;
	}
	public void setAssignmentSupplementItemWithAttachment(AssignmentSupplementItemWithAttachment assignmentSupplementItemWithAttachment)
	{
		this.assignmentSupplementItemWithAttachment = assignmentSupplementItemWithAttachment;
	}
	

}
