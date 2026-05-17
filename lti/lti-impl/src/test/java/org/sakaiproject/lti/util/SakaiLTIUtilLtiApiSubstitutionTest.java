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
import static org.mockito.Mockito.when;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.impl.LtiTestConfiguration;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.tsugi.lti13.LTI13Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * Tests for LTI API launch substitution helpers in {@link SakaiLTIUtil}.
 */
@ContextConfiguration(classes = { LtiTestConfiguration.class })
public class SakaiLTIUtilLtiApiSubstitutionTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private LTIService ltiService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Test
    public void formatLtiApiAvailableScopes_empty() {
        assertEquals("", SakaiLTIUtil.formatLtiApiAvailableScopes(null));
        assertEquals("", SakaiLTIUtil.formatLtiApiAvailableScopes(Collections.emptyList()));
    }

    @Test
    public void formatLtiApiAvailableScopes_mapsPermissionsToOAuthScopes() {
        String scopes = SakaiLTIUtil.formatLtiApiAvailableScopes(
                Arrays.asList("content.read", "gradebook.write"));
        assertEquals(
                SakaiAccessToken.permissionToLtiApiScope("content.read") + " "
                        + SakaiAccessToken.permissionToLtiApiScope("gradebook.write"),
                scopes);
    }

    @Test
    public void addLtiApiLaunchSubstitutions_emptyScopesPresentsKey() {
        when(serverConfigurationService.getBoolean(LTIService.PROPERTY_WEBAPI_ENABLED, LTIService.PROPERTY_WEBAPI_ENABLED_DEFAULT))
          .thenReturn(true);

        Properties lti13subst = new Properties();
        SakaiLTIUtil.addLtiApiLaunchSubstitutions(lti13subst, null, ltiService);
        assertEquals("", lti13subst.getProperty(SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_SCOPES_AVAILABLE));
    }

    @Test
    public void substituteCustom_resolvesEmptyScopesAvailable() {
        Properties lti13subst = new Properties();
        lti13subst.setProperty(SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_SCOPES_AVAILABLE, "");
        Properties custom = new Properties();
        custom.setProperty("scopes", "$" + SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_SCOPES_AVAILABLE);
        LTI13Util.substituteCustom(custom, lti13subst);
        assertEquals("", custom.getProperty("scopes"));
    }

    @Test
    public void substituteCustom_resolvesLtiApiSubstitutionVariables() {
        Properties lti13subst = new Properties();
        lti13subst.setProperty(SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_SCOPES_AVAILABLE,
                SakaiAccessToken.permissionToLtiApiScope("content.read"));

        Properties custom = new Properties();
        custom.setProperty("scopes", "$" + SakaiLTIUtil.SAKAI_LTI_SUBSTITUTION_SCOPES_AVAILABLE);

        LTI13Util.substituteCustom(custom, lti13subst);

        assertTrue(custom.getProperty("scopes").contains("sakai.lti.api.content.read"));
    }
}
