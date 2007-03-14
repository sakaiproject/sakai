package org.sakaiproject.component.app.scheduler;

import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.time.api.Time;

public class ScheduledInvocationManagerImpl implements
		ScheduledInvocationManager {

	public String createDelayedInvocation(Time time, String componentId,
			String opaqueContext) {
		String uuid = null;
		// String uuid = IdManager.generateGUID();
		// DbWrite(INSERT INTO JOBS VALUES(uuid, TIME, componentId, opaqueContext);
		
		return uuid;

	}

	public void deleteDelayedInvocation(String uuid) {
		// TODO Auto-generated method stub

	}

}
