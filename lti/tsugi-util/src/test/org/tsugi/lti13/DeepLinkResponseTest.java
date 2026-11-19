/*
 * Copyright (c) 2026 Apereo Foundation
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.Key;

import org.junit.Test;

import org.tsugi.jackson.JacksonUtil;
import org.tsugi.lti13.objects.DeepLink;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class DeepLinkResponseTest {

	@Test
	public void acceptsResponseWithoutOptionalDataClaim() {
		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
		String idToken = Jwts.builder()
				.claim(LTI13ConstantsUtil.MESSAGE_TYPE,
						LTI13ConstantsUtil.MESSAGE_TYPE_LTI_DEEP_LINKING_RESPONSE)
				.signWith(key)
				.compact();

		assertNotNull(new DeepLinkResponse(idToken));
	}

	@Test
	public void omitsUnsetDataFromRequestSettings() {
		String deepLinkSettings = JacksonUtil.toString(new DeepLink());

		assertFalse(deepLinkSettings.contains("\"data\""));
	}

	@Test
	public void preservesExplicitDataInRequestSettings() {
		DeepLink deepLink = new DeepLink();
		deepLink.data = "{\"vendor_tenant_id\":\"school.example.edu\"}";

		String deepLinkSettings = JacksonUtil.toString(deepLink);

		assertTrue(deepLinkSettings.contains("\"data\""));
		assertTrue(deepLinkSettings.contains("vendor_tenant_id"));
		assertTrue(deepLinkSettings.contains("school.example.edu"));
	}
}
