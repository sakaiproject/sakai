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

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.authz.cover.SecurityService;
public class RosterProfile {
	private static final Log log = LogFactory.getLog(RosterProfile.class);

	// Service & Bean References
	protected ServicesBean services;
	public void setPrefs(RosterPreferences prefs) {
		this.prefs = prefs;
	}

	protected RosterPreferences prefs;
	public void setServices(ServicesBean services) {
		this.services = services;
	}
	
	protected Participant participant;
	
	public String displayProfile() {
		String userId = StringUtils.trimToNull((String) FacesContext
				.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("participantId"));
		
		String returnPage = StringUtils.trimToNull((String) FacesContext
				.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("returnPage"));
		if(returnPage != null) {
			prefs.setReturnPage(returnPage);
		}

		if (userId == null) {
			log.debug("Can not display a profile for null");
			return "profileNotFound";
		}
		participant = services.rosterManager.getParticipantById(userId);
		if (participant == null) {
			log.debug("Can not display a profile for: " + userId);
			return "profileNotFound";
			
		}
        //log event to event service
		if (participant != null) {
			services.eventTrackingService.post(services.eventTrackingService.newEvent("roster.view.profile",participant.getUser().getEid(),false));
		} else {
			services.eventTrackingService.post(services.eventTrackingService.newEvent("roster.view.profile","null",false));
		}
		
        if (services.userDirectoryService.getCurrentUser().getId()
				.equals(userId)||SecurityService.isSuperUser()) {
			// This user is looking at him/her self or isSuperUser
			return "completeProfile";
		}

		if (participant == null || participant.getProfile() == null) {
			if (log.isDebugEnabled())
				log.debug("Can not display a missing profile for user "
						+ userId);
			return "profileNotFound";
		}
		if (participant.getProfile().getHidePublicInfo() != null && participant.getProfile().getHidePublicInfo()) {
			if (log.isDebugEnabled())
				log.debug("You have no authrozation to view this person: " + userId);
			return "profileNotFound";
		}
		
		if (participant.getProfile().getHidePrivateInfo() == null
				|| participant.getProfile().getHidePrivateInfo()) {
			if (log.isDebugEnabled())
				log.debug("Displaying the public profile for " + userId);
			return "publicProfile";
		}
		if (log.isDebugEnabled())
			log.debug("Displaying the complete profile for " + userId);
		return "completeProfile";
	}

	public Participant getParticipant() {
		return participant;
	}

	public boolean isShowCustomPhotoUnavailableForSelectedProfile() {
		if (participant == null || participant.getProfile() == null) {
			return true;
		}
		Profile profile = participant.getProfile();
		if (!services.profileManager.displayCompleteProfile(profile)) {
			return true;
		}

		if (profile.isInstitutionalPictureIdPreferred() == null) {
			return true;
		}
		if (!profile.isInstitutionalPictureIdPreferred().booleanValue()
				&& (profile.getPictureUrl() == null || profile.getPictureUrl().length() < 1)) {
			return true;
		}
		return false;
	}

	public boolean isShowURLPhotoForSelectedProfile() {
		if (participant == null || participant.getProfile() == null) {
			return false;
		}
		Profile profile = participant.getProfile();
		if (services.profileManager.displayCompleteProfile(profile)
				&& profile.getPictureUrl() != null
				&& profile.getPictureUrl().length() > 0) {
			return true;
		}
		return false;
	}

    public boolean isShowCustomIdPhotoForSelectedProfile()
    {
		if (participant == null || participant.getProfile() == null) {
			return false;
		}
		Profile profile = participant.getProfile();
      if (profile.isInstitutionalPictureIdPreferred() == null)
      {
        return false;
      }
      if (services.profileManager.displayCompleteProfile(profile)
          && profile.isInstitutionalPictureIdPreferred().booleanValue())
      {
        return true;
      }
      return false;
    }
}
