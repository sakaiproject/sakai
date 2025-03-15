/**
 * Copyright (c) 2003-2025 The Apereo Foundation
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
package org.sakaiproject.login.social;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Implementation of the SocialAuthenticationService
 */
@Slf4j
public class SocialAuthenticationServiceImpl implements SocialAuthenticationService {

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private ClientRegistrationRepository clientRegistrationRepository;
    
    private static final String CONFIG_PREFIX = "social.authentication.";
    private static final String CONFIG_ENABLED = CONFIG_PREFIX + "enabled";
    
    // Provider-specific configurations
    private static final String PROVIDER_PREFIX = CONFIG_PREFIX + "provider.";
    private static final String PROVIDER_ENABLED = ".enabled";
    private static final String PROVIDER_DISPLAY_NAME = ".displayName";
    private static final String PROVIDER_ICON = ".icon";
    private static final String PROVIDER_USER_ID_ATTRIBUTE = ".userIdAttribute";
    
    // Default configurations for the supported providers
    private static final Map<String, Map<String, String>> DEFAULT_PROVIDER_CONFIGS = new HashMap<>();
    
    static {
        // Google defaults
        Map<String, String> googleDefaults = new HashMap<>();
        googleDefaults.put(PROVIDER_DISPLAY_NAME, "Google");
        googleDefaults.put(PROVIDER_ICON, "bi-google");
        googleDefaults.put(PROVIDER_USER_ID_ATTRIBUTE, "email");
        DEFAULT_PROVIDER_CONFIGS.put("google", googleDefaults);
        
        // Microsoft defaults
        Map<String, String> microsoftDefaults = new HashMap<>();
        microsoftDefaults.put(PROVIDER_DISPLAY_NAME, "Microsoft");
        microsoftDefaults.put(PROVIDER_ICON, "bi-microsoft");
        microsoftDefaults.put(PROVIDER_USER_ID_ATTRIBUTE, "mail");
        DEFAULT_PROVIDER_CONFIGS.put("microsoft", microsoftDefaults);
    }
    
    @Override
    public Set<String> getSupportedProviders() {
        if (!isSocialAuthenticationEnabled()) {
            return Collections.emptySet();
        }
        
        Set<String> enabledProviders = new HashSet<>();
        
        for (String providerId : DEFAULT_PROVIDER_CONFIGS.keySet()) {
            if (isProviderEnabled(providerId)) {
                enabledProviders.add(providerId);
            }
        }
        
        return enabledProviders;
    }

    @Override
    public Map<String, String> getProviderDetails(String providerId) {
        if (!isProviderEnabled(providerId)) {
            return Collections.emptyMap();
        }
        
        Map<String, String> details = new HashMap<>();
        Map<String, String> defaults = DEFAULT_PROVIDER_CONFIGS.getOrDefault(providerId, Collections.emptyMap());
        
        // Get display name from configuration or default
        String displayNameKey = PROVIDER_PREFIX + providerId + PROVIDER_DISPLAY_NAME;
        String displayName = serverConfigurationService.getString(displayNameKey, defaults.get(PROVIDER_DISPLAY_NAME));
        details.put("displayName", displayName);
        
        // Get icon from configuration or default
        String iconKey = PROVIDER_PREFIX + providerId + PROVIDER_ICON;
        String icon = serverConfigurationService.getString(iconKey, defaults.get(PROVIDER_ICON));
        details.put("icon", icon);
        
        return details;
    }

    @Override
    public String getLoginUrl(String providerId) {
        if (!isProviderEnabled(providerId)) {
            return null;
        }
        
        try {
            ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(providerId);
            if (registration != null) {
                return "/login/oauth2/authorization/" + providerId;
            }
        } catch (Exception e) {
            log.error("Error getting client registration for provider: {}", providerId, e);
        }
        
        return null;
    }

    @Override
    public boolean isSocialAuthenticationEnabled() {
        return serverConfigurationService.getBoolean(CONFIG_ENABLED, false);
    }

    @Override
    public boolean isProviderEnabled(String providerId) {
        if (!isSocialAuthenticationEnabled()) {
            return false;
        }
        
        // Check if provider is a supported one
        if (!DEFAULT_PROVIDER_CONFIGS.containsKey(providerId)) {
            return false;
        }
        
        // Check provider-specific enabled flag
        String providerEnabledKey = PROVIDER_PREFIX + providerId + PROVIDER_ENABLED;
        return serverConfigurationService.getBoolean(providerEnabledKey, false);
    }

    @Override
    public String getUserIdAttributeName(String providerId) {
        if (!isProviderEnabled(providerId)) {
            return null;
        }
        
        Map<String, String> defaults = DEFAULT_PROVIDER_CONFIGS.getOrDefault(providerId, Collections.emptyMap());
        String userIdAttributeKey = PROVIDER_PREFIX + providerId + PROVIDER_USER_ID_ATTRIBUTE;
        return serverConfigurationService.getString(userIdAttributeKey, defaults.get(PROVIDER_USER_ID_ATTRIBUTE));
    }
}