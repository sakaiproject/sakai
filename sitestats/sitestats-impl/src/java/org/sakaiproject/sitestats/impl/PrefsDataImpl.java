package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;


public class PrefsDataImpl implements PrefsData, Serializable {
	private static final long		serialVersionUID					= 1L;

	/** Stats Manager object */
	private transient StatsManager	sm									= (StatsManager) ComponentManager.get(StatsManager.class.getName());

	private List<ToolInfo>			toolEventsDef						= new ArrayList<ToolInfo>();
	private boolean					listToolEventsOnlyAvailableInSite	= true;
	private boolean					chartIn3D							= sm.isChartIn3D();
	private float					chartTransparency					= sm.getChartTransparency();
	private boolean					itemLabelsVisible					= sm.isItemLabelsVisible();
	
	
	public PrefsDataImpl(){
		toolEventsDef = new ArrayList<ToolInfo>();
		listToolEventsOnlyAvailableInSite = true;
		
	}

	public List<ToolInfo> getToolEventsDef() {
		return toolEventsDef;
	}

	public List<String> getToolEventsStringList() {
		List<String> toolEventsStringList = new ArrayList<String>();
		Iterator<ToolInfo> iT = getToolEventsDef().iterator();
		while(iT.hasNext()){
			ToolInfo t = iT.next();
			if(t.isSelected()){
				Iterator<EventInfo> iE = t.getEvents().iterator();
				while(iE.hasNext()){
					EventInfo e = iE.next();
					if(e.isSelected())
						toolEventsStringList.add(e.getEventId());
				}
			}
		}
		return toolEventsStringList;
	}

	public void setToolEventsDef(List<ToolInfo> toolEventsDef) {
		this.toolEventsDef = toolEventsDef;
	}
	
	public boolean isListToolEventsOnlyAvailableInSite(){
		return listToolEventsOnlyAvailableInSite;
	}
	
	public void setListToolEventsOnlyAvailableInSite(boolean listToolEventsOnlyAvailableInSite){
		this.listToolEventsOnlyAvailableInSite = listToolEventsOnlyAvailableInSite;
	}
	
	public void setChartIn3D(boolean value){
		this.chartIn3D = value;
	}
	
	public boolean isChartIn3D() {
		return chartIn3D;
	}
	
	public void setChartTransparency(float value){
		this.chartTransparency = value;
	}
	
	public void setChartTransparency(String value){
		this.chartTransparency = Float.parseFloat(value);
	}
	
	public float getChartTransparency() {
		return chartTransparency;
	}
	
	public void setItemLabelsVisible(boolean itemLabelsVisible) {
		this.itemLabelsVisible = itemLabelsVisible;
	}
	
	public boolean isItemLabelsVisible() {
		return itemLabelsVisible;
	}

	public String toXmlPrefs() {
		StringBuffer buff = new StringBuffer();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		
		buff.append("<prefs ");
		buff.append(" listToolEventsOnlyAvailableInSite=\""+ isListToolEventsOnlyAvailableInSite() +"\" ");
		buff.append(" chartIn3D=\""+ isChartIn3D() +"\" ");
		buff.append(" chartTransparency=\""+ getChartTransparency() +"\" ");
		buff.append(" itemLabelsVisible=\""+ isItemLabelsVisible() +"\" ");
		buff.append(">");		

		buff.append("	<toolEventsDef>");
		Iterator<ToolInfo> iT = getToolEventsDef().iterator();
		while(iT.hasNext()){
			ToolInfo t = iT.next();
			buff.append("		<tool toolId=\""+ t.getToolId() +"\" selected=\""+ t.isSelected() +"\">");
			Iterator<EventInfo> iE = t.getEvents().iterator();
			while(iE.hasNext()){
				EventInfo e = iE.next();
				buff.append("			<event eventId=\""+ e.getEventId() +"\" selected=\""+ e.isSelected() +"\" />");
			}
			buff.append("		</tool>");
		}
		buff.append("	</toolEventsDef>");
		
		buff.append("</prefs>");		
		return buff.toString();
	}
	
}
