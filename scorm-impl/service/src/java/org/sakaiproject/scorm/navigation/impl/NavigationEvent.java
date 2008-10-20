package org.sakaiproject.scorm.navigation.impl;

import org.sakaiproject.scorm.navigation.INavigationEvent;

public class NavigationEvent implements INavigationEvent {

	private int event;
	private String choiceEvent;
	
	public NavigationEvent() {
	}
	
	public String getChoiceEvent() {
		return choiceEvent;
	}
	
	public void setChoiceEvent(String choiceEvent) {
		this.choiceEvent = choiceEvent;
	}

	public int getEvent() {
		return event;
	}
	
	public void setEvent(int event) {
		this.event = event;
	}

	public boolean isChoiceEvent() {
		return event == -1;
	}

}
