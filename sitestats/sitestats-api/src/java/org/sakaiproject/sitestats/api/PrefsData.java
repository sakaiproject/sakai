/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;

public class PrefsData implements Serializable {
	private static final long		serialVersionUID					= 1L;

	private List<ToolInfo>			toolEventsDef						= null;
	private boolean					useAllTools							= false;
	private boolean					listToolEventsOnlyAvailableInSite	= true;
	private boolean					chartIn3D							= false;
	private float					chartTransparency					= 1.0f;
	private boolean					itemLabelsVisible					= true;
	
	
	public PrefsData(){
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
	
	public void setUseAllTools(boolean value) {
		this.useAllTools = value;
	}
	
	public boolean isUseAllTools() {
		return useAllTools;
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
		buff.append(" useAllTools=\""+ isUseAllTools() +"\" ");
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

/*public interface PrefsData {
	
	public List<ToolInfo> getToolEventsDef();	
	public void setToolEventsDef(List<ToolInfo> toolEventsDef);
	public List<String> getToolEventsStringList();
	
	public boolean isListToolEventsOnlyAvailableInSite();
	public void setListToolEventsOnlyAvailableInSite(boolean listToolEventsOnlyAvailableInSite);
	
	public void setChartIn3D(boolean value);	
	public boolean isChartIn3D();
	
	public void setUseAllTools(boolean value);	
	public boolean isUseAllTools();
	
	public void setChartTransparency(float value);
	public void setChartTransparency(String value);
	public float getChartTransparency();
	
	public void setItemLabelsVisible(boolean itemLabelsVisible);	
	public boolean isItemLabelsVisible();
	
	public String toXmlPrefs();
}*/
