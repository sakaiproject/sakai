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
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LTI13Util {

	public static Map<String, String> generateKeys()
		throws java.security.NoSuchAlgorithmException
	{
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);

                KeyPair kp = keyGen.genKeyPair();
                byte[] publicKey = kp.getPublic().getEncoded();
                byte[] privateKey = kp.getPrivate().getEncoded();
                Base64.Encoder encoder = Base64.getEncoder();

                String publicRSA = "-----BEGIN RSA PUBLIC KEY-----\n" +
                        encoder.encodeToString(privateKey) +
                        "\n-----END RSA PUBLIC KEY-----\n";
                String privateRSA = "-----BEGIN RSA PRIVATE KEY-----\n" +
                        encoder.encodeToString(privateKey) +
                        "\n-----END RSA PRIVATE KEY-----\n";

		// If we need a pem style for these keys
                // String pemBase64 = javax.xml.bind.DatatypeConverter.printBase64Binary(publicKey);

                Map<String, String> returnMap = new TreeMap<String, String>();
                returnMap.put("platform_public", publicRSA);
                returnMap.put("platform_private", privateRSA);

		// Do it again for the tool
                keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);

                kp = keyGen.genKeyPair();
                publicKey = kp.getPublic().getEncoded();
                privateKey = kp.getPrivate().getEncoded();

                publicRSA = "-----BEGIN RSA PUBLIC KEY-----\n" +
                        encoder.encodeToString(privateKey) +
                        "\n-----END RSA PUBLIC KEY-----\n";
                privateRSA = "-----BEGIN RSA PRIVATE KEY-----\n" +
                        encoder.encodeToString(privateKey) +
                        "\n-----END RSA PRIVATE KEY-----\n";

                returnMap.put("tool_public", publicRSA);
                returnMap.put("tool_private", privateRSA);

		return returnMap;

	}

	public static String stripPKCS8(String input)
	{
		if ( input == null ) return input;
		if ( ! input.startsWith("-----BEGIN") ) return input;
		String[] lines = input.split("\n");
		String retval = "";
		for (String line : lines) {
			if ( line.startsWith("----") ) continue;
			retval = retval + line.trim();
		}
		return retval;
	}

	public static KeyPair generateKeyPair()
	{
		try {
	                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);
			KeyPair kp = keyGen.genKeyPair();
			return kp;
		} catch(Exception e) {
			return null;
		}
	}

	public static String getPublicEncoded(KeyPair kp)
        {
                byte[] encodeArray = kp.getPublic().getEncoded();
                Base64.Encoder encoder = Base64.getEncoder();

                String publicRSA = "-----BEGIN RSA PUBLIC KEY-----\n" +
                        encoder.encodeToString(encodeArray) +
                        "\n-----END RSA PUBLIC KEY-----\n";
		return publicRSA;
	}

	public static String getPrivateEncoded(KeyPair kp)
        {
                byte[] encodeArray = kp.getPrivate().getEncoded();
                Base64.Encoder encoder = Base64.getEncoder();

                String privateRSA = "-----BEGIN RSA PRIVATE KEY-----\n" +
                        encoder.encodeToString(encodeArray) +
                        "\n-----END RSA PRIVATE KEY-----\n";
		return privateRSA;
	}

	public static Key string2PrivateKey(String keyString)
		throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeyFactory kf = KeyFactory.getInstance("RSA");

		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyString.getBytes()));
		return (Key) kf.generatePrivate(keySpecPKCS8);
	}

	public static Key string2PublicKey(String keyString)
		throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		KeyFactory kf = KeyFactory.getInstance("RSA");

		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(keyString));
		return (Key) kf.generatePublic(keySpecX509);
	}

}
