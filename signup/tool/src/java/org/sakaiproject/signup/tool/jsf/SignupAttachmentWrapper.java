package org.sakaiproject.signup.tool.jsf;

import java.util.ArrayList;
import java.util.List;
import org.sakaiproject.signup.model.SignupAttachment;

public class SignupAttachmentWrapper {
	
	private List<SignupAttachment> attendeeAttachments = new ArrayList<SignupAttachment>();
	
	private List<SignupAttachment> eventMainAttachments = new ArrayList<SignupAttachment>();
	
	public SignupAttachmentWrapper(List<SignupAttachment> attachments){
		if(attachments != null){
			for (SignupAttachment attach: attachments) {
				if(attach.getTimeslotId() !=null && ! attach.getViewByAll())
					attendeeAttachments.add(attach);
				else if( attach.getViewByAll())
					eventMainAttachments.add(attach);
				
				//TODO other cases: such as attachment for a specific time slot only.
			}
		}
	}

	public List<SignupAttachment> getAttendeeAttachments() {
		return attendeeAttachments;
	}

	public void setAttendeeAttachments(List<SignupAttachment> attendeeAttachments) {
		this.attendeeAttachments = attendeeAttachments;
	}

	public List<SignupAttachment> getEventMainAttachments() {
		return eventMainAttachments;
	}

	public void setEventMainAttachments(List<SignupAttachment> eventMainAttachments) {
		this.eventMainAttachments = eventMainAttachments;
	}
	

}
