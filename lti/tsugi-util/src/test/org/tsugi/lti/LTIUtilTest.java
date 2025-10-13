package org.tsugi.lti;
import static org.junit.Assert.*;
import static org.tsugi.lti.LTIConstants.LTI_MESSAGE_TYPE;
import static org.tsugi.lti.LTIConstants.LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST;
import static org.tsugi.lti.LTIConstants.LTI_VERSION;
import static org.tsugi.lti.LTIConstants.LTI_VERSION_1;

import net.oauth.OAuth;
import net.oauth.OAuthMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.text.StringEscapeUtils;

@Slf4j
public class LTIUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetRealPath() {
		String fixed = LTIUtil.getRealPath("http://localhost/path/blah/", "https://right.com");
		assertEquals("https://right.com/path/blah/",fixed);
		fixed = LTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com");
		assertEquals("https://right.com/path/blah/",fixed);
		fixed = LTIUtil.getRealPath("https://localhost/path/blah/", "http://right.com");
		assertEquals("http://right.com/path/blah/",fixed);

		// Test folks sending in URL with extra stuff...
		fixed = LTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com/path/blah");
		assertEquals("https://right.com/path/blah/",fixed);
	}

	@Test
	public void testGetRealPathRequest() {
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost/path/blah/"));
		String fixed = LTIUtil.getRealPath(req, "https://right.com");
		assertEquals("https://right.com/path/blah/", fixed);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("https://localhost/path/blah/"));
		fixed = LTIUtil.getRealPath(req, "https://right.com");
		assertEquals("https://right.com/path/blah/", fixed);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("https://localhost/path/blah/"));
		fixed = LTIUtil.getRealPath(req, "http://right.com");
		assertEquals("http://right.com/path/blah/", fixed);

		Mockito.when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost/path/blah/"));
		fixed = LTIUtil.getRealPath(req, "https://right.com/path/blah");
		assertEquals("https://right.com/path/blah/", fixed);
	}

	@Test
	public void testOAuthGET()
	{
		String url = LTIUtil.getOAuthURL("GET", "http://www.dr-chuck.com/page1.htm?x=1", "12345", "secret");
		log.debug("url={}", url);
		assertTrue(url.contains("oauth_consumer_key"));
		assertTrue(url.contains("oauth_signature="));
		assertTrue(url.contains("www.dr-chuck.com"));

		url = LTIUtil.getOAuthURL("GET", null, null, null);
		log.debug("url={}", url);
		assertNull(url);

		/*
		// Comment this test out int trunk as it requires the network to be up...
		HttpURLConnection connection = LTIUtil.sendOAuthURL("GET", "http://www.dr-chuck.com/dump.php?x=1", "12345", "secret");
		try { int responseCode = connection.getResponseCode();
			log.debug("Responsecode={}", responseCode);
		} catch(Exception e) { }
		String data = LTIUtil.readHttpResponse(connection);
		log.debug("data={}", data);
		// End of test with network required
		*/
	}

	@Test
	public void iso8601Text() throws Exception {
		String x = LTIUtil.getISO8601();
		assertTrue(x.contains("Z"));
		assertTrue(x.contains("T"));
		String target = "2017-08-20:10:00:00";
		String iso8601 = "2017-01-20T10:00:00Z";

		DateFormat df = new SimpleDateFormat("yyyy-mm-dd:hh:mm:ss");
				df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date result = df.parse(target);
		String y = LTIUtil.getISO8601(result);
		assertEquals(y,iso8601);

		// Now lets parse some iso dates
		result = LTIUtil.parseIMS8601(iso8601);
		String z = LTIUtil.getISO8601(result);
		assertEquals(z, iso8601);

		result = LTIUtil.parseIMS8601(null);
		assertNull(result);

		String[] variations = {
			"2017-01-20T10:00:00+0000",
			"2017-01-20T11:00:00+0100",
			"2017-01-20T09:00:00-0100",
			"2017-01-20T10:00:00+00:00",
			"2017-01-20T11:00:00+01:00",
			"2017-01-20T09:00:00-01:00",
			"2017-01-20T10:00:00+00",
			"2017-01-20T11:00:00+01",
			"2017-01-20T09:00:00-01",
			"2017-01-20T05:00:00 EST",
		};

		for(int i=0; i<variations.length; i++) {
			result = LTIUtil.parseIMS8601(variations[i]);
			z = LTIUtil.getISO8601(result);
			assertEquals(z, iso8601);
		}
	}

	@Test
	public void isRequest() {
		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);

		//Nothing set = bad request
		boolean isReq = LTIUtil.isRequest(req);
		assertFalse(isReq);

		//Set both things = good request
		Mockito.when(req.getParameter(LTI_MESSAGE_TYPE)).thenReturn(LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST);
		Mockito.when(req.getParameter(LTI_VERSION)).thenReturn(LTI_VERSION_1);
		isReq = LTIUtil.isRequest(req);
		assertTrue(isReq);

		//Bad message type = bad request
		Mockito.when(req.getParameter(LTI_MESSAGE_TYPE)).thenReturn("foobar");
		isReq = LTIUtil.isRequest(req);
		assertFalse(isReq);

		//Bad version = bad request
		Mockito.when(req.getParameter(LTI_MESSAGE_TYPE)).thenReturn(LTI_MESSAGE_TYPE_BASICLTILAUNCHREQUEST);
		Mockito.when(req.getParameter(LTI_VERSION)).thenReturn("taco");
		isReq = LTIUtil.isRequest(req);
		assertFalse(isReq);
	}

	@Test
	public void validateMessageIncorrectConsumerKey() {
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put(OAuth.OAUTH_CONSUMER_KEY, new String[] {"zuul"});

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameterMap()).thenReturn(paramMap);

		String url = "https://www.sakailms.org/";
		String secret = "shhh";
		String key = "zuul2";

		Object obj = LTIUtil.validateMessage(req, url, secret, key);
		assertEquals("Incorrect consumer key zuul", obj);
	}

	@Test
	public void validateMessageNoBaseString() {
		Map<String, String[]> paramMap = new HashMap<>();
		paramMap.put(OAuth.OAUTH_CONSUMER_KEY, new String[] {"zuul"});


		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameterMap()).thenReturn(paramMap);

		String url = "https://www.sakailms.org/";
		String secret = "shhh";
		String key = "zuul";

		Object obj = LTIUtil.validateMessage(req, url, secret, key);
		assertEquals("Unable to find base string", obj);
	}

	@Test
	public void validateGoodMessage() {
		Map<String, String> paramMap = new HashMap<>();

		String url = "https://www.sakailms.org/";
		String secret = "shhh";
		String key = "zuul";

		Map<String, String> signedParams_pre = LTIUtil.signProperties(paramMap, url, OAuthMessage.POST, key, secret,
				"guid", "desc", "tool_url",
				"name", "email", null);

		//Need to convert from a <String, String> to a <String, String[]> so I can mock the HttpServletRequest
		Map<String, String[]> signedParams = signedParams_pre.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						e -> new String[]{e.getValue()}));

		HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
		Mockito.when(req.getParameterMap()).thenReturn(signedParams);
		Mockito.when(req.getMethod()).thenReturn(OAuthMessage.POST);

		Object obj = LTIUtil.validateMessage(req, url, secret, key);
		assertEquals(Boolean.TRUE, obj);
	}

	@Test(expected = IllegalArgumentException.class)
	public void adaptToCustomPropertyNameNull() {
		LTIUtil.adaptToCustomPropertyName(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void adaptToCustomPropertyNameEmpty() {
		LTIUtil.adaptToCustomPropertyName("");
	}

	@Test
	public void adaptToCustomPropertyName() {
		String newPropName = LTIUtil.adaptToCustomPropertyName("foobar");
		assertEquals("custom_foobar", newPropName);
	}

	@Test
	public void mapKeyName() {
		String keyName = LTIUtil.mapKeyName("Vendor:Chapter");
		assertEquals("vendor_chapter", keyName);

		keyName = LTIUtil.mapKeyName("ASDF:1234&asdf.1qaz");
		assertEquals("asdf_1234_asdf_1qaz", keyName);

		keyName = LTIUtil.mapKeyName("foobar");
		assertEquals("foobar", keyName);

		keyName = LTIUtil.mapKeyName("El Ni·ño");
		assertEquals("el_ni_ño", keyName);

		keyName = LTIUtil.mapKeyName(null);
		assertNull(keyName);

		keyName = LTIUtil.mapKeyName("");
		assertNull(keyName);
	}

	@Test
	public void setProperty() {
		Map<String, String> theMap = new HashMap<>();

		LTIUtil.setProperty(theMap, "foo", "bar");
		assertEquals(1, theMap.size());
		assertTrue(theMap.containsKey("foo"));
		assertEquals("bar", theMap.get("foo"));

		LTIUtil.setProperty(theMap, "foo", "qwerty");
		assertEquals(1, theMap.size());
		assertTrue(theMap.containsKey("foo"));
		assertEquals("qwerty", theMap.get("foo"));

		LTIUtil.setProperty(theMap, "foo", "");
		assertEquals(1, theMap.size());
		assertTrue(theMap.containsKey("foo"));
		assertEquals("qwerty", theMap.get("foo"));

		LTIUtil.setProperty(theMap, "lms", "sakai");
		assertEquals(2, theMap.size());
		assertTrue(theMap.containsKey("lms"));
		assertEquals("sakai", theMap.get("lms"));

		LTIUtil.setProperty(theMap, "", "?");
		assertEquals(3, theMap.size());
		assertTrue(theMap.containsKey(""));
		assertEquals("?", theMap.get(""));

		LTIUtil.setProperty(theMap, "asdf", null);
		assertEquals(3, theMap.size());
		assertFalse(theMap.containsKey("asdf"));
		assertNull(theMap.get("asdf"));

		LTIUtil.setProperty(theMap, null, "no boom");
		assertEquals(4, theMap.size());
		assertTrue(theMap.containsKey(null));
		assertEquals("no boom", theMap.get(null));
	}

	@Test
	public void htmlspecialchars() {
		String input = "<hi & \"bye' = ><hi & \"bye' = >";
		String result = LTIUtil.htmlspecialchars(input);
		assertEquals("&lt;hi &amp; &quot;bye' = &gt;&lt;hi &amp; &quot;bye' = &gt;", result);
		String roundtrip = StringEscapeUtils.unescapeHtml4(result);
		assertEquals(input, roundtrip);

		input = "one line\r\ntwo line\r\n";
		result = LTIUtil.htmlspecialchars(input);
		assertEquals("one line&#13;&#10;two line&#13;&#10;", result);
		roundtrip = StringEscapeUtils.unescapeHtml4(result);
		assertEquals(input, roundtrip);

		result = LTIUtil.htmlspecialchars("nothing to see here");
		assertEquals("nothing to see here", result);

		result = LTIUtil.htmlspecialchars(null);
		assertNull(result);
	}

	@Test
	public void convertToMap() {
		Properties props = new Properties();
		props.setProperty("foo", "bar");
		props.setProperty("lms", "Sakai");
		props.setProperty("", "something");

		Map<String, String> results = LTIUtil.convertToMap(props);
		assertNotNull(results);
		assertEquals(3, results.size());
		assertEquals("bar", results.get("foo"));
		assertEquals("Sakai", results.get("lms"));
		assertEquals("something", results.get(""));
		assertNull(results.get("taco"));
	}

	@Test
	public void isBlank() {
		assertTrue(LTIUtil.isBlank(null));
		assertTrue(LTIUtil.isBlank(""));
		assertTrue(LTIUtil.isBlank(" "));
		assertFalse(LTIUtil.isBlank("bob"));
		assertFalse(LTIUtil.isBlank(" bob "));
	}

	@Test
	public void isNotBlank() {
		assertFalse(LTIUtil.isNotBlank(null));
		assertFalse(LTIUtil.isNotBlank(""));
		assertFalse(LTIUtil.isNotBlank(" "));
		assertTrue(LTIUtil.isNotBlank("bob"));
		assertTrue(LTIUtil.isNotBlank(" bob "));
	}

	@Test
	public void equals() {
		assertTrue(LTIUtil.equals(null, null));
		assertFalse(LTIUtil.equals(null, "abc"));
		assertFalse(LTIUtil.equals("abc", null));
		assertTrue(LTIUtil.equals("abc", "abc"));
		assertFalse(LTIUtil.equals("abc", "ABC"));
	}

	@Test
	public void equalsIgnoreCase() {
		assertTrue(LTIUtil.equalsIgnoreCase(null, null));
		assertFalse(LTIUtil.equalsIgnoreCase(null, "abc"));
		assertFalse(LTIUtil.equalsIgnoreCase("abc", null));
		assertTrue(LTIUtil.equalsIgnoreCase("abc", "abc"));
		assertTrue(LTIUtil.equalsIgnoreCase("abc", "ABC"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void postLaunchHtmlEmptyMap() {
		Map<String, String> props = new HashMap<>();
		String endpoint = "";
		String launchtext = "";
		Map<String, String> extra = new HashMap<>();
		LTIUtil.postLaunchHTML(props, endpoint, launchtext, true, extra);
	}

	@Test(expected = IllegalArgumentException.class)
	public void postLaunchHtmlEmptyEndpoint() {
		Map<String, String> props = new HashMap<>();
		props.put("key", "value");
		String endpoint = null;
		String launchtext = "";
		Map<String, String> extra = new HashMap<>();
		LTIUtil.postLaunchHTML(props, endpoint, launchtext, true, extra);
	}

	@Test
	public void postLaunchHtml() {
		Map<String, String> props = new HashMap<>();
		props.put("key", "value");
		props.put("null", null);
		String endpoint = "";
		String launchtext = "";
		Map<String, String> extra = new HashMap<>();
		extra.put("foo", "bar");
		extra.put("BaseString", "base string");
		String html = LTIUtil.postLaunchHTML(props, endpoint, launchtext, false, extra);
		assertNotNull(html);

		html = LTIUtil.postLaunchHTML(props, endpoint, launchtext, true, extra);
		assertNotNull(html);
	}

	@Test
	public void checkProperties() {
		Map<String, String> props = new HashMap<>();
		boolean checkedProperties = LTIUtil.checkProperties(props, "https://www.sakailms.org/", "POST",
				"key", "secret");
		assertFalse(checkedProperties);

		String url = "https://www.sakailms.org/";
		String key = "key";
		String secret = "secret";
		Map<String, String> signedParams = LTIUtil.signProperties(props, url, OAuthMessage.POST, key, secret,
				"guid", "desc", "tool_url",
				"name", "email", null);
		checkedProperties = LTIUtil.checkProperties(signedParams, url, OAuthMessage.POST,
				key, secret);
		assertTrue(checkedProperties);
	}

	@Test
	public void signProperties() {
		Map<String, String> props = new HashMap<>();
		Map<String, String> extra = new HashMap<>();

		String url = "https://www.sakailms.org/";
		String key = "key";
		String secret = "secret";
		Map<String, String> signedParams = LTIUtil.signProperties(props, url, OAuthMessage.POST, key, secret,
				"guid", "desc", "tool_url",
				"name", "email", extra);
		assertNotNull(signedParams);

		signedParams = LTIUtil.signProperties(props, url, OAuthMessage.GET, null, secret,
				"guid", "desc", "tool_url",
				"name", "email", extra);
		assertNotNull(signedParams);
	}

	@Test
	public void testMergeCSV() {
		String retval = LTIUtil.mergeCSV("1,2,3", null, "4");
		assertEquals(retval, "1,2,3,4");
		retval = LTIUtil.mergeCSV("1,2", "3", "4");
		assertEquals(retval, "1,2,3,4");
		retval = LTIUtil.mergeCSV("1,2", "1,2,3,4", "4");
		assertEquals(retval, "1,2,3,4");
		retval = LTIUtil.mergeCSV("1,2", "1,2,3,4", null);
		assertEquals(retval, "1,2,3,4");
		retval = LTIUtil.mergeCSV("1,2", "2,1,3,4", null);
		assertEquals(retval, "1,2,3,4");
	}

	@Test
	public void testShiftJVMDateToTimeZone() {
		TimeZone tz = TimeZone.getTimeZone("America/New_York");
		assertEquals(tz.getRawOffset(), -18000000);
		TimeZone tzla = TimeZone.getTimeZone("America/Los_Angeles");
		assertEquals(tzla.getRawOffset(), -28800000);
		TimeZone tzutc = TimeZone.getTimeZone("UTC");
		assertEquals(tzutc.getRawOffset(), 0);

		// If The JVM is Eastern and the due date is 3PM, when we send a UTC
		// time to a server that is in Mountain and we want it to display as
		// 3PM on the Mountain server, it will be sent as a two hour later UTC
		// Since we don't know the JVM timezone we shift a JVM date into Eastern
		// and Mountain and compare those values
		// The result is that both systems show the due date as 3PM server local time.
		// When you are a multi-tenant LTI tool with tenants in many time zones, and the LTI
		// tool sets a due date and then sends it to the upstream LMS - when the students
		// think it is due at 3PM - it needs to be shown as 2PM on both sysytems.
		// It does mean that late points are best computed in the controlling LMS, not the
		// LTI/SakaiPlus system
		Date dueDate = new Date("Wed Jan 18 15:00:00 2023");
		Date eastDate = LTIUtil.shiftJVMDateToTimeZone(dueDate, "US/Eastern");
		Date mountainDate = LTIUtil.shiftJVMDateToTimeZone(dueDate, "America/Denver");
		assertEquals(eastDate.getTime(), mountainDate.getTime()+2*60*60*1000);
	}


	@Test
	public void testEscapeNewlinesAndCarriageReturns() {
		String input = "This is a line with newlines.\r\nHere is another line.\rAnd another one.\n";
		String expected = "This is a line with newlines.\\r\\nHere is another line.\\rAnd another one.\\n";
		String actual = LTIUtil.escapeSpecialCharacters(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testEscapeTabsAndBackslash() {
		String input = "This \tis \ta \tstring \twith \t\ttabs \\and backslashes \\";
		String expected = "This \\tis \\ta \\tstring \\twith \\t\\ttabs \\\\and backslashes \\\\";
		String actual = LTIUtil.escapeSpecialCharacters(input);
		assertEquals(expected, actual);
	}

	@Test
	public void testToInteger() {
		// Test basic integer values
		assertEquals("Basic integer value conversion failed", Integer.valueOf(2), LTIUtil.toInteger(Integer.valueOf(2), null));
		assertEquals("Negative integer value conversion failed", Integer.valueOf(-2), LTIUtil.toInteger(Integer.valueOf(-2), null));
		assertEquals("Zero value conversion failed", Integer.valueOf(0), LTIUtil.toInteger(Integer.valueOf(0), null));
		
		// Test null and invalid inputs
		Integer defaultVal = Integer.valueOf(42);
		assertEquals("Null input should return default", defaultVal, LTIUtil.toInteger(null, defaultVal));
		assertEquals("Invalid string should return default", defaultVal, LTIUtil.toInteger("fred", defaultVal));
		assertEquals("String 'null' should return default", defaultVal, LTIUtil.toInteger("null", defaultVal));
		assertEquals("String 'NULL' should return default", defaultVal, LTIUtil.toInteger("NULL", defaultVal));
		assertEquals("Empty string should return default", defaultVal, LTIUtil.toInteger("", defaultVal));
		
		// Test string number formats
		assertEquals("String integer conversion failed", Integer.valueOf(2), LTIUtil.toInteger("2", null));
		assertEquals("String negative integer conversion failed", Integer.valueOf(-2), LTIUtil.toInteger("-2", null));
		assertEquals("String zero conversion failed", Integer.valueOf(0), LTIUtil.toInteger("0", null));
		assertEquals("Decimal string should return default", defaultVal, LTIUtil.toInteger("2.0", defaultVal));
		assertEquals("Decimal string should return default", defaultVal, LTIUtil.toInteger("2.5", defaultVal));
		
		// Test other number types
		assertEquals("Long conversion failed", Integer.valueOf(3), LTIUtil.toInteger(Long.valueOf(3), null));
		assertEquals("Double conversion with no decimal failed", Integer.valueOf(3), LTIUtil.toInteger(Double.valueOf(3.0), null));
		assertEquals("Double conversion with decimal truncates", Integer.valueOf(3), LTIUtil.toInteger(Double.valueOf(3.9), null));
		assertEquals("Float conversion failed", Integer.valueOf(3), LTIUtil.toInteger(Float.valueOf(3.0f), null));
		assertEquals("Short conversion failed", Integer.valueOf(3), LTIUtil.toInteger(Short.valueOf((short)3), null));
		assertEquals("Byte conversion failed", Integer.valueOf(3), LTIUtil.toInteger(Byte.valueOf((byte)3), null));
	}

	@Test
	public void testToInt() {
		assertEquals("Basic integer conversion failed", 2, LTIUtil.toInt(Integer.valueOf(2)));
		assertEquals("String conversion failed", 2, LTIUtil.toInt("2"));
		assertEquals("Default for null should be -1", -1, LTIUtil.toInt(null));
		assertEquals("Default for invalid string should be -1", -1, LTIUtil.toInt("invalid"));
	}

	@Test
	public void testToLong() {
		// Test basic long values
		assertEquals("Basic long value conversion failed", Long.valueOf(2L), LTIUtil.toLong(Long.valueOf(2L), null));
		assertEquals("Negative long value conversion failed", Long.valueOf(-2L), LTIUtil.toLong(Long.valueOf(-2L), null));
		assertEquals("Zero value conversion failed", Long.valueOf(0L), LTIUtil.toLong(Long.valueOf(0L), null));
		
		// Test null and invalid inputs
		Long defaultVal = Long.valueOf(42L);
		assertEquals("Null input should return default", defaultVal, LTIUtil.toLong(null, defaultVal));
		assertEquals("Invalid string should return default", defaultVal, LTIUtil.toLong("fred", defaultVal));
		assertEquals("String 'null' should return default", defaultVal, LTIUtil.toLong("null", defaultVal));
		assertEquals("String 'NULL' should return default", defaultVal, LTIUtil.toLong("NULL", defaultVal));
		assertEquals("Empty string should return default", defaultVal, LTIUtil.toLong("", defaultVal));
		
		// Test string number formats
		assertEquals("String long conversion failed", Long.valueOf(2L), LTIUtil.toLong("2", null));
		assertEquals("String negative long conversion failed", Long.valueOf(-2L), LTIUtil.toLong("-2", null));
		assertEquals("String zero conversion failed", Long.valueOf(0L), LTIUtil.toLong("0", null));
		assertEquals("Decimal string should return default", defaultVal, LTIUtil.toLong("2.5", defaultVal));
		assertEquals("Decimal string should return default", defaultVal, LTIUtil.toLong("2.0", defaultVal));
		
		// Test other number types
		assertEquals("Integer conversion failed", Long.valueOf(3L), LTIUtil.toLong(Integer.valueOf(3), null));
		assertEquals("Double conversion with no decimal failed", Long.valueOf(3L), LTIUtil.toLong(Double.valueOf(3.0), null));
		assertEquals("Double conversion with decimal truncates", Long.valueOf(3L), LTIUtil.toLong(Double.valueOf(3.9), null));
		assertEquals("Float conversion failed", Long.valueOf(3L), LTIUtil.toLong(Float.valueOf(3.0f), null));
		assertEquals("Short conversion failed", Long.valueOf(3L), LTIUtil.toLong(Short.valueOf((short)3), null));
		assertEquals("Byte conversion failed", Long.valueOf(3L), LTIUtil.toLong(Byte.valueOf((byte)3), null));
	}

	@Test
	public void testToLongKeyAndNull() {
		// Test toLongKey (returns -1L as default)
		assertEquals("Basic long key conversion failed", Long.valueOf(2L), LTIUtil.toLongKey(Long.valueOf(2L)));
		assertEquals("Null should return -1L", Long.valueOf(-1L), LTIUtil.toLongKey(null));
		assertEquals("Invalid string should return -1L", Long.valueOf(-1L), LTIUtil.toLongKey("invalid"));
		
		// Test toLongNull (returns null as default)
		assertEquals("Basic long conversion failed", Long.valueOf(2L), LTIUtil.toLongNull(Long.valueOf(2L)));
		assertNull("Null should return null", LTIUtil.toLongNull(null));
		assertNull("Invalid string should return null", LTIUtil.toLongNull("invalid"));
	}

	@Test
	public void testToDouble() {
		// Test basic double values
		assertEquals("Basic double value conversion failed", Double.valueOf(2.0), LTIUtil.toDouble(Double.valueOf(2.0), null));
		assertEquals("Decimal double value conversion failed", Double.valueOf(2.5), LTIUtil.toDouble(Double.valueOf(2.5), null));
		assertEquals("Negative double value conversion failed", Double.valueOf(-2.5), LTIUtil.toDouble(Double.valueOf(-2.5), null));
		
		// Test null and invalid inputs
		Double defaultVal = Double.valueOf(42.0);
		assertEquals("Null input should return default", defaultVal, LTIUtil.toDouble(null, defaultVal));
		assertEquals("Invalid string should return default", defaultVal, LTIUtil.toDouble("fred", defaultVal));
		assertEquals("String 'null' should return default", defaultVal, LTIUtil.toDouble("null", defaultVal));
		assertEquals("String 'NULL' should return default", defaultVal, LTIUtil.toDouble("NULL", defaultVal));
		assertEquals("Empty string should return default", defaultVal, LTIUtil.toDouble("", defaultVal));
		
		// Test string number formats
		assertEquals("String decimal conversion failed", Double.valueOf(2.0), LTIUtil.toDouble("2.0", null));
		assertEquals("String float conversion failed", Double.valueOf(2.5), LTIUtil.toDouble("2.5", null));
		assertEquals("String integer conversion failed", Double.valueOf(2.0), LTIUtil.toDouble("2", null));
		assertEquals("String negative number conversion failed", Double.valueOf(-2.5), LTIUtil.toDouble("-2.5", null));
		
		// Test other number types
		assertEquals("Long conversion failed", Double.valueOf(3.0), LTIUtil.toDouble(Long.valueOf(3), null));
		assertEquals("Integer conversion failed", Double.valueOf(3.0), LTIUtil.toDouble(Integer.valueOf(3), null));
		assertEquals("Float conversion failed", Double.valueOf(3.0), LTIUtil.toDouble(Float.valueOf(3.0f), null));
		assertEquals("Short conversion failed", Double.valueOf(3.0), LTIUtil.toDouble(Short.valueOf((short)3), null));
		assertEquals("Byte conversion failed", Double.valueOf(3.0), LTIUtil.toDouble(Byte.valueOf((byte)3), null));
	}

	@Test
	public void testToDoubleNull() {
		assertEquals("Basic double conversion failed", Double.valueOf(2.5), LTIUtil.toDoubleNull(Double.valueOf(2.5)));
		assertNull("Null should return null", LTIUtil.toDoubleNull(null));
		assertNull("Invalid string should return null", LTIUtil.toDoubleNull("invalid"));
		assertEquals("String conversion should work", Double.valueOf(2.5), LTIUtil.toDoubleNull("2.5"));
	}
}
