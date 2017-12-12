/**
 * Copyright (c) 2006-2014 The Sakai Foundation
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
 *
 * This code was adapted from Chuck Hedreick's Linktool utlity code
 */

package org.sakaiproject.basiclti.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.sakaiproject.basiclti.util.PortableShaUtil;

/**
 * Support Blowfish Encryption and Decryption
 */
public class BlowFish {

	// Default JCE only supports 128 bit encryption
	public static final int MAX_KEY_LENGTH = 128 / 8;

	private static char[] hexChars = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};

	/**
	 * Strengthen a key which might be too short by extending with a salt text
	 * 
	 * @param secret
	 *        String
	 * @param salt
	 *        String
	 * @throws Exception.
	 */
	public static String strengthenKey(String secret, String salt )
	{
		if ( secret.length() > BlowFish.MAX_KEY_LENGTH ) return secret;

		// Extend the secret to the maximum Blowfish length
		secret = secret + salt;
		return secret.substring(0, BlowFish.MAX_KEY_LENGTH);
	}


	/**
	 * Decrypt a string using Blowfish
	 *
	 * @param secret
	 *	A hex-encoded secret - secrets longer than the maximum key length will be truncated
	 * @param str	
         *      The plain text to be encoded
	 */
	public static String encrypt(String secret, String str) {
		if ( secret == null ) return null;
		if ( secret.length() > MAX_KEY_LENGTH*2 ) {
			secret = secret.substring(0,MAX_KEY_LENGTH*2);
		}
		try {

			byte[] secretBytes = PortableShaUtil.hex2bin(secret);
			SecretKey secretKey = new SecretKeySpec(secretBytes, "Blowfish");
			Cipher ecipher = Cipher.getInstance("Blowfish");
			ecipher.init(Cipher.ENCRYPT_MODE, secretKey);

			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return PortableShaUtil.bin2hex(enc);
		} catch (javax.crypto.BadPaddingException e) {
			throw new Error(e);
		} catch (javax.crypto.IllegalBlockSizeException e) {
			throw new Error(e);
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new Error(e);
		} catch (java.security.InvalidKeyException e) {
			throw new Error(e);
		} catch (javax.crypto.NoSuchPaddingException e) {
			throw new Error(e);
		} catch (java.io.UnsupportedEncodingException e) {
			throw new Error(e);
		}
	}

	/**
	 * Decrypt a string using Blowfish
	 *
	 * @param secret
	 *	A hex-encoded secret - secrets longer than the maximum key length will be truncated
	 * @param enc	
         *      A hex-encoded ciphertext
	 */
	public static String decrypt (String secret, String enc) {
		if ( secret == null ) return null;
		if ( secret.length() > MAX_KEY_LENGTH*2 ) {
			secret = secret.substring(0,MAX_KEY_LENGTH*2);
		}
		try {
			byte [] secretBytes = PortableShaUtil.hex2bin(secret);
			SecretKey secretKey = new SecretKeySpec(secretBytes, "Blowfish");
			Cipher dcipher = Cipher.getInstance("Blowfish");
			dcipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dec = PortableShaUtil.hex2bin(enc);
			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}

