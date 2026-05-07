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
 * Also provides locale-aware number formatting and parsing utilities.
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

    /**
     * Formats a double value using the separators for the supplied locale.
     */
    String formatDouble(Double value, Locale locale);

    /**
     * Resolves the effective locale for the given site and user, then formats the value.
     */
    String formatDouble(Double value, String siteId, String userId);

    /**
     * Parses a number string using either standard dot decimal notation or the separators
     * for the provided locale. Returns null when parsing fails.
     */
    Double parseDouble(String origin, Locale locale);

    /**
     * Parses a number string using the current user's locale. Returns null when parsing fails.
     */
    Double parseDouble(String origin);

    /**
     * Parses the string and re-formats it using the separators for the supplied locale.
     * Returns the original string unchanged when it cannot be parsed.
     */
    String normalizeDouble(String origin, Locale locale);

    /**
     * Parses the string and re-formats it using the current user's locale.
     * Returns the original string unchanged when it cannot be parsed.
     */
    String normalizeDouble(String origin);

    /**
     * Returns true if the string is a validly formatted number for the supplied locale.
     */
    boolean isValidDouble(String origin, Locale locale);

    /**
     * Returns true if the string is a validly formatted number for the current user's locale.
     */
    boolean isValidDouble(String origin);
}
