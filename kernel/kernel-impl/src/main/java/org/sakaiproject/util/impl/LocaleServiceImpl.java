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
package org.sakaiproject.util.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.api.LocaleService;

@Slf4j
@Setter
public class LocaleServiceImpl implements LocaleService {

    private SiteService siteService;
    private PreferencesService preferencesService;
    private ToolManager toolManager;
    private SessionManager sessionManager;

    public void init() {
        Objects.requireNonNull(siteService);
        Objects.requireNonNull(preferencesService);
        Objects.requireNonNull(toolManager);
        Objects.requireNonNull(sessionManager);
    }

    @Override
    public Locale getLocaleForCurrentSiteAndUser() {
        String siteId = null;

        try {
            Placement currentPlacement = toolManager.getCurrentPlacement();
            siteId = currentPlacement != null ? currentPlacement.getContext() : null;
        } catch (Exception e) {
            log.debug("Unable to resolve current placement while resolving locale", e);
        }

        String userId = sessionManager.getCurrentSessionUserId();
        return getLocaleForSiteAndUser(siteId, userId);
    }

    @Override
    public Locale getLocaleForSiteAndUser(String siteId, String userId) {
        if (StringUtils.isNotBlank(siteId)) {
            try {
                Optional<Locale> siteLocale = siteService.getSiteLocale(siteId);
                if (siteLocale != null && siteLocale.isPresent()) {
                    return siteLocale.get();
                }
            } catch (Exception e) {
                log.debug("Unable to resolve site locale for site {}", siteId, e);
            }
        }

        if (StringUtils.isNotBlank(userId)) {
            try {
                Locale locale = preferencesService.getLocale(userId);
                if (locale != null) {
                    return locale;
                }
            } catch (Exception e) {
                log.debug("Unable to resolve user locale for user {}", userId, e);
            }
        }

        return defaultLocale();
    }

    @Override
    public String formatDouble(Double value, Locale locale) {
        NumberFormat format = NumberFormat.getInstance(locale);
        format.setGroupingUsed(false);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(15);
        return format.format(value);
    }

    @Override
    public String formatDouble(Double value, String siteId, String userId) {
        return formatDouble(value, getLocaleForSiteAndUser(siteId, userId));
    }

    @Override
    public Double parseDouble(String origin, Locale locale) {
        if (origin == null) return null;
        final String trimmed = origin.trim();
        if (trimmed.isEmpty()) return null;
        try {
            return Double.valueOf(trimmed);
        } catch (NumberFormatException nfe) {
            final NumberFormat parseFormat = NumberFormat.getInstance(locale);
            parseFormat.setGroupingUsed(true);
            final ParsePosition pos = new ParsePosition(0);
            final Number number = parseFormat.parse(trimmed, pos);
            return (number != null && pos.getIndex() == trimmed.length()) ? number.doubleValue() : null;
        }
    }

    @Override
    public Double parseDouble(String origin) {
        return parseDouble(origin, getLocaleForCurrentSiteAndUser());
    }

    @Override
    public String normalizeDouble(String origin, Locale locale) {
        if (origin == null) return null;
        final String trimmed = origin.trim();
        if (trimmed.isEmpty()) return origin;
        final Double value = parseDouble(trimmed, locale);
        return value != null ? formatDouble(value, locale) : origin;
    }

    @Override
    public String normalizeDouble(String origin) {
        return normalizeDouble(origin, getLocaleForCurrentSiteAndUser());
    }

    @Override
    public boolean isValidDouble(String origin, Locale locale) {
        if (origin == null || origin.trim().isEmpty()) {
            return false;
        }
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);
        final DecimalFormatSymbols fs = df.getDecimalFormatSymbols();
        final char groupingSeparator = fs.getGroupingSeparator();
        final char decimalSeparator = fs.getDecimalSeparator();
        final String pattern = "\\d{1,3}(\\"
                + groupingSeparator
                + "\\d{3})+\\"
                + decimalSeparator
                + "\\d+|\\d*\\"
                + decimalSeparator
                + "\\d+|\\d{1,3}(\\"
                + groupingSeparator
                + "\\d{3})+|\\d+";
        return origin.trim().matches(pattern);
    }

    @Override
    public boolean isValidDouble(String origin) {
        return isValidDouble(origin, getLocaleForCurrentSiteAndUser());
    }

    @Override
    public String getDecimalSeparator() {
        return String.valueOf(DecimalFormatSymbols.getInstance(getLocaleForCurrentSiteAndUser()).getDecimalSeparator());
    }

    private Locale defaultLocale() {
        return Locale.getDefault();
    }
}
