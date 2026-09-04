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

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.lti.api.SakaiAccessTokenException;
import org.sakaiproject.lti.api.SakaiAccessTokenService;
import org.sakaiproject.lti.beans.LtiToolBean;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.lti13.util.LTI13TokenRequestValidator;
import org.sakaiproject.lti13.util.SakaiAccessToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import org.tsugi.jackson.JacksonUtil;
import org.tsugi.lti.LTIUtil;
import org.tsugi.lti13.LTI13ConstantsUtil;
import org.tsugi.lti13.LTI13JwtUtil;
import org.tsugi.lti13.LTI13Util;
import org.tsugi.oauth2.objects.AccessToken;

import org.apache.commons.lang3.StringUtils;

import static org.sakaiproject.lti.util.SakaiLTIUtil.getOurServerUrl;
import static org.sakaiproject.lti.util.SakaiLTIUtil.LTI13_PATH;

/**
 * Central SAT signing key lifecycle (sakai.properties, then Ignite) and token issue/validate.
 */
@Slf4j
public class SakaiAccessTokenServiceImpl implements SakaiAccessTokenService {

    private static final String CACHE_PUBLIC = "key::public";
    private static final String CACHE_PRIVATE = "key::private";
    private static final String CLIENT_ASSERTION_REPLAY_CACHE_NAME = "org.sakaiproject.lti13.ClientAssertionReplay_cache";

    @Autowired
    private LTIService ltiService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private CacheManager cacheManager;

    private volatile KeyPair tokenKeyPair;
    private volatile Cache cache;
    private volatile Cache clientAssertionReplayCache;

    private ObjectWriter jsonWriter;
    private ObjectReader jsonReader;

	private Object replayCacheMutex = new Object();

    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        jsonWriter = mapper.writerWithDefaultPrettyPrinter();
        jsonReader = mapper.reader();
    }

    public void destroy() {
        tokenKeyPair = null;
        cache = null;
        clientAssertionReplayCache = null;
    }

    @Override
    public boolean isWebApiEnabled() {
        return ltiService.isWebApiEnabled();
    }

    @Override
    public boolean isDirectApiEnabled() {
        return ltiService.isDirectApiEnabled();
    }

    @Override
    public boolean isSigningKeyAvailable() {
        loadOrCreateSigningKeyPair();
        return tokenKeyPair != null;
    }

    @Override
    public Key getVerificationKey() {
        loadOrCreateSigningKeyPair();
        if (tokenKeyPair == null) {
            return null;
        }
        return tokenKeyPair.getPublic();
    }

    @Override
    public boolean isValidBearerHeader(String header) {
        return header != null && header.toLowerCase().startsWith("bearer ");
    }

    @Override
    public String extractBearerToken(String authorizationHeader) throws SakaiAccessTokenException {
        if (!isValidBearerHeader(authorizationHeader)) {
            throw new SakaiAccessTokenException("invalid_authorization", "Missing or invalid Authorization header");
        }
        String[] parts = authorizationHeader.split("\\s+");
        if (parts.length != 2 || parts[1].isEmpty()) {
            throw new SakaiAccessTokenException("invalid_authorization", "Malformed Bearer token");
        }
        return parts[1];
    }

    @Override
    public SakaiAccessToken validateToken(String jws) throws SakaiAccessTokenException {
        loadOrCreateSigningKeyPair();
        if (tokenKeyPair == null) {
            throw new SakaiAccessTokenException("no_token_key", "No token signing key available");
        }
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(tokenKeyPair.getPublic()).parseClaimsJws(jws).getBody();
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException
                | io.jsonwebtoken.security.SignatureException | IllegalArgumentException e) {
            String tokenHash = jws != null ? LTI13Util.sha256(jws) : "[none]";
            log.error("{} Signature error {} tokenHash={}", e.getClass().getName(), e.getMessage(), tokenHash, e);
            throw new SakaiAccessTokenException("signature_error", e.getMessage(), e);
        }

        try {
            String jsonResult = jsonWriter.writeValueAsString(claims);
            SakaiAccessToken sat = jsonReader.readValue(jsonResult, SakaiAccessToken.class);
            long nowSeconds = System.currentTimeMillis() / 1000L;
            if (sat.tool_id != null && sat.scope != null && (sat.expires != null && sat.expires.longValue() > nowSeconds)) {
                return sat;
            }
            log.error("SakaiAccessToken missing required data {}", sat);
            throw new SakaiAccessTokenException("missing_required_data", "Missing required data in access_token");
        } catch (IOException ex) {
            log.error("PARSE ERROR {}", claims, ex);
            throw new SakaiAccessTokenException("token_parse_failure", ex.getMessage(), ex);
        }
    }

    @Override
    public String sign(SakaiAccessToken sat) throws SakaiAccessTokenException {
        loadOrCreateSigningKeyPair();
        if (tokenKeyPair == null) {
            throw new SakaiAccessTokenException("no_token_key", "No token signing key available");
        }
        String payload = JacksonUtil.toString(sat);
        return Jwts.builder().setPayload(payload).signWith(tokenKeyPair.getPrivate()).compact();
    }

    @Override
    public AccessToken issueAccessToken(long toolId, String clientAssertion, String requestedScope)
            throws SakaiAccessTokenException {

        loadOrCreateSigningKeyPair();
        if (tokenKeyPair == null) {
            throw new SakaiAccessTokenException("no_token_key", "No token key available to sign tokens");
        }
        if (StringUtils.isBlank(clientAssertion)) {
            throw new SakaiAccessTokenException("invalid_request", "Missing client_assertion");
        }
        if (StringUtils.isBlank(requestedScope)) {
            throw new SakaiAccessTokenException("invalid_request", "Missing scope");
        }

        if (LTI13JwtUtil.rawJwtBody(clientAssertion) == null) {
            throw new SakaiAccessTokenException("invalid_client_assertion", "Could not find Jwt Body in client_assertion");
        }
        if (LTI13JwtUtil.jsonJwtHeader(clientAssertion) == null) {
            throw new SakaiAccessTokenException("invalid_client_assertion", "Could not parse Jwt Header in client_assertion");
        }

        if (toolId < 1) {
            throw new SakaiAccessTokenException("invalid_tool", "Invalid tool key");
        }

        LtiToolBean tool = ltiService.getToolDaoAsBean(toolId, null, true);
        if (tool == null) {
            throw new SakaiAccessTokenException("invalid_tool", "Could not load tool");
        }

        Key toolPublicKey;
        try {
            toolPublicKey = SakaiLTIUtil.getPublicKey(tool, clientAssertion);
        } catch (Exception e) {
            log.error("Error getting public key for tool {}", toolId, e);
            throw new SakaiAccessTokenException("public_key_error", "Public key retrieval failed", e);
        }

        Jws<Claims> claims = null;
        try {
            claims = Jwts.parser().setAllowedClockSkewSeconds(60)
                    .setSigningKey(toolPublicKey).parseClaimsJws(clientAssertion);
            if (claims == null) {
                throw new SakaiAccessTokenException("invalid_client_assertion", "Could not verify signature");
            }
        } catch (Exception e) {
            log.error("Could not verify client_assertion for tool {}", toolId, e);
            throw new SakaiAccessTokenException("invalid_client_assertion", "Could not verify signature", e);
        }

        Long toolKey = LTIUtil.toLongKey(toolId);
        String tokenAudience = getOurServerUrl() + LTI13_PATH + "token/" + toolKey;
        String claimError = LTI13TokenRequestValidator.validateClientAssertionClaims(claims.getBody(), tool.lti13ClientId, tokenAudience);
        if (claimError != null) {
            log.error("Invalid client_assertion for tool {}: {}", toolId, claimError);
            throw new SakaiAccessTokenException("invalid_client", claimError);
        }

        synchronized (replayCacheMutex) {
            if (clientAssertionReplayCache == null) {
                try {
                    clientAssertionReplayCache = cacheManager.getCache(CLIENT_ASSERTION_REPLAY_CACHE_NAME);
                } catch (RuntimeException ex) {
                    log.warn("Client assertion replay cache {} unavailable.", CLIENT_ASSERTION_REPLAY_CACHE_NAME, ex);
                }
            }
        }

        LTI13TokenRequestValidator.ClientAssertionReplayResult replayResult =
            LTI13TokenRequestValidator.validateClientAssertionReplay(clientAssertionReplayCache, claims.getBody(), tool.lti13ClientId);
        if (replayResult == LTI13TokenRequestValidator.ClientAssertionReplayResult.CACHE_UNAVAILABLE) {
            log.warn("Client assertion replay cache unavailable for tool {} client {}; continuing without replay cache", toolId, tool.lti13ClientId);
        } else if (replayResult == LTI13TokenRequestValidator.ClientAssertionReplayResult.REPLAYED) {
            log.error("Invalid client_assertion replay state for tool {}: {}", toolId, replayResult.getClientMessage());
            throw new SakaiAccessTokenException("invalid_client", replayResult.getClientMessage());
        }

        String scope = requestedScope.toLowerCase();

        final Set<String> requestedScopes = Arrays.stream(requestedScope.split("\\s+"))
            .filter(t -> !t.isEmpty()).collect(Collectors.toSet());

        SakaiAccessToken sat = new SakaiAccessToken();
        sat.tool_id = toolId;

        Set<String> returnScopeSet = new HashSet<>();

        if (requestedScopes.contains(LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY)) {
            if (!Boolean.TRUE.equals(tool.allowlineitems)) {
                throw new SakaiAccessTokenException("invalid_scope", LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY);
            }
            returnScopeSet.add(LTI13ConstantsUtil.SCOPE_LINEITEM_READONLY);
            sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
        }

        if (requestedScopes.contains(LTI13ConstantsUtil.SCOPE_LINEITEM)) {
            if (!Boolean.TRUE.equals(tool.allowlineitems)) {
                throw new SakaiAccessTokenException("invalid_scope", LTI13ConstantsUtil.SCOPE_LINEITEM);
            }
            returnScopeSet.add(LTI13ConstantsUtil.SCOPE_LINEITEM);
            sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS);
            sat.addScope(SakaiAccessToken.SCOPE_LINEITEMS_READONLY);
        }

        if (requestedScopes.contains(LTI13ConstantsUtil.SCOPE_SCORE)) {
            if (!Boolean.TRUE.equals(tool.allowoutcomes) || !Boolean.TRUE.equals(tool.allowlineitems)) {
                throw new SakaiAccessTokenException("invalid_scope", LTI13ConstantsUtil.SCOPE_SCORE);
            }
            returnScopeSet.add(LTI13ConstantsUtil.SCOPE_SCORE);
            sat.addScope(SakaiAccessToken.SCOPE_SCORE);
        }

        if (requestedScopes.contains(LTI13ConstantsUtil.SCOPE_RESULT_READONLY)) {
            if (!Boolean.TRUE.equals(tool.allowoutcomes) || !Boolean.TRUE.equals(tool.allowlineitems)) {
                throw new SakaiAccessTokenException("invalid_scope", LTI13ConstantsUtil.SCOPE_RESULT_READONLY);
            }
            returnScopeSet.add(LTI13ConstantsUtil.SCOPE_RESULT_READONLY);
            sat.addScope(SakaiAccessToken.SCOPE_RESULT_READONLY);
        }

        if (requestedScopes.contains(LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES)) {
            if (!Boolean.TRUE.equals(tool.allowroster)) {
                throw new SakaiAccessTokenException("invalid_scope", LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES);
            }
            returnScopeSet.add(LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES);
            sat.addScope(SakaiAccessToken.SCOPE_ROSTER);
        }

        if (requestedScopes.contains(LTI13ConstantsUtil.SCOPE_CONTEXTGROUP_READONLY)) {
            if (!Boolean.TRUE.equals(tool.allowroster)) {
                throw new SakaiAccessTokenException("invalid_scope", LTI13ConstantsUtil.SCOPE_CONTEXTGROUP_READONLY);
            }
            returnScopeSet.add(LTI13ConstantsUtil.SCOPE_CONTEXTGROUP_READONLY);
            sat.addScope(SakaiAccessToken.SCOPE_CONTEXTGROUP_READONLY);
        }

        if (ltiService.isApiEnabled()) {
            Set<String> grantedPermissions = new HashSet<>(ltiService.getToolPermissions(toolId));
            for (String ltiApiScope : requestedScopes) {
                String permission = SakaiAccessToken.ltiApiScopeToPermission(ltiApiScope);
                if (permission == null || !grantedPermissions.contains(permission)) {
                    continue;
                }
                returnScopeSet.add(ltiApiScope);
                sat.addScope(ltiApiScope);
            }
        }

        if (returnScopeSet.isEmpty()) {
            throw new SakaiAccessTokenException("invalid_scope", "No scopes were granted");
        }

        AccessToken at = new AccessToken();
        at.access_token = sign(sat);
        at.scope = String.join(" ", new ArrayList<String>(returnScopeSet));
        return at;
    }

    /**
     * Ignite is started after some component {@code init} methods run; obtain the cache on first use.
     */
    private Cache resolveIgniteCache(String name) {
        if (cache != null) {
            return cache;
        }
        if (cacheManager == null) {
            return null;
        }

        try {
            cache = cacheManager.getCache(name);
            return cache;
        } catch (RuntimeException ex) {
            log.warn("Ignite cache unavailable for cache {}: Exception: {}", name, ex);
            return null;
        }
    }

    private synchronized void loadOrCreateSigningKeyPair() {
        if (tokenKeyPair != null) {
            return;
        }

        String publicB64 = serverConfigurationService.getString(PROPERTY_PUBLIC, null);
        String privateB64 = serverConfigurationService.getString(PROPERTY_PRIVATE, null);
        if (publicB64 != null && privateB64 != null) {
            tokenKeyPair = LTI13Util.strings2KeyPair(publicB64, privateB64);
            if (tokenKeyPair == null) {
                log.error("Could not load tokenKeyPair from sakai.properties");
            } else {
                log.info("Loaded SAT signing key from sakai.properties");
            }
        }

        Cache igniteCache = resolveIgniteCache(SAT_CACHE_NAME);
        if (tokenKeyPair == null && igniteCache != null) {
            Cache.ValueWrapper cachedPublic = igniteCache.get(CACHE_PUBLIC);
            Cache.ValueWrapper cachedPrivate = igniteCache.get(CACHE_PRIVATE);
            if (cachedPublic != null && cachedPrivate != null) {
                tokenKeyPair = LTI13Util.strings2KeyPair((String) cachedPublic.get(), (String) cachedPrivate.get());
                if (tokenKeyPair == null) {
                    log.error("Could not parse tokenKeyPair from Ignite cache");
                } else {
                    log.info("Loaded SAT signing key from Ignite cache");
                }
            }
        }

        if (tokenKeyPair == null) {
            try {
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                tokenKeyPair = keyGen.genKeyPair();
                String publicB64Out = LTI13Util.getPublicB64(tokenKeyPair);
                String privateB64Out = LTI13Util.getPrivateB64(tokenKeyPair);
                igniteCache = resolveIgniteCache(SAT_CACHE_NAME);
                if (igniteCache != null) {
                    Cache.ValueWrapper cachedPublic = igniteCache.putIfAbsent(CACHE_PUBLIC, publicB64Out);
                    Cache.ValueWrapper cachedPrivate = igniteCache.putIfAbsent(CACHE_PRIVATE, privateB64Out);
                    if (cachedPublic != null || cachedPrivate != null) {
                        // Someone else already holds (part of) the key pair; adopt the persisted winner.
                        Cache.ValueWrapper winningPublic = igniteCache.get(CACHE_PUBLIC);
                        Cache.ValueWrapper winningPrivate = igniteCache.get(CACHE_PRIVATE);
                        if (winningPublic != null && winningPrivate != null) {
                            tokenKeyPair = LTI13Util.strings2KeyPair(
                                (String) winningPublic.get(), (String) winningPrivate.get());
                        }
                    }
                    log.info("Generated SAT signing key and stored in Ignite cache");
                } else {
                    log.info("Generated SAT signing key (Ignite cache not available to persist)");
                }
            } catch (NoSuchAlgorithmException ex) {
                log.error("Unable to generate tokenKeyPair", ex);
            }
        }
    }
}
