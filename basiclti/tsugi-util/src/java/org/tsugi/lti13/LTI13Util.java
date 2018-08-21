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

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.tsugi.basiclti.BasicLTIUtil;
import org.tsugi.basiclti.BasicLTIConstants;
import org.tsugi.lti2.objects.StandardServices;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

@Slf4j
public class LTI13Util {

	private static final String EMPTY_JSON_OBJECT = "{\n}\n";

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

}
