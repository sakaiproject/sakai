/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti13.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SakaiAccessTokenTest {

    @Test
    public void addScopeAccumulates() {
        SakaiAccessToken sat = new SakaiAccessToken();
        sat.addScope(SakaiAccessToken.SCOPE_SCORE);
        sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
        assertTrue(sat.hasScope(SakaiAccessToken.SCOPE_SCORE));
        assertTrue(sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY));
    }

    @Test
    public void addScopeDoesNotDuplicate() {
        SakaiAccessToken sat = new SakaiAccessToken();
        sat.addScope(SakaiAccessToken.SCOPE_SCORE);
        sat.addScope(SakaiAccessToken.SCOPE_SCORE);
        assertTrue(sat.scope.equals(SakaiAccessToken.SCOPE_SCORE));
    }

    @Test
    public void hasScopeFalseWhenEmpty() {
        SakaiAccessToken sat = new SakaiAccessToken();
        assertFalse(sat.hasScope(SakaiAccessToken.SCOPE_SCORE));
    }
}
