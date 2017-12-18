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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import lombok.extern.slf4j.Slf4j;

/**
 * Service to check that a password matches and to generate encrypted passwords from plaintext.
 * By default we use PBKDF2.
 *
 * @author buckett
 */
@Slf4j
public class PasswordService {
	private Random saltSource = new SecureRandom();

	/**
	 * Prefix for password that was converted from an MD5 one.
	 */
	public static final String MD5_SALT_SHA256 = "MD5-SALT-SHA256:";

	/**
	 * Prefix for password that was converted from an MD5 one with the SAK-5922 bug.
	 */
	public static final String MD5TRUNC_SALT_SHA256 = "MD5TRUNC-SALT-SHA256:";

	/**
	 * Prefix for password
	 */
	public static final String PBKDF2 = "PBKDF2:";

	/**
	 * Length of salt in bytes.
	 */
	private int saltLength = 16;

	/**
	 * The character that separates the salt from the hash.
	 * Changing this will break all existing passwords (except MD5).
	 */
	private String saltDelim = ":";

	/**
	 * Check to see if the password matches the encrypted version.
	 *
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
		if (length == 20 || length == 24) {
			String passwordEnc = hash(password, "MD5");
			if (passwordEnc != null) {
				// SAK-5922 Some password are missing last 4 characters.
				if (length == 20) {
					passwordEnc = passwordEnc.substring(0, 20);
				}
				return encrypted.equals(passwordEnc);
			}
		}


		if (encrypted.startsWith(PBKDF2)) {
			return checkPbkdf2(password, encrypted);
		} else {
			return checkLegacy(password, encrypted);
		}
	}

	/**
	 * This is for checking checking legacy passwords.
	 */
	private boolean checkLegacy(String password, String encrypted) {
		String passwordMod = password;
		String expectedHash = encrypted;
		// Some passwords got re-encrypted to improve security, so check this.
		if (encrypted.startsWith(MD5_SALT_SHA256)) {
			// First do the old MD5 hash
			passwordMod = hash(password, "MD5");
			expectedHash = encrypted.substring(MD5_SALT_SHA256.length());
		}
		if (encrypted.startsWith(MD5TRUNC_SALT_SHA256)) {
			// First do the broken old MD5 hash
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
			expectedHash = expectedHash.substring(saltDelimPos + 1);
			shaSource = salt + passwordMod;
		}
		String passwordEnc = hash(shaSource, "SHA-256");
		return expectedHash.equals(passwordEnc);
	}

	/**
	 * This is how all future passwords should be checked.
	 */
	private boolean checkPbkdf2(String password, String encrypted) {
		String expectedHash;
		expectedHash = encrypted.substring(PBKDF2.length());
		int saltDelimPos = expectedHash.indexOf(saltDelim);
		if (saltDelimPos != -1) {
			String salt = expectedHash.substring(0, saltDelimPos);
			expectedHash = expectedHash.substring(saltDelimPos + 1);
			Base64.Decoder decoder = Base64.getDecoder();
			byte[] passwordEnc = encrypt(decoder.decode(salt), password);
			byte[] hash = decoder.decode(expectedHash);
			return Arrays.equals(hash, passwordEnc);
		}
		return false;
	}

	/**
	 * Create a new encrypted password.
	 *
	 * @param password The password to encrypt.
	 * @return The resultant string.
	 */
	public String encrypt(String password) {
		byte[] salt = salt(saltLength);
		Base64.Encoder encoder = Base64.getEncoder();
		return PBKDF2 + encoder.encodeToString(salt) + saltDelim + encoder.encodeToString(encrypt(salt, password));
	}

	public byte[] encrypt(byte[] salt, String password) {
		// 8192 Rounds and 128 bit key
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 8192, 128);
		SecretKeyFactory f;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
			byte[] hash = f.generateSecret(spec).getEncoded();
			return hash;

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException("Failed to encrypt password.", e);
		}
	}

	public String oldEncrpt(String password) {
		String salt = Base64.getEncoder().encodeToString(salt(saltLength));
		String source = salt + password;
		String hash = hash(source, "SHA-256");
		return salt + saltDelim + hash;
	}

	/**
	 * Digest and Base64 encode the password.
	 *
	 * @param password  The password to hash.
	 * @param algorithm The Digest Algorithm to use.
	 * @return The digested password or <code>null</code> if it failed.
	 */
	String hash(String password, String algorithm) {
		try {
			// compute the digest using the MD5 algorithm
			MessageDigest md = MessageDigest.getInstance(algorithm);
			byte[] digest = md.digest(password.getBytes("UTF-8"));

			return Base64.getEncoder().encodeToString(digest);
		} catch (Exception e) {
			log.warn("Failed with " + algorithm, e);
			return null;
		}
	}

	/**
	 * Generate a salt which is Base64 encoded.
	 *
	 * @param length The number of bytes to use for the source.
	 * @return An arrary of salt bytes.
	 */
	private byte[] salt(int length) {
		byte[] salt = new byte[length];
		saltSource.nextBytes(salt);
		return salt;
	}

	public void setSaltLength(int saltLength) {
		this.saltLength = saltLength;
	}

	public void setSaltDelim(String saltDelim) {
		this.saltDelim = saltDelim;
	}

}
