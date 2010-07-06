/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.roster;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

public class FilteredParticipantListingBean implements Serializable {
	private static final Log log = LogFactory.getLog(FilteredParticipantListingBean.class);
	private static final long serialVersionUID = 1L;

	protected ServicesBean services;
	public void setServices(ServicesBean services) {
		this.services = services;
	}
	protected SearchFilter searchFilter;
	public void setSearchFilter(SearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}

	protected String defaultSearchText;
	protected String sectionFilter;
	protected String groupFilter;


	// Cache the participants list so we don't have to fetch it twice (once for the list,
	// and again for its size)
	protected List<Participant> participants;
	protected Integer participantCount;
	protected SortedMap<String, Integer> roleCounts;
    protected boolean displayFilterSingleGroup;

    /**
	 * Initialize this bean once, so we can call our access method as often as we like
	 * without invoking unnecessary service calls.
	 */
	public void init() {
		this.participants = findParticipants();
		this.participantCount = this.participants.size();
		this.roleCounts = findRoleCounts(this.participants);

		if(defaultSearchText == null) defaultSearchText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "roster_search_text");
		if(getSearchFilterString() == null) searchFilter.setSearchFilter(defaultSearchText);
	}

	/**
	 * JSF hack to call init() when a filtering page is rendered.
	 * @return null;
	 */
	public String getInit() {
		init();
		return null;
	}

	// UI Actions

	public void search(ActionEvent ae) {
		// Nothing needs to be done to search
	}

	public void clearSearch(ActionEvent ae) {
		searchFilter.setSearchFilter(defaultSearchText);
	}

	protected List<Participant> findParticipants() {
		// Only get the participants we need
		List<Participant> participants;
		if(sectionFilter != null && isDisplaySectionsFilter()) {
			participants = services.rosterManager.getRoster(sectionFilter);
		} else {
			participants = services.rosterManager.getRoster();
		}
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			if(filterParticipant(participant)) iter.remove();
		}

		return participants;
	}

	/**
	 * Remove this participant if they don't  pass the search filter
	 */
	protected boolean filterParticipant(Participant participant) {
		return getSearchFilterString() != null && ! getSearchFilterString().equals(defaultSearchText) && ! searchMatches(getSearchFilterString(), participant.getUser());
	}

	protected SortedMap<String, Integer> findRoleCounts(Iterable<Participant> participants) {
		SortedMap<String, Integer> roleCountMap = new TreeMap<String, Integer>();
		for(Participant participant : participants) {
			String role = participant.getRoleTitle();

			if(roleCountMap.containsKey(role)) {
				int count = roleCountMap.get(role) + 1;
				roleCountMap.put(role, count);
			} else {
				roleCountMap.put(role, 1);
			}
		}
		return roleCountMap;
	}

	protected boolean searchMatches(String search, User user) {
		return user.getDisplayName().toLowerCase().startsWith(search.toLowerCase()) ||
				   user.getSortName().toLowerCase().startsWith(search.toLowerCase()) ||
				   user.getDisplayId().toLowerCase().startsWith(search.toLowerCase()) ||
				   user.getEmail().toLowerCase().startsWith(search.toLowerCase());
	}

	public List<SelectItem> getSectionSelectItems() {
		List<SelectItem> list = new ArrayList<SelectItem>();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String sepLine = LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "roster_section_sep_line");
        String all_sections = LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "roster_sections_all");

        // Add the "all" select option and a separator line
        list.add(new SelectItem("", all_sections));
        list.add(new SelectItem(sepLine, sepLine));

		// Get the available sections
		List<CourseSection> sections = requestCache().viewableSections;
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
			list.add(new SelectItem(section.getUuid(), section.getTitle()));
		}
		return list;
	}

	public boolean isDisplaySectionsFilter() {
        if(!isHideSingleGroupFilter())return true;
        return requestCache().viewableSections.size() > 1;
	}

    /**
     * Display section/group dropdown filter when site has only a single group or section defined: true or false
     * @return true or false
     */
    public boolean isHideSingleGroupFilter() {
        if("true".equals(services.serverConfigurationService.getString("roster.display.hideSingleGroupFilter")))return true;
        return false;
    }


    public boolean isDisplayPhotoFirstNameLastName(){
       if("true".equals(services.serverConfigurationService.getString("roster.display.firstNameLastName"))) return true;
       return false;
    }
    
    public boolean isGroupedBy() {
		String groupFilter = StringUtils.trimToNull(getGroupFilter());
		if (groupFilter == null)
			return false;
		boolean grouped = Boolean.valueOf(groupFilter);
		return grouped;
	}

    public String getSearchFilterString() {
		return searchFilter.getSearchFilter();
	}

	public void setSearchFilterString(String searchFilter) {
		String trimmedArg = StringUtils.trimToNull(searchFilter);
		if(trimmedArg == null) {
			this.searchFilter.setSearchFilter(defaultSearchText);
		} else {
			this.searchFilter.setSearchFilter(trimmedArg);
		}
	}

	public String getSectionFilter() {
		return sectionFilter;
	}

    public String getSectionFilterTitle(){
        List<CourseSection> sections = requestCache().viewableSections;
        for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
            if(section.getUuid().equals(getSectionFilter())) return section.getTitle();
		}
        return null;
    }

     public String getCourseFilterTitle(){
    	 try {
    		 Site site = services.siteService.getSite(getSiteContext());
    		 return site.getTitle();
    	 } catch (IdUnusedException ide) {
    		 log.warn("Unable to find site: " + ide);
    		 return "unknown site";
    	 }
    }
     
    public void setGroupFilter(String groupFilter) {
    	// Don't allow this value to be set to the separater line
 		if(LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
 				ServicesBean.MESSAGE_BUNDLE, "roster_section_sep_line")
 				.equals(groupFilter)) {
 			this.groupFilter = null;
 		} else {
 			this.groupFilter = StringUtils.trimToNull(groupFilter);
 		}
 	}

    public void setSectionFilter(String sectionFilter) {
		// Don't allow this value to be set to the separater line
		if(LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "roster_section_sep_line")
				.equals(sectionFilter)) {
			this.sectionFilter = null;
		} else {
			this.sectionFilter = StringUtils.trimToNull(sectionFilter);
		}
	}

	public Integer getParticipantCount() {
		return participantCount;
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public String getRoleCountMessage() {
        if(roleCounts.size() == 0) return "";
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(Iterator<Entry<String, Integer>> iter = roleCounts.entrySet().iterator(); iter.hasNext();) {
			Entry<String, Integer> entry = iter.next();
			String[] params = new String[] {entry.getValue().toString(), entry.getKey()};			
			sb.append(getFormattedMessage("role_breakdown_fragment", params));
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	private String getFormattedMessage(String key, String[] params) {
		String rawString = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(), ServicesBean.MESSAGE_BUNDLE, key);
        MessageFormat format = new MessageFormat(rawString);
        return format.format(params);
	}
	
	public String getGroupFilter() {
		return groupFilter;
	}
	
	public List<SelectItem> getGroupSelectItems() {
        List<SelectItem> list = new ArrayList<SelectItem>();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        String ungrouped = LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "roster_group_ungrouped");
        String byGroup = LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "roster_group_bygroup");

        // Add the "all" select option and a separator line
        list.add(new SelectItem("false", ungrouped));
        list.add(new SelectItem("true", byGroup));

        return list;
    }
	
	public boolean isDisplayingParticipants() {
		// if we have entries in the roleCounts map, we have participants to display
		return ! roleCounts.isEmpty();
	}

    protected String getSiteReference() {
		return "/site/" + getSiteContext();
	}
	protected String getSiteContext() {
		return services.toolManager.getCurrentPlacement().getContext();
	}
	
	public String getDefaultSearchText() {
		return defaultSearchText;
	}

	// Request scoped caching

	// We use these request-scoped beans to hold references to the sections in this site.
	// DO NOT cache the RequestCache itself.  Always obtain a reference using
	// requestCache().
	protected RequestCache requestCache() {
		RequestCache rc = (RequestCache)resolveManagedBean("requestCache");
		// Manually initialize the cache, if necessary
		if( ! rc.isInitizlized()) rc.init(services);
		return rc;
	}

	protected StatusRequestCache statusRequestCache() {
		StatusRequestCache rc = (StatusRequestCache)resolveManagedBean("statusRequestCache");
		// Manually initialize the cache, if necessary
		if( ! rc.isInitialized()) rc.init(services);
		return rc;
	}

	// This will either retrieve the existing managed bean, or generate a new one
	protected Object resolveManagedBean(String managedBeanId) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, managedBeanId);
	}

}
