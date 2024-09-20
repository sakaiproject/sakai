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

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.basiclti.util.LegacyShaUtil;

/* SAK-28122 - 
 * org.sakaiproject.basiclti.util.ShaUtil hexadecimal mapping is not compatible cross-language
 * 
 * This is testing the legacy SHA1 and SHA256 routines which use a non-standard
 * byteToHex and hexToByte mapping.
 */
public class LegacyShaUtilTest {

	// As per http://csrc.nist.gov/groups/ST/toolkit/documents/Examples/SHA256.pdf
	// a SHA256 of the string "abc" should be:
	// ba7816bf 8f01cfea 414140de 5dae2223 b00361a3 96177a9c b410ff61 f20015aD
	final String nist_sha256_of_abc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

	// As per echo(hash('sha1',"abc"););
	final String php_sha1_of_abc = "a9993e364706816aba3e25717850c26c9cd0d89d";

	final String sample1 = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï<8c>¶Ä©úÆûÂÉ¾Ö³²µ÷Ã<8d>Å½";
	final String s1_1_hash = "79517ed28fedd514de98c0376e1183ae0a7785a5";
	final String s1_256_hash = "c55b3b5fd494f9d87bf2fd5a3c7a6a0bd0b32718513897d7f78e2cbaf83f3288";

	final String sample2 = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï<8c>¶Ä©úÆûÂÉ¾Ö³²µ÷Ã<8d>Å½";
	final String s2_1_hash = "79517ed28fedd514de98c0376e1183ae0a7785a5";
	final String s2_256_hash = "c55b3b5fd494f9d87bf2fd5a3c7a6a0bd0b32718513897d7f78e2cbaf83f3288";

	final String sample3 = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï<8c>¶Ä©úÆûÂÉ¾Ö³²µ÷Ã<8d>ÅÅ";
	final String s3_1_hash = "e001cf3ace01ca7af06b60ab20c6377a959f0bab";
	final String s3_256_hash = "c90605a819cc45b77f11f9e3444fa5ecb93836c2dd5c650719b9ee6165099fe4";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testSamples1() {
		String hash;
		hash = LegacyShaUtil.sha1Hash(sample1);
		assertEquals(s1_1_hash, hash);
		hash = LegacyShaUtil.sha1Hash(sample2);
		assertEquals(s2_1_hash, hash);
		hash = LegacyShaUtil.sha1Hash(sample3);
		assertEquals(s3_1_hash, hash);
	}

	@Test
	public void testSamples256() {
		String hash;
		hash = LegacyShaUtil.sha256Hash(sample1);
		assertEquals(s1_256_hash, hash);
		hash = LegacyShaUtil.sha256Hash(sample2);
		assertEquals(s2_256_hash, hash);
		hash = LegacyShaUtil.sha256Hash(sample3);
		assertEquals(s3_256_hash, hash);
	}

	@Test
	public void testRange() {
		byte[] allBytes = new byte[(int)Byte.MAX_VALUE - (int)Byte.MIN_VALUE + 1];
		for (int i = 0; i < allBytes.length; i++) {
			allBytes[i] = (byte) (i+Byte.MIN_VALUE);
		}
		assertArrayEquals(allBytes, LegacyShaUtil.hexToByte(LegacyShaUtil.byteToHex(allBytes)));
	}

	@Test
	public void test_byteToHex_not() {
		// Note all are NOT Equals as these are the correct mappings per NIST
		assertFalse("00".equals(LegacyShaUtil.byteToHex(new byte[]{(byte)0})));
		assertFalse("01".equals(LegacyShaUtil.byteToHex(new byte[]{(byte)1})));
		assertFalse("11".equals(LegacyShaUtil.byteToHex(new byte[]{(byte)0x11})));
		assertFalse("ff".equals(LegacyShaUtil.byteToHex(new byte[]{(byte)0xff})));
		assertFalse("000111ff".equals(LegacyShaUtil.byteToHex(new byte[]{0,1,0x11,(byte)0xff})));
	}
	
	@Test
	public void testNist256_not() {
		// Note all are NOT Equals as these are the correct mappings per NIST
		String abc_hash = LegacyShaUtil.sha256Hash("abc");
		assertFalse(abc_hash.equals(nist_sha256_of_abc));
	}

	@Test
	public void testPHP1_not() {
		// Note all are NOT Equals as these are the correct mappings per NIST
		String abc_hash = LegacyShaUtil.sha1Hash("abc");
		assertFalse(abc_hash.equals(php_sha1_of_abc));
	}

}
