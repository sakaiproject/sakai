/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ParameterParser;

public class SubNavEnabler {

    private static final String SITE_PROPERTY = "subpagenav";
    private static final String STATE_KEY = "isSubPageNavEnabled";
    private static final String FORM_INPUT_ID = "isSubPageNavEnabled";
    private static final String CONTEXT_ENABLED_KEY = "isSubPageNavEnabled";


    /**
     * Add SubNav settings to the context for the edit tools page
     *
     * @param context the context
     * @param site    the site
     * @return true if context was modified
     */
    public static boolean addToContext(Context context, Site site) {
        if (context == null || site == null) return false;

        context.put(CONTEXT_ENABLED_KEY, isEnabledForSite(site));

        return true;
    }

    /**
     * Applies the SubNav settings to the state
     * @param state the state
     * @param params the params
     * @return true if the state was modified
     */
    public static boolean applySettingsToState(SessionState state, ParameterParser params) {
        if ("on".equalsIgnoreCase(params.getString(FORM_INPUT_ID))) {
            state.setAttribute(STATE_KEY, true);
        } else {
            state.setAttribute(STATE_KEY, false);
        }

        return true;
    }


    /**
     * Add the current SubNav state to the context for the edit tools confirmation page
     *
     * @param context the context
     * @param state   the state
     * @return true if the context was modified
     */
    public static boolean addStateToEditToolsConfirmationContext(Context context, SessionState state) {

        if (context == null || state == null) {
            return false;
        }

        final boolean isEnabled = (Boolean) state.getAttribute(STATE_KEY);
        if (isEnabled) {
            context.put(CONTEXT_ENABLED_KEY, Boolean.TRUE);
            return true;
        }

        return false;
    }


    /**
     * When user selects to enable the SubNav, update the site property
     * @param site The site
     * @param state the session state
     * @return true if the site properties were modified
     */
    public static boolean prepareSiteForSave(Site site, SessionState state) {
        if (site == null || state == null) return false;

        if (state.getAttribute(STATE_KEY) != null) {
            final boolean isEnabled = (Boolean) state.getAttribute(STATE_KEY);
            final ResourcePropertiesEdit props = site.getPropertiesEdit();
            if (isEnabled) {
                props.addProperty(SITE_PROPERTY, Boolean.TRUE.toString());
            } else {
                props.removeProperty(SITE_PROPERTY);
            }
        }

        return true;
    }


    /**
     * Remove SubNav from the state
     * @param state the state
     * @return true if the state was modifed
     */
    public static boolean removeFromState(SessionState state) {
        if (state != null) {
            state.removeAttribute(STATE_KEY);
            return true;
        }

        return false;
    }


    /**
     * Check the site's properties for the SubNav property
     * @param site The site to check
     * @return true if SubNav is enabled for the site
     */
    private static boolean isEnabledForSite(Site site) {
        return Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROPERTY));
    }
}
