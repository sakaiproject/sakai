package org.sakaiproject.sitestats.tool.bean;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public class MessageHandler {
	
	public void addMessage(String id, FacesMessage fm) {
        FacesContext.getCurrentInstance().addMessage(id, fm);
    }
	
	public void addInfoMessage(String id, String messageText) {
        FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_INFO, messageText, null));
    }
	
	public void addFatalMessage(String id, String messageText) {
        FacesContext.getCurrentInstance().addMessage(id, new FacesMessage(FacesMessage.SEVERITY_FATAL, messageText, null));
    }
	
}
