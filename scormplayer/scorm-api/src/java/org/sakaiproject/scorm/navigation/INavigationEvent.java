package org.sakaiproject.scorm.navigation;

public interface INavigationEvent {

	public String getChoiceEvent();

	public int getEvent();

	public boolean isChoiceEvent();

	public void setChoiceEvent(String choiceEvent);

	public void setEvent(int event);

}
