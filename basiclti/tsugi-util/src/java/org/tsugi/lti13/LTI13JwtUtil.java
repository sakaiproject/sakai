/*
 * Copyright (c) 2018- Charles R. Severance
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
 * 
 */
package org.tsugi.lti13;

import java.security.Key;
import java.util.Base64;

import io.jsonwebtoken.Jwts;
import javax.servlet.http.HttpServletRequest;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class LTI13JwtUtil {

	public static final String ID_TOKEN = "id_token";
	public static final String JWT = "JWT";

	// Allowed clock Skew
	// https://github.com/jwtk/jjwt/blob/master/README.md
	public static Long leeway = 60L;

	/**
	 *
	 */
	public static String getBodyAsString(String jws, Key key) {
		return Jwts.parser().setAllowedClockSkewSeconds(leeway).setSigningKey(key).parseClaimsJws(jws).getBody().toString();
	}

	/**
	 *
	 */
	public static String getHeaderAsString(String jws, Key key) {
		return Jwts.parser().setAllowedClockSkewSeconds(leeway).setSigningKey(key).parseClaimsJws(jws).getHeader().toString();
	}

	public static String rawJwtHeader(String encoded) {
		String[] parts = encoded.split("\\.");
		if (parts.length != 2 && parts.length != 3) {
			return null;
		}
		byte[] bytes = Base64.getUrlDecoder().decode(parts[0]);
		return new String(bytes);
	}

	public static JSONObject jsonJwtHeader(String id_token) {
		String headerStr = rawJwtHeader(id_token);

		if ( headerStr == null ) return null;
		Object headerObj = JSONValue.parse(headerStr);
		if ( headerObj == null ) return null;
		if ( ! ( headerObj instanceof JSONObject) ) return null;
		return (JSONObject) headerObj;
	}

	public static String rawJwtBody(String encoded) {
		String[] parts = encoded.split("\\.");
		if (parts.length != 2 && parts.length != 3) {
			return null;
		}
		byte[] bytes = Base64.getUrlDecoder().decode(parts[1]);
		return new String(bytes);
	}

	public static JSONObject jsonJwtBody(String id_token) {
		String bodyStr = rawJwtBody(id_token);
		if ( bodyStr == null ) return null;
		Object bodyObj = JSONValue.parse(bodyStr);
		if ( bodyObj == null ) return null;
		if ( ! ( bodyObj instanceof JSONObject) ) return null;
		return (JSONObject) bodyObj;
	}

	/**
	 * Check if the incoming request is LTI 1.3
	 */
	public static boolean isRequest(HttpServletRequest req)
	{
		String id_token = req.getParameter(ID_TOKEN);
		return isRequest(id_token);
	}

	/**
	 * Check if the incoming id_token (may be null) is LTI 1.3
	 */
	public static boolean isRequest(String id_token)
	{
		if ( id_token == null ) return false;

		JSONObject header = jsonJwtHeader(id_token);
		if ( header == null ) return false;

		JSONObject body = jsonJwtBody(id_token);
		if ( body == null ) return false;
		if ( body.get("exp") == null ) return false;  // could check more..

		return true;
	}
}
