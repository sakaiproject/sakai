package org.sakaiproject.dash.logic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.LearningResourceStoreService.LRS_Statement;

/************************************************************************
 * Making copies of events
 ************************************************************************/

public class EventCopy implements Event 
{
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	private static Logger logger = LoggerFactory.getLogger(EventCopy.class);

	protected String context;
	protected String eventIdentifier;
	protected Date eventTime;
	protected boolean modify;
	protected int priority;
	protected String entityReference;
	protected String sessionId;
	protected String userId;
	protected LRS_Statement lrsStatement;

	public EventCopy() {
		super();
	}
	
    public EventCopy(Date eventTime, String eventIdentifier, String entityReference, String context, String userId, String sessionId, char eventCode, int priority, LRS_Statement lrsStatement) {
        super();
        this.eventTime= eventTime;
        this.eventIdentifier = eventIdentifier;
        this.entityReference = entityReference;
        this.context = context;
        this.userId = userId;
        this.sessionId = sessionId;
        this.modify = ('m' == eventCode);
        this.lrsStatement = lrsStatement;
    }

	
	public EventCopy(Event original) {
		super();
		this.context = original.getContext();
		this.eventIdentifier = original.getEvent();
		
		try {
			// this.eventTime = original.getEventTime();
			// the getEventTime() method did not exist before kernel 1.2
			// so we use reflection
			Method getEventTimeMethod = original.getClass().getMethod("getEventTime", null);
			this.eventTime = (Date) getEventTimeMethod.invoke(original, null);
		} catch (SecurityException e) {
			logger.warn("Exception trying to get event time: " + e);
		} catch (NoSuchMethodException e) {
			logger.warn("Exception trying to get event time: " + e);
		} catch (IllegalArgumentException e) {
			logger.warn("Exception trying to get event time: " + e);
		} catch (IllegalAccessException e) {
			logger.warn("Exception trying to get event time: " + e);
		} catch (InvocationTargetException e) {
			logger.warn("Exception trying to get event time: " + e);
		}
		if(this.eventTime == null) {
			// If we couldn't get eventTime from event, just use NOW.  That's close enough.
			this.eventTime = new Date();
		}
		
		
		this.modify = original.getModify();
		this.priority = original.getPriority();
		this.entityReference = original.getResource();
		this.sessionId = original.getSessionId();
		this.userId = original.getUserId();
		if(userId == null && sessionId != null) {
			userId = sakaiProxy.getCurrentUserId();
		}
	}
	
	public String getContext() {
		return context;
	}

	public String getEvent() {
		return eventIdentifier;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public boolean getModify() {
		return modify;
	}

	public int getPriority() {
		return priority;
	}

	public String getResource() {
		return entityReference;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getUserId() {
		return userId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EventCopy [context=");
		builder.append(context);
		builder.append(", eventIdentifier=");
		builder.append(eventIdentifier);
		builder.append(", eventTime=");
		builder.append(eventTime);
		builder.append(", modify=");
		builder.append(modify);
		builder.append(", priority=");
		builder.append(priority);
		builder.append(", entityReference=");
		builder.append(entityReference);
		builder.append(", sessionId=");
		builder.append(sessionId);
		builder.append(", userId=");
		builder.append(userId);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public LRS_Statement getLrsStatement() {
		// TODO Auto-generated method stub
		return lrsStatement;
	}
}
