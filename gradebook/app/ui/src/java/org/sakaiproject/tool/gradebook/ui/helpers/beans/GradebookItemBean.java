package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import java.util.Map;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.business.GradebookManager;

import uk.org.ponder.beanutil.entity.EntityBeanLocator;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class GradebookItemBean {
	
	private static final String CANCEL = "cancel";
	private static final String ADD_ITEM = "add_item";
	
	
	private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
    	this.messages = messages;
    }
		
    private Map<String, Assignment> OTPMap;
	@SuppressWarnings("unchecked")
	public void setAssignmentEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
		this.OTPMap = entityBeanLocator.getDeliveredBeans();
	}

	private MessageLocator messageLocator;
	public void setMessageLocator (MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	private GradebookManager gradebookManager;
    public void setGradebookManager(GradebookManager gradebookManager) {
    	this.gradebookManager = gradebookManager;
    }
	
	private Long categoryId;
	public void setCategoryId(Long categoryId){
		this.categoryId = categoryId;
	}
	private Long gradebookId;
	public void setGradebookId(Long gradebookId){
		this.gradebookId = gradebookId;
	}
	
	public String processActionAddItem(){
		for (String key : OTPMap.keySet()) {
			Assignment assignment = OTPMap.get(key);
			try {
				if (this.categoryId != null){
					gradebookManager.createAssignmentForCategory(this.gradebookId, this.categoryId, assignment.getName(), 
							assignment.getPointsPossible(), assignment.getDueDate(), assignment.isCounted(), assignment.isReleased());
				} else {
					gradebookManager.createAssignment(this.gradebookId, assignment.getName(), assignment.getPointsPossible(), 
							assignment.getDueDate(), assignment.isCounted(), assignment.isReleased());
				}
				messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.successful",
						new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_INFO));
			} catch (ConflictingAssignmentNameException e){
				messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.conflicting_name",
						new Object[] {assignment.getName() }, "Assignment." + key + ".name"));
			}
		}
		return ADD_ITEM;
	}
	
	public String processActionCancel(){
		
		return CANCEL;
	}
	
	public Assignment getAssignmentById(Long assignmentId){
		return gradebookManager.getAssignment(assignmentId);
	}
}