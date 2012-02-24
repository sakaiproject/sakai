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
package org.sakaiproject.sitestats.impl.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.tool.api.ToolManager;

public class EventUtil {

	/**
	 * Add missing events not in list (eg, list from Preferences) but present on the full Event Registry.
	 * @param list n Event Registry subset
	 * @return The Event Registry with additional tool ids.
	 */
	public static List<ToolInfo> addMissingAdditionalToolIds(List<ToolInfo> eventRegistrySubset, List<ToolInfo> fullEventRegistry) {
		Iterator<ToolInfo> i = eventRegistrySubset.iterator();
		while (i.hasNext()){
			ToolInfo t = i.next();
			int ix = fullEventRegistry.indexOf(new ToolInfo(t.getToolId()));
			if(ix != -1)
				t.setAdditionalToolIds(fullEventRegistry.get(ix).getAdditionalToolIds());
		}
		return eventRegistrySubset;
	}

	/**
	 * Intersect an event registry subset with tools available in site.
	 * @param eventRegistrySubset An Event Registry subset to intersect with. 
	 * @param siteId The id of the site.
	 * @return The Event Registry containing events for tools present in site.
	 */
	public static List<ToolInfo> getIntersectionWithAvailableToolsInSite(SiteService M_ss, List<ToolInfo> eventRegistrySubset, String siteId) {
		List<ToolInfo> intersected = new ArrayList<ToolInfo>();
		Site site = null;
		try{
			site = M_ss.getSite(siteId);
		}catch(IdUnusedException e){
			return eventRegistrySubset;
		}
	
		// search the pages
		List<ToolConfiguration> siteTools = new ArrayList<ToolConfiguration>();
		for(Iterator<SitePage> iPages = site.getPages().iterator(); iPages.hasNext();){
			SitePage page = iPages.next();
			siteTools.addAll(page.getTools());
		}
	
		// add only tools in both lists
		Iterator<ToolInfo> iTED = eventRegistrySubset.iterator();
		while (iTED.hasNext()){
			ToolInfo t = iTED.next();
			Iterator<ToolConfiguration> iST = siteTools.iterator();
			while (iST.hasNext()){
				ToolConfiguration tc = iST.next();
				if(tc.getToolId().equals(t.getToolId())){
					intersected.add(t);
					break;
				}
			}
		}
	
		return intersected;
	}

	/**
	 * Intersect an event registry with tools available in (whole) Sakai installation.
	 * @param eventRegistrySubset The Event Registry to intersect with. 
	 * @param siteId The id of the site.
	 * @return The Event Registry containing events for tools present in (whole) Sakai installation.
	 */
	public static List<ToolInfo> getIntersectionWithAvailableToolsInSakaiInstallation(ToolManager M_tm, List<ToolInfo> eventRegistrySubset) {
		List<ToolInfo> intersected = new ArrayList<ToolInfo>();
	
		// search the pages
		List<org.sakaiproject.tool.api.Tool> sakaiTools = new ArrayList<org.sakaiproject.tool.api.Tool>();
		sakaiTools.addAll(M_tm.findTools(null, null));
	
		// add only tools in both lists
		Iterator<ToolInfo> iTED = eventRegistrySubset.iterator();
		while (iTED.hasNext()){
			ToolInfo t = iTED.next();
			Iterator<org.sakaiproject.tool.api.Tool> iST = sakaiTools.iterator();
			while (iST.hasNext()){
				org.sakaiproject.tool.api.Tool tc = iST.next();
				if(tc.getId().equals(t.getToolId())){
					intersected.add(t);
					break;
				}
			}
		}
	
		return intersected;
	}

	/**
	 * Union between a given event registry with the full Event Registry.
	 * @param eventRegistrySubset The Event Registry to union with. 
	 * @param fullEventRegistry The full Event Registry. 
	 * @param siteId The id of the site.
	 * @return The Event Registry containing events for tools present in (whole) Sakai installation.
	 */
	public static List<ToolInfo> getUnionWithAllDefaultToolEvents(List<ToolInfo> eventRegistrySubset, List<ToolInfo> fullEventRegistry) {
		List<ToolInfo> union = new ArrayList<ToolInfo>();
	
		// add only tools in default list, as unselected
		Iterator<ToolInfo> iAll = fullEventRegistry.iterator();
		while (iAll.hasNext()){
			ToolInfo t1 = iAll.next();
			Iterator<ToolInfo> iPREFS = eventRegistrySubset.iterator();
			boolean foundTool = false;
			ToolInfo t2 = null;
			while (iPREFS.hasNext()){
				t2 = iPREFS.next();
				if(t2.getToolId().equals(t1.getToolId())){
					foundTool = true;
					break;
				}
			}
			if(!foundTool){
				// tool not found, add as unselected
				ToolInfo toAdd = t1;
				toAdd.setSelected(false);
				for(int i = 0; i < toAdd.getEvents().size(); i++)
					toAdd.getEvents().get(i).setSelected(false);
				union.add(toAdd);
			}else{
				// tool found, add missing events as unselected
				Iterator<EventInfo> aPREFS = t1.getEvents().iterator();
				while (aPREFS.hasNext()){
					EventInfo e1 = aPREFS.next();
					boolean foundEvent = false;
					for(int i = 0; i < t2.getEvents().size(); i++){
						EventInfo e2 = t2.getEvents().get(i);
						if(e2.getEventId().equals(e1.getEventId())){
							foundEvent = true;
							break;
						}
					}
					if(!foundEvent){
						EventInfo toAdd = e1;
						e1.setSelected(false);
						t2.addEvent(toAdd);
					}
				}
				union.add(t2);
			}
		}
		return union;
	}

	/**
	 * Add custom additions to Event Registry.
	 * @param additions Custom Event Registry additions.
	 * @param replaceEventsForTool If tool exists in full Event Registry, replace its events with the ones in additions.
	 * @param eventRegistry The full Event Registry.
	 */
	public static List<ToolInfo> addToEventRegistry(List<ToolInfo> additions, boolean replaceEventsForTool, List<ToolInfo> eventRegistry) {
		if(additions == null || additions.size() == 0)
			return eventRegistry;
		List<ToolInfo> toBeAdded = new ArrayList<ToolInfo>();
		
		// iterate ADD list, add tool if not found in DEFAULT list
		Iterator<ToolInfo> iADDS = additions.iterator();
		while(iADDS.hasNext()){
			ToolInfo newTool = iADDS.next();
			Iterator<ToolInfo> iAll = eventRegistry.iterator();
			boolean foundTool = false;
			ToolInfo existingTool = null;
			while(iAll.hasNext()){
				existingTool = iAll.next(); 
				if(existingTool.equals(newTool)){
					foundTool = true;
					break;
				}
			}
			if(!foundTool){
				// tool not found, add tool and its events
				toBeAdded.add(newTool);
			}else if(foundTool && replaceEventsForTool){
				// tool found, replace its events (but keep the event anonymous flag)
				Iterator<EventInfo> newToolEvents = newTool.getEvents().iterator();
				while(newToolEvents.hasNext()){
					EventInfo newEvent = newToolEvents.next();
					boolean foundEvent = false;
					EventInfo existingEvent = null;
					for(int i=0; i<existingTool.getEvents().size(); i++){
						existingEvent = existingTool.getEvents().get(i);
						if(existingEvent.equals(newEvent)){
							foundEvent = true;
							break;
						}
					}
					if(foundEvent && existingEvent != null && existingEvent.isAnonymous()){
						newEvent.setAnonymous(true);
					}
				}
				existingTool = newTool;
			}else{
				// tool found, add missing events
				Iterator<EventInfo> newToolEvents = newTool.getEvents().iterator();
				while(newToolEvents.hasNext()){
					EventInfo newEvent = newToolEvents.next();
					boolean foundEvent = false;
					for(int i=0; i<existingTool.getEvents().size(); i++){
						EventInfo existingEvent = existingTool.getEvents().get(i);
						if(existingEvent.equals(newEvent)){
							foundEvent = true;
							break;
						}
					}
					if(!foundEvent){
						existingTool.addEvent(newEvent);
					}
				}
			}
		}
		
		eventRegistry.addAll(toBeAdded);
		return eventRegistry;
	}

	/**
	 * Remove specified custom additions from Event Registry.
	 * @param additions Custom Event Registry removals.
	 * @param eventRegistry The full Event Registry.
	 */
	public static List<ToolInfo> removeFromEventRegistry(List<ToolInfo> removals, List<ToolInfo> eventRegistry) {
		if(removals == null || removals.size() == 0)
			return eventRegistry;
		
		// iterate REMOVES list, remove tool if found in DEFAULT list
		Iterator<ToolInfo> iREMOVES = removals.iterator();
		while(iREMOVES.hasNext()){
			ToolInfo delTool = iREMOVES.next();
			Iterator<ToolInfo> iAll = eventRegistry.iterator();
			boolean foundTool = false;
			ToolInfo existingTool = null;
			while(iAll.hasNext()){
				existingTool = iAll.next(); 
				if(existingTool.getToolId().equals(delTool.getToolId())){
					foundTool = true;
					break;
				}
			}
			if(foundTool){
				// tool found
				if(delTool.getEvents().size() == 0) {
					// tool selected for removal, remove tool and its events
					eventRegistry.remove(existingTool);
				} else {
					// events selected for removal, remove events
					Iterator<EventInfo> delToolEvents = delTool.getEvents().iterator();
					while(delToolEvents.hasNext()){
						EventInfo delEvent = delToolEvents.next();
						boolean foundEvent = false;
						for(int i=0; i<existingTool.getEvents().size(); i++){
							EventInfo existingEvent = existingTool.getEvents().get(i);
							if(existingEvent.getEventId().equals(delEvent.getEventId())){
								foundEvent = true;
								break;
							}
						}
						if(foundEvent){
							existingTool.removeEvent(delEvent);
						}
					}
				}
			}
		}
		return eventRegistry;
	}

}
