/**
 * Copyright (c) 2003-2023 The Apereo Foundation
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
package org.sakaiproject.site.tool;

import java.util.Collection;
import java.util.List;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.sitemanage.api.SiteManageConstants;
import org.sakaiproject.util.ParameterParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradebookGroupEnabler {

	private static final String SITE_PROPERTY = "gradebook_group";
	public static final String STATE_KEY = "isGradebookGroupEnabledForSite";
	public static final String FORM_INPUT_ID = "gradebookType";
	public static final String SELECTED_GROUPS = "selectedGroups";

	public static final String VALUE_GRADEBOOK_SITE = "site";
	public static final String VALUE_GRADEBOOK_GROUPS = "groups";

	/**
	 * Add Gradebook group settings to the context for the edit tools confirmation page
	 * @param context the context
	 * @param site the site
	 * @param state the state
	 * @return true if the context was modified
	 */
	public static boolean addSettingsToEditToolsConfirmationContext(Context context, Site site, SessionState state)
	{

		if (site == null || context == null || state == null)
		{
			return false;
		}

		log.debug("state.getAttribute(selectedGroups) " + state.getAttribute(SELECTED_GROUPS));
		List<String> selectedGroups = (List<String>)state.getAttribute(SELECTED_GROUPS);
		context.put(SELECTED_GROUPS, selectedGroups);
		context.put(FORM_INPUT_ID, FORM_INPUT_ID);

		return true;
	}

	/**
	 * When user selects to enable the Gradebook group instances, update the site property
	 * @param site The site
	 * @param state the session state
	 * @return true if the site properties were modified
	 */
	public static boolean prepareSiteForSave(Site site, SessionState state)
	{
		if (site == null || state == null)
		{
			return false;
		}
		
		log.debug("state.getAttribute(FORM_INPUT_ID) " + state.getAttribute(FORM_INPUT_ID));
		final ResourcePropertiesEdit props = site.getPropertiesEdit();
		Collection<ToolConfiguration> gbs = site.getTools(SiteManageConstants.GRADEBOOK_TOOL_ID);
		if (state.getAttribute(FORM_INPUT_ID) != null) {
			if (VALUE_GRADEBOOK_GROUPS.equals(state.getAttribute(FORM_INPUT_ID)) && gbs.size() > 0) {
				props.addProperty(SITE_PROPERTY, Boolean.TRUE.toString());
			} else if (gbs == null || gbs.size() == 0 || VALUE_GRADEBOOK_SITE.equals(state.getAttribute(FORM_INPUT_ID))) {//only remove it if we find the site one or if there are no gb
				props.removeProperty(SITE_PROPERTY);
			}
		}

		return true;
	}

	/**
	 * Remove the Gradebook group keys from the given state
	 * @param state the state
	 * @return true if the state was modifed
	 */
	public static boolean removeFromState(SessionState state)
	{
		if (state != null)
		{
			state.removeAttribute(STATE_KEY);
			return true;
		}

		return false;
	}

	/**
	 * Check the site's properties for the Gradebook group nav property
	 * @param site The site to check
	 * @return true if the Gradebook group is enabled for the site
	 */
	public static boolean isEnabledForSite(Site site) {
		log.debug("isEnabledForSite " + site.getProperties().getProperty(SITE_PROPERTY));
		return Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROPERTY));
	}

	/**
	 * Check the state's properties for the Gradebook group nav property
	 * @param state The current vm state
	 * @return true if the Gradebook group is being enabled
	 */
	public static boolean isEnablingForSite(SessionState state) {
		log.debug("isEnabledForSite2 " + VALUE_GRADEBOOK_GROUPS.equals(state.getAttribute(FORM_INPUT_ID)));
		return VALUE_GRADEBOOK_GROUPS.equals(state.getAttribute(FORM_INPUT_ID));
	}
}
