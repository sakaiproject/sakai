package org.sakaiproject.api.app.scheduler;

import org.sakaiproject.time.api.Time;


public interface ScheduledInvocationManager {
	
	/**
	date - the date and time the method will be invoked
	componentId - the unique name of a bean in the bean factory which implements command pattern 
				DelayedInvocationCommand
	opaqueContext - the key which the tool can use to uniquely identify some entity when invoked; i.e. 
				the context
	returns - Unique receipt for future invocation
	*/
	String createDelayedInvocation(Time time, String componentId, String opaqueContext);

	void deleteDelayedInvocation(String uuid);

}
