package org.sakaiproject.component.app.scheduler.jobs;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.component.app.scheduler.DelayedInvocationReader;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;

public class ScheduledInvocationRunner implements Job {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationRunner.class);

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	public void setSqlService(SqlService service) {
		m_sqlService = service;
	}

	/** Dependency: TimeService */
	protected TimeService m_timeService = null;

	public void setTimeService(TimeService service) {
		m_timeService = service;
	}

	/* (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {

		Time now = m_timeService.newTime();
		String sql = "SELECT INVOCATION_ID, INVOCATION_TIME, COMPONENT, CONTEXT FROM SCHEDULER_DELAYED_INVOCATION WHERE INVOCATION_TIME < ?";

		Object[] fields = new Object[1];

		fields[0] = now;

		LOG.debug("SQL: " + sql + " NOW:" + now);
		List invocations = m_sqlService.dbRead(sql, fields, new DelayedInvocationReader());

		for (Iterator i = invocations.iterator(); i.hasNext();) {

			DelayedInvocation invocation = (DelayedInvocation) i.next();

			if (invocation != null) {

				LOG.debug("processing invocation: [" + invocation + "]");

				try {
					ScheduledInvocationCommand command = (ScheduledInvocationCommand) ComponentManager.get(invocation.componentId);
					command.execute(invocation.contextId);
				} catch (Exception e) {
					LOG.error("Failed to execute component: [" + invocation.componentId + "]: " + e);
				} finally {
					sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION WHERE INVOCATION_ID = ?";

					fields[0] = invocation.uuid;

					LOG.debug("SQL: " + sql);
					m_sqlService.dbWrite(sql, fields);
				}
			}
		}
	}

}
