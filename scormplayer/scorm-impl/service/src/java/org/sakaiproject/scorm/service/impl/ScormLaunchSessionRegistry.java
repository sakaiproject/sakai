/*
 * Copyright (c) 2025 The Apereo Foundation
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
package org.sakaiproject.scorm.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;

/**
 * In-memory registry holding active SCORM launch sessions.
 * Each launch session is scoped to a Sakai user and retains the associated {@link SessionBean}.
 */
public class ScormLaunchSessionRegistry
{
    private final Map<String, Entry> sessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public String register(SessionBean sessionBean, ContentPackage contentPackage, String userId)
    {
        Objects.requireNonNull(sessionBean, "SessionBean must not be null");
        Objects.requireNonNull(contentPackage, "ContentPackage must not be null");
        Objects.requireNonNull(userId, "User id must not be null");

        String sessionId = generateSessionId();
        sessions.put(sessionId, new Entry(sessionBean, contentPackage, userId));
        return sessionId;
    }

    public Optional<Entry> lookup(String sessionId)
    {
        return Optional.ofNullable(sessionId).map(sessions::get);
    }

    public void remove(String sessionId)
    {
        if (sessionId != null)
        {
            sessions.remove(sessionId);
        }
    }

    private String generateSessionId()
    {
        byte[] random = new byte[24];
        secureRandom.nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    @Getter
    public static final class Entry
    {
        private final SessionBean sessionBean;
        private final ContentPackage contentPackage;
        private final String userId;
        private final Instant createdAt;

        private Entry(SessionBean sessionBean, ContentPackage contentPackage, String userId)
        {
            this.sessionBean = sessionBean;
            this.contentPackage = contentPackage;
            this.userId = userId;
            this.createdAt = Instant.now();
        }
    }
}
