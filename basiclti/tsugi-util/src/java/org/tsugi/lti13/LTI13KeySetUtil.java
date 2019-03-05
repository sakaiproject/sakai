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

import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nimbusds.jose.JWSAlgorithm;

@Slf4j
public class LTI13KeySetUtil {

	public static String getPublicKID(Key key) {
		String publicEncoded = LTI13Util.getPublicEncoded(key);
		return new Integer(publicEncoded.hashCode()).toString();
	}

	public static String getKeySetJSON(Map<String, RSAPublicKey> keys)
			throws java.security.NoSuchAlgorithmException {
		JSONArray jar = new JSONArray();

		for (Map.Entry<String, RSAPublicKey> entry : keys.entrySet()) {
			RSAPublicKey key = (RSAPublicKey) entry.getValue();
			com.nimbusds.jose.jwk.RSAKey rsaKey = new com.nimbusds.jose.jwk.RSAKey.Builder(key).algorithm(JWSAlgorithm.RS256).build();
			String keyStr = rsaKey.toJSONString();

			JSONObject kobj = (JSONObject) JSONValue.parse(keyStr);
			kobj.put("kid", (String) entry.getKey());
			kobj.put("use", "sig");
			jar.add(kobj);
		}

		JSONObject keyset = new JSONObject();
		keyset.put("keys", jar);

		String keySetJSON = keyset.toString();
		return keySetJSON;
	}

	public static String getKeySetJSON(String kid, RSAPublicKey key)
			throws java.security.NoSuchAlgorithmException {
		Map<String, RSAPublicKey> keys = new TreeMap<>();
		keys.put(kid, key);
		return getKeySetJSON(keys);
	}
	
	public static String getKeySetJSON(RSAPublicKey key)
			throws java.security.NoSuchAlgorithmException {
		Map<String, RSAPublicKey> keys = new TreeMap<>();
		String kid = getPublicKID(key);
		keys.put(kid, key);
		return getKeySetJSON(keys);
	}

}
