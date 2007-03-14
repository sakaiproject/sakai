package org.sakaiproject.component.app.scheduler;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;

public class ScheduledInvocationManagerImpl implements
		ScheduledInvocationManager {
	
	
	private static final Log LOG = LogFactory.getLog(ScheduledInvocationManagerImpl.class);
	
	/** Dependency: IdManager */
	protected IdManager m_idManager = null;
	
	public void setIdManager(IdManager service)
	{
		m_idManager = service;
	}
	
	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;
	
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}
	
	public void init() {
		
		LOG.info("init()");
		
	}
	
	public String createDelayedInvocation(Time time, String componentId,
			String opaqueContext) {
		
		String[] fields = null;
		
		String uuid = m_idManager.createUuid();
		
		LOG.info("Creating new Delayed Invocation: " + uuid);
		String sql = "INSERT INTO SCHEDULER_DELAYED_INVOCATION SET INVOCATION_ID = ?, INVOCATION_TIME = ?, COMPONENT_ID = ?, CONTEXT = ?";
		
		fields[0]=uuid;
		//TODO: FORMAT DATE
		fields[1]=time.toString();
		fields[2]=componentId;
		fields[3]=opaqueContext;
		
		LOG.info("SQL: "+sql);
		if(m_sqlService.dbWrite(sql, fields)) {
		 
		  return uuid;
		
		} else {
		
		  return null;
		  
		}

	}

	public void deleteDelayedInvocation(String uuid) {
		
		String[] fields = null;
		
		LOG.info("Removing Delayed Invocation: " + uuid);
		String sql = "DELETE FROM SCHEDULER_DELAYED_INVOCATION WHERE INVOCATION_ID = ?";
		
		fields[0]=uuid;
		
		LOG.info("SQL: "+sql);
		m_sqlService.dbWrite(sql, fields);

	}

}
