/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/roster/trunk/roster-app/src/java/org/sakaiproject/tool/roster/RosterOverview.java $
 * $Id: RosterOverview.java 51318 2008-08-24 05:28:47Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007 Sakai Foundation
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

import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

public class RosterGroupMembership extends BaseRosterPageBean {
	private static final Log log = LogFactory.getLog(RosterGroupMembership.class);

	private static final String DISPLAY_ROSTER_PRIVACY_MSG = "roster.privacy.display";

	// UI method calls
	public boolean isGroupedBy() {
		return filter.isGroupedBy();
	}
	@SuppressWarnings("unchecked")
	public Collection<GroupedParticipants> getGroupedParticipants() {
		List<GroupedParticipants> groupedParticipants = null;
		Site site = null;

		try {
			site = filter.services.siteService.getSite(filter.getSiteReference().substring(6));
		} catch (IdUnusedException e) {
			log.error("Unable to find site for: " + getSiteReference() + " " + e.getMessage(), e);
			return null;
		}
				
		if (site != null) {
			groupedParticipants = new ArrayList<GroupedParticipants>();
			Collection<Group> groups = (Collection<Group>) site.getGroups();

			//Use a HashSet because we'll have to use .removeAll() on it many times
			//.remove() is roughly constant time for a HashSet.
			Set<Participant> unassignedParticipants = new HashSet<Participant>(
					filter.services.rosterManager.getRoster()
			);

			for(Iterator<Group> groupIter = groups.iterator(); groupIter.hasNext();)
			{
				Group group = groupIter.next();
				List<Participant> roster = filter.services.rosterManager.getRoster(group.getReference());

				//remove each grouped participant from the 'unassignedParticipants' set
				unassignedParticipants.removeAll(roster);

				groupedParticipants.add(new GroupedParticipants(group.getTitle(), roster, roster.size(), getRoleCountMessage(filter.findRoleCounts(roster))));
			}

			// if we have participants who are ungrouped, we add them here to a new one for rendering called "Unassigned"
			if (!unassignedParticipants.isEmpty())
			{
				String unassigned = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(), ServicesBean.MESSAGE_BUNDLE, "roster_group_unassigned");
				groupedParticipants.add(
						new GroupedParticipants(
								unassigned, 
								unassignedParticipants, 
								unassignedParticipants.size(), 
								getRoleCountMessage(filter.findRoleCounts(unassignedParticipants))
						)
				);
			}

			Collections.sort(groupedParticipants, sortByGroup());
		}
		
        return groupedParticipants;
	}
	
	public class GroupedParticipants {
		Collection<Participant> groupedParticipants = new ArrayList<Participant>();
        String groupTitle;
        int groupedParticipantCount;
        String roleCountMessage;
        
		public int getGroupedParticipantCount() {
			return groupedParticipantCount;
		}
		public void setGroupedParticipantCount(int groupedParticipantCount) {
			this.groupedParticipantCount = groupedParticipantCount;
		}
		public String getGroupTitle() {
			return groupTitle;
		}
		public void setGroupTitle(String groupTitle) {
			this.groupTitle = groupTitle;
		}
		public Collection<Participant> getGroupedParticipants() {
			return groupedParticipants;
		}
		public void setGroupedParticipants(Collection<Participant> groupedParticipants) {
			this.groupedParticipants = groupedParticipants;
		}
		public String getRoleCountMessage() {
			return roleCountMessage;
		}
		public void setRoleCountMessage(String roleCountMessage) {
			this.roleCountMessage = roleCountMessage;
		}
		
		public GroupedParticipants() {}
        
		public GroupedParticipants(String groupTitle, Collection<Participant> groupedParticipants, int groupedParticipantCount, String roleCountMessage) {
			this.groupTitle = groupTitle;
			this.groupedParticipants = groupedParticipants;
			this.groupedParticipantCount = groupedParticipantCount;
			this.roleCountMessage = roleCountMessage;
		}
	}
	
	private Comparator<GroupedParticipants> sortByGroup() {
    	Comparator<GroupedParticipants> groupComparator = new Comparator<GroupedParticipants>() {
			public int compare(GroupedParticipants one, GroupedParticipants another)
			{
				return Collator.getInstance().compare(one.getGroupTitle(),another.getGroupTitle());
			}
    	};
        return groupComparator;
    }
	
	private String getRoleCountMessage(SortedMap<String, Integer> roleCounts) {
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
	
	public boolean isRenderModifyMembersInstructions() {
		String siteRef = getSiteReference();
		return filter.services.securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteRef) ||
				filter.services.securityService.unlock(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, siteRef);
	}

	/**
	 * Determine whether privacy message should be displayed. Will be shown if
	 * roster.privacy.display in sakai.properties is "true" and the user does
	 * not have roster.viewhidden permission
	 * 
	 * @return
	 */
	public boolean isRenderPrivacyMessage() {
		String msgEnabled = ServerConfigurationService.getString(DISPLAY_ROSTER_PRIVACY_MSG, Boolean.TRUE.toString());
		if (Boolean.TRUE.toString().equalsIgnoreCase(msgEnabled)) {
			return ! filter.services.securityService.unlock(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN, getSiteReference());
		} else {
			return ! filter.services.securityService.unlock(RosterFunctions.ROSTER_FUNCTION_VIEWALL, getSiteReference());
		}
	}
			
	public String getPageTitle() {
        filter.services.eventTrackingService.post(filter.services.eventTrackingService.newEvent("roster.view",getSiteReference(),false));
        return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "title_group_membership");
	}

	public boolean isExportablePage() {
		return filter.services.rosterManager.currentUserHasExportPerm();
	}
	public void export(ActionEvent event) {
		List<List<Object>> spreadsheetData = new ArrayList<List<Object>>();
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		
		// Add the header row
		List<Object> header = new ArrayList<Object>();
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_name"));
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_userId"));
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_role"));
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_groups"));
		
		if (isGroupedBy())
		{
			for (Iterator<GroupedParticipants> gp = getGroupedParticipants().iterator(); gp.hasNext();)
			{
				GroupedParticipants gpList = gp.next();
				List<Object> groupTitleRow = new ArrayList<Object>();
				List<Object> blankRow = new ArrayList<Object>();
				blankRow.add("");
				spreadsheetData.add(blankRow);
				groupTitleRow.add(gpList.getGroupTitle());
				spreadsheetData.add(groupTitleRow);
				spreadsheetData.add(blankRow);
				spreadsheetData.add(header);
				for(Iterator<Participant> participantIter = gpList.getGroupedParticipants().iterator(); participantIter.hasNext();) {
					Participant participant = participantIter.next();
					List<Object> row = new ArrayList<Object>();
					row.add(participant.getUser().getSortName());
					row.add(participant.getUser().getDisplayId());
					row.add(participant.getRoleTitle());
					row.add(participant.getGroupsString());
		            spreadsheetData.add(row);
		        }
			}
		}
		else
		{
			spreadsheetData.add(header);
			for(Iterator<Participant> participantIter = getParticipants().iterator(); participantIter.hasNext();) {
				Participant participant = participantIter.next();
				List<Object> row = new ArrayList<Object>();
				row.add(participant.getUser().getSortName());
				row.add(participant.getUser().getDisplayId());
				row.add(participant.getRoleTitle());
				row.add(participant.getGroupsString());
	            spreadsheetData.add(row);
	        }
		}

        String spreadsheetNameRaw = filter.getCourseFilterTitle();
        if (isGroupedBy())
        {
        	spreadsheetNameRaw = spreadsheetNameRaw + "_ByGroup";
        }
        else
        {
        	spreadsheetNameRaw = spreadsheetNameRaw + "_Ungrouped";
        }

        String spreadsheetName = getDownloadFileName(spreadsheetNameRaw);
        SpreadsheetUtil.downloadSpreadsheetData(spreadsheetData, spreadsheetName, new SpreadsheetDataFileWriterXls());
    }
}
