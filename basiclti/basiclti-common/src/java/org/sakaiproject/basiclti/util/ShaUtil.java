/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.basiclti.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShaUtil {
	private static final char[] TOHEX = "0123456789abcdef".toCharArray();
	public static final String UTF8 = "UTF8";

	public static String sha1Hash(final String tohash) {
		byte[] b = null;
		try {
			b = tohash.getBytes("UTF-8");
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			b = sha1.digest(b);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
		return byteToHex(b);
	}

	public static String sha256Hash(final String tohash) {
		byte[] b = null;
		try {
			b = tohash.getBytes("UTF-8");
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			b = sha256.digest(b);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
		return byteToHex(b);
	}

	public static String byteToHex(final byte[] base) {
		if (base != null) {
			char[] c = new char[base.length * 2];
			int i = 0;

			for (byte b : base) {
				int j = b;
				j = j + 128;
				c[i++] = TOHEX[j / 0x10];
				c[i++] = TOHEX[j % 0x10];
			}
			return new String(c);
		} else {
			return null;
		}
	}
	

	public static byte[] hexToByte(String hex) {
		if (hex == null) {
			return null;
		}
		byte[] base = new byte[hex.length() / 2];
		int i = 0;
		for (int j = 0; j < base.length; j++) {
			int digit = -128;
			digit += Character.digit(hex.charAt(i++), 16) * 0x10;
			digit += Character.digit(hex.charAt(i++), 16) % 0x10;
			base[j] = (byte) digit;
		}
		return base;
	}

	public static void main(String[] args) {
		final String sample1 = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï¶Ä©úÆûÂÉ¾Ö³²µ÷ÃÅ½";
		final String sample2 = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï¶Ä©úÆûÂÉ¾Ö³²µ÷ÃÅ½";
		final String sample3 = "12345:/sites/foo/bar !@#$%^&*()_+|}{\":?><[]\';/.,'Áª£¢°¤¦¥»¼Ð­ÇÔÒ¹¿ö¬ ¨«Ï¶Ä©úÆûÂÉ¾Ö³²µ÷ÃÅÅ";
		final String sample1Hash = sha1Hash(sample1);
		final String sample2Hash = sha1Hash(sample2);
		final String sample3Hash = sha1Hash(sample3);
		System.out.println(sample1Hash);
		System.out.println(sample2Hash);
		System.out.println(sample3Hash);
		assert (sample1Hash.equals(sample2Hash));
		System.out.println(sample1Hash.equals(sample2Hash)
				+ "=(sample1Hash.equals(sample2Hash))");
		assert (!sample3Hash.equals(sample1Hash));
		System.out.println(!sample3Hash.equals(sample1Hash)
				+ "=(!sample3Hash.equals(sample1Hash))");
		assert (null == byteToHex(null));
		final boolean testNull = (null == byteToHex(null));
		System.out.println(testNull + "=(null == byteToHex(null))");
	}
}
