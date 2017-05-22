package org.sakaiproject.tool.assessment.facade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;


public class EventLogFacade
implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;
	private Logger log = LoggerFactory.getLogger(EventLogFacade.class);

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
