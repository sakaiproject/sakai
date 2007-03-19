package org.sakaiproject.component.app.scheduler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;

public class ScheduledInvocationManagerImpl implements ScheduledInvocationManager {

	private static final Log LOG = LogFactory.getLog(ScheduledInvocationManagerImpl.class);

	/** Dependency: IdManager */
	protected IdManager m_idManager = null;

	public void setIdManager(IdManager service) {
		m_idManager = service;
	}

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	public void setSqlService(SqlService service) {
		m_sqlService = service;
	}

	public void init() {
		LOG.info("init()");
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#createDelayedInvocation(org.sakaiproject.time.api.Time, java.lang.String, java.lang.String)
	 */
	public String createDelayedInvocation(Time time, String componentId, String opaqueContext) {

		String uuid = m_idManager.createUuid();

		LOG.debug("Creating new Delayed Invocation: " + uuid);
		String sql = "INSERT INTO SCHEDULER_DELAYED_INVOCATION SET INVOCATION_ID = ?, INVOCATION_TIME = ?, COMPONENT = ?, CONTEXT = ?";

		Object[] fields = new Object[4];

		fields[0] = uuid;
		fields[1] = time;
		fields[2] = componentId;
		fields[3] = opaqueContext;

		LOG.debug("SQL: " + sql);
		if (m_sqlService.dbWrite(sql, fields)) {
			LOG.info("Created new Delayed Invocation: uuid=" + uuid);
			return uuid;
		} else {
			LOG.error("Failed to create new Delayed Invocation: componentId=" + componentId + 
					", opaqueContext=" + opaqueContext);
			return null;
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String)
	 */
	public void deleteDelayedInvocation(String uuid) {

		LOG.debug("Removing Delayed Invocation: " + uuid);
		String sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION WHERE INVOCATION_ID = ?";

		Object[] fields = new Object[1];
		fields[0] = uuid;

		LOG.debug("SQL: " + sql);
		m_sqlService.dbWrite(sql, fields);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#deleteDelayedInvocation(java.lang.String, java.lang.String)
	 */
	public void deleteDelayedInvocation(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		//String sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION WHERE COMPONENT = ?, CONTEXT = ?";
		String sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION";

		Object[] fields = new Object[0];
		if (componentId.length() > 0 && opaqueContext.length() > 0) {
			// both non-blank
			sql += " WHERE COMPONENT = ?, CONTEXT = ?";
			fields = new Object[2];
			fields[0] = componentId;
			fields[1] = opaqueContext;
		} else if (componentId.length() > 0) {
			// context blank
			sql += " WHERE COMPONENT = ?";
			fields = new Object[1];
			fields[0] = componentId;
		} else if (opaqueContext.length() > 0) {
			// component blank
			sql += " WHERE CONTEXT = ?";
			fields = new Object[1];
			fields[0] = opaqueContext;
		} else {
			// both blank
		}

		LOG.debug("SQL: " + sql);
		if ( m_sqlService.dbWrite(sql, fields) ) {
			LOG.info("Removed all scheduled invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		} else {
			LOG.error("Failure while attempting to remove invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.api.app.scheduler.ScheduledInvocationManager#findDelayedInvocations(java.lang.String, java.lang.String)
	 */
	public DelayedInvocation[] findDelayedInvocations(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		//String sql = "SELECT * FROM SCHEDULER_DELAYED_INVOCATION WHERE COMPONENT = ?, CONTEXT = ?";
		String sql = "SELECT * FROM SCHEDULER_DELAYED_INVOCATION";

		Object[] fields = new Object[0];
		if (componentId.length() > 0 && opaqueContext.length() > 0) {
			// both non-blank
			sql += " WHERE COMPONENT = ?, CONTEXT = ?";
			fields = new Object[2];
			fields[0] = componentId;
			fields[1] = opaqueContext;
		} else if (componentId.length() > 0) {
			// context blank
			sql += " WHERE COMPONENT = ?";
			fields = new Object[1];
			fields[0] = componentId;
		} else if (opaqueContext.length() > 0) {
			// component blank
			sql += " WHERE CONTEXT = ?";
			fields = new Object[1];
			fields[0] = opaqueContext;
		} else {
			// both blank
		}

		List invocations = m_sqlService.dbRead(sql, fields, new DelayedInvocationReader());
		return (DelayedInvocation[]) invocations.toArray( new DelayedInvocation[] {} );
	}

}
