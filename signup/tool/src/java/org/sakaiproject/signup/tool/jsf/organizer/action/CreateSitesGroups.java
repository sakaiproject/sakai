/*
* Licensed to The Apereo Foundation under one or more contributor license
* agreements. See the NOTICE file distributed with this work for
* additional information regarding copyright ownership.
*
* The Apereo Foundation licenses this file to you under the Educational 
* Community License, Version 2.0 (the "License"); you may not use this file 
* except in compliance with the License. You may obtain a copy of the 
* License at:
*
* http://opensource.org/licenses/ecl2.txt
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.signup.tool.jsf.organizer.action;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.SignupGroup;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.tool.jsf.SignupGroupWrapper;
import org.sakaiproject.signup.tool.jsf.SignupSiteWrapper;

/**
 * <p>
 * This class will generate the Site-Group object according to user's
 * permission.
 * </P>
 */
public class CreateSitesGroups {

	private SignupMeeting signupMeeting;

	private final SakaiFacade sakaiFacade;

	private SignupSiteWrapper currentSite;

	private List<SignupSiteWrapper> otherSites;

	private SignupMeetingService signupMeetingService;

	private boolean siteOrGroupTruncated = false;

	private List<String> missingSites;

	private List<String> missingGroups;

	/**
	 * Constructor
	 * 
	 * @param signupMeeting
	 *            a SignupMeeting obect
	 * @param signupMeetingService
	 *            a SignupMeetingService object.
	 * @param currentUserId
	 *            an unique sakai internal user id.
	 * @param currentSiteId
	 *            an unique sakai site id.
	 */
	public CreateSitesGroups(SignupMeeting signupMeeting, SakaiFacade sakaiFacade,
			SignupMeetingService signupMeetingService) {
		this.signupMeeting = signupMeeting;
		this.sakaiFacade = sakaiFacade;
		this.signupMeetingService = signupMeetingService;

		setupSingupSites();
	}

	/**
	 * Create the Site-Group according to users's permission.
	 */
	private void setupSingupSites() {
		String currentUserId = sakaiFacade.getCurrentUserId();
		String currentSiteId = sakaiFacade.getCurrentLocationId();
		List<SignupSite> tmpSites = sakaiFacade.getUserSites(currentUserId);
		List<SignupSiteWrapper> siteWrappers = new ArrayList<SignupSiteWrapper>();

		for (Iterator<SignupSite> iter = tmpSites.iterator(); iter.hasNext();) {
			SignupSite signupSite = (SignupSite) iter.next();
			String siteId = signupSite.getSiteId();
			boolean siteAllowed = signupMeetingService.isAllowedToCreateinSite(currentUserId, siteId);
			SignupSiteWrapper sSiteWrapper = new SignupSiteWrapper(signupSite, siteAllowed);

			List<SignupGroup> signupGroups = signupSite.getSignupGroups();
			List<SignupGroupWrapper> groupWrappers = new ArrayList<SignupGroupWrapper>();
			for (SignupGroup group : signupGroups) {
				boolean groupAllowed = false;
				if (siteAllowed)
					groupAllowed = true;
				else
					groupAllowed = signupMeetingService.isAllowedToCreateinGroup(currentUserId, siteId, group
							.getGroupId());
				if (groupAllowed) {
					SignupGroupWrapper groupWrapper = new SignupGroupWrapper(group, groupAllowed);
					groupWrappers.add(groupWrapper);
				}
			}
			groupWrappers.sort( (SignupGroupWrapper x, SignupGroupWrapper y) ->{
				Collator collator = Collator.getInstance();
				collator.setStrength(Collator.PRIMARY);
		        return collator.compare(x.getSignupGroup().getTitle(), y.getSignupGroup().getTitle());
			});
			// TODO remove no permission to create any site groupWrappers empty
			// and site-wide not allowed
			sSiteWrapper.setSignupGroupWrappers(groupWrappers);
			/* default setting if having site permission */
			if (siteId.equals(currentSiteId))
				sSiteWrapper.setSelected(siteAllowed);

			if (!currentSiteId.equals(signupSite.getSiteId()))
				siteWrappers.add(sSiteWrapper);
			else
				this.currentSite = sSiteWrapper;
		}
		this.otherSites = siteWrappers;
	}

	/**
	 * Reset all previous user selected check marks since we reuse them.
	 */
	public void resetSiteGroupCheckboxMark() {
		SignupSiteWrapper siteWrp = getCurrentSite();
		if (siteWrp != null) {
			if (siteWrp.isAllowedToCreate())
				siteWrp.setSelected(true);// default setting

			List<SignupGroupWrapper> grpWrpList = siteWrp.getSignupGroupWrappers();
			if (grpWrpList != null)
				for (SignupGroupWrapper grpWrp : grpWrpList) {
					grpWrp.setSelected(false);
				}
		}

		List<SignupSiteWrapper> otherSiteWrpList = getOtherSites();
		if (otherSiteWrpList != null) {
			for (SignupSiteWrapper oSiteWrp : otherSiteWrpList) {
				oSiteWrp.setSelected(false);

				List<SignupGroupWrapper> otherGrpWrpList = oSiteWrp.getSignupGroupWrappers();
				if (otherGrpWrpList != null)
					for (SignupGroupWrapper oGrpWrp : otherGrpWrpList) {
						oGrpWrp.setSelected(false);
					}
			}
		}

	}

	/**
	 * It will provide the user selected Site-Groups via UI
	 * 
	 * @param currentSite
	 *            a SignupSite object which is user's current site.
	 * @param otherSites
	 *            a list of SignupSite object.
	 * @return a list of SignupSite object which user has selected.
	 */
	static public List<SignupSite> getSelectedSignupSites(SignupSiteWrapper currentSite,
			List<SignupSiteWrapper> otherSites) {
		List<SignupSite> sites = new ArrayList<SignupSite>();

		List<SignupSiteWrapper> siteWrappers = new ArrayList<SignupSiteWrapper>(otherSites);
		siteWrappers.add(0, currentSite);

		for (SignupSiteWrapper wrapper : siteWrappers) {
			SignupSite site = wrapper.getSignupSite();
			if (wrapper.isSelected()) {
				/* the meeting is 'site scope' for this site */
				site.setSignupGroups(null);
			} else {
				List<SignupGroupWrapper> signupGroupWrappers = wrapper.getSignupGroupWrappers();
				List<SignupGroup> groups = new ArrayList<SignupGroup>();
				for (SignupGroupWrapper groupWrapper : signupGroupWrappers) {
					if (groupWrapper.isSelected())
						groups.add(groupWrapper.getSignupGroup());
				}
				/* neither site or it's groups aren't selected */
				if (groups.isEmpty())
					continue;
				site.setSignupGroups(groups);
			}
			sites.add(site);
		}
		return sites;

	}

	/**
	 * This is a validation method. it will make sure that one site or group is
	 * selected.
	 * 
	 * @param currentSite
	 *            a SignupSite object.
	 * @param otherSites
	 *            a list of SignupSite objects.
	 * @return false if none of them has been selected.
	 */
	public static boolean isAtleastASiteOrGroupSelected(SignupSiteWrapper currentSite,
			List<SignupSiteWrapper> otherSites) {
		if (currentSite !=null && currentSite.isSelected())
			return true;
		
		if(currentSite !=null){
			List<SignupGroupWrapper> currentGroupsW = currentSite.getSignupGroupWrappers();
			for (SignupGroupWrapper wrapper : currentGroupsW) {
				if (wrapper.isSelected())
					return true;
			}
		}

		if(otherSites !=null){
			for (SignupSiteWrapper siteW : otherSites) {
				if (siteW.isSelected())
					return true;
				List<SignupGroupWrapper> otherGroupsW = siteW.getSignupGroupWrappers();
				for (SignupGroupWrapper groupW : otherGroupsW) {
					if (groupW.isSelected())
						return true;
				}
			}
		}
		return false;
	}

	public SignupSiteWrapper getCurrentSite() {
		return currentSite;
	}

	public void setCurrentSite(SignupSiteWrapper currentSite) {
		this.currentSite = currentSite;
	}

	public List<SignupSiteWrapper> getOtherSites() {
		return otherSites;
	}

	public void setOtherSites(List<SignupSiteWrapper> otherSites) {
		this.otherSites = otherSites;
	}

	/**
	 * Transfer original site/group selections if permitted for current
	 * organizer
	 */
	public void processSiteGroupSelectionMarks() {
		if (signupMeeting != null && signupMeeting.getSignupSites() != null
				&& signupMeeting.getSignupSites().size() > 0) {
			List<SignupSite> origSiteList = signupMeeting.getSignupSites();
			for (SignupSite origSite : origSiteList) {
				markSelectedSite(origSite, getCurrentSite());

				if (getOtherSites() != null) {
					for (SignupSiteWrapper otherSiteWrp : getOtherSites()) {
						markSelectedSite(origSite, otherSiteWrp);
					}
				}

			}
			scanMissingSiteGroups(origSiteList);
		}
	}

	private void markSelectedSite(SignupSite origSite, SignupSiteWrapper siteWrp) {
		if (origSite.getSiteId().equals(siteWrp.getSignupSite().getSiteId())) {
			if ((origSite.getSignupGroups() == null || origSite.getSignupGroups().isEmpty())
					&& siteWrp.isAllowedToCreate())
				siteWrp.setSelected(true);
			else {
				/* reset default setting for currentSite case */
				siteWrp.setSelected(false);

				List<SignupGroup> origGrpList = origSite.getSignupGroups();
				List<SignupGroupWrapper> grpWrpList = siteWrp.getSignupGroupWrappers();
				markSelectedGroups(origGrpList, grpWrpList);
			}
		}

	}

	private void markSelectedGroups(List<SignupGroup> origGrpList, List<SignupGroupWrapper> grpWrpList) {
		if (origGrpList == null || grpWrpList == null)
			return;

		for (SignupGroup origGrp : origGrpList) {
			for (SignupGroupWrapper grpWrp : grpWrpList) {
				if (origGrp.getGroupId().equals(grpWrp.getSignupGroup().getGroupId())) {
					grpWrp.setSelected(true);
					continue;
				}
			}

		}
	}

	// TODO find more efficient way later!
	private void scanMissingSiteGroups(List<SignupSite> origSiteList) {
		this.siteOrGroupTruncated=false;//reset
		this.missingSites = new ArrayList<String>();
		this.missingGroups = new ArrayList<String>();
		if (origSiteList == null || origSiteList.isEmpty())
			return;

		boolean foundMissingOne = false;
		List<SignupSiteWrapper> copyAllSites = new ArrayList<SignupSiteWrapper>(getOtherSites());
		copyAllSites.add(0, getCurrentSite());

		for (SignupSite orgSite : origSiteList) {
			foundMissingOne = true;
			String orgSiteId = orgSite.getSiteId();
			SignupSiteWrapper matchedSiteWrp = null;
			/* scanning for sites */
			boolean lackSiteScope = false;
			for (SignupSiteWrapper cpSiteWrp : copyAllSites) {
				if (cpSiteWrp.getSignupSite().getSiteId().equals(orgSiteId)) {
					matchedSiteWrp = cpSiteWrp;
					if (!cpSiteWrp.isAllowedToCreate() && orgSite.isSiteScope())
						lackSiteScope = true;
					else
						foundMissingOne = false;

					break;
				}

			}
			if (foundMissingOne) {
				this.siteOrGroupTruncated = true;
				if (lackSiteScope)
					getMissingSites().add(orgSite.getTitle() + "  - Lack site scope permission.");
				else
					getMissingSites().add(orgSite.getTitle());
			}

			/* scanning for groups */
			List<SignupGroup> origGroups = orgSite.getSignupGroups();
			if (origGroups == null || origGroups.isEmpty())
				continue;// go next one

			if (matchedSiteWrp == null) {
				/* recording missing groups in this site/group scope */
				for (SignupGroup origGrp : origGroups) {
					getMissingGroups().add(orgSite.getTitle() + "  - " + origGrp.getTitle());
				}
				/* it's not site-wide scope and remove it */
				getMissingSites().remove(orgSite.getTitle());
				continue; // go next one

			}

			List<SignupGroupWrapper> copyGrpWrpList = matchedSiteWrp.getSignupGroupWrappers();
			for (SignupGroup origGrp : origGroups) {
				foundMissingOne = true;
				String origGrpId = origGrp.getGroupId();

				if (copyGrpWrpList == null || copyGrpWrpList.isEmpty()) {
					this.siteOrGroupTruncated = true;
					getMissingGroups().add(orgSite.getTitle() + "  - " + origGrp.getTitle());
					foundMissingOne = false;// already recorded

				}

				for (SignupGroupWrapper cpGrpWrp : copyGrpWrpList) {
					if (origGrpId.equals(cpGrpWrp.getSignupGroup().getGroupId())) {
						foundMissingOne = false;
						break;
					}
				}

				if (foundMissingOne) {
					this.siteOrGroupTruncated = true;
					getMissingGroups().add(orgSite.getTitle() + "  - " + origGrp.getTitle());
				}
			}
		}

	}

	/**
	 * It will show whether there is a site or group get truncated due to
	 * permissions
	 * 
	 * @return true if there is one get truncated.
	 */
	public boolean isSiteOrGroupTruncated() {
		return siteOrGroupTruncated;
	}

	/**
	 * Provide all the missing sites due to permission difference.
	 * 
	 * @return a list of String objects which display the missing site info.
	 */
	public List<String> getMissingSites() {
		return missingSites;
	}

	public void setMissingSites(List<String> missingSites) {
		this.missingSites = missingSites;
	}

	/**
	 * Provide all the missing groups due to permission difference.
	 * 
	 * @return a list of String objects which display the missing groups info.
	 */
	public List<String> getMissingGroups() {
		return missingGroups;
	}

	public void setMissingGroups(List<String> missingGroups) {
		this.missingGroups = missingGroups;
	}

	public void setSignupMeeting(SignupMeeting signupMeeting) {
		this.signupMeeting = signupMeeting;
	}

}
