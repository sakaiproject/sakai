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
package org.sakaiproject.lti.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import static org.sakaiproject.lti.util.SakaiLTIUtil.getOurServerUrl;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI13_PATH;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.lti.api.model.LtiTool;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.lti13.util.SakaiAccessToken;
import org.sakaiproject.site.api.SiteService;
import org.tsugi.lti13.LTI13AccessTokenUtil;
import org.tsugi.lti13.LTI13ConstantsUtil;
import org.tsugi.lti13.LTI13KeySetUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.oauth2.objects.ClientAssertion;

import com.sun.net.httpserver.HttpServer;

/**
 * Unit tests for {@link SakaiAccessTokenServiceImpl}.
 */
@ContextConfiguration(classes = { LtiTestConfiguration.class })
public class SakaiAccessTokenServiceImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final long TOOL_ID = 42L;

    @Autowired
    private FunctionManager functionManager;

    @Autowired
    private LTIService ltiService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    //private SakaiAccessTokenServiceImpl service;
    private SakaiAccessTokenService service;

    @Autowired
    private SiteService siteService;

    @Autowired
    private CacheManager cacheManager;

    private KeyPair toolKeyPair;
    private HttpServer keysetServer;
    private String keysetUrl;

    @Before
    public void setUp() throws Exception {

        when(serverConfigurationService.getString(SakaiAccessTokenService.PROPERTY_PUBLIC, null)).thenReturn(null);
        when(serverConfigurationService.getString(SakaiAccessTokenService.PROPERTY_PRIVATE, null)).thenReturn(null);

        //assertTrue(service.isSigningKeyAvailable());

        toolKeyPair = LTI13Util.generateKeyPair();
        String keysetJson = LTI13KeySetUtil.getKeySetJSON((RSAPublicKey) toolKeyPair.getPublic());
        keysetServer = HttpServer.create(new InetSocketAddress(0), 0);
        keysetServer.createContext("/keyset", exchange -> {
            byte[] body = keysetJson.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        keysetServer.start();
        keysetUrl = "http://localhost:" + keysetServer.getAddress().getPort() + "/keyset";
    }

    @After
    public void tearDown() {
        if (keysetServer != null) {
            keysetServer.stop(0);
        }
    }

    /*
    @Test
    public void initLoadsSigningKeyFromProperties() {
        KeyPair configured = LTI13Util.generateKeyPair();
        String pub = LTI13Util.getPublicB64(configured);
        String priv = LTI13Util.getPrivateB64(configured);

        when(serverConfigurationService.getString(SakaiAccessTokenService.PROPERTY_PUBLIC, null)).thenReturn(pub);
        when(serverConfigurationService.getString(SakaiAccessTokenService.PROPERTY_PRIVATE, null)).thenReturn(priv);

        //SakaiAccessTokenServiceImpl configuredService = new SakaiAccessTokenServiceImpl();
        //configuredService.setServerConfigurationService(serverConfigurationService);
        //configuredService.setCacheManager(cacheManager);
        //configuredService.init();

        //assertTrue(sakaiAccessconfiguredService.isSigningKeyAvailable());
        assertTrue(service.isSigningKeyAvailable());
        assertEquals(configured.getPublic(), service.getVerificationKey());
    }
    */

    /*
    @Test
    public void initGeneratesAndCachesSigningKey() {
        assertTrue(cacheManager.getCache(SakaiAccessTokenService.SAT_CACHE_NAME).get("key::public") != null);
        assertTrue(cacheManager.getCache(SakaiAccessTokenService.SAT_CACHE_NAME).get("key::private") != null);
    }
    */

    @Test
    public void extractBearerTokenParsesHeader() throws Exception {
        SakaiAccessToken sat = sampleSat();
        String jws = service.sign(sat);
        String token = service.extractBearerToken("Bearer " + jws);
        assertEquals(jws, token);
    }

    @Test(expected = SakaiAccessTokenException.class)
    public void extractBearerTokenRejectsMissingHeader() throws Exception {
        service.extractBearerToken(null);
    }

    @Test(expected = SakaiAccessTokenException.class)
    public void extractBearerTokenRejectsMalformedHeader() throws Exception {
        service.extractBearerToken("Bearer");
    }

    @Test
    public void signAndValidateRoundTrip() throws Exception {
        SakaiAccessToken sat = sampleSat();
        sat.site_id = "site-abc";
        String jws = service.sign(sat);

        SakaiAccessToken parsed = service.validateToken(jws);
        assertEquals(sat.tool_id, parsed.tool_id);
        assertEquals(sat.site_id, parsed.site_id);
        assertTrue(parsed.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY));
    }

    @Test(expected = SakaiAccessTokenException.class)
    public void validateTokenRejectsBadSignature() throws Exception {
        SakaiAccessToken sat = sampleSat();
        String jws = service.sign(sat);
        service.validateToken(jws + "x");
    }

    @Test(expected = SakaiAccessTokenException.class)
    public void validateTokenRequiresCoreClaims() throws Exception {
        SakaiAccessToken incomplete = new SakaiAccessToken();
        incomplete.tool_id = TOOL_ID;
        String jws = service.sign(incomplete);
        service.validateToken(jws);
    }

    @Test
    public void issueAccessTokenGrantsLineitemReadonlyScope() throws Exception {
        Long toolId = insertTool(true, true, true);

        String audience = buildAudience(toolId);

        Map clientParams = LTI13AccessTokenUtil.getClientAssertion(
                new String[] { LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY },
                toolKeyPair, "client-1", "deployment-1", audience, null);
        String assertion = (String) clientParams.get(ClientAssertion.CLIENT_ASSERTION);
        String scope = (String) clientParams.get(ClientAssertion.SCOPE);

        AccessToken at = service.issueAccessToken(toolId, assertion, scope);
        assertNotNull(at.access_token);
        assertTrue(at.scope.contains(LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY));

        SakaiAccessToken sat = service.validateToken(at.access_token);
        assertEquals(Long.valueOf(toolId), sat.tool_id);
        assertTrue(sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY));
    }

    @Test
    public void issueAccessTokenLineitemReadonlyDoesNotGrantLineitemScope() throws Exception {
        Long toolId = insertTool(true, true, true);

        String audience = buildAudience(toolId);

        Map clientParams = LTI13AccessTokenUtil.getClientAssertion(
                new String[] { LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY },
                toolKeyPair, "client-1", "deployment-1", audience, null);
        String assertion = (String) clientParams.get(ClientAssertion.CLIENT_ASSERTION);
        String scope = (String) clientParams.get(ClientAssertion.SCOPE);

        AccessToken at = service.issueAccessToken(toolId, assertion, scope);
        assertNotNull(at.access_token);
        Set<String> grantedScopes = new HashSet<>(Arrays.asList(at.scope.split("\\s+")));
        assertTrue(grantedScopes.contains(LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY));
        assertFalse(grantedScopes.contains(LTI13ConstantsUtil.SCOPE_LINEITEM));

        SakaiAccessToken sat = service.validateToken(at.access_token);
        assertTrue(sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY));
        assertFalse(sat.hasScope(SakaiAccessToken.SCOPE_LINEITEMS));
    }

    @Test
    public void issueAccessTokenRejectsNoGrantedScopes() throws Exception {
        Long toolId = insertTool(true, true, true);

        String audience = buildAudience(toolId);

        Map clientParams = LTI13AccessTokenUtil.getClientAssertion(
                new String[] { LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY },
                toolKeyPair, "client-1", "deployment-1", audience, null);
        String assertion = (String) clientParams.get(ClientAssertion.CLIENT_ASSERTION);

        try {
            service.issueAccessToken(toolId, assertion, "https://example.org/unknown/scope");
            fail("Expected invalid_scope");
        } catch (SakaiAccessTokenException e) {
            assertEquals("invalid_scope", e.getErrorKey());
        }
    }

    @Test
    public void issueAccessTokenRejectsDisallowedScope() throws Exception {
        Long toolId = insertTool(false, true, true);

        String audience = buildAudience(toolId);

        Map clientParams = LTI13AccessTokenUtil.getClientAssertion(
                new String[] { LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY },
                toolKeyPair, "client-1", "deployment-1", audience, null);
        String assertion = (String) clientParams.get(ClientAssertion.CLIENT_ASSERTION);
        String scope = (String) clientParams.get(ClientAssertion.SCOPE);

        try {
            service.issueAccessToken(toolId, assertion, scope);
            fail("Expected invalid_scope");
        } catch (SakaiAccessTokenException e) {
            assertEquals("invalid_scope", e.getErrorKey());
        }
    }

    @Test
    public void issueAccessTokenRejectsMissingTool() throws Exception {
        Map clientParams = LTI13AccessTokenUtil.getClientAssertion(
                new String[] { LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY },
                toolKeyPair, "client-1", "deployment-1", null, null);
        String assertion = (String) clientParams.get(ClientAssertion.CLIENT_ASSERTION);
        String scope = (String) clientParams.get(ClientAssertion.SCOPE);
        try {
            service.issueAccessToken(99L, assertion, scope);
            fail("Expected invalid_tool");
        } catch (SakaiAccessTokenException e) {
            assertEquals("invalid_tool", e.getErrorKey());
        }
    }

    @Test
    public void issueAccessTokenGrantsLtiApiScopeWhenPermissionGranted() throws Exception {
        Long toolId = insertTool(false, false, false);

        when(siteService.allowUpdateSite(LTIService.ADMIN_SITE)).thenReturn(true);
        when(functionManager.getRegisteredFunctions()).thenReturn(List.of("content.read"));

		ltiService.setToolPermissions(toolId, Set.of("content.read"), LTIService.ADMIN_SITE);

        when(serverConfigurationService.getBoolean(LTIService.PROPERTY_WEBAPI_ENABLED, LTIService.PROPERTY_WEBAPI_ENABLED_DEFAULT))
          .thenReturn(true);

        String audience = buildAudience(toolId);

        Map clientParams = LTI13AccessTokenUtil.getClientAssertion(
                new String[] { LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY },
                toolKeyPair, "client-1", "deployment-1", audience, null);
        String assertion = (String) clientParams.get(ClientAssertion.CLIENT_ASSERTION);
        String scope = SakaiAccessToken.permissionToLtiApiScope("content.read");

        AccessToken at = service.issueAccessToken(toolId, assertion, scope);
        assertNotNull(at.access_token);
        assertTrue(at.scope.contains(scope));

        SakaiAccessToken sat = service.validateToken(at.access_token);
        assertTrue(sat.hasScope(scope));
    }

    @Test
    public void issueAccessTokenRejectsLtiApiScopeWhenPermissionNotGranted() throws Exception {
        Long toolId = insertTool(false, false, false);

        String audience = buildAudience(toolId);

        Map clientParams = LTI13AccessTokenUtil.getClientAssertion(
                new String[] { LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY },
                toolKeyPair, "client-1", "deployment-1", audience, null);
        String assertion = (String) clientParams.get(ClientAssertion.CLIENT_ASSERTION);
        String scope = SakaiAccessToken.permissionToLtiApiScope("content.read");

        try {
            service.issueAccessToken(toolId, assertion, scope);
            fail("Expected invalid_scope");
        } catch (SakaiAccessTokenException e) {
            assertEquals("invalid_scope", e.getErrorKey());
        }
    }

    @Test
    public void issueAccessTokenRejectsMissingClientAssertion() throws Exception {
        try {
            service.issueAccessToken(TOOL_ID, null, LTI13ConstantsUtil.SCOPE_SCORE);
            fail("Expected invalid_request");
        } catch (SakaiAccessTokenException e) {
            assertEquals("invalid_request", e.getErrorKey());
        }
    }

    @Test
    public void isLtiBearerWebApiEnabledDefaultsTrue() {
        when(serverConfigurationService.getBoolean(LTIService.PROPERTY_WEBAPI_ENABLED,
                LTIService.PROPERTY_WEBAPI_ENABLED_DEFAULT))
                .thenReturn(true);
        assertTrue(service.isWebApiEnabled());
    }

    @Test
    public void isLtiBearerWebApiEnabledWhenDisabled() {
        when(serverConfigurationService.getBoolean(LTIService.PROPERTY_WEBAPI_ENABLED,
                LTIService.PROPERTY_WEBAPI_ENABLED_DEFAULT))
                .thenReturn(false);
        assertFalse(service.isWebApiEnabled());
    }

    @Test
    public void isLtiBearerDirectEnabledDefaultsTrue() {
        when(serverConfigurationService.getBoolean(LTIService.PROPERTY_DIRECTAPI_ENABLED,
                LTIService.PROPERTY_DIRECTAPI_ENABLED_DEFAULT))
                .thenReturn(true);
        assertTrue(service.isDirectApiEnabled());
    }

    @Test
    public void isLtiBearerDirectEnabledWhenDisabled() {
        when(serverConfigurationService.getBoolean(LTIService.PROPERTY_DIRECTAPI_ENABLED,
                LTIService.PROPERTY_DIRECTAPI_ENABLED_DEFAULT))
                .thenReturn(false);
        assertFalse(service.isDirectApiEnabled());
    }

    private String buildAudience(Long toolId) {
        return getOurServerUrl() + LTI13_PATH + "token/" + toolId;
    }

    private SakaiAccessToken sampleSat() {
        SakaiAccessToken sat = new SakaiAccessToken();
        sat.tool_id = TOOL_ID;
        sat.expires = Long.valueOf(System.currentTimeMillis() / 1000L + 3600L);
        sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
        return sat;
    }

    private Long insertTool(boolean lineItems, boolean outcomes, boolean roster) {

        Map<String, Object> props = new HashMap<>();
        props.put(LTIService.LTI_TITLE, "Nova");
		props.put(LTIService.LTI13_CLIENT_ID, "client-1");
        props.put(LTIService.LTI_DESCRIPTION, "A Nova thing");
        props.put(LTIService.LTI13_TOOL_KEYSET, keysetUrl);
        props.put(LTIService.LTI_ALLOWLINEITEMS, lineItems ? 1 : 0);
        props.put(LTIService.LTI_ALLOWOUTCOMES, outcomes ? 1 : 0);
        props.put(LTIService.LTI_ALLOWROSTER, roster ? 1 : 0);
        props.put(LTIService.LTI_LAUNCH, "http://foo.com/launch");

        return (Long) ltiService.insertTool(props, "site1");
    }
}
