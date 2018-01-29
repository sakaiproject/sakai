/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
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

package org.sakaiproject.linktool;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * Some Sakai Utility code for the Rutgers LinkTool.
 */
@Slf4j
public class LinkToolUtil {

	private static final String privkeyname = "sakai.rutgers.linktool.privkey";
	private static final String saltname = "sakai.rutgers.linktool.salt";

	private static Object sync_object = new Object();
	private static boolean LinkToolSetupComplete = false;
	private static String homedir = null;
	private static SecretKey secretKey = null;
	private static SecretKey salt = null;

	private static SecretKey readSecretKey(String filename, String alg) 
	{
		FileInputStream file = null;
		SecretKey privkey = null;
		try {
			file = new FileInputStream(filename);
			byte[] bytes = new byte[file.available()];
			file.read(bytes);
			privkey = new SecretKeySpec(bytes, alg);
		}
		catch (Exception e)
		{
			log.error("Unable to read key from {}", filename);
			privkey = null;
		}
		finally
		{
			if ( file != null ) 
			{
				try
				{
					file.close();
				}
				catch (Exception e)
				{
					log.error("Unable to close file {}", filename);
				}
			}
		}
		return privkey;
	}

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
	 * Generate a secret key, and write it to a file
	 * 
	 * @param dirname
	 *        writes to file privkeyname in this 
	 *        directory. dirname assumed to end in /
	 */

	private static void genkey(String dirname) 
	{
		try 
		{
			/* Generate key. */
			log.info("Generating new key in {}{}", dirname, privkeyname);
			SecretKey key = KeyGenerator.getInstance("Blowfish").generateKey();

			/* Write private key to file. */
			writeKey(key, dirname + privkeyname);
		} catch (Exception e) {
			log.debug("Error generating key", e);
		}

	}

	/**
	 * Writes <code>key</code> to file with name <code>filename</code>
	 *
	 * @throws IOException if something goes wrong.
	 */
	private static void writeKey(Key key, String filename) 
	{
		FileOutputStream file = null;
		try
		{
			file = new FileOutputStream(filename);
			file.write(key.getEncoded());
		}
		catch (FileNotFoundException e)
		{
			log.error("Unable to write new key to {}", filename);
		}
		catch (IOException e)
		{
			log.error("Unable to write new key to {}", filename);
		}
		finally
		{
			if ( file != null ) 
			{
				try
				{
					file.close();
				}
				catch (Exception e)
				{
					log.error("Unable to write new key to {}", filename);
				}
			}
		}
	}

	/**
	 * Generate a random salt, and write it to a file
	 * 
	 * @param dirname
	 *        writes to file saltname in this 
	 *        directory. dirname assumed to end in /
	 */

	private static void gensalt(String dirname) 
	{
		try {
			// Generate a key for the HMAC-SHA1 keyed-hashing algorithm
			KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
			SecretKey key = keyGen.generateKey();
			writeKey(key, dirname + saltname);
		} catch (Exception e) {
			log.warn("Error generating salt", e);
		}
	}

	public static String encrypt(String str) {
		LinkToolSetup();
		if ( secretKey == null ) return null;
		try {

			Cipher ecipher = Cipher.getInstance("Blowfish");
			ecipher.init(Cipher.ENCRYPT_MODE, secretKey);

			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return byteArray2Hex(enc);
		} catch (javax.crypto.BadPaddingException e) {
			log.warn("linktool encrypt bad padding");
		} catch (javax.crypto.IllegalBlockSizeException e) {
			log.warn("linktool encrypt illegal block size");
		} catch (java.security.NoSuchAlgorithmException e) {
			log.warn("linktool encrypt no such algorithm");
		} catch (java.security.InvalidKeyException e) {
			log.warn("linktool encrypt invalid key");
		} catch (javax.crypto.NoSuchPaddingException e) {
			log.warn("linktool encrypt no such padding");
		} catch (java.io.UnsupportedEncodingException e) {
			log.warn("linktool encrypt unsupported encoding");
		}
		return null;
	}

	public static String decrypt (String enc) {
		LinkToolSetup();
		if ( secretKey == null ) return null;
		try {
			Cipher dcipher = Cipher.getInstance("Blowfish");
			dcipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] dec = hex2byte(enc);
			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);
			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (Exception ignore) {
			log.warn("linktool decrypt failed");
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


	// Setup the LinkTool Environment Variables one time
	// If this fails, it does not re-try
	public static void LinkToolSetup()
	{
		if ( LinkToolSetupComplete ) return;

		// We will only do this once - so we make all wait here
		synchronized(sync_object) {
			homedir = ServerConfigurationService.getString("linktool.home", ServerConfigurationService.getSakaiHomePath());
			if (homedir == null) homedir = "/etc/";
			if (!homedir.endsWith("/")) homedir = homedir + "/";

			if (!(new File(homedir + privkeyname)).canRead()) {
				genkey(homedir);
			}

			secretKey = readSecretKey(homedir + privkeyname, "Blowfish");

			if (!(new File(homedir + saltname)).canRead()) {
				gensalt(homedir);
			}

			salt = readSecretKey(homedir + saltname, "HmacSHA1");

			if ( salt != null && secretKey != null ) {
				log.info("LinkToolSetup complete");
			} else {
				log.warn("LinkToolSetup failed - cannot create encrypted sessions");
			}
			LinkToolSetupComplete = true;
		}
	}

}
