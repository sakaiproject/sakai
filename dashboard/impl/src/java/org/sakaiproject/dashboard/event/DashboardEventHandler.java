/**
 * 
 */
package org.sakaiproject.dashboard.event;

import java.util.Collection;
import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.dashboard.logic.DashboardLogic;
import org.sakaiproject.dashboard.model.DashboardItem;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventHandler;
import org.sakaiproject.event.api.EventHandlerRegistry;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * 
 *
 */
public class DashboardEventHandler implements EventHandler {
	
	private Log log = LogFactory.getLog(DashboardEventHandler.class);
	
	protected EventHandlerRegistry eventHandlerRegistry;
	public void setEventHandlerRegistry(EventHandlerRegistry eventHandlerRegistry) {
		this.eventHandlerRegistry = eventHandlerRegistry;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	protected Set<String> createEvents = new TreeSet<String>();
	protected Set<String> modifyEvents = new TreeSet<String>();
	protected Set<String> deleteEvents = new TreeSet<String>();
	
	protected Queue<Event> eventQueue = new ConcurrentLinkedQueue<Event>(); 
	
	protected EventProcessor eventProcessor;
	
	protected Object waitLock = new Object();
	
	public void init() {
		log.info("init()");
		
		this.eventHandlerRegistry.registerEventHandler(this);
		
		loadEventLists();
		
	}
	
	public void destroy() {
		// TODO: stop the eventProcessor?
	}
	
	@Override
	public boolean handleEvent(Event event) {
		this.filter(event);
		
		if(this.eventProcessor == null) {
			this.eventProcessor = new EventProcessor("Dashboard Event Processor");
			this.eventProcessor.setDaemon(true);
			this.eventProcessor.run();
		}
		return false;
	}

	@Override
	public int getPriority() {
		return EventHandler.PRIORITY_MEDIUM;
	}

	protected void filter(Event event) {
		if(event.getModify()) {
			if(this.createEvents.contains(event.getEvent()) || this.modifyEvents.contains(event.getEvent()) || this.deleteEvents.contains(event.getEvent())) {
				Event copy = new EventCopy(event);
				if(log.isInfoEnabled()) {
					log.info("adding event to queue: " + copy);
				}
				this.eventQueue.add(copy);
			}
		}
	}
	
	protected void loadEventLists() {
		this.createEvents.add("content.new");
		this.createEvents.add("asn.new.assignment");
		this.createEvents.add("calendar.new");
		this.createEvents.add("annc.new");
		
		
		this.modifyEvents.add("content.revise");
		this.modifyEvents.add("asn.revise.assignment");
		this.modifyEvents.add("calendar.revise");
		this.modifyEvents.add("annc.revise.any");
		this.modifyEvents.add("annc.revise.own");
		
		this.deleteEvents.add("content.delete");
		
	}
	
	/*
	 *
	 * 
	 */

	public class EventProcessor extends Thread {

		private static final long TIME_TO_WAIT = 2L * 1000L;

		public EventProcessor(String name) {
			super(name);
		}

		@Override
		public void run() {
			while(true) {
				Event event = null;
				synchronized(eventQueue) {
					event = eventQueue.poll();
				}
				if(log.isInfoEnabled()) {
					log.info("event == " + event);
				}
				if(event == null) {
					// wait and try again 
					try {
						synchronized(waitLock) {
							waitLock.wait(TIME_TO_WAIT);
						}
					} catch (InterruptedException e) {
						log.info("InterruptedException " + e);
					}
				} else {
					if(log.isInfoEnabled()) {
						log.info("processing event: " + event);
					}
					
					// process the event
					dashboardLogic.postDashboardItems(event.getEvent(), event.getEventTime(), event.getContext(), event.getResource());
				}
				
			}
			
		}
		
	}

	public class EventCopy implements Event {

		private Date eventTime;
		private int priority;
		private boolean modify;
		private String userId;
		private String sessionId;
		private String context;
		private String resource;
		private String event;
		
		public EventCopy(Event other) {
			this.context = other.getContext();
			this.event = other.getEvent();
			this.eventTime = new Date(other.getEventTime().getTime());
			this.modify = other.getModify();
			this.priority = other.getPriority();
			this.resource = other.getResource();
			this.sessionId = other.getSessionId();
			this.userId = other.getUserId();
		}

		@Override
		public String getEvent() {
			return event;
		}

		@Override
		public String getResource() {
			return resource;
		}

		@Override
		public String getContext() {
			return context;
		}

		@Override
		public String getSessionId() {
			return sessionId;
		}

		@Override
		public String getUserId() {
			return userId;
		}

		@Override
		public boolean getModify() {
			return modify;
		}

		@Override
		public int getPriority() {
			return priority;
		}

		@Override
		public Date getEventTime() {
			return eventTime;
		}
		
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append(super.toString());
			buf.append(" event: ");
			buf.append(this.event);
			buf.append(" eventTime: ");
			buf.append(this.eventTime);
			buf.append(" context: ");
			buf.append(this.context);
			buf.append(" resource: ");
			buf.append(this.resource);
			buf.append(" sessionId: ");
			buf.append(this.sessionId);
			buf.append(" userId: ");
			buf.append(this.userId);
			buf.append(" modify: ");
			buf.append(this.modify);
			buf.append(" priority: ");
			buf.append(this.priority);
			return buf.toString();
		}
	}

}
