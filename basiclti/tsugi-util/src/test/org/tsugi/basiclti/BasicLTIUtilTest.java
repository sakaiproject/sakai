package org.tsugi.basiclti;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.tsugi.basiclti.BasicLTIUtil;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BasicLTIUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetRealPath() {
        String fixed = BasicLTIUtil.getRealPath("http://localhost/path/blah/", "https://right.com");
        assertEquals("https://right.com/path/blah/",fixed);
        fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com");
        assertEquals("https://right.com/path/blah/",fixed);
        fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "http://right.com");
        assertEquals("http://right.com/path/blah/",fixed);

        // Test folks sending in URL with extra stuff...
        fixed = BasicLTIUtil.getRealPath("https://localhost/path/blah/", "https://right.com/path/blah");
        assertEquals("https://right.com/path/blah/",fixed);
	}

	@Test
	public void testOAuthGET()
	{
		String url = BasicLTIUtil.getOAuthURL("GET", "http://www.dr-chuck.com/page1.htm?x=1", "12345", "secret");
		log.debug("url={}", url);
		assertTrue(url.contains("oauth_consumer_key"));
		assertTrue(url.contains("oauth_signature="));
		assertTrue(url.contains("www.dr-chuck.com"));
		
		/*
		// Comment this test out int trunk as it requires the network to be up...
		HttpURLConnection connection = BasicLTIUtil.sendOAuthURL("GET", "http://www.dr-chuck.com/dump.php?x=1", "12345", "secret");
		try { int responseCode = connection.getResponseCode();
		    log.debug("Responsecode={}", responseCode);
		} catch(Exception e) { }
		String data = BasicLTIUtil.readHttpResponse(connection);
		log.debug("data={}", data);
		// End of test with network required
		*/
	}

	@Test
	public void iso8601Text() throws Exception {
		String x = BasicLTIUtil.getISO8601();
		assertTrue(x.contains("Z"));
		String target = "2017-08-20:10:00:00";
		DateFormat df = new SimpleDateFormat("yyyy-mm-dd:hh:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date result =  df.parse(target);
		String y = BasicLTIUtil.getISO8601(result);
		assertEquals(y,"2017-01-20T10:00:00Z");
	}
	
}
