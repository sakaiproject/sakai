package org.sakaiproject.user.detail;

import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * This class provides encryption/decryption services. Service is thread safe.
 */
public class ValueEncryptionUtilities {
	
	private final static String CIPHER_INSTANCE = "AES/CBC/PKCS5Padding";
	private final static String SECRET_KEYFACTORY = "PBKDF2WithHmacSHA256";
	private final static String SECRET_KEYSPEC = "AES";

	private static Log M_log = LogFactory.getLog(ValueEncryptionUtilities.class);

	private static SecureRandom random = new SecureRandom();
	private static Base64.Encoder encoder = Base64.getEncoder();
	private static Base64.Decoder decoder = Base64.getDecoder();

	// The encryption key.
	private static String key;
	private static String getKey() {
		if(key == null) {
			key = ServerConfigurationService.getString("ValueEncryptionUtilities.key", "aaeeiioouu12345#");
		}
		return key;
	}


	/**
	 * This salts and encrypts a value and returns a base64 encoded version of the encrypted value.
	 * @param value The value to be encrypted.
	 * @return A salted base64 encrypted version of the value.
	 * @throws RuntimeException If encryption fails for any reason.
	 */
	public static String encrypt(String value) {
		try {

			byte[] salt = new byte[16];
			//generate a new random SALT
			random.nextBytes(salt);
			//new secret with given key and salt
			SecretKey secret = getSecret(getKey(), salt);
			Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
			cipher.init(Cipher.ENCRYPT_MODE, secret);
			AlgorithmParameters params = cipher.getParameters();
			//get IV from cipher parameters
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
			byte[] ciphertext = cipher.doFinal(value.getBytes());
			//create final array (in bytes) : IV + SALT + TEXT
			byte[] finalCiphertext = new byte[ciphertext.length+2*16];
			System.arraycopy(iv, 0, finalCiphertext, 0, 16);
			System.arraycopy(salt, 0, finalCiphertext, 16, 16);
			System.arraycopy(ciphertext, 0, finalCiphertext, 32, ciphertext.length);
			//encode all bytes in a Base64 string
			return encoder.encodeToString(finalCiphertext);
		} catch(Exception e){
			M_log.error("Error while encrypting value " + value + " : " + e);
			return null;
		}
	}

	/**
	 * This extracts the salt and decrypts a value.
	 * @param encrypted The salted and encrypted value which is base64 encoded.
	 * @return The plain value;
	 * @throws RuntimeException If decryption fails for any reason.
	 */
	public static String decrypt(String encrypted) {
		try {
			//decode the whole string -> result : IV + SALT + TEXT
			byte[] finalCipherBytes = decoder.decode(encrypted.getBytes("UTF-8"));
			//0 - 16 : IV
			byte[] ivBytes = new byte[16];
			System.arraycopy(finalCipherBytes, 0, ivBytes, 0, 16);
			//16 - 32 : SALT
			byte[] saltBytes = new byte[16];
			System.arraycopy(finalCipherBytes, 16, saltBytes, 0, 16);
			//32 - TOEND : TEXT
			byte[] ciphertext = new byte[finalCipherBytes.length-2*16];
			System.arraycopy(finalCipherBytes, 32, ciphertext, 0, ciphertext.length);
			//generate secret with same key and salt as in encryption process
			SecretKey secret = getSecret(getKey(), saltBytes);
			// Decrypt the message, given derived key and initialization vector.
			Cipher cipher = Cipher.getInstance(CIPHER_INSTANCE);
			cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));
			String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
			return plaintext;
		} catch(Exception e) {
			M_log.error("Error while decrypting value " + encrypted + " : " + e);
			return null;
		}
	}

	private static SecretKey getSecret(String strKey, byte[] salt) throws Exception{
		//Derive the key, given base key and salt.
		KeySpec spec = new PBEKeySpec(strKey.toCharArray(), salt, 65536, 256);
		SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEYFACTORY);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), SECRET_KEYSPEC);
		return secret;
	}

}
