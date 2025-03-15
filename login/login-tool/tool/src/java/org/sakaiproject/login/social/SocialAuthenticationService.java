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

import java.util.Map;
import java.util.Set;

/**
 * Service for handling social authentication providers
 */
public interface SocialAuthenticationService {

    /**
     * Get a set of all supported social authentication providers
     * 
     * @return Set of provider IDs (e.g., "google", "microsoft")
     */
    Set<String> getSupportedProviders();
    
    /**
     * Get details about a specific provider (like display name and icon)
     * 
     * @param providerId The provider ID (e.g., "google", "microsoft")
     * @return Provider details map
     */
    Map<String, String> getProviderDetails(String providerId);
    
    /**
     * Get the URL for the provider's login page
     * 
     * @param providerId The provider ID (e.g., "google", "microsoft")
     * @return Login URL
     */
    String getLoginUrl(String providerId);
    
    /**
     * Check if social authentication is enabled globally
     * 
     * @return true if enabled, false otherwise
     */
    boolean isSocialAuthenticationEnabled();
    
    /**
     * Check if a specific provider is enabled
     * 
     * @param providerId The provider ID to check
     * @return true if the provider is enabled, false otherwise
     */
    boolean isProviderEnabled(String providerId);
    
    /**
     * Get the attribute name to extract from the user profile for Sakai user ID mapping
     * 
     * @param providerId The provider ID
     * @return The attribute name to use for user ID mapping
     */
    String getUserIdAttributeName(String providerId);
}