/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.videotraining.impl.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.videotraining.api.VideoTrainingConstants;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingOAuthCredentials;
import org.sakaiproject.videotraining.api.repository.VideoTrainingOAuthCredentialsRepository;
import org.sakaiproject.videotraining.api.service.VideoTrainingOAuthCredentialsService;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class VideoTrainingOAuthCredentialsServiceImpl implements VideoTrainingOAuthCredentialsService {

    @Setter
    private VideoTrainingOAuthCredentialsRepository credentialsRepository;

    @Setter
    private ServerConfigurationService serverConfigurationService;

    @Setter(AccessLevel.PACKAGE)
    private String encryptionKey;

    @Override
    public List<VideoTrainingOAuthCredentials> getAllCredentials() {
        return credentialsRepository.findAllByOrderByProviderTypeAsc();
    }

    @Override
    public Optional<VideoTrainingOAuthCredentials> getCredentials(VideoProviderType providerType) {
        if (providerType == null) {
            return Optional.empty();
        }

        return credentialsRepository.findByProviderType(providerType).map(this::decryptSecretIfNeeded);
    }

    @Override
    public VideoTrainingOAuthCredentials saveCredentials(VideoProviderType providerType, String clientId, String apiKey, String clientSecret, String refreshToken) {
        if (providerType == null) {
            throw new IllegalArgumentException("providerType must not be null");
        }

        VideoTrainingOAuthCredentials credentials = credentialsRepository.findByProviderType(providerType)
                .orElseGet(VideoTrainingOAuthCredentials::new);
        credentials.setProviderType(providerType);
        String normalizedClientId = StringUtils.trimToNull(clientId);
        String normalizedApiKey = StringUtils.trimToNull(apiKey);
        String normalizedClientSecret = StringUtils.trimToNull(clientSecret);
        String normalizedRefreshToken = StringUtils.trimToNull(refreshToken);

        if (normalizedClientId != null || credentials.getId() == null) {
            credentials.setClientId(normalizedClientId);
        }
        if (normalizedApiKey != null || credentials.getId() == null) {
            credentials.setApiKey(normalizedApiKey);
        }
        if (normalizedClientSecret != null || credentials.getId() == null) {
            credentials.setClientSecret(encrypt(normalizedClientSecret));
        }
        if (normalizedRefreshToken != null || credentials.getId() == null) {
            credentials.setRefreshToken(encrypt(normalizedRefreshToken));
        }
        if (credentials.getCreatedOn() == null) {
            credentials.setCreatedOn(Instant.now());
        }
        credentials.setModifiedOn(Instant.now());
        return credentialsRepository.save(credentials);
    }

    @Override
    public boolean isConfigured(VideoProviderType providerType) {
        return getCredentials(providerType)
            .map(credentials -> StringUtils.isNotBlank(credentials.getClientId())
                    && StringUtils.isNotBlank(credentials.getClientSecret())
                    && StringUtils.isNotBlank(credentials.getRefreshToken()))
            .orElse(false);
    }

    @Override
    public boolean isAuthorizationReady(VideoProviderType providerType) {
        return getCredentials(providerType)
                .map(credentials -> StringUtils.isNotBlank(credentials.getClientId()) && StringUtils.isNotBlank(credentials.getClientSecret()))
                .orElse(false);
    }

    @Override
    public boolean isYoutubeMetadataConfigured() {
        return StringUtils.isNotBlank(serverConfigurationService.getString(
            VideoTrainingConstants.PROVIDER_YOUTUBE_API_KEY_PROPERTY,
            VideoTrainingConstants.DEFAULT_PROVIDER_YOUTUBE_API_KEY));
    }

    @Override
    public VideoTrainingOAuthCredentials loadCredentialsForForm(VideoProviderType providerType) {
        return getCredentials(providerType).orElseGet(() -> {
            VideoTrainingOAuthCredentials credentials = new VideoTrainingOAuthCredentials();
            credentials.setProviderType(providerType);
            return credentials;
        });
    }

    @Override
    public String buildYoutubeAuthorizationUrl(String redirectUri, String state) {
        VideoTrainingOAuthCredentials credentials = getCredentials(VideoProviderType.YOUTUBE_UPLOAD)
                .orElseThrow(() -> new IllegalStateException("YouTube OAuth credentials are not configured"));

        StringBuilder builder = new StringBuilder("https://accounts.google.com/o/oauth2/v2/auth");
        builder.append("?client_id=").append(urlEncode(credentials.getClientId()));
        builder.append("&redirect_uri=").append(urlEncode(redirectUri));
        builder.append("&response_type=code");
        builder.append("&scope=").append(urlEncode(VideoTrainingConstants.YOUTUBE_UPLOAD_SCOPE));
        builder.append("&access_type=offline");
        builder.append("&prompt=consent");
        builder.append("&include_granted_scopes=true");
        if (StringUtils.isNotBlank(state)) {
            builder.append("&state=").append(urlEncode(state));
        }
        return builder.toString();
    }

    @Override
    public String exchangeYoutubeRefreshToken(String redirectUri, String authorizationCode) throws IOException {
        VideoTrainingOAuthCredentials credentials = getCredentials(VideoProviderType.YOUTUBE_UPLOAD)
                .orElseThrow(() -> new IllegalStateException("YouTube OAuth credentials are not configured"));

        GoogleAuthorizationCodeTokenRequest tokenRequest = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                credentials.getClientId(),
                credentials.getClientSecret(),
                authorizationCode,
                redirectUri);
        GoogleTokenResponse tokenResponse = tokenRequest.execute();
        String refreshToken = StringUtils.trimToNull(tokenResponse.getRefreshToken());
        if (StringUtils.isBlank(refreshToken)) {
            refreshToken = StringUtils.trimToNull(credentials.getRefreshToken());
        }
        if (StringUtils.isBlank(refreshToken)) {
            throw new IllegalStateException("Google did not return a refresh token");
        }
        return refreshToken;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(StringUtils.defaultString(value), StandardCharsets.UTF_8).replace("+", "%20");
    }

    private VideoTrainingOAuthCredentials decryptSecretIfNeeded(VideoTrainingOAuthCredentials credentials) {
        if (credentials == null) {
            return null;
        }

        VideoTrainingOAuthCredentials copy = new VideoTrainingOAuthCredentials();
        copy.setId(credentials.getId());
        copy.setProviderType(credentials.getProviderType());
        copy.setClientId(credentials.getClientId());
        copy.setApiKey(credentials.getApiKey());
        copy.setClientSecret(decrypt(credentials.getClientSecret()));
        copy.setRefreshToken(decrypt(credentials.getRefreshToken()));
        copy.setCreatedOn(credentials.getCreatedOn());
        copy.setModifiedOn(credentials.getModifiedOn());
        return copy;
    }

    private byte[] getKey() {
        String configuredKey = serverConfigurationService != null
                ? serverConfigurationService.getString(VideoTrainingConstants.OAUTH_ENCRYPTION_KEY_PROPERTY, VideoTrainingConstants.DEFAULT_OAUTH_ENCRYPTION_KEY)
                : VideoTrainingConstants.DEFAULT_OAUTH_ENCRYPTION_KEY;
        String key = StringUtils.defaultIfBlank(encryptionKey, configuredKey);
        if (key == null || key.length() != 32) {
            throw new RuntimeException("Property '" + VideoTrainingConstants.OAUTH_ENCRYPTION_KEY_PROPERTY + "' must be defined in sakai.properties and contain 32 ASCII characters for AES-256");
        }
        return key.getBytes(StandardCharsets.UTF_8);
    }

    private String encrypt(String plainText) {
        if (plainText == null) {
            return null;
        }

        try {
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[VideoTrainingConstants.IV_LENGTH];
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(VideoTrainingConstants.ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(VideoTrainingConstants.TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[VideoTrainingConstants.IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, VideoTrainingConstants.IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, VideoTrainingConstants.IV_LENGTH, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("Error encrypting video training credential", e);
            return null;
        }
    }

    private String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            if (decoded.length <= VideoTrainingConstants.IV_LENGTH) {
                return null;
            }

            byte[] iv = new byte[VideoTrainingConstants.IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - VideoTrainingConstants.IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, VideoTrainingConstants.IV_LENGTH);
            System.arraycopy(decoded, VideoTrainingConstants.IV_LENGTH, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(VideoTrainingConstants.ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(getKey(), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(VideoTrainingConstants.TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Error decrypting video training credential", e);
            return null;
        }
    }
}
