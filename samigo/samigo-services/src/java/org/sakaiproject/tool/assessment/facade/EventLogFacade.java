package org.sakaiproject.tool.assessment.facade;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;


public class EventLogFacade
implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(EventLogFacade.class);

	private EventLogData data;
	
	public EventLogFacade() {
	}
	
	public EventLogFacade(EventLogData data) {
		this.data = data;
	}
	
	public EventLogData getData() {
		return data;
	}
	
	public void setData(EventLogData data) {
		this.data = data;
	}
}