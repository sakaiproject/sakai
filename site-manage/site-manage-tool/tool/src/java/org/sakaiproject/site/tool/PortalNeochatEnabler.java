/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ParameterParser;

public class PortalNeochatEnabler {
    private static final String STATE_KEY = "isPortalNeochatEnabledForSite";
    private static final String FORM_INPUT_ID = "isPortalNeochatEnabledForSite";
    private static final String CONTEXT_ENABLED_KEY = "isPortalNeochatEnabledForSite";

    /**
     * Add PortalNeochat settings to the context for the edit tools page
     * @param context the context
     * @param site the site
     * @param state the state
     * @return true if context was modified
     */
    public static boolean addToEditToolsContext(Context context, Site site, SessionState state) {
        if (context == null || site == null || state == null || !isEnabledForSite(site)) {
            return false;
        }
        context.put(CONTEXT_ENABLED_KEY, isEnabledForSite(site));
        return true;
    }

    /**
     * Add PortalNeochat settings to the Site Info (view, edit, or confirm) context
     * @param context the context
     * @param site the site
     * @param state the session state
     * @return true if the context was modified
     */
    public static boolean addToSiteInfoContext(Context context, Site site, SessionState state) {
        if (context == null || site == null || state == null) {
            return false;
        }
        context.put(CONTEXT_ENABLED_KEY, isEnabledForSite(site));
        return true;
    }

    /**
     * Applies the PortalNeochat settings to the workflow state
     * @param state the state
     * @param site the site
     * @param params the params
     * @return true if the state was modified
     */
    public static boolean applyToolSettingsToState(SessionState state, Site site, ParameterParser params) {
        // Allow checkbox to set the isPortalNeochatEnabledForSite site property
        // If true or false, add this to the state
        String neoChat = ServerConfigurationService.getString(Site.PROP_SITE_PORTAL_NEOCHAT, "never");
        if ("true".equals(neoChat) || "false".equals(neoChat)) {
            if ("on".equals(params.getString(FORM_INPUT_ID))) {
                state.setAttribute(STATE_KEY, true);
            } else {
                state.setAttribute(STATE_KEY, false);
            }
        }
        return true;
    }

    /**
     * Add PortalNeochat settings to the context for the edit tools confirmation page
     * @param context the context
     * @param site the site
     * @param state the state
     * @return true if the context was modified
     */
    public static boolean addSettingsToEditToolsConfirmationContext(Context context, Site site, SessionState state) {
        if (site == null || context == null || state == null) {
            return false;
        }

        // Show a message on the confirmation screen if PortalNeochat is enabled for the site
        final Boolean isEnabled = (Boolean) state.getAttribute(STATE_KEY);
        if (isEnabled != null && isEnabled) {
            context.put(CONTEXT_ENABLED_KEY, Boolean.TRUE);
            return true;
        }

        return false;
    }

    /**
     * When user selects to enable the PortalNeochat, update the site property
     * @param site The site
     * @param state the session state
     * @return true if the site properties were modified
     */
    public static boolean prepareSiteForSave(Site site, SessionState state) {
        if (site == null || state == null) {
            return false;
        }

        if (state.getAttribute(STATE_KEY) != null) {
            final boolean isEnabled = (Boolean) state.getAttribute(STATE_KEY);
            final ResourcePropertiesEdit props = site.getPropertiesEdit();
            props.removeProperty(Site.PROP_SITE_PORTAL_NEOCHAT);
            if (isEnabled) {
                props.addProperty(Site.PROP_SITE_PORTAL_NEOCHAT, Boolean.TRUE.toString());
            } else {
                props.addProperty(Site.PROP_SITE_PORTAL_NEOCHAT, Boolean.FALSE.toString());
            }
        }

        return true;
    }

    /**
     * Remove the PortalNeochat keys from the given state
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
     * Check the site's properties for the PortalNeochat property
     * @param site The site to check
     * @return true if the PortalNeochat is enabled for the site
     */
    private static boolean isEnabledForSite(Site site) {
        String enabled = site.getProperties().getProperty(Site.PROP_SITE_PORTAL_NEOCHAT);
        String neoChatProperty = ServerConfigurationService.getString(Site.PROP_SITE_PORTAL_NEOCHAT, "never");
        if (enabled == null) {
            if ("true".equals(neoChatProperty) || "false".equals(neoChatProperty)) {
                return Boolean.valueOf(neoChatProperty);
            }
            return false;

        } else {
            return Boolean.parseBoolean(enabled);
        }
    }
}
