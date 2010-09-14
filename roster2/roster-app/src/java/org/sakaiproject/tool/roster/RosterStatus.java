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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterXls;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.jsf.util.LocaleUtil;

public class RosterStatus extends BaseRosterPageBean {
	public static final Comparator<Participant> enrollmentStatusComparator;
	public static final Comparator<Participant> enrollmentCreditsComparator;

	static {
		enrollmentStatusComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				EnrolledParticipant p1 = (EnrolledParticipant)one;
				EnrolledParticipant p2 = (EnrolledParticipant)another;
				String status1 = p1.getEnrollmentStatus();
				String status2 = p2.getEnrollmentStatus();
				if(status1 != null && status2 == null) {
					return 1;
				}
				if(status1 == null && status2 != null) {
					return -1;
				}
				if(status1 == null && status2 == null) {
					return BaseRosterPageBean.sortNameComparator.compare(one, another);
				}
				int comparison = Collator.getInstance().compare(p1.getEnrollmentStatus(),
						p2.getEnrollmentStatus());
				return comparison == 0 ? BaseRosterPageBean.sortNameComparator.compare(one,
						another) : comparison;
			}
		};

		enrollmentCreditsComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				EnrolledParticipant p1 = (EnrolledParticipant)one;
				EnrolledParticipant p2 = (EnrolledParticipant)another;

				String credits1 = p1.getEnrollmentCredits();
				String credits2 = p2.getEnrollmentCredits();
				if(credits1 != null && credits2 == null) {
					return 1;
				}
				if(credits1 == null && credits2 != null) {
					return -1;
				}
				if(credits1 == null && credits2 == null) {
					return BaseRosterPageBean.sortNameComparator.compare(one, another);
				}
				int comparison = Collator.getInstance().compare(p1.getEnrollmentCredits(),
						p2.getEnrollmentCredits());
				return comparison == 0 ? BaseRosterPageBean.sortNameComparator.compare(one,
						another) : comparison;
			}
		};

	}
	
	protected Comparator<Participant> getComparator() {
		String sortColumn = prefs.sortColumn;

		Comparator<Participant> comparator;

		if (EnrolledParticipant.SORT_BY_ID.equals(sortColumn)) {
			comparator = BaseRosterPageBean.displayIdComparator;
		} else if (EnrolledParticipant.SORT_BY_NAME.equals(sortColumn)) {
			comparator = BaseRosterPageBean.sortNameComparator;
		} else if (EnrolledParticipant.SORT_BY_EMAIL.equals(sortColumn)) {
			comparator = BaseRosterPageBean.emailComparator;
		} else if (EnrolledParticipant.SORT_BY_STATUS.equals(sortColumn)) {
			comparator = enrollmentStatusComparator;
		} else if (EnrolledParticipant.SORT_BY_CREDITS.equals(sortColumn)) {
			comparator = enrollmentCreditsComparator;
		} else {
			comparator = BaseRosterPageBean.sortNameComparator;
		}
		return comparator;
	}
	
	public String getPageTitle() {
        filter.services.eventTrackingService.post(filter.services.eventTrackingService.newEvent("roster.view",getSiteReference(),false));
        return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "title_status");
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

        if (isEmailColumnRendered()){
            header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_email"));
        }
        header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_status"));
		header.add(LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "facet_credits"));
		spreadsheetData.add(header);

		for(Iterator<Participant> participantIter = getParticipants().iterator(); participantIter.hasNext();) {
			Participant participant = participantIter.next();
			List<Object> row = new ArrayList<Object>();
			row.add(participant.getUser().getSortName());
			row.add(participant.getUser().getDisplayId());
            if (isEmailColumnRendered()){
			    row.add(participant.getUser().getEmail());
            }
			row.add(((EnrolledParticipant)participant).getEnrollmentStatus());
			row.add(((EnrolledParticipant)participant).getEnrollmentCredits());
			spreadsheetData.add(row);
		}

        String spreadsheetNameRaw = ((FilteredStatusListingBean)filter).getFirstEnrollmentSetTitle();
        String spreadsheetName = getDownloadFileName(spreadsheetNameRaw);
        SpreadsheetUtil.downloadSpreadsheetData(spreadsheetData,spreadsheetName, new SpreadsheetDataFileWriterXls());
	}

	/**
	 * Since we're already here, skip the permission check and just display the status link
	 */
	public boolean isRenderStatusLink() {
		return true;
	}
}
