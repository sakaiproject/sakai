package org.sakaiproject.conditions.impl;

import org.sakaiproject.conditions.api.EvaluationAction;
import org.sakaiproject.event.api.Event;

public class ToyCommand implements EvaluationAction{
	
	public void execute(Event e, boolean evalResult) throws Exception {
		ToyMessagePad.messages.add("I've been hit!");
		ToyMessagePad.messages.add(e.getResource());
		
	}

}
