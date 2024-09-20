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
import org.sakaiproject.basiclti.util.PortableShaUtil;


public class PortableShaUtilTest {

	// As per http://csrc.nist.gov/groups/ST/toolkit/documents/Examples/SHA256.pdf
	// a SHA256 of the string "abc" should be:
	// ba7816bf 8f01cfea 414140de 5dae2223 b00361a3 96177a9c b410ff61 f20015aD
	final String nist_sha256_of_abc = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";

	// Also output of echo(hash("sha512","abc")."\n"); in PHP
	final String nist_sha512_of_abc = "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f";

	// As per echo(hash('sha1',"abc"););
	final String php_sha1_of_abc = "a9993e364706816aba3e25717850c26c9cd0d89d";

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_hex2bin() {
		assertEquals("00", PortableShaUtil.bin2hex(new byte[]{(byte)0}));
		assertEquals("01", PortableShaUtil.bin2hex(new byte[]{(byte)1}));
		assertEquals("11", PortableShaUtil.bin2hex(new byte[]{(byte)0x11}));
		assertEquals("ff", PortableShaUtil.bin2hex(new byte[]{(byte)0xff}));
		assertEquals("000111ff", PortableShaUtil.bin2hex(new byte[]{0,1,0x11,(byte)0xff}));
	}
	
	@Test
	public void test_bin2hex() {
		assertArrayEquals(new byte[]{0}, PortableShaUtil.hex2bin("00"));
		assertArrayEquals(new byte[]{0x11}, PortableShaUtil.hex2bin("11"));
		assertArrayEquals(new byte[]{(byte)0xff}, PortableShaUtil.hex2bin("ff"));
		assertArrayEquals(new byte[]{1,2,3,4,5}, PortableShaUtil.hex2bin("0102030405"));
	}
	
	@Test
	public void testRange() {
		byte[] allBytes = new byte[(int)Byte.MAX_VALUE - (int)Byte.MIN_VALUE + 1];
		for (int i = 0; i < allBytes.length; i++) {
			allBytes[i] = (byte) (i+Byte.MIN_VALUE);
		}
		assertArrayEquals(allBytes, PortableShaUtil.hex2bin(PortableShaUtil.bin2hex(allBytes)));
	}

	@Test
	public void testNist256() {
		String abc_hash = PortableShaUtil.sha256Hash("abc");
		assertEquals(abc_hash, nist_sha256_of_abc);
	}

	@Test
	public void testNist512() {
		String abc_hash = PortableShaUtil.sha512Hash("abc");
		assertEquals(abc_hash, nist_sha512_of_abc);
	}


	@Test
	public void testPHP1() {
		String abc_hash = PortableShaUtil.sha1Hash("abc");
		assertEquals(abc_hash, php_sha1_of_abc);
	}

}
