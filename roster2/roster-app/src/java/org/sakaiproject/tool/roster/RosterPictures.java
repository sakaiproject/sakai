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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.jsf.util.LocaleUtil;

public class RosterPictures extends BaseRosterPageBean {
	private static final Log log = LogFactory.getLog(RosterPictures.class);

	/**
	 * Always sort by users' sort names on this page.
	 */
	protected Comparator<Participant> getComparator() {
		return BaseRosterPageBean.sortNameComparator;
	}

	public String getPageTitle() {
        filter.services.eventTrackingService.post(filter.services.eventTrackingService.newEvent("roster.view.photos",getSiteReference(),false));
        return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "title_pictures");
	}
	public boolean isExportablePage() {
		return false;
	}
	public void export(ActionEvent event) {
		log.warn("Can not export roster photos");
	}
	
	public void hideNames(ActionEvent event) {
		prefs.setDisplayNames(false);
	}

	public void showNames(ActionEvent event) {
		prefs.setDisplayNames(true);
	}
	
	/**
	 * JSF (at least myfaces) doesn't translate strings to boolean values for radio
	 * buttons properly.  As a workaround, we build the select items manually.
	 * 
	 * @return
	 */
	public List<SelectItem> getPhotoSelectItems() {
		List<SelectItem> items = new ArrayList<SelectItem>(2);
		items.add(new SelectItem(Boolean.FALSE, LocaleUtil.getLocalizedString(
				FacesContext.getCurrentInstance(), ServicesBean.MESSAGE_BUNDLE, "roster_official_photos")));
		items.add(new SelectItem(Boolean.TRUE, LocaleUtil.getLocalizedString(
				FacesContext.getCurrentInstance(), ServicesBean.MESSAGE_BUNDLE, "roster_profile_photos")));
		return items;
	}

	/**
	 * Override the permission check... since we're already here, display the pictures link
	 */
	public boolean isRenderPicturesLink() {
		return true;
	}

	/**
	 * We render the picture options only if the user can see both official and profile pictures
	 * @return
	 */
	public boolean isRenderPicturesOptions() {
		return filter.services.rosterManager.isOfficialPhotosViewable() && filter.services.rosterManager.isProfilesViewable();
	}

}
