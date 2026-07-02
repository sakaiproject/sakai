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

import java.util.Locale;
import java.util.Set;

import org.junit.Test;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.tsugi.lti13.LTI13ConstantsUtil;

public class LTI13ServletTest {

	@Test
	public void validateRequestedScopesAcceptsSupportedScopes() {
		String originalScope = LTI13ConstantsUtil.SCOPE_LINEITEM + " "
				+ LTI13ConstantsUtil.SCOPE_RESULT_READONLY;
		Set<String> scopes = SakaiAccessToken.parseScopes(originalScope);

		assertNull(LTI13Servlet.validateRequestedScopes(scopes, originalScope));
	}

	@Test
	public void validateRequestedScopesRejectsWhitespaceOnlyScopeRequest() {
		String originalScope = "   ";
		Set<String> scopes = SakaiAccessToken.parseScopes(originalScope);

		assertEquals(originalScope, LTI13Servlet.validateRequestedScopes(scopes, originalScope));
	}

	@Test
	public void validateRequestedScopesRejectsUnsupportedScopeTokens() {
		String unsupportedScope = LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY + "x";
		String originalScope = LTI13ConstantsUtil.SCOPE_LINEITEM + " " + unsupportedScope;
		Set<String> scopes = SakaiAccessToken.parseScopes(originalScope);

		assertEquals(unsupportedScope, LTI13Servlet.validateRequestedScopes(scopes, originalScope));
	}

	@Test
	public void validateRequestedScopesRejectsNonCanonicalScopeCase() {
		String originalScope = LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY.toUpperCase(Locale.ROOT);
		Set<String> scopes = SakaiAccessToken.parseScopes(originalScope);

		assertEquals(originalScope, LTI13Servlet.validateRequestedScopes(scopes, originalScope));
	}
}
