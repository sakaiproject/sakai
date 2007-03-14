package org.sakaiproject.api.app.scheduler;


//this is the worker API that tools implement and actually do the real work
public interface ScheduledInvocationCommand {

	
	public void execute(String opaqueContext);
	
	
}
