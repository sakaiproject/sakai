package org.tsugi.http;

import java.util.Map;
import java.util.TreeMap;

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
}
