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

/* SAK-28122
 * Note that the binary to hex conversions lead to non-standard SHA1 and SHA256
 * as per http://csrc.nist.gov/groups/ST/toolkit/documents/Examples/SHA256.pdf
 * according to that document, a SHA256 of the string "abc" should be:
 *
 * BA7816BF 8F01CFEA 414140DE 5DAE2223 B00361A3 96177A9C B410FF61 F20015AD
 *
 * Of course the convention is to do lower case and no spaces so it would be
 * 
 *  ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad
 * 
 * But this class shifts the numbers up by 128 (instead of masking with 0xff) 
 * and so the hex strings that are returned are not interoperable with other sha
 * algorithms.
 *
 * I have renamed this class LegacyShaUtil - we cannot remove it because 
 * we use it for AES encrypted values that are now stored in the our 
 * database as well as LTI tool databases all over the place. :(
 */

package org.sakaiproject.lti.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegacyShaUtil {
	private static final char[] TOHEX = "0123456789abcdef".toCharArray();
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
}