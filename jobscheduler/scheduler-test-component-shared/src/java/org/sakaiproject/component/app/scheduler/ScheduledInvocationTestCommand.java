package org.sakaiproject.component.app.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public class ScheduledInvocationTestCommand implements
		ScheduledInvocationCommand {

	private static final Logger LOG = LoggerFactory.getLogger(ScheduledInvocationTestCommand.class);
	
	public void execute(String opaqueContext) {
		
		LOG.info("Command executed!  Context: "+opaqueContext);

	}

}
