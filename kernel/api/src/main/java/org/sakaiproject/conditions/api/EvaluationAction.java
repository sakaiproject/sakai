package org.sakaiproject.conditions.api;

import org.sakaiproject.event.api.Event;

public interface EvaluationAction {
	
	public void execute(Event e, boolean evalResult) throws Exception;

}
