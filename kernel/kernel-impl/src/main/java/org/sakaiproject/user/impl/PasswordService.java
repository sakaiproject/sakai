/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-impl/src/main/java/org/sakaiproject/user/impl/OpenAuthnComponent.java $
 * $Id: OpenAuthnComponent.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008, 2009, 2010 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Random;

import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Service to check that a password matches and to generate encrypted passwords from plaintext.
 * By default it's salted and SHA-256 digested.
 * @author buckett
 *
 */
public class PasswordService {

	private final static Log log = LogFactory.getLog(PasswordService.class);

	private Random saltSource = new Random();

	/**
	 * Prefix for password that was converted from an MD5 one.
	 */
	public static final String MD5_SALT_SHA256 = "MD5-SALT-SHA256:";

	/**
	 * Prefix for password that was converted from an MD5 one with the SAK-5922 bug.
	 */
	public static final String MD5TRUNC_SALT_SHA256 = "MD5TRUNC-SALT-SHA256:";

	/** 
	 * Length of salt in bytes.
	 */
	private int saltLength = 4;

	/**
	 * The character that separates the salt from the hash.
	 * Changing this will break all existing passwords (except MD5).
	 */
	private String saltDelim = ":";

	/**
	 * The default algorithm to use then setting a password and when checking a password.
	 * Changing this will break all existing password (except MD5).
	 */
	private String defaultAlgorithm = "SHA-256";

	/**
	 * Check to see if the password matches the encrypted version.
	 * @param password
	 * @param encrypted
	 * @return
	 */
	public boolean check(String password, String encrypted) {
		// Make sure we have good data.
		if (password == null || encrypted == null)
			return false;
		
		int length = encrypted.length();
		// This is the old way of encrypting passwords.
		if ( length == 20 || length == 24 ) {
			String passwordEnc = hash(password, "MD5");
			if (passwordEnc != null) {
				// SAK-5922 Some password are missing last 4 characters.
				if (length == 20) {
					passwordEnc = passwordEnc.substring(0, 20);
				}
				return encrypted.equals(passwordEnc);
			}
		}
		
		String passwordMod = password;
		String expectedHash = encrypted;
		
		if (encrypted.startsWith(MD5_SALT_SHA256)) {
			passwordMod = hash(password, "MD5");
			expectedHash = encrypted.substring(MD5_SALT_SHA256.length());
		}
		if (encrypted.startsWith(MD5TRUNC_SALT_SHA256)) {
			passwordMod = hash(password, "MD5");
			if (passwordMod != null && passwordMod.length() > 20) {
				passwordMod = passwordMod.substring(0, 20);
			}
			expectedHash = encrypted.substring(MD5TRUNC_SALT_SHA256.length());
		}
		int saltDelimPos = expectedHash.indexOf(saltDelim);
		String shaSource = passwordMod;
		if (saltDelimPos != -1) {
			String salt = expectedHash.substring(0, saltDelimPos);
			expectedHash = expectedHash.substring(saltDelimPos+1);
			shaSource = salt + passwordMod;
		}
		String passwordEnc = hash(shaSource, defaultAlgorithm);
		return expectedHash.equals(passwordEnc);
	}
	
	/**
	 * Create a new encrypted password.
	 * @param password The password to encrypt.
	 * @return The resultant string.
	 */
	public String encrypt(String password) {
		String salt = salt(saltLength);
		String source = salt + password;
		String hash = hash(source, defaultAlgorithm);
		return salt + saltDelim + hash;
	}
	
	/**
	 * Digest and Base64 encode the password.
	 * @param password The password to hash.
	 * @param algorithm The Digest Algorithm to use.
	 * @return The digested password or <code>null</code> if it failed.
	 */
	protected String hash(String password, String algorithm) {
		try
		{
			// compute the digest using the MD5 algorithm
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] digest = md.digest(password.getBytes("UTF-8"));

			// encode as base64
			ByteArrayOutputStream bas = new ByteArrayOutputStream(lengthBase64(digest.length));
			OutputStream encodedStream = MimeUtility.encode(bas, "base64");
			encodedStream.write(digest);
			
			// close the stream to complete the encoding
			encodedStream.close();
			String rv = bas.toString().trim(); // '\r\n' is appended by encode()

			return rv;
		}
		catch (Exception e)
		{
			log.warn("Failed with "+ algorithm, e);
			return null;
		}
	}
	
	/**
	 * Generate a salt which is Base64 encoded.
	 * @param length The number of bytes to use for the source.
	 * @return A Base64 version of the salt. So it's longer than the source length.
	 */
	protected String salt(int length) {
		try{
			byte[] salt = new byte[length];
			saltSource.nextBytes(salt);
			ByteArrayOutputStream bas = new ByteArrayOutputStream(lengthBase64(length));
			OutputStream saltStream;
			saltStream = MimeUtility.encode(bas, "base64");
			saltStream.write(salt);
			saltStream.close();
			String rv =  bas.toString().trim(); // '\r\n' is appended by encode() 
			return rv;
		}catch(Exception e){
			log.warn("Failed to generate salt.", e);
		}
		return "";
	}

	/**
	 * The length needed for base64 encoding some input.
	 * http://en.wikipedia.org/wiki/Base64
	 * @param length The length in bytes of the source.
	 * @return The size in bytes of the output.
	 */
	private int lengthBase64(int length) {
		return (length + 2 - ((length + 2) % 3)) * 4 / 3;
	}

	public void setSaltLength(int saltLength) {
		this.saltLength = saltLength;
	}

	public void setSaltDelim(String saltDelim) {
		this.saltDelim = saltDelim;
	}

	public void setDefaultAlgorithm(String defaultAlgorithm) {
		this.defaultAlgorithm = defaultAlgorithm;
	}
	
}
