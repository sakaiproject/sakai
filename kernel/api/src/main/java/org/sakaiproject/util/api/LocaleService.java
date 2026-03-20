/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.util.api;

import java.util.Locale;

/**
 * Resolves Sakai's effective locale using the standard precedence:
 * site locale, then user preference, then JVM default locale.
 */
public interface LocaleService {

    /**
     * Resolves the effective locale using the current placement/site and current session user.
     *
     * @return effective locale for the current context, never null
     */
    Locale getLocaleForCurrentSiteAndUser();

    /**
     * Resolves the effective locale for an explicit site and user.
     *
     * @param siteId the site id, may be blank
     * @param userId the user id, may be blank
     * @return effective locale for the provided context, never null
     */
    Locale getLocaleForSiteAndUser(String siteId, String userId);
}
