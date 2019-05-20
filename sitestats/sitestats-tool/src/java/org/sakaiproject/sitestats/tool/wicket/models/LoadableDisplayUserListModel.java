/**
 * Copyright (c) 2006-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableDisplayUserListModel.DisplayUser;
import org.sakaiproject.sitestats.tool.wicket.util.Comparators;

/**
 * LoadableDetachableModel for a list of DisplayUser objects. Loads with all trackable users from the
 * given site.
 * @author plukasew
 */
public class LoadableDisplayUserListModel extends LoadableDetachableModel<List<DisplayUser>>
{
	private final String siteId;

	/**
	 * Constructor
	 * @param siteId the site id to get the users for
	 */
	public LoadableDisplayUserListModel(String siteId)
	{
		this.siteId = siteId;
	}

	@Override
	protected List<DisplayUser> load()
	{
		List<String> users = Locator.getFacade().getDetailedEventsManager().getUsersForTracking(siteId);
		List<DisplayUser> displayUsers = users.stream().map(u -> new DisplayUser(u, siteId))
				.sorted(Comparators.getDisplayUserComparator()).collect(Collectors.toList());
		displayUsers.add(0, DisplayUser.NONE);
		return displayUsers;
	}

	public static class DisplayUser implements Serializable
	{
		public static final DisplayUser NONE = new DisplayUser(ReportManager.WHO_NONE, "");

		public final String userId, display;

		public DisplayUser (String userId, String siteId)
		{
			this.userId = userId;
			display = Locator.getFacade().getStatsManager().getUserInfoForDisplay(userId, siteId);
		}
	}
}
