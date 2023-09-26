package org.tsugi.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import org.tsugi.util.Base62;

/* This code was written by ChatGPT with the following prompt
 * Please write a unit test for a Base62 class
 */

public class Base62Test {

	@Test
	public void testEncodeAndDecode() {
		String[] testStrings = {
			"Hello, Base62!",
			"Testing encoding and decoding.",
			"12345",
			"This is a longer string with more characters.",
			"A tree 🌲 was here",
			"A",
			""
		};

		for (String input : testStrings) {
			String encoded = Base62.encode(input);
			String decoded = Base62.decode(encoded);
			assertEquals("Decoding did not produce the original string for input: " + input, input, decoded);

			try {
				String urlEncoded = java.net.URLEncoder.encode(encoded, "UTF-8");
				assertEquals("UrlEncoding changed the string: " + encoded, encoded, urlEncoded);
			} catch (java.io.UnsupportedEncodingException e) {
				fail("Unxpected UnsupportedEncodingException for URLEncoder "+encoded);
			}
		}

		assertEquals("Encode of null should produce null: ", null, Base62.encode(null));
		assertEquals("Decode of null should produce null: ", null, Base62.decode(null));
	}

	@Test
	public void testInvalidCharacters() {
		// Test invalid characters in decoding
		String invalidBase62String = "InvalidString$#@!";
		try {
			Base62.decode(invalidBase62String);
			fail("Expected IllegalArgumentException for invalid Base62 string");
		} catch (IllegalArgumentException e) {
			// Expected exception
		}
		assertEquals("Encode of null should produce null: ", null, Base62.encode(null));
	}

	@Test
	public void testInvalidCharactersSafe() {
		// Test invalid characters in decoding
		String invalidBase62String = "InvalidString$#@!";
		String decoded = Base62.decodeSafe(invalidBase62String);

		assertEquals("Safe decode of non-base22 string should return the string: ", invalidBase62String, decoded);

		String encoded = Base62.encode(invalidBase62String);
		decoded = Base62.decodeSafe(encoded);
		assertEquals("Safe decode of base22 string should return the decoded string: ", invalidBase62String, decoded);
	}

	/**
	 * As is always the case, developers resist the idea of using Base62 with things like
	 * Base64 URL safe encoding.  Which as the example below shows that whilst the resulting
	 * encoded strings *are* URL Encodable - they will suffer when double URL encoding happens.
	 */
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

	/**
	 * Also suggested in code review was the serialization used by io.jsonwebtoken.io.Base64
	 * which does not pad.  But the writers of said code made it private on purpose - probably
	 * because it is not precisely Base64 because the lack of padding.
	 *
	 * This test might work if it were not for the fact that io.jsonwebtoken.io.Base64 is 
	 * explicitly not public by its creators.  Adding this import above will fail to compile:
	 *
	 * import io.jsonwebtoken.io.Base64;
	 *
	 * [ERROR] Base62Test.java:[10,25] error: Base64 is not public in io.jsonwebtoken.io; cannot be accessed from outside package
	 *
	 * The test below might work if it were not for the fact that io.jsonwebtoken.io.Base64 is 
	 * explicitly not public by its creators.  Adding this import above will fail to compile:
	 */

	/*
	@Test
	public void testIoJsonWebTokenBase64UrlIsPrivate() throws Exception {
		String[] testStrings = {
			"Hello, World!",
			"12345",
			"abcABC",
			"A tree 🌲 was here",
			"!@#$%^&*()_+{}[]|\\:;<>,.?/~`"
		};

		for (String input : testStrings) {
			// Encode using jjwt-api base64 URL encoding
			boolean linesep = false;
			String base64UrlEncoded = io.jsonwebtoken.io.Base64.URL_SAFE.encodeToString(input.getBytes("UTF-8"), linesep);

			// URL-encode the base64 URL encoded string
			String urlEncoded = java.net.URLEncoder.encode(base64UrlEncoded, "UTF-8");

			// Compare the original encoded string with the URL-encoded string
			assertEquals(base64UrlEncoded, urlEncoded);
		}
	}
	*/
}

