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
package org.sakaiproject.sitestats.tool.wicket.providers;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.ReportDefModel;

@Slf4j
public class ReportDefsProvider implements IDataProvider {
	private static final long		serialVersionUID				= 1L;
	public final static int			MODE_MYREPORTS					= 0;
	public final static int			MODE_PREDEFINED_REPORTS			= 1;
	public final static int			MODE_MY_AND_PREDEFINED_REPORTS	= 2;
	
	private String					siteId;
	private	int						mode;
	private boolean 				filterWithToolsInSite;
	private boolean 				includeHidden;
	private List<ReportDef>			data;

	public ReportDefsProvider(String siteId, int mode, boolean filterWithToolsInSite, boolean includeHidden) {
		Injector.get().inject(this);		
		this.siteId = siteId;
		this.mode = mode;
		this.filterWithToolsInSite = filterWithToolsInSite;
		this.includeHidden = includeHidden;
	}

	public Iterator iterator(long first, long count) {
		return getReportDefs().iterator();		
	}
	
	public List<ReportDef> getReportDefs() {
		if(data == null) {
			switch(mode) {
				case MODE_MYREPORTS:
					data = Locator.getFacade().getReportManager().getReportDefinitions(siteId, false, includeHidden);
					break;
				case MODE_PREDEFINED_REPORTS:
					data = Locator.getFacade().getReportManager().getReportDefinitions(null, false, includeHidden);
					break;
				case MODE_MY_AND_PREDEFINED_REPORTS:
					data = Locator.getFacade().getReportManager().getReportDefinitions(siteId, true, includeHidden);
					break;
			}
			if(filterWithToolsInSite) {
				data = filterWithToolsInSite(data);
			}
			data = fixReportParamsSiteIdForPredefinedReports(data);
			Collections.sort(data, getReportDefComparator());
		}		
		return data;
	}

	public IModel model(Object object) {
		return new ReportDefModel((ReportDef) object);
	}

	public long size() {
		return getReportDefs().size();
	}

	public void detach() {
		data = null;
	}
	
	private List<ReportDef> fixReportParamsSiteIdForPredefinedReports(List<ReportDef> list) {
		List<ReportDef> fixed = new ArrayList<ReportDef>();
		for(ReportDef rd : list) {
			if(rd.getSiteId() == null) {
				// fix siteId for predefined reports
				rd.getReportParams().setSiteId(siteId);
				fixed.add(rd);
			}else{
				fixed.add(rd);
			}
		}
		return fixed;
	}

	private List<ReportDef> filterWithToolsInSite(List<ReportDef> list) {
		List<ReportDef> filtered = new ArrayList<ReportDef>();
		if(list != null) {
			try{
				Site site = Locator.getFacade().getSiteService().getSite(siteId);
				for(ReportDef rd : list){
					if(canIncludeReport(rd, site)){
						filtered.add(rd);
					}
				}
			}catch(Exception e){
				filtered = list;
			}
		}
		return filtered;
	}
	
	private boolean canIncludeReport(ReportDef reportDef, Site site) {
		List<ToolConfiguration> siteTools = new ArrayList<ToolConfiguration>();
		for(Iterator<SitePage> iPages = site.getPages().iterator(); iPages.hasNext();){
			SitePage page = iPages.next();
			siteTools.addAll(page.getTools());
		}
		
		if(ReportManager.WHAT_VISITS.equals(reportDef.getReportParams().getWhat())) {
			// keep visit based reports if site visits are enabled
			if(Locator.getFacade().getStatsManager().isEnableSiteVisits()) {
				return true;
			}
		}else if(ReportManager.WHAT_RESOURCES.equals(reportDef.getReportParams().getWhat())) {
			// keep resource based reports if Resources tool is present
			for(ToolConfiguration tc : siteTools) {
				if(tc.getToolId().equals(StatsManager.RESOURCES_TOOLID)) {
					return true;
				}
			}
		}else if(ReportManager.WHAT_PRESENCES.equals(reportDef.getReportParams().getWhat())) {
			// keep presence based reports if site presences are enabled
			if(Locator.getFacade().getStatsManager().isEnableSitePresences()) {
				return true;
			}
		}else{
			// at least one tool from the selection must be present
			if(ReportManager.WHAT_EVENTS_BYEVENTS.equals(reportDef.getReportParams().getWhatEventSelType())) {
				for(ToolConfiguration tc : siteTools) {
					Map<String,ToolInfo> map = Locator.getFacade().getEventRegistryService().getEventIdToolMap();
					for(String eventId : reportDef.getReportParams().getWhatEventIds()) {
						if(tc.getToolId().equals(map.get(eventId).getToolId())) {
							return true;
						}
					}
				}
			}else if(ReportManager.WHAT_EVENTS_BYTOOL.equals(reportDef.getReportParams().getWhatEventSelType())) {
				if(reportDef.getReportParams().getWhatToolIds().contains(ReportManager.WHAT_EVENTS_ALLTOOLS)) {
					return true;
				}else{
					for(ToolConfiguration tc : siteTools) {
						if(reportDef.getReportParams().getWhatToolIds().contains(tc.getToolId())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public final Comparator<ReportDef> getReportDefComparator() {
		return new Comparator<ReportDef>() {
			private transient Collator		collator = Collator.getInstance();
			{
				try{
					collator= new RuleBasedCollator(((RuleBasedCollator)Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
				}catch(ParseException e){
				    log.error("Unable to create RuleBasedCollator");
				}		
			}
			
			public int compare(ReportDef o1, ReportDef o2) {
				String title1 = null;
				String title2 = null;
				if(o1.isTitleLocalized()) {
					title1 = (String) new ResourceModel(o1.getTitleBundleKey()).getObject();
				}else{
					title1 = o1.getTitle();
				}
				if(o2.isTitleLocalized()) {
					title2 = (String) new ResourceModel(o2.getTitleBundleKey()).getObject();
				}else{
					title2 = o2.getTitle();
				}
				return collator.compare(title1, title2);
			}
			
		};
	}
}
