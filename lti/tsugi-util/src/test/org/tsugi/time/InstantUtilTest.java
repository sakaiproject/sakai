package org.tsugi.time;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;

import org.tsugi.time.InstantUtil;

public class InstantUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testInstantParse() throws Exception {
		Instant i = InstantUtil.parseGMTFormats("bob");
		assertNull(i);

		String txt = "2007-12-03T10:15:30.00Z";
		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "2007-12-03T10:15:30Z");

		txt = "Wed, 09 Apr 2008 23:55:38 GMT";
		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "2008-04-09T23:55:38Z");

		txt = "Fri Feb 15 14:45:01 2013";
		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "2013-02-15T14:45:01Z");

		// This one is funky - but not really worth supporting or agonizing over
		// Keep it here to see if anything changes beneath us
		txt = "Fri, 19 Nov 82 16:14:55 EST";
		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "0082-11-17T21:14:55Z");

		txt = "Wed, 02 Oct 2002 08:00:00 EST";
		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "2002-10-02T13:00:00Z");

        txt = "Wed, 02 Oct 2002 13:00:00 GMT";
		assertEquals(i.toString(), "2002-10-02T13:00:00Z");

		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "2002-10-02T13:00:00Z");

        txt = "Wed, 02 Oct 2002 15:00:00 +0200";
		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "2002-10-02T13:00:00Z");

		// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
		// Retry-After: Wed, 21 Oct 2015 07:28:00 GMT
        txt = "Wed, 21 Oct 2015 07:28:00 GMT";
		i = InstantUtil.parseGMTFormats(txt);
		assertEquals(i.toString(), "2015-10-21T07:28:00Z");
	}

}
