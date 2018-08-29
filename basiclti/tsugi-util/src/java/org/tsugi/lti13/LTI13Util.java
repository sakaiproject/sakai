/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2013- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.tsugi.lti13;

import java.util.Map;
import java.util.TreeMap;
import java.util.Base64;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LTI13Util {

	public static Map<String, String> generateKeys()
			throws java.security.NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);

		KeyPair kp = keyGen.genKeyPair();
		byte[] publicKey = kp.getPublic().getEncoded();
		byte[] privateKey = kp.getPrivate().getEncoded();
		Base64.Encoder encoder = Base64.getEncoder();

		String publicRSA = "-----BEGIN PUBLIC KEY-----\n"
				+ encoder.encodeToString(privateKey)
				+ "\n-----END PUBLIC KEY-----\n";
		String privateRSA = "-----BEGIN PRIVATE KEY-----\n"
				+ encoder.encodeToString(privateKey)
				+ "\n-----END PRIVATE KEY-----\n";

		// If we need a pem style for these keys
		// String pemBase64 = javax.xml.bind.DatatypeConverter.printBase64Binary(publicKey);
		Map<String, String> returnMap = new TreeMap<>();
		returnMap.put("platform_public", publicRSA);
		returnMap.put("platform_private", privateRSA);

		// Do it again for the tool
		keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(2048);

		kp = keyGen.genKeyPair();
		publicKey = kp.getPublic().getEncoded();
		privateKey = kp.getPrivate().getEncoded();

		publicRSA = "-----BEGIN RSA PUBLIC KEY-----\n"
				+ encoder.encodeToString(privateKey)
				+ "\n-----END RSA PUBLIC KEY-----\n";
		privateRSA = "-----BEGIN RSA PRIVATE KEY-----\n"
				+ encoder.encodeToString(privateKey)
				+ "\n-----END RSA PRIVATE KEY-----\n";

		returnMap.put("tool_public", publicRSA);
		returnMap.put("tool_private", privateRSA);

		return returnMap;

	}

	public static String stripPKCS8(String input) {
		if (input == null) {
			return input;
		}
		if (!input.startsWith("-----BEGIN")) {
			return input;
		}
		String[] lines = input.split("\n");
		String retval = "";
		for (String line : lines) {
			if (line.startsWith("----")) {
				continue;
			}
			retval = retval + line.trim();
		}
		return retval;
	}

	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			KeyPair kp = keyGen.genKeyPair();
			return kp;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getPublicEncoded(KeyPair kp) {
		return getPublicEncoded(kp.getPublic());
	}

	public static String getPublicEncoded(Key key) {
		byte[] encodeArray = key.getEncoded();
		Base64.Encoder encoder = Base64.getEncoder();

		String publicRSA = "-----BEGIN PUBLIC KEY-----\n"
				+ breakKeyIntoLines(encoder.encodeToString(encodeArray))
				+ "\n-----END PUBLIC KEY-----\n";
		return publicRSA;
	}

	public static String getPrivateEncoded(KeyPair kp) {
		return getPrivateEncoded(kp.getPrivate());
	}

	public static String getPrivateEncoded(Key key) {
		byte[] encodeArray = key.getEncoded();
		Base64.Encoder encoder = Base64.getEncoder();

		String privateRSA = "-----BEGIN PRIVATE KEY-----\n"
				+ breakKeyIntoLines(encoder.encodeToString(encodeArray))
				+ "\n-----END PRIVATE KEY-----\n";
		return privateRSA;
	}

	public static String breakKeyIntoLines(String rawkey) {
		int len = 65;
		StringBuilder ret = new StringBuilder();

		String trimmed = rawkey.trim();
		for (int i = 0; i < trimmed.length(); i += len) {
			int end = i + len;
			if (ret.length() > 0) {
				ret.append("\n");
			}
			if (end > trimmed.length()) {
				end = trimmed.length();
			}
			ret.append(trimmed.substring(i, end));
		}
		return ret.toString();
	}

	public static Key string2PrivateKey(String keyString) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");

			keyString = stripPKCS8(keyString);
			PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString.getBytes()));
			return (Key) kf.generatePrivate(keySpecPKCS8);
		} catch (IllegalArgumentException | InvalidKeySpecException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static Key string2PublicKey(String keyString) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA");

			keyString = stripPKCS8(keyString);
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(keyString));
			return (Key) kf.generatePublic(keySpecX509);
		} catch (IllegalArgumentException | InvalidKeySpecException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static String sha256(String input) {

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			// return Base64.getEncoder().encodeToString((md.digest(convertme));
			// md.update(input.getBytes());
			// byte[] output = Base64.encode(md.digest());
			String hash = Base64.getEncoder().encodeToString(md.digest(input.getBytes()));
			return hash;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
