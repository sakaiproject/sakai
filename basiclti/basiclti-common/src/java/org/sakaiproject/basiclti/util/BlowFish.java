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

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import org.sakaiproject.component.cover.ServerConfigurationService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

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
	 * Convert byte array to hex string
	 * 
	 * @param ba
	 *        array of bytes
	 * @throws Exception.
	 */
	private static String byteArray2Hex(byte[] ba){
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < ba.length; i++){
			int hbits = (ba[i] & 0x000000f0) >> 4;
			int lbits = ba[i] & 0x0000000f;
			sb.append("" + hexChars[hbits] + hexChars[lbits]);
		}
		return sb.toString();
	}

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


	public static String encrypt(String secret, String str) {
		if ( secret == null ) return null;
		if ( secret.length() > MAX_KEY_LENGTH ) {
			secret = secret.substring(0,MAX_KEY_LENGTH);
		}
		try {

			SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "Blowfish");
			Cipher ecipher = Cipher.getInstance("Blowfish");
			ecipher.init(Cipher.ENCRYPT_MODE, secretKey);

			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return byteArray2Hex(enc);
		} catch (javax.crypto.BadPaddingException e) {
			System.out.println("Blowfish encrypt bad padding");
		} catch (javax.crypto.IllegalBlockSizeException e) {
			System.out.println("Blowfish encrypt illegal block size");
		} catch (java.security.NoSuchAlgorithmException e) {
			System.out.println("Blowfish encrypt no such algorithm");
		} catch (java.security.InvalidKeyException e) {
			System.out.println("Blowfish encrypt invalid key");
		} catch (javax.crypto.NoSuchPaddingException e) {
			System.out.println("Blowfish encrypt no such padding");
		} catch (java.io.UnsupportedEncodingException e) {
			System.out.println("Blowfish encrypt unsupported encoding");
		}
		return null;
	}

	public static String decrypt (String secret, String enc) {
		if ( secret == null ) return null;
		if ( secret.length() > MAX_KEY_LENGTH ) {
			secret = secret.substring(0,MAX_KEY_LENGTH);
		}
		try {
			SecretKey secretKey = new SecretKeySpec(secret.getBytes(), "Blowfish");
			Cipher dcipher = Cipher.getInstance("Blowfish");
			dcipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dec = hex2byte(enc);
			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (Exception ignore) {
			System.out.println("Blowfish decrypt failed");
		}
		return null;
	}


	public static byte[] hex2byte(String strhex) {
		if(strhex==null) return null;
		int l = strhex.length();

		if(l %2 ==1) return null;
		byte[] b = new byte[l/2];
		for(int i = 0 ; i < l/2 ;i++){
			b[i] = (byte)Integer.parseInt(strhex.substring(i *2,i*2 +2),16);
		}
		return b;
	}
}

