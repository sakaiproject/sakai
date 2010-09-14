/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterCsv;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.site.api.SiteService;

public class RosterOverview extends BaseRosterPageBean {
	private static final String DISPLAY_ROSTER_PRIVACY_MSG = "roster.privacy.display";

	// UI method calls
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
				ServicesBean.MESSAGE_BUNDLE, "title_overview");
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

        if (isEmailColumnRendered()) {
            header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_email"));
        }
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_role"));
		
		spreadsheetData.add(header);
		for(Iterator<Participant> participantIter = getParticipants().iterator(); participantIter.hasNext();) {
			Participant participant = participantIter.next();
			List<Object> row = new ArrayList<Object>();
			row.add(participant.getUser().getSortName());
            row.add(participant.getUser().getDisplayId());

            if (isEmailColumnRendered()){
                row.add(participant.getUser().getEmail());
            }
			row.add(participant.getRoleTitle());
            spreadsheetData.add(row);
        }

        String spreadsheetNameRaw;
        if(StringUtils.trimToNull(filter.sectionFilter) == null) {
        	spreadsheetNameRaw = filter.getCourseFilterTitle();
        } else {
        	CourseSection section = filter.services.sectionAwareness.getSection(filter.getSectionFilter());
        	spreadsheetNameRaw = filter.getCourseFilterTitle() + "_" + section.getTitle();
        }

        String spreadsheetName = getDownloadFileName(spreadsheetNameRaw);
        SpreadsheetUtil.downloadSpreadsheetData(spreadsheetData, spreadsheetName, new SpreadsheetDataFileWriterXls());
    }
}
