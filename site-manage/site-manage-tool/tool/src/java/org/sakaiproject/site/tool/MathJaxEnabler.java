/**********************************************************************************

 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.site.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.util.ParameterParser;

public class MathJaxEnabler
{
    private static final String ENABLED_SAKAI_PROP = "portal.mathjax.enabled";
    private static final String SRC_PATH_SAKAI_PROP = "portal.mathjax.src.path";
    private static final String VERSION_SERVICE_SAKAI_PROP = "version.service";
    private static final String VERSION_SERVICE_DEFAULT = "Sakai";
    private static final String SRC_PATH_SAKAI_PROP_DEFAULT = "https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=default,Safe";
    private static final boolean ENABLED_SAKAI_PROP_DEFAULT = true;
    
    private static final String SITE_PROP_MATHJAX_ENABLED = "mathJaxEnabled";
    private static final String SITE_PROP_MATHJAX_ALLOWED = "mathJaxAllowed";
    private static final String STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST = "toolRegistrationMathJaxEnabledList";
    private static final String STATE_KEY_IS_MATHJAX_ALLOWED_IN_SITE = "isMathJaxAllowedInSite";
    private static final String CONTEXT_IS_MATHJAX_INSTALLED_KEY = "isMathJaxInstalled";
    private static final String CONTEXT_SAKAI_SERVICE_KEY = "sakaiService";
    private static final String CONTEXT_DO_ENABLE_MATHJAX_KEY = "doEnableMathJax";
    private static final String CONTEXT_MATHJAX_ENABLED_TOOL_ID_SUFFIX_KEY = "mathJaxToolIdSuffix";
    private static final String CONTEXT_MATHJAX_ENABLED_TOOL_ID_SUFFIX_VALUE = "-jax";
    private static final String CONTEXT_CONFIRM_MATHJAX_ENABLED_TOOLS = "confirmMathJaxEnabledTools";
    private static final String PARAM_MATHJAX_ENABLED_TOOLS_KEY = "mathJaxEnabledTools";
    private static final String PARAM_MATHJAX_ALLOWED_KEY = "allowMathJax";
    private static final String TOOL_DELIM = ",";
        
    private static final String SRC_PATH = ServerConfigurationService.getString(SRC_PATH_SAKAI_PROP, SRC_PATH_SAKAI_PROP_DEFAULT);
    private static final boolean ENABLED_AT_SYSTEM_LEVEL = ServerConfigurationService.getBoolean(ENABLED_SAKAI_PROP, ENABLED_SAKAI_PROP_DEFAULT) && !SRC_PATH.trim().isEmpty();
    private static final String SAKAI_SERVICE = ServerConfigurationService.getString(VERSION_SERVICE_SAKAI_PROP, VERSION_SERVICE_DEFAULT);
    
    /**
     * Add MathJax settings to the context for the edit tools page
     * @param context the context
     * @param site the site
     * @param state the state
     * @return true if context was modified
     */
    public static boolean addMathJaxSettingsToEditToolsContext(Context context, Site site, SessionState state)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL  || context == null || site == null || state == null || !isMathJaxAllowedForSite(site, state))
        {
            return false;
        }

        Set<String> mathJaxEnabledTools = getMathJaxEnabledToolsForSite(site, state);
        
        context.put(CONTEXT_IS_MATHJAX_INSTALLED_KEY, Boolean.TRUE);
        context.put(CONTEXT_SAKAI_SERVICE_KEY, SAKAI_SERVICE);
        context.put(STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST, mathJaxEnabledTools);
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
        if (!ENABLED_AT_SYSTEM_LEVEL || site == null || context == null || state == null || !isMathJaxAllowedForSite(site, state))
        {
            return false;
        }
                        
        if (state.getAttribute(STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST) != null)
        {
            Set<String> mathJaxEnabledTools = getMathJaxEnabledToolsForSite(site, state);
            if (!mathJaxEnabledTools.isEmpty())
            {  
                Map<String, String> toolTitleMap = (Map<String, String>) state.getAttribute(toolTitleListKey);
                if (toolTitleMap != null)
                {
                    List<String> titleList = new ArrayList<String>();
                    for (String toolId : toolTitleMap.keySet())
                    {
                        if (mathJaxEnabledTools.contains(toolId) && site.getToolForCommonId(toolId) != null)
                        {
                            titleList.add(toolTitleMap.get(toolId));
                        }
                    }
                    Collections.sort(titleList);

                    context.put(CONTEXT_DO_ENABLE_MATHJAX_KEY, !titleList.isEmpty());
                    context.put(CONTEXT_IS_MATHJAX_INSTALLED_KEY, Boolean.TRUE);
                    context.put(CONTEXT_CONFIRM_MATHJAX_ENABLED_TOOLS, StringUtils.join(titleList, ", "));
                }
            }
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
        context.put(STATE_KEY_IS_MATHJAX_ALLOWED_IN_SITE, Boolean.valueOf(isMathJaxAllowedForSite(site, state)));
        
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
        if (!ENABLED_AT_SYSTEM_LEVEL || site == null || state == null || !isMathJaxAllowedForSite(site, state))
        {
            return false;
        }

        Set<String> mathJaxEnabledTools = (Set<String>) state.getAttribute(STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST);
        String mathJaxEnabledToolString = StringUtils.join(mathJaxEnabledTools, TOOL_DELIM);
        ResourcePropertiesEdit props = site.getPropertiesEdit();
        if (mathJaxEnabledTools != null && !mathJaxEnabledTools.isEmpty())
        {
            props.addProperty(SITE_PROP_MATHJAX_ENABLED, mathJaxEnabledToolString);
        }
        else
        {
            props.removeProperty(SITE_PROP_MATHJAX_ENABLED);
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

        Boolean mathJaxAllowed = (Boolean) state.getAttribute(STATE_KEY_IS_MATHJAX_ALLOWED_IN_SITE);
        if (mathJaxAllowed == null)
        {
            return false;
        }

        if (mathJaxAllowed.booleanValue())
        {
            site.getPropertiesEdit().addProperty(SITE_PROP_MATHJAX_ALLOWED, Boolean.toString(true));
        }
        else
        {
            site.getPropertiesEdit().removeProperty(SITE_PROP_MATHJAX_ALLOWED);
            
            // clear any mathjax enabled tools
            site.getPropertiesEdit().removeProperty(SITE_PROP_MATHJAX_ENABLED); 
            removeMathJaxToolsAttributeFromState(state);
        }

        return true;
    }

    /**
     * Removes the mathjax enabled (list of tools) attribute from the given state
     * @param state the state
     * @return true if the state was modified
     */
    public static boolean removeMathJaxToolsAttributeFromState(SessionState state)
    {
        if (ENABLED_AT_SYSTEM_LEVEL && state != null)
        {
            state.removeAttribute(STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST);
            return true;
        }

        return false;
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
            state.removeAttribute(STATE_KEY_IS_MATHJAX_ALLOWED_IN_SITE);
            return true;
        }

        return false;
    }

    /**
     * Applies the current mathjax tool settings defined in the given params to the given state
     * @param state the state
     * @param params the params
     * @return true if the state was modified
     */
    public static boolean applyToolSettingsToState(SessionState state, Site site, ParameterParser params)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL || state == null || params == null || !isMathJaxAllowedForSite(site, state))
        {
            return false;
        }

        Set<String> mathJaxEnabledTools = new HashSet<String>();
        String[] mathJaxEnabledToolsArray = params.getStrings(PARAM_MATHJAX_ENABLED_TOOLS_KEY);
        if (mathJaxEnabledToolsArray != null)
        {
            mathJaxEnabledTools.addAll(Arrays.asList(mathJaxEnabledToolsArray)); 
        }
        state.setAttribute(STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST, mathJaxEnabledTools);

        return true;
    }

    /**
     * Applies the current mathjax allowed setting defined in the given params to the given state
     * @param state the state
     * @param params the params
     * @return true if the state was modified
     */
    public static boolean applyAllowedSettingsToState(SessionState state, ParameterParser params)
    {
        if (!ENABLED_AT_SYSTEM_LEVEL || state == null || params == null)
        {
            return false;
        }

        state.setAttribute(STATE_KEY_IS_MATHJAX_ALLOWED_IN_SITE, Boolean.valueOf(params.getString(PARAM_MATHJAX_ALLOWED_KEY)));
        return true;
    }

    /**
     * Check the current state of MathJax Tool Support in the given site. Looks at the state first,
     * and if the information cannot be found, reads from site properties
     * @param site The site to enable or disable MathJax Support on, should not be null
     * @param state the session state, should not be null
     * @return returns comma-separated list of tool ids, or empty string if mathjax not enabled in site
     */
    private static Set<String> getMathJaxEnabledToolsForSite(Site site, SessionState state)
    {
        Set<String> mathJaxEnabledTools = (Set<String>) state.getAttribute(STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST);

        if (mathJaxEnabledTools == null)  // no state information, read from site properties instead
        {
            mathJaxEnabledTools = new HashSet<String>();

            String strMathJaxEnabled = site.getProperties().getProperty(SITE_PROP_MATHJAX_ENABLED);
            if (!StringUtils.isBlank(strMathJaxEnabled))
            {
                mathJaxEnabledTools.addAll(Arrays.asList(strMathJaxEnabled.split(TOOL_DELIM)));
                state.setAttribute(STATE_KEY_TOOL_REGISTRATION_MATHJAX_ENABLED_LIST, mathJaxEnabledTools);
            }
        }

        return mathJaxEnabledTools;
    }

    /**
     * Returns whether or not mathjax is allowed in the given site. Checks the state first, if setting
     * is not found in state, checks site properties.
     * @param site the site
     * @param state the state
     * @return true if mathjax is allowed in the site
     */
    private static boolean isMathJaxAllowedForSite(Site site, SessionState state)
    {
        Boolean mathJaxAllowed = (Boolean) state.getAttribute(STATE_KEY_IS_MATHJAX_ALLOWED_IN_SITE);

        if (mathJaxAllowed == null) // no state information, read from site properties instead
        {
            boolean allowed = Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROP_MATHJAX_ALLOWED));
            state.setAttribute(STATE_KEY_IS_MATHJAX_ALLOWED_IN_SITE, Boolean.valueOf(allowed));
            return allowed;
        }

        return mathJaxAllowed.booleanValue();
    }
        
} // end class
