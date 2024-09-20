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

public class PortableShaUtil {
	public static final String UTF8 = "UTF8";

	public static byte[] sha1(final String todigest) {
		try {
			byte[] b = todigest.getBytes("UTF-8");
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			return sha1.digest(b);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}

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
		return bin2hex(b);
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
		return bin2hex(b);
	}

	public static String sha512Hash(final String tohash) {
		byte[] b = null;
		try {
			b = tohash.getBytes("UTF-8");
			MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
			b = sha512.digest(b);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
		return bin2hex(b);
	}

	private static final char[] TOHEX = "0123456789abcdef".toCharArray();

	/**
	 * Checks to see if a string is legitimate hex data
         */
	public static boolean isHex(String str)
	{
		for(int i = 0; i < str.length(); i++)
		{
			int digit = Character.digit(str.charAt(i), 16);
			if ( digit == -1 ) return false;
		}
		return true;
	}

	// Support bin2hex as per 
	// http://csrc.nist.gov/groups/ST/toolkit/documents/Examples/SHA256.pdf
	// Using lower case as convention
	public static String bin2hex(final byte[] base) {
		if (base != null) {
			char[] c = new char[base.length * 2];
			int i = 0;

			for (byte b : base) {
				int j = b;
				// j = j + 128;  // Wrong - not portable
				j = j & 0xff; // Right - portable
				c[i++] = TOHEX[j / 0x10];
				c[i++] = TOHEX[j % 0x10];
			}
			return new String(c);
		} else {
			return null;
		}
	}

	/**
	 * Hex-encode a string of characters - convienence class
         */
	public static String str2hex(String str) {
		byte[] b = null;
		try {
			b = str.getBytes("UTF-8");
			return bin2hex(b);
		} catch (UnsupportedEncodingException e) {
			throw new Error(e);
		}
	}

	public static byte[] hex2bin(String hex) {
		if (hex == null) {
			return null;
		}
		byte[] base = new byte[hex.length() / 2];
		int i = 0;
		for (int j = 0; j < base.length; j++) {
			int digit = 0; 

			int ch = Character.digit(hex.charAt(i++), 16);
			if ( ch < 0 ) {
				throw new RuntimeException("Illegal character in hex string");
			}
			digit += ch * 0x10;

			ch = Character.digit(hex.charAt(i++), 16);
			if ( ch < 0 ) {
				throw new RuntimeException("Illegal character in hex string");
			}
			digit += ch % 0x10;

			base[j] = (byte) digit;
		}
		return base;
	}

}
