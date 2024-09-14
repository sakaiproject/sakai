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


public class ByteToHexAndBack {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHexToBytes() {
		assertEquals("80", LegacyShaUtil.byteToHex(new byte[]{0}));
		assertEquals("81", LegacyShaUtil.byteToHex(new byte[]{1}));
		assertEquals("7f", LegacyShaUtil.byteToHex(new byte[]{-1}));
	}
	
	@Test
	public void testByteToHex() {
		assertArrayEquals(new byte[]{0}, LegacyShaUtil.hexToByte("80"));
		assertArrayEquals(new byte[]{1}, LegacyShaUtil.hexToByte("81"));
		assertArrayEquals(new byte[]{-1}, LegacyShaUtil.hexToByte("7f"));
	}
	
	@Test
	public void testRange() {
		byte[] allBytes = new byte[(int)Byte.MAX_VALUE - (int)Byte.MIN_VALUE + 1];
		for (int i = 0; i < allBytes.length; i++) {
			allBytes[i] = (byte) (i+Byte.MIN_VALUE);
		}
		assertArrayEquals(allBytes, LegacyShaUtil.hexToByte(LegacyShaUtil.byteToHex(allBytes)));
	}

}
