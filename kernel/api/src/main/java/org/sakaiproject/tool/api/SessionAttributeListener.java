package org.sakaiproject.tool.api;

public interface SessionAttributeListener {

	public void attributeAdded(SessionBindingEvent se);
	public void attributeRemoved(SessionBindingEvent se);
	
}
