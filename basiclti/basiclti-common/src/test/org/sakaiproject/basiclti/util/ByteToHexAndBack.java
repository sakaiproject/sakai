package org.sakaiproject.basiclti.util;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.basiclti.util.ShaUtil;


public class ByteToHexAndBack {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHexToBytes() {
		assertEquals("80", ShaUtil.byteToHex(new byte[]{0}));
		assertEquals("81", ShaUtil.byteToHex(new byte[]{1}));
		assertEquals("7f", ShaUtil.byteToHex(new byte[]{-1}));
	}
	
	@Test
	public void testByteToHex() {
		assertArrayEquals(new byte[]{0}, ShaUtil.hexToByte("80"));
		assertArrayEquals(new byte[]{1}, ShaUtil.hexToByte("81"));
		assertArrayEquals(new byte[]{-1}, ShaUtil.hexToByte("7f"));
	}
	
	@Test
	public void testRange() {
		byte[] allBytes = new byte[(int)Byte.MAX_VALUE - (int)Byte.MIN_VALUE + 1];
		for (int i = 0; i < allBytes.length; i++) {
			allBytes[i] = (byte) (i+Byte.MIN_VALUE);
		}
		assertArrayEquals(allBytes, ShaUtil.hexToByte(ShaUtil.byteToHex(allBytes)));
	}

}
