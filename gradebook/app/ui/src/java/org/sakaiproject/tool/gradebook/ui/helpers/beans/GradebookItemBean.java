package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import java.util.Map;

//import org.sakaiproject.assignment2.logic.AssignmentLogic;
//import org.sakaiproject.assignment2.logic.ExternalLogic;
//import org.sakaiproject.assignment2.logic.ExternalGradebookLogic;
//import org.sakaiproject.assignment2.model.Assignment2;

import uk.org.ponder.beanutil.entity.EntityBeanLocator;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;

public class GradebookItemBean {
	
	private static final String CANCEL = "cancel";
	private static final String ADD_ITEM = "add_item";
	
	
	private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
    	this.messages = messages;
    }
		
/**
    private Map<String, Assignment2> OTPMap;
	@SuppressWarnings("unchecked")
	public void setEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
		this.OTPMap = entityBeanLocator.getDeliveredBeans();
	}
**/
/**	
	private ExternalLogic externalGradebookLogic;
	public void setExternalGradebookLogic(ExternalGradebookLogic externalGradebookLogic) {
		this.externalGradebookLogic = externalGradebookLogic;
	}
**/
	private MessageLocator messageLocator;
	public void setMessageLocator (MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	public String processActionAddItem(){
		
		return ADD_ITEM;
	}
	
	public String processActionCancel(){
		
		return CANCEL;
	}
}