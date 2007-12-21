package org.sakaiproject.scorm.navigation;

public interface INavigationEvent {

	public int getEvent();
	
	public void setEvent(int event);
	
	public String getChoiceEvent();
	
	public void setChoiceEvent(String choiceEvent);
	
	public boolean isChoiceEvent();
	
}
