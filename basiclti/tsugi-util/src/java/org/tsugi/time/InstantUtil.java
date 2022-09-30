package org.tsugi.time;

import java.util.Date;
import java.time.Instant;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.httpclient.util.DateParseException;

@Slf4j
public class InstantUtil {

	public static Instant parseGMTFormats(String dateString)
	{
		Instant retval = null;
		Date d = null;
		SimpleDateFormat format = null;
	
		// https://docs.oracle.com/javase/10/docs/api/java/time/Instant.html#parse(java.lang.CharSequence)
		// "2007-12-03T10:15:30.00Z"
		try {
			retval = Instant.parse(dateString);
			if ( retval != null ) return retval;
		} catch (DateTimeParseException e) {
			// Ignore
		}

		// https://stackoverflow.com/questions/1930158/how-to-parse-date-from-http-last-modified-header
		// "Wed, 09 Apr 2008 23:55:38 GMT"
		try {
			format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			d = format.parse(dateString);
			if ( d != null && d.toInstant() != null ) return d.toInstant();
		} catch (Exception e) {
			log.debug("Date parse error: {}", dateString);
		}

		// https://hc.apache.org/httpclient-legacy/apidocs/org/apache/commons/httpclient/util/DateUtil.html

		try {
			// PATTERN_ASCTIME
			// Fri Feb 15 14:45:01 2013
			
			// PATTERN_RFC1036
			// https://datatracker.ietf.org/doc/html/rfc1036
			// Fri, 19 Nov 82 16:14:55 EST

			// PATTERN_RFC1123
			// https://datatracker.ietf.org/doc/html/rfc1123
			// https://datatracker.ietf.org/doc/html/rfc822#section-5
			// Wed, 02 Oct 2002 08:00:00 EST
			// Wed, 02 Oct 2002 13:00:00 GMT
			// Wed, 02 Oct 2002 15:00:00 +0200

			d = DateUtil.parseDate(dateString);
			if ( d != null && d.toInstant() != null ) return d.toInstant();
		} catch(DateParseException e) {
			log.debug("Date parse error: {}", dateString);
		}

		return null;
	}

}
