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
package org.sakaiproject.sitestats.api.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.sakaiproject.sitestats.api.parser.EventParserTip;

public class ToolInfo implements Serializable, Cloneable {
	private static final long			serialVersionUID	= 1L;

	@Getter @Setter private String			toolId;
	@Getter @Setter private List<String>	additionalToolIds;
	@Getter @Setter private boolean			selected;
	@Getter			private final List<EventParserTip> eventParserTips;
	private List<EventInfo>	eventInfos;
	
	public ToolInfo(String toolId) {
		this.toolId = toolId;
		eventInfos = new ArrayList<>();
		eventParserTips = new ArrayList<>(3);
	}

	public ToolInfo(String toolId, List<String> additionalToolIds) {
		this.toolId = toolId;
		this.additionalToolIds = additionalToolIds;
		eventInfos = new ArrayList<>();
		eventParserTips = new ArrayList<>(3);
	}

	public ToolInfo(ToolInfo tool) {
		toolId = tool.toolId;
		additionalToolIds = tool.additionalToolIds != null ? new ArrayList<>(tool.additionalToolIds) : null;
		eventInfos = new ArrayList<>(tool.eventInfos.size());
		for (EventInfo info : tool.eventInfos) {
			eventInfos.add(info.clone());
		}
		selected = tool.selected;
		eventParserTips = new ArrayList<>(tool.eventParserTips.size());
		for (EventParserTip tip : tool.eventParserTips) {
			eventParserTips.add(tip.clone());
		}
	}

	@Override
	public ToolInfo clone() {
		return new ToolInfo(this);
	}

	public List<EventInfo> getEvents() {
		return eventInfos;
	}

	public void setEvents(List<EventInfo> eventInfos) {
		this.eventInfos = eventInfos;
	}
	
	public void addEvent(EventInfo eventInfo){
		eventInfos.add(eventInfo);
	}
	
	public void removeEvent(EventInfo eventInfo) {
		eventInfos.remove(eventInfo);
	}

	public void setAdditionalToolIdsStr(String ids) {
		if(ids != null) {
			this.additionalToolIds = new ArrayList<String>();
			String[] _ids = ids.split(",");
			for(int i=0; i<_ids.length; i++)
				this.additionalToolIds.add(_ids[i].trim());
		}
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 == null || !(arg0 instanceof ToolInfo))
			return false;
		else {
			ToolInfo other = (ToolInfo) arg0;
			return getToolId().equals(other.getToolId());
		}
	}
	
	@Override
	public int hashCode() {
		return getToolId().hashCode();
	}

	public void addEventParserTip(EventParserTip tip) {
		eventParserTips.add(tip);
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("ToolInfo: "+getToolId()+" ["+isSelected()+"]");
		if(additionalToolIds != null) {
			Iterator<String> i = additionalToolIds.iterator();
			while(i.hasNext())
				buff.append("/" + i.next());
		}
		buff.append("\n");
		Iterator<EventInfo> iE = getEvents().iterator();
		while(iE.hasNext()){
			EventInfo e = iE.next();
			buff.append(e.toString());
		}
		return buff.toString();
	}
}
