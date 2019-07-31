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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;
import java.util.Base64;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.lti13.objects.LTI11Transition;

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
		if ( keyString == null ) return null;
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
		if ( keyString == null ) return null;
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

	// https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	/*
		sign=base64(hmac_sha256(utf8bytes('179248902&689302&https://lmsvendor.com&PM48OJSfGDTAzAo&1551290856&172we8671fd8z'), utf8bytes('my-lti11-secret')))

		{
			"nonce": "172we8671fd8z",
			"iat": 1551290796,
			"exp": 1551290856,
			"iss": "https://lmsvendor.com",
			"aud": "PM48OJSfGDTAzAo",
			"sub": "3",
			"https://purl.imsglobal.org/spec/lti/claim/deployment_id": "689302",
			"https://purl.imsglobal.org/spec/lti/claim/lti1p1": {
				"user_id": "34212",
				"oauth_consumer_key": "179248902",
				"oauth_consumer_key_sign": "lWd54kFo5qU7xshAna6v8BwoBm6tmUjc6GTax6+12ps="
			}
		}

	 */

	/**
	 * Compute the base string for a Launch JWT
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * @param lj The Launch JSON Web Token with the LTI 1.1 transition data
	 *
	 * @return string This is null if the base string cannot be computed
	 */
	public static String getLTI11TransitionBase(LaunchJWT lj) {

		String nonce =  lj.nonce;
		Long expires = lj.expires;
		String issuer = lj.issuer;
		String client_id = lj.audience;
		String subject = lj.subject;
		String deployment_id = lj.deployment_id;
		if ( nonce == null || issuer == null || expires == null ||
				client_id == null || subject == null || deployment_id == null) return null;

		if ( lj.lti11_transition == null ) return null;
		LTI11Transition lti11_transition = lj.lti11_transition;
		String user_id = lti11_transition.user_id;
		String oauth_consumer_key = lti11_transition.oauth_consumer_key;
		if ( user_id == null || oauth_consumer_key == null ) return null;

		String base = oauth_consumer_key + "&" + deployment_id + "&" + issuer + "&" +
			client_id + "&" + expires + "&" + nonce;

		return base;
	}

	/**
	 * Compute the OAuth signature for an LTI 1.3 Launch JWT
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * @param lj The Launch JSON Web Token with the LTI 11 transition data
	 * @param secret The OAuth secret
	 *
	 * @return string This is null if the signature cannot be computed
	 */
	public static String signLTI11Transition(LaunchJWT lj, String secret) {

		if ( secret == null ) return null;

		String base = getLTI11TransitionBase(lj);
		if ( base == null ) return null;

		String signature = compute_HMAC_SHA256(base, secret);
		return signature;
	}

	/**
	 * Check the OAuth signature for an LTI 1.3 Launch JWT
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * @param lj The Launch JSON Web Token with the LTI 11 transition data
	 * @param key The OAuth key
	 * @param secret The OAuth secret
	 *
	 * @return true if the signature matches, false if the JWT
	 * the signature does not match or the JWT data is malformed.
	 */
	public static boolean checkLTI11Transition(LaunchJWT lj, String key, String secret) {

		if ( key == null ) return false;
		if ( secret == null ) return false;

		LTI11Transition lti11_transition = lj.lti11_transition;
		if ( lti11_transition == null ) return false;
		String oauth_consumer_key_sign = lti11_transition.oauth_consumer_key_sign;
		if ( oauth_consumer_key_sign == null ) return false;
		String oauth_consumer_key = lti11_transition.oauth_consumer_key;
		if ( oauth_consumer_key == null ) return false;

		if ( !oauth_consumer_key.equals(key) ) return false;

		String base = getLTI11TransitionBase(lj);
		if ( base == null ) return false;

		String signature = compute_HMAC_SHA256(base, secret);
		return oauth_consumer_key_sign.equals(signature);
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

	/**
	 * Compute the HMAC256 of a string (part of LTI 1.1 Transition)
	 *
	 * See: https://www.imsglobal.org/spec/lti/v1p3/migr#lti-1-1-migration-claim
	 *
	 * Based on:
	 * https://www.jokecamp.com/blog/examples-of-creating-base64-hashes-using-hmac-sha256-in-different-languages/#php
	 * https://stackoverflow.com/questions/7124735/hmac-sha256-algorithm-for-signature-calculation
	 *
	 * @param object $message The message to sign
	 * @param string $secret The secret used to sign the message
	 *
	 * @return string The signed message
	 */
	public static String compute_HMAC_SHA256(String message, String secret)
	{

		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);

			String hash = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(message.getBytes()));
			return hash;
		}
		catch (Exception e){
			return null;
		}
	}

	/*
		HTTP/1.1 400 OK
		Content-Type: application/json;charset=UTF-8
		Cache-Control: no-store
		Pragma: no-cache

		{
			"error" : "invalid_scope"
		}
	*/
	public static void return400(HttpServletResponse response, String error, String detail) {
			response.setContentType("application/json;charset=UTF-8");
			response.setHeader("Cache-Control", "no-store");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			if ( detail != null ) response.setHeader("X-Tsugi-LTI13-Error-Detail", detail);
			JSONObject job = new JSONObject();
			job.put("error", error);
			String retval = org.tsugi.jackson.JacksonUtil.toString(job);
			try {
				PrintWriter out = response.getWriter();
				out.println(retval);
			} catch (IOException e) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.error(e.getMessage(), e);
		}
	}

	public static void return400(HttpServletResponse response, String error) {
		return400(response, error, null);
	}

}
