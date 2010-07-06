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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

public class FilteredStatusListingBean extends FilteredParticipantListingBean implements Serializable {

	private static final String ALL_STATUS="ALL_STATUS";
	private static final Log log = LogFactory.getLog(FilteredStatusListingBean.class);
	private static final long serialVersionUID = 1L;

	protected String statusFilter;
	protected Set<String> studentRoles; // TODO Is it OK to store this in the session?
	
	public void init() {
		// Get the student roles before any filtering occurs
		try {
			AuthzGroup azg = services.authzService.getAuthzGroup(getSiteReference());
			studentRoles = azg.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
		} catch (GroupNotDefinedException gnde) {
			log.error("Unable to find site " + getSiteReference());
			studentRoles = new HashSet<String>();
		}

		// Set the filter status before any filtering occurs
		if(this.statusFilter == null) {
			this.statusFilter = ALL_STATUS;
		}

		if(defaultSearchText == null) defaultSearchText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "roster_search_text");
		if(getSearchFilterString() == null) searchFilter.setSearchFilter(defaultSearchText);

		this.participants = findParticipants();
		this.participantCount = participants.size();
	}

	protected List<Participant> findParticipants() {
		if(log.isDebugEnabled()) log.debug("Finding participants filtered by enrollment status");

		// Find the enrollment status descriptions for the current user's locale
		Locale locale = new ResourceLoader().getLocale();
		Map<String, String> statusCodes = services.cmService.getEnrollmentStatusDescriptions(locale);

		// Make sure we're looking at an enrollment set
		if(sectionFilter == null) {
			this.sectionFilter = (String)statusRequestCache().enrollmentSets.get(0).getEid();
		}
		
		// There is no reason to use the roster manager here anymore, since we only
		// need CM and user data
		List<Participant> participants = new ArrayList<Participant>();
		Set<Enrollment> enrollments = services.cmService.getEnrollments(sectionFilter);
		final String studentRole;
		if(studentRoles.isEmpty()) {
			studentRole = "";
		} else {
			// just pick one
			studentRole = studentRoles.iterator().next();
		}

		for(Iterator<Enrollment> iter = enrollments.iterator(); iter.hasNext();) {
			Enrollment enr = iter.next();
			final User user;
			try {
				user = services.userDirectoryService.getUserByEid(enr.getUserId());
			} catch (UserNotDefinedException unde) {
				log.warn("Can not find user " + enr.getUserId());
				continue;
			}
			Participant p = new Participant() {
				public Profile getProfile() {return null;}
				public String getRoleTitle() {return studentRole;}
				public User getUser() {return user;}
				public boolean isOfficialPhotoPreferred() {return false;}
				public boolean isOfficialPhotoPublicAndPreferred() {return false;}
				public boolean isProfilePhotoPublic() {return false;}
				public String getGroupsString() {return "";}
			};
			EnrolledParticipant ep = new EnrolledParticipant(p, statusCodes.get(enr.getEnrollmentStatus()), enr.getCredits());
			participants.add(ep);
		}
		
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			if(filterParticipant(participant)) iter.remove();
		}

		
		if(ALL_STATUS.equals(statusFilter) || StringUtils.trimToNull(statusFilter) == null) {
			// No need for further filtering
			return participants;
		}
		
		// Filter the participants further, by status
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			if( ! statusFilter.equals(((EnrolledParticipant)iter.next()).getEnrollmentStatus())) {
				iter.remove();
			}
		}
		return participants;
	}

	/**
	 * Filter this participant?
	 */
	protected boolean filterParticipant(Participant participant) {
		if(super.filterParticipant(participant)) return true;
		return ! studentRoles.contains(participant.getRoleTitle()); 
	}

	
	public List<SelectItem> getSectionSelectItems() {
		List<SelectItem> selectItems = new ArrayList<SelectItem>();
		for(Iterator<EnrollmentSet> iter = statusRequestCache().enrollmentSets.iterator(); iter.hasNext();) {
			EnrollmentSet es = iter.next();
			selectItems.add(new SelectItem(es.getEid(), es.getTitle()));
		}
		return selectItems;
	}
	
	public boolean isMultipleEnrollmentSetsDisplayed() {
		return statusRequestCache().enrollmentSets.size() > 1;
	}
	
	public List<SelectItem> getEnrollmentSetSelectItems() {
		List<SelectItem> selectItems = new ArrayList<SelectItem>();
		for(Iterator<EnrollmentSet> iter = statusRequestCache().enrollmentSets.iterator(); iter.hasNext();) {
			EnrollmentSet es = iter.next();
			selectItems.add(new SelectItem(es.getEid(), es.getTitle()));
		}
		return selectItems;
	}

	public String getStatusFilter() {
		return statusFilter;
	}

	public void setStatusFilter(String statusFilter) {
		this.statusFilter = statusFilter;
	}
	

	public String getCurrentlyDisplayingMessage() {
		String key = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "enrollments_currently_displaying");

		Object[] params = new Object[2];
		params[0] = participantCount;
		if(ALL_STATUS.equals(statusFilter)) {
			params[1] = "";
		} else {
			params[1] = statusFilter;
		}
		return MessageFormat.format(key, params);
	}
	
	public String getAllStatus() {
		return ALL_STATUS;
	}
		
	public String getFirstEnrollmentSetTitle() {
		return statusRequestCache().enrollmentSets.get(0).getTitle();
	}

	public List<SelectItem> getStatusSelectItems() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		Map<String, String> map = services.cmService.getEnrollmentStatusDescriptions(LocaleUtil.getLocale(FacesContext.getCurrentInstance()));

		// The UI doesn't care about status IDs... just labels
		List<String> statusLabels = new ArrayList<String>();
		statusLabels.addAll(map.values());
		Collections.sort(statusLabels);
		for(Iterator<String> iter = statusLabels.iterator(); iter.hasNext();) {
			String statusLabel = iter.next();
			SelectItem item = new SelectItem(statusLabel, statusLabel);
			list.add(item);
		}
		return list;
	}

}
