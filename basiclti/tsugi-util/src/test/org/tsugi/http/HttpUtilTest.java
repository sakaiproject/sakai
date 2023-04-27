package org.tsugi.http;

import java.util.Map;
import java.util.List;
import java.util.TreeMap;
import java.util.ArrayList;

import static org.junit.Assert.*;

import org.junit.Test;

import org.tsugi.http.HttpUtil;

public class HttpUtilTest {

	@Test
	public void testOne() {
		String url = "https://www.sakailms.org?search=plus";
		Map parms = new TreeMap<String, String>();
		parms.put("funky", ")(*&^%$#$%^&*U(");
		parms.put("town", "burgers");
		String newURL = HttpUtil.augmentGetURL(url, parms);
		assertEquals(newURL, "https://www.sakailms.org?search=plus&funky=%29%28*%26%5E%25%24%23%24%25%5E%26*U%28&town=burgers");
	}

	@Test
	// All values of Link: [<http://localhost:8080/imsblis/lti13/namesandroles/8b206920-d4d9-4df5-9aba-bd100a2a0af0?start=2&limit=2>; rel="next"]
	// https://www.imsglobal.org/spec/lti-nrps/v2p0#limit-query-parameter
	public void testLinkExtractRel() {
		List headerValues = new ArrayList<String>();
		headerValues.add("<https://lms.example.com/sections/2923/memberships?p=1>; rel=\"prev\"");
		headerValues.add("<https://lms.example.com/sections/2923/memberships?p=2>; rel=\"next\"");
		headerValues.add("<https://lms.example.com/sections/2923/memberships?since=1422554502>; rel=\"differences\"");
		headerValues.add("funky>; rel=\"funky\"");

		assertEquals(null, HttpUtil.extractLinkByRel(null, null));
		assertEquals(null, HttpUtil.extractLinkByRel(null, "new"));
		assertEquals(null, HttpUtil.extractLinkByRel(headerValues, null));
		assertEquals(null, HttpUtil.extractLinkByRel(headerValues, "missing"));

		assertEquals(null, HttpUtil.extractLinkByRel(headerValues, "funky"));

		String nextUrl = HttpUtil.extractLinkByRel(headerValues, "next");
		assertEquals("https://lms.example.com/sections/2923/memberships?p=2", nextUrl);
		String differencesUrl = HttpUtil.extractLinkByRel(headerValues, "differences");
		assertEquals("https://lms.example.com/sections/2923/memberships?since=1422554502", differencesUrl);

	}
}
