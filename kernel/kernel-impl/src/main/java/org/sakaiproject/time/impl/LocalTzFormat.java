package org.sakaiproject.time.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**********************************************************************************************************************************************************************************************************************************************************
 * LocalTzFormat -- maintains a local timezone, locale and DateFormats
 *********************************************************************************************************************************************************************************************************************************************************/

class LocalTzFormat
{
    // The time zone for our local times
    public TimeZone M_tz_local = null;

    // The Locale for our local date/time formatting
    public Locale M_locale = null;

    // a calendar to clone for GMT time construction
    public GregorianCalendar M_GCall = null;

    // The formatter for our special local timezone format(s)
    public DateFormat M_fmtAl = null;

    public DateFormat M_fmtBl = null;

    public DateFormat M_fmtBlz = null;

    public DateFormat M_fmtCl = null;

    public DateFormat M_fmtClz = null;

    public DateFormat M_fmtDl = null;

    public DateFormat M_fmtD2 = null;

    public DateFormat M_fmtFl = null;

    private LocalTzFormat()
    {
    }; // disable default constructor

    public LocalTzFormat(String timeZoneId, String localeId )
    {
        M_tz_local = TimeZone.getTimeZone(timeZoneId);

        Locale M_locale = null;
        String langLoc[] = localeId.split("_");
        if ( langLoc.length >= 2 )
            if (langLoc[0].equals("en") && langLoc[1].equals("ZA"))
                M_locale = new Locale("en", "GB");
            else
                M_locale = new Locale(langLoc[0], langLoc[1]);
        else
            M_locale = new Locale(langLoc[0]);

        M_fmtAl = (DateFormat)(new SimpleDateFormat("yyyyMMddHHmmssSSS"));
        M_fmtBl = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, M_locale);
        M_fmtBlz = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, M_locale);
        M_fmtCl = DateFormat.getTimeInstance(DateFormat.SHORT, M_locale);
        M_fmtClz = DateFormat.getTimeInstance(DateFormat.LONG, M_locale);
        M_fmtDl = DateFormat.getDateInstance(DateFormat.MEDIUM, M_locale);
        M_fmtD2 = DateFormat.getDateInstance(DateFormat.SHORT, M_locale);
        M_fmtFl = (DateFormat)(new SimpleDateFormat("HH:mm:ss"));

        // Strip the seconds from the Blz and Clz (default) formats
        try
        {
            SimpleDateFormat sdf = ((SimpleDateFormat)M_fmtBlz);
            String pattern = sdf.toLocalizedPattern();
            pattern = pattern.replaceAll(":ss","");
            sdf.applyLocalizedPattern( pattern );

            sdf = ((SimpleDateFormat)M_fmtClz);
            pattern = sdf.toLocalizedPattern();
            pattern = pattern.replaceAll(":ss","");
            sdf.applyLocalizedPattern( pattern );
        }
        catch (ClassCastException e)
        {
            // ignore -- not all locales support this
        }

        M_fmtAl.setTimeZone(M_tz_local);
        M_fmtBl.setTimeZone(M_tz_local);
        M_fmtBlz.setTimeZone(M_tz_local);
        M_fmtCl.setTimeZone(M_tz_local);
        M_fmtClz.setTimeZone(M_tz_local);
        M_fmtDl.setTimeZone(M_tz_local);
        M_fmtD2.setTimeZone(M_tz_local);
        M_fmtFl.setTimeZone(M_tz_local);

        M_GCall = BasicTimeService.newCalendar(M_tz_local, 0, 0, 0, 0, 0, 0, 0);
    }

}
