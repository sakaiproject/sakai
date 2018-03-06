/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.user.detail;

import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

/**
 * This class provides encryption/decryption services. Service is thread safe.
 * It makes sure that the same source text never encrypts to the same ciphertext. It's designed to be performant
 * so that multiple encryption/decryptions can be done on a single request. Please don't use this
 * for anything else as it's specifically designed with one opertaion in mind.
 */
@Slf4j
public class ValueEncryptionUtilities {

	public static final int UTF_8_ILLEGAL = 255;

	private final static String CIPHER_INSTANCE = "AES/CBC/PKCS5Padding";
	private final static String SECRET_KEYFACTORY = "PBKDF2WithHmacSHA256";
	private final static String SECRET_KEYSPEC = "AES";
	// We don't need many rounds as we aren't getting users to type a key but are instead
	// generating one. Having lots of rounds makes the key generation part too slow.
	private static final int KEY_ROUNDS = 1;

	private SecureRandom random = new SecureRandom();
	private Base64.Encoder encoder = Base64.getEncoder();
	private Base64.Decoder decoder = Base64.getDecoder();

	private int keyLength;

	public void init() throws NoSuchAlgorithmException {
		keyLength = Math.min(Cipher.getMaxAllowedKeyLength(CIPHER_INSTANCE), 256);
	}

	// The encryption key, this should be identical across nodes so that if we move to caches that are shared
	// across nodes decryption works ok.
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) throws GeneralSecurityException {
		this.key = key;
	}

	/**
	 * This salts and encrypts a value and returns a base64 encoded version of the encrypted value.
	 * @param value The value to be encrypted.
	 * @param length The number on bytes to expand out to the source value to. This is so that all encryption
	 *               operations generate the same length output. Watch out for multibyte characters as these will mean
	 *               that your length must be more than the number of character in the string. If 0 then no padding is
	 *               done.
	 * @return A salted base64 encrypted version of the value.
	 * @throws RuntimeException If encryption fails for any reason.
	 */
	public String encrypt(String value, int length) {
		try {
			byte[] salt = getSalt();
			SecretKey secret = getSecret(key, salt, getKeyLength());
			Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
			cipher.init(Cipher.ENCRYPT_MODE, secret);
			AlgorithmParameters params = cipher.getParameters();
			//get IV from cipher parameters
			IvParameterSpec parameterSpec = params.getParameterSpec(IvParameterSpec.class);
			// AES always has 128bit IV
			byte[] iv = parameterSpec.getIV();
			byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
			if (length != 0 && bytes.length > length) {
				throw new IllegalArgumentException("Can't encode as it's longer than our fixed length.");
			}
			int finalLength = (length == 0)?bytes.length: length;
			byte[] source = new byte[finalLength];
			System.arraycopy(bytes, 0, source, 0, bytes.length);
			// Fill the remainded of the array with illegal UTF-8 characters.
			Arrays.fill(source, bytes.length, source.length, (byte) UTF_8_ILLEGAL);
			byte[] ciphertext = cipher.doFinal(source);

			//create final array (in bytes) : IV + SALT + TEXT
			byte[] finalCiphertext = new byte[ciphertext.length+2*16];
			System.arraycopy(iv, 0, finalCiphertext, 0, 16);
			System.arraycopy(salt, 0, finalCiphertext, 16, 16);
			System.arraycopy(ciphertext, 0, finalCiphertext, 32, ciphertext.length);
			//encode all bytes in a Base64 string
			return encoder.encodeToString(finalCiphertext);
		} catch(Exception e){
			// We must not log out the value here so that the plaintext can't accidentally end up in the logs
			log.error("Error while encrypting.", e);
			return null;
		}
	}

	/**
	 * This extracts the salt and decrypts a value.
	 * @param encrypted The salted and encrypted value which is base64 encoded.
	 * @return The plain value;
	 * @throws RuntimeException If decryption fails for any reason.
	 */
	public String decrypt(String encrypted) {
		try {
			//decode the whole string -> result : IV + SALT + TEXT
			byte[] finalCipherBytes = decoder.decode(encrypted);
			// IV + SALT + 1 BLOCK = 48
			if (finalCipherBytes.length < 48) {
				throw new IllegalArgumentException("The supplied data is not correctly encoded: "+ encrypted);
			}
			//0 - 16 : IV
			byte[] ivBytes = new byte[16];
			System.arraycopy(finalCipherBytes, 0, ivBytes, 0, 16);
			byte[] salt = new byte[16];
			System.arraycopy(finalCipherBytes, 16, salt, 0, 16);
			//16 - TOEND : TEXT
			byte[] ciphertext = new byte[finalCipherBytes.length-2*16];
			System.arraycopy(finalCipherBytes, 32, ciphertext, 0, ciphertext.length);
			// Decrypt the message, given derived key and initialization vector.
			Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);

			SecretKey secret = getSecret(key, salt, getKeyLength());
			cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));
			byte[] bytes = cipher.doFinal(ciphertext);
			// This is so that we ignore invalid padding characters.
			String plaintext = StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.IGNORE).
					decode(ByteBuffer.wrap(bytes)).toString();
			return plaintext;
		} catch(Exception e) {
			log.error("Error while decrypting value {}", encrypted, e);
			return null;
		}
	}

	/**
	 * The generates the secret key used for encryption/decryption.
	 * @param strKey The key used for encryption/decryption, we don't do many rounds do you must have a very long key,
	 *               ideally the size of the keyspace. As this is just read from a configuration file and users don't
	 *               have to remember it that is practical.
	 */
	private SecretKey getSecret(String strKey, byte[] salt, int keyLength) throws GeneralSecurityException {
		KeySpec spec = new PBEKeySpec(strKey.toCharArray(), salt, KEY_ROUNDS, keyLength);
		SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEYFACTORY);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), SECRET_KEYSPEC);
		return secret;
	}

	/**
	 * This gets the key length to use. If it's available use 256, otherwise fallback to 128.
	 * This is set in the init method to improve performance.
	 * @return The key length to use, 128 or 256.
	 */
	protected int getKeyLength() {
		return keyLength;
	}

	protected byte[] getSalt() {
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return salt;
	}
}
