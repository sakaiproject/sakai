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
package org.sakaiproject.lti.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.junit.Test;

import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.tsugi.lti13.LTI13Util;

/**
 * Tests for LTI API launch substitution helpers in {@link SakaiLTIUtil}.
 */
public class SakaiLTIUtilLtiApiSubstitutionTest {

    @Test
    public void formatLtiApiAvailableScopes_empty() {
        assertEquals("", SakaiLTIUtil.formatLtiApiAvailableScopes(null));
        assertEquals("", SakaiLTIUtil.formatLtiApiAvailableScopes(Collections.emptyList()));
    }

    @Test
    public void formatLtiApiAvailableScopes_mapsFunctionsToOAuthScopes() {
        String scopes = SakaiLTIUtil.formatLtiApiAvailableScopes(
                Arrays.asList("content.read", "gradebook.write"));
        assertEquals(
                SakaiAccessToken.functionToLtiApiScope("content.read") + " "
                        + SakaiAccessToken.functionToLtiApiScope("gradebook.write"),
                scopes);
    }

    @Test
    public void substituteCustom_resolvesLtiApiSubstitutionVariables() {
        Properties lti13subst = new Properties();
        lti13subst.setProperty(SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_DIRECT_URL, "https://lms.example.edu/direct");
        lti13subst.setProperty(SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_API_URL, "https://lms.example.edu/api");
        lti13subst.setProperty(SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_SCOPES_AVAILABLE,
                SakaiAccessToken.functionToLtiApiScope("content.read"));

        Properties custom = new Properties();
        custom.setProperty("direct_url", "$" + SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_DIRECT_URL);
        custom.setProperty("api_url", "$" + SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_API_URL);
        custom.setProperty("scopes", "$" + SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_SCOPES_AVAILABLE);

        LTI13Util.substituteCustom(custom, lti13subst);

        assertEquals("https://lms.example.edu/direct", custom.getProperty("direct_url"));
        assertEquals("https://lms.example.edu/api", custom.getProperty("api_url"));
        assertTrue(custom.getProperty("scopes").contains("sakai.lti.api.content.read"));
    }
}
