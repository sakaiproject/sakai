package org.sakaiproject.sitestats.api.event;

import java.util.List;

import org.sakaiproject.sitestats.api.event.parser.EventParserTip;


public interface ToolInfo {

	public List<EventInfo> getEvents();

	public void setEvents(List<EventInfo> eventInfos);
	
	public void addEvent(EventInfo eventInfo);
	
	public void removeEvent(EventInfo eventInfo);

	public boolean isSelected();

	public void setSelected(boolean selected);

	public String getToolId();

	public void setToolId(String toolId);

	public List<String> getAdditionalToolIds();

	public void setAdditionalToolIds(List<String> toolIds);

	public void setAdditionalToolIdsStr(String toolIds);

	public String getToolName();

	public void setToolName(String toolName);
	
	public EventParserTip getEventParserTip();
	
	public void setEventParserTip(EventParserTip eventParserTip);

}