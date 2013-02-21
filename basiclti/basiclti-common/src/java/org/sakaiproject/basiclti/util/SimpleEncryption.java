package org.sakaiproject.basiclti.util;

import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This is a simple class that does AES encryption of a string.
 * It should never be used for very much data as it's just aimed at encrypting
 * passwords stored in the database so you need the secret key to 
 * decrypt them. If just hex encodes the data which balloons it a little.
 * <p>
 * It doesn't pull in any external dependenies (commons-codec or http://www.jasypt.org/)
 * as Basic LTI tries to keep them to a minimum. 
 * 
 * @author buckett
 *
 */
public class SimpleEncryption {

	private static final String CIPHER = "AES/CBC/PKCS5Padding";
	
	public static String encrypt(String key, String source) {
		if (source == null) {
			return null;
		}

		byte[] salt = new byte[8];
		new Random().nextBytes(salt);
		char[] password = key.toCharArray();

		try {
			SecretKey secret = generateSecret(password, salt);
			/* Encrypt the message. */
			Cipher cipher = Cipher.getInstance(CIPHER);
			cipher.init(Cipher.ENCRYPT_MODE, secret);
			AlgorithmParameters params = cipher.getParameters();
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

			byte[] ciphertext = cipher.doFinal(source.getBytes("UTF-8"));
			
			// Pack the byte arrays into a string hex encoded. 
			StringBuffer out = new StringBuffer();
			out.append(ShaUtil.byteToHex(salt));
			out.append(":");
			out.append(ShaUtil.byteToHex(iv));
			out.append(":");
			out.append(ShaUtil.byteToHex(ciphertext));
			return out.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static String decrypt(String key, String encrypted) {
		if (encrypted == null) {
			return null;
		}
		
		char[] password = key.toCharArray();

		String parts[] = encrypted.split(":");
		if (parts.length != 3) {
			throw new RuntimeException("Corrupt encrypted source. Can't split source.");
		}
		byte[] salt = ShaUtil.hexToByte(parts[0]);
		byte[] iv = ShaUtil.hexToByte(parts[1]);
		byte[] ciphertext = ShaUtil.hexToByte(parts[2]);

		try {
			SecretKey secret = generateSecret(password, salt);

			Cipher cipher = Cipher.getInstance(CIPHER);
			cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
			String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
			return plaintext;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static SecretKey generateSecret(char[] password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		// 128 bit keys don't cause problems with the export restrictions in the JVM
		KeySpec spec = new PBEKeySpec(password, salt, 8, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
		return secret;
	}
}
