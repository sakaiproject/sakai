/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ParameterParser;

public class LessonsSubnavEnabler {

    private static final String SITE_PROPERTY = "lessons_submenu";
    private static final String STATE_KEY = "isLessonsSubNavEnabledForSite";
    private static final String FORM_INPUT_ID = "isLessonsSubNavEnabledForSite";
    private static final String CONTEXT_ENABLED_KEY = "isLessonsSubNavEnabledForSite";


    /**
     * Add MathJax settings to the context for the edit tools page
     * @param context the context
     * @param site the site
     * @param state the state
     * @return true if context was modified
     */
    public static boolean addToEditToolsContext(Context context, Site site, SessionState state)
    {
        if (context == null || site == null || state == null)
        {
            return false;
        }

        context.put(CONTEXT_ENABLED_KEY, isEnabledForSite(site));

        return true;
    }


    /**
     * Add Lesson subnav settings to the Site Info (view, edit, or confirm) context
     * @param context the context
     * @param site the site
     * @param state the session state
     * @return true if the context was modified
     */
    public static boolean addToSiteInfoContext(Context context, Site site, SessionState state)
    {
        if (context == null || site == null || state == null)
        {
            return false;
        }

        context.put(CONTEXT_ENABLED_KEY, isEnabledForSite(site));

        return true;
    }


    /**
     * Applies the Lessons sub-nav settings to the workflow state
     * @param state the state
     * @param params the params
     * @return true if the state was modified
     */
    public static boolean applyToolSettingsToState(SessionState state, Site site, ParameterParser params)
    {
        // Allow checkbox to set the isLessonsSubNavEnabledForSite site property
        // If checked, add this to the state
        if ("on".equals(params.getString(FORM_INPUT_ID)))
        {
            state.setAttribute(STATE_KEY, true);
        } else {
            state.setAttribute(STATE_KEY, false);
        }

        return true;
    }


    /**
     * Add Lessons subnav settings to the context for the edit tools confirmation page
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

        // Show a message on the confirmation screen if Lesson subnav is enabled for the site
        final boolean isEnabled = (Boolean) state.getAttribute(STATE_KEY);
        if (isEnabled)
        {
            context.put(CONTEXT_ENABLED_KEY, Boolean.TRUE);
            return true;
        }

        return false;
    }


    /**
     * When user selects to enable the Lessons subnav, update the site property
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
     * Remove the Lessons subnav keys from the given state
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
     * Check the site's properties for the Lessons sub nav property
     * @param site The site to check
     * @return true if the lessons subnav is enabled for the site
     */
    private static boolean isEnabledForSite(Site site) {
        return Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROPERTY));
    }
}
