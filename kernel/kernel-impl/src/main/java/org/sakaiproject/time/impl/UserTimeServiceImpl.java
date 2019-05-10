package org.sakaiproject.time.impl;

import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;

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

        if (timeZone == null || timeZone.equals(""))
            timeZone = TimeZone.getDefault().getID();

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
    public String  dateFormatLong(Date date, Locale locale) {
        log.debug("dateFormat: " + date.toString() + ", " + locale.toString());

        DateFormat dsf = DateFormat.getDateInstance(DateFormat.LONG, locale);
        dsf.setTimeZone(getLocalTimeZone());
        String d = dsf.format(date); 
        return d;
    }


    @Override
	public String  dateTimeFormatLong(Date date, Locale locale) {
        log.debug("dateFormat: " + date.toString() + ", " + locale.toString());

        DateFormat dsf = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
        dsf.setTimeZone(getLocalTimeZone());
        String d = dsf.format(date);
        return d;
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
}
