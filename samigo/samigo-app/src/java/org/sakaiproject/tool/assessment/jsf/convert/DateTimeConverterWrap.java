package org.sakaiproject.tool.assessment.jsf.convert;

import java.util.Locale;
import java.util.TimeZone;

import javax.faces.convert.DateTimeConverter;

import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class is a wrapper for {@link DateTimeConverter}.<br>
 * The locale and time zone by Sakai preferences are used as a default.
 */
public class DateTimeConverterWrap extends DateTimeConverter {

	@Override
	public void setLocale(final Locale locale) {
		if (locale == null) {
			super.setLocale(new ResourceLoader().getLocale());
		} else {
			super.setLocale(locale);
		}
	}

	@Override
	public void setTimeZone(final TimeZone timeZone) {
		if (timeZone == null) {
			// SAM-3106 This previously would use client timezone (TimeService.getLocalTimeZone)
			// which could produce inconsistent results when client in different timezone than server.
			super.setTimeZone(TimeZone.getDefault());
		} else {
			super.setTimeZone(timeZone);
		}
	}
}
