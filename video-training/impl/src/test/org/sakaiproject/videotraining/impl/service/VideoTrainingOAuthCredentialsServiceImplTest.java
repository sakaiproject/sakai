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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sakaiproject.videotraining.api.model.VideoProviderType;
import org.sakaiproject.videotraining.api.model.VideoTrainingOAuthCredentials;
import org.sakaiproject.videotraining.api.repository.VideoTrainingOAuthCredentialsRepository;

public class VideoTrainingOAuthCredentialsServiceImplTest {

    private static final String VALID_KEY = "0123456789abcdef0123456789abcdef";

    private VideoTrainingOAuthCredentialsServiceImpl service;
    private VideoTrainingOAuthCredentialsRepository credentialsRepository;

    @Before
    public void setUp() {
        credentialsRepository = Mockito.mock(VideoTrainingOAuthCredentialsRepository.class);
        service = new VideoTrainingOAuthCredentialsServiceImpl();
        service.setCredentialsRepository(credentialsRepository);
        service.setEncryptionKey(VALID_KEY);
    }

    @Test
    public void saveAndGetCredentialsShouldEncryptAndDecryptRoundTrip() {
        AtomicReference<VideoTrainingOAuthCredentials> store = new AtomicReference<>();

        when(credentialsRepository.findByProviderType(VideoProviderType.YOUTUBE_UPLOAD))
            .thenAnswer(invocation -> Optional.ofNullable(store.get()));
        when(credentialsRepository.save(any(VideoTrainingOAuthCredentials.class))).thenAnswer(invocation -> {
            VideoTrainingOAuthCredentials value = invocation.getArgument(0);
            if (value.getId() == null) {
                value.setId("cred-1");
                value.setCreatedOn(Instant.now());
            }
            value.setModifiedOn(Instant.now());
            store.set(value);
            return value;
        });

        VideoTrainingOAuthCredentials saved = service.saveCredentials(
            VideoProviderType.YOUTUBE_UPLOAD,
            "client-id",
            "api-key",
            "client-secret-value",
            "refresh-token-value");

        assertNotNull(saved.getClientSecret());
        assertNotNull(saved.getRefreshToken());
        assertNotEquals("client-secret-value", saved.getClientSecret());
        assertNotEquals("refresh-token-value", saved.getRefreshToken());

        VideoTrainingOAuthCredentials readBack = service.getCredentials(VideoProviderType.YOUTUBE_UPLOAD).orElse(null);
        assertNotNull(readBack);
        assertEquals("client-id", readBack.getClientId());
        assertEquals("api-key", readBack.getApiKey());
        assertEquals("client-secret-value", readBack.getClientSecret());
        assertEquals("refresh-token-value", readBack.getRefreshToken());
    }

    @Test
    public void saveCredentialsShouldUseRandomIvSoRepeatedSecretsProduceDifferentCiphertext() {
        ArgumentCaptor<VideoTrainingOAuthCredentials> captor = ArgumentCaptor.forClass(VideoTrainingOAuthCredentials.class);

        when(credentialsRepository.findByProviderType(VideoProviderType.YOUTUBE_UPLOAD)).thenReturn(Optional.empty());
        when(credentialsRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveCredentials(VideoProviderType.YOUTUBE_UPLOAD, "client-id", "api-key", "same-plaintext", "refresh-token-value");
        service.saveCredentials(VideoProviderType.YOUTUBE_UPLOAD, "client-id", "api-key", "same-plaintext", "refresh-token-value");

        List<VideoTrainingOAuthCredentials> savedValues = captor.getAllValues();
        assertEquals(2, savedValues.size());

        String first = savedValues.get(0).getClientSecret();
        String second = savedValues.get(1).getClientSecret();

        assertNotNull(first);
        assertNotNull(second);
        assertNotEquals(first, second);
    }

    @Test
    public void saveCredentialsShouldPreserveNullSecrets() {
        AtomicReference<VideoTrainingOAuthCredentials> store = new AtomicReference<>();

        when(credentialsRepository.findByProviderType(VideoProviderType.YOUTUBE_UPLOAD))
            .thenAnswer(invocation -> Optional.ofNullable(store.get()));
        when(credentialsRepository.save(any(VideoTrainingOAuthCredentials.class))).thenAnswer(invocation -> {
            VideoTrainingOAuthCredentials value = invocation.getArgument(0);
            if (value.getId() == null) {
                value.setId("cred-2");
            }
            store.set(value);
            return value;
        });

        VideoTrainingOAuthCredentials saved = service.saveCredentials(
            VideoProviderType.YOUTUBE_UPLOAD,
            "client-id",
            "api-key",
            null,
            null);

        assertNull(saved.getClientSecret());
        assertNull(saved.getRefreshToken());

        VideoTrainingOAuthCredentials readBack = service.getCredentials(VideoProviderType.YOUTUBE_UPLOAD).orElse(null);
        assertNotNull(readBack);
        assertNull(readBack.getClientSecret());
        assertNull(readBack.getRefreshToken());
    }

    @Test
    public void getCredentialsShouldReturnNullForMalformedCipherText() {
        VideoTrainingOAuthCredentials stored = new VideoTrainingOAuthCredentials();
        stored.setId("cred-3");
        stored.setProviderType(VideoProviderType.YOUTUBE_UPLOAD);
        stored.setClientId("client-id");
        stored.setApiKey("api-key");
        stored.setClientSecret("this-is-not-base64");
        stored.setRefreshToken("AQID");

        when(credentialsRepository.findByProviderType(VideoProviderType.YOUTUBE_UPLOAD)).thenReturn(Optional.of(stored));

        VideoTrainingOAuthCredentials readBack = service.getCredentials(VideoProviderType.YOUTUBE_UPLOAD).orElse(null);
        assertNotNull(readBack);
        assertNull(readBack.getClientSecret());
        assertNull(readBack.getRefreshToken());
    }

    @Test
    public void saveCredentialsShouldReturnNullCipherFieldsWhenKeyLengthIsInvalid() {
        when(credentialsRepository.findByProviderType(VideoProviderType.YOUTUBE_UPLOAD)).thenReturn(Optional.empty());
        when(credentialsRepository.save(any(VideoTrainingOAuthCredentials.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.setEncryptionKey("short-key");

        VideoTrainingOAuthCredentials saved = service.saveCredentials(
            VideoProviderType.YOUTUBE_UPLOAD,
            "client-id",
            "api-key",
            "client-secret-value",
            "refresh-token-value");

        assertNull(saved.getClientSecret());
        assertNull(saved.getRefreshToken());
        assertFalse(service.isConfigured(VideoProviderType.YOUTUBE_UPLOAD));
    }

    @Test
    public void getCredentialsShouldReturnEmptyWhenProviderTypeIsNull() {
        assertTrue(service.getCredentials(null).isEmpty());
    }
}