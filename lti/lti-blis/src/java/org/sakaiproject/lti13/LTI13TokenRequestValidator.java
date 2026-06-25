/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.lti13;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.tsugi.oauth2.objects.ClientAssertion;

import io.jsonwebtoken.Claims;

final class LTI13TokenRequestValidator {

	static final long CLIENT_ASSERTION_CLOCK_SKEW_MILLISECONDS = 60_000L;

	private static final String CACHE_CLIENT_ASSERTION_JTI = "client_assertion_jti::";

	private LTI13TokenRequestValidator() {
	}

	static String validateTokenRequest(String grantType, String clientAssertionType) {
		if (!ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS.equals(grantType)) {
			return "Invalid grant_type";
		}
		if (!ClientAssertion.CLIENT_ASSERTION_TYPE_JWT.equals(clientAssertionType)) {
			return "Invalid client_assertion_type";
		}
		return null;
	}

	static String validateClientAssertionClaims(Claims claims, String clientId, String tokenAudience) {
		if (claims == null) {
			return "Missing client_assertion claims";
		}
		if (StringUtils.isBlank(clientId)) {
			return "Tool is missing client_id";
		}
		if (!clientId.equals(claims.getIssuer())) {
			return "Invalid issuer";
		}
		if (!clientId.equals(claims.getSubject())) {
			return "Invalid subject";
		}
		if (!hasAudience(claims, tokenAudience)) {
			return "Invalid audience";
		}
		if (claims.getIssuedAt() == null) {
			return "Missing iat";
		}
		if (claims.getExpiration() == null) {
			return "Missing exp";
		}
		if (StringUtils.isBlank(claims.getId())) {
			return "Missing jti";
		}

		long now = System.currentTimeMillis();
		if (claims.getIssuedAt().getTime() > now + CLIENT_ASSERTION_CLOCK_SKEW_MILLISECONDS) {
			return "Invalid iat";
		}
		if (claims.getExpiration().getTime() <= now - CLIENT_ASSERTION_CLOCK_SKEW_MILLISECONDS) {
			return "Invalid exp";
		}
		if (claims.getIssuedAt().after(claims.getExpiration())) {
			return "Invalid iat";
		}
		return null;
	}

	static boolean hasAudience(Claims claims, String tokenAudience) {
		if (claims == null || StringUtils.isBlank(tokenAudience)) {
			return false;
		}

		Object audience = claims.get(Claims.AUDIENCE);
		if (audience instanceof String) {
			return tokenAudience.equals(audience);
		}
		if (audience instanceof Collection<?>) {
			for (Object entry : (Collection<?>) audience) {
				if (tokenAudience.equals(entry)) {
					return true;
				}
			}
		}
		return false;
	}

	static String validateClientAssertionReplay(Cache cache, Claims claims, String clientId) {
		if (cache == null) {
			return "Replay cache unavailable";
		}
		if (claims == null || StringUtils.isBlank(claims.getId()) || StringUtils.isBlank(clientId) || claims.getExpiration() == null) {
			return "Missing jti";
		}

		String cacheKey = CACHE_CLIENT_ASSERTION_JTI + clientId + "::" + claims.getId();
		Long expires = Long.valueOf(claims.getExpiration().getTime() + CLIENT_ASSERTION_CLOCK_SKEW_MILLISECONDS);
		long now = System.currentTimeMillis();
		Cache.ValueWrapper existing = cache.putIfAbsent(cacheKey, expires);
		if (existing != null) {
			Object existingExpires = existing.get();
			if (!(existingExpires instanceof Long) || ((Long) existingExpires).longValue() > now) {
				return "Replayed client_assertion";
			}
			cache.put(cacheKey, expires);
		}
		return null;
	}
}
