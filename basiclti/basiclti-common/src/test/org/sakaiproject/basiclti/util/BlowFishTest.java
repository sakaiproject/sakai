/**
 * Copyright (c) 2009-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.basiclti.util;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.basiclti.util.BlowFish;
import org.sakaiproject.basiclti.util.PortableShaUtil;

@Slf4j
public class BlowFishTest {

	public static final String goodsha1 = "e5e9fa1ba31ecd1ae84f75caaa474f3a663f05f4";
	public static final String encode1 = "0308b4f79fdfdfb17de9fc29356be82d";

	public static final String encode2 = "f18c3f45a1a635ba0c03649b101b7fbd";
	public static final String hexkey2 = "7468697320697320746865206b6579";

	public static final String strings[] = {
		"Short", "Medium", 
		"12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï<8c>¶Ä©úÆûÂÉ¾Ö³²µ÷Ã<8d>Å½"
	} ;


	@Before
	public void setUp() throws Exception {
	}

	private String str2hex(String str) { return PortableShaUtil.str2hex(str); }

	@Test	
	public void testKeyOfSecret() {
		String plain = "I am plain text";
		String secret = "secret"; // A weak but common test key
		String sha1Secret = PortableShaUtil.sha1Hash(secret);
		log.debug("sha1Secret={}", sha1Secret);
		assertTrue(sha1Secret.equals(goodsha1));
		String enc = BlowFish.encrypt(sha1Secret, plain);
		log.debug("Blowfish encoded: {}", enc);
		assertTrue(enc.equals(encode1));
		String dec = BlowFish.decrypt(sha1Secret, enc);
		log.debug("Blowfish decoded: {}", dec);
		assertTrue(dec.equals(plain));
	}
	
	@Test	
	public void testBackAndForth() {
		String plain = "I am plain text";
		String hexkey = str2hex("this is the key");
		log.debug("Blowfish hexkey: {}", hexkey);
		assertTrue(hexkey.equals(hexkey2));
		String enc = BlowFish.encrypt(hexkey, plain);
		log.debug("Blowfish encoded: {}", enc);
		assertTrue(enc.equals(encode2));
		String dec = BlowFish.decrypt(hexkey, enc);
		log.debug("Blowfish decoded: {}", dec);
		assertTrue(dec.equals(plain));
	}

	@Test(expected=Exception.class)
	public void testNonHexKey() {
		String zzz = BlowFish.encrypt("this is not a hex key", "some text");
	}

	@Test
	public void testRoundTrip() {
		for ( String x : strings ) {
			String enc = BlowFish.encrypt(str2hex("this is the key"), x);
			String dec = BlowFish.decrypt(str2hex("this is the key"), enc);
			assertTrue(x.equals(dec));
		}
	}

	@Test
	public void testLengths() {
		String plain = "";
		String key = "sally";
		Random r = new Random();
		for ( int i = 0; i < 200; i++ ) {
			int n = r.nextInt() % 10;
			plain = plain + n;
			String enc = BlowFish.encrypt(str2hex(key), plain);
			String dec = BlowFish.decrypt(str2hex(key), enc);
			assertTrue(plain.equals(dec));
			key = plain;
		}
	}

	@Test
	public void testStrength() {
		String strong = BlowFish.strengthenKey("abc","ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		assertTrue("abcABCDEFGHIJKLM".equals(strong));
	}

	@Test
	// We make sure to test ths because changing the Blowfish Key Length can 
	// affect Interoperability with external tools that assume that the key length
	// is always 16 since the JCE only ships with 128-bit security by default
	public void testKeyLength() {
		if ( BlowFish.MAX_KEY_LENGTH != 16 ) {
			log.warn("Warning: changing the BlowFish.MAX_KEY_LENGTH may cause interoperability issues");
		}
		assertEquals(16,BlowFish.MAX_KEY_LENGTH);
	}

}
