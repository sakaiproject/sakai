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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;
import org.springframework.cache.Cache;
import org.tsugi.oauth2.objects.ClientAssertion;

import io.jsonwebtoken.Claims;

public class LTI13TokenRequestValidatorTest {

	private static final String CLIENT_ID = "client-123";
	private static final String TOKEN_AUDIENCE = "https://sakai.example/imsblis/lti13/token/42";

	@Test
	public void validateTokenRequestRequiresClientCredentialsGrant() {
		assertNull(LTI13TokenRequestValidator.validateTokenRequest(ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS,
				ClientAssertion.CLIENT_ASSERTION_TYPE_JWT));
		assertEquals("Invalid grant_type", LTI13TokenRequestValidator.validateTokenRequest("authorization_code",
				ClientAssertion.CLIENT_ASSERTION_TYPE_JWT));
		assertEquals("Invalid client_assertion_type", LTI13TokenRequestValidator.validateTokenRequest(
				ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS, "jwt"));
	}

	@Test
	public void validateClientAssertionClaimsAcceptsRequiredClaims() {
		assertNull(LTI13TokenRequestValidator.validateClientAssertionClaims(validClaims("jti-1"), CLIENT_ID, TOKEN_AUDIENCE));
	}

	@Test
	public void validateClientAssertionClaimsRejectsWrongClientIdClaims() {
		Claims claims = validClaims("jti-1");
		claims.setSubject("other-client");

		assertEquals("Invalid subject", LTI13TokenRequestValidator.validateClientAssertionClaims(claims, CLIENT_ID, TOKEN_AUDIENCE));
	}

	@Test
	public void validateClientAssertionClaimsRejectsWrongAudience() {
		Claims claims = validClaims("jti-1");
		claims.setAudience("https://sakai.example/imsblis/lti13/token/99");

		assertEquals("Invalid audience", LTI13TokenRequestValidator.validateClientAssertionClaims(claims, CLIENT_ID, TOKEN_AUDIENCE));
	}

	@Test
	public void validateClientAssertionClaimsAcceptsAudienceLists() {
		Claims claims = validClaims("jti-1");
		claims.put(Claims.AUDIENCE, Arrays.asList("https://sakai.example/other", TOKEN_AUDIENCE));

		assertNull(LTI13TokenRequestValidator.validateClientAssertionClaims(claims, CLIENT_ID, TOKEN_AUDIENCE));
	}

	@Test
	public void validateClientAssertionReplayRejectsRepeatedJti() {
		MapCache cache = new MapCache();
		Claims claims = validClaims("jti-1");

		assertNull(LTI13TokenRequestValidator.validateClientAssertionReplay(cache, claims, CLIENT_ID));
		assertEquals("Replayed client_assertion", LTI13TokenRequestValidator.validateClientAssertionReplay(cache, claims, CLIENT_ID));
	}

	@Test
	public void validateClientAssertionReplayRejectsRepeatedJtiDuringClockSkewWindow() {
		MapCache cache = new MapCache();
		Claims claims = validClaims("jti-1");
		claims.setExpiration(new Date(System.currentTimeMillis() - 1_000L));

		assertNull(LTI13TokenRequestValidator.validateClientAssertionReplay(cache, claims, CLIENT_ID));
		assertEquals("Replayed client_assertion", LTI13TokenRequestValidator.validateClientAssertionReplay(cache, claims, CLIENT_ID));
	}

	private Claims validClaims(String jti) {
		long now = System.currentTimeMillis();
		return new TestClaims()
				.setIssuer(CLIENT_ID)
				.setSubject(CLIENT_ID)
				.setAudience(TOKEN_AUDIENCE)
				.setIssuedAt(new Date(now - 1_000L))
				.setExpiration(new Date(now + 60_000L))
				.setId(jti);
	}

	private static class TestClaims extends HashMap<String, Object> implements Claims {

		@Override
		public String getIssuer() {
			return get(ISSUER, String.class);
		}

		@Override
		public Claims setIssuer(String iss) {
			put(ISSUER, iss);
			return this;
		}

		@Override
		public String getSubject() {
			return get(SUBJECT, String.class);
		}

		@Override
		public Claims setSubject(String sub) {
			put(SUBJECT, sub);
			return this;
		}

		@Override
		public String getAudience() {
			return get(AUDIENCE, String.class);
		}

		@Override
		public Claims setAudience(String aud) {
			put(AUDIENCE, aud);
			return this;
		}

		@Override
		public Date getExpiration() {
			return get(EXPIRATION, Date.class);
		}

		@Override
		public Claims setExpiration(Date exp) {
			put(EXPIRATION, exp);
			return this;
		}

		@Override
		public Date getNotBefore() {
			return get(NOT_BEFORE, Date.class);
		}

		@Override
		public Claims setNotBefore(Date nbf) {
			put(NOT_BEFORE, nbf);
			return this;
		}

		@Override
		public Date getIssuedAt() {
			return get(ISSUED_AT, Date.class);
		}

		@Override
		public Claims setIssuedAt(Date iat) {
			put(ISSUED_AT, iat);
			return this;
		}

		@Override
		public String getId() {
			return get(ID, String.class);
		}

		@Override
		public Claims setId(String jti) {
			put(ID, jti);
			return this;
		}

		@Override
		public <T> T get(String claimName, Class<T> requiredType) {
			Object value = get(claimName);
			return value == null ? null : requiredType.cast(value);
		}
	}

	private static class MapCache implements Cache {

		private final Map<Object, Object> values = new HashMap<>();

		@Override
		public String getName() {
			return "test";
		}

		@Override
		public Object getNativeCache() {
			return values;
		}

		@Override
		public ValueWrapper get(Object key) {
			if (!values.containsKey(key)) {
				return null;
			}
			Object value = values.get(key);
			return () -> value;
		}

		@Override
		public <T> T get(Object key, Class<T> type) {
			Object value = values.get(key);
			return value == null ? null : type.cast(value);
		}

		@Override
		public <T> T get(Object key, Callable<T> valueLoader) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void put(Object key, Object value) {
			values.put(key, value);
		}

		@Override
		public ValueWrapper putIfAbsent(Object key, Object value) {
			Object existing = values.putIfAbsent(key, value);
			return existing == null ? null : () -> existing;
		}

		@Override
		public void evict(Object key) {
			values.remove(key);
		}

		@Override
		public void clear() {
			values.clear();
		}
	}
}
