/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
package org.sakaiproject.time.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * This just deals with the user specific part of what timezone they are in.
 */
@Slf4j
public class UserTimeServiceImpl implements UserTimeService {
    // Cache of userIds to Timezone
    private Cache<String, String> M_userTzCache;

    // Map of Timezone/Locales to LocalTzFormat objects
    private ConcurrentHashMap<String, TimeZone> tzCache = new ConcurrentHashMap<>();

    // Default Timezone/Locale
    private String defaultTimezone = TimeZone.getDefault().getID();

    private MemoryService memoryService;
    private SessionManager sessionManager;
    private PreferencesService preferencesService;

    @Setter private ResourceLoader resourceLoader;

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setPreferencesService(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    public void init() {
        //register the Cache
        M_userTzCache = memoryService.getCache("org.sakaiproject.time.impl.BasicTimeService.userTimezoneCache");
    }

    private String getUserTimezone() {
        String userId = sessionManager.getCurrentSessionUserId();
        return getUserTimezone(userId);
    }

    private String getUserTimezone(String userId) {
        if (userId == null) return defaultTimezone;

        String timeZoneLocale = M_userTzCache.get(userId);
        if (timeZoneLocale != null) return timeZoneLocale;

        // Otherwise, get the user's preferred time zone
        Preferences prefs = preferencesService.getPreferences(userId);
        ResourceProperties tzProps = prefs.getProperties(TimeService.APPLICATION_ID);
        String timeZone = tzProps.getProperty(TimeService.TIMEZONE_KEY);

        if (StringUtils.isBlank(timeZone)) {
            timeZone = TimeZone.getDefault().getID();
        }
        else {
            try {
                ZoneId.of(timeZone);
            }
            catch (Exception e) {
                log.warn("getUserTimezone bad tz: {}, {}", userId, timeZone);
                timeZone = TimeZone.getDefault().getID();
            }
        }

        timeZoneLocale = timeZone;

        M_userTzCache.put(userId, timeZoneLocale);

        return timeZoneLocale;
    }

    @Override
    public TimeZone getLocalTimeZone() {
        String tz = getUserTimezone();
        // Not holding a cache can be slow.
        return tzCache.computeIfAbsent(tz, TimeZone::getTimeZone);
    }

    @Override
    public TimeZone getLocalTimeZone(String userId) {
        String tz = getUserTimezone(userId);
        return tzCache.computeIfAbsent(tz, TimeZone::getTimeZone);
    }

    @Override
    public boolean clearLocalTimeZone(String userId) {
        M_userTzCache.remove(userId);
        return true;
    }

    @Override
    public String timeFormat(Date time, Locale locale, int df) {
        if (time == null || locale == null) return "";
        log.debug("timeFormat: {}, {}, {}", time.toString(), locale.toString(), df);

        DateFormat dsf = DateFormat.getTimeInstance(df, locale);
        dsf.setTimeZone(getLocalTimeZone());
        return dsf.format(time); 
    }

    @Override
    public String dateFormat(Date date, Locale locale, int df) {
        if (date == null || locale == null) return "";
        log.debug("dateFormat: {}, {}, {}", date.toString(), locale.toString(), df);

        DateFormat dsf = DateFormat.getDateInstance(df, locale);
        dsf.setTimeZone(getLocalTimeZone());
        return dsf.format(date); 
    }

    @Override
    public String dateTimeFormat(Date date, Locale locale, int df) {
        if (date == null || locale == null) return "";
        log.debug("dateTimeFormat: {}, {}, {}", date.toString(), locale.toString(), df);

        DateFormat dsf = DateFormat.getDateTimeInstance(df, df, locale);
        dsf.setTimeZone(getLocalTimeZone());
        return dsf.format(date);
    }

    @Override
    public String dateTimeFormat(Instant date, FormatStyle dateStyle, FormatStyle timeStyle) {

        if (date == null) return "";
        if (dateStyle == null) { dateStyle = FormatStyle.MEDIUM; }
        if (timeStyle == null) { timeStyle = FormatStyle.SHORT; }
        DateTimeFormatter df = DateTimeFormatter.ofLocalizedDateTime(dateStyle, timeStyle)
                                                .withZone(getLocalTimeZone().toZoneId())
                                                .withLocale(resourceLoader.getLocale());
        return df.format(date);
    }

    @Override
    public String dayOfWeekFormat(Date date, Locale locale, int df) {
        String format = df > 1 ? "EEEEE" : "E";

        log.debug("dateTimeFormat: {}, {}, {}", date.toString(), locale.toString(), format);

        DateFormat dsf = new SimpleDateFormat(format, locale);
        dsf.setTimeZone(getLocalTimeZone());
        return dsf.format(date);
    }

    @Override
    public String shortLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale) {
        ZonedDateTime userDate = ZonedDateTime.ofInstant(instant, timezone.toZoneId());
        return userDate.format(buildTimestampFormatter(FormatStyle.MEDIUM, FormatStyle.SHORT, TextStyle.SHORT, locale));
    }

    @Override
    public String shortLocalizedTimestamp(Instant instant, Locale locale) {
        return shortLocalizedTimestamp(instant, getLocalTimeZone(), locale);
    }

    @Override
    public String shortPreciseLocalizedTimestamp(Instant instant, TimeZone timezone, Locale locale) {
        ZonedDateTime userDate = ZonedDateTime.ofInstant(instant, timezone.toZoneId());
        return userDate.format(buildTimestampFormatter(FormatStyle.MEDIUM, FormatStyle.MEDIUM, TextStyle.SHORT, locale));
    }

    @Override
    public String shortPreciseLocalizedTimestamp(Instant instant, Locale locale) {
        return shortPreciseLocalizedTimestamp(instant, getLocalTimeZone(), locale);
    }

    @Override
    public String shortLocalizedDate(LocalDate date, Locale locale) {
        DateTimeFormatter df = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
        return date.format(df);
    }

    private DateTimeFormatter buildTimestampFormatter(FormatStyle dateStyle, FormatStyle timeStyle, TextStyle zoneStyle, Locale locale) {
        return new DateTimeFormatterBuilder().appendLocalized(dateStyle, timeStyle)
                .appendLiteral(" ").appendZoneText(zoneStyle).toFormatter(locale);
    }

    @Override
    public Date parseISODateInUserTimezone(final String dateString) {
        // Hidden field from the datepicker will look like: 2015-02-19T02:25:00-06:00
        // JavaScript Date will always be the computer timezone and not the user's Sakai-preferred timezone
        // So we should ignore the browser-provided timezone and assume the user is working in their Sakai-preferred timezone
        final String localDateString = StringUtils.left(dateString, 19);
        LocalDateTime ldt = LocalDateTime.parse(localDateString);
        log.debug("parseISODateInUserTimezone: string={}, localDate={}", dateString, ldt.toString());

        TimeZone clientTimezone = getLocalTimeZone();
        TimeZone serverTimezone = TimeZone.getDefault();

        if (ldt != null && clientTimezone != null && serverTimezone != null && !clientTimezone.hasSameRules(serverTimezone)) {
            ZonedDateTime zdt = ldt.atZone(clientTimezone.toZoneId());
            log.debug("parseISODateInUserTimezone: original={}, zoned={}", dateString, zdt.toString());
            return Date.from(zdt.toInstant());
        }
        else if (ldt != null && serverTimezone != null) {
            ZonedDateTime zdt = ldt.atZone(serverTimezone.toZoneId());
            return Date.from(zdt.toInstant());
        }
        else if (ldt != null) {
            return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        }

        return null;
    }

}
