/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.microsoft.api.MicrosoftCommonService;
import org.sakaiproject.microsoft.api.MicrosoftConfigurationService;
import org.sakaiproject.microsoft.api.MicrosoftSynchronizationService;
import org.sakaiproject.microsoft.api.data.MicrosoftCredentials;
import org.sakaiproject.microsoft.api.model.SiteSynchronization;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class MicrosoftSynchronizationEnabler {

    private static final String SITE_PROPERTY       = "microsoftSynchronization";
    private static final String STATE_KEY           = "isMicrosoftSynchronizationEnabled";
    private static final String FORM_INPUT_ID       = "isMicrosoftSynchronizationEnabled";
    private static final String CONTEXT_ENABLED_KEY = "isMicrosoftSynchronizationEnabled";
    private static final String CONTEXT_ALLOWED_KEY = "isMicrosoftSynchronizationAllowed";

    private static final String SAKAI_PROPERTY_SITETYPE = "microsoft.forced.synchronization.sitetype";
    private static final String SAKAI_PROPERTY_SITE_PROPERTY_NAME = "microsoft.forced.synchronization.propertyname";

    private static final ResourceLoader rb = new ResourceLoader("sitesetupgeneric");

    /**
     * Add MicrosoftSynchronization settings to the context for the edit tools page.
     * The checkbox will only be available if the site type is allowed in sakai.properties.
     *
     * @param context the context
     * @param site    the site
     * @param state   the session state
     * @return true if context was modified
     */
	public static boolean addToContext(Context context, Site site, SessionState state) {
        if (context == null || site == null) return false;

        final boolean isAllowed = isSiteTypeAllowed(site);
        context.put(CONTEXT_ALLOWED_KEY, isAllowed);

        if (isAllowed) {
            context.put(CONTEXT_ENABLED_KEY, isEnabledForSite(site));
        }

        if (state != null) {
            final String alertMessage = (String) state.getAttribute("alertMessage");
            if (alertMessage != null) {
                context.put("alertMessage", alertMessage);
                state.removeAttribute("alertMessage");
            }
        }

        return true;
    }

    /**
     * Applies the MicrosoftSynchronization settings to the state.
     * Only applies if the site type is allowed in sakai.properties.
     *
     * @param state  the state
     * @param params the params
     * @param site   the site
     * @return true if the state was modified
     */
    public static boolean applySettingsToState(SessionState state, ParameterParser params, Site site) {
        if (state == null || params == null || site == null) return false;

        if (!isSiteTypeAllowed(site)) {
            return false;
        }

        if ("on".equalsIgnoreCase(params.getString(FORM_INPUT_ID))) {
            state.setAttribute(STATE_KEY, true);
        } else {
            state.setAttribute(STATE_KEY, false);
        }

        return true;
    }

    /**
     * Add the current MicrosoftSynchronization state to the context for the edit tools confirmation page.
     *
     * @param context the context
     * @param state   the state
     * @return true if the context was modified
     */
    public static boolean addStateToEditToolsConfirmationContext(Context context, SessionState state) {
        if (context == null || state == null) {
            return false;
        }

        final Boolean isEnabled = (Boolean) state.getAttribute(STATE_KEY);
        if (Boolean.TRUE.equals(isEnabled)) {
            context.put(CONTEXT_ENABLED_KEY, Boolean.TRUE);
            return true;
        }

        return false;
    }

    /**
     * When user selects to enable MicrosoftSynchronization, update the site property
     * and create the Microsoft Team + SiteSynchronization if it was just enabled.
     * Only persists if the site type is allowed in sakai.properties.
     *
     * @param site                            the site
     * @param state                           the session state
     * @return true if the site properties were modified
     */
    public static boolean prepareSiteForSave(Site site, SessionState state) {
        if (site == null || state == null) return false;

        if (!isSiteTypeAllowed(site)) {
            return false;
        }

        if (state.getAttribute(STATE_KEY) != null) {
            final boolean isEnabled = (Boolean) state.getAttribute(STATE_KEY);
            final ResourcePropertiesEdit props = site.getPropertiesEdit();
            final String[] nameAndValueForSynchronization = getSitePropertyNameAndValueForSynchronization(site);
            MicrosoftCommonService microsoftCommonService =  (MicrosoftCommonService) ComponentManager.get(MicrosoftCommonService.class);
            MicrosoftSynchronizationService microsoftSynchronizationService = (MicrosoftSynchronizationService) ComponentManager.get(MicrosoftSynchronizationService.class);

            if (isEnabled) {
                props.addProperty(SITE_PROPERTY, Boolean.TRUE.toString());

                MicrosoftConfigurationService microsoftConfigurationService = (MicrosoftConfigurationService) ComponentManager.get(MicrosoftConfigurationService.class);

                // Add site property defined in microsoft.forced.synchronization.propertyname (only if not already present)
                if (nameAndValueForSynchronization.length > 0) {
                	final String propName = nameAndValueForSynchronization[0];
                    final String propValue = nameAndValueForSynchronization[1];

                    // Validate propValue is not empty
                    if (propValue == null || propValue.isEmpty()) {
                        log.error("Cannot enable Microsoft Synchronization: property value for '{}' is empty in site: {}", propName, site.getId());
                        state.setAttribute("alertMessage", rb.getString("sinfo.error.enabling.synchronization.term_eid.empty"));
                        state.setAttribute(STATE_KEY, false);
                        return false;
                    }

                    if (props.getProperty(propName) == null) {
                        props.addProperty(propName, propValue);
                        log.debug("Added site property {}={} for site: {}", propName, propValue, site.getId());
                    }
                }

                createOrRestoreMicrosoftTeamForSite(site, microsoftCommonService, microsoftConfigurationService, microsoftSynchronizationService);

                
            } else {
                props.removeProperty(SITE_PROPERTY);

                // If a SiteSynchronization exists with a Team, archive it
                if (microsoftSynchronizationService != null && microsoftCommonService != null) {
                    List<SiteSynchronization> ssList = microsoftSynchronizationService.getSiteSynchronizationsBySite(site.getId());
                    if (ssList != null) {
                        for (SiteSynchronization ss : ssList) {
                            // If the SiteSynchronization has a Team, archive it first
                            if (ss.getTeamId() != null && !ss.getTeamId().isEmpty()) {
                                try {
                                    if (microsoftCommonService.archiveTeam(ss.getTeamId())) {
                                        log.info("Microsoft Team archived for site: {}, teamId: {}", site.getId(), ss.getTeamId());
                                    }
                                } catch (Exception e) {
                                    log.warn("Could not archive Microsoft Team for site: {}, teamId: {}, {}", site.getId(), ss.getTeamId(), e.toString());
                                }
                            }
                            // Disable and update the SiteSynchronization
                            ss.setDisabled(true);
                            microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
                            log.info("SiteSynchronization disabled for site: {}", site.getId());
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Remove MicrosoftSynchronization from the state.
     *
     * @param state the state
     * @return true if the state was modified
     */
    public static boolean removeFromState(SessionState state) {
        if (state != null) {
            state.removeAttribute(STATE_KEY);
            return true;
        }

        return false;
    }

    /**
     * Creates or restores a Microsoft Team for the given site and saves the SiteSynchronization.
     * If a SiteSynchronization already exists for the site, the associated Team is unarchived and restored.
     * Otherwise, a new Team is created and a new SiteSynchronization is saved.
     *
     * @param site                            the site
     * @param microsoftCommonService          the Microsoft common service
     * @param microsoftConfigurationService   the Microsoft configuration service
     * @param microsoftSynchronizationService the Microsoft synchronization service
     */
    private static void createOrRestoreMicrosoftTeamForSite(
            Site site,
            MicrosoftCommonService microsoftCommonService,
            MicrosoftConfigurationService microsoftConfigurationService,
            MicrosoftSynchronizationService microsoftSynchronizationService) {

        if (microsoftCommonService == null || microsoftConfigurationService == null || microsoftSynchronizationService == null) {
            log.warn("One or more Microsoft services are null, cannot create team for site: {}", site.getId());
            return;
        }

        try {
            final MicrosoftCredentials credentials = microsoftConfigurationService.getCredentials();

            if (credentials == null || credentials.getEmail() == null) {
                log.warn("Could not resolve Microsoft credentials email for site: {}, team will not be created", site.getId());
                return;
            }

            final long syncDuration = microsoftConfigurationService.getSyncDuration();
            final ZonedDateTime syncDateFrom = ZonedDateTime.now();
            final ZonedDateTime syncDateTo = ZonedDateTime.now().plusMonths(syncDuration);

            List<SiteSynchronization> existingSsList = microsoftSynchronizationService.getSiteSynchronizationsBySite(site.getId());
            if (existingSsList != null && !existingSsList.isEmpty()) {
                // Team already existed before — unarchive it and restore the SiteSynchronization
                for (SiteSynchronization existingSs : existingSsList) {
                    if (existingSs.getTeamId() != null && !existingSs.getTeamId().isEmpty()) {
                        try {
                            if (!microsoftCommonService.unarchiveTeam(existingSs.getTeamId())) {
                                log.error("Could not unarchive Microsoft Team for site: {}, teamId: {}, skipping state update", site.getId(), existingSs.getTeamId());
                                continue;
                            }
                        } catch (Exception e) {
                            log.error("Could not unarchive Microsoft Team for site: {}, teamId: {}, {}", site.getId(), existingSs.getTeamId(), e.toString());
                            continue;
                        }
                    }
                    // Only reached if unarchiveTeam succeeded
                    existingSs.setSyncDateFrom(syncDateFrom);
                    existingSs.setSyncDateTo(syncDateTo);
                    existingSs.setDisabled(false);
                    microsoftSynchronizationService.saveOrUpdateSiteSynchronization(existingSs);
                    log.info("SiteSynchronization restored for site: {}, teamId: {}", site.getId(), existingSs.getTeamId());
                }
            } else {
                // No existing SiteSynchronization — create a new Team and SiteSynchronization
                final String teamId;
                try {
                    teamId = microsoftCommonService.createTeam(site.getTitle(), credentials.getEmail());
                } catch (Exception e) {
                    log.error("Microsoft Team creation failed for site: {}, {}", site.getId(), e.toString());
                    return;
                }

                if (teamId == null || teamId.isEmpty()) {
                    log.warn("Microsoft Team creation returned null or empty teamId for site: {}", site.getId());
                    return;
                }

                final SiteSynchronization ss = SiteSynchronization.builder()
                        .siteId(site.getId())
                        .teamId(teamId)
                        .forced(true)
                        .syncDateFrom(syncDateFrom)
                        .syncDateTo(syncDateTo)
                        .build();

                microsoftSynchronizationService.saveOrUpdateSiteSynchronization(ss);
                log.info("Microsoft Team created and SiteSynchronization saved: teamId={}, siteId={}", teamId, site.getId());
            }
        } catch (Exception e) {
            log.error("Unexpected error while creating Microsoft Team for site: {}, {}", site.getId(), e.toString());
        }
    }

    /**
     * Check the site's properties for the MicrosoftSynchronization property.
     *
     * @param site the site to check
     * @return true if MicrosoftSynchronization is enabled for the site
     */
    private static boolean isEnabledForSite(Site site) {
        return Boolean.parseBoolean(site.getProperties().getProperty(SITE_PROPERTY));
    }

    /**
     * Check whether the site's type is allowed to use MicrosoftSynchronization,
     * based on the "microsoft.forced.synchronization.sitetype" property in sakai.properties.
     * Use "all" to allow all site types, or a comma-separated list (e.g. "course,project").
     *
     * @param site the site to check
     * @return true if the site type is allowed
     */
    private static boolean isSiteTypeAllowed(Site site) {
        final String configValue = ServerConfigurationService.getString(SAKAI_PROPERTY_SITETYPE, "").trim();

        if (configValue.isEmpty()) {
            return false;
        }

        if ("all".equalsIgnoreCase(configValue)) {
            return true;
        }

        final String siteType = site.getType();
        if (siteType == null || siteType.isEmpty()) {
            return false;
        }

        final List<String> allowedTypes = Arrays.asList(configValue.split("\\s*,\\s*"));
        return allowedTypes.contains(siteType);
    }

    /**
     * Retrieves the property name and value to be used for Microsoft synchronization from the site.
     * <p>
     * The property name is obtained from the Sakai configuration property defined by
     * "microsoft.forced.synchronization.propertyname". The property value is taken from the site's
     * "term_eid" property. If the configuration property is not set or is blank, an empty array is returned.
     * If the site's "term_eid" property is not set, the value will be an empty string.
     *
     * @param site the site from which to retrieve the property name and value
     * @return a String array with two elements: [0] = property name, [1] = property value;
     *         or an empty array if the property name is not configured
     */
    private static String[] getSitePropertyNameAndValueForSynchronization(Site site) {
        final String propertyName = ServerConfigurationService.getString(SAKAI_PROPERTY_SITE_PROPERTY_NAME, null);
        if (propertyName  == null || propertyName.isBlank()) {
            return new String[0];
        }
        final String propertyValue = site.getProperties().getProperty("term_eid");
        return new String[] { propertyName.trim(), propertyValue != null ? propertyValue : "" };
    }
}