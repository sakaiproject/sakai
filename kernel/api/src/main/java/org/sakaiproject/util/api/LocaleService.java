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
 * Resolves the effective {@link Locale} for the current Sakai context and provides
 * locale-aware formatting, parsing, and validation of {@code double} values.
 *
 * <h2>Locale resolution precedence</h2>
 * <ol>
 *   <li><b>Site locale</b> &ndash; set by the site administrator in Site Info.</li>
 *   <li><b>User preference locale</b> &ndash; set by the user in their account preferences.</li>
 *   <li><b>JVM default locale</b> &ndash; {@link Locale#getDefault()}, ultimately determined
 *       by the {@code user.language} / {@code user.country} JVM system properties or the
 *       host OS locale.</li>
 * </ol>
 * The first non-null, non-blank locale found at each level is returned; subsequent levels
 * are not consulted.
 *
 * <h2>Number formatting contract</h2>
 * All formatting methods suppress the thousands-grouping separator and allow up to
 * 15 significant fraction digits.  The decimal separator character is locale-specific
 * (e.g. {@code '.'} in {@code en_US}, {@code ','} in {@code de_DE}).
 *
 * <h2>Number parsing contract</h2>
 * Parsing first attempts standard IEEE {@code Double.valueOf()} (dot as decimal separator).
 * Only if that fails is locale-aware parsing attempted.  This means a dot-decimal string
 * such as {@code "3.14"} is always accepted regardless of the active locale, which keeps
 * programmatically generated values interoperable across locales.
 */
public interface LocaleService {

    /**
     * Resolves the effective locale for the caller's current Sakai context.
     *
     * <p>The site is determined from the active tool placement obtained via
     * {@code ToolManager.getCurrentPlacement()}.  The user is the session owner
     * returned by {@code SessionManager.getCurrentSessionUserId()}.  Both lookups
     * are performed defensively; any exception causes that tier to be skipped and
     * resolution continues at the next precedence level.
     *
     * @return the effective locale for the current placement and session user; falls
     *         back to {@link Locale#getDefault()} when neither the site nor the user
     *         has an explicit locale configured; never {@code null}
     */
    Locale getLocaleForCurrentSiteAndUser();

    /**
     * Resolves the effective locale for the supplied site and user identifiers.
     *
     * <p>Resolution follows the standard precedence: site locale, then user preference
     * locale, then {@link Locale#getDefault()}.  Blank or {@code null} values for
     * {@code siteId} or {@code userId} cause the corresponding tier to be skipped
     * without error.
     *
     * @param siteId the Sakai site identifier; blank or {@code null} skips the site tier
     * @param userId the Sakai user identifier; blank or {@code null} skips the user tier
     * @return the effective locale for the given context; falls back to
     *         {@link Locale#getDefault()} when no locale is found; never {@code null}
     */
    Locale getLocaleForSiteAndUser(String siteId, String userId);

    /**
     * Formats {@code value} as a locale-aware decimal string.
     *
     * <p>The thousands-grouping separator is suppressed.  The decimal separator is
     * the one defined by {@code locale} (e.g. a comma for German).  Between 0 and
     * 15 fraction digits are emitted, with trailing zeros omitted.
     *
     * <p>Examples for {@code 1234.5}:
     * <ul>
     *   <li>{@code en_US} &rarr; {@code "1234.5"}</li>
     *   <li>{@code de_DE} &rarr; {@code "1234,5"}</li>
     * </ul>
     *
     * @param value  the value to format; must not be {@code null}
     * @param locale the locale whose decimal symbols are used; must not be {@code null}
     * @return the formatted string; never {@code null}
     */
    String formatDouble(Double value, Locale locale);

    /**
     * Resolves the effective locale for the given site and user, then formats {@code value}.
     *
     * <p>Equivalent to {@code formatDouble(value, getLocaleForSiteAndUser(siteId, userId))}.
     *
     * @param value  the value to format; must not be {@code null}
     * @param siteId the Sakai site identifier; blank or {@code null} skips the site tier
     * @param userId the Sakai user identifier; blank or {@code null} skips the user tier
     * @return the formatted string using the resolved locale; never {@code null}
     */
    String formatDouble(Double value, String siteId, String userId);

    /**
     * Parses a decimal string into a {@code Double} using a two-pass strategy.
     *
     * <ol>
     *   <li>Standard parse via {@link Double#valueOf(String)} (dot as decimal separator).
     *       Succeeds for strings like {@code "3.14"} regardless of locale.</li>
     *   <li>Locale-aware parse via {@link java.text.NumberFormat} with grouping enabled.
     *       Handles strings like {@code "3,14"} when {@code locale} uses a comma decimal.</li>
     * </ol>
     *
     * <p><b>Known JDK limitation (JDK-4745837):</b> The locale-aware fallback uses
     * {@link java.text.NumberFormat} in lenient (default) mode with grouping enabled.
     * In Java releases prior to 23, the lenient parser can ambiguously treat a locale's
     * decimal separator as a grouping separator when both roles are assigned to the same
     * character.  For example, with an {@code es_ES} locale (comma = decimal separator),
     * {@code "4,5"} may be parsed as {@code 45.0} instead of {@code 4.5} because the
     * comma is also recognised as a valid grouping separator in lenient mode.
     * Java 23 introduced {@code NumberFormat.setStrict(true)} (JDK-8327703) as opt-in
     * strict parsing that resolves this ambiguity, but it is not available on Java 17.
     * Callers that need reliable comma-decimal parsing on Java 17 should pre-process the
     * input (e.g. replacing a known locale comma separator with a dot) before invoking
     * this method, or use {@link #isValidDouble(String, Locale)} to validate first.</p>
     *
     * @param origin the string to parse; {@code null} or blank returns {@code null}
     * @param locale the locale used for the fallback parse attempt; must not be {@code null}
     * @return the parsed value, or {@code null} if both parse strategies fail or
     *         {@code origin} is {@code null} / blank
     */
    Double parseDouble(String origin, Locale locale);

    /**
     * Parses a decimal string using the effective locale for the current site and session user.
     *
     * <p>Equivalent to {@code parseDouble(origin, getLocaleForCurrentSiteAndUser())}.
     *
     * @param origin the string to parse; {@code null} or blank returns {@code null}
     * @return the parsed value, or {@code null} if parsing fails or {@code origin} is
     *         {@code null} / blank
     */
    Double parseDouble(String origin);

    /**
     * Parses {@code origin} and re-formats the result using the decimal symbols of
     * {@code locale}, producing a canonical representation for that locale.
     *
     * <p>If parsing fails the original string is returned unchanged, so callers can
     * safely pass arbitrary input without a null/exception guard.  A {@code null}
     * {@code origin} is propagated as {@code null}.
     *
     * <p>Example &ndash; converting a dot-decimal string for a German locale:
     * {@code normalizeDouble("3.14", Locale.GERMANY)} &rarr; {@code "3,14"}.
     *
     * @param origin the string to normalize; may be {@code null}
     * @param locale the target locale; must not be {@code null}
     * @return the locale-formatted value if parsing succeeded, the original string
     *         if parsing failed, or {@code null} if {@code origin} is {@code null}
     */
    String normalizeDouble(String origin, Locale locale);

    /**
     * Parses {@code origin} and re-formats it using the effective locale for the
     * current site and session user.
     *
     * <p>Equivalent to {@code normalizeDouble(origin, getLocaleForCurrentSiteAndUser())}.
     *
     * @param origin the string to normalize; may be {@code null}
     * @return the locale-formatted value if parsing succeeded, the original string
     *         if parsing failed, or {@code null} if {@code origin} is {@code null}
     */
    String normalizeDouble(String origin);

    /**
     * Returns {@code true} if {@code origin} is a syntactically valid number for
     * {@code locale}.
     *
     * <p>Validation is regex-based and uses the grouping and decimal separator
     * characters reported by {@link java.text.DecimalFormatSymbols} for the locale.
     * Accepted forms (using {@code en_US} separators as illustration):
     * <ul>
     *   <li>Plain integer: {@code "42"}</li>
     *   <li>Decimal: {@code "3.14"}</li>
     *   <li>Grouped integer: {@code "1,234,567"}</li>
     *   <li>Grouped decimal: {@code "1,234.56"}</li>
     * </ul>
     *
     * <p><b>Note:</b> this method validates <em>formatting</em>, not range.  A string
     * that exceeds {@code Double.MAX_VALUE} may still return {@code true}.  Use
     * {@link #parseDouble(String, Locale)} to obtain and range-check the value.
     *
     * @param origin the string to validate; must not be {@code null}
     * @param locale the locale whose separators define the valid format; must not be {@code null}
     * @return {@code true} if {@code origin} matches the expected number pattern for
     *         {@code locale}, {@code false} otherwise
     */
    boolean isValidDouble(String origin, Locale locale);

    /**
     * Returns {@code true} if {@code origin} is a syntactically valid number for
     * the effective locale of the current site and session user.
     *
     * <p>Equivalent to {@code isValidDouble(origin, getLocaleForCurrentSiteAndUser())}.
     *
     * @param origin the string to validate; must not be {@code null}
     * @return {@code true} if {@code origin} matches the expected number pattern for
     *         the resolved locale, {@code false} otherwise
     */
    boolean isValidDouble(String origin);

    /**
     * Returns the decimal separator character for the effective locale of the current site
     * and session user (e.g. {@code "."} for {@code en_US}, {@code ","} for {@code de_DE}).
     *
     * <p>This is the direct replacement for {@link org.sakaiproject.util.api.FormattedText#getDecimalSeparator()},
     * with the added benefit that locale is resolved via the full Sakai precedence chain
     * (site locale &rarr; user preference &rarr; JVM default) rather than user preference alone.
     *
     * @return single-character string containing the decimal separator; never {@code null}
     */
    String getDecimalSeparator();
}
