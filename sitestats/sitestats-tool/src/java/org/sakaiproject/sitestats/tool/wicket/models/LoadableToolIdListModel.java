/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.util.Tools;

/**
 * LoadableDetachableModel for a list of tool id strings. Loads a list of tools from the given site, sorted by tool name.
 * @author plukasew, bjones86
 */
public class LoadableToolIdListModel extends LoadableDetachableModel<List<String>>
{
	private final String siteId;

	/**
	 * Constructor
	 * @param siteId the site id
	 */
	public LoadableToolIdListModel(final String siteId)
	{
		this.siteId = siteId;
	}

	@Override
	protected List<String> load()
	{
		PrefsData pd = Locator.getFacade().getStatsManager().getPreferences(siteId, false);
		List<String> toolIds = Tools.getToolIds(siteId, pd);
		List<String> tools = new ArrayList<>(toolIds.size() + 3);
		tools.add(ReportManager.WHAT_EVENTS_ALLTOOLS);
		tools.add(ReportManager.WHAT_EVENTS_ALLTOOLS_EXCLUDE_CONTENT_READ);
		tools.add(StatsManager.PRESENCE_TOOLID);
		tools.addAll(toolIds);

		// Sort the list by the corresponding tool name (user facing name)
		Collections.sort(tools, new Comparator<String>()
		{
			@Override
			public int compare(String lhs, String rhs)
			{
				String toolName1 = Locator.getFacade().getEventRegistryService().getToolName(lhs);
				String toolName2 = Locator.getFacade().getEventRegistryService().getToolName(rhs);

				return toolName1.compareTo(toolName2);
			}
		});

		return tools;
	}
}
