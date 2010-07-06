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
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.tool.cover.ToolManager;

public abstract class BaseRosterPageBean {
	
	public static final String ROSTER_VIEW_EMAIL = "roster_view_email";
	
	public abstract String getPageTitle();
	public abstract boolean isExportablePage();
	public abstract void export(ActionEvent event);
	
	// Static comparators
	public static final Comparator<Participant> sortNameComparator;
	public static final Comparator<Participant> displayIdComparator;
	public static final Comparator<Participant> emailComparator;
	public static final Comparator<Participant> roleComparator;

    static {
		sortNameComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				int comparison = Collator.getInstance().compare(
						one.getUser().getSortName(),
						another.getUser().getSortName());
				return comparison == 0 ? displayIdComparator.compare(one,
						another) : comparison;
			}
		};

		displayIdComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				return Collator.getInstance().compare(one.getUser().getDisplayId(),
						another.getUser().getDisplayId());
			}
		};

		emailComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				String email1 = one.getUser().getEmail();
				String email2 = another.getUser().getEmail();
				if(email1 != null && email2 == null) {
					return 1;
				}
				if(email1 == null && email2 != null) {
					return -1;
				}
				if(email1 == null && email2 == null) {
					return sortNameComparator.compare(one, another);
				}
				int comparison = Collator.getInstance().compare(one.getUser().getEmail(),
						another.getUser().getEmail());
				return comparison == 0 ? sortNameComparator.compare(one,
						another) : comparison;
			}
		};

		roleComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				int comparison = Collator.getInstance().compare(one.getRoleTitle(),
						another.getRoleTitle());
				return comparison == 0 ? sortNameComparator.compare(one,
						another) : comparison;
			}
		};
	}

	// Service & Bean References
	protected FilteredParticipantListingBean filter;
	public FilteredParticipantListingBean getFilter() {
		return filter;
	}
	public void setFilter(FilteredParticipantListingBean filter) {
		this.filter = filter;
	}
	protected RosterPreferences prefs;
	public void setPrefs(RosterPreferences prefs) {
		this.prefs = prefs;
	}

	// Utility methods
	protected String getSiteReference() {
		return filter.services.siteService.siteReference(getSiteContext());
	}
	
	protected String getSiteContext() {
		return filter.services.toolManager.getCurrentPlacement().getContext();
	}

	public List<Participant> getParticipants() {
		List<Participant> participants = filter.getParticipants();
		if (participants != null && participants.size() >= 1) {
			Collections.sort(participants, getComparator());
			if(!prefs.sortAscending) {
				Collections.reverse(participants);
			}
        }
        return participants;
    }

    protected Comparator<Participant> getComparator() {
    	String sortColumn = prefs.getSortColumn();
        Comparator<Participant> comparator;
        if (Participant.SORT_BY_ID.equals(sortColumn)) {
            comparator = displayIdComparator;
        } else if (Participant.SORT_BY_EMAIL.equals(sortColumn)) {
            comparator = emailComparator;
        } else if(Participant.SORT_BY_ROLE.equals(sortColumn)) {
            comparator = roleComparator;
        } else if(Participant.SORT_BY_NAME.equals(sortColumn)) {
        	comparator = sortNameComparator;
        } else {
            // Default to the sort name
            comparator = sortNameComparator;
        }
        return comparator;
    }

    // UI logic
    protected Boolean renderOfficialPhotos;
    protected Boolean renderStatusLink;
    protected Boolean renderPicturesLink;
    protected Boolean renderProfileLinks;
    protected Boolean renderGroupMembershipLink;

    public boolean isRenderStatusLink() {
        if(renderStatusLink == null) {
            renderStatusLink = filter.services.securityService.unlock(
                    RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS, getSiteReference()) &&
                    ! filter.statusRequestCache().enrollmentSets.isEmpty();
        }
        return renderStatusLink.booleanValue();
    }

    public boolean isRenderPicturesLink() {
        if(renderPicturesLink == null) {
            renderPicturesLink = filter.services.rosterManager.isOfficialPhotosViewable() || filter.services.rosterManager.isProfilesViewable();
        }
        return renderPicturesLink.booleanValue();
    }

    public boolean isRenderProfileLinks() {
        if(renderProfileLinks == null) {
            renderProfileLinks = filter.services.rosterManager.isProfilesViewable();
        }
        return renderProfileLinks.booleanValue();
    }
    
    public boolean isRenderGroupMembershipLink() {
    	if(renderGroupMembershipLink == null) {
    		renderGroupMembershipLink = filter.services.rosterManager.isGroupMembershipViewable();
    	}
    	return renderGroupMembershipLink.booleanValue();
    }

    public boolean isOfficialPhotosAvailableToCurrentUser() {
        if(renderOfficialPhotos == null) {
            renderOfficialPhotos = filter.services.rosterManager.isOfficialPhotosViewable();
        }
        return renderOfficialPhotos.booleanValue();
    }
    
    public String getPrintFriendlyUrl()
	{
    	return ServerConfigurationService.getToolUrl() + Entity.SEPARATOR
    		+ ToolManager.getCurrentPlacement().getId() + Entity.SEPARATOR + "printFriendly";
	}

    /**
     * Some institutions use an email address as a user's displayId.  For these institutions,
     * we provide a way to hide the email column.
     */
    public boolean isEmailColumnRendered() {
        return Boolean.TRUE.toString().equalsIgnoreCase(
                filter.services.serverConfigurationService.getString(ROSTER_VIEW_EMAIL, "true"));
	}
	
	protected String getDownloadFileName(String rawString) {
        String dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(new Date());
        return rawString.replaceAll("\\W","_")+ "_"+dateString;
    }
  
	public String groupMembership()
	{
		// clears section filter for the group membership page
		filter.sectionFilter = null;
		return "groupMembership";
	}
}
