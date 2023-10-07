package org.tsugi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import org.tsugi.util.Base64DoubleUrlEncodeSafe;

/* This code was written by ChatGPT with the following prompt
 * Please write a unit test for a Base64DoubleUrlEncodeSafe class
 */

public class Base64DoubleUrlEncodeSafeTest {

	@Test
	public void testEncodeAndDecode() {
		String[] testStrings = {
			"Hello, Base64DoubleUrlEncodeSafe!",
			"Testing encoding and decoding.",
			"12345",
			"This is a longer string with more characters.",
			"A tree 🌲 was here",
			"A",
			""
		};

		for (String input : testStrings) {
			String encoded = Base64DoubleUrlEncodeSafe.encode(input);
			String decoded = Base64DoubleUrlEncodeSafe.decode(encoded);
			assertEquals("Decoding did not produce the original string for input: " + input, input, decoded);

			try {
				String urlEncoded = java.net.URLEncoder.encode(encoded, "UTF-8");
				assertEquals("UrlEncoding changed the string: " + encoded, encoded, urlEncoded);
			} catch (java.io.UnsupportedEncodingException e) {
				fail("Unxpected UnsupportedEncodingException for URLEncoder "+encoded);
			}
		}

		assertEquals("Encode of null should produce null: ", null, Base64DoubleUrlEncodeSafe.encode(null));
		assertEquals("Decode of null should produce null: ", null, Base64DoubleUrlEncodeSafe.decode(null));
	}

	@Test
	public void testInvalidCharacters() {
		// Test invalid characters in decoding
		String invalidBase64DoubleUrlEncodeSafeString = "InvalidString$#@!";
		try {
			Base64DoubleUrlEncodeSafe.decode(invalidBase64DoubleUrlEncodeSafeString);
			fail("Expected IllegalArgumentException for invalid Base64DoubleUrlEncodeSafe string");
		} catch (Exception e) {
			// Expected exception
		}
		assertEquals("Encode of null should produce null: ", null, Base64DoubleUrlEncodeSafe.encode(null));
	}

	@Test
	public void testVerifyJavaBase64UrlSafeEncodingFails() throws Exception {
		String[] testStrings = {
			"Hello, World!",
			"12345",
			"abcABC",
			"A tree 🌲 was here",
			"!@#$%^&*()_+{}[]|\\:;<>,.?/~`"
		};

		int failcount = 0;

		for (String input : testStrings) {
			String base64UrlEncoded = java.util.Base64.getUrlEncoder().encodeToString(input.getBytes("UTF-8"));

			// URL-encode the base64 URL encoded string
			String urlEncoded = java.net.URLEncoder.encode(base64UrlEncoded, "UTF-8");

			// Compare the original encoded string with the URL-encoded string
			if ( ! base64UrlEncoded.equals(urlEncoded)) failcount ++;
		}
		assertFalse(failcount == 0);
	}

}

