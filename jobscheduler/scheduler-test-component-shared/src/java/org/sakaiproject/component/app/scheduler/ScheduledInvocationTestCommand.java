package org.sakaiproject.component.app.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;

public class ScheduledInvocationTestCommand implements
		ScheduledInvocationCommand {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationTestCommand.class);
	
	public void execute(String opaqueContext) {
		
		LOG.info("Command executed!  Context: "+opaqueContext);

	}

}
