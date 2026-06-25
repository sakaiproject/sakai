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
package org.sakaiproject.lti13.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SakaiAccessTokenTest {

	@Test
	public void hasScopeRequiresExactScopeTokenMatch() {
		SakaiAccessToken token = new SakaiAccessToken();
		token.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);

		assertTrue(token.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY));
		assertFalse(token.hasScope(SakaiAccessToken.SCOPE_LINEITEMS));
	}

	@Test
	public void addScopeKeepsPrefixRelatedScopesDistinct() {
		SakaiAccessToken token = new SakaiAccessToken();
		token.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
		token.addScope(SakaiAccessToken.SCOPE_LINEITEMS);
		token.addScope(SakaiAccessToken.SCOPE_LINEITEMS);

		assertEquals(SakaiAccessToken.SCOPE_LINEITEMS_READONLY + " " + SakaiAccessToken.SCOPE_LINEITEMS, token.scope);
		assertTrue(token.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY));
		assertTrue(token.hasScope(SakaiAccessToken.SCOPE_LINEITEMS));
	}
}
