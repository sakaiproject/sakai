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

public class LTI13JwtUtil {

	/**
	 *
	 */
	public static String getBodyAsString(String jws, Key key) {
		return Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().toString();
	}

	/**
	 *
	 */
	public static String getHeaderAsString(String jws, Key key) {
		return Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getHeader().toString();
	}

	public static String rawJwtHeader(String encoded) {
		String[] parts = encoded.split("\\.");
		if (parts.length != 2 && parts.length != 3) {
			return null;
		}
		byte[] bytes = Base64.getDecoder().decode(parts[0]);
		return new String(bytes);
	}

	public static String rawJwtBody(String encoded) {
		String[] parts = encoded.split("\\.");
		if (parts.length != 2 && parts.length != 3) {
			return null;
		}
		byte[] bytes = Base64.getDecoder().decode(parts[1]);
		return new String(bytes);
	}

}
