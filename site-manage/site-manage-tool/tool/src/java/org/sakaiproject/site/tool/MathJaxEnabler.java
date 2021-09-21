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
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ParameterParser;

public class MathJaxEnabler
{
    private static final String ENABLED_SAKAI_PROP = "portal.mathjax.enabled";
    private static final String ENABLED_SAKAI_PROP_NEW_SITE = "portal.mathjax.newSites.enabled";
    private static final String SRC_PATH_SAKAI_PROP = "portal.mathjax.src.path";
    public static final String HELP_URL_SAKAI_PROP = "portal.mathjax.help.url";
    private static final String VERSION_SERVICE_SAKAI_PROP = "version.service";
    private static final String VERSION_SERVICE_DEFAULT = "Sakai";
    private static final boolean ENABLED_SAKAI_PROP_DEFAULT = true;
    
    private static final String STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE = "isMathJaxEnabledForSite";
    private static final String CONTEXT_IS_MATHJAX_INSTALLED_KEY = "isMathJaxInstalled";
    private static final String CONTEXT_SAKAI_SERVICE_KEY = "sakaiService";
    private static final String CONTEXT_DO_ENABLE_MATHJAX_KEY = "doEnableMathJax";
    private static final String CONTEXT_MATHJAX_ENABLED_TOOL_ID_SUFFIX_KEY = "mathJaxToolIdSuffix";
    private static final String CONTEXT_MATHJAX_ENABLED_TOOL_ID_SUFFIX_VALUE = "-jax";
    public static final String CONTEXT_MATHJAX_HELP_URL = "mathJaxHelpURL";
    private static final String PARAM_MATHJAX_ENABLED_KEY = "isMathJaxEnabledForSite";
        
    private static final String SRC_PATH = ServerConfigurationService.getString(SRC_PATH_SAKAI_PROP);
    private static final boolean ENABLED_AT_SYSTEM_LEVEL = ServerConfigurationService.getBoolean(ENABLED_SAKAI_PROP, ENABLED_SAKAI_PROP_DEFAULT) && !SRC_PATH.trim().isEmpty();
    private static final String SAKAI_SERVICE = ServerConfigurationService.getString(VERSION_SERVICE_SAKAI_PROP, VERSION_SERVICE_DEFAULT);
    public static final String HELP_URL = ServerConfigurationService.getString(HELP_URL_SAKAI_PROP, ServerConfigurationService.getPortalUrl() + "/help/TOCDisplay/content.hlp?docId=howdoiaddlatexlanguagetomycoursesite");
    
    /**
     * Add MathJax settings to the context for the edit tools page
     * @param context the context
     * @param site the site
     * @param state the state
     * @return true if context was modified
     */
    public static boolean addMathJaxSettingsToEditToolsContext(Context context, Site site, SessionState state)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL  || context == null || site == null || state == null || !isMathJaxEnabledForSite(site, state))
        {
            return false;
        }

        context.put(CONTEXT_IS_MATHJAX_INSTALLED_KEY, Boolean.TRUE);
        context.put(CONTEXT_SAKAI_SERVICE_KEY, SAKAI_SERVICE);
        context.put(CONTEXT_MATHJAX_ENABLED_TOOL_ID_SUFFIX_KEY, CONTEXT_MATHJAX_ENABLED_TOOL_ID_SUFFIX_VALUE);

        return true;
    }
    
    /**
     * Add MathJax settings to the context for the edit tools confirmation page
     * @param context the context
     * @param site the site
     * @param state the state
     * @return true if the context was modified
     */
    public static boolean addMathJaxSettingsToEditToolsConfirmationContext(Context context, Site site, SessionState state, String toolTitleListKey)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL || site == null || context == null || state == null || !isMathJaxEnabledForSite(site, state))
        {
            return false;
        }

        // show a message on the confirmation screen if MathJax is enabled for the site
        boolean isMathJaxEnabledForSite = (Boolean) state.getAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE);

        if (isMathJaxEnabledForSite) {
            context.put(CONTEXT_IS_MATHJAX_INSTALLED_KEY, Boolean.TRUE);
            context.put(CONTEXT_DO_ENABLE_MATHJAX_KEY, Boolean.TRUE);
        }
        return true;
    }
    
    /**
     * Add MathJax settings to the Site Info (view, edit, or confirm) context 
     * @param context the context
     * @param site the site
     * @param state the session state
     * @return true if the context was modified
     */
    public static boolean addMathJaxSettingsToSiteInfoContext(Context context, Site site, SessionState state)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL || context == null || site == null || state == null)
        {
            return false;
        }
        
        context.put(CONTEXT_IS_MATHJAX_INSTALLED_KEY, Boolean.TRUE);
        context.put(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE, Boolean.valueOf(isMathJaxEnabledForSite(site, state)));
        
        return true;
    }
    
    /**
     * When user selects to enable mathjax in certain tools, update the site property
     * @param site The site
     * @param state the session state
     * @return true if the site properties were modified
     */
    public static boolean prepareMathJaxToolSettingsForSave(Site site, SessionState state)
    {   
        if (!ENABLED_AT_SYSTEM_LEVEL || site == null || state == null)
        {
            return false;
        }

        if (state.getAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE) != null) {
            boolean isMathJaxEnabledForSite = (Boolean) state.getAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE);
            ResourcePropertiesEdit props = site.getPropertiesEdit();
            if (isMathJaxEnabledForSite) {
                props.addProperty(Site.PROP_SITE_MATHJAX_ALLOWED, Boolean.TRUE.toString());
            } else {
                props.removeProperty(Site.PROP_SITE_MATHJAX_ALLOWED);
            }
        }

        return true;
    }

    /**
     * When user selects to enable/disable mathjax support, update the site properties
     * @param site the site
     * @param state the session state
     * @return true if the site properties were modified
     */
    public static boolean prepareMathJaxAllowedSettingsForSave(Site site, SessionState state)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL || site == null || state == null)
        {
            return false;
        }

        Boolean mathJaxAllowed = (Boolean) state.getAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE);
        if (mathJaxAllowed == null)
        {
            return false;
        }

        if (mathJaxAllowed.booleanValue())
        {
            site.getPropertiesEdit().addProperty(Site.PROP_SITE_MATHJAX_ALLOWED, Boolean.toString(true));
        }
        else
        {
            site.getPropertiesEdit().removeProperty(Site.PROP_SITE_MATHJAX_ALLOWED);
        }

        return true;
    }

    /**
     * Remove the mathjax allowed attribute from the given state
     * @param state the state
     * @return true if the state was modifed
     */
    public static boolean removeMathJaxAllowedAttributeFromState(SessionState state)
    {
        if (ENABLED_AT_SYSTEM_LEVEL && state != null)
        {
            state.removeAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE);
            return true;
        }

        return false;
    }

    /**
     * Applies the current mathjax settings defined in the given params to the given state
     * @param state the state
     * @param params the params
     * @return true if the state was modified
     */
    public static boolean applySettingsToState(SessionState state, ParameterParser params)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL || state == null || params == null)
        {
            return false;
        }

        // Allow checkbox to set the isMathJaxAllowedInSite site property
        // If checked, add this to the state
        if ("on".equals(params.getString(PARAM_MATHJAX_ENABLED_KEY)))
        {
            state.setAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE, true);
        } else {
            state.setAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE, false);
        }

        return true;
    }


    /**
     * Returns whether or not mathjax is allowed in the given site. Checks the state first, if setting
     * is not found in state, checks site properties.
     * @param site the site
     * @param state the state
     * @return true if mathjax is allowed in the site
     */
    private static boolean isMathJaxEnabledForSite(Site site, SessionState state)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL || state == null)
        {
            return false;
        }

        Boolean mathJaxEnabled = (Boolean) state.getAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE);

        if (mathJaxEnabled == null) // no state information, read from site properties instead
        {
            boolean enabled = Boolean.parseBoolean(site.getProperties().getProperty(Site.PROP_SITE_MATHJAX_ALLOWED));
            state.setAttribute(STATE_KEY_IS_MATHJAX_ENABLED_FOR_SITE, Boolean.valueOf(enabled));
            return enabled;
        }

        return mathJaxEnabled.booleanValue();
    }
        
} // end class
