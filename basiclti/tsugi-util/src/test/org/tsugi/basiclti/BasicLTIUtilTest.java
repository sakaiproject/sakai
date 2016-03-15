package org.tsugi.basiclti;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.tsugi.basiclti.BasicLTIUtil;
import java.net.HttpURLConnection;

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
		// System.out.println("url="+url);
		assertTrue(url.contains("oauth_consumer_key"));
		assertTrue(url.contains("oauth_signature="));
		assertTrue(url.contains("www.dr-chuck.com"));
		
		/*
		// Comment this test out int trunk as it requires the network to be up...
		HttpURLConnection connection = BasicLTIUtil.sendOAuthURL("GET", "http://www.dr-chuck.com/dump.php?x=1", "12345", "secret");
		try { int responseCode = connection.getResponseCode();
		    System.out.println("Responsecode="+responseCode); 
		} catch(Exception e) { }
		String data = BasicLTIUtil.readHttpResponse(connection);
		System.out.println("data="+data);
		// End of test with network required
		*/
	}
	
}
