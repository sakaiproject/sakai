package org.sakaiproject.basiclti.util;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.basiclti.util.BlowFish;


public class BlowFishTest {

	public static final String encode1 = "f18c3f45a1a635ba0c03649b101b7fbd";

	public static final String strings[] = {
		"Short", "Medium", 
		"12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï<8c>¶Ä©úÆûÂÉ¾Ö³²µ÷Ã<8d>Å½"
	} ;


	@Before
	public void setUp() throws Exception {
	}

	
	@Test	
	public void testNullDecrypt() {
		String plain = "I am plain text";
		String enc = BlowFish.encrypt("this is the key", plain);
		assertTrue(enc.equals(encode1));
		// System.out.println("Blowfish encoded: "+enc);
		String dec = BlowFish.decrypt("this is the key", enc);
		// System.out.println("Blowfish decoded: "+dec);
		assertTrue(dec.equals(plain));
	}

	@Test
	public void testRoundTrip() {
		for ( String x : strings ) {
			String enc = BlowFish.encrypt("this is the key", x);
			String dec = BlowFish.decrypt("this is the key", enc);
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
			String enc = BlowFish.encrypt(key, plain);
			String dec = BlowFish.decrypt(key, enc);
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
			System.out.println("Warning: changing the BlowFish.MAX_KEY_LENGTH may cause interoperability issues");
		}
		assertEquals(16,BlowFish.MAX_KEY_LENGTH);
	}

}
