package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import java.util.Map;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.AuthorizationFailedProducer;

import uk.org.ponder.beanutil.entity.EntityBeanLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class GradebookItemBean {
	
	private static final String CANCEL = "cancel";
	private static final String SUBMIT = "submit";
	private static final String FAILURE = "failure";
	
	public static final Long CATEGORY_UNASSIGNED = -1L;
	
	public Boolean requireDueDate = false;
	
	private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
    	this.messages = messages;
    }
		
    private Map<String, Assignment> OTPMap;
    private EntityBeanLocator assignmentEntityBeanLocator;
	@SuppressWarnings("unchecked")
	public void setAssignmentEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
		this.OTPMap = entityBeanLocator.getDeliveredBeans();
		this.assignmentEntityBeanLocator = entityBeanLocator;
	}
	
	private GradebookManager gradebookManager;
    public void setGradebookManager(GradebookManager gradebookManager) {
    	this.gradebookManager = gradebookManager;
    }
    
    private GradebookService gradebookService;
    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }
	
	private Long categoryId;
	public void setCategoryId(Long categoryId){
		this.categoryId = categoryId;
	}
	private Long gradebookId;
	public void setGradebookId(Long gradebookId){
		this.gradebookId = gradebookId;
	}
	private Boolean counted = Boolean.FALSE;
	public void setCounted(Boolean counted) {
		this.counted = counted;
	}
	
	public String processActionAddItem(){
		Boolean errorFound = Boolean.FALSE;
		
		Gradebook gradebook = gradebookManager.getGradebook(this.gradebookId);
		
		if (!gradebookService.currentUserHasEditPerm(gradebook.getUid())) {
		    return AuthorizationFailedProducer.VIEW_ID;
		}
		
		for (String key : OTPMap.keySet()) {
			Assignment assignment = OTPMap.get(key);
			assignment.setNotCounted(!counted);	
			
			//check for null name
			if (assignment.getName() == null || assignment.getName().equals("")) {
				messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.null_name"));
				errorFound = Boolean.TRUE;
			}
			
			//check for null points
			if (assignment.getPointsPossible() == null ||
			        assignment.getPointsPossible().doubleValue() <= 0) {
			    if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
			        messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.invalid_rel_weight"));
			    } else {
		             messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.invalid_points"));
			    }
				errorFound = Boolean.TRUE;
			}
			
			// check for more than 2 decimal places
			if (assignment.getPointsPossible() != null) {
			    String pointsAsString = assignment.getPointsPossible().toString();
			    String[] decimalSplit = pointsAsString.split("\\.");
			    if (decimalSplit.length == 2) {
			        String decimal = decimalSplit[1];
			        if (decimal != null && decimal.length() > 2) {
			            messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.invalid_decimal"));
			            errorFound = Boolean.TRUE;
			        }
			    }
			}
			
			if (this.requireDueDate == null || this.requireDueDate == Boolean.FALSE) {
				assignment.setDueDate(null);				
			}
			
			if (assignment.isCounted() && !assignment.isReleased()) {
			    messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.counted_not_released"));
                errorFound = Boolean.TRUE;
			}
				
			if (errorFound) {
				return FAILURE;
			}
			
			if (key.equals(EntityBeanLocator.NEW_PREFIX + "1")){
				//We have a new assignment object
				Long id = null;
				try {
					if (this.categoryId != null && this.categoryId != CATEGORY_UNASSIGNED){
						id = gradebookManager.createAssignmentForCategory(this.gradebookId, this.categoryId, assignment.getName(), 
								assignment.getPointsPossible(), assignment.getDueDate(), assignment.isCounted(), assignment.isReleased());
					} else {
						id = gradebookManager.createAssignment(this.gradebookId, assignment.getName(), assignment.getPointsPossible(), 
								assignment.getDueDate(), assignment.isCounted(), assignment.isReleased());
					}
					assignment.setId(id);
					//new UIELBinding("Assignment." + key + ".id", id);
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.successful",
							new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_INFO));
				} catch (ConflictingAssignmentNameException e){
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.conflicting_name",
							new Object[] {assignment.getName() }, "Assignment." + key + ".name"));
					errorFound = Boolean.TRUE;
				}
			} else {
				//we are editing an existing object
				try {
				    if (this.categoryId != null && !this.categoryId.equals(CATEGORY_UNASSIGNED)) {
				        // we need to retrieve the category and add it to the
				        // assignment
				        Category cat = gradebookManager.getCategory(categoryId);
				        if (cat != null) {
				            assignment.setCategory(cat);
				        }
				    } else {
				        // this assignment does not have a category
				        assignment.setCategory(null);
				    }
				    
					gradebookManager.updateAssignment(assignment);
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.successful",
							new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_INFO));
				} catch (ConflictingAssignmentNameException e){
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.conflicting_name",
							new Object[] {assignment.getName() }, "Assignment." + key + ".name"));
					errorFound = Boolean.TRUE;
				}
			}
		}
		if(errorFound) {
			return FAILURE;
		}
		
		return SUBMIT;
	}
	
	public String processActionCancel(){
		
		return CANCEL;
	}
	
	/**
	 * this is the fetchMethod for the EntityBeanLocator
	 * @param assignmentId
	 * @return
	 */
	public Assignment getAssignmentById(Long assignmentId){
		return gradebookManager.getAssignment(assignmentId);
	}
}